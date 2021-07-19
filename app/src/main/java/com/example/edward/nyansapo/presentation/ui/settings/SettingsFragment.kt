package com.example.edward.nyansapo.presentation.ui.settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentSettingsBinding
import com.example.edward.nyansapo.presentation.ui.main.*
import com.firebase.ui.auth.AuthUI
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment() : Fragment(R.layout.fragment_settings) {
    @Inject
    lateinit var sharePref: SharedPreferences
    private lateinit var binding: FragmentSettingsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        setDefaults()
        setOnClickListeners()
        setUpToolBar()
    }

    private fun setUpToolBar() {
        binding.toolbar.root.inflateMenu(R.menu.overflow_menu)
        binding.toolbar.root.title = "Settings"
        binding.toolbar.root.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun setOnClickListeners() {
        binding.apply {
            ivSwitchCurriculum.setOnClickListener {
                switchCurriculumClicked()
            }
            ivExportData.setOnClickListener {
                exportDataClicked()
            }
            ivAppTutorial.setOnClickListener {
                appTutorialClicked()
            }
            ivLogout.setOnClickListener {
                logOutClicked()
            }
        }
    }

    private fun switchCurriculumClicked() {
        if (sharePref.curriculum == 0) {
            sharePref.curriculum = 1
        } else {
            sharePref.curriculum = 0

        }
    }

    private fun exportDataClicked() {
    }

    private fun appTutorialClicked() {
    }

    private fun logOutClicked() {
        AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
            requireActivity().finish()
        }
    }

    private fun setDefaults() {
        binding.apply {
            tvInstructorName.text = sharePref.instructorName
            tvCounty.text = sharePref.county
            tvSchoolName.text = sharePref.schoolName
            tvGroup.text = "Group: ${sharePref.groupPos}"
            tvCamp.text = "Camp: ${sharePref.campPos}"
            tvCurriculumLable.text = "${sharePref.curriculum} Curriculumn"
            tvAppVersion.text = getAppVersion()
        }
    }

    private fun getAppVersion(): String? {

        val manager = context?.packageManager
        val info = manager?.getPackageInfo(
                context?.packageName, 0
        )

        val versionName = info?.versionName
        val versionNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info?.longVersionCode
        } else {
            info?.versionCode
        }
        return versionName
    }
}