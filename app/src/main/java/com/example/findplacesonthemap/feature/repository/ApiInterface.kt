package com.example.findplacesonthemap.feature.repository


import com.example.findplacesonthemap.core.Const
import com.example.findplacesonthemap.feature.repository.model.PlacesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterface {
    @GET(Const.ServiceType.Place_Details)
    fun getDetailsAddres(
        @Path("lat") lat: Double,
        @Path("long") long: Double
    ): Observable<PlacesResponse>

}