package com.example.aircondition_test.admin;


// 各个房间使用空调情况
public class AirconditionState {

    private String RoomID ;  //房间号
    private String power ; // 空调使用状态（正在送风/正在等待送风）
    private String mode ;         //制冷/制热
    private String windLevel ;     // 微风/中风/大风
    private String temp ;            // 房间温度
    private String tar_temp ;        // 目标温度
    private String totalPower ;      // 耗电量
    private String fee ;             // 费用

    public String getRoomID() {
        return RoomID;
    }

    public void setRoomID(String roomID) {
        RoomID = roomID;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getWindLevel() {
        return windLevel;
    }

    public void setWindLevel(String windLevel) {
        this.windLevel = windLevel;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTar_temp() {
        return tar_temp;
    }

    public void setTar_temp(String tar_temp) {
        this.tar_temp = tar_temp;
    }




    public String getTotalPower() {
        return totalPower;
    }

    public void setTotalPower(String totalPower) {
        this.totalPower = totalPower;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }
}
