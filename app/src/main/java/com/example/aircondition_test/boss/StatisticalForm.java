package com.example.aircondition_test.boss;

// 报表
public class StatisticalForm {

    private String RoomID ;        // 房间号
    private String total_power;     // 房间总耗电量
    private String fee ;            // 费用
    private String OnOffTimes ;     // 开机关机次数
    private String setParamTimes ;        // 设置参数次数
    private String useTime ;         // 使用空调时间

    public String getRoomID() {
        return RoomID;
    }

    public void setRoomID(String roomID) {
        RoomID = roomID;
    }

    public String getTotal_power() {
        return total_power;
    }

    public void setTotal_power(String total_power) {
        this.total_power = total_power;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getOnOffTimes() {
        return OnOffTimes;
    }

    public void setOnOffTimes(String onOffTimes) {
        OnOffTimes = onOffTimes;
    }

    public String getSetParamTimes() {
        return setParamTimes;
    }

    public void setSetParamTimes(String setParamTimes) {
        this.setParamTimes = setParamTimes;
    }

    public String getUseTime() {
        return useTime;
    }

    public void setUseTime(String useTime) {
        this.useTime = useTime;
    }
}
