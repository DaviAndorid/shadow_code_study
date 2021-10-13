package com.example.demohot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.constant.Constant;
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

            /**
             * 参数准备
             * */
            Bundle bundle = new Bundle();
            //插件zip包的路径，具体zip里面有什么见：https://juejin.cn/post/7008060973038698503#heading-1
            bundle.putString(Constant.KEY_PLUGIN_ZIP_PATH, PluginHelper.getInstance().pluginManagerFile.getAbsolutePath());
            //要启动的插件标示 partKey
            bundle.putString(Constant.KEY_PLUGIN_PART_KEY, Constant.PART_KEY_PLUGIN_MAIN_APP);
            //todo 要启动插件的activity
            bundle.putString(Constant.KEY_ACTIVITY_CLASSNAME, "要启动插件的activity");

            /**
             * 进入插件
             * */
            mPluginManager.enter(this, FROM_ID_START_ACTIVITY, bundle, null);
        });
    }

    private void loadPluginManager(File apk) {
        if (mPluginManager == null) {
            mPluginManager = Shadow.getPluginManager(apk);
        }
    }


}