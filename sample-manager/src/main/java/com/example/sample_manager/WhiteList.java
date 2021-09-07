
package com.example.sample_manager;

/**
 * 1）此类包名及类名固定
 * 2）classLoader的白名单
 * 3）PluginManager 可以加载《宿主中》位于白名单内的类
 */

public interface WhiteList {
    String[] sWhiteList = new String[]
            {
                   // "com.tencent.host.shadow",
                   // "com.tencent.shadow.test.lib.constant",
            };
}
