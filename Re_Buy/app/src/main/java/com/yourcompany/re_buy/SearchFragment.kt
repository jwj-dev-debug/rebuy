package com.yourcompany.re_buy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourcompany.re_buy.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var productRepository: ProductRepository
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ProductRepository
        productRepository = ProductRepository(requireContext())

        // Setup RecyclerView with ProductAdapter
        productAdapter = ProductAdapter(
            lifecycleOwner = viewLifecycleOwner
        )
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }

        // Setup region dropdown
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.regions,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerRegion.adapter = adapter
        }

        // Setup category dropdown
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }

        // Setup search button click listener
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // Setup checkbox listener to trigger search when toggled
        binding.cbHideSoldOut.setOnCheckedChangeListener { _, _ ->
            performSearch()
        }

        // Show all products initially
        performSearch()
    }

    private fun performSearch() {
        val query = binding.etSearchQuery.text.toString().trim()
        val regionPosition = binding.spinnerRegion.selectedItemPosition
        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        val hideSoldOut = binding.cbHideSoldOut.isChecked

        // Map spinner positions to filter values
        val region = when (regionPosition) {
            1 -> "seodaemun"  // 서대문구
            2 -> "dongdaemun"  // 동대문구
            else -> "all"      // 전체
        }

        // Get the actual category name from the spinner
        // Position 0 = "전체" (All), Position 1+ = specific categories
        val category = if (categoryPosition == 0) {
            "all"
        } else {
            binding.spinnerCategory.selectedItem.toString()
        }

        // Perform search using repository
        val results = productRepository.searchProducts(query, region, category, hideSoldOut)

        // Update RecyclerView with results
        productAdapter.updateProducts(results)

        // Update result count
        val countText = getString(R.string.products_found, results.size)
        binding.tvResultCount.text = countText

        // Show/hide empty state
        if (results.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh favorite states in case they changed in My Page
        productAdapter.refreshFavoriteStates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}