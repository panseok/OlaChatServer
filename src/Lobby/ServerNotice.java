package Lobby;

import DevTool.LittleEndianWriter;

import java.util.ArrayList;
import java.util.List;

public class ServerNotice {
    private String type, name, comment, date;



    public ServerNotice(String type, String name, String comment, String date){
        this.type = type;
        this.name = name;
        this.comment = comment;
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public byte[] sendServerNotice(LittleEndianWriter o){
        o.writeLengthAsciiString(getType());
        o.writeLengthAsciiString(getName());
        o.writeLengthAsciiString(getComment());
        o.writeLengthAsciiString(getDate());
        return o.getPacket();

    }


}
