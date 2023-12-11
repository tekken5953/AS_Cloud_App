package app.core_customview

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.core_databse.db.sp.GetAppInfo.getUserFontScale
import app.core_databse.db.sp.SetSystemInfo
import app.core_databse.db.sp.SpDao.TEXT_SCALE_BIG
import app.core_databse.db.sp.SpDao.TEXT_SCALE_SMALL
import java.util.concurrent.CompletableFuture

/**
 * @author : Lee Jae Young
 * @since : 2023-03-28 오전 11:15
 **/

class ShowDialogClass(private val activity: Activity) {
    private var builder: androidx.appcompat.app.AlertDialog.Builder =
        androidx.appcompat.app.AlertDialog.Builder(activity, R.style.AlertDialog)
    private lateinit var alertDialog: androidx.appcompat.app.AlertDialog

    init {
        // 폰트 크기 설정
        when(getUserFontScale(activity)) {
            TEXT_SCALE_SMALL -> SetSystemInfo.setTextSizeSmall(activity)
            TEXT_SCALE_BIG -> SetSystemInfo.setTextSizeLarge(activity)
            else -> SetSystemInfo.setTextSizeDefault(activity)
        }
    }

    /** 다이얼로그 뒤로가기 버튼 리스너 등록 **/
    fun setBackPressed(imageView: View): ShowDialogClass {
        imageView.setOnClickListener { dismiss() }
        return this
    }

    /** 다이얼로그 뒤로가기 버튼 후 액티비티 갱신 **/
    fun setBackPressRefresh(imageView: ImageView): ShowDialogClass {
        imageView.setOnClickListener {
            CompletableFuture
                .supplyAsync { dismiss() }
                .thenAccept {
                    activity.let {
                        it.finish() //인텐트 종료
                        it.overridePendingTransition(0, 0) //인텐트 효과 없애기
                        val intent = it.intent //인텐트
                        it.startActivity(intent) //액티비티 열기
                        it.overridePendingTransition(0, 0) //인텐트 효과 없애기
                    }
                }
        }
        return this
    }

    /** 다이얼로그 뷰 소멸 **/
    fun dismiss() {
        if (alertDialog.isShowing) alertDialog.dismiss()
    }

    /** 다이얼로그 뷰 갱신 **/
    fun show(v: View, cancelable: Boolean) {
        v.let {
            if(v.parent != null) (v.parent as ViewGroup).removeView(v)
            builder.setView(v).setCancelable(cancelable)
            alertDialog = builder.create()
            alertDialog.show()
        }
    }
}