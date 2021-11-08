
package com.example.dynamic_host;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;


import com.example.dynamic_host.common.InstalledApk;

import java.io.File;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;

/**
 * 方案：通过修改loader的继承关系，来实现加载，而不是我们说的自定义dex loader模式！！！
 * <p>
 * 将runtime apk加载到DexPathClassLoader，形成如下结构的classLoader树结构
 * ---BootClassLoader（Android 系统启动时会使用 BootClassLoader 来预加载常用类）
 * ----RuntimeClassLoader（！！！！）
 * ------PathClassLoader（加载系统类和应用程序的类）
 */
public class DynamicRuntime {

    private static final String SP_NAME = "ShadowRuntimeLoader";

    protected static final String TAG = "daviAndroid";

    private static final String KEY_RUNTIME_APK = "KEY_RUNTIME_APK";
    private static final String KEY_RUNTIME_ODEX = "KEY_RUNTIME_ODEX";
    private static final String KEY_RUNTIME_LIB = "KEY_RUNTIME_LIB";

    /**
     * 加载runtime apk
     *
     * @return true 加载了新的runtime
     */
    public static boolean loadRuntime(InstalledApk installedRuntimeApk) {
        //宿主的 ClassLoader
        ClassLoader contextClassLoader = DynamicRuntime.class.getClassLoader();

        //ClassLoader 的继承关系被改过，具体见： hackParentToRuntime(installedRuntimeApk, contextClassLoader);
        RuntimeClassLoader runtimeClassLoader = getRuntimeClassLoader();
        if (runtimeClassLoader != null) {
            String apkPath = runtimeClassLoader.apkPath;
            Log.i(TAG, "DynamicRuntime, last apkPath:" + apkPath + " new apkPath:" + installedRuntimeApk.apkFilePath);

            if (TextUtils.equals(apkPath, installedRuntimeApk.apkFilePath)) {
                //已经加载相同版本的runtime了,不需要加载
                Log.i(TAG, "DynamicRuntime, 已经加载相同apkPath的runtime了,不需要加载");
                return false;
            } else {
                //版本不一样，说明要更新runtime，先恢复正常的classLoader结构
                Log.i(TAG, "DynamicRuntime, 版本不一样，说明要更新runtime，先恢复正常的classLoader结构");
                try {
                    recoveryClassLoader();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }


        //正常处理，将runtime 挂到 pathclassLoader 之上
        /**
         * todo : 没有明白，修改loader继承链的作用
         * 将runtime apk加载到DexPathClassLoader，形成如下结构的classLoader树结构
         * ---BootClassLoader（Android 系统启动时会使用 BootClassLoader 来预加载常用类）
         * ----RuntimeClassLoader（！！！！）
         * ------PathClassLoader（加载系统类和应用程序的类）
         */
        try {
            Log.i(TAG, "DynamicRuntime, 正常处理，将runtime 挂到 pathclassLoader 之上");
            hackParentToRuntime(installedRuntimeApk, contextClassLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * todo : 没有明白，修改loader继承链的作用
     * 将runtime apk加载到DexPathClassLoader，形成如下结构的classLoader树结构
     * ---BootClassLoader（Android 系统启动时会使用 BootClassLoader 来预加载常用类）
     * ----RuntimeClassLoader（！！！！）
     * ------PathClassLoader（加载系统类和应用程序的类）
     */
    @Deprecated
    private static void recoveryClassLoader() throws Exception {
        Log.i(TAG, "DynamicRuntime, recoveryClassLoader");

        ClassLoader contextClassLoader = DynamicRuntime.class.getClassLoader();
        ClassLoader child = contextClassLoader;
        ClassLoader tmpClassLoader = contextClassLoader.getParent();//DynamicRuntime loader 的父loader
        while (tmpClassLoader != null) {
            if (tmpClassLoader instanceof RuntimeClassLoader) {  //找到，RuntimeClassLoader
                hackParentClassLoader(
                        child, // 需要修改的ClassLoader
                        tmpClassLoader.getParent()//classLoader的新的parent
                );
                return;
            }
            child = tmpClassLoader;
            tmpClassLoader = tmpClassLoader.getParent();
        }
    }

    private static RuntimeClassLoader getRuntimeClassLoader() {
        //宿主的 ClassLoader
        ClassLoader contextClassLoader = DynamicRuntime.class.getClassLoader();
        //
        ClassLoader tmpClassLoader = contextClassLoader.getParent();
        while (tmpClassLoader != null) {
            if (tmpClassLoader instanceof RuntimeClassLoader) {
                return (RuntimeClassLoader) tmpClassLoader;
            }
            tmpClassLoader = tmpClassLoader.getParent();
        }
        return null;
    }


    /**
     * 正常处理，将runtime 挂到 pathclassLoader 之上
     */
    private static void hackParentToRuntime(
            InstalledApk installedRuntimeApk,
            ClassLoader contextClassLoader// contextClassLoader = DynamicRuntime.class.getClassLoader();
    ) throws Exception {

        /***
         * 地址：https://juejin.cn/post/6844903929562529800
         *
         * 《继承关系》
         *BootClassLoader
         *  - ClassLoader
         *      - BaseDexClassLoader
         *          - DexClassLoader（可以从SD卡中加载未安装的apk）
         *          - PathClassLoader（只能加载系统中已经安装过的apk）
         *
         *          - RuntimeClassLoader
         *
         *
         * 代码：
         * 1）RuntimeClassLoader 的父loader 设置为以前的 contextClassLoader的父loader
         * 2）contextClassLoader 的父loader 设置为 RuntimeClassLoader
         * 最后关系变为了：
         *  - BootClassLoader（Android 系统启动时会使用 BootClassLoader 来预加载常用类）
         *   - RuntimeClassLoader 加载插件的loader
         *     - PathClassLoader（加载系统类和应用程序的类）
         *
         * 这样子改，基于双亲委托机制
         *  - BootClassLoader先加载
         *  - 加载不到，RuntimeClassLoader加载这样（自定义的！！！）
         *
         * 这种就不需要自定义 DexClassLoader ？？？？？
         *
         * */

        //RuntimeClassLoader 的父loader 设置为以前的 contextClassLoader的父loader
        RuntimeClassLoader runtimeClassLoader = new RuntimeClassLoader(
                installedRuntimeApk.apkFilePath,
                installedRuntimeApk.oDexPath,
                installedRuntimeApk.libraryPath,
                contextClassLoader.getParent());

        //contextClassLoader 的父loader 设置为 RuntimeClassLoader
        hackParentClassLoader(
                contextClassLoader,  //contextClassLoader = DynamicRuntime.class.getClassLoader();
                runtimeClassLoader);
    }


    /**
     * 修改ClassLoader的parent
     *
     * @param classLoader          需要修改的ClassLoader
     * @param newParentClassLoader classLoader的新的parent
     * @throws Exception 失败时抛出
     */
    static void hackParentClassLoader(ClassLoader classLoader, ClassLoader newParentClassLoader) throws Exception {
        //ClassLoader类的parent域
        Field field = getParentField();
        if (field == null) {
            throw new RuntimeException("在ClassLoader.class中没找到类型为ClassLoader的parent域");
        }
        field.setAccessible(true);
        field.set(classLoader, newParentClassLoader);
    }

    /**
     * 安全地获取到ClassLoader类的parent域
     *
     * @return ClassLoader类的parent域.或不能通过反射访问该域时返回null.
     */
    private static Field getParentField() {
        ClassLoader classLoader = DynamicRuntime.class.getClassLoader();
        ClassLoader parent = classLoader.getParent();
        Field field = null;
        for (Field f : ClassLoader.class.getDeclaredFields()) {
            try {
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                Object o = f.get(classLoader);
                f.setAccessible(accessible);
                if (o == parent) {
                    field = f;
                    break;
                }
            } catch (IllegalAccessException ignore) {
            }
        }
        return field;
    }

    /**
     * 重新恢复runtime
     *
     * @return true 进行了runtime恢复
     */
    public static boolean recoveryRuntime(Context context) {
        InstalledApk installedApk = getLastRuntimeInfo(context);
        if (installedApk != null && new File(installedApk.apkFilePath).exists()) {
            if (installedApk.oDexPath != null && !new File(installedApk.oDexPath).exists()) {
                return false;
            }
            try {
                hackParentToRuntime(installedApk, DynamicRuntime.class.getClassLoader());
                return true;
            } catch (Exception e) {
                removeLastRuntimeInfo(context);
            }
        }
        return false;
    }

    @SuppressLint("ApplySharedPref")
    public static void saveLastRuntimeInfo(Context context, InstalledApk installedRuntimeApk) {
        SharedPreferences preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_RUNTIME_APK, installedRuntimeApk.apkFilePath)
                .putString(KEY_RUNTIME_ODEX, installedRuntimeApk.oDexPath)
                .putString(KEY_RUNTIME_LIB, installedRuntimeApk.libraryPath)
                .commit();
    }

    private static InstalledApk getLastRuntimeInfo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String apkFilePath = preferences.getString(KEY_RUNTIME_APK, null);
        String oDexPath = preferences.getString(KEY_RUNTIME_ODEX, null);
        String libraryPath = preferences.getString(KEY_RUNTIME_LIB, null);

        if (apkFilePath == null) {
            return null;
        } else {
            return new InstalledApk(apkFilePath, oDexPath, libraryPath);
        }
    }

    @SuppressLint("ApplySharedPref")
    private static void removeLastRuntimeInfo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .remove(KEY_RUNTIME_APK)
                .remove(KEY_RUNTIME_ODEX)
                .remove(KEY_RUNTIME_LIB)
                .commit();
    }


    static class RuntimeClassLoader extends BaseDexClassLoader {
        /**
         * 加载的apk路径
         */
        private String apkPath;


        RuntimeClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
            super(dexPath, optimizedDirectory == null ? null : new File(optimizedDirectory), librarySearchPath, parent);
            this.apkPath = dexPath;
        }
    }
}
