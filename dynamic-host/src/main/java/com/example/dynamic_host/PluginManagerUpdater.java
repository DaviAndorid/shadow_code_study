
package com.example.dynamic_host;

import java.io.File;
import java.util.concurrent.Future;

/**
 * PluginManager文件升级器
 */
public interface PluginManagerUpdater {
    /**
     * @return <code>true</code>表示之前更新过程中意外中断了
     */
    boolean wasUpdating();

    /**
     * 更新
     *
     * @return 当前最新的PluginManager，可能是之前已经返回过的文件，但它是最新的了。
     */
    Future<File> update();

    /**
     * 获取本地最新可用的
     *
     * @return <code>null</code>表示本地没有可用的
     */
    File getLatest();

    /**
     * 查询是否可用
     *
     * @param file PluginManagerUpdater返回的file
     * @return <code>true</code>表示可用，<code>false</code>表示不可用
     */
    Future<Boolean> isAvailable(File file);
}
