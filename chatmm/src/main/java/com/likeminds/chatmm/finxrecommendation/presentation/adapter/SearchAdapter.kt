package com.likeminds.chatmm.finxrecommendation.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.likeminds.chatmm.finxrecommendation.domain.model.FinxSmSearchApiRsp
import com.likeminds.chatmm.databinding.ItemSearchScripLmBinding

class SearchAdapter(
    private var dataList: List<FinxSmSearchApiRsp>,
    private val itemClickListener: (FinxSmSearchApiRsp) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSearchScripLmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FinxSmSearchApiRsp) {
            binding.tvScripName.text = item.getScripName()
            binding.tvExchangeName.text = "" //item.exchangeSegment
            binding.tvDesc.text = item.exchangeSegment

            itemView.setOnClickListener(View.OnClickListener() {
                itemClickListener(item)
            })
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

    fun updateData(newData: List<FinxSmSearchApiRsp>) {
        dataList = newData
        notifyDataSetChanged()
    }
}