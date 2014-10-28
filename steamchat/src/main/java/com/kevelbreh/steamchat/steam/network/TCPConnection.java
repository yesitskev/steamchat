package com.kevelbreh.steamchat.steam.network;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.handler.IHandler;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.network.packet.ChannelEncryptRequest;
import com.kevelbreh.steamchat.steam.network.packet.ChannelEncryptResponse;
import com.kevelbreh.steamchat.steam.network.packet.ChannelEncryptResult;
import com.kevelbreh.steamchat.steam.network.packet.ClientLogOnResponse;
import com.kevelbreh.steamchat.steam.network.packet.HeartBeat;
import com.kevelbreh.steamchat.steam.network.packet.MultiPacket;
import com.kevelbreh.steamchat.steam.network.packet.Packet;
import com.kevelbreh.steamchat.steam.security.Cryptography;
import com.kevelbreh.steamchat.steam.security.NetEncryption;
import com.kevelbreh.steamchat.steam.security.PublicKey;
import com.kevelbreh.steamchat.steam.security.RSA;
import com.kevelbreh.steamchat.steam.util.BinaryReader;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;

/**
 * Steam TCPConnection class connects to the steam content delivery network on a specified IP.
 * Depending on the IP and port, you will arrive on different environment which will have a universe
 * of {@link com.kevelbreh.steamchat.steam.language.Universe} type.
 */
public final class TCPConnection extends Thread {

    public int sessionid;
    public void setSessionId(int value) {
        sessionid = value;
    }
    public long steamid;
    public void setSteamId(long value) {
        steamid = value;
    }

    /**
     * Steam magic number for packets ("VT01")
     */
    final static int MAGIC = 0x31305456;

    /**
     * Cryptography filter for all the steam data.
     */
    private NetEncryption netFilter;
    private byte[] temp_session_key;

    /**
     * Connection socket.
     */
    private Socket socket;

    /**
     * Stream reader and writer used to interact with {@link #socket}.
     */
    private BinaryReader reader;
    private BinaryWriter writer;

    /**
     * Flag describing whether the socket is connected or not.
     */
    private volatile boolean isConnected;

    /**
     * Event {@link com.kevelbreh.steamchat.steam.handler.IHandler} for events dispatched from the
     * Steam network and connection status.
     */
    private IHandler handler;

    /**
     * Steam's network address and port.
     */
    private String address;
    private int port;

    /**
     * Steam's universe this connection is connected to.
     */
    private int universe;

    /**
     * Packet queue that needs to be sent to server.
     */
    private LinkedBlockingQueue<Packet> queue;

    /**
     * Local IP address of the client.
     */
    private volatile int localIpAddress;

    /**
     * Setup the Steam TCPConnection.
     * @param address of the steam server.
     * @param port of the steam server.
     * @param handler to be used for incoming events.
     */
    public TCPConnection(final String address, final int port, IHandler handler) {
        super("SteamConnection-Thread");

        this.address = address;
        this.port = port;
        this.handler = handler;
        this.queue = new LinkedBlockingQueue<Packet>();
    }

    /**
     * @return whether or not the connection to steam has been encrypted or not.
     */
    public boolean isEncrypted() {
        return (netFilter != null);
    }

    /**
     * @return if the socket is connected or not.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * @return gets the local IP address of the connected client.
     */
    public int getLocalIpAddress() {
        return localIpAddress;
    }


    /**
     * @return the connected universe.
     */
    public int getUniverse() {
        return universe;
    }

    /**
     * @param packet to be sent to the server.
     */
    public void addPacketQueue(Packet packet) {
        //if (isConnected() && netFilter != null) {
            send(packet);
        //} else {
        //    queue.offer(packet);
        //}
    }

    /**
     * Connect a steam network connection.
     */
    public void connect() {
        socket = null;
        try {
            socket = new Socket(address, port);
            if (!socket.isConnected()) {
                disconnect();
                return;
            }

            isConnected = true;
            reader = new BinaryReader(socket.getInputStream());
            writer = new BinaryWriter(socket.getOutputStream());
        }
        catch(final IOException e) {
            SteamChat.debug(this, "Failed to connect to steam network: " + e.getMessage());
        }
    }

    /**
     * Disconnect from the steam network.
     */
    public void disconnect() {
        if (!isConnected) return;

        isConnected = false;
        netFilter = null;

        // Cleanup after disconnect.
        if (socket != null) {
            try {
                if (socket.isConnected()) {
                   socket.setSoTimeout(1);
                   socket.shutdownInput();
                   socket.shutdownOutput();
                }
                socket.close();
            }
            catch(final IOException e) {
                SteamChat.debug(this, "Failed to clean to steam connection: " + e.getMessage(), e);
            }
            finally {
                socket = null;
            }
        }

        interrupt();
        handler.onDisconnected(this);
    }

    /**
     * Start heart beating
     *
     * Todo: define once, send same packet over and over.
     */
    public void startHeartBeat(int value) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                HeartBeat heartBeat = new HeartBeat();
                heartBeat.getHeader().setSteamid(steamid);
                heartBeat.getHeader().setClientSessionid(sessionid);
                heartBeat.getHeader().setJobidSource(BinaryReader.LongMaxValue);
                heartBeat.getHeader().setJobidTarget(BinaryReader.LongMaxValue);
                send(heartBeat);
                SteamChat.debug(this, "Client -> PING -> Server.");
            }

        }, 0, value * 1000);
    }
    /**
     * Read a single packet. The packet header consists of only the packet length and the "VT01"
     * magic.  UDP packets are a whole different ball game.
     */
    private void readPacket() {
        int length, magic;
        byte[] data;

        try {
            try {
                length = reader.readInt();
                magic = reader.readInt();
            }
            catch(final IOException e) {
                throw new IOException("Connection lost while reading packet header.", e);
            }

            if (magic != MAGIC) {
                throw new IOException("Invalid packet magic");
            }

            data = reader.readBytes(length);
            if (data.length != length) {
                throw new IOException("Connection lost while reading packet payload");
            }

            if (netFilter != null) {
                data = netFilter.processIncoming(data);
            }

        }
        catch(final IOException e) {
            SteamChat.debug(this, "Failed to read packet: " + e.getMessage());
            disconnect();
            return;
        }

        try {
            Packet packet = new Packet().withData(data);
            processPacket(packet);
        }
        catch(Exception e) {
            SteamChat.debug(this, e.toString(), e);
        }
    }

    private void processPacket(Packet packet) throws IOException {
        int type = packet.getMessageType();
        handler.onEventReceived(this, type, packet);

        switch(type) {
            case Message.CHANNEL_ENCRYPT_REQUEST:
                final ChannelEncryptRequest request = new ChannelEncryptRequest().withData(packet.getData());
                request.deserialize();
                universe = request.getUniverse();

                final ChannelEncryptResponse response = new ChannelEncryptResponse();

                final byte[] public_key = PublicKey.UNIVERSE_PUBLIC;
                temp_session_key = Cryptography.GenerateRandomBlock(32);

                final RSA rsa = new RSA(public_key);
                byte[] encrypted_session_key = rsa.encrypt(temp_session_key);
                final byte[] key_crc =Cryptography.CRCHash(encrypted_session_key);

                try {
                    response.setEncryptedSessionKey(encrypted_session_key);
                    response.setKeyCRC(key_crc);
                    send(response);
                }
                catch(Exception e) {
                    SteamChat.debug(this, e.toString(), e);
                }

                break;
            case Message.CHANNEL_ENCRYPT_RESULT:
                final ChannelEncryptResult result = new ChannelEncryptResult()
                        .withData(packet.getData());
                result.deserialize();
                if (result.getResult() == Language.Result.OK) {
                    this.netFilter = new NetEncryption(temp_session_key);
                }

                handler.onConnected(this);
                break;

            case Message.MULTI:
                if (!packet.isProto()) return;

                final MultiPacket multi = new MultiPacket().withData(packet.getData());
                multi.deserialize();

                byte[] payload = multi.getBody().getMessageBody().toByteArray();


                if (multi.getBody().getSizeUnzipped() > 0) {
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(payload));
                        byte[] buffer = new byte[1024];
                        int read;

                        while ((read = stream.read(buffer)) > 0) {
                            out.write(buffer, 0, read);
                        }

                        payload = out.toByteArray();
                        out.close();
                        stream.close();
                    }
                    catch (final IOException e) {
                        SteamChat.debug(this, e.toString(), e);
                        SteamChat.debug(this, "failed on payload unzipping");
                        return;
                    }
                }

                final BinaryReader reader = new BinaryReader(payload);
                while (!reader.isAtEnd()) {
                    final int subsize = reader.readInt();
                    final byte[] subdata = reader.readBytes(subsize);

                    try {
                        Packet p = new Packet().withData(subdata);
                        processPacket(p);
                    }
                    catch(Exception e) {
                        SteamChat.debug(this, e.toString(), e);
                        SteamChat.debug(this, "failed on processing packet");
                    }
                }
                break;

            case Message.CLIENT_LOGGED_OFF:
                break;
            case Message.CLIENT_SERVER_LIST:
                break;
            case Message.CLIENT_LOG_ON_RESPONSE:
                if (!packet.isProto()) return;

                try {
                    ClientLogOnResponse loggedOnResponse = new ClientLogOnResponse().withData(packet.getData());
                    loggedOnResponse.deserialize();

                    if (loggedOnResponse.getBody().getEresult() == Language.Result.OK) {
                        steamid = loggedOnResponse.getHeader().getSteamid();
                        sessionid = loggedOnResponse.getHeader().getClientSessionid();
                        startHeartBeat(loggedOnResponse.getBody().getOutOfGameHeartbeatSeconds());
                    }
                } catch (Exception e) {
                    SteamChat.debug(this, e.toString(), e);
                }
                break;
        }
    }

    /**
     * Send a packet through the socket to the Steam network.
     * @param packet to be sent.
     */
    private void send(Packet packet) {
        try {
            packet.send(netFilter, writer);
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.toString());
        }
    }

    private static int getIPAddress() {
        try {
            final ByteBuffer buff = ByteBuffer.wrap(InetAddress.getLocalHost().getAddress());
            return (int) (buff.getInt() & 0xFFFFFFFFL);
        }
        catch(UnknownHostException e) {
            SteamChat.debug(e.toString());
        }
        return 0;
    }

    @Override
    public void run() {
        connect();

        if (isConnected()) {
            localIpAddress = getIPAddress();
        }

        if (isConnected()) while (true) {
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                break;
            }

            if (!isConnected()) {
                break;
            }

            /*if (isConnected() && isEncrypted() && queue.size() > 0) {
                try {
                    SteamChat.debug(this, "Sending from queue");
                    queue.take().send(netFilter, writer);
                } catch (final InterruptedException e) {
                    SteamChat.debug(this, "Failed to get packet from queue: " + e.toString());
                } catch (final IOException e) {
                    SteamChat.debug(this, "Failed to send packet:" + e.toString());
                }
            }*/

            try {

                SteamChat.debug(this, "isAtEnd=" + reader.getStream().isAtEnd());
                if (!reader.isAtEnd()) {
                    readPacket();
                } else {
                    disconnect();
                }
            }
            catch(final IOException e) {
                SteamChat.debug(this, "Failed to read packet:" + e.toString());
            }
        }
    }
}
