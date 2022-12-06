package com.example.gam_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
//import com.example.gam_project.tracking.R
//import com.example.gam_project.tracking.databinding.ActivityCalenderBinding
import com.example.gam_project.tracking.databinding.FragmentCalenderBinding
import kotlinx.android.synthetic.main.fragment_calender.*
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalenderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalenderFragment : Fragment() {
    // TODO: Rename and change types of parameters
    /*private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calender, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalenderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalenderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
    lateinit var scheduleRecyclerViewAdapter: RecyclerViewAdapter
    private var _binding: FragmentCalenderBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalenderBinding.inflate(inflater, container, false)
        initView()
        val view = binding.root

        return view
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleRecyclerViewAdapter = RecyclerViewAdapter(this)

        binding.rvSchedule.layoutManager = GridLayoutManager(requireContext(), BaseCalendar.DAYS_OF_WEEK)
        binding.rvSchedule.adapter = scheduleRecyclerViewAdapter
        binding.rvSchedule.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL))
        binding.rvSchedule.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        binding.tvPrevMonth.setOnClickListener {
            scheduleRecyclerViewAdapter.changeToPrevMonth()
        }

        binding.tvNextMonth.setOnClickListener {
            scheduleRecyclerViewAdapter.changeToNextMonth()
        }
    }*/

    fun initView() {

        scheduleRecyclerViewAdapter = RecyclerViewAdapter(this)

        binding.rvSchedule.layoutManager = GridLayoutManager(getContext(), BaseCalendar.DAYS_OF_WEEK)
        binding.rvSchedule.adapter = scheduleRecyclerViewAdapter
        binding.rvSchedule.addItemDecoration(DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL))
        binding.rvSchedule.addItemDecoration(DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL))

        binding.tvPrevMonth.setOnClickListener {
            scheduleRecyclerViewAdapter.changeToPrevMonth()
        }

        binding.tvNextMonth.setOnClickListener {
            scheduleRecyclerViewAdapter.changeToNextMonth()
        }
    }

    fun refreshCurrentMonth(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy MM", Locale.KOREAN)
        binding.tvCurrentMonth.text = sdf.format(calendar.time)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}