package com.tencent.shadow.core.gradle

import com.tencent.shadow.core.gradle.extensions.PackagePluginExtension
import com.tencent.shadow.core.gradle.extensions.PluginBuildType
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

internal fun createPackagePluginTask(project: Project, buildType: PluginBuildType): Task {

    /**
     * json 目录
     * */
    val targetConfigFile = File(project.projectDir.absolutePath + "/generatePluginConfig/${buildType.name}/config.json")

    /**
     * 1）依赖于 createGenerateConfigTask
     * 2）压缩zip包
     * */
    return project.tasks.create("package${buildType.name.capitalize()}Plugin", Zip::class.java) {
        System.err.println("PackagePluginTask task start run...")

        //runtime apk file
        val runtimeApkName: String = buildType.runtimeApkConfig.first
        var runtimeFile: File? = null
        if (runtimeApkName.isNotEmpty()) {
            System.err.println("runtime apk...")
            runtimeFile = ShadowPluginHelper.getRuntimeApkFile(project, buildType, false)
        }

        //loader apk file
        val loaderApkName: String = buildType.loaderApkConfig.first
        var loaderFile: File? = null
        if (loaderApkName.isNotEmpty()) {
            System.err.println("loader apk...")
            loaderFile = ShadowPluginHelper.getLoaderApkFile(project, buildType, false)
        }

        //config file
        //不存在：/Users/yabber/AndroidStudioProjects/DemoHot/sample-plugin-app/build + /intermediates
        //val targetConfigFile = File(project.buildDir.absolutePath + "/intermediates/generatePluginConfig/${buildType.name}/config.json")
        //val targetConfigFile = File(project.projectDir.absolutePath + "/generatePluginConfig/${buildType.name}/config.json")
        val result = targetConfigFile.parentFile.mkdirs()
        System.err.println("mkdirs config.json parentFile dir， result = " + result)
        System.err.println("targetConfigFile = " + targetConfigFile.absoluteFile)

        //all plugin apks
        val pluginFiles: MutableList<File> = mutableListOf()
        for (i in buildType.pluginApks) {
            val file = ShadowPluginHelper.getPluginFile(project, i, false)
            System.err.println("pluginFile = " + file.absoluteFile)
            pluginFiles.add(file)
        }

        it.group = "plugin"
        it.description = "打包插件"
        it.outputs.upToDateWhen { false }
        if (runtimeFile != null) {
            pluginFiles.add(runtimeFile)
        }
        if (loaderFile != null) {
            pluginFiles.add(loaderFile)
        }
        it.from(pluginFiles, targetConfigFile)//  from

        val packagePlugin = project.extensions.findByName("packagePlugin")
        val extension = packagePlugin as PackagePluginExtension

        val suffix = if (extension.archiveSuffix.isEmpty()) "" else extension.archiveSuffix
        val prefix = if (extension.archivePrefix.isEmpty()) "plugin" else extension.archivePrefix
        if (suffix.isEmpty()) {
            it.archiveName = "$prefix-${buildType.name}.zip"
        } else {
            it.archiveName = "$prefix-${buildType.name}-$suffix.zip"
        }
        //destinationDir
        it.destinationDir = File(if (extension.destinationDir.isEmpty()) "${project.rootDir}/build" else extension.destinationDir)
        System.err.println("destinationDir = " + it.destinationDir.absoluteFile)

        System.err.println("PackagePluginTask task end...")
    }.dependsOn(createGenerateConfigTask(project, buildType))
}

/***
 * 1）生成 config.json 文件
 * 2）依赖于 pluginApkTasks
 * */
private fun createGenerateConfigTask(project: Project, buildType: PluginBuildType): Task {
    System.err.println("GenerateConfigTask task run ... ")

    /**
     * 目录
     * */
    val targetConfigFile = File(project.projectDir.absolutePath + "/generatePluginConfig/${buildType.name}/config.json")

    val packagePlugin = project.extensions.findByName("packagePlugin")
    val extension = packagePlugin as PackagePluginExtension

    //runtime apk build task
    val runtimeApkName = buildType.runtimeApkConfig.first
    var runtimeTask = ""
    if (runtimeApkName.isNotEmpty()) {
        runtimeTask = buildType.runtimeApkConfig.second
        System.err.println("runtime task = $runtimeTask")
        //println("runtime task = $runtimeTask")
    }

    //loader apk build task
    val loaderApkName = buildType.loaderApkConfig.first
    var loaderTask = ""
    if (loaderApkName.isNotEmpty()) {
        loaderTask = buildType.loaderApkConfig.second
        System.err.println("loader task = $loaderTask")
        //println("loader task = $loaderTask")
    }

    //插件工程任务，如：:sample-manager:assembleDebug
    val pluginApkTasks: MutableList<String> = mutableListOf()
    for (i in buildType.pluginApks) {
        val task = i.buildTask
        System.err.println("pluginApkProjects task = $task")
        //println("pluginApkProjects task = $task")
        pluginApkTasks.add(task)
    }

    /**
     * 1）依赖于  pluginApkTasks
     * */
    val task = project.tasks.create("generate${buildType.name.capitalize()}Config") {
        it.group = "plugin"
        it.description = "生成插件配置文件"
        it.outputs.file(targetConfigFile)
        it.outputs.upToDateWhen { false }
    }
            .dependsOn(pluginApkTasks)//依赖于插件工程任务，如：:sample-manager:assembleDebug
            .doLast {

                System.err.println("generate json Config task begin")
                //println("generateConfig task begin")
                val json = extension.toJson(project, loaderApkName, runtimeApkName, buildType)
                System.err.println("json = " + json)

                val bizWriter = BufferedWriter(FileWriter(targetConfigFile))
                bizWriter.write(json.toJSONString())
                bizWriter.newLine()
                bizWriter.flush()
                bizWriter.close()

                System.err.println("generateConfig task done")
            }


    if (loaderTask.isNotEmpty()) {
        task.dependsOn(loaderTask)
    }
    if (runtimeTask.isNotEmpty()) {
        task.dependsOn(runtimeTask)
    }
    return task
}