
package com.tencent.shadow.core.transform.specific

class IntentServiceTransform : SimpleRenameTransform(
        mapOf("android.app.IntentService" to "com.tencent.shadow.core.runtime.ShadowIntentService")
)
