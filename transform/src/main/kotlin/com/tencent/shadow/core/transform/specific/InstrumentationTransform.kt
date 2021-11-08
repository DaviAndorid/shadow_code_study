
package com.tencent.shadow.core.transform.specific

import com.tencent.shadow.core.transform_kit.ReplaceClassName
import com.tencent.shadow.core.transform_kit.SpecificTransform
import com.tencent.shadow.core.transform_kit.TransformStep
import javassist.CodeConverter
import javassist.CtClass
import javassist.CtMethod

class InstrumentationTransform : SpecificTransform() {
    companion object {
        const val AndroidInstrumentationClassname = "android.app.Instrumentation"
        const val ShadowInstrumentationClassname = "com.tencent.shadow.core.runtime.ShadowInstrumentation"
    }

    override fun setup(allInputClass: Set<CtClass>) {
        val shadowInstrumentation = mClassPool[ShadowInstrumentationClassname]

        val newShadowApplicationMethods = shadowInstrumentation.getDeclaredMethods("newShadowApplication")

        val newShadowActivityMethod = shadowInstrumentation.getDeclaredMethod("newShadowActivity")

        newStep(object : TransformStep {
            override fun filter(allInputClass: Set<CtClass>) = allInputClass

            override fun transform(ctClass: CtClass) {
                ReplaceClassName.replaceClassName(
                        ctClass,
                        AndroidInstrumentationClassname,
                        ShadowInstrumentationClassname
                )
            }
        })
        newStep(object : TransformStep {
            override fun filter(allInputClass: Set<CtClass>) = allInputClass

            override fun transform(ctClass: CtClass) {
                ctClass.defrost()
                val codeConverter = CodeConverter()
                newShadowApplicationMethods.forEach { codeConverter.redirectMethodCall("newApplication", it) }

                codeConverter.redirectMethodCall("newActivity", newShadowActivityMethod)
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
