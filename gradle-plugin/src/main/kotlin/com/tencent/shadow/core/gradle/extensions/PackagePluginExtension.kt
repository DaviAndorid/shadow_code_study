package com.tencent.shadow.core.gradle.extensions

import com.tencent.shadow.core.gradle.ShadowPluginHelper
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import java.util.*

open class PackagePluginExtension {

    var loaderApkProjectPath = ""
    var runtimeApkProjectPath = ""

    var archivePrefix = ""
    var archiveSuffix = ""
    var destinationDir = ""

    var uuid = ""
    var version: Int = 0
    var uuidNickName = ""
    var compactVersion: Array<Int> = emptyArray()

    var buildTypes: NamedDomainObjectContainer<PluginBuildType>

    constructor(project: Project) {
        buildTypes = project.container(PluginBuildType::class.java)
        buildTypes.all {
            it.pluginApks = project.container(PluginApkConfig::class.java)
        }
    }

    fun pluginTypes(closure: Closure<PluginBuildType>) {
        buildTypes.configure(closure)
    }

    /***
     * json文件生成
     * */
    fun toJson(
            project: Project,
            loaderApkName: String,
            runtimeApkName: String,
            buildType: PluginBuildType
    ): JSONObject {
        val json = JSONObject()

        /**
         *  "pluginLoader":{
        "apkName":"sample-loader-release.apk",
        "hash":"11654AE11DF3C43642A10CCF21461468"
        },
         * */
        if (loaderApkName.isNotEmpty()) {
            //Json文件中 plugin-loader部分信息
            val pluginLoaderObj = JSONObject()
            pluginLoaderObj["apkName"] = loaderApkName
            val loaderFile = ShadowPluginHelper.getLoaderApkFile(project, buildType, true)
            pluginLoaderObj["hash"] = ShadowPluginHelper.getFileMD5(loaderFile)
            json["pluginLoader"] = pluginLoaderObj
        }

        /**
         * "runtime":{
        "apkName":"sample-runtime-release.apk",
        "hash":"FEC73F1212FD22D7261E9064D9DFAF3B"
        },
         * */
        if (runtimeApkName.isNotEmpty()) {
            //Json文件中 plugin-runtime部分信息
            val runtimeObj = JSONObject()
            runtimeObj["apkName"] = runtimeApkName
            val runtimeFile = ShadowPluginHelper.getRuntimeApkFile(project, buildType, true)
            runtimeObj["hash"] = ShadowPluginHelper.getFileMD5(runtimeFile)
            json["runtime"] = runtimeObj
        }


        /**
         *   "plugins":[
        {
        "partKey":"sample-plugin-app",
        "apkName":"sample-plugin-app-release.apk",
        "businessName":"sample-plugin-app",
        "hostWhiteList":[
        "com.tencent.shadow.sample.host.lib"
        ],
        "hash":"13FC58F2176FCF9BF3CCF92E14F0FDD3"
        },
        {
        "partKey":"sample-plugin-app2",
        "apkName":"sample-plugin-app-release2.apk",
        "businessName":"sample-plugin-app2",
        "hostWhiteList":[
        "com.tencent.shadow.sample.host.lib"
        ],
        "hash":"13FC58F2176FCF9BF3CCF92E14F0FDD3"
        }
        ],
         * */
        val jsonArr = JSONArray()
        for (i in buildType.pluginApks) {
            val pluginObj = JSONObject()
            pluginObj["businessName"] = i.businessName
            pluginObj["partKey"] = i.partKey
            pluginObj["apkName"] = i.apkName
            pluginObj["hash"] = ShadowPluginHelper.getFileMD5(ShadowPluginHelper.getPluginFile(project, i, true))
            if (i.dependsOn.isNotEmpty()) {
                val dependsOnJson = JSONArray()
                for (k in i.dependsOn) {
                    dependsOnJson.add(k)
                }
                pluginObj["dependsOn"] = dependsOnJson
            }
            if (i.hostWhiteList.isNotEmpty()) {
                val hostWhiteListJson = JSONArray()
                for (k in i.hostWhiteList) {
                    hostWhiteListJson.add(k)
                }
                pluginObj["hostWhiteList"] = hostWhiteListJson
            }
            jsonArr.add(pluginObj)
        }
        json["plugins"] = jsonArr


        //Config.json版本号
        if (version > 0) {
            json["version"] = version
        } else {
            json["version"] = 1
        }


        /**
         *  "UUID":"A0AE9AF8-330A-4D80-9D29-F7B903AEE90B",
         * */
        val uuid = "${project.rootDir}" + "/build/uuid.txt"
        val uuidFile = File(uuid)
        when {
            uuidFile.exists() -> {
                json["UUID"] = uuidFile.readText()
                println("uuid = " + json["UUID"] + " 由文件生成")
            }
            this.uuid.isEmpty() -> {
                json["UUID"] = UUID.randomUUID().toString().toUpperCase()
                println("uuid = " + json["UUID"] + " 随机生成")
            }
            else -> {
                json["UUID"] = this.uuid
                println("uuid = " + json["UUID"] + " 由配置生成")
            }
        }

        /**
         *   "UUID_NickName":"1.1.5"
         * */
        if (uuidNickName.isNotEmpty()) {
            json["UUID_NickName"] = uuidNickName
        } else {
            json["UUID_NickName"] = "1.0"
        }

        /**
         *   "compact_version":[
        1,
        2,
        3
        ],
         * */
        if (compactVersion.isNotEmpty()) {
            val jsonArray = JSONArray()
            for (i in compactVersion) {
                jsonArray.add(i)
            }
            json["compact_version"] = jsonArray
        }
        return json
    }
}