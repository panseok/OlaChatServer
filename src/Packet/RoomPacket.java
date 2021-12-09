package Packet;

import Data.User;
import DevTool.LittleEndianWriter;
import Room.ChatRoom;
import Opcode.SendOpcode;

import java.util.List;

public class RoomPacket {


    public static byte[] sendJoinRoomRequestResult(ChatRoom room, boolean isBan ,boolean isSucceed, User user){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_JOIN_REQUEST_RESULT.ordinal());
        o.write(isBan);
        o.write(isSucceed);

        if(isSucceed){
            o.writeLong(user.getUserId());
            o.writeInt(room.getRoom_number());
            o.writeLengthAsciiString(room.getRoom_name());
            o.writeInt(room.getRoom_member_count());
            o.writeInt(room.getRoom_member_maxcount());
        }

        return o.getPacket();
    }

    public static byte[] sendJoinRoomRequestResult(User user,String chatData){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_SEND_CHAT.ordinal());

        o.writeLong(user.getUserId());
        o.writeLengthAsciiString(chatData);
        return o.getPacket();
    }



    public static byte[] sendNoticeText(String chatData){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_SEND_NOTICE.ordinal());
        o.writeLengthAsciiString(chatData);
        return o.getPacket();
    }

    public static byte[] sendRoomChat(User user,int roomNumber, String chatData, int userProfileImageCode){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_SEND_CHAT.ordinal());
        o.writeInt(roomNumber);
        o.writeLong(user.getUserId());
        o.writeLengthAsciiString(user.getUserName());
        o.writeLengthAsciiString(chatData);
        o.writeInt(userProfileImageCode);
        return o.getPacket();
    }

    public static byte[] sendUserList(List<User> userList){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_USER_LIST.ordinal());
        o.writeInt(userList.size());
        for (User user : userList){
            user.sendUserInfo(o);
        }
        return o.getPacket();
    }

    public static byte[] sendGameActivityJoin(int code){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_START_GAME_ACTIVITY.ordinal());
        o.writeInt(code);
        return o.getPacket();
    }

    public static byte[] sendFriedList(List<User> friendList){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_INVITE_FRIEND_LIST.ordinal());
        o.writeInt(friendList.size());
        for (User user : friendList){
            user.sendUserInfo(o);
        }
        return o.getPacket();
    }

    public static byte[] openInviteRoomDialog(User invitee, ChatRoom room){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_OPEN_INVITE_ACCPET.ordinal());
        invitee.sendUserInfo(o);
        o.writeInt(room.getRoom_number());
        o.writeLengthAsciiString(room.getRoom_name());
        return o.getPacket();
    }

    public static byte[] sendBanRoom(){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.ROOM_EXIT_BAN.ordinal());
        return o.getPacket();
    }

}
