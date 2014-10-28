package com.kevelbreh.steamchat.steam.language;

/**
 * Created by kevin on 2014/08/14.
 */
public class Language {

    public static class Account {
        public static final int INVALID                 = 0;
        public static final int INDIVIDUAL              = 1;
        public static final int MULTISEAT               = 2;
        public static final int GAME_SERVER             = 3;
        public static final int ANONOMOUS_GAME_SERVER   = 4;
        public static final int PENDING                 = 5;
        public static final int CONTENT_SERVER          = 6;
        public static final int CLAN                    = 7;
        public static final int CHAT                    = 8;
        public static final int CONSOLE_USER            = 9;
        public static final int ANONOMOUS_USER          = 10;
        public static final int MAX                     = 11;
    }

    public class Universe {
        public static final int INVALID                 = 0;
        public static final int PUBLIC                  = 1;
        public static final int BETA                    = 2;
        public static final int INTERNAL                = 3;
        public static final int DEV                     = 4;
        public static final int RC                      = 5;
        public static final int MAX                     = 6;
    }

    public class Result {
        public static final int INVALID                                         = 0;
        public static final int OK                                              = 1;
        public static final int FAIL                                            = 2;
        public static final int NO_CONNECTION                                   = 3;
        public static final int INVALID_PASSWORD                                = 5;
        public static final int LOGGED_IN_ELSEWHERE                             = 6;
        public static final int INVALID_PROTOCOL_VERSION                        = 7;
        public static final int INVALID_PARAM                                   = 8;
        public static final int FILE_NOT_FOUND                                  = 9;
        public static final int BUSY                                            = 10;
        public static final int INVALID_STATE                                   = 11;
        public static final int INVALID_NAME                                    = 12;
        public static final int INVALID_EMAIL                                   = 13;
        public static final int DUPLICATE_NAME                                  = 14;
        public static final int ACCESS_DENIED                                   = 15;
        public static final int TIMEOUT                                         = 16;
        public static final int BANNED                                          = 17;
        public static final int ACCOUNT_NOT_FOUND                               = 18;
        public static final int INVALID_STEAM_ID                                = 19;
        public static final int SERVICE_NOT_AVAILABLE                           = 20;
        public static final int NOT_LOGGED_ON                                   = 21;
        public static final int PENDING                                         = 22;
        public static final int ENCRYPTION_FAILURE                              = 23;
        public static final int INSUFFICIENT_PRIVILEGE                          = 24;
        public static final int LIMIT_EXCEEDED                                  = 25;
        public static final int REVOKED                                         = 26;
        public static final int EXPIRED                                         = 27;
        public static final int ALREADY_REDEEMED                                = 28;
        public static final int DUPLICATE_REQUEST                               = 29;
        public static final int ALREADY_OWNED                                   = 30;
        public static final int IP_NOT_FOUND                                    = 31;
        public static final int PERSIST_FAILED                                  = 32;
        public static final int LOCKING_FAILED                                  = 33;
        public static final int LOGON_SESSION_REPLACED                          = 34;
        public static final int CONNECT_FAILED                                  = 35;
        public static final int HANDSHAKE_FAILED                                = 36;
        public static final int IO_FAILURE                                      = 37;
        public static final int REMOTE_DISCONNECT                               = 38;
        public static final int SHOPPING_CART_NOT_FOUND                         = 39;
        public static final int BLOCKED                                         = 40;
        public static final int IGNORED                                         = 41;
        public static final int NO_MATCH                                        = 42;
        public static final int ACCOUNT_DISABLED                                = 43;
        public static final int SERVICE_READ_ONLY                               = 44;
        public static final int ACCOUNT_NOT_FEATURED                            = 45;
        public static final int ADMINISTRATOR_OK                                = 46;
        public static final int CONTENT_VERSION                                 = 47;
        public static final int TRY_ANOTHER_CM                                  = 48;
        public static final int PASSWORD_REQUIRED_TO_KICK_SESSION               = 49;
        public static final int ALREADY_LOGGED_IN_ELSEWHERE                     = 50;
        public static final int SUSPENDED                                       = 51;
        public static final int CANCELLED                                       = 52;
        public static final int DATA_CORRUPTION                                 = 53;
        public static final int DISK_FULL                                       = 54;
        public static final int REMOTE_CALL_FAILED                              = 55;
        public static final int PASSWORD_NOT_SET                                = 56;
        public static final int PSN_ACCOUNT_NOT_LINKED                          = 57;
        public static final int INVALID_PSN_TICKET                              = 58;
        public static final int PSN_ACCOUNT_ALREADY_LINKED                      = 59;
        public static final int REMOTE_FILE_CONFLICT                            = 60;
        public static final int ILLEGAL_PASSWORD                                = 61;
        public static final int SAME_AS_PREVIOUS_VALUE                          = 62;
        public static final int ACCOUNT_LOGON_DENIED                            = 63;
        public static final int CANNOT_USE_OLD_PASSWORD                         = 64;
        public static final int INVALID_LOGIN_AUTH_TOKEN                        = 65;
        public static final int ACCOUNT_LOGIN_DENIED_NO_MAIL_SENT               = 66;
        public static final int HARDWARE_NOT_CAPABLE_OF_IPT                     = 67;
        public static final int IPT_INIT_ERROR                                  = 68;
        public static final int PARENTAL_CONTROL_RESTRICTED                     = 69;
        public static final int FACEBOOK_QUERY_ERROR                            = 70;
        public static final int EXPIRED_LOGIN_AUTH_CODE                         = 71;
        public static final int IP_LOGIN_RESTRICTION_FAILED                     = 72;
        public static final int ACCOUNT_LOCKED                                  = 73;
        public static final int ACCOUNT_LOGON_DENIED_VERIFIED_EMAIL_REQUIRED    = 74;
    }

    public static class OperatingSystem {
        public static final int UNKNOWN                 = -1;
        public static final int PS3                     = -300;
        public static final int MAC_UNKNOWN             = -102;
        public static final int MAC_104                 = -101;
        public static final int MAC_105                 = -100;
        public static final int MAC_1058                = -99;
        public static final int MAC_106                 = -95;
        public static final int MAC_1064                = -94;
        public static final int MAC_1064_SLGU           = -93;
        public static final int MAC_1067                = -92;
        public static final int MAC_107                 = -90;
        public static final int LINUX_UNKNOWN           = -203;
        public static final int LINUX_22                = -202;
        public static final int LINUX_24                = -201;
        public static final int LINUX_26                = -200;
        public static final int WINDOWS_UNKNOWN         = 0;
        public static final int WINDOWS_311             = 1;
        public static final int WINDOWS_95              = 2;
        public static final int WINDOWS_98              = 3;
        public static final int WINDOWS_ME              = 4;
        public static final int WINDOWS_NT              = 5;
        public static final int WINDOWS_2000            = 6;
        public static final int WINDOWS_XP              = 7;
        public static final int WINDOWS_2003            = 8;
        public static final int WINDOWS_VISTA           = 9;
        public static final int WINDOWS_7               = 10;
        public static final int WINDOWS_2008            = 11;
        public static final int WINDOWS_MAX             = 12;
        public static final int MAX                     = 23;
    }

    public static class Platform {
        public static final int UNKNOWN                 = 0;
        public static final int WINDOWS_32              = 1;
        public static final int WINDOWS_64              = 2;
        public static final int LINUX                   = 3;
        public static final int OSX                     = 4;
        public static final int PS3                     = 5;
        public static final int MAX                     = 6;
    }

    public static class PersonaState {
        public static final int OFFLINE                 = 0;
        public static final int ONLINE                  = 1;
        public static final int BUSY                    = 2;
        public static final int AWAY                    = 3;
        public static final int SNOOZE                  = 4;
        public static final int LOOKING_TO_TRADE        = 5;
        public static final int LOOKING_TO_PLAY         = 6;
    }

    public static class ClientPersonaStateFlag {
        public static final int STATUS                  = 1;
        public static final int PLAYER_NAME             = 2;
        public static final int QUERY_PORT              = 4;
        public static final int SOURCE_ID               = 8;
        public static final int PRESENCE                = 16;
        public static final int METADATA                = 32;
        public static final int LAST_SEEN               = 64;
        public static final int CLAN_INFO               = 128;
        public static final int GAME_EXTRA_INFO         = 256;
        public static final int GAME_DATA_BLOB          = 512;
        public static final int CLAN_TAG                = 1024;
        public static final int FACEBOOK                = 2048;
    }

    public static class Friend {
        public static class Relationship {
            public static final int NONE                   = 0;
            public static final int BLOCKED                = 1;
            public static final int PENDING_INVITEE        = 2;
            public static final int REQUEST_RECIPIENT      = 2;
            public static final int FRIEND                 = 3;
            public static final int REQUEST_INITIATOR      = 4;
            public static final int PENDING_INVITER        = 4;
            public static final int IGNORED                = 5;
            public static final int IGNORED_FRIEND         = 6;
            public static final int SUGGESTED_FRIEND       = 7;
        }
    }

    public static class Chat {
        public static class Entry {
            public static final int INVALID                         = 0;
            public static final int CHAT_MESSAGE                    = 1;
            public static final int TYPING                          = 2;
            public static final int INVITE_GAME                     = 3;
            public static final int EMOTE                           = 4;
            public static final int LOBBY_GAME_START                = 5;
            public static final int LEFT_CONVERSATION               = 6;
            public static final int ENTERED                         = 7;
            public static final int WAS_KICKED                      = 8;
            public static final int WAS_BANNED                      = 9;
            public static final int DISCONNECTED                    = 10;
            public static final int HISTORICAL_CHAT                 = 11;
        }

        public static class Action {

        }

        public static class Info {
            public static final int STATE_CHANGED                   = 1;
            public static final int INFO_UPDATE                     = 2;
            public static final int MEMBER_LIMIT_CHANGED            = 3;
        }

    }

    public static class Message {
        public static final int INVALID                                                 = 0;
        public static final int MULTI                                                   = 1;
        public static final int JOB_HEARTBEAT                                           = 123;
		public static final int CLIENT_HEARTBEAT										= 703;
        public static final int CLIENT_CHANGE_STATUS                                    = 716;
        public static final int CLIENT_LOG_ON_RESPONSE                                  = 751;
        public static final int CLIENT_LOGGED_OFF                                       = 757;
        public static final int CLIENT_PERSONA_STATE                                    = 766;
        public static final int CLIENT_FRIENDS_LIST                                     = 767;
        public static final int CLIENT_ACCOUNT_INFO                                     = 768;
        public static final int CLIENT_GAME_CONNECT_TOKENS                              = 779;
        public static final int CLIENT_LICENSE_LIST                                     = 780;
        public static final int CLIENT_VAC_BAN_STATUS                                   = 782;
        public static final int CLIENT_CM_LIST                                          = 783;
        public static final int CLIENT_UPDATE_GUEST_PASSES_LIST                         = 798;
        public static final int CLIENT_REQUEST_FRIEND_DATA                              = 815;
        public static final int CLIENT_SESSION_TOKEN                                    = 850;
        public static final int CLIENT_SERVER_LIST                                      = 880;
        public static final int CHANNEL_ENCRYPT_REQUEST                                 = 1303;
        public static final int CHANNEL_ENCRYPT_RESPONSE                                = 1304;
        public static final int CHANNEL_ENCRYPT_RESULT                                  = 1305;
        public static final int CLIENT_FRIEND_MSG_INCOMING                              = 5427;
        public static final int CLIENT_IS_LIMITED_ACCOUNT                               = 5430;
        public static final int CLIENT_EMAIL_ADDRESS_INFO                               = 5456;
        public static final int CLIENT_NEW_LOGIN_KEY                                    = 5463;
		public static final int CLIENT_NEW_LOGIN_KEY_ACCEPTED							= 5464;
        public static final int CLIENT_REQUESTED_CLIENT_STATS                           = 5480;
        public static final int CLIENT_SERVERS_AVAILABLE                                = 5501;
        public static final int CLIENT_MARKETING_MESSAGE_UPDATE_2                       = 5510;
        public static final int CLIENT_LOG_ON                                           = 5514;
        public static final int CLIENT_WALLET_INFO_UPDATE                               = 5528;
        public static final int CLIENT_UPDATE_MACHINE_AUTH                              = 5537;
        public static final int CLIENT_UPDATE_MACHINE_AUTH_RESPONSE                     = 5538;
        public static final int CLIENT_FRIENDS_GROUPS_LIST                              = 5553;
        public static final int CLIENT_FRIEND_MESSAGE_ECHO_TO_SENDER                    = 5578;
        public static final int CLIENT_PLAYER_NICKNAME_LIST                             = 5587;
    }
}
