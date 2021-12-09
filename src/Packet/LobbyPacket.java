package Packet;

import Data.User;
import DevTool.LittleEndianWriter;
import Lobby.ServerNotice;
import Room.ChatRoom;
import Opcode.SendOpcode;

import java.util.List;

public class LobbyPacket {

public static byte[] sendServerNoticeList(List<ServerNotice> serverNotices){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.SERVER_NOTICE.ordinal());

        o.writeInt(serverNotices.size());
        for(ServerNotice notice : serverNotices){
            notice.sendServerNotice(o);
        }
        return o.getPacket();
    }

    public static byte[] sendRoomList(List<ChatRoom> roomList){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.LOBBY_ROOM_LIST.ordinal());

        o.writeInt(roomList.size());
        for(ChatRoom room : roomList){
            o.writeInt(room.getRoom_number());
            o.writeLengthAsciiString(room.getRoom_name());
            o.writeInt(room.getRoom_member_count());
            o.writeInt(room.getRoom_member_maxcount());
        }
        return o.getPacket();
    }

    public static byte[] sendChangeProfileImageResult(boolean isSucceed, int profileImageCode){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.USER_CHANGE_PROFILE_IMAGE_RESULT.ordinal());
        o.write(isSucceed);
        o.writeInt(profileImageCode);
        return o.getPacket();
    }

    public static byte[] sendUserInfo(User user){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.USER_GET_USER_INFO.ordinal());
        user.sendUserInfo(o);
        return o.getPacket();
    }

    public static byte[] sendOpenUserProfile(User user, boolean isExist){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.USER_OPEN_PROFILE.ordinal());
        o.write(isExist);
        if(isExist){
            user.sendUserInfo(o);
        }
        return o.getPacket();
    }

    public static byte[] sendChangeUserMemo(String memo, boolean isSucced){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.USER_CHANGE_MEMO_RESULT.ordinal());
        o.write(isSucced);
        o.writeLengthAsciiString(memo);
        return o.getPacket();
    }


}
