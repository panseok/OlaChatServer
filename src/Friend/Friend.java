package Friend;

import Data.User;
import DevTool.LittleEndianReader;
import MySql.DatabaseConnection;
import Packet.FriendPacket;
import Packet.LobbyPacket;
import Server.ChannelServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Friend {

    public static List<User> getFriendsFromDB(User user){

        List<User> friendList = new ArrayList<>();

        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        String name = "";
        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM friend WHERE userid = ?");

            ps.setLong(1, user.getUserId());

            rs = ps.executeQuery();

            while(rs.next()){
                long friendId = rs.getLong("friendid");

                User friend = new User();
                friend.getUserInfoFromDB(friendId);
                friendList.add(friend);
              //  System.out.println("getFriendInfoDB debug " +user.getUserName() + " addfriend id : "+ friend.getUserName());
            }

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }


        return friendList;
    }

    public static boolean addRequestFriendtoDB(User user, long requestId){
        boolean isSucceed = false;

        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("INSERT INTO friend_request (userid, requestid) VALUES (?, ?)");
            ps.setLong(1,user.getUserId());
            ps.setLong(2,requestId);
            ps.executeUpdate();
            ps.close();
            con.close();
            con.close();
            isSucceed = true;

        }catch (SQLException e){
            e.printStackTrace();
        }
        return isSucceed;
    }


    public static List<User> getRequestFriendInfoToDB(User user){

        List<User> userList = new ArrayList<>();

        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM friend_request WHERE requestid = ?");

            ps.setLong(1, user.getUserId());

            rs = ps.executeQuery();

            while(rs.next()) {
                long friendId = rs.getLong("userid");
                User request = new User();
                request.getUserInfoFromDB(friendId);
                userList.add(request);
            }

            ps.close();
            rs.close();
            con.close();

            }catch (SQLException e){
            e.printStackTrace();
        }

        return userList;

    }

    public static List<User> getMyRequestList (long userid){

        List<User> userList = new ArrayList<>();

        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM friend_request WHERE userid = ?");

            ps.setLong(1, userid);

            rs = ps.executeQuery();

            while(rs.next()) {
                long friendId = rs.getLong("requestid");
                User request = new User();
                request.getUserInfoFromDB(friendId);
                userList.add(request);
            }

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return userList;

    }


    public static boolean addFriendtoDB(User user, long friendId){
        boolean isSucceed = false;

        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("INSERT INTO friend (userid, friendid) VALUES (?, ?)");
            ps.setLong(1,user.getUserId());
            ps.setLong(2,friendId);
            ps.executeUpdate();
            ps.close();

            ps2 = con.prepareStatement("INSERT INTO friend (userid, friendid) VALUES (?, ?)");
            ps2.setLong(1,friendId);
            ps2.setLong(2,user.getUserId());
            ps2.executeUpdate();
            ps2.close();

            con.close();
            isSucceed = true;



        }catch (SQLException e){
            e.printStackTrace();
            isSucceed = false;
        }
        return isSucceed;
    }

    public static boolean deleteFriend(User user, long friendId){
        boolean isSucceed = true;


        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("DELETE FROM friend WHERE userid = ? and friendid = ?");

            ps.setLong(1, user.getUserId());
            ps.setLong(2, friendId);

            ps.executeUpdate();

            ps.close();

            ps2 = con.prepareStatement("DELETE FROM friend WHERE friendid = ? and userid = ?");

            ps2.setLong(1, user.getUserId());
            ps2.setLong(2, friendId);

            ps2.executeUpdate();

            ps2.close();

            con.close();
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("friend remove : no data");
            isSucceed = false;
        }

        return isSucceed;
    }

    public static boolean deleteFriendRequest(User user, long requestId){
        boolean isSucceed = true;


        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("DELETE FROM friend_request WHERE userid = ? and requestid = ?");

            ps.setLong(1, requestId);
            ps.setLong(2, user.getUserId());

            ps.executeUpdate();

            ps.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("requestF remove : no data");
            isSucceed = false;
        }

        return isSucceed;
    }

    public static void sendFriendRequestList(User user){
        user.getSesson().writeAndFlush(FriendPacket.sendFriendRequestList(user.getRequestFriendList()));
    }



    public static void sendFriendActivityInfo(User user){

        user.getFriendList().clear();
        user.setFriendList(Friend.getFriendsFromDB(user));
        user.getRequestFriendList().clear();
        user.setRequestFriendList(Friend.getRequestFriendInfoToDB(user));

        user.getSesson().writeAndFlush(FriendPacket.sendFriendList(user.getFriendList()));
        user.getSesson().writeAndFlush(FriendPacket.sendFriendRequestList(user.getRequestFriendList()));
    }


    public static void getSearchUserName(User user, LittleEndianReader r){
        String searchName = r.readLengthAsciiString();
        long targetId = User.getUserIdByUserName(searchName);
        User target = new User();

        if(User.isUserOnline(targetId)){
            target = user.getLobby().findOnlineUserbyUserId(targetId);
        }else{
            target.getUserInfoFromDB(targetId);
        }

        boolean isExist = false;
        if(target != null){
            isExist = true;
        }
        user.getSesson().writeAndFlush(LobbyPacket.sendOpenUserProfile(target,isExist));
    }

    public static void HandleFriendAddRequest(User user,LittleEndianReader r){
        long requestid = r.readLong();

        boolean isRequested = false;
        boolean isFriend = false;
        boolean isOwn = false;

        if(user.getUserId() == requestid){
            isOwn = true;
            user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("자기 자신에게는 친구요청을 할 수 없습니다."));
        }

        if(!isOwn){

            for(User request : Friend.getMyRequestList(user.getUserId())){
                if(request.getUserId() == requestid){
                    isRequested = true;
                }
            }

            for(User friend : user.getFriendList()){
                if(friend.getUserId() == requestid){
                    isFriend = true;
                }
            }

            if(isFriend){
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("이미 친구 입니다."));
            }else if(isRequested){
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("이미 요청을 보낸 유저입니다."));
            }

        }

        if(!isFriend && !isRequested && !isOwn){
            Friend.addRequestFriendtoDB(user,requestid);
            user.setRequestFriendList(Friend.getRequestFriendInfoToDB(user)); //갱신
            user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구 요청을 보냈습니다."));

            User request = user.getLobby().findOnlineUserbyUserId(requestid);
            if(request != null){
                request.setRequestFriendList(Friend.getRequestFriendInfoToDB(request));
                request.getSesson().writeAndFlush(FriendPacket.sendFriendRequestList(request.getRequestFriendList()));
                request.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg(user.getUserName()+" 님이 친구 요청을 하였습니다."));
            }

        }

    }

    public static void removeFriend(User user, LittleEndianReader r){
        long friendId = r.readLong();

        boolean isFriend = false;

        for(User friend : user.getFriendList()){
            if(friend.getUserId() == friendId){
                isFriend = true;
            }
        }
        if(isFriend){
            boolean isSucceed = Friend.deleteFriend(user, friendId);

            user.getFriendList().clear();
            user.setFriendList(Friend.getFriendsFromDB(user));
            user.getSesson().writeAndFlush(FriendPacket.sendFriendList(user.getFriendList()));

            if(isSucceed){
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구 삭제에 성공하였습니다."));

                User friend = user.getLobby().findOnlineUserbyUserId(friendId);
                if(friend != null){
                    friend.setFriendList(Friend.getFriendsFromDB(friend));
                    friend.getSesson().writeAndFlush(FriendPacket.sendFriendList(friend.getFriendList()));
                    friend.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg(user.getUserName()+" 님이 친구를 끊었습니다."));
                }

            }else{
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구 삭제에 실패하였습니다."));
            }
        }else{
            user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구가 아닌 유저입니다."));
        }

    }

    public static void refuseFriendRequest(User user, LittleEndianReader r){
        long requestid = r.readLong();

        boolean isRequest = false;

        for(User request : user.getRequestFriendList()){
            if(request.getUserId() == requestid){
                isRequest = true;
            }
        }

        if(isRequest){
            boolean isSucceed = Friend.deleteFriendRequest(user, requestid);

            user.getRequestFriendList().clear();
            user.setRequestFriendList(Friend.getRequestFriendInfoToDB(user));
            user.getSesson().writeAndFlush(FriendPacket.sendFriendRequestList(user.getRequestFriendList()));

            if(isSucceed){
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구 요청을 거절하였습니다."));

                User request = user.getLobby().findOnlineUserbyUserId(requestid);
                if(request != null){
                    request.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg(user.getUserName()+" 님이 친구 요청을 거절하였습니다."));
                }

            }else{
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구 요청 처리에 에러가 발생하였습니다."));
            }
        }else{
            user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("이미 거절한 유저입니다."));
        }
    }

    public static void acceptFriendRequest(User user, LittleEndianReader r){
        long friendid = r.readLong();

        boolean isFriend = false;

        for(User request : user.getFriendList()){
            if(request.getUserId() == friendid){
                isFriend = true;
            }
        }
        boolean isSucceed2 = Friend.deleteFriendRequest(user, friendid);
        user.getRequestFriendList().clear();
        user.setRequestFriendList(Friend.getRequestFriendInfoToDB(user));
        user.getSesson().writeAndFlush(FriendPacket.sendFriendRequestList(user.getRequestFriendList()));

        if(!isFriend){
            boolean isSucceed = Friend.addFriendtoDB(user,friendid);

            user.getFriendList().clear();
            user.setFriendList(Friend.getFriendsFromDB(user));
            user.getSesson().writeAndFlush(FriendPacket.sendFriendList(user.getFriendList()));

            if(isSucceed && isSucceed2){

                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구 요청을 수락하였습니다."));
                User request = user.getLobby().findOnlineUserbyUserId(friendid);

                if(request != null){
                    request.getFriendList().clear();
                    request.setFriendList(Friend.getFriendsFromDB(request));
                    request.getSesson().writeAndFlush(FriendPacket.sendFriendList(request.getFriendList()));
                    request.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg(user.getUserName()+" 님이 친구 요청을 수락하였습니다."));
                }

            }else{
                user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("친구 요청 처리에 에러가 발생하였습니다."));
            }
        }else{
            user.getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("이미 친구입니다."));
        }

    }




}
