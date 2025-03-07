package org.iriswallet.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.iriswallet.R
import org.iriswallet.databinding.FragmentBitcoinUnspentBinding

class BitcoinUnspentFragment :
    MainBaseFragment<FragmentBitcoinUnspentBinding>(FragmentBitcoinUnspentBinding::inflate) {

    private lateinit var adapter: BitcoinUnspentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getBitcoinUnspents()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLoader(true)
        binding.unspentRV.layoutManager = LinearLayoutManager(view.context)
        binding.unspentSwipeRefresh.setOnRefreshListener {
            disableUI()
            viewModel.getBitcoinUnspents()
        }
        if (!::adapter.isInitialized) adapter = BitcoinUnspentAdapter(listOf())
        binding.unspentRV.adapter = adapter

        viewModel.unspents.observe(viewLifecycleOwner) {
            val prevResponse = it.peekContent()
            if (it.hasBeenHandled && prevResponse.data?.isNotEmpty() == true) {
                adapter = BitcoinUnspentAdapter(prevResponse.data)
                binding.unspentRV.adapter = adapter
            }
            it.getContentIfNotHandled()?.let { response ->
                if (response.data != null) { // empty list allowed (no unspents)
                    adapter = BitcoinUnspentAdapter(response.data)
                    binding.unspentRV.adapter = adapter
                    enableUI()
                } else {
                    handleError(response.error!!) {
                        toastError(R.string.err_listing_unspents, response.error.message)
                        findNavController().popBackStack()
                    }
                }
            }
            setLoader(false)
        }
    }

    override fun enableUI() {
        super.enableUI()
        setLoader(false)
    }

    private fun disableUI() {
        setLoader(true)
    }

    private fun setLoader(state: Boolean) {
        binding.unspentSwipeRefresh.post { binding.unspentSwipeRefresh.isRefreshing = state }
    }
}
