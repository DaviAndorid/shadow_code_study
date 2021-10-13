
package com.example.dynamic_host;

import android.content.Context;

public interface LoaderFactory {
    PluginLoaderImpl buildLoader(String uuid, Context context);
}
