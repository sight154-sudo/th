package com.tanhua.dubbo.server.enums;

public enum HuanXinMessageType {

    TXT("txt"), IMG("img"), LOC("loc"), AUDIO("audio"), VIDEO("video"), FILE("file");

    String type;

    HuanXinMessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}