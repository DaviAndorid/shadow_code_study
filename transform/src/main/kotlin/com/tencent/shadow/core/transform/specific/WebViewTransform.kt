
package com.tencent.shadow.core.transform.specific

import com.tencent.shadow.core.transform_kit.SpecificTransform
import com.tencent.shadow.core.transform_kit.TransformStep
import javassist.CodeConverter
import javassist.CtClass

class WebViewTransform : SpecificTransform() {
    companion object {
        const val AndroidWebViewClassname = "android.webkit.WebView"
        const val ShadowWebViewClassname = "com.tencent.shadow.core.runtime.ShadowWebView"
    }

    val codeConverter = CodeConverter()
    override fun setup(allInputClass: Set<CtClass>) {
        codeConverter.replaceNew(mClassPool[AndroidWebViewClassname], mClassPool[ShadowWebViewClassname])

        newStep(object : TransformStep {
            override fun filter(allInputClass: Set<CtClass>) =
                    filterRefClasses(allInputClass, listOf(AndroidWebViewClassname))

            override fun transform(ctClass: CtClass) {
                if (ctClass.superclass.name == AndroidWebViewClassname) {
                    ctClass.classFile.superclass = ShadowWebViewClassname
                }
            }
        })

        newStep(object : TransformStep {
            override fun filter(allInputClass: Set<CtClass>) =
                    filterRefClasses(allInputClass, listOf(AndroidWebViewClassname))

            override fun transform(ctClass: CtClass) {
                try {
                    ctClass.instrument(codeConverter)
                } catch (e: Exception) {
                    System.err.println("处理" + ctClass.name + "时出错")
                    throw e
                }
            }
        })
    }
}