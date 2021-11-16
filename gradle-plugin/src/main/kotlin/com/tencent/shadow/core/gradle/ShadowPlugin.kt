package com.tencent.shadow.core.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.tencent.shadow.core.gradle.extensions.PackagePluginExtension
import com.tencent.shadow.core.gradle.transformkit.AndroidClassPoolBuilder
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import java.io.File
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible

class ShadowPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        System.err.println("（0）ShadowPlugin project.name = " + project.name)

        /**
         * 1）Returns the Android extension.
         * 2）创建目录 androidJar
         * */
        val baseExtension = getBaseExtension(project)
        val sdkDirectory = baseExtension.sdkDirectory
        val androidJarPath = "platforms/${baseExtension.compileSdkVersion}/android.jar"
        val androidJar = File(sdkDirectory, androidJarPath)
        System.err.println("（1）找到了本地开发的 androidJar = " + androidJar.absoluteFile)

        /**
         * 在这里取到的contextClassLoader
         * 1）包含运行时库(classpath方式引入的)shadow-runtime ？？？
         * */
        val contextClassLoader = Thread.currentThread().contextClassLoader

        //todo disable_shadow_transform 开关功能？ 自定义 classPool
        //val classPoolBuilder = AndroidClassPoolBuilder(project, contextClassLoader, androidJar)
        //自定义 拓展shadow
        val shadowExtension = project.extensions.create("shadow", ShadowExtension::class.java)
        System.err.println("（2）自定义 拓展shadow ok ..")
        //todo disable_shadow_transform 开关功能？
        /*if (!project.hasProperty("disable_shadow_transform")) {
            baseExtension.registerTransform(ShadowTransform(
                    project,
                    classPoolBuilder,
                    { shadowExtension.transformConfig.useHostContext }
            ))
        }*/
        //自定义 拓展packagePlugin
        project.extensions.create("packagePlugin", PackagePluginExtension::class.java, project)
        System.err.println("（3）自定义 拓展packagePlugin ok ..")

        /***
         * 使用拓展
         * */
        project.afterEvaluate {
            System.err.println("（4）自定义 start afterEvaluate。。")

            val packagePlugin = project.extensions.findByName("packagePlugin")
            val extension = packagePlugin as PackagePluginExtension
            val buildTypes = extension.buildTypes

            //根据配置的 pluginTypes 创建执行任务
            val tasks = mutableListOf<Task>()
            for (i in buildTypes) {
                System.err.println("（5）根据配置的 pluginTypes 创建执行任务《PackagePluginTask》 ，buildTypes = " + i.name)
                //组织apk和json等插件为zip
                val task = createPackagePluginTask(project, i)
                tasks.add(task)
            }

            //入口：packageAllPlugin 使用插件的模块（如：sample-plugin-app）中的plugin组任务里面找按钮
            if (tasks.isNotEmpty()) {
                System.err.println("（6）《PackagePluginTask》依赖到指定的入口任务【packageAllPlugin】")
                project.tasks.create("packageAllPlugin") {
                    it.group = "plugin"
                    it.description = "打包所有插件"
                }.dependsOn(tasks)
            }
        }

    }

    /**
     * Returns the Android extension.
     * 1）3.0.0版本的时候
     * 2）其他版本的时候
     * */
    fun getBaseExtension(project: Project): BaseExtension {
        //获得《com.android.application》插件
        val plugin = project.plugins.getPlugin(AppPlugin::class.java)
        //
        if (com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION == "3.0.0") {
            val method = BasePlugin::class.declaredFunctions.first { it.name == "getExtension" }
            method.isAccessible = true
            return method.call(plugin) as BaseExtension
        } else {
            return project.extensions.getByName("android") as BaseExtension
        }
    }

    /***
     * 自定义拓展 shadow
     * open关键字：
     * 1）在java中，允许创建任意的子类并重写方法任意的方法，除非显示的使用了final关键字进行标注。
     * 2）在kotlin的世界里面则不是这样，在kotlin中它所有的类默认都是final的
     * 2.1）为类增加open，class就可以被继承了
     * 2.2）为方法增加open，那么方法就可以被重写了
     * */
    open class ShadowExtension {
        var transformConfig = TransformConfig()
        fun transform(action: Action<in TransformConfig>) {
            action.execute(transformConfig)
        }
    }

    class TransformConfig {
        var useHostContext: Array<String> = emptyArray()
    }


}