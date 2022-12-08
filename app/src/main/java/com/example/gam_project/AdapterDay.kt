package com.example.gam_project

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.gam_project.tracking.databinding.ListItemDayBinding
import java.util.*

class AdapterDay(val tempMonth:Int, val dayList: MutableList<Date>): RecyclerView.Adapter<AdapterDay.DayView>() {
    val ROW = 6

    inner class DayView(val binding: ListItemDayBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {
        var binding = ListItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayView(binding)
    }

    override fun onBindViewHolder(holder: DayView, position: Int) {
        holder.binding.itemDayLayout.setOnClickListener {
            Toast.makeText(holder.binding.root.context, "${dayList[position]}", Toast.LENGTH_SHORT).show()
        }
        holder.binding.itemDayText.text = dayList[position].date.toString()

        holder.binding.itemDayText.setTextColor(when(position % 7) {
            0 -> Color.RED
            6 -> Color.BLUE
            else -> Color.BLACK
        })

        if(tempMonth != dayList[position].month - 1) {
            holder.binding.itemDayText.alpha = 0.3f
        }
    }

    override fun getItemCount(): Int {
        return ROW * 7
    }
}