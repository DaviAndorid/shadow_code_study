
package com.example.dynamic_manager;

import static com.example.dynamic_host.FailedException.ERROR_CODE_FILE_NOT_FOUND_EXCEPTION;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.dynamic_host.DynamicRuntime;
import com.example.dynamic_host.FailedException;
import com.example.dynamic_host.LoaderImplLoader;
import com.example.dynamic_host.PluginLoaderImpl;
import com.example.dynamic_host.PluginManagerImpl;
import com.example.dynamic_host.common.InstalledApk;
import com.example.dynamic_loader.PluginLoader;

import java.io.File;


/**
 * <p>
 * <p>
 * 高能！！！！
 * 如果是宿主的intent直接返回，如果是插件的需要包装下～
 * <p>
 * 插件加载服务端接口6
 * <p>
 * 具体实现在：PluginLoaderBinder  ，      【dynamic-loader-impl】
 * 更深的实现：DynamicPluginLoader ，      【dynamic-loader-impl】
 * 再深一层：ComponentManager，            【loader】
 */
public abstract class PluginManagerThatUseDynamicLoader
        extends BaseDynamicPluginManager implements PluginManagerImpl {


    protected PluginLoaderImpl mPluginLoader;

    protected PluginManagerThatUseDynamicLoader(Context context) {
        super(context);
    }

    @Override
    protected void onPluginServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    protected void onPluginServiceDisconnected(ComponentName name) {
    }

    /***
     * 1）加载runtime apk
     * 2）知识回顾：http://gityuan.com/2017/03/19/android-classloader/
     * 3）
     * */
    public final void loadRunTime(String uuid) {
        InstalledApk installedApk;
        // 原著那边是通过服务方式实现，这里为了简化所以选择直接调用
        installedApk = getRuntime(uuid);
        InstalledApk installedRuntimeApk = new InstalledApk(installedApk.apkFilePath, installedApk.oDexPath, installedApk.libraryPath);
        boolean loaded = DynamicRuntime.loadRuntime(installedRuntimeApk);
        if (loaded) {
            DynamicRuntime.saveLastRuntimeInfo(mHostContext, installedRuntimeApk);
        }
    }

    /**
     *
     */
    public final void loadPluginLoader(String uuid) {
        InstalledApk installedApk;
        installedApk = getPluginLoader(uuid);
        File file = new File(installedApk.apkFilePath);
        if (!file.exists()) {
            Log.e(TAG, file.getAbsolutePath() + ", 文件不存在");
        }
        LoaderImplLoader implLoader = new LoaderImplLoader();
        mPluginLoader = implLoader.load(installedApk, uuid, mHostContext);
    }


}
