package Netty;

import Data.User;
import DevTool.LittleEndianReader;
import Friend.Friend;
import Game.BoomSpin.BoomSpin;
import Lobby.Lobby;
import Opcode.ReceiveOpcode;
import Packet.LobbyPacket;
import Packet.LoginPacket;
import Room.ChatRoom;
import Server.ChannelServer;
import Server.ServerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


public class NettyHandler extends ChannelInboundHandlerAdapter {

    private final ServerType serverType;
    private final int channel;

    public NettyHandler(ServerType type, int channel){
        this.serverType = type;
        this.channel = channel;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        final String address = ctx.channel().remoteAddress().toString().split(":")[0];
        User user = new User(null);
        switch (serverType) {
            case LOGIN:
                System.out.println("[알림] " + address + " 에서 로그인 서버에 접속했습니다.");
                ctx.channel().writeAndFlush(LoginPacket.sendLoginServerConnect(true));
                user = new User(ctx.channel());
                break;
            case CHANNEL:
                System.out.println("[알림] " + address + " 에서 채널 서버에 접속했습니다.");
                user = new User(ctx.channel(), ChannelServer.getLobby(channel));
                ChannelServer.getLobby(channel).addUser(user);
                break;
            case ETC:
                System.out.println("[알림] " + address + " 에서 테스트 서버에 접속했습니다.");
                break;
            default:
        }

        if(user.getSesson() != null){
            ctx.channel().attr(User.ClientKey).set(user);
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet){

        LittleEndianReader r = (LittleEndianReader) packet;

        final User user = (User) ctx.channel().attr(User.ClientKey).get();

        System.out.println("DEBUG [recv] "+r.toString());

        final int opcode = r.readShort();

        for(ReceiveOpcode recv : ReceiveOpcode.values()){
            if(recv.ordinal() == opcode){
                try {
                    handlePacket(user ,r,recv);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // 채널 비활성

        final String address = ctx.channel().remoteAddress().toString().split(":")[0];

        User user = ctx.channel().attr(User.ClientKey).get();

        if(user != null){
            if(user.getLobby() != null){
                user.getLobby().disconnectGame(user);
                user.getLobby().removeUser(user);
                user.setLobby(null);
            }

            System.out.println(address + " : "+user.getUserName()+" disconnected.");
        }
        ctx.channel().attr(User.ClientKey).set(null);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            User user = ctx.channel().attr(User.ClientKey).get();
             if (e.state() == IdleState.READER_IDLE) {
               //  System.out.println(user.getUserName()+"  읽기 시간 초과로 close.");
              //   ctx.close();
             } else if (e.state() == IdleState.WRITER_IDLE) {
                 ctx.writeAndFlush(LoginPacket.sendPing());
             }
        }
    }


    public static void handlePacket(User user,LittleEndianReader r, ReceiveOpcode opcode){

        System.out.println("RECV header : "+opcode.toString());

        switch (opcode){
            case SERVER_PONG:{
                break;
            }

            case SERVER_NOTICE_REQUEST:{
                user.sendServerNotice();
                break;
            }

            case USER_LOGIN:{
                User.checkUserLogin(user,r);
                break;
            }
            case USER_CHECK_NAME:{
                User.checkUserName(user,r);
                break;
            }

            case USER_CONNECT_CHANNEL:{
                user.getUserInfo(user,r);
                break;
            }

            case USER_PROFILE_IMAGE_CHANGE:{
                user.setProfileImage(user,r);
                break;
            }

            case USER_MEMO_CHANGE:{
                user.changeUserMemo(r);
                break;
            }

            case USER_OPEN_PROFILE:{
                user.sendOpenProfile(r);
                break;
            }

            case LOBBY_CREATE_ROOM:{
               user.getLobby().createRoom(user,r);
               break;
            }

            case LOBBY_ROOM_GETLIST:{
                user.getLobby().sendRoomList(user);
                break;
            }

            case ROOM_JOIN_REQUEST:{
                user.getLobby().getRoomJoinRequest(user,r);
                break;
            }

            case ROOM_SEND_CHAT:{
                user.getLobby().getUserChatRoom(user,r);
                break;
            }

            case ROOM_JOIN_USER:{
                user.getLobby().getRoomJoin(user,r);
                break;
            }

            case ROOM_EXIT_USER:{
                user.getLobby().getRoomExit(user,r);
                break;
            }

            case ROOM_BAN_USER:{
                user.getRoom().requestBanPlayer(user, r);
                break;
            }

            case ROOM_OPEN_INVITE_FRIEND:{
                user.getRoom().sendOpenInviteFriendRequest(user);
                break;
            }

            case ROOM_INVITE_FRIEND:{
                user.getRoom().requestInviteFriend(user,r);
                break;
            }

            case FRIEND_JOIN_ACTIVITY:{
                Friend.sendFriendActivityInfo(user);
                break;
            }

            case FRIEND_REMOVE:{
                Friend.removeFriend(user,r);
                break;
            }

            case FRIEND_SEARCH:{
                Friend.getSearchUserName(user,r);
                break;
            }

            case FRIEND_OPEN_REQUEST:{
                Friend.sendFriendRequestList(user);
                break;
            }

            case FRIEND_REQUEST_ADD:{
                Friend.HandleFriendAddRequest(user,r);
                break;
            }

            case FRIEND_REQUEST_REFUSE:{
                Friend.refuseFriendRequest(user,r);
                break;
            }

            case FRIEND_REQUEST_ACCEPT:{
                Friend.acceptFriendRequest(user,r);
                break;
            }

            case GAME_START_REQUEST:{
                ChatRoom.requestGameStart(user,r);
                break;
            }

            case GAME_BOOM_SPIN:{
                BoomSpin.handleBoomSpinPacket(user, r);
                break;
            }


        }

    }

}
