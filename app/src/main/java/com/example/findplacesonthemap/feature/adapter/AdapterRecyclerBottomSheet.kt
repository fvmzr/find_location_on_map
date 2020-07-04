package com.example.findplacesonthemap.feature.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.findplacesonthemap.R
import com.example.findplacesonthemap.feature.repository.model.DetailsPlacesResponse
import kotlinx.android.synthetic.main.recycler_bottom_sheet.view.*

class AdapterRecyclerBottomSheet(val item: ArrayList<DetailsPlacesResponse>, val context: Context) :
    RecyclerView.Adapter<ViewHolderBottomSheet>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBottomSheet {
        return ViewHolderBottomSheet(
            LayoutInflater.from(context).inflate(R.layout.recycler_bottom_sheet, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolderBottomSheet, position: Int) {
        holder?.txt_rcv_bottom_sheet?.text = item.get(position).title
    }

    override fun getItemCount(): Int {
        return item.size
    }
}

class ViewHolderBottomSheet(view: View) : RecyclerView.ViewHolder(view) {
    val txt_rcv_bottom_sheet = view.txt_rcv_bottom_sheet
}