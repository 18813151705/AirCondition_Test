package com.example.aircondition_test.reception;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircondition_test.MainActivity;
import com.example.aircondition_test.R;

public class ReceptionLoginActivity extends AppCompatActivity {

    EditText username,password;
    Button login ;
    Button back ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qiantai_login);

        username = (EditText) findViewById(R.id.editText1);
        password = (EditText) findViewById(R.id.editText2);
        login = (Button)findViewById(R.id.buttonlogin);
        back = (Button)findViewById(R.id.backto) ;

        // 按钮“前台登录” 的点击事件
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String un = username.getText().toString();   //用户名
                String pw = password.getText().toString();   //密码

                if( "".equals(un) || "".equals(pw)){
                    Toast.makeText(ReceptionLoginActivity.this,"账号或密码不能为空！",Toast.LENGTH_SHORT ).show();
                }
                else if("nanding".equals(un)==false ||"nanding".equals(pw)==false ){
                    Toast.makeText(ReceptionLoginActivity.this,"账号或密码不正确！",Toast.LENGTH_SHORT ).show();
                }
                else if("nanding".equals(un) && "nanding".equals(pw) ){
                    Toast.makeText(ReceptionLoginActivity.this,"前台登陆成功！",Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent(ReceptionLoginActivity.this, ReceptionActivity.class);
                    startActivity(intent);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReceptionLoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
