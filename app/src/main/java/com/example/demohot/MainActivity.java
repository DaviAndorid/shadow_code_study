package com.example.demohot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.dynamic_host.PluginManager;

public class MainActivity extends AppCompatActivity {

    //给Manager定义的接口，就是一个类似传统Main函数的接口
    private PluginManager mPluginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /***
         * 1）准备好目录变量
         * 2）asset下的目录拷贝到本地磁盘
         * */
        PluginHelper.getInstance().init(this);
    }


}