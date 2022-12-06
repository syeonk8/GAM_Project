package com.example.gam_project

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gam_project.tracking.R
import kotlinx.android.synthetic.main.item_schedule.view.*
import java.util.*

class RecyclerViewAdapter(val calenderFragment: CalenderFragment) : RecyclerView.Adapter<ViewHolderHelper>() {
    val baseCalendar = BaseCalendar()

    init {
        baseCalendar.initBaseCalendar {
            refreshView(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHelper {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ViewHolderHelper(view)
    }

    override fun getItemCount(): Int {
        return BaseCalendar.LOW_OF_CALENDAR * BaseCalendar.DAYS_OF_WEEK
    }

    override fun onBindViewHolder(holder: ViewHolderHelper, position: Int) {

        if (position % BaseCalendar.DAYS_OF_WEEK == 0) holder.containerView.tv_date.setTextColor(Color.parseColor("#ff1200"))
        else if (position % BaseCalendar.DAYS_OF_WEEK == 6) holder.containerView.tv_date.setTextColor(Color.parseColor("#0c70f2"))
        else holder.containerView.tv_date.setTextColor(Color.parseColor("#676d6e"))

        if (position < baseCalendar.prevMonthTailOffset
            || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate) {
            holder.containerView.tv_date.alpha = 0.3f
        } else {
            holder.containerView.tv_date.alpha = 1f
        }
        holder.containerView.tv_date.text = baseCalendar.data[position].toString()
    }

    fun changeToPrevMonth() {
        baseCalendar.changeToPrevMonth {
            refreshView(it)
        }
    }

    fun changeToNextMonth() {
        baseCalendar.changeToNextMonth {
            refreshView(it)
        }
    }

    private fun refreshView(calendar: Calendar) {
        notifyDataSetChanged()
        calenderFragment.refreshCurrentMonth(calendar)
    }
}