package com.kevelbreh.steamchat.steam2;

import android.os.Bundle;

import com.kevelbreh.steamchat.SteamChat;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class SteamEventBus extends Thread {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface SteamEvent {
        int event() default 0;
        boolean isProto() default true;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface UserEvent {
        int event() default 0;
    }

    /**
     * Steam service.
     */
    private SteamService mSteamService;

    /**
     * Map of the cached handlers.
     */
    private ArrayList<Method> mSteamHandlers;
    private ArrayList<Method> mUserHandlers;


    private LinkedBlockingQueue<EventStructureInterface> mEventQueue;

    /**
     *
     * @param service that this event bus runs on.
     */
    public SteamEventBus(SteamService service) {
        super("SteamEventBus-Thread");
        mSteamService = service;

        mEventQueue = new LinkedBlockingQueue<EventStructureInterface>();

        mSteamHandlers = new ArrayList<Method>();
        mUserHandlers = new ArrayList<Method>();
    }

    /**
     *
     * @param handler that will handle certain events.
     */
    public void register(Class handler) {
        for (Method method : handler.getDeclaredMethods()) {
            Annotation steam = method.getAnnotation(SteamEvent.class);
            Annotation user = method.getAnnotation(UserEvent.class);

            // Only allowed to handle 1 event per method handle!
            if (steam != null && user != null) {
                continue;
            }

            if (steam != null) {
                mSteamHandlers.add(method);
                continue;
            }

            if (user != null) {
                mUserHandlers.add(method);
            }
        }
    }

    /**
     * Handle events fired from {@link com.kevelbreh.steamchat.steam2.SteamConnection}. Decided to
     * go against sending packet objects to reduce the object boiler plate. Primitive types will do
     * just fine.
     *
     * @param event {@link com.kevelbreh.steamchat.steam.language.Language.Message} of the event.
     * @param proto of flag if the data is protobuf backed or not.
     * @param data of the packet (full data).
     */
    public synchronized void handleSteamEvent(int event, boolean proto, final byte[] data) {
        SteamChat.debug(this, "SteamEvent: event=" + event + " proto=" + proto + " length="+data.length);
        mEventQueue.offer(new SteamEventStructure(event, proto, data));
    }

    /**
     * Handle events from the user or other events fired that are not from steam.
     *
     * @param event type.
     * @param data bundle which can be null.
     */
    public synchronized void handleUserEvent(int event, final Bundle bundle) {
        SteamChat.debug(this, "UserEvent: event=" + event);
        mEventQueue.offer(new UserEventStructure(event, bundle));
    }

    @Override
    public void run() {
        while (true) {
            try {
                while (!mEventQueue.isEmpty()) {
                    EventStructureInterface event = mEventQueue.take();

                    if (event.getType() == 1) {
                        SteamEventStructure steamEvent = (SteamEventStructure) event;
                        for (Method method : mSteamHandlers) {
                            SteamEvent annotation = method.getAnnotation(SteamEvent.class);
                            if (annotation.event() == steamEvent.event && annotation.isProto() == steamEvent.isProto) {
                                try {
                                    method.invoke(this, mSteamService, steamEvent.data);
                                }
                                catch(Exception e) {
                                    SteamChat.debug(this, "Failed to invoke handle", e);
                                }
                            }
                        }
                        continue;
                    }

                    if (event.getType() == 2) {
                        UserEventStructure userEvent = (UserEventStructure) event;
                        for (Method method : mUserHandlers) {
                            UserEvent annotation = method.getAnnotation(UserEvent.class);
                            if (annotation.event() == userEvent.event) {
                                try {
                                    method.invoke(this, mSteamService, userEvent.data);
                                }
                                catch(Exception e) {
                                    SteamChat.debug(this, "Failed to invoke handle", e);
                                }
                            }
                        }
                    }
                }

                sleep(1000);
            }
            catch(final InterruptedException e) {
                SteamChat.debug(this, e.getMessage(), e);
                break;
            }
        }
    }

    /**
     *
     */
    private class SteamEventStructure implements EventStructureInterface {
        public int event;
        public boolean isProto;
        public byte[] data;

        public SteamEventStructure(final int event, final boolean isProto, final byte[] data) {
            this.event = event;
            this.isProto = isProto;
            this.data = data;
        }

        @Override
        public int getType() {
            return 1;
        }
    }

    /**
     *
     */
    private class UserEventStructure implements EventStructureInterface {
        public int event;
        public Bundle data;

        public UserEventStructure(final int event, final Bundle data) {
            this.event = event;
            this.data = data;
        }

        @Override
        public int getType() {
            return 2;
        }
    }

    private interface EventStructureInterface {
        public int getType();
    }
}
