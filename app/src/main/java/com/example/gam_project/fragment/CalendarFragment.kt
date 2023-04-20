package com.example.gam_project.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.gam_project.adapter.AdapterMemo
import com.example.gam_project.adapter.AdapterMonth
import com.example.gam_project.dao.CalendarDao
import com.example.gam_project.db.GamDatabase
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.FragmentCalendarBinding
import com.example.gam_project.viewmodel.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class CalendarFragment : Fragment() {
    private lateinit var calendarDao: CalendarDao
    private val CalendarViewModel: CalendarViewModel by viewModels()
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val memoListAdapter: AdapterMemo by lazy {
        AdapterMemo() {
            val arguments = bundleOf("entity" to it)
            findNavController().navigate(R.id.detailPageFragment, args = arguments)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        calendarDao = GamDatabase.getDatabase(requireContext()).getCalendarDao()

        val monthListManager =
            LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)

        val monthListAdapter = AdapterMonth(calendarDao) {
            CalendarViewModel.setSelectedDate(Calendar.getInstance().apply {
                set(it.year, it.month - 1, it.day)
            })
        }

        binding.memoCustom.layoutManager = LinearLayoutManager(requireContext())
        binding.memoCustom.adapter = memoListAdapter

        CalendarViewModel.entities.observe(viewLifecycleOwner) {
            memoListAdapter.setData(it)
        }

        binding.calendarCustom.apply {
            layoutManager = monthListManager
            adapter = monthListAdapter
            scrollToPosition(Int.MAX_VALUE / 2)
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