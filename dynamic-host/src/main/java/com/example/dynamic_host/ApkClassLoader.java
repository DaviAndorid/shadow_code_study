

package com.example.dynamic_host;

import android.os.Build;
import android.util.Log;

import com.example.dynamic_host.common.InstalledApk;

import dalvik.system.DexClassLoader;

/**
 * Apk插件加载专用ClassLoader
 * 1）将宿主apk和插件apk隔离。
 * 2）但例外的是，插件可以从宿主apk中加载到约定的接口。
 * 3）这样隔离的目的是：让宿主apk中的类可以通过约定的接口使用《插件apk中的实现》。而插件中的类不会使用到和宿主同名的类。
 * <p>
 * 配套博客：
 * https://juejin.cn/post/6999514042968752135#heading-17
 */

class ApkClassLoader extends DexClassLoader {

    static final String TAG = "daviAndroid";
    private ClassLoader mGrandParent;
    private final String[] mInterfacePackageNames;

    ApkClassLoader(InstalledApk installedApk,
                   ClassLoader parent,////parent  =  宿主ClassLoader
                   String[] mInterfacePackageNames,
                   int grandTimes) {
        super(installedApk.apkFilePath, installedApk.oDexPath, installedApk.libraryPath, parent);

        Log.i(TAG, "ApkClassLoader，installedApk.apkFilePath = " + installedApk.apkFilePath);
        Log.i(TAG, "ApkClassLoader，installedApk.oDexPath = " + installedApk.oDexPath);
        Log.i(TAG, "ApkClassLoader，installedApk.libraryPath = " + installedApk.libraryPath);

        //默认代
        ClassLoader grand = parent;//parent  =  宿主ClassLoader

        //外面定第几代
        for (int i = 0; i < grandTimes; i++) {
            grand = grand.getParent();
        }
        mGrandParent = grand;

        this.mInterfacePackageNames = mInterfacePackageNames;
    }

    /***
     * findClass与loadClass的区别：
     * 1）findClass
     *      - 类加载逻辑
     *      - JDK1.2后提出的，目的是为了保证加载的类符合双亲委派模型
     *      - 根据名称或者位置加载Class字节码，然后使用defineClass 通常由子类去实现
     * 2）loadClass
     *      - 如果父类加载器加载失败，则会调用自定义的findClass方法
     * */
    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        String packageName;
        int dot = className.lastIndexOf('.');
        if (dot != -1) {
            packageName = className.substring(0, dot);
        } else {
            packageName = "";
        }
        boolean isInterface = false;
        for (String interfacePackageName : mInterfacePackageNames) {
            if (packageName.equals(interfacePackageName)) {
                isInterface = true;
                break;
            }
        }
        //apkFilePath = /data/user/0/com.tencent.shadow.sample.host/files/pluginmanager.apk
        //oDexPath = /data/user/0/com.tencent.shadow.sample.host/files/ManagerImplLoader/ksi9pl9k
        if (isInterface) {
            //情况1：插件可以加载宿主的类实现：
            return super.loadClass(className, resolve);
        } else {
            //情况2：插件不需要加载宿主的类实现
            Class<?> clazz = findLoadedClass(className);//1）系统里面找
            if (clazz == null) {
                ClassNotFoundException suppressed = null;
                try {
                    //否则先从自己的dexPath中查找
                    clazz = findClass(className);//2）自己的dexPath中查找
                } catch (ClassNotFoundException e) {
                    suppressed = e;
                }
                if (clazz == null) {
                    //如果找不到,则再从parent的parent ClassLoader中查找。
                    //BootClassLoader
                    try {
                        clazz = mGrandParent.loadClass(className);//父亲找
                    } catch (ClassNotFoundException e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            e.addSuppressed(suppressed);
                        }
                        throw e;
                    }
                }
            }
            return clazz;
        }
    }


    /**
     * 从apk中读取接口的实现
     *
     * @param clazz     接口类
     * @param className 实现类的类名
     * @param <T>       接口类型
     * @return 所需接口
     * @throws Exception
     */
    <T> T getInterface(Class<T> clazz, String className) throws Exception {
        try {
            Class<?> interfaceImplementClass = loadClass(className);
            Object interfaceImplement = interfaceImplementClass.newInstance();
            return clazz.cast(interfaceImplement);
        } catch (ClassNotFoundException | InstantiationException
                | ClassCastException | IllegalAccessException e) {
            throw new Exception(e);
        }
    }

}

