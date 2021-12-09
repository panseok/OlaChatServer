package Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import Netty.NettyDecoder;
import Netty.NettyEncoder;
import Netty.NettyHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class LoginServer {
    private static ServerBootstrap bootstrap;

    public static final void run_loginServer(int port){

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
                            ch.pipeline().addLast("handler",new NettyHandler(ServerType.LOGIN,-1));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_SNDBUF, 4096 * 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = bootstrap.bind(port).sync(); // (7)
          //  f.channel().closeFuture().sync();
            System.out.println("[알림] 로그인서버가 " + port + " 포트를 성공적으로 개방하였습니다.");

        }catch (InterruptedException e){
            System.err.println("[오류] 로그인서버가 " + port + " 포트를 개방하는데 실패했습니다.");
            e.printStackTrace();

        }/*finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }*/

    }


}
