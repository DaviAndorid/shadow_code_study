package com.example.dynamic_manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;

import com.example.dynamic_host.FailedException;
import com.example.dynamic_host.NotFoundException;
import com.example.dynamic_host.common.InstalledApk;
import com.example.manager.BasePluginManager;
import com.example.manager.InstalledPlugin;
import com.example.manager.InstalledType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static android.content.Context.BIND_AUTO_CREATE;

abstract public class BaseDynamicPluginManager extends BasePluginManager implements UuidManagerImpl {

    static final String TAG = "daviAndroid";

    public BaseDynamicPluginManager(Context context) {
        super(context);
    }

    /**
     * 防止绑定service重入
     */
    private AtomicBoolean mServiceConnecting = new AtomicBoolean(false);
    /**
     * 等待service绑定完成的计数器
     */
    private AtomicReference<CountDownLatch> mConnectCountDownLatch = new AtomicReference<>();

    /**
     * 启动PluginProcessService
     *
     * @param serviceName 注册在宿主中的《插件进程管理service完整名字》
     */
    public final void bindPluginProcessService(final String serviceName) {
        Log.i(TAG, "BaseDynamicPluginManager, --bindPluginProcessService start--");

        Log.i(TAG, "BaseDynamicPluginManager, bindPluginProcessService, serviceName = " + serviceName);

        if (mServiceConnecting.get()) {
            Log.i(TAG, "BaseDynamicPluginManager, bindPluginProcessService, pps service connecting");
            return;
        }
        mConnectCountDownLatch.set(new CountDownLatch(1));
        mServiceConnecting.set(true);

        /**
         * 地址：https://www.cnblogs.com/dolphin0520/p/3920397.html
         *
         * 1）java.util.concurrent包下一个同步工具类
         * java 1.5中，提供了一些非常有用的辅助类来帮助我们进行并发编程
         *
         * 2）允许一个或多个线程等待直到在其他线程中一组操作执行完成。
         *
         * */
        final CountDownLatch startBindingLatch = new CountDownLatch(1);
        final boolean[] asyncResult = new boolean[1];
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(mHostContext, serviceName));
                boolean binding = mHostContext.bindService(intent, new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Log.i(TAG, "BaseDynamicPluginManager, " + "onServiceConnected connectCountDownLatch:" + mConnectCountDownLatch);

                        mServiceConnecting.set(false);

                        // service connect 后处理逻辑
                        onPluginServiceConnected(name, service);

                        mConnectCountDownLatch.get().countDown();

                        Log.i(TAG, "onServiceConnected countDown:" + mConnectCountDownLatch);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Log.i(TAG, "onServiceDisconnected, name = " + name);

                        mServiceConnecting.set(false);
                        onPluginServiceDisconnected(name);
                    }
                }, BIND_AUTO_CREATE);
                //绑定成功，通知等待的线程
                asyncResult[0] = binding;
                startBindingLatch.countDown();
            }
        });

        try {
            /***
             * 1）等待bindService真正开始
             * 2）调用await()方法的线程会被挂起
             * 3）等待直到count值为0才继续执行
             * */
            startBindingLatch.await(10, TimeUnit.SECONDS);
            if (!asyncResult[0]) {
                throw new IllegalArgumentException("无法绑定PPS:" + serviceName);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Log.i(TAG, "BaseDynamicPluginManager, --bindPluginProcessService end--");
    }

    public final void waitServiceConnected(int timeout, TimeUnit timeUnit) throws TimeoutException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("waitServiceConnected 不能在主线程中调用");
        }
        try {
            long s = System.currentTimeMillis();
            boolean isTimeout = !mConnectCountDownLatch.get().await(timeout, timeUnit);
            if (isTimeout) {
                throw new TimeoutException("连接Service超时 ,等待了：" + (System.currentTimeMillis() - s));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void onPluginServiceConnected(ComponentName name, IBinder service);

    protected abstract void onPluginServiceDisconnected(ComponentName name);

    /**
     * PluginManager对象创建的时候回调
     *
     * @param bundle 当PluginManager有更新时会回调老的PluginManager对象onSaveInstanceState存储数据，bundle不为null说明发生了更新
     *               为null说明是首次创建
     */
    @Deprecated
    public void onCreate(Bundle bundle) {
        Log.i(TAG, "onCreate bundle:" + bundle);
    }

    /**
     * 当PluginManager有更新时会先回调老的PluginManager对象 onSaveInstanceState存储数据
     *
     * @param bundle 要存储的数据
     */
    public void onSaveInstanceState(Bundle bundle) {
    }

    /**
     * 当PluginManager有更新时先会销毁老的PluginManager对象，回调对应的onDestroy
     */
    public void onDestroy() {
    }

    public InstalledApk getPlugin(String uuid, String partKey) throws FailedException, NotFoundException {
        return new InstalledApk("", "", "");
    }

    private InstalledApk getInstalledPL(String uuid, int type) {
        InstalledPlugin.Part part;
        part = getLoaderOrRunTimePart(uuid, type);
        return new InstalledApk(part.pluginFile.getAbsolutePath(),
                part.oDexDir == null ? null : part.oDexDir.getAbsolutePath(),
                part.libraryDir == null ? null : part.libraryDir.getAbsolutePath());
    }

    public InstalledApk getPluginLoader(String uuid){
        return getInstalledPL(uuid, InstalledType.TYPE_PLUGIN_LOADER);
    }

    public InstalledApk getRuntime(String uuid){
        return getInstalledPL(uuid, InstalledType.TYPE_PLUGIN_RUNTIME);
    }
}
