package com.example.aircondition_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.aircondition_test.admin.AdminLoginActivity;
import com.example.aircondition_test.boss.BossLoginActivity;
import com.example.aircondition_test.client.ClientLoginActivity;
import com.example.aircondition_test.reception.ReceptionLoginActivity;

/**
 * @author NanDing
 * @time 2020/6/02 23:19
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity_login);

        Button btnClient = findViewById(R.id.button1);
        Button btnAdmin = findViewById(R.id.button2);
        Button btnBoss = findViewById(R.id.button3);
        Button btnQiantai = findViewById(R.id.button4);

        // 点击“顾客登录”  跳转到ClientActivity
        btnClient.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientLoginActivity.class);
                startActivity(intent);
            }
        });

        // 点击“管理员登录”  跳转到AdminActivity
        btnAdmin.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });

        // 点击“经理登录”  跳转到BossActivity
        btnBoss.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BossLoginActivity.class);
                startActivity(intent);
            }
        });

        // 点击“前台登录”  跳转到QiantaiActivity
        btnQiantai.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReceptionLoginActivity.class);
                startActivity(intent);
            }
        });
    }

}
