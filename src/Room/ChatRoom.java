package Room;

import Data.User;
import DevTool.LittleEndianReader;
import DevTool.Timer;
import Friend.Friend;
import Game.BoomSpin.BoomSpin;
import Packet.BoomSpinPacket;
import Packet.FriendPacket;
import Packet.RoomPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class ChatRoom implements Comparable<ChatRoom> {

    private List<User> userList = Collections.synchronizedList(new ArrayList<>());
    private List<User> banList = Collections.synchronizedList(new ArrayList<>());

    private User Leader;
    private BoomSpin boomSpin = null;
    private ScheduledFuture<?> schedule = null;

    private Integer room_number= -1;
    private String room_name = "";
    private Integer room_member_count = 0;
    private Integer room_member_maxcount = 8;

    public ChatRoom(Integer room_number, String room_name, Integer room_member_maxcount){
        this.room_number = room_number;
        this.room_name = room_name;
        this.room_member_maxcount = room_member_maxcount;
    }


    public BoomSpin getBoomSpin() {
        return boomSpin;
    }

    public void setBoomSpin(BoomSpin boomSpin) {
        this.boomSpin = boomSpin;
    }

    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }

    public Integer getRoom_member_count() {
        return room_member_count;
    }

    public Integer getRoom_member_maxcount() {
        return room_member_maxcount;
    }

    public Integer getRoom_number() {
        return room_number;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public void setRoom_member_count(Integer room_member_count) {
        this.room_member_count = room_member_count;
    }

    public void setRoom_member_maxcount(Integer room_member_maxcount) {
        this.room_member_maxcount = room_member_maxcount;
    }

    public void setRoom_number(Integer room_number) {
        this.room_number = room_number;
    }

    @Override
    public int compareTo(ChatRoom room){
        if(this.room_number > room.room_number){
            return 1;
        }else if(this.room_number < room.room_number){
            return -1;
        }else{
            return 0;
        }
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void addUser(User user){
        this.userList.add(user);
    }

    public void removeUser(User user){
        this.userList.remove(user);
        if(getLeader() == user){
            if(userList.size() > 0){
                setLeader(userList.get(0));
                userList.get(0).setRoomLeader(true);
              //  System.out.println(user.getUserName()+" ??? ????????? "+userList.get(0).getUserName()+" ??? ????????????.");
            }
        }
    }

    public User getLeader() {
        return Leader;
    }

    public void setLeader(User leader) {
        Leader = leader;
    }

    public List<User> getBanList() {
        return banList;
    }

    public void sendPacket(byte[] packet){
        for(User user : userList){
            user.getSesson().writeAndFlush(packet);
        }
    }


    public void sendOpenInviteFriendRequest(User user){
        user.getFriendList().clear();
        user.setFriendList(Friend.getFriendsFromDB(user));

        List<User> friendList = new ArrayList<>();

        for(User friend : user.getFriendList()){
            User request = user.getLobby().findOnlineUserbyUserId(friend.getUserId());
            if (request != null) {
                if(request.getRoom() == null){
                    friendList.add(friend);
                }
            }
        }

        user.getSesson().writeAndFlush(RoomPacket.sendFriedList(friendList));
    }

    public void requestInviteFriend(User user, LittleEndianReader r){
        user.getFriendList().clear();
        user.setFriendList(Friend.getFriendsFromDB(user));

        long friendId = r.readLong();

        if(user.getRoom() == null){
            return;
        }

        for(User friend : user.getFriendList()) {
            if (friend.getUserId() == friendId) {
                User request = user.getLobby().findOnlineUserbyUserId(friendId);
                if (request != null) {
                    if(request.getRoom() == null){
                        request.getSesson().writeAndFlush(RoomPacket.openInviteRoomDialog(user, user.getRoom()));
                    }
                }
                break;
            }
        }
    }

    public void requestBanPlayer(User user,LittleEndianReader r){
        long banId = r.readLong();
        if(user.getUserId() == getLeader().getUserId()){

            if(banId == getLeader().getUserId()){ //?????? ?????? ??????
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("?????? ????????? ????????? ??? ????????????."));
                return;
            }

            if(boomSpin != null){
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("????????? ?????? ????????? ????????? ??? ????????????."));
                return;
            }

            User banP = null;
            for(User ban : userList){
                if(ban.getUserId() == banId){
                    banP = ban;
                    break;
                }
            }

            if(banP != null){
                banList.add(banP);
                getRoomBanExit(banP);
            }

        }else{ //????????? ????????????
            user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("????????? ????????? ???????????????."));
        }


    }

    public void getRoomBanExit(User user){
        sendNotice("#y["+user.getUserName()+"]#l ?????? #r???????????? ???????????????.#l");
        user.getSesson().writeAndFlush(RoomPacket.sendBanRoom());
        removeUser(user);
        setRoom_member_count(user.getRoom().getUserList().size());
        sendUserList();
        user.setRoom(null);
        user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("????????? ?????? ???????????? ??????????????????."));
    }



    public static void requestGameStart(User user, LittleEndianReader r){
        int code = r.readInt(); //????????????

        ChatRoom room = user.getRoom();

        if(room == null){
            System.out.println(user.getUserName()+ " ????????? ?????? NULL ?????????. (requestGameStart)");
            return;
        }

        if(room.boomSpin != null){
            room.sendNotice("?????? ????????? ?????????????????????.");
            return;
        }


        if(room.getUserList().size() < 1){
            room.sendNotice("#r????????? ???????????? ????????? ???????????? ??????????????????.#l");
        }else{
            room.sendNotice("#g?????? ??? ????????? ???????????????.#l");
            room.sendNotice("#r[?????? ????????? ????????? ?????? ?????? ???????????? ?????? ??? ????????????.]#l");
            room.setBoomSpin(new BoomSpin(room));

            room.setSchedule(Timer.RoomTimer.getInstance().register(new Runnable() {
                int i = 5;
                @Override
                public void run() {
                    room.sendNotice("#r"+i+"??? ??? ????????? ???????????????.#l");
                    i--;
                    if(i < 0){
                        room.sendPacket(RoomPacket.sendGameActivityJoin(0));
                        room.getBoomSpin().initStartGame();
                        room.getSchedule().cancel(false);
                        room.setSchedule(null);
                    }
                }
            },1000));

        }


    }






    public void sendNotice(String msg){
        sendPacket(RoomPacket.sendNoticeText(msg));
    }

    public void sendUserList(){
        sendPacket(RoomPacket.sendUserList(userList));
    }
}
