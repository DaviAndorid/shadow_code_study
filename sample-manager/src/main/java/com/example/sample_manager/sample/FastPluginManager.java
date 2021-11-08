
package com.example.sample_manager.sample;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;


import com.example.dynamic_host.FailedException;
import com.example.dynamic_manager.PluginManagerThatUseDynamicLoader;
import com.example.manager.InstalledPlugin;
import com.example.manager.InstalledType;
import com.example.manager.PluginConfig;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/***
 * Manager的功能就是管理插件
 * 1）插件的下载逻辑
 * 2）入口逻辑
 * 3）预加载逻辑等
 * 4）一切还没有进入到Loader之前的所有事情
 * */
public abstract class FastPluginManager extends PluginManagerThatUseDynamicLoader {

    protected static final String TAG = "daviAndroid";

    private ExecutorService mFixedPool = Executors.newFixedThreadPool(4);

    public FastPluginManager(Context context) {
        super(context);
    }


    /***
     * 【odex优化】
     * 0）地址，https://www.infoq.cn/article/teckfpg08pddkakivgxp
     *
     * 1）背景
     * 1-1）Android 低版本（4.X 及以下，SDK < 21）的设备，采用的 Java 运行环境是 Dalvik 虚拟机
     * 1-2）它相比于高版本，最大的问题就是在安装或者升级更新之后，首次冷启动的耗时漫长
     * 1-3）这常常需要花费几十秒甚至几分钟，用户不得不面对一片黑屏，熬过这段时间才能正常使用 APP
     * 1-4）尤其在海外，像东南亚以及拉美等地区，还存有着很大量的低端机。4.X 以下低版本用户虽然比较少
     * 但对于抖音及 TikTok 这样有着亿级规模的用户的 APP，即使占比 10%，数目也有上千万；
     *
     * 2）根本原因
     * 2-1）安装或者升级后首次 MultiDex 花费的时间过于漫长
     *
     * 3）行业解决方案
     * 3-1）抖音， BoostMultiDex
     *
     * 4）起因
     * 4-1）官方早期设计不完善 + 业务量大 = 多dex（如：dex，dex1，dex2等）
     * 4-2）Android 4.4 及以下采用的是 Dalvik 虚拟机
     * 4-3）Dalvik 虚拟机只能执行做过 OPT 优化的 DEX 文件，也就是我们常说的 ODEX 文件。
     * 4-4）APK 在安装的时候，其中的 dex1 会自动做 ODEX 优化，
     * 启动的时候由系统默认直接加载到 APP 的PathClassLoader里面，
     * 因此 dex1 中的类肯定能直接访问，不需要我们操心。
     * 4-5）除它之外的 DEX 文件，也就是classes2.dex、classes3.dex、classes4.dex等 DEX 文件
     * （这里我们统称为 Secondary DEX 文件），这些文件都需要靠我们自己进行 ODEX 优化
     * 并加载到 ClassLoader 里，才能正常使用其中的类。否则在访问这些类的时候，就会抛出ClassNotFound异常从而引起崩溃。
     * PS：因此，Android 官方推出了 MultiDex 方案
     *
     * 5）业界的优化方式
     * ***方案1-异步：
     * 5-1）启动阶段要使用的类尽可能多地打包到主 Dex 里面，尽量多地不依赖 Secondary DEX 来跑业务代码
     * 5-2）异步调用MultiDex.install，而在后续某个时间点需要用到 Secondary DEX 的时候，
     * 如果 MultiDex 还没执行完，就停下来同步等待它完成再继续执行后续的代码
     * 短板：如果启动阶段牵扯了太多业务逻辑，很可能并行执行不了太多代码，就很快又被 install 堵住了。
     * ***方案2-模块懒加载-异步的升级（美团）：
     * 5-3）编译期间就需要对 DEX 按模块进行拆分。
     * 5-4）一级界面的 Activity、Service、Receiver、Provider 涉及到的代码都放到第一个 DEX 中
     * 5-5）二级、三级页面的 Activity 以及非高频界面的代码放到了 Secondary DEX 中
     * 5-6）当后面需要执行某个模块的时候，先判断这个模块的 Class 是否已经加载完成，如果没有完成，就等待 install 完成后再继续执行。
     * 短板：对业务的改造程度相当巨大，而且已经有了一些插件化框架的雏形
     * ***方案3-多线程
     * 5-7）每个 DEX 分别用各自线程做 OPT。
     * 短板：几乎没有优化效果
     * ODEX 本身其实是重度 I/O 类型的操作，对于并发而言，多个线程同时进行 I/O 操作并不能带来明显收益
     * ***方案4-后台进程加载
     * 短板：只是规避了主进程 ANR 的问题，第一次启动的整体等待时间并没有减少
     *
     * 6）抖音， BoostMultiDex
     * 6-1）MultiDex.install操作本身 聚焦
     * 6-2）操作里面有odex优化
     * 6-3） ODEX 优化的时间是否可以避免 ？
     * 6-4）避免 ODEX 优化，又想要 APP 能够正常运行？
     * ！！！！绕过 MultiDex ！！！！！！
     * ！！！！！先这样吧！！！因为目前聚焦的是插件为什么要进行odex优化，原因知道了，所以就不展开了！！！！！！！！！！
     *
     *
     * */
    //在线程中执行
    public InstalledPlugin installPlugin(String zip, String hash, boolean odex)
            throws IOException, JSONException, InterruptedException, ExecutionException {

        //1）zip 转换为 PluginConfig 配置
        final PluginConfig pluginConfig = installPluginFromZip(new File(zip), hash);

        final String uuid = pluginConfig.UUID;
        List<Future> futures = new LinkedList<>();

        //2）框架插件runTime/pluginLoader 的 odex 优化
        if (pluginConfig.runTime != null && pluginConfig.pluginLoader != null) {
            //runTime
            Future odexRuntime = mFixedPool.submit((Callable) () -> {
                oDexPluginLoaderOrRunTime(uuid, InstalledType.TYPE_PLUGIN_RUNTIME, pluginConfig.runTime.file);
                return null;
            });
            futures.add(odexRuntime);

            //pluginLoader
            Future odexLoader = mFixedPool.submit((Callable) () -> {
                oDexPluginLoaderOrRunTime(uuid, InstalledType.TYPE_PLUGIN_LOADER, pluginConfig.pluginLoader.file);
                return null;
            });
            futures.add(odexLoader);
        }

        //3）业务插件的so解压/odex优化等
        for (Map.Entry<String, PluginConfig.PluginFileInfo> plugin : pluginConfig.plugins.entrySet()) {
            final String partKey = plugin.getKey();
            final File apkFile = plugin.getValue().file;

            //业务插件，插件apk的so解压
            Future extractSo = mFixedPool.submit((Callable) () -> {
                //插件apk的so解压
                extractSo(uuid, partKey, apkFile);
                return null;
            });
            futures.add(extractSo);

            //业务插件，odex优化
            if (odex) {
                Future odexPlugin = mFixedPool.submit((Callable) () -> {
                    oDexPlugin(uuid, partKey, apkFile);
                    return null;
                });
                futures.add(odexPlugin);
            }
        }

        //4）任务执行
        for (Future future : futures) {
            /**
             * get（）方法可以：
             * 1）当任务结束后返回一个结果值，
             * 2）如果工作没有结束，则会阻塞当前线程，直到任务执行完毕
             * */
            future.get();
        }

        //5）执行完毕，将插件信息持久化到数据库(如：soDir/oDexDir等)
        onInstallCompleted(pluginConfig);

        //6）获取已安装的插件，最后安装的排在返回List的最前面
        return getInstalledPlugins(1).get(0);
    }


    /***
     * 启动插件的activity
     * 1）"com.tencent.shadow.sample.plugin.runtime.PluginDefaultProxyActivity"      【run-time模块】
     * */
    public void startPluginActivity(InstalledPlugin installedPlugin, String partKey, Intent pluginIntent) {
        //1）intent 的包装
        Intent intent = convertActivityIntent(installedPlugin, partKey, pluginIntent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //2）启动
        mPluginLoader.startActivityInPluginProcess(intent);
    }

    public Intent convertActivityIntent(InstalledPlugin installedPlugin, String partKey, Intent pluginIntent) {

        //1）加载框架插件（如：loader/runtime）和业务插件
        loadPlugin(installedPlugin.UUID, partKey);

        //2）包装插件的intent等
        return mPluginLoader.convertActivityIntent(pluginIntent);
    }

    private void loadPluginLoaderAndRuntime(String uuid, String partKey) {
        /***
         * sample-runtime-release.apk
         * */
        loadRunTime(uuid);
        /**
         *sample-loader-release.apk
         * */
        loadPluginLoader(uuid);
    }

    private void loadPlugin(String uuid, String partKey) {
        //1）加载 loader 和 runtime
        loadPluginLoaderAndRuntime(uuid, partKey);

        //2）通过loader，加载插件；例子：partKey = sample-plugin-app
        mPluginLoader.loadPlugin(partKey);
    }


    protected abstract String getPluginProcessServiceName(String partKey);

}
