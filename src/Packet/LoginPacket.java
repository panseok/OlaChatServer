package Packet;

import Data.User;
import DevTool.LittleEndianWriter;
import Opcode.SendOpcode;

public class LoginPacket {

    public static byte[] sendPing(){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.SERVER_PING.ordinal());
        return o.getPacket();
    }

    public static byte[] sendLoginServerConnect(boolean isSucceed){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.CONNECT_LOGIN_SERVER_ONLINE.ordinal());
        o.write(isSucceed);

        return o.getPacket();
    }

    public static byte[] sendLoginStatus(short type){
        return sendLoginStatus(type, "");
    }
    public static byte[] sendLoginStatus(short type, String msg){
        /*
        * 0 : 가입 성공
        * 1 : 가입 실패
        * 2 : 닉네임 생성 성공
        * 3 : 닉네임 생성 실패
        * 4 : 로그인 성공 (이미 계정 있음)
        * 5 : 이미 있는 닉네임
        * 6 : 기타
        * */
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.LOGIN_STATUS_MSG.ordinal());
        o.writeShort(type);
        if(type == 6){
            o.writeLengthAsciiString(msg);
        }
        return o.getPacket();
    }

    public static byte[] sendCreateUserName(){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.LOGIN_CREATE_NAME.ordinal());
        return o.getPacket();
    }

    public static byte[] sendConnectChannelServer(){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.CONNECT_CHANNEL_SERVER.ordinal());
        return o.getPacket();
    }

    public static byte[] sendConnectChannelServerSucceed(User user){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.CONNECT_CHANNEL_SERVER_ONLINE.ordinal());
        user.sendUserInfo(o);
        return o.getPacket();
    }
}
