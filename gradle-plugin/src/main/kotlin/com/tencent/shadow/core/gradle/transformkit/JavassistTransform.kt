
package com.tencent.shadow.core.gradle.transformkit

import com.android.build.api.transform.TransformInvocation
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipInputStream

/*
open class JavassistTransform(project: Project, val classPoolBuilder: ClassPoolBuilder) : ClassTransform(project) {
    val mCtClassInputMap = mutableMapOf<CtClass, InputClass>()
    lateinit var classPool: ClassPool

    override fun onOutputClass(className: String, outputStream: OutputStream) {
        classPool[className].writeOut(outputStream)
    }

    override fun DirInputClass.onInputClass(classFile: File, outputFile: File) {
        classFile.inputStream().use {
            val ctClass: CtClass = classPool.makeClass(it)
            addOutput(ctClass.name, outputFile)
            mCtClassInputMap[ctClass] = this
        }
    }

    override fun JarInputClass.onInputClass(zipInputStream: ZipInputStream, entryName: String) {
        val ctClass = classPool.makeClass(zipInputStream)
        addOutput(ctClass.name, entryName)
        mCtClassInputMap[ctClass] = this
    }

    override fun beforeTransform(invocation: TransformInvocation) {
        super.beforeTransform(invocation)
        mCtClassInputMap.clear()
        classPool = classPoolBuilder.build()
    }


    override fun onTransform() {
        //do nothing.
    }

    fun CtClass.writeOut(output: OutputStream) {
        this.toBytecode(java.io.DataOutputStream(output))
    }

}*/

/**
 * 1）一个CtClass（compile-time class）的实例是一个可以用来操作class文件的句柄
 * 2）ClassPool是一个存放着代表class文件的CtClass类容器，
 * 3）想要修改一个class文件，用户必须实例化一个ClassPool类，并且使用ClassPool的get方法取得代表这个那个class文件的CtClass的引用
 * */
interface ClassPoolBuilder {
    fun build(): ClassPool
}