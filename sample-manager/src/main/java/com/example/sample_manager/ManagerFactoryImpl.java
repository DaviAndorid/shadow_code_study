
package com.example.sample_manager;

import android.content.Context;

import com.example.dynamic_host.ManagerFactory;
import com.example.dynamic_host.PluginManagerImpl;
import com.example.sample_manager.sample.SamplePluginManager;


/**
 * 此类包名及类名固定
 */
public final class ManagerFactoryImpl implements ManagerFactory {
    @Override
    public PluginManagerImpl buildManager(Context context) {
        /***
         * Manager的功能就是管理插件
         * 1）插件的下载逻辑
         * 2）入口逻辑
         * 3） 预加载逻辑等
         * 4）一切还没有进入到Loader之前的所有事情
         * */
        return new SamplePluginManager(context);
    }
}
