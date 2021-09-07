
package com.example.sample_manager.sample;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.constant.Constant;
import com.example.dynamic_host.EnterCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * Manager的功能就是管理插件
 * 1）插件的下载逻辑
 * 2）入口逻辑
 * 3） 预加载逻辑等
 * 4）一切还没有进入到Loader之前的所有事情
 * */
public class SamplePluginManager extends FastPluginManager {

    static final String TAG = "daviAndroid";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Context mCurrentContext;

    public SamplePluginManager(Context context) {
        super(context);
        mCurrentContext = context;
    }

    /**
     * @return PluginManager实现的别名，用于区分不同PluginManager实现的数据存储路径
     */
    @Override
    protected String getName() {
        return "test-dynamic-manager";
    }

    /**
     * @return 宿主so的ABI。插件必须和宿主使用相同的ABI。
     */
    @Override
    public String getAbi() {
        return "";
    }

    /**
     * @return 宿主中注册的PluginProcessService实现的类名
     */
    @Override
    protected String getPluginProcessServiceName(String partKey) {
        /**
         * 1）一个插件，对应一个服务
         * 2）服务用来做加载插件等操作
         * 3）服务是注册在宿主里面的
         * */
        if ("sample-plugin-app".equals(partKey)) {
            return "com.tencent.shadow.sample.host.PluginProcessPPS";

        } else if ("sample-plugin-app2".equals(partKey)) {
            return "com.tencent.shadow.sample.host.Plugin2ProcessPPS";//在这里支持多个插件

        } else {
            //如果有默认PPS，可用return代替throw
            throw new IllegalArgumentException("unexpected plugin load request: " + partKey);
        }
    }

    @Override
    public void enter(final Context context, long fromId, Bundle bundle, final EnterCallback callback) {
        if (fromId == Constant.FROM_ID_NOOP) {
            //do nothing.
        } else if (fromId == Constant.FROM_ID_START_ACTIVITY) {
            Log.i(TAG, "SamplePluginManager, enter : onStartActivity");
            onStartActivity(context, bundle, callback);
        } else {
            throw new IllegalArgumentException("不认识的fromId==" + fromId);
        }
    }

    private void onStartActivity(final Context context, Bundle bundle, final EnterCallback callback) {
        //1）加载插件
        executorService.execute(() -> {

        });


        //2）插件启动 todo 这个环节先不展开，下个阶段展开
        Log.e(TAG, "SamplePluginManager, 插件启动，这个环节先不展开，下个阶段展开");
    }
}
