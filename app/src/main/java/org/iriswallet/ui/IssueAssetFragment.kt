package org.iriswallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import org.iriswallet.R
import org.iriswallet.databinding.FragmentIssueAssetBinding

class IssueAssetFragment :
    MainBaseFragment<FragmentIssueAssetBinding>(FragmentIssueAssetBinding::inflate) {

    private lateinit var editableFields: Array<EditText>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editableFields = arrayOf(binding.tickerInputET, binding.nameInputET, binding.amountInputET)
        for (editText in editableFields) {
            editText.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {}

                    override fun onTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {}

                    override fun afterTextChanged(editable: Editable) {
                        val ticker = binding.tickerInputET.text.toString()
                        if (ticker != ticker.uppercase()) {
                            binding.tickerInputET.setText(ticker.uppercase())
                            binding.tickerInputET.setSelection(binding.tickerInputET.text.length)
                        }
                        if (editText.inputType == InputType.TYPE_CLASS_NUMBER)
                            fixETInteger(editText, editable.toString())
                        binding.issueBtn.isEnabled =
                            allETsFilled(editableFields) && isETPositive(binding.amountInputET)
                    }
                }
            )
        }
        binding.issueBtn.setOnClickListener {
            disableUI()
            viewModel.issueAsset(
                binding.tickerInputET.text.toString(),
                binding.nameInputET.text.toString(),
                binding.amountInputET.text.toString()
            )
        }

        viewModel.issuedAsset.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                if (response.data != null) {
                    viewModel.viewingAsset = response.data
                    findNavController()
                        .navigate(
                            IssueAssetFragmentDirections
                                .actionIssueAssetFragmentToAssetDetailFragment(
                                    viewModel.viewingAsset!!.name
                                )
                        )
                } else {
                    handleError(response.error!!) {
                        toastError(R.string.err_issuing_asset, response.error.message)
                    }
                }
                enableUI()
            }
        }
    }

    override fun enableUI() {
        super.enableUI()
        binding.issueBtn.isEnabled = true
        binding.issuePB.visibility = View.INVISIBLE
    }

    private fun disableUI() {
        mActivity.backEnabled = false
        binding.issuePB.visibility = View.VISIBLE
        binding.issueBtn.isEnabled = false
    }
}
