package Game.BoomSpin;

import DevTool.LittleEndianWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerSkillInfo {
    public static List<PlayerSkillInfo> help_comment_list = new ArrayList<>();
    public static List<PlayerSkillInfo> dead_player_skill_List = new ArrayList<>();
    public static List<PlayerSkillInfo> passive_skill_List = new ArrayList<>();
    public static List<PlayerSkillInfo> auction_skill_List = new ArrayList<>();
    public static List<PlayerSkillInfo> auction_debuff_skill_List = new ArrayList<>();
    private int skillcode;
    private String skillname, skillcomment;

    static{
        help_comment_list.add(new PlayerSkillInfo(-1, "[폭탄돌리기]", "#p[최후의 1인]#l#w이 될때까지 살아남아라!#l"));
        help_comment_list.add(new PlayerSkillInfo(-1, "[기본]", "#w날마다 폭탄이 1개씩 돌아가며#l #p[최후의 생존자]#l#w가 나올때까지 진행된다.#l"));
        help_comment_list.add(new PlayerSkillInfo(-1, "[코인]", "#w날이 바뀔때 마다 #l#p[1000 코인]#l#w씩 지급되며 경매에서 쓸 수 있다.#l"));
        help_comment_list.add(new PlayerSkillInfo(-1, "[능력]", "#w날이 바뀔때 마다 기본 능력이 초기화되며 기본 능력 목록 중 1개의 능력이 선정된다.#l"));
        help_comment_list.add(new PlayerSkillInfo(-1, "[경매]", "#w날이 바뀔때 마다 시작되며 경매 능력 목록 중 1개의 능력이 판매된다.#l"));
        help_comment_list.add(new PlayerSkillInfo(-1, "[채팅]", "#w오른쪽 상단의 말풍선 버튼을 이용하여 플레이어들과 채팅을 할 수 있다.#l"));

        dead_player_skill_List.add(new PlayerSkillInfo(-1, "#r[사망]#l", "여긴 어디일까 할 수 있는 것은 없어 보인다. 상황을 지켜보자."));

        passive_skill_List.add(new PlayerSkillInfo(0, "#r[봉인]#l", "#w턴마다 #l#p[5초]#l#w간 폭탄을 돌릴 수 없다.#l"));
        passive_skill_List.add(new PlayerSkillInfo(1, "#o[자폭]#l", "#w폭탄이 터지면 플레이어 중 한명을 데려간다.#l"));
        passive_skill_List.add(new PlayerSkillInfo(2, "#r[오작동]#l", "#w폭탄을 돌리지 않으면 #l#p[5초]#l#w마다#l #p[1 * 경과된 초%]#l #w확률로 폭탄이 터진다.#l"));
        passive_skill_List.add(new PlayerSkillInfo(3, "#g[방탄헬멧]#l", "#p[25%]#l #w확률로 #l#o[자폭] [럭키가이]#l#w 으로부터 생존 할 수 있다.#l"));
        passive_skill_List.add(new PlayerSkillInfo(4, "#o[불장난]#l", "#w폭탄을 돌릴때 마다 폭파 시간을 #l#p[3초]#l#w씩 앞당긴다.#l"));
        passive_skill_List.add(new PlayerSkillInfo(5, "#g[방패]#l", "#w폭탄이 터지는 것으로 부터 생존 할 수 있다.#l"));

        auction_skill_List.add(new PlayerSkillInfo(10, "#g[방탄조끼]#l", "#w폭탄이 터지거나#l #o[자폭] [럭키가이]#l #w으로부터 생존 할 수 있다.#l"));
        auction_skill_List.add(new PlayerSkillInfo(11, "#o[럭키가이]#l", "#p[50%]#l #w확률로 폭탄이 터지면 플레이어 중 한명이 대신 피해를 받는다.#l"));
        auction_skill_List.add(new PlayerSkillInfo(12, "#g[천리안]#l", "#w폭탄이 언제 터지는지 타이머를 볼 수 있게 된다.#l"));
        auction_skill_List.add(new PlayerSkillInfo(13, "#g[시간왜곡]#l", "#w나를 제외한 플레이어들에게 폭탄을 턴마다 #l#p[5초]#l#w간 돌릴 수 없게 만든다.#l"));
        auction_skill_List.add(new PlayerSkillInfo(14, "#y[바이러스]#l", "#w모든 플레이어들이 폭탄을 돌릴때마다#l #p[1~10초]#l#w씩 폭파 시간을 앞당긴다.#l"));
        auction_skill_List.add(new PlayerSkillInfo(15, "#y[새치기]#l", "#w폭탄이 플레이어 순서대로 돌아가지 않고#l #p[랜덤]#l#w으로 돌아간다.#l"));


        auction_debuff_skill_List.add(new PlayerSkillInfo(100, "#g[시간왜곡]#l", "#w폭탄을 턴마다 #l#p[5초]#l#w간 돌릴 수 없다.#l"));
        auction_debuff_skill_List.add(new PlayerSkillInfo(14, "#o[바이러스]#l", "#w폭탄을 돌릴때마다 #l#p[1~10초]#l#w씩 폭파 시간을 앞당긴다.#l"));
        auction_debuff_skill_List.add(new PlayerSkillInfo(15, "#o[새치기]#l", "#w폭탄이 플레이어 순서대로 돌아가지 않고 #l#p[랜덤]#l#w으로 돌아간다.#l"));

    }



    public PlayerSkillInfo(int skillcode, String skillName, String skillComment){
        this.skillcode = skillcode;
        this.skillname = skillName;
        this.skillcomment = skillComment;
    }

    public int getSkillcode() {
        return skillcode;
    }

    public String getSkillname() {
        return skillname;
    }

    public String getSkillcomment() {
        return skillcomment;
    }

    public byte[] sendSkillInfo(LittleEndianWriter o){
        o.writeInt(getSkillcode());
        o.writeLengthAsciiString(getSkillname());
        o.writeLengthAsciiString(getSkillcomment());
        return o.getPacket();
    }


}
