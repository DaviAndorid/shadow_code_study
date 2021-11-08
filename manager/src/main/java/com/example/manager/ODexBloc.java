
package com.example.manager;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

public class ODexBloc {

    protected static final String TAG = "daviAndroid";

    private static ConcurrentHashMap<String, Object> sLocks = new ConcurrentHashMap<>();

    public static void oDexPlugin(File apkFile, File oDexDir, File copiedTagFile) throws InstallPluginException {
        Log.i(TAG, "ODexBloc, oDexPlugin, " + "odex优化 --start--");
        /**
         * 流程：
         * 1-1）如果odex目录存在但是个文件，不是目录，那超出预料了。删除了也不一定能工作正常。直接抛异常
         * 1-2）创建oDex目录
         * 1-3）
         * */
        String key = apkFile.getAbsolutePath();
        //key：sample-loader-debug.apk / sample-runtime-debug.apk 等
        //value：Object
        Object lock = sLocks.get(key);
        if (lock == null) {
            lock = new Object();
            sLocks.put(key, lock);
        }

        synchronized (lock) {
            if (copiedTagFile.exists()) {
                return;
            }

            //如果odex目录存在但是个文件，不是目录，那超出预料了。删除了也不一定能工作正常。
            if (oDexDir.exists() && oDexDir.isFile()) {
                throw new InstallPluginException("oDexDir=" + oDexDir.getAbsolutePath() + "已存在，但它是个文件，不敢贸然删除");
            }

            //创建oDex目录
            oDexDir.mkdirs();

            /***
             * dexPath：
             * 1）dex、jar、apk文件的路径
             *
             * optimizedDirectory：
             * 1）dex文件首次加载时，会进行 dexopt 操作
             * 2）optimizedDirectory即为优化后的odex文件的存放目录，不允许为空
             * 3）官方推荐使用应用私有目录来缓存优化后的dex文件
             *
             * parent：
             * 1）当前类加载器的父加载器
             *
             * libraryPath：
             * 1）动态库的路径，可以为空
             *
             * 原理分析：https://shuwoom.com/?p=269
             * */
            new DexClassLoader(
                    apkFile.getAbsolutePath(),//dexPath
                    oDexDir.getAbsolutePath(),//optimizedDirectory
                    null,//librarySearchPath
                    ODexBloc.class.getClassLoader());//ClassLoader parent

            //执行成功，就创建tag标志文件
            try {
                copiedTagFile.createNewFile();
            } catch (IOException e) {
                throw new InstallPluginException("oDexPlugin完毕 创建tag文件失败：" + copiedTagFile.getAbsolutePath(), e);
            }
        }

        Log.i(TAG, "ODexBloc, oDexPlugin, " + "odex优化 --end--");
    }

}


