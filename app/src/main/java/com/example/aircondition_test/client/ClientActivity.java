package com.example.aircondition_test.client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircondition_test.MainActivity;
import com.example.aircondition_test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author NanDingt
 * @time 2020/6/09 21:51
 */

public class ClientActivity extends AppCompatActivity {

    Button req ,btnswitch ,logout ;
    private String pat ,tem , wind;  // 获取下拉框的值（分别是模式/温度/风速）
    int mode ,temperature ,wind_level ;
    String roomNum ,roomTemp ;
    int _roomNum = 0 ;
    private Handler handler;
    TextView curTem ,modeState, windState,tergetTem,runState;
    TextView roomID , usedPower, feeRate , fee ;
    float _roomTemperature ,_roomFee = 0 ;
    int breakout = 0,breakout2 = 0 ,send_stop = 0 ,inService = 0; //  在服务队列中时 inservice 为1
    JSONArray list = new JSONArray();

    int pre_windlevel  = 0 , wind_flag = 0 ;

    int ac_off = 0 ;   // 是否关机
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        Intent it2 = getIntent();
        roomNum = it2.getStringExtra("roomnum");  // 房间号
        _roomNum = Integer.parseInt(roomNum);            // 整形房间号
        roomTemp = it2.getStringExtra("roomtemp"); // 房间温度
        _roomTemperature = Float.parseFloat(roomTemp);   // ***房间当前温度***
        System.out.println("roomnum : "+ roomNum);
        curTem = (TextView)findViewById(R.id.tem_cur);
        curTem.setText(roomTemp);  // 显示当前的房间温度
        modeState = (TextView)findViewById(R.id.mode_state);
        windState = (TextView)findViewById(R.id.wind_state);
        tergetTem = (TextView)findViewById(R.id.target_tem);
        runState = (TextView)findViewById(R.id.run_state);
        req = (Button)findViewById(R.id.button1);
        btnswitch = (Button)findViewById(R.id.buttonswitch);
        logout = (Button)findViewById(R.id.buttonlogout) ;

        // 运行时间/耗电量/费率/费用
        roomID = (TextView) findViewById(R.id.roomid);
        roomID.setText(roomNum);
        usedPower = (TextView)findViewById(R.id.power);
        feeRate = (TextView) findViewById(R.id.feerate);
        fee = (TextView) findViewById(R.id.fee);

        // 空调设置
        req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner pattern = (Spinner)findViewById(R.id.spinner);
                Spinner temp = (Spinner)findViewById(R.id.spinner2);
                Spinner windspeed = (Spinner)findViewById(R.id.spinner3);

                pat = (String) pattern.getSelectedItem();
                if(pat.equals("制冷"))
                    mode = 0 ;   //制冷是0             // 发送给服务端 的 模式
                else if(pat.equals("制热"))
                    mode = 1 ;
                tem = (String) temp.getSelectedItem();
                temperature = Integer.parseInt(tem);       // 发送给服务端的目标温度
                wind = (String)windspeed.getSelectedItem();
                if(wind.equals("微风"))                     // 发送给服务端的 风速
                    wind_level = 1 ;
                else if(wind.equals("中风"))
                    wind_level = 2 ;
                else if(wind.equals("大风"))
                    wind_level = 3 ;

                if(pre_windlevel != wind_level)  // 如果当前风速与前一个风速不同，则为调整风速请求
                    wind_flag = 1 ;
                else
                    wind_flag = 0 ;
                pre_windlevel = wind_level;   // 当前风速变为 pre风速
                System.out.println(pat+tem+wind);

                // 开启与服务端交互线程
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        req_set();              // 与服务器建立连接并发送数据
                    }
                }).start();

            }
        });

        handler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                        DecimalFormat df = new DecimalFormat("#.0");
                        String newcur_tem = df.format(_roomTemperature);
                        String newcur_fee = df.format(_roomFee);
                        float fcur_fee = Float.parseFloat(newcur_fee);
                        curTem.setText(newcur_tem.toString().substring(0,4));
                        usedPower.setText(String.valueOf(fcur_fee)+"度");
                        fee.setText(String.valueOf(fcur_fee)+"元");
                        breakout = msg.getData().getInt("breakout");
                        if(breakout == 1)
                            runState.setText("停止送风");
                        break;
                    case 0:
                        breakout = 0 ;     // 是否停止送风
                        send_stop = 0 ;
                        // 显示模式
                        if(mode == 1)
                            modeState.setText("制热");
                        else
                            modeState.setText("制冷");
                        // 显示风速
                        if(wind_level == 1){
                            windState.setText("微风");
                        }
                        else if(wind_level == 2){
                            windState.setText("中风");
                        }
                        else{
                            windState.setText("大风");
                        }
                        //显示费率
                        feeRate.setText("1元/度");
                        // 显示目标温度
                        tergetTem.setText(tem);
                        // 显示空调运行状态（开机/待机/送风？？）
                        runState.setText("等待调度");
                        break;
                    case 2:   // 关机
                        ac_off = 1;
                        runState.setText("已关机");
                        break ;

                    case 3:    //停止送风
                        break;

                    case 4:     //回温算法
                        DecimalFormat df2 = new DecimalFormat("#.0");
                        String newcur_tem2 = df2.format(_roomTemperature);
                        curTem.setText(newcur_tem2.toString().substring(0,4));
                        breakout = msg.getData().getInt("breakout");
//                        if(breakout == 0)
//                            runState.setText("正在送风");
                        break;

                    case 5:    //关机之后的回温算法
                        DecimalFormat df3 = new DecimalFormat("#.0");
                        String newcur_tem3 = df3.format(_roomTemperature);
                        curTem.setText(newcur_tem3.toString().substring(0,4));
                        breakout2 = msg.getData().getInt("breakout2");
                        break;

                    case 6:
                        int flag = 0 ;
                        for(int i = 0 ; i<list.length();i++){
                            try {
                                if(roomNum.equals(list.get(i).toString())){
                                    runState.setText("正在送风");
                                    inService = 1 ;    //运行
                                    flag = 1 ;
                                    System.out.println("inservicing~~~~~");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(flag == 0){
                            inService = 0 ;   //等待
                            runState.setText("等待调度");
                            if(ac_off == 1)
                                runState.setText("已关机");
                        }
                        break;
                }
            }
        };

        // 开机开关机
        btnswitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnswitch.getText().toString().equals("空调开机")){
                    ac_off = 0 ;   // 开机
                    Toast.makeText(ClientActivity.this,"空调已开机！",Toast.LENGTH_SHORT ).show();
                    btnswitch.setText("空调关机");

                    Spinner pattern = (Spinner)findViewById(R.id.spinner);
                    Spinner temp = (Spinner)findViewById(R.id.spinner2);
                    Spinner windspeed = (Spinner)findViewById(R.id.spinner3);

                    pat = (String) pattern.getSelectedItem();
                    if(pat.equals("制冷"))
                        mode = 0 ;   //制冷是0             // 发送给服务端 的 模式
                    else if(pat.equals("制热"))
                        mode = 1 ;
                    tem = (String) temp.getSelectedItem();
                    temperature = Integer.parseInt(tem);       // 发送给服务端的目标温度
                    wind = (String)windspeed.getSelectedItem();
                    if(wind.equals("微风"))                     // 发送给服务端的 风速
                        wind_level = 1 ;
                    else if(wind.equals("中风"))
                        wind_level = 2 ;
                    else if(wind.equals("大风"))
                        wind_level = 3 ;
                    pre_windlevel = wind_level;   // 当前风速变为 pre风速

                    // 开启与服务端交互线程
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            AirConditionOn();              // 与服务器建立连接并发送数据
                            // 请求调度列表？
                            while (true){
                                send_msg();     // 定时发送空调运行状态？
                                try {               // 每隔五秒钟
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(ac_off == 1 || breakout == 1 )   // 关机则跳出循环
                                    break;
                                getServiceArray() ;    // 获取调度队列
                            }
                        }
                    }).start();

                    // 开启与服务端交互线程
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            while (true){
                                try {
                                    Thread.sleep(12000);   // 每12秒更新一次UI
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(breakout == 1){
                                    if(send_stop == 0){
                                        stopService() ;    // 停止送风
                                        send_stop ++ ;
                                    }
                                    cal_temprise();
                                    continue;
                                }
                                if(breakout ==0 && send_stop == 1 ){    // 超过目标温度一度，恢复送风请求
                                    if(ac_off == 0)
                                        AirConditionOn();
                                    continue;
                                }
                                if(ac_off == 1)    // 关机了
                                    break;
                                if(inService == 1){     //  在运行队列中
                                    cal() ;       // 更新控件
                                    send_msg();  // 定时给服务端发送运行信息（费用/耗电）
                                }
                            }

                        }
                    }).start();

                }
                else{
                    btnswitch.setText("空调开机");
                    Toast.makeText(ClientActivity.this,"空调已关机！",Toast.LENGTH_SHORT ).show();

                    // 关机—— 开启与服务端交互线程
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            AirConditionOff();              // 关机
                        }
                    }).start();

                    // 回温算法——每分钟回温0.5度，知道回到初始温度
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true){
                                try {
                                    Thread.sleep(12000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(breakout2 == 1 || ac_off == 0)    // 到达房间初始温度或者 已经重新开机
                                    break;
                                cal_temprise2();
                            }
                        }
                    }).start();

                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ac_off = 1 ;
                Intent intent = new Intent(ClientActivity.this, ClientLoginActivity.class);
                startActivity(intent);
            }
        });

    }


    int times = 0 ;
    // 开机 和 设置参数 时候调用
    // 获取当前设置的温度，费用，风速 等等，显示到界面上
    public void cal() {
        int mode = 0 ;  //制冷mode=0
        if(modeState.getText().toString().equals("制热"))
            mode = 1 ;  // 制热mode=1
        //float cur_tem = Float.parseFloat(curTem.getText().toString());   // 当前温度
        float tar_tem = Float.parseFloat(tergetTem.getText().toString()); //目标温度
//        float cur_fee = Float.parseFloat(fee.getText().toString()) ;
        String wind = windState.getText().toString();
        int breakout = 0;        // 到达目标温度——停止送风—— 启动回温算法
        if(wind.equals("微风"))                       // 风速
            wind_level = 1 ;
        else if(wind.equals("中风"))
            wind_level = 2 ;
        else if(wind.equals("大风"))
            wind_level = 3 ;

        if(wind_level == 1){
            times ++ ;
            if(times == 3 ) {
                _roomFee += 0.20;       // 微风 ：1度/3min  0.4 ℃/1min
                times = 0;
            }

            if(_roomTemperature < tar_tem)
                _roomTemperature += 0.08 ;
            else
                _roomTemperature -= 0.08 ;
        }
        else if(wind_level == 2 ){         // 中风： 1度/2min     0.5 ℃/1min
            _roomFee += 0.10 ;
            if(_roomTemperature < tar_tem)
                _roomTemperature += 0.10 ;
            else
                _roomTemperature -= 0.10 ;
        }
        else if(wind_level == 3){      // 大风    1度/1min      0.6 ℃/1min
            _roomFee += 0.20 ;
            if(_roomTemperature < tar_tem)
                _roomTemperature += 0.12 ;
            else
                _roomTemperature -= 0.12 ;
        }

        // 是否到达目标温度
        // 到达目标温度： breakout = 1
        if(mode == 0){    // 制冷状态下，到达目标温度
            if(_roomTemperature <= tar_tem)
                breakout = 1 ;
        }
        else if(mode == 1){
            if(_roomTemperature >= tar_tem)
                breakout = 1 ;
        }

       // DecimalFormat df = new DecimalFormat("#.0");
        //String newcur_fee = df.format(cur_fee);
        Message msg = new Message();
        msg.what = 1 ;
        Bundle bundle = new Bundle();
        //bundle.putFloat("cur_fee",Float.parseFloat(newcur_fee));
        bundle.putInt("breakout",breakout);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    // 请求开机——设置空调的运行参数——请求送风
    public void AirConditionOn(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();


            long timeStamp = System.currentTimeMillis();
            long timeStamp_Second = timeStamp / 1000;
            //System.out.println(timeStamp);
            System.out.println(timeStamp_Second);

            String wlevel = "" ;
            if(wind_level == 1)
                wlevel = "low";
            else if(wind_level == 2)
                wlevel = "mid";
            else
                wlevel = "high";
            JSONObject jsonObject = new JSONObject();  // 需要发送的json数据类
            jsonObject.put("type","AirConditionerOn");
            JSONObject jsonObject1 = new JSONObject();   //data字段
            jsonObject1.put("room_num",Integer.parseInt(roomNum));
            jsonObject1.put("power","on");  // 1开/0关
            jsonObject1.put("mode","cold");
            jsonObject1.put("wind_level",wlevel);
            jsonObject1.put("temperature",temperature);
            jsonObject1.put("open_time",timeStamp_Second);
            jsonObject.put("data",jsonObject1.toString());
            //jsonObject.put("length",len);
            String jsonstring = jsonObject.toString();
            System.out.println("send_ON: "+jsonstring);
            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            Log.i("received_ON：", s);
            JSONObject jsonObject2 = new JSONObject(s);
            String data = jsonObject2.get("data").toString();
            JSONObject jsonObject3 = new JSONObject(data);
            if(Integer.parseInt(jsonObject3.get("code").toString())==200){     // 正确返回
                System.out.println("inininininininiin");
                Message msg = new Message();
                msg.what = 0 ;
                msg.obj = temperature ;
                handler.sendMessage(msg);
            }

            socketClient.close();									//5.关闭
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();} catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 设置空调的运行参数
    public void req_set(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            long timeStamp = System.currentTimeMillis();
            long timeStamp_Second = timeStamp / 1000;
            System.out.println(timeStamp_Second);
            String wlevel = "" ;
            if(wind_level == 1)
                wlevel = "low";
            else if(wind_level == 2)
                wlevel = "mid";
            else
                wlevel = "high";
            JSONObject jsonObject = new JSONObject();  // 需要发送的json数据类
            jsonObject.put("type","AirConditionerSetParam");
            JSONObject jsonObject1 = new JSONObject();   //data字段
            jsonObject1.put("room_num",Integer.parseInt(roomNum));
            jsonObject1.put("mode","cold");
            jsonObject1.put("wind_level",wlevel);
            jsonObject1.put("temperature",temperature);
            jsonObject1.put("wind_flag",wind_flag);   // 是否需要 调度  / 是否是调整风速请求
            //jsonObject1.put("open_time",timeStamp_Second);
            jsonObject.put("data",jsonObject1.toString());
            //jsonObject.put("length",len);
            String jsonstring = jsonObject.toString();

            Message msg = new Message();
            msg.what = 0 ;
            handler.sendMessage(msg);

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

//            while(true){ }
            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            Log.i("received_ON：", s);

            socketClient.close();									//5.关闭
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();} catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 请求设置空调的运行参数  停止送风
    public void stopService(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            long timeStamp = System.currentTimeMillis();
            long timeStamp_Second = timeStamp / 1000;
            //System.out.println(timeStamp);
            System.out.println(timeStamp_Second);

            JSONObject jsonObject = new JSONObject();  // 需要发送的json数据类
            jsonObject.put("type","AirConditionerStopWind");
            JSONObject jsonObject1 = new JSONObject();   //data字段
            jsonObject1.put("room_num",Integer.parseInt(roomNum));   //房间号
            jsonObject1.put("end_wind",timeStamp_Second);             // 停止送风
            jsonObject.put("data",jsonObject1.toString());
            String jsonstring = jsonObject.toString();

            // 发送给 handler
            Message msg = new Message();
            msg.what = 3 ;
            handler.sendMessage(msg);

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            Log.i("received_STOP：", s);

            socketClient.close();									//5.关闭
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();} catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 关机请求
    public void AirConditionOff(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            long timeStamp = System.currentTimeMillis();
            long timeStamp_Second = timeStamp / 1000;
            JSONObject jsonObject = new JSONObject();  // 需要发送的json数据类
            jsonObject.put("type","AirConditionerOff");
            JSONObject jsonObject1 = new JSONObject();   //data字段
            jsonObject1.put("room_num",Integer.parseInt(roomNum));
            jsonObject1.put("power",0);  // 1开/0关
            jsonObject1.put("close_time",timeStamp_Second);
            jsonObject.put("data",jsonObject1.toString());
            String jsonstring = jsonObject.toString();

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            // 发送给handler
            Message msg = new Message();
            msg.what = 2 ;
            handler.sendMessage(msg);

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            Log.i("received_OFF：", s);

            socketClient.close();									//5.关闭
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();} catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 定时发送 消息 ： 耗电量，费用等等
    public void send_msg(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            long timeStamp = System.currentTimeMillis();
            long timeStamp_Second = timeStamp / 1000;
            //System.out.println(timeStamp);
            System.out.println(timeStamp_Second);

            JSONObject jsonObject = new JSONObject();  // 需要发送的json数据类
            jsonObject.put("type","SetRoomData");
            JSONObject jsonObject1 = new JSONObject();   //data字段
            jsonObject1.put("room_num",Integer.parseInt(roomNum));   //房间号
            DecimalFormat df = new DecimalFormat("#.0");
            String newcur_tem = df.format(_roomTemperature);
            jsonObject1.put("room_temperature",Float.parseFloat(newcur_tem));  // 当前温度
            jsonObject1.put("total_power",_roomFee);           // 费用
            jsonObject.put("data",jsonObject1.toString());
            String jsonstring = jsonObject.toString();

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            Log.i("received_SENDMSG：", s);

            socketClient.close();									//5.关闭
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();} catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 获取 服务队列 ： 根据服务队列来判断是否 进行送风请求
    public void getServiceArray(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            JSONObject jsonObject = new JSONObject();  // 需要发送的json数据类
            jsonObject.put("type","GetServingQueue");
            String jsonstring = jsonObject.toString();

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            Log.i("received-ServiceArray：", s);
            JSONObject jsonObject1 = new JSONObject(s);
            String data = jsonObject1.get("data").toString();
            JSONObject jsonObject2 = new JSONObject(data);
            list = (JSONArray) jsonObject2.get("serving_queue");     // 获取到 服务队列
            System.out.println("abcd"+list);

            // 发送给 handler
            Message msg = new Message();
            msg.what = 6 ;
            handler.sendMessage(msg);

            socketClient.close();									//5.关闭
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();} catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 到达目标温度之后的 回温算法
    public void cal_temprise(){
        int mode = 0 ;  //制冷mode=0
        if(modeState.getText().toString().equals("制热"))
            mode = 1 ;  // 制热mode=1
        //float cur_tem = Float.parseFloat(curTem.getText().toString());   // 当前温度
        float tar_tem = Float.parseFloat(tergetTem.getText().toString()); //目标温度

        float startTemp =Float.parseFloat(roomTemp);       // 房间初始温度
        int breakout = 1;        // 如果超过目标温度1度： breakout = 0

        if(startTemp > tar_tem)   // 如果房间初始温度大于目标温度
            _roomTemperature += 0.10 ;
        else
            _roomTemperature -= 0.10 ;

        // 是否超过目标温度1度
        // 超过目标温度1度： breakout = 0 ： 重新开始送风
        if(mode == 0){    // 制冷状态下
            if(_roomTemperature >= tar_tem + 1 )
                breakout = 0 ;
        }
        else if(mode == 1){
            if(_roomTemperature <= tar_tem - 1 )
                breakout = 0 ;
        }

        //DecimalFormat df = new DecimalFormat("#.0");
        //String newcur_tem = df.format(cur_tem);
        Message msg = new Message();
        msg.what = 4 ;
        Bundle bundle = new Bundle();
        //bundle.putFloat("cur_tem",Float.parseFloat(newcur_tem));
        bundle.putInt("breakout",breakout);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    // 关机之后的回温算法
    public void cal_temprise2(){
        int mode = 0 ;  //制冷mode=0
        if(modeState.getText().toString().equals("制热"))
            mode = 1 ;  // 制热mode=1
        //float cur_tem = Float.parseFloat(curTem.getText().toString());   // 当前温度
        float tar_tem = Float.parseFloat(tergetTem.getText().toString()); //目标温度

        float startTemp = Float.parseFloat(roomTemp);       // 房间初始温度
        int breakout2 = 0;        // 如果到达初始温度： breakout2 = 0

        _roomTemperature += 0.10 ;    // 回温0.5 度

        // 是否超过目标温度1度
        // 超过目标温度1度： breakout = 0 ： 重新开始送风
        if(mode == 0){    // 制冷状态下
            if( Math.abs(_roomTemperature - startTemp)<0.0001 )    // 回到初始温度
                breakout2 = 1 ;        // 退出循环
        }

        Message msg = new Message();
        msg.what = 5 ;
        Bundle bundle = new Bundle();
        //bundle.putFloat("cur_tem",Float.parseFloat(newcur_tem));
        bundle.putInt("breakout2",breakout2);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

}
