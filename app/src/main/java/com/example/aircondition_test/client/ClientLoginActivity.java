package com.example.aircondition_test.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircondition_test.MainActivity;
import com.example.aircondition_test.R;
import com.example.aircondition_test.admin.AdminLoginActivity;

/**
 * @author NanDing
 * @time 2020/6/02 23:42
 */



public class ClientLoginActivity extends AppCompatActivity {

    EditText username ,password ;
    Button login ;
    Button logout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_login);

        username = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);
        login = (Button)findViewById(R.id.buttonlogin);
        logout = (Button)findViewById(R.id.gotomain);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String roomnum = username.getText().toString();   //用户名
                String pw = password.getText().toString();   //密码

                if( "".equals(roomnum) || "".equals(pw)){
                    Toast.makeText(ClientLoginActivity.this,"房间号或密码不能为空！",Toast.LENGTH_SHORT ).show();
                }
                else if(Integer.parseInt(roomnum) > 1005 || Integer.parseInt(roomnum)< 1001 ){
                    Toast.makeText(ClientLoginActivity.this,"房间号有误！",Toast.LENGTH_SHORT ).show();
                }
                else if(roomnum.equals(pw)){
                    Toast.makeText(ClientLoginActivity.this,roomnum+"号房间空调系统登陆成功！",Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent(ClientLoginActivity.this, ClientActivity.class);
                    String roomTemp = "";
                    switch (roomnum){
                        case "1001":
                            roomTemp = "32" ;
                            break;
                        case "1002":
                            roomTemp = "26" ;
                            break;
                        case "1003":
                            roomTemp = "30" ;
                            break;
                        case "1004":
                            roomTemp = "29" ;
                            break;
                        case "1005":
                            roomTemp = "35" ;
                            break;
                         default:
                             break ;
                    }
                    intent.putExtra("roomtemp",roomTemp);
                    intent.putExtra("roomnum",roomnum);
                    startActivity(intent);
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClientLoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
