package com.example.gam_project.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gam_project.adapter.AdapterMemo
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.FragmentMyRecordBinding

import com.example.gam_project.viewmodel.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyRecordFragment : Fragment(),  SearchView.OnQueryTextListener {
    private val CalendarViewModel : CalendarViewModel by viewModels()
    private var _binding: FragmentMyRecordBinding? = null
    private val binding get() = _binding!!
    private var selectedOption: Int = 1 // 추가된 변수
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
    ): View? {
        _binding = FragmentMyRecordBinding.inflate(inflater, container, false)

        binding.searchCustom.layoutManager = LinearLayoutManager(requireContext())
        binding.searchCustom.adapter = memoListAdapter

        //검색
        binding.searchView.setOnQueryTextListener(this)

        radioBtnChange(1)

        //라디오 버튼 클릭 이벤트 검색
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.newButton -> { //최신순
                    selectedOption = 1
                    radioBtnChange(1)
                }
                R.id.oldButton -> { //오래된순
                    selectedOption = 2
                    radioBtnChange(2)
                }
                R.id.starButton -> { //별점순
                    selectedOption = 3
                    radioBtnChange(3)
                }
            }
        }
        val view = binding.root

        return view
    }

    private fun radioBtnChange(s_option: Int) {
        when (s_option) {
            1 -> {
                binding.newButton.isSelected = true
                binding.oldButton.isSelected = false
                binding.starButton.isSelected = false
                CalendarViewModel.allDataDesc.observe(viewLifecycleOwner, Observer { memoListAdapter.setData(it) })
            }
            2 -> {
                binding.newButton.isSelected = false
                binding.oldButton.isSelected = true
                binding.starButton.isSelected = false
                CalendarViewModel.allDataAsc.observe(viewLifecycleOwner, Observer { memoListAdapter.setData(it) })
            }
            3 -> {
                binding.newButton.isSelected = false
                binding.oldButton.isSelected = false
                binding.starButton.isSelected = true
                CalendarViewModel.allDataRating.observe(viewLifecycleOwner, Observer { memoListAdapter.setData(it) })
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        // 서치뷰 검색버튼 클릭 시
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val searchQuery = "%$newText%"
        when(selectedOption){
            1 -> {
                CalendarViewModel.getSearchDataDesc(searchQuery).observe(viewLifecycleOwner, androidx.lifecycle.Observer { memoListAdapter.setData(it)})
            }
            2 -> {
                CalendarViewModel.getSearchDataAsc(searchQuery).observe(viewLifecycleOwner, androidx.lifecycle.Observer { memoListAdapter.setData(it)})
            }
            3 -> {
                CalendarViewModel.getSearchDataRating(searchQuery).observe(viewLifecycleOwner, androidx.lifecycle.Observer { memoListAdapter.setData(it)})
            }
        }
        //검색된 상태에서 정렬
        //최신순
        binding.newButton.setOnClickListener {
            CalendarViewModel.getSearchDataDesc(searchQuery).observe(viewLifecycleOwner, androidx.lifecycle.Observer { memoListAdapter.setData(it)})
        }
        //오래된순
        binding.oldButton.setOnClickListener {
            CalendarViewModel.getSearchDataAsc(searchQuery).observe(viewLifecycleOwner, androidx.lifecycle.Observer { memoListAdapter.setData(it)})
        }
        //별점순
        binding.starButton.setOnClickListener {
            CalendarViewModel.getSearchDataRating(searchQuery).observe(viewLifecycleOwner, androidx.lifecycle.Observer { memoListAdapter.setData(it)})
        }
        return true
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}