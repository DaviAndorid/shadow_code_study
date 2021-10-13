
package com.example.dynamic_host;

import android.content.Context;
import android.util.Log;


import com.example.dynamic_host.common.InstalledApk;

import java.io.File;

final class ManagerImplLoader extends ImplLoader {
    static final String TAG = "daviAndroid";

    private static final String MANAGER_FACTORY_CLASS_NAME = "com.example.sample_manager.ManagerFactoryImpl";
    //1）背景：插件apk 和 宿主 都用了的
    //2）实现方式：插件apk 编译的时候生效，但是生成apk的时候不ManagerFactory打进去；在宿主运行加载插件的时候，对插件的classLoader
    //定制话，实现插件用宿主的类
    private static final String[] REMOTE_PLUGIN_MANAGER_INTERFACES = new String[]
            {
                    "com.example.dynamic_host",
            };
    final private Context applicationContext;
    final private InstalledApk installedApk;

    ManagerImplLoader(Context context, File apk) {
        //odexDir 创建
        applicationContext = context.getApplicationContext();
        File root = new File(applicationContext.getFilesDir(), "ManagerImplLoader");
        File odexDir = new File(root, Long.toString(apk.lastModified(), Character.MAX_RADIX));
        odexDir.mkdirs();
        Log.i(TAG, "ManagerImplLoader, start， odexDir = " + odexDir.getAbsolutePath());

        installedApk = new InstalledApk(apk.getAbsolutePath(), odexDir.getAbsolutePath(), null);
    }

    PluginManagerImpl load() {
        String[] strArr = {"张三", "李四", "王二麻"};
        //Apk插件加载专用ClassLoader，将宿主apk和插件apk隔离。
        ApkClassLoader apkClassLoader = new ApkClassLoader(
                installedApk,
                getClass().getClassLoader(),// 宿主ClassLoader
                loadWhiteList(installedApk),
                1
        );

        //将原Context的《Resource》和《ClassLoader》重新修改为新的Apk。
        Context pluginManagerContext = new ChangeApkContextWrapper(
                applicationContext,
                installedApk.apkFilePath,
                apkClassLoader
        );

        try {
            //从apk中读取接口的实现
            ManagerFactory managerFactory = apkClassLoader.getInterface(
                    ManagerFactory.class,
                    MANAGER_FACTORY_CLASS_NAME
            );
            return managerFactory.buildManager(pluginManagerContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    String[] getCustomWhiteList() {
        return REMOTE_PLUGIN_MANAGER_INTERFACES;
    }
}
