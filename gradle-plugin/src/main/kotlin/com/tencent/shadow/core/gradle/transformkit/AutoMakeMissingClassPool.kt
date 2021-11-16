package com.tencent.shadow.core.gradle.transformkit

import javassist.ClassPool
import javassist.CtClass

class AutoMakeMissingClassPool(useDefaultPath: Boolean) : ClassPool(useDefaultPath) {

    /**
     * 1）Kotlin 的一个特性：没有静态成员。
     * 2）companion object 修饰为伴生对象,伴生对象在类中只能存在一个，类似于java中的静态方法
     * 3）地址：https://zhuanlan.zhihu.com/p/26713535
     * */
    companion object {
        /***
         * 1）地址1：https://github.com/Tencent/Shadow/actions/runs/415350303/workflow
         * 2）地址2：https://github.com/Tencent/Shadow/issues/451（todo 没懂！！）
         * 3）自动创建缺失类时支持更多调用到fixTypes2的路径（todo 没懂！！）
         * */
        fun isFromFixTypes2Called(newThrowable: Throwable): Boolean {
            for (stackTraceElement in newThrowable.stackTrace) {
                if (stackTraceElement.methodName == "fixTypes2") {
                    return true
                }
            }
            return false
        }
    }

    override fun get0(classname: String?, useCache: Boolean): CtClass? {
        var get0 = super.get0(classname, useCache)

        // 来自javassist.bytecode.stackmap.TypeData.TypeVar.fixTypes2的调用时，
        // 如果类不存在，就构造一个。

        // fixTypes2是重建StackMap的步骤，参考：https://stackoverflow.com/a/37310409/11616914
        // 我们的Transform不会去修改找不到的类型相关的代码，
        // 而fixTypes2处理的逻辑是在确定泛型的下界，
        // 由于我们没改任何跟找不到类型相关的逻辑，所以未知类型的父类重定义为Object，应该没有危险。
        //
        // 这里必须判断是来自的fixTypes2的调用，而不是所有调用都构造类型，是因为存在像
        // javassist.compiler.MemberResolver.lookupClass0
        // 依赖NotFoundException的逻辑存在。
        if (get0 == null && isFromFixTypes2Called(Throwable())) {
            get0 = makeClass(classname)
            if (useCache) cacheCtClass(get0.getName(), get0, false)
        }

        return get0
    }
}