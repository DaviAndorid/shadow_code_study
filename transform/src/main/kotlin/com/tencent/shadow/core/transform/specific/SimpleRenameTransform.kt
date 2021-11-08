
package com.tencent.shadow.core.transform.specific

import com.tencent.shadow.core.transform_kit.ReplaceClassName
import com.tencent.shadow.core.transform_kit.SpecificTransform
import com.tencent.shadow.core.transform_kit.TransformStep
import javassist.CtClass

open class SimpleRenameTransform(private val fromToMap: Map<String, String>) : SpecificTransform() {
    final override fun setup(allInputClass: Set<CtClass>) {
        newStep(object : TransformStep {
            override fun filter(allInputClass: Set<CtClass>) = allInputClass

            override fun transform(ctClass: CtClass) {
                fromToMap.forEach {
                    ReplaceClassName.replaceClassName(ctClass, it.key, it.value)
                }
            }
        })
    }
}