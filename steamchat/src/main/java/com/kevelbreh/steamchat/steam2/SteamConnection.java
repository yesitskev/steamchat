package com.kevelbreh.steamchat.steam2;

import com.kevelbreh.steamchat.SteamChat;
import com.kevelbreh.steamchat.steam.language.Language;
import com.kevelbreh.steamchat.steam.language.Message;
import com.kevelbreh.steamchat.steam.network.packet.HeartBeat;
import com.kevelbreh.steamchat.steam.proto.SteamMessagesClientServerProto.CMsgClientHeartBeat;
import com.kevelbreh.steamchat.steam.security.Cryptography;
import com.kevelbreh.steamchat.steam.util.BinaryReader;
import com.kevelbreh.steamchat.steam.util.BinaryWriter;
import com.kevelbreh.steamchat.steam2.packet.Packet;
import com.kevelbreh.steamchat.steam2.packet.ProtoPacket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class SteamConnection extends Thread {

    /**
     * This {@link java.net.Socket} is a connection to the Steam server.
     */
    private Socket socket;

    /**
     * The host of the Steam server.
     */
    private String host;

    /**
     * The port of the Steam server.
     */
    private int port;

    /**
     * The {@link com.kevelbreh.steamchat.steam.util.BinaryReader} reads the data from the
     * {@link java.net.Socket#getInputStream()}.
     */
    private BinaryReader in;

    /**
     * The {@link com.kevelbreh.steamchat.steam.util.BinaryWriter} writes the data to the
     * {@link java.net.Socket#getOutputStream()}.
     */
    private BinaryWriter out;

    /**
     * This is the {@link com.kevelbreh.steamchat.steam2.SteamConnection#socket} timeout in milliseconds.
     * The default timeout is set to 5 minutes.
     */
    private int timeout = 1000 * 60 * 5;

    /**
     * Session key is used to encrypt and decrypt steam data after {@link com.kevelbreh.steamchat.steam.language.Message#CHANNEL_ENCRYPT_RESPONSE}
     * has been received with an OK result.
     */
    private byte[] sessionKey;

    /**
     * The session id received once the user has logged in. This needs to be put in the header of each
     * packet being sent to steam after logging in.
     */
    private int sessionId;

    /**
     * The steam id received once the user has logged in. This needs to be put in the header of each
     * packet being sent to steam after logging in.
     */
    private long steamId;

    /**
     * This is used to dispatch data events to a listener bound to this connection.
     */
    private OnDataReceivedListener listener;

	/**
	 * Heartbeat packet that needs to be sent to steam every few seconds.  This time is returned from steam during
	 * the login procedure.  Once the user successfully logs in, a value is returned for the "out of game heartbeat"
	 * time interval.
	 */
	private ProtoPacket<CMsgClientHeartBeat.Builder> heartbeat;

	/**
	 * Heartbeat timer that will send the a {@link #heartbeat} packet every few seconds defined by steam after
	 * logging in.  Without the timer steam will reject the connection and treat it as if the user has timed out.
	 */
	private Timer heartbeatTimer;

    /**
     *
     * @param host
     * @param port
     */
    public SteamConnection(String host, int port) {
        super("SteamConnection-Thread");
        this.host = host;
        this.port = port;
    }

    /**
     * Establish a connection to the steam server.
     *
     * @throws IOException
     */
    private void connect() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(this.host, this.port);
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.getMessage(), e);
            if (socket != null) {
                socket.close();
            }
            throw e;
        }
        this.prepare(socket);
    }

    /**
     * Invoked by the {@link #connect()} method, this prepares the connection. It initializes the
     * input and output streams and then starts the thread for receiving messages from the Steam Network.
     *
     * @param socket which is used for the connection.
     * @throws IOException if errors occur.
     */
    private void prepare(Socket socket) throws IOException {
        if (socket == null) {
            throw new SocketException("Socket can not be null.");
        }
        this.socket = socket;
        this.socket.setSoTimeout(this.timeout);
        this.in = new BinaryReader(this.socket.getInputStream());
        this.out = new BinaryWriter(this.socket.getOutputStream());
    }

    /**
     * The {@link java.lang.Thread} is started by the {@link #connect()} method. It's task is to
     * receive data from the Steam network and hand them over to the {@link #process(byte[])} method.
     */
    @Override
    public void run() {
        try {
            connect();

            while(!in.isAtEnd()) {
                final int length = in.readInt();
                final int magic = in.readInt();

                if (magic != 0x31305456) {
                    throw new IOException("Invalid magic");
                }

                byte[] data = in.readBytes(length);
                if (data.length != length) {
                    throw new IOException("Connection lost while reading packet");
                }

                if (sessionKey != null) {
                    data = Cryptography.SymmetricDecrypt(data, sessionKey);
                }

                process(data);
                sleep(500);
            }
        }
        catch(SocketException e) {
			/*
			09-23 15:05:49.157  22263-22881/com.kevelbreh.steamchat:SteamService D/SteamConnection﹕ recvfrom failed: ETIMEDOUT (Connection timed out)
    		java.net.SocketException: recvfrom failed: ETIMEDOUT (Connection timed out)
            at libcore.io.IoBridge.maybeThrowAfterRecvfrom(IoBridge.java:552)

            On ETIMEDOUT we should set a timeout thing to auto-reconnect after x amount of seconds.
			 */
            SteamChat.debug(this, e.getMessage(), e);
            close();
        }
        catch(IOException e) {
            SteamChat.debug(this, e.getMessage(), e);
            close();
        }
        catch(InterruptedException e) {
            SteamChat.debug(this, e.getMessage(), e);
            close();
        }
        finally {
            close();
        }
    }

    /**
     * Serialize the packet data and then write it to the {@link #out} stream.
     * @param packet that will be sent to Steam.
     */
    public synchronized void send(Packet packet) {
        try {
            if (steamId != 0) {
                packet.setSteamId(steamId);
            }

            if (sessionId != 0) {
                packet.setSessionId(sessionId);
            }

            byte[] data = packet.serialize();
            if (sessionKey != null) {
                data = Cryptography.SymmetricEncrypt(data, sessionKey);
            }

            out.write(data.length);
            out.write(0x31305456);
            out.write(data);
            out.flush();
        }
        catch(final IOException e) {
            SteamChat.debug(this, e.getMessage(), e);
        }
    }

    /**
     * Process an incoming packet.  Retrieve the message message type from the data, then fire an
     * {@link com.kevelbreh.steamchat.steam2.SteamConnection.OnDataReceivedListener#onDataReceived(int, boolean, byte[])}
     * event to whichever handler is attached.
     *
     * @param data that needs to be processed.
     */
    private void process(final byte[] data) throws IOException {
        final byte[] type = new byte[4];
        for (int i = 0; i < 4; i++) {
            type[3 - i] = data[i];
        }

        final ByteBuffer buffer = ByteBuffer.wrap(type);
        final int rawType = buffer.getInt();

        // Set a default listener that does nothing.
        if (listener == null) {
            listener = new OnDataReceivedListener() {
                @Override
                public synchronized void onDataReceived(final int event, final boolean isProto, final byte[] data) {
                    SteamChat.debug(this, "Received some data from Steam...");
                }
            };
        }

        listener.onDataReceived(Message.forType(rawType), Message.isProtoBuffed(rawType), data);
    }

    /**
     * Close down the connection.
     */
    public synchronized void close() {
		stopHeartbeat();

        try {
            if (!isInterrupted()) {
                interrupt();
            }
        } catch(Exception e) {
            SteamChat.debug(this, e.getMessage(), e);
        }

        try {
            if (this.socket != null) {
                //this.socket.shutdownOutput();
                //this.socket.shutdownInput();
                this.socket.close();
            }
        } catch(Exception e) {
            SteamChat.debug(this, e.getMessage(), e);
        }

        this.socket = null;
        this.in = null;
        this.out = null;
    }

	/**
	 * Create or return an already created heart beat packet which will be sent to steam to keep the connection alive
	 * .  Without this steam will terminate the connection as if the client timed out.
	 *
	 * @return a {@link com.kevelbreh.steamchat.steam2.packet.ProtoPacket} for steam heart beating.
	 */
	private ProtoPacket getHeartbeat() {
		if (heartbeat == null) {
			heartbeat = new ProtoPacket<CMsgClientHeartBeat.Builder>(CMsgClientHeartBeat.class,
					Language.Message.CLIENT_HEARTBEAT);
		}
		return heartbeat;
	}

	/**
	 * Start a timer to send the {@link #getHeartbeat()} packet every few seconds defined by steam once the user has
	 * successfully logged in.  Without heart beating, steam will terminate the connection.
	 *
	 * @param interval in which the heartbeat needs to be sent.
	 */
	public synchronized void startHeartbeat(int interval) {
		heartbeatTimer = new Timer();
		heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				send(getHeartbeat());
				SteamChat.debug(this, "Client -> PING -> Server.");
			}
		}, 0, interval * 1000);
	}

	/**
	 * Stop steam heart beating.  This could only possibly be called when the steam connection gets recycled and
	 * broke down.
	 */
	private void stopHeartbeat() {
		if (heartbeatTimer != null) {
			heartbeatTimer.cancel();
		}
	}

    /**
     * Set the {@link #listener} of this connection.  When a packet is received, the packet will
     * be dispatched to this listener.
     * @param listener to be attached.
     */
    public synchronized void setDataReceivedListener(OnDataReceivedListener listener) {
        this.listener = listener;
    }

    /**
     * Set the {@link #sessionKey} for this connection.  This is a requirement else any data received
     * will be mushed up and any data received will be exposed to anyone.
     * @param key to be used for cryptography.
     */
    public synchronized void setSessionKey(byte[] key) {
        this.sessionKey = key;
    }

    /**
     *
     * @param id of the currently logged in steam user.
     */
    public synchronized void setSteamId(long id) {
        this.steamId = id;
    }

    /**
     *
     * @param id of the currently logged in steam user's connection session.
     */
    public synchronized void setSessionId(int id) {
        this.sessionId = id;
    }

    /**
     * Interface for receiving data.
     */
    public interface OnDataReceivedListener {

        /**
         * Fired when a packet is received.
         *
         * @param event message type.
         * @param proto boolean whether the data is proto buffed or not.
         * @param data payload.
         */
        public void onDataReceived(final int event, final boolean proto, final byte[] data);
    }
}
