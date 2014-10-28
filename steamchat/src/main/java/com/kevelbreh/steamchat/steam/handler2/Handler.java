package com.kevelbreh.steamchat.steam.handler2;

public class Handler {

    /**
     *
     */
    public @interface Handle {

        /**
         *
         * @return
         */
        int message() default 0;

        /**
         *
         * @return
         */
        boolean proto() default true;
    }

    public Handler() {

    }
}
