package Game.BoomSpin;

import Data.User;
import DevTool.LittleEndianReader;
import DevTool.LittleEndianWriter;
import DevTool.Timer;
import Packet.BoomSpinPacket;
import Packet.FriendPacket;
import Packet.LobbyPacket;
import Packet.RoomPacket;
import Room.ChatRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

public class BoomSpin{

    private List<User> userList = Collections.synchronizedList(new ArrayList<>());
    private List<PlayerInfo> players = Collections.synchronizedList(new ArrayList<>());
    private ScheduledFuture<?> schedule = null, schedule2 = null, boom_schedule = null, auction_start_schedule = null,auction_schedule = null;
    private BoomSpinAuction boomSpinAuction = null;

    private String[] names = {"1Player","2Player","3Player","4Player","5Player","6Player","7Player","8Player"};

    private int day = 0, auctionTimeSec = 15, time = 0;
    private PlayerInfo boomGetPlayer = null;

    private boolean isRandomSpin = false;
    private boolean isAuctionNow = false;
    private boolean isBoomTimerRunning = false;
    private static Random random = new Random();

    public BoomSpin(ChatRoom chatRoom){
        userList = chatRoom.getUserList();
    }


    public void setboltPlayer(User user){

        PlayerInfo player = getPlayer(user);

        player.setAlive(false);

        if(player.isMyturn()){
            player.setMyturn(false);

            boolean isNext = false;
            boolean isSucceed = false;
            PlayerInfo nextPlayer_ = null;

            if(isRandomSpin){
                List<PlayerInfo> alives = getAlivePlayerList();
                List<PlayerInfo> deads = getDeadPlayerList();

                Collections.shuffle(alives);

                players.clear();
                players.addAll(alives);
                players.addAll(deads);
            }

            for(PlayerInfo nextPlayer : players){
                if(isNext && nextPlayer.isAlive()){
                    nextPlayer.setMyturn(true);
                    nextPlayer_ = nextPlayer;
                    isSucceed = true;
                    break;
                }
                if(nextPlayer == player){
                    isNext = true;
                }
            }

            if(!isSucceed){ //????????? ???????????? ?????? ?????? ???????????? ??????.
                for(PlayerInfo nextPlayer : players){
                    if(nextPlayer.isAlive() && nextPlayer != player){
                        nextPlayer.setMyturn(true);
                        nextPlayer_ = nextPlayer;
                        break;
                    }
                }
            }

            if(nextPlayer_ != null){
                if(nextPlayer_.getLockSpinSec() > 0){
                    setLockSpin(nextPlayer_,0);
                }
            }
            sendNotice("#r["+user.getUserName()+"] ?????? [??????]?????? ?????? ?????????????????? ????????? ???????????????.#l");
        }
        sendNotice("#r["+user.getUserName()+"] ?????? [??????]?????? [??????]???????????????.#l");

        sendPacket(BoomSpinPacket.sendBoomSpinPlayerList(players));

    }

    public void sendMoney(){
        for(PlayerInfo player : players){
            player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinMoney(player.getMoney()));
        }
    }

    public void sendDay(int day){
        sendPacket(BoomSpinPacket.sendBoomSpinDay(day));
    }

    public void sendPacket(byte[] packet){
        for(User user : userList){
            user.getSesson().writeAndFlush(packet);
        }
    }

    public void sendRefreshUserInfo(){
        for(User user : userList){
            user.getSesson().writeAndFlush(LobbyPacket.sendUserInfo(user));
        }
    }

    public void sendNotice(String msg){
        sendPacket(BoomSpinPacket.sendBoomSpinNotice(msg));
    }

    public PlayerInfo getPlayer(User user){
        for(PlayerInfo player : players){
            if(player.getUser().getUserId() == user.getUserId()){
                return player;
            }
        }
        return null;
    }

    public void sendUserList(){
        sendPacket(RoomPacket.sendUserList(userList));
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }

    public ScheduledFuture<?> getSchedule2() {
        return schedule2;
    }

    public void setSchedule2(ScheduledFuture<?> schedule2) {
        this.schedule2 = schedule2;
    }

    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    public ScheduledFuture<?> getBoom_schedule() {
        return boom_schedule;
    }

    public PlayerInfo getBoomGetPlayer() {
        return boomGetPlayer;
    }

    public ScheduledFuture<?> getAuction_schedule() {
        return auction_schedule;
    }

    public void setAuction_schedule(ScheduledFuture<?> auction_schedule) {
        this.auction_schedule = auction_schedule;
    }

    public ScheduledFuture<?> getAuction_start_schedule() {
        return auction_start_schedule;
    }

    public void setAuction_start_schedule(ScheduledFuture<?> auction_start_schedule) {
        this.auction_start_schedule = auction_start_schedule;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void setBoom_schedule(ScheduledFuture<?> boom_schedule) {
        this.boom_schedule = boom_schedule;
    }

    public BoomSpinAuction getBoomSpinAuction() {
        return boomSpinAuction;
    }

    public void setBoomSpinAuction(BoomSpinAuction boomSpinAuction) {
        this.boomSpinAuction = boomSpinAuction;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void addTime(int add){
        this.time += add;
    }

    public static void handleBoomSpinPacket(User user, LittleEndianReader r){
        int code = r.readInt();
        switch (code) {
            case 0: {
                break;
            }
            case 1: {
                break;
            }
            case 4: {

                break;
            }
            case 6: { //??????
                long userid = r.readLong();
                String userName = r.readLengthAsciiString();
                String userMsg = r.readLengthAsciiString();
                int userProfileimgCode = r.readInt();
                user.getRoom().getBoomSpin().sendPacket(BoomSpinPacket.sendBoomSpinChat(user,userMsg,userProfileimgCode));
                break;
            }
            case 7: { //?????? ??????
                int price = r.readInt();
                handleAutionBid(user,price);
                break;
            }
            case 8:{ //?????? ?????????
                handleBoomSpin(user);
                break;
            }
        }
    }

    public void initStartGame(){
        sendJoinMsg();
        runGame();
    }

    public void runGame(){
        day++;
        sendGameInfo();
        prepareAuction();
    }

    public void sendJoinMsg(){
        setSchedule(Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                sendPacket(BoomSpinPacket.sendSkillbookInfo());
                sendNotice("[?????? ????????? ????????? ?????? ?????? ???????????????]");
                sendNotice("#y[????????? ???????????????]#l");
            }
        },1000));
    }

    public void startBoomTimer(){
        time = random.nextInt(60)+60;
        setBoom_schedule(Timer.BoomSpinTimer.getInstance().register(new Runnable() {
            int cout = 0;
            @Override
            public void run() {
                if(!isBoomTimerRunning){
                    isBoomTimerRunning = true;
                }

                cout++;
                if(cout % 30 == 0){
                    sendPacket(BoomSpinPacket.sendBoomSpinNotice("#b["+cout+"???]#l#w ?????? ???????????????.#l"));
                }

                /*for(int i = 0; i < 100; i++){ //??????????????? ???????????? ???????????? ?????? ?????????????????? ???????????? ????????????
                    sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r???#l#o???#l#y???#l#g???#l#b???#l#p???#l"));
                }*/

                for(PlayerInfo player : players){
                    if(player.isAlive()){
                        if(player.hasSkill(12)){
                            player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinBoomTime(time+""));
                        }else{
                            player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinBoomTime("?"));
                        }
                    }else{
                        player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinBoomTime("?"));
                    }
                }

                for(PlayerInfo player : players){
                    if(player.isAlive() && player.isMyturn()){
                        if(player.hasSkill(2)){ //?????????
                            player.addBoomskill_percent(1);
                            if(cout % 5 == 0){
                                if(random.nextInt(100) < player.getBoomskill_percent()){
                                    sendPacket(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(2).getSkillname()+" ???(???) ???????????? ????????? ????????????."));
                                    player.setBoomskill_percent(0); //?????????
                                    boomPlayer();
                                }else{
                                    player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(2).getSkillname()+" ???(???) ?????? ??? ????????? #p[5%]#l ?????????????????????."
                                            +"\n?????? ?????? : #p["+player.getBoomskill_percent()+"%]#l"));
                                }
                            }
                        }
                    }
                }
                time--;
                if(time < 0){
                    boomPlayer();
                }
            }
        },1000));
    }

    public List<PlayerInfo> getAlivePlayerList(){
        List<PlayerInfo> playerlist = new ArrayList<>();
        for(PlayerInfo player : players){
            if(player.isAlive()){
                playerlist.add(player);
            }
        }
        return playerlist;
    }

    public List<PlayerInfo> getDeadPlayerList(){
        List<PlayerInfo> playerlist = new ArrayList<>();
        for(PlayerInfo player : players){
            if(!player.isAlive()){
                playerlist.add(player);
            }
        }
        return playerlist;
    }

    public List<PlayerInfo> addPlayerInfoList(List<User> users){
        List<PlayerInfo> players = Collections.synchronizedList(new ArrayList<>());
        Random random = new Random();
        for(User user : users){
            List<PlayerSkillInfo> skills = new ArrayList<>();
            skills.add(PlayerSkillInfo.passive_skill_List.get(random.nextInt(PlayerSkillInfo.passive_skill_List.size())));
            players.add(new PlayerInfo(user,skills,1000,true));
        }
        return players;
    }

    public void boomPlayer(){
        getBoom_schedule().cancel(false);
        setBoom_schedule(null);
        isBoomTimerRunning = false;

        if(getAlivePlayerList().size() == 1){

            //????????? ?????? ????????? ????????? ?????? ?????? ??????
            if(getAlivePlayerList().size() == 1){
                getAlivePlayerList().forEach((player -> {
                    sendPacket(BoomSpinPacket.sendShowBoomImg(10));
                    sendPacket(BoomSpinPacket.sendBoomSpinNotice("#y?????? ?????????????????? #l#r[??????]#l#y??? ?????? ?????? ?????? ?????? #l#r[??????]#l#y??? ????????? ????????????.#l"));
                    player.getUser().getRoom().sendNotice("#y?????? ?????????????????? #l#r[??????]#l#y??? ?????? ?????? ?????? ?????? #l#r[??????]#l#y??? ????????? ????????????.#l");
                    player.getUser().getRoom().sendNotice("#y["+player.getUser().getUserName()+"]#l #w?????? #l#p[??????????????? ????????? 1???]#l#w ?????? ?????????????????????!");

                    player.getUser().addUserWin();
                    Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            sendPacket(BoomSpinPacket.sendBoomSpinExit());
                            sendRefreshUserInfo();
                            player.getUser().getRoom().setBoomSpin(null);  //???????????? ??????????????? ????????? ???????????? ???????????? ?????????????
                        }
                    },3000);

                }));
                getDeadPlayerList().forEach(player -> {
                    player.getUser().addUserLose();
                });
            }
            return;
        }

        sendPacket(BoomSpinPacket.sendShowBoomImg(10));
        sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r!!BOOM!!#l"));
        sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r!![??????]??? ???????????????!!#l"));
        sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r!!BOOM!!#l"));

        PlayerInfo boomPlayer = null;

        for(PlayerInfo player : players){
            if(player.isMyturn()){
                boomPlayer = player;
                break;
            }
        }

        if(boomPlayer == null){
            System.out.println("boomPlayer has NULL ERR");
            return;
        }

        handleCalcBoom(boomPlayer, true);

        boomPlayer.setMyturn(false);


        sendPacket(BoomSpinPacket.sendBoomSpinPlayerList(players));

        int cout = getAlivePlayerList().size();

        if(cout == 1){ //????????? 1???
            getAlivePlayerList().forEach((player -> {
                player.getUser().getRoom().sendNotice("#y"+player.getUser().getUserName()+"#l #w?????? #l#p[??????????????? ????????? 1???]#l#w ?????? ?????????????????????!");
                player.getUser().addUserWin();
            }));
            getDeadPlayerList().forEach(player -> {
                player.getUser().addUserLose();
            });

            PlayerInfo finalBoomPlayer = boomPlayer;
            Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sendPacket(BoomSpinPacket.sendBoomSpinExit());
                    sendRefreshUserInfo();
                    finalBoomPlayer.getUser().getRoom().setBoomSpin(null);
                }
            },3000);
        }else if(cout == 0){
            //?????????
            boomPlayer.getUser().getRoom().sendNotice("?????? ???????????? ????????? ???????????? ????????????..");
            PlayerInfo finalBoomPlayer = boomPlayer;
            Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sendPacket(BoomSpinPacket.sendBoomSpinExit());
                    sendRefreshUserInfo();
                    finalBoomPlayer.getUser().getRoom().setBoomSpin(null);
                }
            },3000);
        }else{
            //?????? ????????????
            Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    runGame();
                }
            },3000);

        }
    }


    public void handleCalcBoom(PlayerInfo boomPlayer, boolean isBoom){
        // isBoom > ???????????? ?????????????????? ?????????
        //?????? ??????
        if(boomPlayer.hasSkill(1) && isBoom){//??????
            List<PlayerInfo> aliveplayers = getAlivePlayerList();
            PlayerInfo boomTogether = aliveplayers.get(random.nextInt(aliveplayers.size()));
            System.out.println(boomTogether.getUser().getUserName());
            sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+boomPlayer.getUser().getUserName()+"] ?????? "+boomPlayer.getSkill(1).getSkillname()+" ?????? "
                    +boomTogether.getUser().getUserName()+"?????? ???????????????."));
            handleCalcBoom(boomTogether, false);
        }

        if(!handleAliveSkill(boomPlayer,isBoom)){
            if(isBoom){
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r[??????]??? ?????? ["+boomPlayer.getUser().getUserName()+"] ?????? ?????????????????????.#l"));
            }else{
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r[??????]??? ?????? ["+boomPlayer.getUser().getUserName()+"] ?????? ?????????????????????.#l"));
            }
            boomPlayer.setAlive(false);
        }
    }

    public boolean handleAliveSkill(PlayerInfo player, boolean isBoom){
        // isBoom > ???????????? ????????????????????? ?????????     ,  ??? if????????? ?????? ?????? ?????? ???????????? ?????????????????? 2????????? ????????? ????????? ?????? ????????? ????????? ??????????..
        boolean isAlive = true;

        if(player.hasSkill(4) && !isBoom) { //?????? ??????
            if(random.nextInt(100) < 24){
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName() + "] ?????? " + player.getSkill(4).getSkillname() + " ?????? ?????????????????????!!"));
                isAlive = true;
            }
        }else if(player.hasSkill(5) && isBoom) { //??????
            sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName() + "] ?????? " + player.getSkill(5).getSkillname() + " ?????? ?????????????????????!!"));
            isAlive = true;
        }else if(player.hasSkill(10)){ //????????????
            sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName()+"] ?????? "+player.getSkill(10).getSkillname()+" ?????? ?????????????????????!!"));
            isAlive = true;
        }else if(player.hasSkill(11) && isBoom) { //????????????
            if(random.nextInt(100) < 49){
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName()+"] ?????? "+player.getSkill(11).getSkillname()+" ?????? ?????????????????????!!"));
                isAlive = true;
                List<PlayerInfo> aPlayer = getAlivePlayerList();
                Collections.shuffle(aPlayer);

                for(PlayerInfo aliveplayer : aPlayer){
                    PlayerInfo boomTogether = aliveplayer;
                    if(boomTogether != player){
                        sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName()+"] ?????? "+player.getSkill(1).getSkillname()+" ?????? "
                                +"["+boomTogether.getUser().getUserName()+"] ?????? ????????? ????????????."));
                        handleCalcBoom(boomTogether, false);
                        break;
                    }
                }
            }
        }
        return isAlive;
    }

    public boolean handleLockSkill(PlayerInfo player){
        boolean isLock = false;
        player.setLockSpinSec(0); //???????????????

        if(player.hasSkill(0)){ //??????
            player.setLockSpinSec(player.getLockSpinSec()+5);
            isLock = true;
        }
        if(player.hasSkill(100)){ //????????????
            player.setLockSpinSec(player.getLockSpinSec()+5);
            isLock = true;
        }
        return isLock;
    }



    public void sendGameInfo(){
        setSchedule2(Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                sendDay(day);
                sendNotice("#p["+day+"?????? ???]#l#w??? ?????? ????????? ??????????????? #l#y[??????]#l#w???#l #y[?????? ??????]#l#w??? ???????????????.#l");
                if(players.size() == 0){
                    players = addPlayerInfoList(userList);
                }else{
                    resetRandomPlayerInfoList();
                }
                sendPacket(BoomSpinPacket.sendBoomSpinPlayerList(players));
                sendMoney();
            }
        },3000));
    }

    public void resetRandomPlayerInfoList(){
        Random random = new Random();
        int money = 1000;
        sendNotice("[????????? ????????? ???????????? ??????????????? ?????? ???????????????]");
        isRandomSpin = false;
        for(PlayerInfo player : players){
            if(player.isAlive()){
                player.setSkilllist(new ArrayList<>());
                List<PlayerSkillInfo> skills = new ArrayList<>();
                skills.add(PlayerSkillInfo.passive_skill_List.get(random.nextInt(PlayerSkillInfo.passive_skill_List.size())));
                player.setSkilllist(skills);
                player.setMoney(player.getMoney() + money);
            }else{
                player.setSkilllist(new ArrayList<>());
                List<PlayerSkillInfo> skills = new ArrayList<>();
                skills.add(PlayerSkillInfo.dead_player_skill_List.get(0));
                player.setSkilllist(skills);
                player.setMoney(0);
            }
        }
    }

    public static void handleBoomSpin(User user){
        BoomSpin boomSpin = user.getRoom().getBoomSpin();
        if(boomSpin == null){
            return;
        }

        PlayerInfo player = boomSpin.getPlayer(user);
        if(player == null){
            return;
        }
        if(!player.isMyturn()){
            return;
        }

        if(player.isLock()){
            player.getUser().getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("????????? ?????? ?????? ????????? ?????? ??? ????????????."));
            return;
        }
        if(player.hasSkill(2)){ //?????????
            player.setBoomskill_percent(0); //?????????

        }

        if(boomSpin.getTime() > 0){
            if(player.hasSkill(4)){ //?????????
                boomSpin.addTime(-3);
                player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(4).getSkillname()+" ?????? ?????? ????????? #p[3???]#l ??????????????????."));
            }
            if(player.hasSkill(14)){ //????????????
                int timeadd = 1+random.nextInt(9);
                boomSpin.addTime(-timeadd);
                boomSpin.sendPacket(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(14).getSkillname()+" ?????? ?????? ????????? #p["+timeadd+"???]#l ?????????????????????."));
            }
        }
        player.setMyturn(false);

        boolean isNext = false;
        boolean isSucceed = false;
        PlayerInfo nextPlayer_ = null;

        if(boomSpin.isRandomSpin){
            List<PlayerInfo> alives = boomSpin.getAlivePlayerList();
            List<PlayerInfo> deads = boomSpin.getDeadPlayerList();

            Collections.shuffle(alives);

            boomSpin.players.clear();
            boomSpin.players.addAll(alives);
            boomSpin.players.addAll(deads);
        }

        for(PlayerInfo nextPlayer : boomSpin.players){
            if(isNext && nextPlayer.isAlive()){
                nextPlayer.setMyturn(true);
                nextPlayer_ = nextPlayer;
                isSucceed = true;
                break;
            }
            if(nextPlayer == player){
                isNext = true;
            }
        }

        if(!isSucceed){ //????????? ???????????? ?????? ?????? ???????????? ??????.
            for(PlayerInfo nextPlayer : boomSpin.players){
                if(nextPlayer.isAlive() && nextPlayer != player){
                    nextPlayer.setMyturn(true);
                    nextPlayer_ = nextPlayer;
                    break;
                }
            }
        }

        if(nextPlayer_ != null){
            if(nextPlayer_.getLockSpinSec() > 0){
                boomSpin.setLockSpin(nextPlayer_,0);
            }
        }

        boomSpin.sendPacket(BoomSpinPacket.sendBoomSpinPlayerList(boomSpin.players));
        //boomSpin.sendPacket(BoomSpinPacket.sendBoomSpinNotice(user.getUserName()+" ?????? ????????? ???????????????!"));
    }

    public void calcPlayersetHighPricePlayer(PlayerInfo playerInfo,int price){
        if(price < 0){
            return;
        }
        if(playerInfo.getMoney() >= price){
            if(getBoomSpinAuction().getLow_bid() > price){
                playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#r?????? ??????????????? ?????? ????????? ?????????????????????.#l"));
            }else{
                getBoomSpinAuction().setHighPricePlayer(playerInfo);
                getBoomSpinAuction().setHigh_price(price);
                getBoomSpinAuction().setLow_bid(price + (int) (price*0.1));
                sendPacket(BoomSpinPacket.sendAuctionInfo(getBoomSpinAuction(),"#w????????? ??????????????? ????????? #l#g??????#l#w???????????????#l",playerInfo.getMoney()));
                playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#g??????????????? ?????????????????????#l"));
                resetAuctionTimer();
            }
        }else{
            playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#r???????????? ???????????????#l"));
        }

    }



    public static void handleAutionBid(User user, int price){

        BoomSpin boomSpin = user.getRoom().getBoomSpin();
        if(boomSpin == null){
            return;
        }

        PlayerInfo player = boomSpin.getPlayer(user);
        if(player == null){
            return;
        }

        if(!boomSpin.isAuctionNow){
            user.getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#r????????? ???????????? ?????? ????????????#l"));
            return;
        }


        PlayerInfo highPricePlayer = boomSpin.getBoomSpinAuction().getHighPricePlayer();

        if(highPricePlayer != null){
            if(highPricePlayer != player){
                boomSpin.calcPlayersetHighPricePlayer(player,price);
            }else{
                user.getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("?????? ????????? ??????????????????"));
            }
        }else{
            boomSpin.calcPlayersetHighPricePlayer(player,price);
        }

    }


    public void prepareAuction(){
        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                sendPacket(BoomSpinPacket.sendShowBoomImg(8));
                sendNotice("#p["+day+"?????? ???]#l#w ?????? ????????? ?????????????????????.#l");
                sendNotice("????????? ????????? ???????????? ???????????? ????????? ??? ????????????.");
                sendNotice("?????? ????????? ?????? ????????? ???????????? ???????????? ????????? ???????????????.");
            }
        },7000);
        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                List<PlayerSkillInfo> auction_skillInfos = PlayerSkillInfo.auction_skill_List;

                int lowPrice = 100;
                int highPrice = 0;


                boomSpinAuction = new BoomSpinAuction(
                        auction_skillInfos.get(random.nextInt(auction_skillInfos.size())),
                        lowPrice,
                        highPrice,
                        null
                );

                sendAuctionOpen(boomSpinAuction);
                sendNotice("#p["+day+"?????? ???]#l#w ?????? ????????? ?????????????????????.#l");
            }
        },12000);
    }

    public void sendAuctionOpen(BoomSpinAuction boomSpinAuction){
        for(PlayerInfo playerInfo : players){
            if(playerInfo.isAlive()){
                playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendOpenActuion(boomSpinAuction,playerInfo.getMoney()));
            }
        }
        sendPacket(BoomSpinPacket.sendAuctionNotice("#p["+day+"?????? ???]#l??? ????????? ??? ?????????????????????."));
        setAuction_start_schedule(Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                startAuctionTimer();
            }
        },2000));
    }

    public void startAuctionTimer(){
        sendPacket(BoomSpinPacket.sendAuctionNotice("????????? ?????? ??? ?????????.."));
        auctionTimeSec = 15;
        setAuction_schedule(Timer.BoomSpinTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if(auctionTimeSec == 15){
                    isAuctionNow = true;
                    sendPacket(BoomSpinPacket.sendAuctionNotice("????????? ?????????????????????."));
                }
                sendPacket(BoomSpinPacket.sendAuctionTime(auctionTimeSec));
                auctionTimeSec--;
                if(auctionTimeSec < 0){
                    sendPacket(BoomSpinPacket.sendAuctionNotice("????????? ?????????????????????."));
                    isAuctionNow = false;
                    setAuctionEnd();
                    getAuction_schedule().cancel(false);
                    setAuction_schedule(null);
                }
            }
        },1000,3000));
    }

    public void resetAuctionTimer(){
        getAuction_schedule().cancel(false);
        setAuction_schedule(null);
        auctionTimeSec = 15;
        isAuctionNow = false;
        setAuction_schedule(Timer.BoomSpinTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                isAuctionNow = true;
                sendPacket(BoomSpinPacket.sendAuctionTime(auctionTimeSec));
                auctionTimeSec--;
                if(auctionTimeSec < 0){
                    sendPacket(BoomSpinPacket.sendAuctionNotice("#p["+day+"?????? ???]#l ?????? ????????? ?????????????????????.\n????????? ?????????????????? ???????????????."));
                    getAuction_schedule().cancel(false);
                    setAuction_schedule(null);
                    isAuctionNow = false;
                    setAuctionEnd();
                }
            }
        },1000));
    }

    public void setAuctionEnd(){
        PlayerInfo player = getBoomSpinAuction().getHighPricePlayer();
        if(player != null){
            player.setMoney(player.getMoney() - getBoomSpinAuction().getHigh_price());
            player.getSkilllist().add(getBoomSpinAuction().getSkillInfo());
            player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice("#y["+getBoomSpinAuction().getHigh_price()+" ??????]#l??? ???????????? "
                    +getBoomSpinAuction().getSkillInfo().getSkillname()+" ??? ?????????????????????.\n?????? ????????? ?????? ???????????? ???????????? #g[?????????]#l#w ?????????.#l"));

            sendMoney();

        }

        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                sendNotice("#p["+day+"?????? ???]#l ?????? ????????? ?????????????????????.");
                sendPacket(BoomSpinPacket.sendAuctionExit());
            }
        },3000);


        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if(player != null){
                    if(player.hasSkill(13)){//????????????
                        for(PlayerInfo oPlayer : players){
                            if(player != oPlayer && oPlayer.isAlive()){
                                oPlayer.getSkilllist().add(PlayerSkillInfo.auction_debuff_skill_List.get(0));
                            }
                        }
                    }

                    if(player.hasSkill(14)){//????????????
                        for(PlayerInfo oPlayer : players){
                            if(player != oPlayer && oPlayer.isAlive()){
                                oPlayer.getSkilllist().add(PlayerSkillInfo.auction_debuff_skill_List.get(1));
                            }
                        }
                    }
                    if(player.hasSkill(15)){//?????????
                        isRandomSpin = true;
                        sendNotice(player.getSkill(15).getSkillname()+" ??? ????????? ????????? #p[??????]#l?????? ???????????????.");
                        for(PlayerInfo oPlayer : players){
                            if(player != oPlayer && oPlayer.isAlive()){
                                oPlayer.getSkilllist().add(PlayerSkillInfo.auction_debuff_skill_List.get(2));
                            }
                        }
                    }

                }

                for(PlayerInfo oPlayer : players){
                    handleLockSkill(oPlayer);
                }

                sendNotice("#p["+day+"?????? ???]#l ?????? ????????? ????????? #p[??????]#l?????? ???????????????.");
                if(!isRandomSpin){
                    sendNotice("#b[??????]#l#w??? ???????????? ?????????#l #g[???????????? ?????????]#l ???????????? ????????? ?????????.");
                }
                setBoomOrder();
            }
        },4000);

    }

    public void setLockSpin(PlayerInfo player, int delay){
        player.setLock(true);
        player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice("?????? ????????? ?????? ????????? #p["+player.getLockSpinSec()+"???]#l ??? ?????????????????????."));


        delay = delay + (int)(player.getLockSpinSec()*1000);
        System.out.println(delay);

        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice("?????? ????????? ?????? ????????? ?????? ???????????????."));
                player.setLock(false);
            }
        },delay);
    }

    public void setBoomOrder(){

        List<PlayerInfo> alives = getAlivePlayerList();
        List<PlayerInfo> deads = getDeadPlayerList();

        Collections.shuffle(alives);

        players.clear();
        players.addAll(alives);
        players.addAll(deads);

        if(players.get(0).getLockSpinSec() > 0){
            setLockSpin(players.get(0),2000);
        }

        players.get(0).setMyturn(true);

        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                startBoomTimer();
                sendPacket(BoomSpinPacket.sendBoomSpinPlayerList(players));
                sendNotice("#p["+day+"?????? ???]#l ?????? ???????????? ?????????????????????.");
                sendNotice("????????? ??????????????? #y[1??? ~ 2???]#l ????????? ????????? ???????????? ????????? ??????????????????.");
                sendNotice("????????? ?????? ?????? ????????? ??????????????? ????????? ??? ????????????.");
            }
        },2000);
    }
}
