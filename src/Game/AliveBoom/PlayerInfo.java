package Game.AliveBoom;

import Data.User;

public class PlayerInfo {

    private User user;
    private int round = 0;
    private boolean isAlive = true;

    public PlayerInfo(User user, int round, boolean isAlive){
        this.user = user;
        this.round = round;
        this.isAlive = isAlive;
    }

}
