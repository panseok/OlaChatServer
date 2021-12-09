package Server;

import Data.User;
import DevTool.Timer;
import Lobby.Lobby;
import MySql.DatabaseConnection;
import com.mysql.jdbc.MySQLConnection;

public class InitServer {

    public static boolean isTest =  false;

    public static class Shutdown implements Runnable{

        @Override
        public void run(){
            System.out.printf("서버가 종료됩니다..");
            Lobby.shutdownServer();
        }

    }

    public static void main(String args[]){
        System.out.println("서버 구동 시작");

        DatabaseConnection.init();
        User.setAllUserLogOffToDB();
        LoginServer.run_loginServer(8484);
        ChannelServer.run_ChannelServer(8485,0);

        /*for(int i = 1; i <= 5 ; i++){
            ChannelServer.run_ChannelServer(8484+i,i);
        }*/

        Timer.RoomTimer.getInstance().start();
        Timer.BoomSpinTimer.getInstance().start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
        System.out.println("서버 구동 완료");
    }



}



