package Data;

import DevTool.LittleEndianReader;
import DevTool.LittleEndianWriter;
import Friend.Friend;
import Lobby.Lobby;
import MySql.DatabaseConnection;
import Packet.FriendPacket;
import Packet.LobbyPacket;
import Packet.LoginPacket;
import Room.ChatRoom;
import Server.InitServer;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class User {
    private long userId;
    private String userName, userMemo;
    public final static AttributeKey<User> ClientKey = AttributeKey.valueOf("olachat_user");
    private transient Channel sesson;
    private Lobby lobby;
    private ChatRoom room = null;
    private int userProfileImageCode ,userWin, userLose, userPopularity;
    private List<User> friendList = Collections.synchronizedList(new ArrayList<>());
    private List<User> requestFriendList = Collections.synchronizedList(new ArrayList<>());
    private boolean isLogin = false, isRoomLeader = false;



    public User(Channel channel){
        this.sesson = channel;
    }

    public User(Channel channel, Lobby lobby){
     this.sesson = channel;
     this.lobby = lobby;
    }

    public User(long userId, String userName){
        this.userId = userId;
        this.userName = userName;
    }

    public User(){

    }

    public boolean saveToDB(){
        boolean record = saveUserRecord();

        return record;
    }

    public void getUserInfoFromDB(long userId){
        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        String name = "";
        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");

            ps.setLong(1, userId);

            rs = ps.executeQuery();


            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("유저이름을 불러올수 없습니다.");
            }

            this.userId = userId;
            this.userName = rs.getString("userName");
            this.userProfileImageCode = rs.getInt("userProfileImageCode");
            this.userWin = rs.getInt("userWin");
            this. userLose = rs.getInt("userLose");
            this.userPopularity = rs.getInt("userPopularity");
            this.userMemo = rs.getString("userMemo");
            this.isLogin = rs.getByte("isLogin") == 1;

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void setAllUserLogOffToDB(){
        boolean isOnline = false;
        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();


            ps = con.prepareStatement("SELECT * FROM user");
            rs = ps.executeQuery();

            while (rs.next()){
                ps2 = con.prepareStatement("UPDATE user SET isLogin = ?");
                ps2.setByte(1,(byte) 0);
                ps2.executeUpdate();
                ps2.close();
            }

            ps.close();
            rs.close();
            con.close();

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public static boolean getAccountExists(User user, long userId) {
        boolean isExists = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT userId FROM user WHERE userId = ?");

            ps.setLong(1, userId);

            rs = ps.executeQuery();

            if (rs.first()) {
                user.setUserId(userId);
                isExists = true;
            }

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return isExists;
    }

    public static boolean getNameExists(User user, String userName) {
        boolean isExists = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT userId FROM user WHERE userName = ?");

            ps.setString(1, userName);

            rs = ps.executeQuery();

            if (rs.first()) {
                isExists = true;
            }

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return isExists;
    }

    public boolean saveUserRecord() {

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");
            ps.setLong(1,userId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(createUserNameToDB) 유저ID를 불러올수 없습니다.");
            }

            ps = con.prepareStatement("UPDATE user SET userWin = ?,  userLose = ?  WHERE userId = ?");

            ps.setInt(1,userWin);
            ps.setInt(2,userLose);
            ps.setLong(3,userId);
            ps.executeUpdate();

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getUserName(long userId) {

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        String name = "";
        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");

            ps.setLong(1, userId);

            rs = ps.executeQuery();


            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                return name;
            }

            name = rs.getString("userName");


            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return name;
    }

    public static boolean checkUserNameToDB(String userName){
        boolean isExists = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        String name = "";
        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM user");
            rs = ps.executeQuery();


            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(checkUserNameToDB) 유저이름을 불러올수 없습니다.");
            }

            name = rs.getString("userName");

            if(name.equals(userName)){
                isExists = true;
            }

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return isExists;
    }


    //유저 검색용.. 코딩중
    public static long getUserIdByUserName(String userName){
        long userId = 0;

        boolean isExists = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT * FROM user WHERE userName = ?");

            ps.setString(1,userName);

            rs = ps.executeQuery();


            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                System.out.println("DB에서 유저ID를 찾을 수 없습니다.");
                return -1;
            }

           userId = rs.getLong("userId");

            ps.close();
            rs.close();
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return userId;
    }



    public static boolean createUserDataDB(User user, long userId, String userName){
        boolean isSucceed = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("INSERT INTO user (userId, userName, userProfileImageCode, userWin, userLose, userPopularity, userMemo, isLogin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setLong(1,userId);
            ps.setString(2,userName);
            ps.setInt(3,0);
            ps.setInt(4,0);
            ps.setInt(5,0);
            ps.setInt(6,0);
            ps.setString(7,"");
            ps.setByte(8,(byte) 0);
            ps.executeUpdate();
            ps.close();
            con.close();

            isSucceed = true;


        }catch (SQLException e){
            e.printStackTrace();
        }


        return isSucceed;
    }


    public static boolean createUserNameToDB(long userId, String userName){
        boolean isSucceed = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();


            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");
            ps.setLong(1,userId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(createUserNameToDB) 유저ID를 불러올수 없습니다.");
            }


            ps = con.prepareStatement("UPDATE user SET userName = ? WHERE userId = ?");
            ps.setString(1,userName);
            ps.setLong(2,userId);
            ps.executeUpdate();

            ps.close();
            rs.close();
            con.close();

            isSucceed = true;

        }catch (SQLException e){
            e.printStackTrace();
        }


        return isSucceed;
    }

    public static boolean changeuserMemoToDB(long userId, String userMemo){
        boolean isSucceed = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();


            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");
            ps.setLong(1,userId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(createUserNameToDB) 유저ID를 불러올수 없습니다.");
            }


            ps = con.prepareStatement("UPDATE user SET userMemo = ? WHERE userId = ?");
            ps.setString(1,userMemo);
            ps.setLong(2,userId);
            ps.executeUpdate();

            ps.close();
            rs.close();
            con.close();

            isSucceed = true;

        }catch (SQLException e){
            e.printStackTrace();
        }




        return isSucceed;
    }



    public static boolean changeUserProfileImageToDB(long userId, int userProfileImageCode){
        boolean isSucceed = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();


            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");
            ps.setLong(1,userId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(changeUserProfileImageToDB) 유저ID를 불러올수 없습니다.");
            }


            ps = con.prepareStatement("UPDATE user SET userProfileImageCode = ? WHERE userId = ?");
            ps.setInt(1,userProfileImageCode);
            ps.setLong(2,userId);
            ps.executeUpdate();

            ps.close();
            rs.close();
            con.close();

            isSucceed = true;

        }catch (SQLException e){
            e.printStackTrace();
        }


        return isSucceed;
    }

    public static boolean setUserOnline(User user, boolean isLogin){
        boolean isSucceed = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();


            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");
            ps.setLong(1,user.getUserId());
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(changeUserProfileImageToDB) 유저ID를 불러올수 없습니다.");
            }


            ps = con.prepareStatement("UPDATE user SET isLogin = ? WHERE userId = ?");
            ps.setByte(1,isLogin ? (byte) 1 : 0);
            ps.setLong(2,user.getUserId());
            ps.executeUpdate();

            ps.close();
            rs.close();
            con.close();

            isSucceed = true;

            user.setLogin(isLogin);

        }catch (SQLException e){
            e.printStackTrace();
        }


        return isSucceed;
    }

    public static boolean isUserOnline(long userId){
        boolean isLogin = false;

        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();


            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");
            ps.setLong(1,userId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(changeUserProfileImageToDB) 유저ID를 불러올수 없습니다.");
            }

            isLogin = rs.getByte("isLogin") == 1;

            ps.close();
            rs.close();
            con.close();

        }catch (SQLException e){
            e.printStackTrace();
        }


        return isLogin;
    }


    public static boolean getUserOnlineToDB(long userid){
        boolean isOnline = false;
        Connection con = null;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();


            ps = con.prepareStatement("SELECT * FROM user WHERE userId = ?");
            ps.setLong(1,userid);
            rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                con.close();
                throw new RuntimeException("(changeUserProfileImageToDB) 유저ID를 불러올수 없습니다.");
            }

            isOnline = rs.getByte("isLogin") == 1;

            ps.close();
            rs.close();
            con.close();

        }catch (SQLException e){
            e.printStackTrace();
        }

        return isOnline;

    }


    /*
     * 0 : 가입 성공
     * 1 : 가입 실패
     * 2 : 닉네임 생성 성공
     * 3 : 닉네임 생성 실패
     * 4 : 로그인 성공 (이미 계정 있음)
     * 5 : 이미 있는 닉네임
     * 6 : 기타
     * */



    public void getUserInfo(long userId){

        // 테스트 용
        if(InitServer.isTest){
            Random rand = new Random();
            this.userId = rand.nextInt();
            this.userName = "플레이어"+rand.nextInt(100);
            this.userProfileImageCode = rand.nextInt(11);
            this.userWin = rand.nextInt();
            this.userLose = rand.nextInt();
            this.userPopularity = rand.nextInt();
            this.userMemo = "자기소개를 적는 곳입니다 !!";
            this.isLogin = true;
        }else{
            getUserInfoFromDB(userId); //유저 정보
            setRequestFriendList(Friend.getRequestFriendInfoToDB(this)); //친구 요청 목록
            setFriendList(Friend.getFriendsFromDB(this)); //친구 목록

        }

    }

    public void getUserInfo(User user,LittleEndianReader r){
        long userId = r.readLong();
        getUserInfo(userId);
        getSesson().writeAndFlush(LoginPacket.sendConnectChannelServerSucceed(user));
    }

    public static void succeedLogin(User user){
        user.getUserInfo(user.getUserId());

        //테스트 용 주석
        if(!InitServer.isTest){
            User.setUserOnline(user,true);
        }
        user.getSesson().writeAndFlush(LoginPacket.sendConnectChannelServer());
    }

    public static void checkUserName(User user,LittleEndianReader r){
        String userName = r.readLengthAsciiString();

        if(User.isUserOnline(user.getUserId())){
            user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 6, "현재 접속 중인 ID입니다."));
        }else{
            boolean isExists = checkUserNameToDB(userName);

            if(!isExists){
                if(createUserNameToDB(user.getUserId(),userName)){
                    user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 2));
                    succeedLogin(user);
                }else{
                    user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 6,"닉네임 설정에 오류가 발생하였습니다. 다시 시도하세요."));
                }
            }else{
                user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 5));
            }
        }
    }

    public static void checkUserLogin(User user,LittleEndianReader r){
        long id = r.readLong();

        if(!InitServer.isTest){
            //계정 체크 및 생성
            if(!getAccountExists(user, id)){
                if (createUserDataDB(user, id, "")) {
                    user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 0));
                } else {
                    user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 1));
                }
            }

            user.setUserId(id);
            //닉네임 체크 및 생성
            if(User.getUserName(user.getUserId()).equals("")){
                user.getSesson().writeAndFlush(LoginPacket.sendCreateUserName());
            }else{
                if(User.isUserOnline(id)){
                    user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 6, "현재 접속 중인 ID입니다."));
                }else {
                    user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 4));
                    succeedLogin(user);
                }
            }
        }else{
            //테스트용

       Random rand = new Random();
        user.setUserId(rand.nextInt());

        user.getSesson().writeAndFlush(LoginPacket.sendLoginStatus((short) 4));
        succeedLogin(user);
        }

    }



    public void setProfileImage(User user,LittleEndianReader r){
        int profileImageCode = r.readInt();

        boolean isSucceed = changeUserProfileImageToDB(user.getUserId(),profileImageCode);
        if(isSucceed){
            user.setUserProfileImageCode(profileImageCode);
        }
        user.getSesson().writeAndFlush(LobbyPacket.sendChangeProfileImageResult(isSucceed, user.getUserProfileImageCode()));

    }


    public void changeUserMemo(LittleEndianReader r){
        String memo = r.readLengthAsciiString();
        if(changeuserMemoToDB(getUserId(),memo)){
            getSesson().writeAndFlush(LobbyPacket.sendChangeUserMemo(memo,true));
        }else{
            getSesson().writeAndFlush(LobbyPacket.sendChangeUserMemo(getUserMemo(),false));
        }
    }

    public void sendOpenProfile(LittleEndianReader r){
        long userid = r.readLong();
        User user = new User();

        if(User.isUserOnline(userid)){
            user = lobby.findOnlineUserbyUserId(userid);
        }else{
            user.getUserInfoFromDB(userid);
        }

        getSesson().writeAndFlush(LobbyPacket.sendOpenUserProfile(user,true));
    }


    public void sendServerNotice(){
        getSesson().writeAndFlush(LobbyPacket.sendServerNoticeList(getLobby().getServerNotices()));
    }

    public byte[] sendUserInfo(LittleEndianWriter o){
        o.writeLong(getUserId());
        o.writeLengthAsciiString(getUserName());
        o.writeInt(getUserProfileImageCode());
        o.writeInt(getUserWin());
        o.writeInt(getUserLose());
        o.writeInt(getUserPopularity());
        o.writeLengthAsciiString(getUserMemo());
        o.write(isLogin());
        o.write(isRoomLeader());
        return o.getPacket();
    }

    public List<User> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
    }

    public List<User> getRequestFriendList() {
        return requestFriendList;
    }

    public void setRequestFriendList(List<User> requestFriendList) {
        this.requestFriendList = requestFriendList;
    }

    public ChatRoom getRoom() {
        return room;
    }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public int getUserLose() {
        return userLose;
    }

    public int getUserPopularity() {
        return userPopularity;
    }

    public int getUserWin() {
        return userWin;
    }

    public int getUserProfileImageCode() {
        return userProfileImageCode;
    }

    public void setUserLose(int userLose) {
        this.userLose = userLose;
    }

    public void setUserPopularity(int userPopularity) {
        this.userPopularity = userPopularity;
    }

    public void setUserWin(int userWin) {
        this.userWin = userWin;
    }

    public void setUserProfileImageCode(int userProfileImageCode) {
        this.userProfileImageCode = userProfileImageCode;
    }

    public String getUserMemo() {
        return userMemo;
    }

    public void setUserMemo(String userMemo) {
        this.userMemo = userMemo;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public Channel getSesson() {
        return sesson;
    }

    public void setSesson(Channel sesson) {
        this.sesson = sesson;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public boolean isRoomLeader() {
        return isRoomLeader;
    }

    public void setRoomLeader(boolean roomLeader) {
        isRoomLeader = roomLeader;
    }

    public void addUserWin(){
        userWin += 1;
    }

    public void addUserLose(){
        userLose += 1;
    }
}
