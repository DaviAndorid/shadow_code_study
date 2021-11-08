
package com.example.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BasePluginManager {
    protected static final String TAG = "daviAndroid";

    /*
     * 宿主的context对象
     */
    public Context mHostContext;

    /**
     * UI线程的handler
     */
    protected Handler mUiHandler = new Handler(Looper.getMainLooper());

    /**
     * 从压缩包中将插件解压出来，解析成InstalledPlugin
     */
    private UnpackManager mUnpackManager;


    public BasePluginManager(Context context) {
        this.mHostContext = context.getApplicationContext();
        this.mUnpackManager = new UnpackManager(mHostContext.getFilesDir(), getName());
        this.mInstalledDao = new InstalledDao(new InstalledPluginDBHelper(mHostContext, getName()));
    }

    /**
     * PluginManager的名字
     * 用于和其他PluginManager区分持续化存储的名字
     */
    abstract protected String getName();

    /**
     * 从文件夹中解压插件
     *
     * @param dir 文件夹路径
     * @return PluginConfig
     */
    public final PluginConfig installPluginFromDir(File dir) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * 从压缩包中解压插件
     *
     * @param zip  压缩包路径
     * @param hash 压缩包hash
     * @return PluginConfig
     */
    public final PluginConfig installPluginFromZip(File zip, String hash) throws IOException, JSONException {
        Log.i(TAG, "BasePluginManager, installPluginFromZip，从压缩包中解压插件");
        return mUnpackManager.unpackPlugin(hash, zip);
    }

    /**
     * 安装完成时调用
     * <p>
     * 将插件信息持久化到数据库
     *
     * @param pluginConfig 插件配置信息
     */
    public final void onInstallCompleted(PluginConfig pluginConfig) {
        File root = mUnpackManager.getAppDir();
        String soDir = AppCacheFolderManager.getLibDir(root, pluginConfig.UUID).getAbsolutePath();
        String oDexDir = AppCacheFolderManager.getODexDir(root, pluginConfig.UUID).getAbsolutePath();

        mInstalledDao.insert(pluginConfig, soDir, oDexDir);
    }
    /**
     * 插件信息查询数据库接口
     */
    private InstalledDao mInstalledDao;



    protected InstalledPlugin.Part getPluginPartByPartKey(String uuid, String partKey) {
        return new InstalledPlugin.Part(0, new File(""), new File(""), new File(""));
    }

    protected InstalledPlugin getInstalledPlugin(String uuid) {
        return new InstalledPlugin();
    }

    protected InstalledPlugin.Part getLoaderOrRunTimePart(String uuid, int type) {
        return new InstalledPlugin.Part(0, new File(""), new File(""), new File(""));
    }

    /**
     * odex优化
     *
     * @param uuid    插件包的uuid
     * @param partKey 要oDex的插件partkey
     */
    public final void oDexPlugin(String uuid, String partKey, File apkFile) throws InstallPluginException {
        try {
            File root = mUnpackManager.getAppDir();
            File oDexDir = AppCacheFolderManager.getODexDir(root, uuid);
            ODexBloc.oDexPlugin(apkFile, oDexDir, AppCacheFolderManager.getODexCopiedFile(oDexDir, partKey));
        } catch (InstallPluginException e) {
            throw e;
        }
    }


    /**
     * 【odex优化】
     * 地址：https://skytoby.github.io/2019/Android%20dex%EF%BC%8Codex%EF%BC%8Coat%EF%BC%8Cvdex%EF%BC%8Cart%E6%96%87%E4%BB%B6%E7%BB%93%E6%9E%84/
     * <p>
     * （1）《dex》
     * - Dex是Android平台上(Dalvik虚拟机)的可执行文件, 相当于Windows平台中的exe文件
     * - java程序编译成class后，使用dx工具将所有的class文件整合到一个dex文件
     * - 目的是其中各个类能够共享数据，在一定程度上降低了冗余，同时也是文件结构更加经凑，dex文件是传统jar文件大小的50%左右。
     * （2）《odex》（ 5.0之前）
     * - 全名Optimized DEX，即优化过的DEX。
     * - Apk在安装(installer)时，就会进行验证和优化
     * - 目的是为了校验代码合法性及优化代码执行速度，验证和优化后，会产生ODEX文件
     * - 运行Apk的时候，直接加载ODEX，避免重复验证和优化，加快了Apk的响应时间。
     * - 优化过程会根据不同设备上Dalvik虚拟机的版本、Framework库的不同等因素而不同
     * - 在一台设备上被优化过的ODEX文件，拷贝到另一台设备上不一定能够运行。
     * （3）《oat》（5.0及5.0之后）
     * - oat 文件是 ART 运行的文件，是一种ELF格式的二进制可运行文件
     * - 包含 DEX 文件和编译出的本地机器指令文件
     * - 因为 oat 文件包含 DEX 文件，因此比 ODEX 文件占用空间更大。
     * - 在安装时，classes.dex文件会被工具dex2oat翻译成本地机器指令
     * 官方解释：https://source.android.com/devices/tech/dalvik/configure
     *
     * @param uuid    插件包的uuid
     * @param type    要oDex的插件类型 @class IntalledType  loader or runtime
     * @param apkFile 插件apk文件
     */
    public final void oDexPluginLoaderOrRunTime(String uuid, int type, File apkFile) throws InstallPluginException {
        try {
            File root = mUnpackManager.getAppDir();
            File oDexDir = AppCacheFolderManager.getODexDir(root, uuid);
            String key = type == InstalledType.TYPE_PLUGIN_LOADER ? "loader" : "runtime";
            ODexBloc.oDexPlugin(apkFile, oDexDir, AppCacheFolderManager.getODexCopiedFile(oDexDir, key));
        } catch (InstallPluginException e) {
            throw e;
        }
    }


    /**
     * 插件apk的so解压
     *
     * @param uuid    插件包的uuid
     * @param partKey 要解压so的插件partkey
     * @param apkFile 插件apk文件
     */
    public final void extractSo(String uuid, String partKey, File apkFile) throws InstallPluginException {
        try {
            File root = mUnpackManager.getAppDir();
            String filter = "lib/" + getAbi() + "/";
            File soDir = AppCacheFolderManager.getLibDir(root, uuid);
            CopySoBloc.copySo(apkFile, soDir
                    , AppCacheFolderManager.getLibCopiedFile(soDir, partKey), filter);
        } catch (InstallPluginException e) {
            throw e;
        }
    }

    /**
     * 插件apk的so解压
     *
     * @param uuid    插件包的uuid
     * @param type    要oDex的插件类型 @class IntalledType  loader or runtime
     * @param apkFile 插件apk文件
     */
    public final void extractLoaderOrRunTimeSo(String uuid, int type, File apkFile) {

    }


    /**
     * 获取已安装的插件，最后安装的排在返回List的最前面
     *
     * @param limit 最多获取个数
     */
    public final List<InstalledPlugin> getInstalledPlugins(int limit) {
        return mInstalledDao.getLastPlugins(limit);
    }


    /**
     * 删除指定uuid的插件
     *
     * @param uuid 插件包的uuid
     * @return 是否全部执行成功
     */
    public boolean deleteInstalledPlugin(String uuid) {
        return true;
    }

    private boolean deletePart(InstalledPlugin.Part part) {
        boolean suc = true;
        if (!part.pluginFile.delete()) {
            suc = false;
        }
        if (part.oDexDir != null) {
            if (!part.oDexDir.delete()) {
                suc = false;
            }
        }
        if (part.libraryDir != null) {
            if (!part.libraryDir.delete()) {
                suc = false;
            }
        }
        return suc;
    }


    /**
     * 业务插件的abi
     *
     * @return
     */
    public String getAbi() {
        return null;
    }
}
