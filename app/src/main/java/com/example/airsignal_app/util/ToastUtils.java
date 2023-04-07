package com.example.airsignal_app.util;

import android.app.Activity;
import android.widget.Toast;

public class ToastUtils {
    private final Activity mContext;
    private Toast toast;

    /** constructor **/
    public ToastUtils(Activity context) {
        this.mContext = context;
    }

    /** 토스트메시지를 2초간 비동기로 보여준다**/
    public void shortMessage(final String message) {
        Runnable r = () -> {
            cancelToast();
            toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
            toast.show();
        };

        mContext.runOnUiThread(r);
    }

    /** 토스트메시지가 보여지는 시간을 동적으로 설정한다 **/
    public void customDurationMessage(final String message, final int duration) {
        Runnable r = () -> {
            cancelToast();
            toast = Toast.makeText(mContext, message, duration);
            toast.show();
        };

    }

    /** 토스트메시지가 보여지고 있으면 취소시킨다 **/
    private void cancelToast() {
        if (toast != null) {
            this.mContext.runOnUiThread(() -> toast.cancel());
        }

        toast = new Toast(mContext);
    }
}
