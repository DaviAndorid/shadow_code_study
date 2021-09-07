
package com.example.demohot.manager;

import android.util.Log;


import com.example.dynamic_host.DynamicPluginManager;
import com.example.dynamic_host.PluginManager;

import java.io.File;

public class Shadow {

    static final String TAG = "daviAndroid";

    public static PluginManager getPluginManager(File apk) {
        Log.i(TAG, "Shadow, getPluginManager, apk = " + apk.getAbsolutePath());

        //它只提供需要升级时的功能，如下载和向远端查询文件是否还可用。
        final FixedPathPmUpdater fixedPathPmUpdater = new FixedPathPmUpdater(apk);
        File tempPm = fixedPathPmUpdater.getLatest();

        if (tempPm != null) {
            return new DynamicPluginManager(fixedPathPmUpdater);
        }
        return null;
    }

}
