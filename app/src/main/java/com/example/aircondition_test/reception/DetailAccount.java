package com.example.aircondition_test.reception;

import org.json.JSONArray;

// 详细账单类
public class DetailAccount {

    private String RoomID ;

    public JSONArray startTime = new JSONArray() ;// 开始时间（若送风时被抢占优先级或时间片过时，会被送到等待队列，所以开始时间和结束时间可能不止一个）
    public JSONArray endTime = new JSONArray() ;  //结束时间
    private int serviceTime ;   // 总送风时间
    private int waitTime ;      // 总等待时间

    private String Power ;    // 耗电
    private String FeeRate ;  // 费率
    private String Fee ;      // 费用

    public DetailAccount() {
    }

    public static void getDetailAccount(String RoomID){
        // 向服务器请求获取 房间空调详细账单
        // 接收服务器的回送并展示到页面
    }


    public String getRoomID() {
        return RoomID;
    }

    public void setRoomID(String roomID) {
        RoomID = roomID;
    }

    public JSONArray getStartTime() {
        return startTime;
    }

    public void setStartTime(JSONArray startTime) {
        this.startTime = startTime;
    }


    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public String getFeeRate() {
        return FeeRate;
    }

    public void setFeeRate(String feeRate) {
        FeeRate = feeRate;
    }

    public String getFee() {
        return Fee;
    }

    public void setFee(String fee) {
        Fee = fee;
    }

    public String getPower() {
        return Power;
    }

    public void setPower(String power) {
        Power = power;
    }

    public JSONArray getEndTime() {
        return endTime;
    }

    public void setEndTime(JSONArray endTime) {
        this.endTime = endTime;
    }
}
