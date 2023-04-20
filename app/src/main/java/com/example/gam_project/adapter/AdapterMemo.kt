package com.example.gam_project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.tracking.databinding.ListItemMemoBinding

class AdapterMemo(private val onClickListener: (entity: CalendarEntity) -> Unit) :
    RecyclerView.Adapter<AdapterMemo.MemoView>() {
    private var memoList = emptyList<CalendarEntity>()

    inner class MemoView(val binding: ListItemMemoBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var calendarEntity: CalendarEntity

        fun bind(calendarEntity: CalendarEntity) {
            binding.calendarEntity = calendarEntity
            binding.root.setOnClickListener {
                onClickListener(calendarEntity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoView {
        var binding =
            ListItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoView(binding)
    }

    override fun onBindViewHolder(holder: MemoView, position: Int) {
        val entity = memoList[position]
        holder.bind(entity)
    }

    override fun getItemCount(): Int {
        return memoList.size
    }

    fun setData(calendarEntity: List<CalendarEntity>) {
        memoList = calendarEntity
        notifyDataSetChanged()
    }
}