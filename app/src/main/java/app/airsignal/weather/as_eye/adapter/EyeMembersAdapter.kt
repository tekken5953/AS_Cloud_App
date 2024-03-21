package app.airsignal.weather.as_eye.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.util.OnSingleClickListener
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.SnackBarUtils
import io.opencensus.trace.Span
import org.w3c.dom.Text


class EyeMembersAdapter(private val context: Context, list: ArrayList<EyeDataModel.Members>) :
    RecyclerView.Adapter<EyeMembersAdapter.ViewHolder>() {
    private var mList = list

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item_members, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val id = itemView.findViewById<TextView>(R.id.listItemMembersId)
        private val isMaster = itemView.findViewById<TextView>(R.id.listItemMembersMaster)
        private val delete = itemView.findViewById<ImageView>(R.id.listItemMembersDelete)
        private val changeMaster = itemView.findViewById<ImageView>(R.id.listItemMembersChangeMaster)

        fun bind(dao: EyeDataModel.Members) {
            id.text = dao.id

            delete.visibility = if (dao.isMaster) View.GONE else View.VISIBLE

            changeMaster.visibility = if(dao.isMaster) View.GONE else View.VISIBLE

            isMaster.text = if (dao.isMaster) "소유자" else "게스트"
            isMaster.setTextColor(context.getColor(if(dao.isMaster) R.color.main_blue_color else R.color.eye_graph_gray))

            delete.setOnClickListener {
                val dialog = MakeDoubleDialog(context)
                val deleteString ="'${dao.id}'을 등록 멤버에서 해제하시겠습니까?"
                val deleteSpan = SpannableStringBuilder(deleteString)
                deleteSpan.setSpan(ForegroundColorSpan(context.getColor(R.color.ae_very_bad_main)),
                    deleteString.indexOf("'${dao.id}'"),
                    deleteString.indexOf("'${dao.id}'") + "'${dao.id}'".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val make = dialog.make(deleteSpan,"해제하기","취소",R.color.ae_very_bad_main)
                make.first.setOnClickListener {
                    dialog.dismiss()
                    ToastUtils(context).showMessage("삭제가 완료되었습니다")
                }
                make.second.setOnClickListener {
                    dialog.dismiss()
                }
            }

            changeMaster.setOnClickListener {
                val dialog = AlertDialog.Builder(context)
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_member_trans, null, false)
                dialog.setView(view)
                val alertDialog = dialog.create()

                val apply = view.findViewById<AppCompatButton>(R.id.dialogMemberTransApply)
                val cancel = view.findViewById<AppCompatButton>(R.id.dialogMemberTransCancel)
                val et = view.findViewById<EditText>(R.id.dialogMemberTransEt)
                val guide = view.findViewById<TextView>(R.id.dialogMemberTransGuide)
                val contents = view.findViewById<TextView>(R.id.dialogMemberTransContents)

                val guideText = "소유자 권한을 변경 할 이메일인\n'${dao.id}'\n을 아래에 입력해주세요"
                val guideSpan = SpannableStringBuilder(guideText)
                guideSpan.setSpan(ForegroundColorSpan(
                    context.getColor(R.color.main_blue_color)),
                    guideText.indexOf("'${dao.id}'"),guideText.indexOf("'${dao.id}'") + "'${dao.id}'".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                guideSpan.setSpan(StyleSpan(Typeface.BOLD),
                    guideText.indexOf("'${dao.id}'"), guideText.indexOf("'${dao.id}'") + "'${dao.id}'".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                guide.text = guideSpan

                val contentsText = "멤버 관리는 소유자 전용 기능입니다. 소유자를 변경 하시면 해당 계정에서 더 이상 접근이 불가능합니다."
                val contentsSpan = SpannableStringBuilder(contentsText)
                contentsSpan.setSpan(ForegroundColorSpan(context.getColor(R.color.ae_very_bad_main)),
                contentsText.indexOf("불가능"), contentsText.indexOf("불가능") + "불가능".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                contents.text = contentsSpan

                apply.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        alertDialog.dismiss()
                    }
                })

                cancel.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        alertDialog.dismiss()
                    }
                })

                et.doAfterTextChanged {
                    if (et.text.toString() == dao.id) {
                        apply.isEnabled = true
                        KeyboardController.onKeyboardDown(context, et)
                    } else {
                        apply.isEnabled = false
                    }
                }

                alertDialog.show()
            }
        }
    }
}