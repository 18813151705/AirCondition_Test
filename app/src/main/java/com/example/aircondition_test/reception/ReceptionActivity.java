package com.example.aircondition_test.reception;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircondition_test.FileHelper;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceptionActivity extends AppCompatActivity {

    EditText roomNum ;
    int _roomNum ;         // 房间号
    TextView show ;
    Button getAccount, getDetaiAccount ;      // 获取账单和详单按钮
    Button printAccount , printDetailAccount ;  // 打印账单和详单按钮
    Button logout ;     // 退出登录
    public Handler handler ,handler2 ;
    private Context context ;
    FileHelper fileHelper1 , fileHelper2 ;     // 文件输出流
    Account account = new Account() ;
    DetailAccount detailAccount = new DetailAccount();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qiantai);
        context = getApplicationContext();

        logout = (Button)findViewById(R.id.logout);
        roomNum = (EditText)findViewById(R.id.roomnum);        // 输入房间号
        getAccount = (Button)findViewById(R.id.get_account);  // 获取账单按钮
        getDetaiAccount = (Button)findViewById(R.id.get_Daccount);  // 获取详单按钮
        printAccount = (Button)findViewById(R.id.print_account) ;
        printDetailAccount = (Button)findViewById(R.id.print_detailaccount);  // 打印账单和详单按钮
        show = (TextView)findViewById(R.id.showaccount);         // 展示账单和详单的文本框


        // 账单点击事件
        getAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _roomNum = Integer.parseInt(roomNum.getText().toString());  // 获取房间号
                new Thread(new Runnable(){
                    @Override
                    public void run() {

                        getAccount();  //请求获取账单！
                    }
                }).start();
            }
        });

        // 详单点击事件
        getDetaiAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _roomNum = Integer.parseInt(roomNum.getText().toString());  // 获取房间号
                new Thread(new Runnable(){
                    @Override
                    public void run() {

                        getDetailAccount();   // 请求获取详单
                    }
                }).start();
            }
        });

        // 打印账单点击事件
        printAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileHelper1 = new FileHelper(context);
                String fileName = String.valueOf(_roomNum)+"_account.txt" ;
                String fileContext = show.getText().toString();
                try {
                    fileHelper1.save(fileName,fileContext);
                    Toast.makeText(getApplicationContext(), "账单打印成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 打印详单
        printDetailAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileHelper2 = new FileHelper(context);
                String fileName = String.valueOf(_roomNum)+"_detail_account.txt" ;
                String fileContext = show.getText().toString();
                try {
                    fileHelper2.save(fileName,fileContext);
                    Toast.makeText(getApplicationContext(), "详单打印成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // 使用handler更新UI
        handler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                        String s = "";
                        s += "房间号："+account.getRoomID()+'\n';
                        if(account.getFee().toString().length() <= 3 )
                            s += "费用："+ account.getFee()+"元";
                        else{
                            s += "费用："+ account.getFee().toString().substring(0,4)+"元";
                        }
                        show.setText(s);    // 显示到显示框
                        break;
                    case 2:
                        String s2 = "";
                        s2 += "房间号："+detailAccount.getRoomID()+'\n';
                        s2 += "费率：1元/度\n——————————————\n" ;

                        for(int i=0;i<detailAccount.startTime.length();i++){
                            try {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date start = new Date(Long.parseLong(detailAccount.startTime.get(i).toString())*1000L);
                                //if(i < detailAccount.endTime.length())
                                Date end = new Date(Long.parseLong(detailAccount.endTime.get(i).toString())*1000L);
                                s2 += "开机："+ simpleDateFormat.format(start)+"\n";
                                s2 += "关机："+ simpleDateFormat.format(end) +"\n" ;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        s2 += "——————————————\n";
                        if(detailAccount.getFee().toString().length() <= 3 ){
                            s2 += "耗电："+ detailAccount.getFee()+"度\n";
                            s2 += "费用："+ detailAccount.getFee()+"元\n";
                        }
                        else{
                            s2 += "耗电："+ detailAccount.getFee().toString().substring(0,4)+"度\n";
                            s2 += "费用："+ detailAccount.getFee().toString().substring(0,4)+"元\n";
                        }

                        show.setText(s2);

                        break;
                }
            }
        };

        //  退出登录
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReceptionActivity.this, ReceptionLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void getAccount(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            JSONObject jsonObject = new JSONObject();          // 需要发送的json数据类
            jsonObject.put("type","FeeQuery");
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("room_num",_roomNum) ;
            jsonObject.put("data",jsonObject1.toString());
            String jsonstring = jsonObject.toString();

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            // 转换为json
            Log.i("账单请求收到：", s);
            JSONObject jsonObject2 = new JSONObject(s);             // 识别收到的json / 保存到类里
            String data = jsonObject2.get("data").toString();
            JSONObject jsonObject3 = new JSONObject(data);
            String fee = jsonObject3.get("fee").toString();
            JSONObject jsonObject4 = new JSONObject(fee);
            System.out.println("fee"+fee);
            account.setRoomID(jsonObject4.get("room_num").toString());   // 保存到账单类
            account.setFee(jsonObject4.get("cost").toString());

            Message msg = new Message();
            msg.what = 1 ;       // 1——账单
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

    // 获取详单
    public void getDetailAccount(){
        try{
            Socket socketClient = new Socket("192.168.0.10", 8888);     	//1.bind

            InputStream in=socketClient.getInputStream();			//2.获得IO流
            OutputStream out = socketClient.getOutputStream();

            System.out.println("+++++++++++++++ ininin ++++++++++++++++");

            JSONObject jsonObject = new JSONObject();          // 需要发送的json数据类
            jsonObject.put("type","GetDetailList");
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("room_num",_roomNum);
            jsonObject.put("data",jsonObject1.toString());
            String jsonstring = jsonObject.toString();

            out.write(jsonstring.getBytes());					//3.发送
            out.flush();

            byte[] bytes = new byte[1024];
            in.read(bytes);											//4.接收
            String s=new String(bytes,"UTF-8");
            // 转换为json
            JSONObject jsonObject2 = new JSONObject(s);
            Log.i("详单请求收到：", s);
            String data = jsonObject2.get("data").toString();
            JSONObject jsonObject3 = new JSONObject(data);
            String fee = jsonObject3.get("detail").toString();
            JSONObject jsonObject4 = new JSONObject(fee);
            detailAccount.setRoomID(jsonObject4.get("room_num").toString());
            detailAccount.setFee(jsonObject4.get("total_fee").toString());
            detailAccount.setFeeRate("1");
            detailAccount.setPower(jsonObject4.get("total_power").toString());
            detailAccount.startTime  = (JSONArray) jsonObject4.get("start_wind_list");
            detailAccount.endTime = (JSONArray)jsonObject4.get("stop_wind_list") ;

            Message msg = new Message();
            msg.what = 2 ;
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
