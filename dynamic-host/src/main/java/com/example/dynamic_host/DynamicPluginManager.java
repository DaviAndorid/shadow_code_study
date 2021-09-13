
package com.example.dynamic_host;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import static com.example.dynamic_host.utils.Md5.md5File;


public final class DynamicPluginManager implements PluginManager {

    static final String TAG = "daviAndroid";

    final private PluginManagerUpdater mUpdater;

    private PluginManagerImpl mManagerImpl;

    private String mCurrentImplMd5;

    public DynamicPluginManager(PluginManagerUpdater updater) {
        if (updater.getLatest() == null) {
            throw new IllegalArgumentException("构造DynamicPluginManager时传入的PluginManagerUpdater" +
                    "必须已经已有本地文件，即getLatest()!=null");
        }
        mUpdater = updater;
    }

    @Override
    public void enter(Context context, long fromId, Bundle bundle, EnterCallback callback) {
        Log.i(TAG, "enter fromId:" + fromId + " callback:" + callback);

        //1)根据mUpdater，确认文件是否更新，进一步确认 mManagerImpl 是否重新构建
        //2)load plumanager apk
        updateManagerImpl(context);

        //入口进入
        mManagerImpl.enter(context, fromId, bundle, callback);

        mUpdater.update();
    }

    public void release() {
        if (mManagerImpl != null) {
            mManagerImpl.onDestroy();
            mManagerImpl = null;
        }
    }

    private void updateManagerImpl(Context context) {
        File latestManagerImplApk = mUpdater.getLatest();
        String md5 = md5File(latestManagerImplApk);

        Log.i(TAG, "DynamicPluginManager, updateManagerImpl，" +
                "TextUtils.equals(mCurrentImplMd5, md5) : " + (TextUtils.equals(mCurrentImplMd5, md5)));

        if (!TextUtils.equals(mCurrentImplMd5, md5)) {
            //文件更新了
            ManagerImplLoader implLoader = new ManagerImplLoader(context, latestManagerImplApk);
            PluginManagerImpl newImpl = implLoader.load();
            Bundle state;
            if (mManagerImpl != null) {
                state = new Bundle();
                mManagerImpl.onSaveInstanceState(state);
                mManagerImpl.onDestroy();
            } else {
                state = null;
            }
            newImpl.onCreate(state);
            mManagerImpl = newImpl;
            mCurrentImplMd5 = md5;
        }
    }

    public PluginManager getManagerImpl() {
        return mManagerImpl;
    }

}
