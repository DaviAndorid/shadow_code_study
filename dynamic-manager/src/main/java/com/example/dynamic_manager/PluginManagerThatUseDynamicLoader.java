
package com.example.dynamic_manager;

import android.content.ComponentName;
import android.content.Context;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.dynamic_host.FailedException;
import com.example.dynamic_host.PluginManagerImpl;


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


    protected PluginManagerThatUseDynamicLoader(Context context) {
        super(context);
    }

    @Override
    protected void onPluginServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    protected void onPluginServiceDisconnected(ComponentName name) {
    }

    @Deprecated
    public final void loadRunTime(String uuid) throws RemoteException, FailedException {

    }

    @Deprecated
    public final void loadPluginLoader(String uuid) throws RemoteException, FailedException {

    }
}
