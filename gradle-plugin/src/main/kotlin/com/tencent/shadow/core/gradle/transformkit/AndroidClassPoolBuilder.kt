package com.tencent.shadow.core.gradle.transformkit

import javassist.ClassPool
import javassist.LoaderClassPath
import org.gradle.api.Project
import org.gradle.internal.classloader.VisitableURLClassLoader
import java.io.File

/***
 * 自定义ClassPool中，添加了两个路径
 * 1）contextClassLoader的
 * 2）androidJar的
 * */
class AndroidClassPoolBuilder(project: Project,
                              val contextClassLoader: ClassLoader,//Thread.currentThread().contextClassLoader
                              val androidJar: File)
    : ClassPoolBuilder {

    init {
        System.err.println("contextClassLoader = " + contextClassLoader)
        System.err.println("androidJar = " + androidJar.absoluteFile)
    }

    override fun build(): ClassPool {
        /**
         * 1）如果类不存在，就构造一个。
         * 2）这里使用 useDefaultPath:false 是因为这里取到的contextClassLoader不包含classpath指定进来的runtime
         * 3）所以在外部先获取一个包含了runtime的contextClassLoader传进来
         * */
        val classPool = AutoMakeMissingClassPool(false)
        //LoaderClassPath：类加载器的类搜索路径
        classPool.appendClassPath(LoaderClassPath(contextClassLoader))

        if (contextClassLoader is VisitableURLClassLoader) {
            val sb = StringBuilder()
            sb.appendln()
            for (urL in contextClassLoader.urLs) {
                sb.appendln(urL)
            }
            System.err.println("AndroidClassPoolBuilder appendClassPath contextClassLoader URLs:$sb")
        }

        classPool.appendClassPath(androidJar.absolutePath)
        System.err.println("AndroidClassPoolBuilder appendClassPath androidJar:${androidJar.absolutePath}")

        return classPool
    }
}