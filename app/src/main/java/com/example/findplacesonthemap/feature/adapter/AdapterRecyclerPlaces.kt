package com.example.findplacesonthemap.feature.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.findplacesonthemap.R
import com.example.findplacesonthemap.feature.repository.model.DetailsPlacesResponse
import com.example.findplacesonthemap.feature.repository.model.PlacesResponse
import kotlinx.android.synthetic.main.recycler_list_places.view.*

class AdapterRecyclerPlaces(val item: ArrayList<String>, val contex: Context) :
    RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(contex).inflate(R.layout.recycler_list_places, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.locality?.text = item.get(position)
    }

    override fun getItemCount(): Int {
        return item.size
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val locality = view.txt_locality
}