package com.example.gam_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.gam_project.tracking.databinding.FragmentCalendarMainBinding

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarMainBinding.inflate(inflater, container, false)
        val monthListManager = LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        val monthListAdapter = AdapterMonth()

        binding.calendarCustom.apply {
            layoutManager = monthListManager
            adapter = monthListAdapter
            scrollToPosition(Int.MAX_VALUE/2)
        }
        val snap = PagerSnapHelper()
        snap.attachToRecyclerView(binding.calendarCustom)
        val view = binding.root

        return view
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}