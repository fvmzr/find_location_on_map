package com.example.findplacesonthemap.feature.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findplacesonthemap.R
import com.example.findplacesonthemap.feature.adapter.AdapterRecyclerBottomSheet
import com.example.findplacesonthemap.feature.adapter.AdapterRecyclerPlaces
import com.example.findplacesonthemap.feature.adapter.OnItemClickListener
import com.example.findplacesonthemap.feature.adapter.addOnItemClickListener
import com.example.findplacesonthemap.feature.repository.model.DetailsPlacesResponse
import com.example.findplacesonthemap.feature.repository.model.PlacesResponse
import com.example.findplacesonthemap.feature.viewmodel.MapViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import io.reactivex.Observable
import io.reactivex.Observer
import kotlinx.android.synthetic.main.bottom_sheet.*


class MapView : AppCompatActivity() {
    private var mapboxMap: MapboxMap? = null
    private lateinit var mapViewModel: MapViewModel
    private val listLocality = ArrayList<String>()
    private lateinit var adaterPlaces: AdapterRecyclerPlaces
    private lateinit var adaterBottomSheet: AdapterRecyclerBottomSheet
    private lateinit var layoutBottomSheet: LinearLayout
    private lateinit var titleBotomSheet: TextView
    private lateinit var bottonClose: ImageView
    private var listBottomSheets: ArrayList<DetailsPlacesResponse> = ArrayList()
    private var placesType = MutableLiveData<Int>()
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapBox_tocken))

        setContentView(R.layout.activity_main)
        createMapFragment(savedInstanceState)
        mapViewModel = MapViewModel()

        layoutBottomSheet = findViewById(R.id.bottom_sheet_use)
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)

        titleBotomSheet = findViewById(R.id.txt_name_place)
        bottonClose = findViewById(R.id.img_btn_sheet_close)
        bottonClose.setOnClickListener {
            sheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    private fun createMapFragment(savedInstanceState: Bundle?) {
        var mapFragment: SupportMapFragment
        if (savedInstanceState == null) {

            // Create fragment
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

            // Build a Mapbox map
            val options = MapboxMapOptions.createFromAttributes(this, null)
            options.camera(
                CameraPosition.Builder()
                    .zoom(15.0)
                    .build()
            )

            // Create map fragment
            mapFragment = SupportMapFragment.newInstance(options)

            // Add map fragment to parent container
            transaction.add(
                R.id.location_frag_container,
                mapFragment,
                "com.mapbox.map"
            )
            transaction.commit()
        } else {
            mapFragment =
                (supportFragmentManager.findFragmentByTag("com.mapbox.map") as SupportMapFragment?)!!
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync { mapboxMap ->
                this.mapboxMap = mapboxMap
                mapboxMap.setStyle(Style.OUTDOORS, object : Style.OnStyleLoaded {
                    override fun onStyleLoaded(style: Style) {
                        enableLocationComponent(style)

                        changelocation()
                    }
                })
            }
        }

        addItemToList()
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {

        val customLocationComponentOptions = LocationComponentOptions.builder(this)
            .trackingGesturesManagement(true)
            .accuracyColor(
                ContextCompat.getColor(
                    this,
                    R.color.mapbox_blue
                )
            )
            .build()

        val locationComponentActivationOptions =
            LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

        mapboxMap!!.locationComponent.apply {

            activateLocationComponent(locationComponentActivationOptions)

            isLocationComponentEnabled = true

            cameraMode = CameraMode.TRACKING

            renderMode = RenderMode.NORMAL
        }
    }

    private fun changelocation() {
        mapboxMap!!.addOnMapClickListener { point ->
            Toast.makeText(
                this@MapView,
                String.format("User clicked at: %s", point.toString()),
                Toast.LENGTH_SHORT
            ).show()

            mapViewModel.handelReciveData(point.latitude, point.longitude)

            mapViewModel.places.observe(this, object : androidx.lifecycle.Observer<PlacesResponse> {
                override fun onChanged(p: PlacesResponse?) {
                    log("cafee :" + p?.cafes)
                    placesType.observe(this@MapView, object : androidx.lifecycle.Observer<Int> {
                        override fun onChanged(t: Int?) {

                            when (t) {
                                1 -> {
                                    listBottomSheets.clear()
                                    if (!p?.cafes.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.cafes)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.cafes)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }

                                }
                                2 -> {
                                    listBottomSheets.clear()
                                    if (!p?.restaurants.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.restaurants)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.restaurants)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                3 -> {
                                    listBottomSheets.clear()
                                    if (!p?.parks.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.parks)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.parks)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                4 -> {
                                    listBottomSheets.clear()
                                    if (!p?.shopping_malls.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.shopping_malls)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.shopping_malls)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                5 -> {
                                    listBottomSheets.clear()
                                    if (!p?.mosques.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.mosques)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.mosques)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                6 -> {
                                    listBottomSheets.clear()
                                    if (!p?.pharmacies.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.pharmacies)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.pharmacies)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                7 -> {
                                    listBottomSheets.clear()
                                    if (!p?.hospitals.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.hospitals)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.hospitals)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                8 -> {
                                    listBottomSheets.clear()
                                    if (!p?.schools.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.schools)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.schools)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                9 -> {
                                    listBottomSheets.clear()
                                    if (!p?.gyms.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.gyms)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.gyms)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                10 -> {
                                    listBottomSheets.clear()
                                    if (!p?.bookstores.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.bookstores)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.bookstores)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                11 -> {
                                    listBottomSheets.clear()
                                    if (!p?.flower_shops.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.flower_shops)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.flower_shops)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                12 -> {
                                    listBottomSheets.clear()
                                    if (!p?.libraries.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.libraries)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.libraries)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                13 -> {
                                    listBottomSheets.clear()
                                    if (!p?.hotels.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.hotels)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.hotels)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                                14 -> {
                                    listBottomSheets.clear()
                                    if (!p?.parkings.isNullOrEmpty()) {
                                        listBottomSheets.addAll(p!!.parkings)
                                        createRecyclerSheet(listBottomSheets)
                                        adaterBottomSheet.notifyDataSetChanged()
                                        titleBotomSheet.text = getString(R.string.parkings)
                                        sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                                    } else {
                                        listBottomSheets.clear()
                                        titleBotomSheet.text = getString(R.string.not_find)
                                    }
                                }
                            }
                        }
                    })
                }
            })

            true
        }
    }

    private fun addItemToList() {
        listLocality.clear()
        listLocality.add(getString(R.string.parkings))
        listLocality.add(getString(R.string.hotels))
        listLocality.add(getString(R.string.libraries))
        listLocality.add(getString(R.string.flower_shops))
        listLocality.add(getString(R.string.bookstores))
        listLocality.add(getString(R.string.gyms))
        listLocality.add(getString(R.string.schools))
        listLocality.add(getString(R.string.hospitals))
        listLocality.add(getString(R.string.pharmacies))
        listLocality.add(getString(R.string.mosques))
        listLocality.add(getString(R.string.shopping_malls))
        listLocality.add(getString(R.string.parks))
        listLocality.add(getString(R.string.restaurants))
        listLocality.add(getString(R.string.cafes))

        createRecycler()
    }

    private fun createRecycler() {
        var rcv_locality = findViewById<RecyclerView>(R.id.rcv_locality)
        rcv_locality?.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        adaterPlaces = AdapterRecyclerPlaces(listLocality, this)
        rcv_locality?.adapter = adaterPlaces

        rcv_locality.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                log("item name : " + listLocality.get(position))
                when (listLocality.get(position)) {
                    getString(R.string.cafes) -> placesType.value = 1
                    getString(R.string.restaurants) -> placesType.value = 2
                    getString(R.string.parks) -> placesType.value = 3
                    getString(R.string.shopping_malls) -> placesType.value = 4
                    getString(R.string.mosques) -> placesType.value = 5
                    getString(R.string.pharmacies) -> placesType.value = 6
                    getString(R.string.hospitals) -> placesType.value = 7
                    getString(R.string.schools) -> placesType.value = 8
                    getString(R.string.gyms) -> placesType.value = 9
                    getString(R.string.bookstores) -> placesType.value = 10
                    getString(R.string.flower_shops) -> placesType.value = 11
                    getString(R.string.libraries) -> placesType.value = 12
                    getString(R.string.hotels) -> placesType.value = 13
                    getString(R.string.parkings) -> placesType.value = 14
                }
            }
        })
    }

    private fun createRecyclerSheet(list: ArrayList<DetailsPlacesResponse>) {
        var rcv_bottom_sheet = findViewById<RecyclerView>(R.id.rcv_bottom_sheet)
        rcv_bottom_sheet?.layoutManager = LinearLayoutManager(this)

        adaterBottomSheet = AdapterRecyclerBottomSheet(list, this)
        rcv_bottom_sheet?.adapter = adaterBottomSheet
    }

    private fun log(message: String) {
        Log.e(TAG, message)
    }

}


