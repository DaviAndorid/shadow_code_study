
package com.example.demohot.manager;

import com.example.dynamic_host.PluginManagerUpdater;

import java.io.File;
import java.util.concurrent.Future;

/**
 * PluginManager文件升级器
 * <p>
 * 注意这个类不负责什么时候该升级PluginManager，
 * 它只提供需要升级时的功能，如下载和向远端查询文件是否还可用。
 * <p>
 * todo：这里没有实现，需要自己实现自己上层业务的插件apk升级功能，如：查询当前版本/更新逻辑等
 */
public class FixedPathPmUpdater implements PluginManagerUpdater {

    final private File apk;

    FixedPathPmUpdater(File apk) {
        this.apk = apk;
    }

    @Override
    public boolean wasUpdating() {
        return false;
    }

    @Override
    public Future<File> update() {
        return null;
    }

    @Override
    public File getLatest() {
        return apk;
    }

    @Override
    public Future<Boolean> isAvailable(final File file) {
        return null;
    }
}