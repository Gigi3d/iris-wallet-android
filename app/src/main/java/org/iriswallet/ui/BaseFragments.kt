package org.iriswallet.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceFragmentCompat
import androidx.viewbinding.ViewBinding
import java.lang.NumberFormatException
import java.math.BigDecimal
import org.iriswallet.R
import org.iriswallet.utils.AppError
import org.iriswallet.utils.AppErrorType
import org.iriswallet.utils.TAG

private fun getErrMsg(fragment: Fragment, baseMsgID: Int, extraMsg: String? = null): String {
    var errMsg = fragment.getString(baseMsgID)
    if (!extraMsg.isNullOrBlank())
        errMsg =
            fragment.getString(
                R.string.app_exception_msg,
                errMsg,
                extraMsg.replaceFirstChar(Char::lowercase)
            )
    return errMsg
}

private fun toastError(fragment: Fragment, baseMsgID: Int, extraMsg: String? = null) {
    val errMsg = getErrMsg(fragment, baseMsgID, extraMsg)
    Log.d(fragment.TAG, errMsg)
    Toast.makeText(fragment.requireContext(), errMsg, Toast.LENGTH_LONG).show()
}

abstract class PreferenceBaseFragment : PreferenceFragmentCompat() {
    private lateinit var mActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
        mActivity.backEnabled = true
    }

    fun toastError(baseMsgID: Int, extraMsg: String? = null) = toastError(this, baseMsgID, extraMsg)
}

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class MainBaseFragment<B : ViewBinding>(private val inflate: Inflate<B>) : Fragment() {
    private var _binding: B? = null
    protected val binding
        get() = _binding!!

    protected val viewModel: MainViewModel by activityViewModels()

    protected lateinit var mActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
        mActivity.backEnabled = true
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCache()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getErrMsg(baseMsgID: Int, extraMsg: String? = null): String =
        getErrMsg(this, baseMsgID, extraMsg)

    fun toastError(baseMsgID: Int, extraMsg: String? = null) = toastError(this, baseMsgID, extraMsg)

    fun handleError(error: AppError, callback: () -> Unit) {
        if (error.type == AppErrorType.TIMEOUT_EXCEPTION) {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.err_timeout))
                .setPositiveButton(getString(R.string.exit)) { _, _ -> mActivity.finish() }
                .setCancelable(false)
                .create()
                .show()
        } else {
            callback()
        }
    }

    open fun enableUI() {
        mActivity.backEnabled = true
    }

    fun allETsFilled(editTexts: Array<EditText>): Boolean {
        var allFilled = true
        for (et in editTexts) {
            if (et.text.toString().trim().isEmpty()) allFilled = false
        }
        return allFilled
    }

    fun fixETInteger(editText: EditText, intString: String) {
        if (intString.isNotEmpty()) {
            var fixed = intString
            // remove leading zero
            if (fixed.length > 1 && fixed.startsWith("0")) fixed = fixed.drop(1)
            if (fixed != intString) {
                editText.setText(fixed)
                editText.setSelection(editText.text.length)
            }
        }
    }

    fun isETPositive(editText: EditText): Boolean {
        val amount = editText.text.toString()
        var enable = false
        if (amount.isNotEmpty()) {
            enable =
                try {
                    amount.toBigDecimal() > BigDecimal.ZERO
                } catch (e: NumberFormatException) {
                    false
                }
        }
        return enable
    }
}
