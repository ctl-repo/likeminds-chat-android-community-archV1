package com.likeminds.chatmm.buysellwidget.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.likeminds.chatmm.buysellwidget.domain.model.Response
import com.likeminds.chatmm.databinding.ItemSearchScripLmBinding

class SearchAdapter(
    private var dataList: List<Response>,
    private val itemClickListener: (Response) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSearchScripLmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Response) {
            binding.tvDesc.text = item.secDesc
            binding.tvScripName.text = item.secName
            binding.tvExchangeName.text = item.exchangeSegment
            itemView.setOnClickListener {
                itemClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSearchScripLmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newData: List<Response>) {
        dataList = newData
        notifyDataSetChanged()
    }
}