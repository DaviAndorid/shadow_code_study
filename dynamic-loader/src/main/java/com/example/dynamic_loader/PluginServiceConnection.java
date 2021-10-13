
package com.example.dynamic_loader;

import android.content.ComponentName;
import android.os.IBinder;

public interface PluginServiceConnection {
    String DESCRIPTOR = PluginServiceConnection.class.getName();
    int TRANSACTION_onServiceConnected = IBinder.FIRST_CALL_TRANSACTION;
    int TRANSACTION_onServiceDisconnected = IBinder.FIRST_CALL_TRANSACTION + 1;

    void onServiceConnected(ComponentName name, IBinder service);

    void onServiceDisconnected(ComponentName name);

}
