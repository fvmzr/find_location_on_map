package com.example.findplacesonthemap.feature.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.findplacesonthemap.feature.repository.ApiClient
import com.example.findplacesonthemap.feature.repository.model.PlacesResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MapViewModel {
    private var myCompositeDisposable: CompositeDisposable? = null
    var places = MutableLiveData<PlacesResponse>()

    fun handelReciveData(lat: Double, lng: Double) {
        Log.e("handelReciveData", "lat " + lat)
        myCompositeDisposable = CompositeDisposable()
        myCompositeDisposable?.add(
            ApiClient().requestInterface.getDetailsAddres(lat, lng)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .retry(90000)
                .subscribe(::handelResponseRecive, { e ->
                    Throwable().message
                })
        )
    }

    private fun handelResponseRecive(data: PlacesResponse) {
        places.value =data
        if (!data.cafes.isNullOrEmpty()) {
            for (i in data.cafes)
                Log.e("MapViewModel", " cofe list " + i)
        }

    }
}