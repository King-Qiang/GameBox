package me.wangqiang.c.common.mvvm.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import me.wangqiang.gamebox.c.common.R

class NoticeDialog (
    private val context: Context,
    private val resource: Int?,
    private val title: String,
    private val content: String,
    private val btnUpText: String,
    private val btnDownText: String,
    private val onUpClick: (() -> Any?)?,
    private val onDownClick: (() -> Any?)?,
) {
    private lateinit var mDialog: Dialog
    private lateinit var mParent: View
    private var tvTitle: TextView? = null
    private var tvContent: TextView? = null
    private var btnLeft: Button? = null
    private var btnRight: Button? = null

    init {
        initView()
        initEvent()
    }

    private fun initView() {
        mParent = LayoutInflater.from(context)
            .inflate(resource ?: R.layout.dialog_notice, null, false)
        tvTitle = mParent.findViewById(R.id.tv_title)
        tvContent = mParent.findViewById(R.id.tv_content)
        btnLeft = mParent.findViewById(R.id.btn_left)
        btnRight = mParent.findViewById(R.id.btn_right)
        tvTitle?.text = title
        tvContent?.text = content
        btnLeft?.text = btnUpText
        btnRight?.text = btnDownText
        if (btnDownText.isEmpty()) {
            btnRight?.visibility = View.GONE
        }
        if (btnUpText.isEmpty()) {
            btnLeft?.visibility = View.GONE
        }

        mDialog = Dialog(context, R.style.CommonDialog).apply {
            setContentView(mParent)
            window?.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun initEvent() {
        btnLeft?.setOnClickListener{
            onUpClick?.invoke()
            dismiss()
        }
        btnRight?.setOnClickListener{
            onDownClick?.invoke()
            dismiss()
        }
    }

    fun show() {
        mDialog.show()
    }

    private fun dismiss() {
        mDialog.dismiss()
    }

    fun isShowing(): Boolean {
        return mDialog.isShowing
    }

    class Builder(private val context: Context, private val resource: Int?) {
        private var title: String = ""
        private var content: String = ""
        private var btnUpText: String = ""
        private var btnDownText: String = ""
        private var onUpClick: (() -> Any?)? = null
        private var onDownClick: (() -> Any?)? = null
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }
        fun setContent(content: String): Builder {
            this.content = content
            return this
        }
        fun setBtnUpText(btnUpText: String, onUpClick: (() -> Any?)?): Builder {
            this.btnUpText = btnUpText
            this.onUpClick = onUpClick
            return this
        }
        fun setBtnDownText(btnDownText: String, onDownClick: (() -> Any?)?): Builder {
            this.btnDownText = btnDownText
            this.onDownClick = onDownClick
            return this
        }
        fun build(): NoticeDialog {
            return NoticeDialog(context, resource, title, content, btnUpText, btnDownText, onUpClick, onDownClick)
        }
    }
}