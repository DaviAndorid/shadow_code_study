
package com.tencent.shadow.core.transform

import com.tencent.shadow.core.transform.specific.*
import com.tencent.shadow.core.transform_kit.AbstractTransformManager
import com.tencent.shadow.core.transform_kit.InputClass
import com.tencent.shadow.core.transform_kit.SpecificTransform
import javassist.ClassPool
import javassist.CtClass

class TransformManager(ctClassInputMap: Map<CtClass, InputClass>,
                       classPool: ClassPool,
                       useHostContext: () -> Array<String>
) : AbstractTransformManager(ctClassInputMap, classPool) {

    override val mTransformList: List<SpecificTransform> = listOf(
            ApplicationTransform(),
            ActivityTransform(),
            ServiceTransform(),
            IntentServiceTransform(),
            InstrumentationTransform(),
            FragmentSupportTransform(),
            DialogSupportTransform(),
            WebViewTransform(),
            ContentProviderTransform(),
            PackageManagerTransform(),
            PackageItemInfoTransform(),
            AppComponentFactoryTransform(),
            LayoutInflaterTransform(),
            KeepHostContextTransform(useHostContext())
    )
}