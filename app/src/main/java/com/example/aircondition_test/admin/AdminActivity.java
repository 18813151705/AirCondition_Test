package com.example.aircondition_test.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircondition_test.MainActivity;
import com.example.aircondition_test.R;
import com.example.aircondition_test.boss.BossActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Array;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// 管理员主界面（管理员功能——实时查看各个房间空调的运行状态）
// 开机监控按钮，每分钟自动发送查看房间状态请求
public class AdminActivity extends AppCompatActivity {

    Button req,logout ;
    private Handler handler;
    List<AirconditionState>airconditionStates = new ArrayList<>(5);    // 长度为5的数组（保存获取到的房间空调信息）
    int _logout = 0 ;    // 为1时 退出获取房间信息的循环
    TextView show ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);

        show = (TextView)findViewById(R.id.showroomstate);   // 展示框

        int i = 1 ;
        for(int j = 0 ; j< 5 ;j++){            // 初始化空调的list
            AirconditionState item = new AirconditionState();
            item.setRoomID("100"+ String.valueOf(i));
            i++ ;
            airconditionStates.add(item);
        }


        // 给服务器发送请求，获取房价空调的状态
        req = (Button)findViewById(R.id.req);
        req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 开启一个新线程
                // 安卓不允许在主线程进行网络操作，必须开启新线程
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        while(true){       // 循环获取
                            if(_logout == 1)     // 点击退出登录时，_logout置为1，退出循环
                                break;

                            getMechineState();              // 与服务器建立连接并发送数据

                            try {
                                Thread.sleep(5000);      // 线程等待5秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

                // handler更新UI —— 通信线程与主线程之间的交互
                handler = new Handler(){
                    public void handleMessage(Message msg){
                        switch (msg.what){
                            case 1:
                                String _show = "";
                                for(AirconditionState item : airconditionStates){          // 将 List 里的数据 格式化 并显示到 文本框
                                    String s = "";
                                    if("".equals(item.getPower())){
                                        s += "房间号："+item.getRoomID()+"  ";
                                        s += "空调状态：未开机"+ "\n————————————————————\n";
                                    }
                                    else if(("off").equals(item.getPower())){  //关机中的空调
                                        s += "房间号："+item.getRoomID()+"  ";
                                        s += "空调状态：已关机"+ "\n";
                                        if(item.getTemp().length()>3)
                                            s += "当前温度："+item.getTemp().substring(0,4)+"℃  ";
                                        else
                                            s += "当前温度："+item.getTemp()+"℃  ";
                                        // 格式化耗电量
                                        if(item.getTotalPower().length()>3)
                                            s += "耗电量："+item.getTotalPower().substring(0,4)+"\n————————————————————\n";
                                        else
                                            s += "耗电量："+item.getTotalPower()+"\n————————————————————\n";
                                    }
                                    else if (("on").equals(item.getPower()) ){
                                        s += "房间号："+item.getRoomID()+"  ";
                                        s += "空调状态：正在运行"+ "\n";
                                        s += "风速："+item.getWindLevel()+"  ";
                                        s += "模式："+item.getMode()+"\n";
                                        s += "目标温度："+item.getTar_temp()+"℃  ";
                                        // 格式化当前温度
                                        if(item.getTemp().length()>3)
                                            s += "当前温度："+item.getTemp().substring(0,4)+"℃  \n";
                                        else
                                            s += "当前温度："+item.getTemp()+"℃  \n";
                                        // 格式化耗电量
                                        if(item.getTotalPower().length()>3)
                                            s += "耗电量："+item.getTotalPower().substring(0,4)+"\n————————————————————\n";
                                        else
                                            s += "耗电量："+item.getTotalPower()+"\n————————————————————\n";
                                    }
                                    _show += s;
                                }
                                show.setText(_show);
                                break;
                        }
                    }
                };
            }
        });
        // 退出登录
        logout = (Button)findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logout = 1 ;
                Toast.makeText(AdminActivity.this,"管理员已退出系统！",Toast.LENGTH_SHORT ).show();
                Intent intent = new Intent(AdminActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });

    }

    // 与服务器通信函数
    // 获取各个房间空调 的 运行状态
    private void getMechineState(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            JSONObject jsonObject = new JSONObject();          // 需要发送的json数据类
            jsonObject.put("type","AirConditionerFindAll");
            String jsonstring = jsonObject.toString();
            out.write(jsonstring.getBytes());					//3 .发送
            out.flush();

            byte[] bytes = new byte[2048];
            in.read(bytes);											// 4.接收到的数据
            String s=new String(bytes,"UTF-8");
            System.out.println("收到监控信息："+ s );
            // 转换为json
            JSONObject jsonObject1 = new JSONObject(s);
            String data = jsonObject1.get("data").toString();
            JSONObject jsonObject2 = new JSONObject(data);                                      // 获取到的data —— 将它保存到空调的list里
            String air_conditioners = jsonObject2.get("air_conditioners").toString();             // 获取到空调状态的数组
            JSONArray jsonArray = new JSONArray(air_conditioners);
            for(int i=0;i<5;i++){                                                          //将获取的数据保存到 List 里 ， 之后展示在文本框时，从List读数据
                airconditionStates.get(i).setRoomID(jsonArray.getJSONObject(i).get("room_num").toString());
                airconditionStates.get(i).setPower(jsonArray.getJSONObject(i).get("power").toString());
                airconditionStates.get(i).setMode("制冷");
                airconditionStates.get(i).setWindLevel(jsonArray.getJSONObject(i).get("wind_level").toString());
                airconditionStates.get(i).setTar_temp(jsonArray.getJSONObject(i).get("temperature").toString());
                airconditionStates.get(i).setTemp(jsonArray.getJSONObject(i).get("room_temperature").toString());
                airconditionStates.get(i).setTotalPower(jsonArray.getJSONObject(i).get("total_power").toString());
            }

            Message msg = new Message();
            msg.what = 1 ;
            handler.sendMessage(msg);


            socketClient.close();									//5.关闭
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();}
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
