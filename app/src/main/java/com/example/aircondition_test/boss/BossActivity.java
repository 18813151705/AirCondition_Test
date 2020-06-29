package com.example.aircondition_test.boss;

import android.content.Context;
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

import com.example.aircondition_test.FileHelper;
import com.example.aircondition_test.MainActivity;
import com.example.aircondition_test.R;
import com.example.aircondition_test.admin.AirconditionState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class BossActivity extends AppCompatActivity {

    Handler handler;
    Button getReport , printReport ,btnlogout;
    FileHelper fileHelper1;
    private Context context ;
    TextView txv ;
    List<StatisticalForm>statisticalForms = new ArrayList<>(5);   // 报表的数组
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boss);

        context = getApplicationContext();

        // 获取报表和打印报表
        txv = (TextView)findViewById(R.id.account_window);
        getReport = (Button)findViewById(R.id.reportform);
        printReport = (Button)findViewById(R.id.print_reportform);
        btnlogout = (Button)findViewById(R.id.logout);

        getReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = 1 ;
                for(int j = 0 ; j< 5 ;j++){            // 初始化报表的类
                    StatisticalForm item = new StatisticalForm();
                    item.setRoomID("100"+ String.valueOf(i));  // 1001 —— 1005
                    i++ ;
                    statisticalForms.add(item);
                }

                // 开启新线程——与服务端交互
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        getStatement();
                    }
                }).start();
            }
        });

        // 输出报表到文件
        printReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileHelper1 = new FileHelper(context);
                String fileName = "report.txt" ;
                String fileContext = txv.getText().toString();
                try {
                    fileHelper1.save(fileName,fileContext);
                    Toast.makeText(getApplicationContext(), "报表打印成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 退出登录
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BossActivity.this, BossLoginActivity.class);
                startActivity(intent);
            }
        });

        // 使用handler更新UI
        // 通信子线程与主线程之间的交互
        handler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0:
                        String forms = "";
                        for (StatisticalForm st :statisticalForms){
                            forms += "房间号："+st.getRoomID()+"   ";
                            if( st.getTotal_power().length()>3)
                                forms += "耗电量："+st.getTotal_power().substring(0,4) +"度\n";
                            else
                                forms += "耗电量："+st.getTotal_power() +"度\n";

                            if(st.getFee().length()>3)
                                forms += "费用：" + st.getFee().substring(0,4)+"元   ";
                            else
                                forms += "费用：" + st.getFee()+"元   ";

                            forms += "使用时间:" + st.getUseTime()+"\n";
                            forms += "开关次数："+st.getOnOffTimes()+"   ";
                            forms += "设置参数次数："+ st.getSetParamTimes()+"\n————————————————————\n";
                        }
                        txv.setText(forms);
                        break;
                }
            }
        };

    }

    // 请求获取 报表
    public void getStatement(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            JSONObject jsonObject = new JSONObject();          // 需要发送的json数据类
            jsonObject.put("type","GetReport");
            String jsonstring = jsonObject.toString();

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            Log.i("经理收到服务端数据：", s);
            JSONObject jsonObject1 = new JSONObject(s);
            String data = jsonObject1.get("data").toString();
            JSONObject jsonObject2 = new JSONObject(data);
            String air_conditioners = jsonObject2.get("reports").toString();   // 获取到空调状态的数组
            JSONArray jsonArray = new JSONArray(air_conditioners);
            for(int i=0;i<5;i++){
                statisticalForms.get(i).setFee(jsonArray.getJSONObject(i).get("total_fee").toString());
                statisticalForms.get(i).setTotal_power(jsonArray.getJSONObject(i).get("total_power").toString());
                statisticalForms.get(i).setOnOffTimes(jsonArray.getJSONObject(i).get("close_num").toString()); // 开关次数
                statisticalForms.get(i).setSetParamTimes(jsonArray.getJSONObject(i).get("set_param_num").toString());  //设置参数的次数
                statisticalForms.get(i).setUseTime(jsonArray.getJSONObject(i).get("used_time").toString());  //设置参数的次数
            }
            Message msg = new Message();
            msg.what = 0 ;
            handler.sendMessage(msg);
            socketClient.close();									//5.关闭通信
        }catch (UnknownHostException e){
            e.printStackTrace();}
        catch (IOException e){
            e.printStackTrace();}
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}