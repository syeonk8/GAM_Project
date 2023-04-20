package com.example.gam_project.adapter

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.gam_project.entity.CalendarEntity
import com.example.gam_project.others.Constants.calendar_date
import com.example.gam_project.dao.CalendarDao
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.ListItemDayBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AdapterDay(
    val tempMonth: Int,
    val dayList: MutableList<Date>,
    private val calendarDao: CalendarDao,
    private val onClickListener: (Day) -> Unit
) : RecyclerView.Adapter<AdapterDay.DayView>() {
    var selectedPosition = -1

    data class Day(val year: Int, val month: Int, val day: Int)

    inner class DayView(val binding: ListItemDayBinding) : RecyclerView.ViewHolder(binding.root){
        private var calendarEntity: CalendarEntity? = null

        fun bindCalendarEntity(calendarEntity: CalendarEntity?){
            this.calendarEntity = calendarEntity
            if (calendarEntity != null){
                binding.checkBox.visibility = View.VISIBLE
            } else {
                binding.checkBox.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {
        var binding = ListItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayView(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayView, position: Int) {

        if (selectedPosition == position) {
            holder.binding.itemDayLayout.setBackgroundColor(Color.parseColor("#eeeeee"))
        } else {
            holder.binding.itemDayLayout.setBackgroundColor(Color.WHITE)
        }

        val date = dayList[position]
        val day = Day(date.year + 1900, date.month + 1, date.date) // 선택한 일자 정보 저장
        GlobalScope.launch(Dispatchers.IO){
            val calendarEntity = calendarDao.getCalendarCheckList(date.year + 1900, date.month + 1, date.date)
            withContext(Dispatchers.Main){
                holder.bindCalendarEntity(calendarEntity)
            }
        }

        holder.binding.itemDayText.text = date.date.toString()

        holder.binding.itemDayText.setTextColor(
            when (position % 7) {
                0 -> Color.RED
                6 -> Color.BLUE
                else -> Color.BLACK
            }
        )

        if (tempMonth != dayList[position].month - 1) {
            holder.binding.itemDayText.alpha = 0.3f
            holder.binding.checkBox.alpha = 0.3f
        } else {
            holder.binding.itemDayLayout.setOnClickListener {
                onClickListener(day)

                var beforeSelectedPosition = selectedPosition
                selectedPosition = position

                notifyItemChanged(beforeSelectedPosition)
                notifyItemChanged(selectedPosition)
            }

            holder.binding.itemDayText.setOnClickListener { view ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                calendar_date = dateFormat.format(dayList[position])
                view.findNavController().navigate(R.id.dailyFragment)
            }
        }
    }

    override fun getItemCount(): Int = dayList.size
}
