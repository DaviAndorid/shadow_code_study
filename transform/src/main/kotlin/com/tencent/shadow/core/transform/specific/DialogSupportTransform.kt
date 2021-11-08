
package com.tencent.shadow.core.transform.specific

import com.tencent.shadow.core.transform_kit.SpecificTransform
import com.tencent.shadow.core.transform_kit.TransformStep
import javassist.CodeConverter
import javassist.CtClass
import javassist.bytecode.Descriptor

class DialogSupportTransform : SpecificTransform() {
    companion object {
        const val ShadowActivityClassname = "com.tencent.shadow.core.runtime.ShadowActivity"
        const val AndroidDialogClassname = "android.app.Dialog"
        const val DialogSupportTransformClassname = "com.tencent.shadow.core.runtime.ShadowDialogSupport"
    }

    override fun setup(allInputClass: Set<CtClass>) {
        val androidDialog = mClassPool[AndroidDialogClassname]
        val shadowDialogSupport = mClassPool[DialogSupportTransformClassname]
        val shadowActivity = mClassPool[ShadowActivityClassname]

        val setOwnerActivityMethod = androidDialog.getDeclaredMethod("setOwnerActivity")
        val getOwnerActivityMethod = androidDialog.getDeclaredMethod("getOwnerActivity")

        //appClass中的Activity都已经被改名为ShadowActivity了．所以要把方法签名也先改一下．
        val shadowActivitySig = "Lcom/tencent/shadow/core/runtime/ShadowActivity;"
        setOwnerActivityMethod.methodInfo.descriptor = "($shadowActivitySig)V"
        getOwnerActivityMethod.methodInfo.descriptor = "()$shadowActivitySig"

        val dialogSetOwnerActivityMethod = shadowDialogSupport.getMethod("dialogSetOwnerActivity",
                Descriptor.ofMethod(CtClass.voidType,
                        arrayOf(androidDialog, shadowActivity)))
        val dialogGetOwnerActivityMethod = shadowDialogSupport.getMethod("dialogGetOwnerActivity",
                Descriptor.ofMethod(shadowActivity,
                        arrayOf(androidDialog)))

        newStep(object : TransformStep {
            override fun filter(allInputClass: Set<CtClass>) = allInputClass

            override fun transform(ctClass: CtClass) {
                ctClass.defrost()
                val codeConverter = CodeConverter()
                codeConverter.redirectMethodCallToStatic(
                    setOwnerActivityMethod,
                    dialogSetOwnerActivityMethod
                )
                codeConverter.redirectMethodCallToStatic(
                    getOwnerActivityMethod,
                    dialogGetOwnerActivityMethod
                )
                try {
                    ctClass.instrument(codeConverter)
                } catch (e: Exception) {
                    System.err.println("处理" + ctClass.name + "时出错:" + e)
                    throw e
                }
            }
        })

    }

}