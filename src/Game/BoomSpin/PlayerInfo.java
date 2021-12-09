package Game.BoomSpin;

import Data.User;
import DevTool.LittleEndianWriter;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {

    private User user;
    private List<PlayerSkillInfo> skilllist = new ArrayList<>();
    private int money = 0;
    private boolean isMyturn = false;
    private boolean isAlive = false;
    private boolean isLock = false;
    private int lockSpinSec = 0;
    private int boomskill_percent = 1;

    public PlayerInfo(User user, List<PlayerSkillInfo> skilllist, int money, boolean isAlive){
        this.user = user;
        this.skilllist = skilllist;
        this.money = money;
        this.isAlive = isAlive;
    }

    public int getBoomskill_percent() {
        return boomskill_percent;
    }

    public void setBoomskill_percent(int boomskill_percent) {
        this.boomskill_percent = boomskill_percent;
    }

    public void addBoomskill_percent(int add) {
        this.boomskill_percent += add;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isMyturn() {
        return isMyturn;
    }

    public void setMyturn(boolean myturn) {
        isMyturn = myturn;
    }

    public User getUser() {
        return user;
    }

    public void setSkilllist(List<PlayerSkillInfo> skilllist) {
        this.skilllist = skilllist;
    }

    public List<PlayerSkillInfo> getSkilllist() {
        return skilllist;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getLockSpinSec() {
        return lockSpinSec;
    }

    public void setLockSpinSec(int lockSpinSec) {
        this.lockSpinSec = lockSpinSec;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    public boolean hasSkill(int skillid){
        for(PlayerSkillInfo skillInfo : skilllist){
            if(skillid == skillInfo.getSkillcode()){
                return true;
            }
        }
        return false;
    }

    public PlayerSkillInfo getSkill(int skillid){
        for(PlayerSkillInfo skillInfo : skilllist){
            if(skillid == skillInfo.getSkillcode()){
                return skillInfo;
            }
        }
        return null;
    }

    public byte[] sendPlayerInfo(LittleEndianWriter o){
        getUser().sendUserInfo(o);
        o.writeInt(skilllist.size());
        for(PlayerSkillInfo playerSkillInfo : skilllist){
            playerSkillInfo.sendSkillInfo(o);
        }
        o.writeInt(getMoney());
        o.write(isAlive());
        o.write(isMyturn());
        return o.getPacket();
    }
}
