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

            if(!isSucceed){ //순서가 돌고돌아 다시 첫턴 플레이어 일때.
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
            sendNotice("#r["+user.getUserName()+"] 님이 [탈주]하여 다음 플레이어에게 폭탄이 넘어갑니다.#l");
        }
        sendNotice("#r["+user.getUserName()+"] 님이 [탈주]하여 [사망]처리됩니다.#l");

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
            case 6: { //채팅
                long userid = r.readLong();
                String userName = r.readLengthAsciiString();
                String userMsg = r.readLengthAsciiString();
                int userProfileimgCode = r.readInt();
                user.getRoom().getBoomSpin().sendPacket(BoomSpinPacket.sendBoomSpinChat(user,userMsg,userProfileimgCode));
                break;
            }
            case 7: { //경매 입찰
                int price = r.readInt();
                handleAutionBid(user,price);
                break;
            }
            case 8:{ //폭탄 돌리기
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
                sendNotice("[폭탄 돌리기 게임에 오신 것을 환영합니다]");
                sendNotice("#y[게임이 시작됩니다]#l");
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
                    sendPacket(BoomSpinPacket.sendBoomSpinNotice("#b["+cout+"초]#l#w 경과 하였습니다."));
                }

                /*for(int i = 0; i < 100; i++){ //순간적으로 많은양의 데이터를 날려 어플리케이션 중지여부 테스트용
                    sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r가#l#o나#l#y다#l#g라#l#b마#l#p라#l"));
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
                        if(player.hasSkill(2)){ //오작동
                            player.addBoomskill_percent(1);
                            if(cout % 5 == 0){
                                if(random.nextInt(100) < player.getBoomskill_percent()){
                                    sendPacket(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(2).getSkillname()+" 이(가) 발동되어 폭탄이 터집니다."));
                                    player.setBoomskill_percent(0); //초기화
                                    boomPlayer();
                                }else{
                                    player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(2).getSkillname()+" 이(가) 발동 될 확률이 #p[5%]#l 증가하였습니다."
                                            +"\n현재 확률 : #p["+player.getBoomskill_percent()+"%]#l"));
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
           /* for(PlayerInfo player : players){
                if(player.isAlive()){
                    final PlayerInfo alivePlayer = player;
                    sendPacket(BoomSpinPacket.sendShowBoomImg(10));
                    sendPacket(BoomSpinPacket.sendBoomSpinNotice("#y다른 플레이어들의 #l#r[탈주]#l#y로 인해 혼자 남은 경우 #l#r[폭탄]#l#y은 터지지 않습니다.#l"));

                    alivePlayer.getUser().addUserWin();
                    player.getUser().getRoom().sendNotice("#y다른 플레이어들의 #l#r[탈주]#l#y로 인해 혼자 남은 경우 #l#r[폭탄]#l#y은 터지지 않습니다.#l");
                    player.getUser().getRoom().sendNotice("#y["+alivePlayer.getUser().getUserName()+"]#l #w님이 #l#p[폭탄돌리기 최후의 1인]#l#w 으로 승리하셨습니다!");
                    Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            sendPacket(BoomSpinPacket.sendBoomSpinExit());
                            alivePlayer.getUser().getRoom().setBoomSpin(null);
                        }
                    },3000);
                    break;
                }
            }*/

            //탈주로 인한 폭탄이 터지지 않는 승부 결정
            if(getAlivePlayerList().size() == 1){
                getAlivePlayerList().forEach((player -> {
                    sendPacket(BoomSpinPacket.sendShowBoomImg(10));
                    sendPacket(BoomSpinPacket.sendBoomSpinNotice("#y다른 플레이어들의 #l#r[탈주]#l#y로 인해 혼자 남은 경우 #l#r[폭탄]#l#y은 터지지 않습니다.#l"));
                    player.getUser().getRoom().sendNotice("#y다른 플레이어들의 #l#r[탈주]#l#y로 인해 혼자 남은 경우 #l#r[폭탄]#l#y은 터지지 않습니다.#l");
                    player.getUser().getRoom().sendNotice("#y["+player.getUser().getUserName()+"]#l #w님이 #l#p[폭탄돌리기 최후의 1인]#l#w 으로 승리하셨습니다!");

                    player.getUser().addUserWin();
                    Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            sendPacket(BoomSpinPacket.sendBoomSpinExit());
                            sendRefreshUserInfo();
                            player.getUser().getRoom().setBoomSpin(null);  //채팅방의 폭탄돌리기 객체를 가져와서 눌하는게 나을지도?
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
        sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r!![폭탄]이 터졌습니다!!#l"));
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

       /* PlayerInfo alivePlayer = null;
        for(PlayerInfo player : players){
            if(player.isAlive()){
                alivePlayer = player;
                cout++;
            }
        }*/


        if(cout == 1){ //최후의 1인
            getAlivePlayerList().forEach((player -> {
                player.getUser().getRoom().sendNotice("#y"+player.getUser().getUserName()+"#l #w님이 #l#p[폭탄돌리기 최후의 1인]#l#w 으로 승리하셨습니다!");
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
            //무승부
            boomPlayer.getUser().getRoom().sendNotice("모두 죽어 무효 게임처리 되었습니다.");
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
            //다시 날바꾸기
            Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    runGame();
                }
            },3000);

        }
    }


    public void handleCalcBoom(PlayerInfo boomPlayer, boolean isBoom){
        // isBoom > 폭사인지 자폭스킬인지 구분용
        //능력 계산
        if(boomPlayer.hasSkill(1) && isBoom){//자폭
            List<PlayerInfo> aliveplayers = getAlivePlayerList();
            PlayerInfo boomTogether = aliveplayers.get(random.nextInt(aliveplayers.size()));
            System.out.println(boomTogether.getUser().getUserName());
            sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+boomPlayer.getUser().getUserName()+"] 님의 "+boomPlayer.getSkill(1).getSkillname()+" 으로 "
                    +boomTogether.getUser().getUserName()+"님을 데려갑니다."));
            handleCalcBoom(boomTogether, false);
        }

        if(!handleAliveSkill(boomPlayer,isBoom)){
            if(isBoom){
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r[폭탄]이 터져 ["+boomPlayer.getUser().getUserName()+"] 님이 사망하였습니다.#l"));
            }else{
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("#r[능력]에 의해 ["+boomPlayer.getUser().getUserName()+"] 님이 사망하였습니다.#l"));
            }
            boomPlayer.setAlive(false);
        }
    }

    public boolean handleAliveSkill(PlayerInfo player, boolean isBoom){
        // isBoom > 폭사인지 동반자스킬인지 구분용
        boolean isAlive = false;

        if(player.hasSkill(4) && !isBoom) { //방탄 헬멧
            if(random.nextInt(100) < 24){
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName() + "] 님이 " + player.getSkill(4).getSkillname() + " 으로 생존하였습니다!!"));
                isAlive = true;
            }
        }else if(player.hasSkill(5) && isBoom) { //방패
            sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName() + "] 님이 " + player.getSkill(5).getSkillname() + " 으로 생존하였습니다!!"));
            isAlive = true;
        }else if(player.hasSkill(10)){ //방탄조끼
            sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName()+"] 님이 "+player.getSkill(10).getSkillname()+" 으로 생존하였습니다!!"));
            isAlive = true;
        }else if(player.hasSkill(11) && isBoom) { //럭키가이
            if(random.nextInt(100) < 24){
                sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName()+"] 님이 "+player.getSkill(11).getSkillname()+" 으로 생존하였습니다!!"));
                isAlive = true;
                List<PlayerInfo> aPlayer = getAlivePlayerList();
                Collections.shuffle(aPlayer);

                for(PlayerInfo aliveplayer : aPlayer){
                    PlayerInfo boomTogether = aliveplayer;
                    if(boomTogether != player){
                        sendPacket(BoomSpinPacket.sendBoomSpinNotice("["+player.getUser().getUserName()+"] 님의 "+player.getSkill(1).getSkillname()+" 으로 "
                                +"["+boomTogether.getUser().getUserName()+"] 님이 피해를 받습니다."));
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
        player.setLockSpinSec(0); //초기화먼저

        if(player.hasSkill(0)){ //봉인
            player.setLockSpinSec(player.getLockSpinSec()+5);
            isLock = true;
        }
        if(player.hasSkill(100)){ //시간왜곡
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
                sendNotice("#p["+day+"번째 날]#l#w이 되어 생존자 여러분들께 #l#y[코인]#l#w과#l #y[랜덤 능력]#l#w이 부여됩니다.#l");
                if(players.size() == 0){
                    players = addPlayerInfoList(userList);
                }else{
                    resetRandomPlayerInfoList();
                }
                sendPacket(BoomSpinPacket.sendBoomSpinPlayerList(players));
                sendMoney();
            }
        },2000));
    }

    public void resetRandomPlayerInfoList(){
        Random random = new Random();
        int money = 1000;
        sendNotice("[전날에 지급된 능력들이 초기화되고 새로 지급됩니다]");
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
            player.getUser().getSesson().writeAndFlush(FriendPacket.sendFriendNoticeMsg("능력에 의해 지금 폭탄을 돌릴 수 없습니다."));
            return;
        }
        if(player.hasSkill(2)){ //오작동
            player.setBoomskill_percent(1); //초기화

        }

        if(boomSpin.getTime() > 0){
            if(player.hasSkill(4)){ //불장난
                boomSpin.addTime(-3);
                player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(4).getSkillname()+" 으로 폭파 시간을 #p[3초]#l 앞당겼습니다."));
            }
            if(player.hasSkill(14)){ //바이러스
                int timeadd = 1+random.nextInt(9);
                boomSpin.addTime(-timeadd);
                boomSpin.sendPacket(BoomSpinPacket.sendBoomSpinNotice(player.getSkill(14).getSkillname()+" 으로 폭파 시간이 #p["+timeadd+"초]#l 앞당겨졌습니다."));
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

        if(!isSucceed){ //순서가 돌고돌아 다시 첫턴 플레이어 일때.
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
        boomSpin.sendPacket(BoomSpinPacket.sendBoomSpinNotice(user.getUserName()+" 님이 폭탄을 돌렸습니다!"));
    }

    public void calcPlayersetHighPricePlayer(PlayerInfo playerInfo,int price){
        if(price < 0){
            return;
        }
        if(playerInfo.getMoney() >= price){
            if(getBoomSpinAuction().getLow_bid() > price){
                playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#r최소 입찰가보다 적은 금액을 입력하셨습니다.#l"));
            }else{
                getBoomSpinAuction().setHighPricePlayer(playerInfo);
                getBoomSpinAuction().setHigh_price(price);
                getBoomSpinAuction().setLow_bid(price + (int) (price*0.1));
                sendPacket(BoomSpinPacket.sendAuctionInfo(getBoomSpinAuction(),"#w익명의 플레이어가 입찰에 #l#g성공#l#w하였습니다#l",playerInfo.getMoney()));
                playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#g정상적으로 입찰되었습니다#l"));
                resetAuctionTimer();
            }
        }else{
            playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#r소지금이 부족합니다#l"));
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
            user.getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("#r경매가 진행되고 있지 않습니다#l"));
            return;
        }


        PlayerInfo highPricePlayer = boomSpin.getBoomSpinAuction().getHighPricePlayer();

        if(highPricePlayer != null){
            if(highPricePlayer != player){
                boomSpin.calcPlayersetHighPricePlayer(player,price);
            }else{
                user.getSesson().writeAndFlush(BoomSpinPacket.sendAuctionNotice("이미 최고가 입찰자입니다"));
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
                sendNotice("#p["+day+"번째 날]#l#w 능력 경매가 시작되겠습니다.#l");
                sendNotice("능력은 날마다 지급되는 코인으로 구매할 수 있습니다.");
                sendNotice("경매 중에는 다른 기능이 제한되며 익명으로 경매가 진행됩니다.");
            }
        },5000);
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
                sendNotice("#p["+day+"번째 날]#l#w 능력 경매가 시작되었습니다.#l");
            }
        },10000);
    }

    public void sendAuctionOpen(BoomSpinAuction boomSpinAuction){
        for(PlayerInfo playerInfo : players){
            if(playerInfo.isAlive()){
                playerInfo.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendOpenActuion(boomSpinAuction,playerInfo.getMoney()));
            }
        }
        sendPacket(BoomSpinPacket.sendAuctionNotice("#p["+day+"번째 날]#l의 경매가 곧 시작되겠습니다."));
        setAuction_start_schedule(Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                startAuctionTimer();
            }
        },2000));
    }

    public void startAuctionTimer(){
        sendPacket(BoomSpinPacket.sendAuctionNotice("경매가 준비 중 입니다.."));
        auctionTimeSec = 15;
        setAuction_schedule(Timer.BoomSpinTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if(auctionTimeSec == 15){
                    isAuctionNow = true;
                    sendPacket(BoomSpinPacket.sendAuctionNotice("경매가 시작되었습니다."));
                }
                sendPacket(BoomSpinPacket.sendAuctionTime(auctionTimeSec));
                auctionTimeSec--;
                if(auctionTimeSec < 0){
                    sendPacket(BoomSpinPacket.sendAuctionNotice("경매가 종료되었습니다."));
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
                    sendPacket(BoomSpinPacket.sendAuctionNotice("#p["+day+"번째 날]#l 능력 경매가 종료되었습니다.\n잠시후 게임화면으로 전환됩니다."));
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
            player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice("#y["+getBoomSpinAuction().getHigh_price()+" 코인]#l을 지불하고 "
                    +getBoomSpinAuction().getSkillInfo().getSkillname()+" 을 구입하였습니다.\n해당 능력은 폭탄 돌리기가 시작되면 #g[활성화]#l#w 됩니다.#l"));

            sendMoney();

        }

        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                sendNotice("#p["+day+"번째 날]#l 능력 경매가 종료되었습니다.");
                sendPacket(BoomSpinPacket.sendAuctionExit());
            }
        },3000);


        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if(player != null){
                    if(player.hasSkill(13)){//시간왜곡
                        for(PlayerInfo oPlayer : players){
                            if(player != oPlayer && oPlayer.isAlive()){
                                oPlayer.getSkilllist().add(PlayerSkillInfo.auction_debuff_skill_List.get(0));
                            }
                        }
                    }

                    if(player.hasSkill(14)){//바이러스
                        for(PlayerInfo oPlayer : players){
                            if(player != oPlayer && oPlayer.isAlive()){
                                oPlayer.getSkilllist().add(PlayerSkillInfo.auction_debuff_skill_List.get(1));
                            }
                        }
                    }
                    if(player.hasSkill(15)){//새치기
                        isRandomSpin = true;
                        sendNotice(player.getSkill(15).getSkillname()+" 에 의하여 순서가 #p[랜덤]#l으로 돌아갑니다.");
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

                sendNotice("#p["+day+"번째 날]#l 폭탄 돌리기 순서가 #p[랜덤]#l으로 정해집니다.");
                if(!isRandomSpin){
                    sendNotice("#b[순서]#l#w는 플레이어 목록의#l #g[왼쪽에서 오른쪽]#l 방향으로 되풀이 됩니다.");
                }
                setBoomOrder();
            }
        },4000);

    }

    public void setLockSpin(PlayerInfo player, int delay){
        player.setLock(true);
        player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice("폭탄 돌리기 잠금 능력이 #p["+player.getLockSpinSec()+"초]#l 간 적용되었습니다."));


        delay = delay + (int)(player.getLockSpinSec()*1000);
        System.out.println(delay);

        Timer.BoomSpinTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                player.getUser().getSesson().writeAndFlush(BoomSpinPacket.sendBoomSpinNotice("폭탄 돌리기 잠금 능력이 헤제 되었습니다."));
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
                sendNotice("#p["+day+"번째 날]#l 폭탄 돌리기가 시작되었습니다.");
                sendNotice("폭탄은 기본적으로 #y[1분 ~ 2분]#l 사이의 랜덤한 시간에서 터지게 되어있습니다.");
                sendNotice("능력에 의해 폭파 시간은 단축되거나 길어질 수 있습니다.");
            }
        },2000);
    }
}
