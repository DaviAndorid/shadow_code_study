
package com.example.dynamic_host;

import android.content.Context;

import com.example.dynamic_host.common.InstalledApk;


public final class LoaderImplLoader extends ImplLoader {
    protected static final String TAG = "daviAndroid";

    /**
     * 加载{@link #sLoaderFactoryImplClassName}时
     * 需要从宿主PathClassLoader（含双亲委派）中加载的类
     */
    private static final String[] sInterfaces = new String[]{
            //当runtime是动态加载的时候，runtime的ClassLoader是PathClassLoader的parent，
            // 所以不需要写在这个白名单里。但是写在这里不影响，也可以兼容runtime打包在宿主的情况。
    };

    // todo plugin-debug.zip/sample-loader-debug.apk
    private final static String sLoaderFactoryImplClassName
            = "com.tencent.shadow.dynamic.loader.impl.LoaderFactoryImpl";

    /***
     * 1）具体实现：PluginLoaderBinder
     * 2）installedApk  ： plugin-debug.zip/sample-loader-debug.apk
     * */
    public PluginLoaderImpl load(InstalledApk installedApk, String uuid, Context appContext) {
        ApkClassLoader pluginLoaderClassLoader = new ApkClassLoader(
                installedApk,
                LoaderImplLoader.class.getClassLoader(),
                loadWhiteList(installedApk),
                1
        );

        //从apk中，读取接口的实现
        //plugin-debug.zip/sample-loader-debug.apk
        LoaderFactory loaderFactory = null;
        try {
            loaderFactory = pluginLoaderClassLoader.getInterface(
                    LoaderFactory.class,
                    sLoaderFactoryImplClassName
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (loaderFactory == null) {
            return null;
        }

        //buildLoader
        return loaderFactory.buildLoader(uuid, appContext);
    }

    @Override
    String[] getCustomWhiteList() {
        return sInterfaces;
    }
}
