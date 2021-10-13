
package com.example.dynamic_host;

import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;

import java.util.Map;

/**
 * 取出跨进程的封装，直接定义为调用
 */
public interface PluginLoaderImpl {//extends IBinder {
    //void setUuidManager(UuidManager uuidManager);

    void loadPlugin(String partKey);

    Map getLoadedPlugin();

    void callApplicationOnCreate(String partKey);

    Intent convertActivityIntent(Intent pluginActivityIntent);

    ComponentName startPluginService(Intent pluginServiceIntent);

    boolean stopPluginService(Intent pluginServiceIntent);

    void startActivityInPluginProcess(Intent intent);
}
