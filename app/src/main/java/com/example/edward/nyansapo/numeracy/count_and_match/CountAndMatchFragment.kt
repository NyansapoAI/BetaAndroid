package com.example.edward.nyansapo.numeracy.count_and_match

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentCountAndMatchBinding
import com.example.edward.nyansapo.wrappers.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CountAndMatchFragment : Fragment(R.layout.fragment_count_and_match) {
    private lateinit var binding: FragmentCountAndMatchBinding

    @Inject
    lateinit var requestManager: RequestManager
    private val viewModel: CountAndMatchViewModel by viewModels()

    private var numberOfSelectedBalls = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCountAndMatchBinding.bind(view)
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getCountAndMatch.collect {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            setClickListenersForChoices()
                            initUI()
                        }
                        Resource.Status.ERROR -> {
                            showToastInfo(it.exception?.message!!)
                        }
                    }
                }
            }
        }
    }

    private fun setClickListenersForChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, number ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.setOnClickListener {
                choiceClicked(number)
            }

        }


        binding.skipTxtView.setOnClickListener {
            findNavController().navigate(R.id.action_countAndMatchFragment_to_numberRecognition2Fragment)
        }
    }

    private fun choiceClicked(number: Int) {
        if (number == viewModel.getBalls()) {
            viewModel.correctCount++
            showToastInfo("Answer correct")
        } else {
            showToastInfo("Answer Wrong")
        }
        goToNext()
    }

    private fun goToNext() {
        refresh()
        viewModel.counter++
        displayBalls()

    }

    private fun refresh() {
        for (i in 1..viewModel.getBalls()) {
            hideBall(i)
        }
        disableChoices()
    }

    private fun hideBall(i: Int) {
        val imageView = binding.root.findViewWithTag<ImageView>("ball_$i")

        imageView.isVisible = false
        requestManager.load(R.mipmap.ball_unselected).into(imageView)

    }

    private fun initUI() {
        setChoices()
        displayBalls()
    }

    private fun setChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, i ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.text = i.toString()

        }
    }

    private fun displayBalls() {
        for (i in 1..viewModel.getBalls()) {
            showBall(i)
        }
    }

    private fun showBall(i: Int) {
        val imageView = binding.root.findViewWithTag<ImageView>("ball_$i")

        imageView.isVisible = true
        requestManager.load(R.mipmap.ball_unselected).into(imageView)

        imageView.setOnClickListener {
            requestManager.load(R.mipmap.ball_selected).into(imageView)
            numberOfSelectedBalls++
            if (numberOfSelectedBalls == viewModel.getBalls()) {
                enableChoices()
            }

        }
    }

    private fun enableChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, i ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.isEnabled = true
        }
    }

    private fun disableChoices() {
        viewModel.getCountAndMatch.value.data!!.forEachIndexed { index, i ->
            val textView = binding.root.findViewWithTag<TextView>("choice_${index + 1}")
            textView.isEnabled = false
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }
}