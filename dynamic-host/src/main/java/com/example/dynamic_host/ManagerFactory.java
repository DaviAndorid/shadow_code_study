
package com.example.dynamic_host;

import android.content.Context;

public interface ManagerFactory {
    PluginManagerImpl buildManager(Context context);
}
