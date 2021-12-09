package Server;

import Lobby.Lobby;
import Netty.NettyDecoder;
import Netty.NettyEncoder;
import Netty.NettyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelServer {
    private static ServerBootstrap bootstrap;
    private static Map<Integer,Lobby> lobby = new HashMap<>();


    public static void run_ChannelServer(int port, int channel){


        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("encoder",new NettyEncoder());
                            ch.pipeline().addLast("decoder",new NettyDecoder());
                            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(30,10,0));
                            ch.pipeline().addLast("handler",new NettyHandler(ServerType.CHANNEL,channel));

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_SNDBUF, 4096 * 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = bootstrap.bind(port).sync();

            lobby.put(channel,new Lobby(channel));
            System.out.println("[알림] 채널 "+channel+ " 서버가 " + port + " 포트를 성공적으로 개방하였습니다.");

        }catch (InterruptedException e){
            System.err.println("[오류] 채널 "+channel+ " 서버가 " + port + " 포트를 개방하는데 실패했습니다.");
            e.printStackTrace();

        }
    }

    public static Lobby getLobby(int channel) {
        return lobby.get(channel);
    }
}
