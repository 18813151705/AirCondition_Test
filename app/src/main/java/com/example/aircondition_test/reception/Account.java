package com.example.aircondition_test.reception;

// 账单类
public class Account {

    private String RoomID ; //房间号
    private String Fee ;       //所需支付费用


    public String getRoomID() {
        return RoomID;
    }

    public void setRoomID(String roomID) {
        RoomID = roomID;
    }

    public String getFee() {
        return Fee;
    }

    public void setFee(String fee) {
        Fee = fee;
    }
}
