package Game.BoomSpin;

public class BoomSpinAuction {

    private PlayerSkillInfo skillInfo = null;
    private PlayerInfo highPricePlayer = null;
    private int low_bid, high_price;

    public BoomSpinAuction(PlayerSkillInfo skillInfo, int low_bid, int high_price,PlayerInfo highPricePlayer){
        this.skillInfo = skillInfo;
        this.low_bid = low_bid;
        this.high_price = high_price;
        this.highPricePlayer = highPricePlayer;
    }

    public int getHigh_price() {
        return high_price;
    }

    public int getLow_bid() {
        return low_bid;
    }

    public PlayerInfo getHighPricePlayer() {
        return highPricePlayer;
    }

    public PlayerSkillInfo getSkillInfo() {
        return skillInfo;
    }

    public void setSkillInfo(PlayerSkillInfo skillInfo) {
        this.skillInfo = skillInfo;
    }

    public void setHigh_price(int high_price) {
        this.high_price = high_price;
    }

    public void setHighPricePlayer(PlayerInfo highPricePlayer) {
        this.highPricePlayer = highPricePlayer;
    }

    public void setLow_bid(int low_bid) {
        this.low_bid = low_bid;
    }

}
