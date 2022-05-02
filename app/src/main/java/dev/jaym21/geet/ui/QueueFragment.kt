package dev.jaym21.geet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.IQueueRVAdapter
import dev.jaym21.geet.adapters.QueueRVAdapter
import dev.jaym21.geet.databinding.FragmentQueueBinding
import dev.jaym21.geet.widgets.QueueDragCallback

class QueueFragment : BaseFragment(), IQueueRVAdapter {

    private var _binding: FragmentQueueBinding? = null
    private val binding: FragmentQueueBinding
        get() = _binding!!
    private var queueAdapter = QueueRVAdapter(this)
    private var touchHelper: ItemTouchHelper? = null
    private var callback: QueueDragCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBackButtonQueue.setOnClickListener {
            findNavController().navigateUp()
        }

        setUpRecyclerView()

        nowPlayingViewModel.queueData.observe(viewLifecycleOwner) {
            mainViewModel.getSongForIds(it.queue)
        }

        mainViewModel.songsForIds.observe(viewLifecycleOwner) {
            queueAdapter.submitList(it)
        }
    }


    private fun setUpRecyclerView() {
        binding.apply {
            rvQueue.adapter = queueAdapter
            rvQueue.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onPickUp(viewHolder: RecyclerView.ViewHolder) {

    }

    private fun getTouchHelper(): ItemTouchHelper {
        check(!isDetached)
        val instance = touchHelper
        if (instance != null) {
            return instance
        }
        val newCallback = QueueDragCallback(mainViewModel, nowPlayingViewModel, viewLifecycleOwner)
        val newInstance = ItemTouchHelper(newCallback)
        callback = newCallback
        touchHelper = newInstance
        return newInstance
    }
}