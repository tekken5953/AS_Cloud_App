package app.airsignal.weather.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtils {
    private final Context mContext;
    private Toast toast;
    private final Handler mHandler;

    /**
     * Constructor
     **/
    public ToastUtils(Context context) {
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 토스트메시지를 지정된 시간동안 비동기로 보여준다
     **/
    public void showMessage(final String message, final long duration) {
        mHandler.post(() -> {
            cancelToast();
            toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
            toast.show();
            mHandler.postDelayed(this::cancelToast, duration * 1000L);
        });
    }

    public void showMessage(final String message) {
        mHandler.post(() -> {
            cancelToast();
            toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    /**
     * 토스트메시지가 보여지고 있으면 취소시킨다
     **/
    private void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }
}