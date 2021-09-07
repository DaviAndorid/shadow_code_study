
package com.example.dynamic_host;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;

import static android.content.pm.PackageManager.GET_META_DATA;

/**
 * 1）修改Context的apk路径的Wrapper。
 * 2）可将原Context的《Resource》和《ClassLoader》重新修改为新的Apk。
 */
class ChangeApkContextWrapper extends ContextWrapper {

    static final String TAG = "daviAndroid";

    private Resources mResources;

    private LayoutInflater mLayoutInflater;

    final private ClassLoader mClassloader;

    @Deprecated
    ChangeApkContextWrapper(Context base, String apkPath, ClassLoader mClassloader) {
        super(base);
        this.mClassloader = mClassloader;
        mResources = createResources(apkPath, base);
    }

    /**
     * 教程：https://sharrychoo.github.io/blog/android-source/resources-manager
     * 方案：https://github.com/13767004362/HookDemo/blob/master/document/Android%E6%8F%92%E4%BB%B6%E5%8C%96%E4%B9%8B%E5%8A%A0%E8%BD%BDResource%E8%B5%84%E6%BA%90.md
     * <p>
     * 独立式的Resource方案：
     * - 创建插件新的Resource，与宿主隔离
     * - 优点：资源不存在冲突，不需要特殊处理。
     * - 缺点：存在插件、宿主之间资源信息共享问题。
     * - getResourcesForApplication
     * - 入口：https://blog.csdn.net/maplejaw_/article/details/51530442
     * -
     */
    //其实也可以用另外一种方式：AssetManager 直接指定目录，如：addPath模式【这样做是不希望hook吧】
    //AssetManager比getResourcesForApplication要灵活很多，使用场景也更广。
    private Resources createResources(String apkPath, Context base) {
        Log.i(TAG, "ChangeApkContextWrapper, ---createResources start----");

        PackageManager packageManager = base.getPackageManager();
        //获取安装包信息
        //但是，这些包都是没有在PMS中注册的
        PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(apkPath, GET_META_DATA);
        packageArchiveInfo.applicationInfo.publicSourceDir = apkPath;
        packageArchiveInfo.applicationInfo.sourceDir = apkPath;
        Log.i(TAG, "ChangeApkContextWrapper, createResources, applicationInfo.publicSourceDir = " + apkPath);
        Log.i(TAG, "ChangeApkContextWrapper, createResources, applicationInfo.sourceDir = " + apkPath);
        //
        try {
            Log.i(TAG, "ChangeApkContextWrapper, ---createResources end----");
            return packageManager.getResourcesForApplication(packageArchiveInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            //这些包都是没有在PMS中注册的。如果仍然这样获取，会提示如下错误。
            throw new RuntimeException(e);
        }
    }

    @Override
    public AssetManager getAssets() {
        return mResources.getAssets();
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

    @Override
    public Resources.Theme getTheme() {
        return mResources.newTheme();
    }

    @Override
    public Object getSystemService(String name) {
        if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mLayoutInflater == null) {
                LayoutInflater layoutInflater = (LayoutInflater) super.getSystemService(name);
                mLayoutInflater = layoutInflater.cloneInContext(this);
            }
            return mLayoutInflater;
        }
        return super.getSystemService(name);
    }

    @Override
    public ClassLoader getClassLoader() {
        return mClassloader;
    }
}


