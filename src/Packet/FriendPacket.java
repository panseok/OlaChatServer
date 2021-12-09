package Packet;

import Data.User;
import DevTool.LittleEndianWriter;
import Opcode.SendOpcode;

import java.util.List;

public class FriendPacket {

    public  static byte[] sendFriendList(List<User> friendlist){
        LittleEndianWriter o = new LittleEndianWriter();

        o.writeShort(SendOpcode.FRIEND_SEND_USER_LIST.ordinal());
        o.writeInt(friendlist.size());
        for(User firend : friendlist){
            firend.sendUserInfo(o);
        }
        return o.getPacket();
    }

    public  static byte[] sendFriendRequestList(List<User> friendlist){
        LittleEndianWriter o = new LittleEndianWriter();

        o.writeShort(SendOpcode.FRIEND_SEND_REQUEST_LIST.ordinal());
        o.writeInt(friendlist.size());
        for(User firend : friendlist){
            firend.sendUserInfo(o);
        }
        return o.getPacket();
    }

    public static byte[] sendFriendNoticeMsg(String msg){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.FRIEND_NOTICE_MSG.ordinal());
        o.writeLengthAsciiString(msg);
        return o.getPacket();
    }

}
