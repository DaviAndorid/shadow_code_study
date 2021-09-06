

package com.example.dynamic_host;

import android.view.View;

public interface EnterCallback {

    void onShowLoadingView(View view);

    void onCloseLoadingView();

    void onEnterComplete();
}
