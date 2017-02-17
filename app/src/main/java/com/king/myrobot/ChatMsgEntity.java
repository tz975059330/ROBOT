package com.king.myrobot;

/**
 * Created by 16230 on 2016/8/3.
 */

public class ChatMsgEntity {
    private static final String TAG = ChatMsgEntity.class.getSimpleName();
    //名字
    private String name;
    //日期
    private String date;

    public ChatMsgEntity() {
    }

    public ChatMsgEntity(String date, String text, String name, boolean isComMeg) {
        this.date = date;
        this.text = text;
        this.name = name;
        this.isComMeg = isComMeg;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setComMeg(boolean comMeg) {
        isComMeg = comMeg;
    }

    //内容
    private String text;

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public boolean isComMeg() {
        return isComMeg;
    }

    public String getText() {
        return text;
    }

    //是否是对方发来的消息
    private boolean isComMeg = true;


}
