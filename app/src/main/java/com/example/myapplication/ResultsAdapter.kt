package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter(private var resultsList: List<String>) :
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    // ViewHolder class to hold the views for each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewResult: TextView = itemView.findViewById(R.id.textViewResult)
    }

    // Inflate the item layout and create the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result, parent, false)
        return ViewHolder(itemView)
    }

    // Bind data to the views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resultName = resultsList[position]
        holder.textViewResult.text = resultName

        holder.itemView.setOnClickListener {
            // Notify MainActivity of the selected name
            (it.context as? MainActivity)?.onSearchResultSelected(resultName)
        }
    }

    // Return the size of your data set
    override fun getItemCount(): Int = resultsList.size

    // Update the data in the adapter
    fun updateData(newResults: List<String>) {
        resultsList = newResults
        notifyDataSetChanged()
    }
}
