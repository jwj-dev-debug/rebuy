package com.yourcompany.re_buy

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.yourcompany.re_buy.databinding.FragmentMapBinding
import com.yourcompany.re_buy.models.RecyclingCenter

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var googleMap: GoogleMap? = null
    private val allCenters = RecyclingCenter.getSeoulCenters()
    private val markerCenterMap = mutableMapOf<Marker, RecyclingCenter>()
    private var selectedCenter: RecyclingCenter? = null
    private var filteredCenters = listOf<RecyclingCenter>()
    private val selectedDistricts = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading indicator
        binding.progressLoading.visibility = View.VISIBLE

        // Initialize filtered centers with all centers
        filteredCenters = allCenters

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // Setup search functionality
        setupSearchBar()

        // Setup filter chips
        setupFilterChips()

        // Setup info card buttons
        setupInfoCardButtons()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Hide loading indicator
        binding.progressLoading.visibility = View.GONE

        // Configure map
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = true
        }

        // Set map style to light
        try {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Add markers for all recycling centers
        updateMarkers()

        // Set marker click listener
        map.setOnMarkerClickListener { marker ->
            val center = markerCenterMap[marker]
            if (center != null) {
                showCenterInfo(center)
                // Animate camera to marker
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
                true
            } else {
                false
            }
        }

        // Hide info card when clicking on map
        map.setOnMapClickListener {
            hideCenterInfo()
        }

        // Move camera to show all of Seoul
        moveCameraToSeoul()
    }

    private fun setupSearchBar() {
        // Search text change listener
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                filterCenters(query)
            }
        })

        // Clear search button
        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun setupFilterChips() {
        // Get all unique districts
        val districts = allCenters.map { it.district }.distinct().sorted()

        // Add "전체" (All) chip
        val allChip = createFilterChip("전체", true)
        allChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Uncheck all other chips
                for (i in 1 until binding.chipGroupDistricts.childCount) {
                    (binding.chipGroupDistricts.getChildAt(i) as? Chip)?.isChecked = false
                }
                selectedDistricts.clear()
                filterCenters(binding.etSearch.text?.toString() ?: "")
            }
        }
        binding.chipGroupDistricts.addView(allChip)

        // Add chips for each district
        districts.forEach { district ->
            val chip = createFilterChip(district, false)
            chip.setOnCheckedChangeListener { _, isChecked ->
                // Uncheck "전체" chip
                (binding.chipGroupDistricts.getChildAt(0) as? Chip)?.isChecked = false

                if (isChecked) {
                    selectedDistricts.add(district)
                } else {
                    selectedDistricts.remove(district)
                    // If no districts selected, check "전체"
                    if (selectedDistricts.isEmpty()) {
                        (binding.chipGroupDistricts.getChildAt(0) as? Chip)?.isChecked = true
                    }
                }
                filterCenters(binding.etSearch.text?.toString() ?: "")
            }
            binding.chipGroupDistricts.addView(chip)
        }
    }

    private fun createFilterChip(label: String, isChecked: Boolean): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            this.isChecked = isChecked
            chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), android.R.color.white)
            setTextColor(Color.parseColor("#424242"))
            checkedIcon = null
            chipStrokeWidth = 2f
            chipStrokeColor = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark)
            setChipBackgroundColorResource(android.R.color.white)
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_light))
                } else {
                    setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), android.R.color.white))
                }
            }
            if (isChecked) {
                setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_light))
            }
        }
    }

    private fun filterCenters(query: String) {
        filteredCenters = allCenters.filter { center ->
            val matchesQuery = query.isEmpty() ||
                center.name.contains(query, ignoreCase = true) ||
                center.district.contains(query, ignoreCase = true) ||
                center.address.contains(query, ignoreCase = true)

            val matchesDistrict = selectedDistricts.isEmpty() ||
                selectedDistricts.contains(center.district)

            matchesQuery && matchesDistrict
        }

        updateMarkers()

        // Show toast if no results
        if (filteredCenters.isEmpty()) {
            Toast.makeText(requireContext(), "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMarkers() {
        googleMap?.let { map ->
            // Clear existing markers
            map.clear()
            markerCenterMap.clear()

            // Add markers for filtered centers
            filteredCenters.forEach { center ->
                val position = LatLng(center.latitude, center.longitude)
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(center.name)
                        .snippet(center.district)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
                if (marker != null) {
                    markerCenterMap[marker] = center
                }
            }

            // Update camera to show filtered markers
            if (filteredCenters.isNotEmpty()) {
                moveCameraToShowMarkers(filteredCenters)
            }
        }
    }

    private fun moveCameraToSeoul() {
        moveCameraToShowMarkers(allCenters)
    }

    private fun moveCameraToShowMarkers(centers: List<RecyclingCenter>) {
        if (centers.isEmpty()) return

        googleMap?.let { map ->
            try {
                if (centers.size == 1) {
                    // If only one center, zoom to it
                    val center = centers[0]
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(center.latitude, center.longitude),
                            14f
                        )
                    )
                } else {
                    // Create bounds that include all centers
                    val boundsBuilder = LatLngBounds.Builder()
                    centers.forEach { center ->
                        boundsBuilder.include(LatLng(center.latitude, center.longitude))
                    }
                    val bounds = boundsBuilder.build()

                    // Move camera to show all markers with padding
                    val padding = 150 // padding in pixels
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                }
            } catch (e: Exception) {
                // Fallback to center of Seoul if bounds don't work
                val seoulCenter = LatLng(37.5665, 126.9780)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(seoulCenter, 11f))
            }
        }
    }

    private fun setupInfoCardButtons() {
        // Website button
        binding.btnVisitWebsite.setOnClickListener {
            selectedCenter?.let { center ->
                openWebsite(center.websiteUrl)
            }
        }

        // Call button
        binding.btnCall.setOnClickListener {
            selectedCenter?.let { center ->
                makePhoneCall(center.phone)
            }
        }

        // Directions button
        binding.btnDirections.setOnClickListener {
            selectedCenter?.let { center ->
                openDirections(center.latitude, center.longitude, center.name)
            }
        }

        // Close button
        binding.btnCloseInfo.setOnClickListener {
            hideCenterInfo()
        }
    }

    private fun showCenterInfo(center: RecyclingCenter) {
        selectedCenter = center
        binding.tvCenterName.text = center.name
        binding.tvCenterAddress.text = center.address
        binding.tvCenterPhone.text = center.phone
        binding.cardCenterInfo.visibility = View.VISIBLE
    }

    private fun hideCenterInfo() {
        binding.cardCenterInfo.visibility = View.GONE
        selectedCenter = null
    }

    private fun openWebsite(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "웹사이트를 열 수 없습니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "전화 앱을 열 수 없습니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openDirections(lat: Double, lng: Double, name: String) {
        try {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($name)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser if Google Maps not installed
            try {
                val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "길찾기를 열 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
