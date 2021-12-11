package Lobby;

import Data.User;
import DevTool.LittleEndianReader;
import DevTool.Timer;
import Game.BoomSpin.PlayerInfo;
import Packet.BoomSpinPacket;
import Packet.LobbyPacket;
import Packet.RoomPacket;
import Room.ChatRoom;
import Server.ChannelServer;
import Server.InitServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lobby {

    private List<ChatRoom> lobbyRoomList =  Collections.synchronizedList(new ArrayList<>());
    private static List<User> usersList = Collections.synchronizedList(new ArrayList<>());
    private int channel;
    private List<ServerNotice> serverNotices = new ArrayList<>();




    public Lobby(int channel){
        serverNotices.add(new ServerNotice("[안내]","BOOM을 이용해 주셔서 감사합니다","안내 공지의 내용입니다.","2021/10/14"));
        serverNotices.add(new ServerNotice("[업데이트]","클라이언트 1.0 베타 패치","클라이언트 1.0 베타 패치 내역 입니다.\n" +
                "1. 게임 추가\n" +
                "-능력 폭탄 돌리기 게임이 추가됩니다.\n" +
                "2. 기능 개선\n" +
                "-친구 추가 및 삭제가 가능하게 됩니다.","2021/10/1"));
        serverNotices.add(new ServerNotice("[보안]","개인 정보를 보호하세요.","받아오는 개인 정보도 없습니다..","2021/10/14"));
        serverNotices.add(new ServerNotice("[알림]","능력 폭탄돌리기 게임 베타테스트 진행중","그런 내용입니다..","2021/10/17"));
        serverNotices.add(new ServerNotice("[알림]","개발자 이야기","개발자의 이야기 공지 내용입니다..","2021/10/17"));
        serverNotices.add(new ServerNotice("[이벤트]","할로윈 이벤트 안내","그런거 없습니다","2021/10/31"));

        lobbyRoomList.add(new ChatRoom(0,"폭탄 돌리기 할사람(test room)",0));
        lobbyRoomList.add(new ChatRoom(1,"들어오셈(test room)",0));
        lobbyRoomList.add(new ChatRoom(2,"개념만 컴온(test room)",0));
        lobbyRoomList.add(new ChatRoom(3,"빨리 게임 추가좀요(test room)",0));
        lobbyRoomList.add(new ChatRoom(4,"폭탄돌리기는 이상한겜임(test room)",0));
        lobbyRoomList.add(new ChatRoom(5,"테스트용 방이에요1",0));
        lobbyRoomList.add(new ChatRoom(6,"테스트용 방입니다2",0));
        lobbyRoomList.add(new ChatRoom(7,"테스트용 방입니다3",0));
        this.channel = channel;
    }

    public void createRoom(User user, LittleEndianReader r){
        long userId = r.readLong();
        String roomName = r.readLengthAsciiString();

        int room_number = 0;

        Collections.sort(lobbyRoomList); //방 정렬
        for(ChatRoom room : lobbyRoomList){
            if(room_number != room.getRoom_number()){
                break;
            }
            room_number++;
        }

        ChatRoom room = createRoomInfo(user, room_number,roomName,8);
        user.setRoom(room);
        user.setRoomLeader(true);
        sendPacket(LobbyPacket.sendRoomList(lobbyRoomList));
        user.getSesson().writeAndFlush(RoomPacket.sendJoinRoomRequestResult(room,false,true,user));
    }

    public ChatRoom createRoomInfo(User user,int roomNumber, String roomName , int roomMemberMaxCount){
        ChatRoom room = new ChatRoom(roomNumber,roomName,roomMemberMaxCount);
        room.addUser(user);
        room.setLeader(user);
        room.setRoom_member_count(room.getUserList().size());
        lobbyRoomList.add(room);
        return room;
    }

    public void sendRoomList(User user){

        for(ChatRoom l :lobbyRoomList){
            System.out.println(l.getRoom_name());
        }

        user.getSesson().writeAndFlush(LobbyPacket.sendRoomList(lobbyRoomList));
    }

    public void getRoomJoinRequest(User user, LittleEndianReader r){
        r.readLong(); //userid
        int roomNum = r.readInt();

        boolean isAready = false;

        if(user.getRoom() != null){
            isAready = true;
        }

        boolean isSucceed = false;

        ChatRoom room = null;
        if(!isAready){
            for(ChatRoom l :lobbyRoomList){
                if(l.getRoom_number() == roomNum){
                    for(User ban : l.getBanList()){ //추방의 경우
                        if(ban.getUserId() == user.getUserId()){
                            user.getSesson().writeAndFlush(RoomPacket.sendJoinRoomRequestResult(room,true,false,user));
                            return;
                        }
                    }
                    if(l.getRoom_member_count() < l.getRoom_member_maxcount()){
                        l.addUser(user);
                        l.setRoom_member_count(l.getUserList().size());
                        user.setRoom(l);
                        room = l;
                        isSucceed = true;
                    }
                    break;
                }
            }
        }


        user.getSesson().writeAndFlush(RoomPacket.sendJoinRoomRequestResult(room,false,isSucceed,user));
    }

    public void getRoomJoin(User user, LittleEndianReader r){
        r.readLong();
        r.readLengthAsciiString();
        int roomNumber = r.readInt();

        for(ChatRoom l :lobbyRoomList){
            if(l.getRoom_number() == roomNumber){
                l.sendUserList();
                l.sendNotice("#y["+user.getUserName()+"]#l 님이 입장하였습니다.");
                break;
            }
        }
    }

    public void getRoomExit(User user, LittleEndianReader r){
        r.readLong();
        r.readLengthAsciiString();
        int roomNumber = r.readInt();

        ChatRoom room = null;

        for(ChatRoom l :lobbyRoomList){
            if(l.getRoom_number() == roomNumber){
                room = l;
                l.sendNotice("#y["+user.getUserName()+"]#l 님이 퇴장하였습니다.");
                l.removeUser(user);
                l.setRoom_member_count(l.getUserList().size());
                l.sendUserList();
                user.setRoom(null);
                user.setRoomLeader(false);
                break;
            }
        }

        deleteRoom(room);
    }

    public void getRoomExit(User user){

        ChatRoom room = user.getRoom();

        room.sendNotice("#y["+user.getUserName()+"]#l 님이 퇴장하였습니다.");
        room.removeUser(user);
        room.setRoom_member_count(user.getRoom().getUserList().size());
        room.sendUserList();
        user.setRoom(null);
        user.setRoomLeader(false);

        deleteRoom(room);
    }


    public void deleteRoom(ChatRoom room){
        if(room.getUserList().size() <= 0){
            lobbyRoomList.remove(room);
            sendPacket(LobbyPacket.sendRoomList(lobbyRoomList));
        }
    }

    public void setUsersList(List<User> usersList) {
        this.usersList = usersList;
    }

    public List<User> getUsersList() {
        return usersList;
    }

    public void addUser(User user){
        usersList.add(user);
    }

    public void removeUser(User user){
        usersList.remove(user);
    }

    public void getUserChatRoom(User user,LittleEndianReader r){
        int roomNumber = r.readInt();
        long userId = r.readLong();
        String userName = r.readLengthAsciiString();
        String msg = r.readLengthAsciiString();
        int userProfileImageCode = r.readInt();
        user.getRoom().sendPacket(RoomPacket.sendRoomChat(user, roomNumber, msg, userProfileImageCode));
    }

    public void sendPacket(byte[] packet){
        for(User user : usersList){
            System.out.println("로비 유저아이디 :"+ user.getUserId()+" 유저이름 :"+ user.getUserName());
            user.getSesson().writeAndFlush(packet);
        }
    }

    public void disconnectGame(User user){
        if(user.getRoom() != null){
            if(user.getRoom().getBoomSpin() != null){
                user.getRoom().getBoomSpin().getPlayer(user).setAlive(false);
                user.getRoom().getBoomSpin().setboltPlayer(user);
            }
            getRoomExit(user);
        }else {
            System.out.println(user.getUserName()+" 유저는 방이 없기에 그냥 종료됨");
        }

        //종료시 유저데이터 저장
        if(!user.saveToDB()){
            System.out.println(user.getUserName()+" 유저의 데이터 저장에 실패하였습니다.");
        }

        // 테스트 용
        if(!InitServer.isTest){
            User.setUserOnline(user,false);
        }
    }

    public User findOnlineUserbyUserId(long userId){
        for(User user : getUsersList()){
            if(user.getUserId() == userId){
                return  user;
            }
        }
        return null;
    }

    public List<ServerNotice> getServerNotices() {
        return serverNotices;
    }

    public static void shutdownServer(){
        //유저리스트를 스태틱 변수로 하면 나중에 채널마다 로비클래스 생성이 의미가 있을까..? 아마 로비(채널별)과 전체 유저리스트 따로따로 나눠야될듯.

        usersList.forEach(user -> {
            if(!user.saveToDB()){
                System.out.println(user.getUserName()+" 유저의 정보를 저장하지 못함.");
            }
        });
    }

}
