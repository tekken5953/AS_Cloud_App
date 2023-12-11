package app.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private final Context mContext;
    private Toast toast;

    /**
     * Constructor
     **/
    public ToastUtils(Context context) {
        this.mContext = context;
    }

    /**
     * 토스트메시지를 2초간 비동기로 보여준다
     **/
    public void showMessage(final String message) {
        Runnable r = () -> {
            cancelToast();
            toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
            toast.show();
        };

        r.run();
    }

    /**
     * 토스트메시지가 보여지는 시간을 동적으로 설정한다
     **/
    public void showMessage(final String message, final int duration) {
        Runnable r = () -> {
            cancelToast();
            toast = Toast.makeText(mContext, message, duration);
            toast.show();
        };

        r.run();
    }

    /**
     * 토스트메시지가 보여지고 있으면 취소시킨다
     **/
    private void cancelToast() {
        if (toast != null) toast.cancel();
        toast = new Toast(mContext);
    }
}
