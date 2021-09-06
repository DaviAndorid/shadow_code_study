package com.example.demohot;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginHelper {

    static final String TAG = "daviAndroid";

    /**
     * 动态加载的插件管理apk
     */
    public final static String sPluginManagerName = "pluginmanager.apk";

    public File pluginManagerFile;

    /**
     * 动态加载的插件包，里面包含以下几个部分
     * 1）插件apk，
     * 2）插件框架apk（loader apk和runtime apk）
     * 3）apk信息配置关系json文件等
     */
    //public final static String sPluginZip = BuildConfig.DEBUG ? "plugin-debug.zip" : "plugin-release.zip";

    //public File pluginZipFile;

    public ExecutorService singlePool = Executors.newSingleThreadExecutor();

    private Context mContext;

    private static PluginHelper sInstance = new PluginHelper();

    public static PluginHelper getInstance() {
        return sInstance;
    }

    private PluginHelper() {
    }

    /***
     * 1)准备好目录变量
     * 2）asset下的目录拷贝到本地磁盘
     * */
    public void init(Context context) {
        pluginManagerFile = new File(context.getFilesDir(), sPluginManagerName);
        //pluginZipFile = new File(context.getFilesDir(), sPluginZip);
        mContext = context.getApplicationContext();
        Log.i(TAG, "PluginHelper, pluginManagerFile = " + pluginManagerFile.getAbsolutePath());
        //Log.i(TAG, "PluginHelper, pluginZipFile = " + pluginZipFile.getAbsolutePath());

        singlePool.execute(new Runnable() {
            @Override
            public void run() {
                preparePlugin();
            }
        });
    }

    /***
     * 2）asset下的目录拷贝到本地磁盘
     * */
    private void preparePlugin() {
        try {
            //pluginmanager.apk
            InputStream is = mContext.getAssets().open(sPluginManagerName);
            FileUtils.copyInputStreamToFile(is, pluginManagerFile);
            if (pluginManagerFile.exists()) {
                Log.i(TAG, "PluginHelper,  copy ok ... ");
            }

            //zip
            //InputStream zip = mContext.getAssets().open(sPluginZip);
            //FileUtils.copyInputStreamToFile(zip, pluginZipFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("从assets中复制apk出错", e);
        }
    }


}
