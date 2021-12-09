package Opcode;

public enum SendOpcode {
    SERVER_PING,
    SERVER_NOTICE,
    LOGIN_STATUS_MSG,
    LOGIN_CREATE_NAME,
    CONNECT_LOGIN_SERVER_ONLINE,
    CONNECT_CHANNEL_SERVER,
    CONNECT_CHANNEL_SERVER_ONLINE,
    USER_CHANGE_PROFILE_IMAGE_RESULT,
    USER_CHANGE_MEMO_RESULT,
    USER_GET_USER_INFO,
    USER_OPEN_PROFILE,
    LOBBY_ROOM_LIST,
    LOBBY_CREATE_ROOM,
    ROOM_JOIN_REQUEST_RESULT,
    ROOM_JOIN_USER,
    ROOM_SEND_CHAT,
    ROOM_SEND_NOTICE,
    ROOM_EXIT_USER,
    ROOM_EXIT_BAN,
    ROOM_USER_LIST,
    ROOM_INVITE_FRIEND_LIST,
    ROOM_OPEN_INVITE_ACCPET,
    FRIEND_NOTICE_MSG,
    FRIEND_SEND_USER_LIST,
    FRIEND_SEND_REQUEST_LIST,
    ROOM_START_GAME_ACTIVITY,
    GAME_BOOM_SPIN;
}
