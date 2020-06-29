package com.example.aircondition_test.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aircondition_test.MainActivity;
import com.example.aircondition_test.R;

import java.sql.SQLOutput;


// 管理员登录界面
public class AdminLoginActivity extends AppCompatActivity {
    EditText username ,password ;
    Button login ,btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login);

        username = (EditText) findViewById(R.id.editText1);
        password = (EditText) findViewById(R.id.editText2);
        login = (Button)findViewById(R.id.buttonlogin);
        btnReturn = (Button)findViewById(R.id.backto);


        // 按钮“管理员登录” 的点击事件
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String un = username.getText().toString();   //用户名
                String pw = password.getText().toString();   //密码
                System.out.println(un);
                System.out.println(pw);

                if( "".equals(un) || "".equals(pw)){
                    Toast.makeText(AdminLoginActivity.this,"用户名或密码不能为空！",Toast.LENGTH_SHORT ).show();
                }
                else if("nanding".equals(un)==false ||"nanding".equals(pw)==false ){
                    Toast.makeText(AdminLoginActivity.this,"用户名或密码不正确！",Toast.LENGTH_SHORT ).show();
                }
                else if("nanding".equals(un) && "nanding".equals(pw) ){
                    Toast.makeText(AdminLoginActivity.this,"管理员登陆成功！",Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent(AdminLoginActivity.this, AdminActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
