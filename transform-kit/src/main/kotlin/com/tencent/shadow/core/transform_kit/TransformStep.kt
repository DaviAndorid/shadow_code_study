package com.tencent.shadow.core.transform_kit

import javassist.CtClass

/**
 *Step：
 * 1）n.步;步伐;迈步;脚步声;步态;一步(的距离)
 * 2）vi.迈步;踩;踏;行走
 * */
interface TransformStep {
    fun filter(allInputClass: Set<CtClass>): Set<CtClass>

    fun transform(ctClass: CtClass)
}