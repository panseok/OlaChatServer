package Packet;

import Data.User;
import DevTool.LittleEndianWriter;
import Game.BoomSpin.BoomSpinAuction;
import Game.BoomSpin.PlayerInfo;
import Game.BoomSpin.PlayerSkillInfo;
import Opcode.SendOpcode;

import java.util.List;

public class BoomSpinPacket {

    /*
    * 0  턴
    * 1  알림판
    * 2  폭탄 시간
    * 3  자기폭탄 시간
    * 4  플레이어 리스트
    * 5  돈
    * 6  플레이어 채팅
    * 7  경매 다이얼로그 오픈
    * 8  경매 인포
    * 9  경매 알림
    * 10 경매 타이머
    * 11 경매 종료
    * 12 게임 종료
    * 13 토스트이미지
    * */



    public static byte[] sendBoomSpinDay(int day){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(0);
        o.writeInt(day);
        return o.getPacket();
    }

    public static byte[] sendBoomSpinNotice(String text){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(1);
        o.writeLengthAsciiString(text);
        return o.getPacket();
    }

    public static byte[] sendBoomSpinBoomTime(String time){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(2);
        o.writeLengthAsciiString(time);
        return o.getPacket();
    }



    public static byte[] sendBoomSpinPlayerList(List<PlayerInfo> players){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(4);
        o.writeInt(players.size());
        for(PlayerInfo player : players){
            player.sendPlayerInfo(o);
        }
        return o.getPacket();
    }

    public static byte[] sendBoomSpinMoney(int coin){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(5);
        o.writeInt(coin);
        return o.getPacket();
    }

    public static byte[] sendBoomSpinChat(User user, String chatData, int userProfileImageCode){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(6);
        o.writeLong(user.getUserId());
        o.writeLengthAsciiString(user.getUserName());
        o.writeLengthAsciiString(chatData);
        o.writeInt(userProfileImageCode);
        return o.getPacket();
    }

    public static byte[] sendOpenActuion(BoomSpinAuction boomSpinAuction, int my_coin){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(7);
        o.writeLengthAsciiString(boomSpinAuction.getSkillInfo().getSkillname());
        o.writeLengthAsciiString(boomSpinAuction.getSkillInfo().getSkillcomment());
        o.writeInt(boomSpinAuction.getLow_bid());
        o.writeInt(boomSpinAuction.getHigh_price());
        o.writeInt(my_coin);
        return o.getPacket();
    }

    public static byte[] sendAuctionInfo(BoomSpinAuction boomSpinAuction, String msg, int my_coin){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(8);
        o.writeLengthAsciiString(boomSpinAuction.getSkillInfo().getSkillname());
        o.writeLengthAsciiString(boomSpinAuction.getSkillInfo().getSkillcomment());
        o.writeInt(boomSpinAuction.getLow_bid());
        o.writeInt(boomSpinAuction.getHigh_price());
        o.writeLengthAsciiString(msg);
        o.writeInt(my_coin);
        return o.getPacket();
    }

    public static byte[] sendAuctionNotice(String msg){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(9);
        o.writeLengthAsciiString(msg);
        return o.getPacket();
    }

    public static byte[] sendAuctionTime(int time){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(10);
        o.writeInt(time);
        return o.getPacket();
    }

    public static byte[] sendAuctionExit(){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(11);
        return o.getPacket();
    }
    public static byte[] sendBoomSpinExit(){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(12);
        return o.getPacket();
    }

    public static byte[] sendShowBoomImg(int code){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(13);
        o.writeInt(code);
        return o.getPacket();
    }

    public static byte[] sendSkillbookInfo(){
        LittleEndianWriter o = new LittleEndianWriter();
        o.writeShort(SendOpcode.GAME_BOOM_SPIN.ordinal());
        o.writeInt(14);

        o.writeInt(PlayerSkillInfo.help_comment_list.size());
        for(PlayerSkillInfo playerSkillInfo : PlayerSkillInfo.help_comment_list){
            o.writeInt(playerSkillInfo.getSkillcode());
            o.writeLengthAsciiString(playerSkillInfo.getSkillname());
            o.writeLengthAsciiString(playerSkillInfo.getSkillcomment());
        }

        o.writeInt(PlayerSkillInfo.dead_player_skill_List.size());
        for(PlayerSkillInfo playerSkillInfo : PlayerSkillInfo.dead_player_skill_List){
            o.writeInt(playerSkillInfo.getSkillcode());
            o.writeLengthAsciiString(playerSkillInfo.getSkillname());
            o.writeLengthAsciiString(playerSkillInfo.getSkillcomment());
        }

        o.writeInt(PlayerSkillInfo.passive_skill_List.size());
        for(PlayerSkillInfo playerSkillInfo : PlayerSkillInfo.passive_skill_List){
            o.writeInt(playerSkillInfo.getSkillcode());
            o.writeLengthAsciiString(playerSkillInfo.getSkillname());
            o.writeLengthAsciiString(playerSkillInfo.getSkillcomment());
        }

        o.writeInt(PlayerSkillInfo.auction_skill_List.size());
        for(PlayerSkillInfo playerSkillInfo : PlayerSkillInfo.auction_skill_List){
            o.writeInt(playerSkillInfo.getSkillcode());
            o.writeLengthAsciiString(playerSkillInfo.getSkillname());
            o.writeLengthAsciiString(playerSkillInfo.getSkillcomment());
        }

        return o.getPacket();
    }
}
