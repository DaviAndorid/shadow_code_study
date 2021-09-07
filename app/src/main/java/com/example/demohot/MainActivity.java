package com.example.demohot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.demohot.manager.Shadow;
import com.example.dynamic_host.PluginManager;

import java.io.File;

import static com.example.constant.Constant.FROM_ID_START_ACTIVITY;

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


        findViewById(R.id.bt_start_plu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAct();
            }
        });
    }


    void onClickAct() {
        PluginHelper.getInstance().singlePool.execute(() -> {
            //只是构建了 PluginManager 的实现，并没有实质调用
            loadPluginManager(PluginHelper.getInstance().pluginManagerFile);

            Bundle bundle = new Bundle(); //todo bundle 传递信息
            mPluginManager.enter(this, FROM_ID_START_ACTIVITY, bundle, null);
        });
    }

    private void loadPluginManager(File apk) {
        if (mPluginManager == null) {
            mPluginManager = Shadow.getPluginManager(apk);
        }
    }


}