package com.example.constant;

public class Constant {

    public static final int FROM_ID_NOOP = 1000;
    public static final int FROM_ID_START_ACTIVITY = 1002;
    public static final int FROM_ID_BIND_SERVICE = 1003;

    public static final String KEY_PLUGIN_ZIP_PATH = "pluginZipPath";
    public static final String KEY_ACTIVITY_CLASSNAME = "KEY_ACTIVITY_CLASSNAME";
    public static final String KEY_EXTRAS = "KEY_EXTRAS";
    public static final String KEY_PLUGIN_PART_KEY = "KEY_PLUGIN_PART_KEY";
    /**
     * 插件名字partKey
     * 具体见：https://juejin.cn/post/7008060973038698503#heading-1
     * 里面的 partKey 字眼，就明白是什么意思了
     */
    public static final String PART_KEY_PLUGIN_MAIN_APP = "sample-plugin-app";
    public static final String PART_KEY_PLUGIN_ANOTHER_APP = "sample-plugin-app2";
}
