package com.example.edward.nyansapo.presentation.ui.change_program

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.edward.nyansapo.R
import com.edward.nyansapo.databinding.FragmentChangeProgramBinding
import com.example.edward.nyansapo.presentation.ui.attendance.AttendanceFragment
import com.example.edward.nyansapo.presentation.ui.main.*
import com.example.edward.nyansapo.util.FirebaseUtils
import com.example.edward.nyansapo.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ChangeProgramFragment : Fragment(R.layout.fragment_change_program) {
    private val TAG = "HomePageFragment"

    val TYPE_PROGRAM = 0
    val TYPE_GROUP = 1
    val TYPE_CAMP = 2

    //makes sure the spinners are not invoked the first time the view is created
    var programCheck = 0
    var groupCheck = 0
    ////////////////////////////

    lateinit var programNames: QuerySnapshot
    lateinit var groupNames: QuerySnapshot
    lateinit var campNames: QuerySnapshot

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: FragmentChangeProgramBinding

    private val viemModel: ChangeProgramViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChangeProgramBinding.bind(view)
        Log.d(TAG, "onViewCreated: ")
        setOnClickListeners()
        setItemClickListener()
        subscribeToObservers()


    }


    private fun subscribeToObservers() {
        Log.d(TAG, "subscribeToObservers: ")

        //program status
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viemModel.programsStatus.collect {
                Log.d(TAG, "subscribeToObservers: status:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        //show progress bar
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)

                        val programs = it.data!!
                        programNames = programs

                        val spinnerValue = programs.map {
                            val program = it.toObject(Program::class.java)

                            "Program: ${program?.number}"
                        }

                        val adapter = SpinnerAdapter(requireContext(), programNames, spinnerValue, {
                            deleteItem(it)

                        }) { documentReference, documentSnapshot ->
                            val program = documentSnapshot.toObject(Program::class.java)
                            editItem(TYPE_PROGRAM, documentReference, program!!)
                        }


                        binding.programNameSpinner.setAdapter(adapter)

                        setDefaultProgram()
                        startFetchingSpecificGroup()

                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                        programsIsEmpty()
                        updateProgramSharedPref()
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToast(it.exception?.message!!)
                    }
                }
            }

        }
        //////////////group status


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viemModel.groupsStatus.collect {
                Log.d(TAG, "subscribeToObservers: status:${it.status.name}")

                when (it.status) {
                    Resource.Status.LOADING -> {
                        //show progress bar
                        showProgress(true)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        val groups = it.data
                        groupNames = groups!!


                        val spinnerValue = groups.map {
                            "Group: ${it.toObject(Group::class.java).number}"
                        }
                        val adapter = SpinnerAdapter(requireContext()!!, groupNames, spinnerValue, { deleteItem(it) }) { documentReference, documentSnapshot ->
                            val group = documentSnapshot.toObject(Group::class.java)

                            editItem(TYPE_GROUP, documentReference, group!!)
                        }
                        binding.groupSpinner.setAdapter(adapter)
                        setDefaultGroup()
                        startFetchingSpecificCamp()


                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                        groupsIsEmpty()
                        updateGroupSharedPref()
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToast(it.exception?.message!!)
                    }
                }
            }

        }

        //////////////camp status


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viemModel.campStatus.collect {
                Log.d(TAG, "subscribeToObservers: status:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        //show progress bar
                        showProgress(true)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        val camps = it.data
                        campNames = camps!!

                        val spinnerValue = camps.map {
                            "Camp: ${it.toObject(Camp::class.java).number}"

                        }
                        val adapter = SpinnerAdapter(requireContext()!!, campNames!!, spinnerValue, { deleteItem(it) }) { documentReference, documentSnapshot ->
                            val camp = documentSnapshot.toObject(Camp::class.java)

                            editItem(TYPE_CAMP, documentReference, camp!!)
                        }
                        binding.campSpinner.setAdapter(adapter)
                        setDefaultCamp()
                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                        campIsEmpty()
                        updateCampSharedPref()
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToast(it.exception?.message!!)
                    }
                }
            }

        }

    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        //setting main activity toolbar to visible
//        (requireContext()!! as MainActivity2).binding.root.findViewById<Toolbar>(R.id.toolbar).isVisible = true
        //  (requireContext()!! as MainActivity2).binding.root.findViewById<Toolbar>(R.id.toolbar).title = "Home"
    }


    private fun programsIsEmpty() {
        Log.d(TAG, "programsIsEmpty: ")
        binding.programNameSpinner.adapter = null
        groupsIsEmpty()
    }

    private fun editItem(type: Int, documentReference: DocumentReference, organisation: Organisation) {
        when (type) {
            TYPE_PROGRAM -> {
                createAlertDialog("Program", "Edit program: ${organisation.number} name", documentReference, organisation)
            }
            TYPE_GROUP -> {
                createAlertDialog("Group", "Edit group: ${organisation.number} name", documentReference, organisation)
            }
            TYPE_CAMP -> {
                createAlertDialog("Camp", "Edit camp: ${organisation.number} name", documentReference, organisation)
            }
        }

    }

    private fun showToast(message: String) {

        Toasty.info(requireContext()!!, message, Toasty.LENGTH_LONG).show()

    }

    private fun deleteItem(reference: DocumentReference) {
        Log.d(TAG, "deleteItem: started deleteing item")
        showProgress(true)
        reference.delete().addOnSuccessListener {
            Log.d(TAG, "deleteItem: deletion success")

            showProgress(false)
        }

    }

    private fun setItemClickListener() {

        binding.programNameSpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (++programCheck > 1) {
                    Log.d(TAG, "onItemSelected: program spinner")
                    updateProgramSharedPref()
                    startFetchingSpecificGroup()


                }

            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: ")

            }
        })
        binding.groupSpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                Log.d(TAG, "onItemSelected:  group spinner")
                if (++groupCheck > 1) {
                    updateGroupSharedPref()
                    startFetchingSpecificCamp()


                }

            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: ")

            }
        })
        binding.campSpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {

                Log.d(TAG, "onItemSelected: camp spinner current pos: ${binding.campSpinner.selectedItemPosition}")
                if (++groupCheck > 1) {
                    //saving campId to be accessed in other screens
                    updateCampSharedPref()
                }

            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: ")

            }
        })


    }

    private fun updateProgramSharedPref() {
        Log.d(TAG, "updateProgramSharedPref: updating program shared pref")
        try {
            val program = binding.programNameSpinner.selectedProgram?.toObject(Program::class.java)
            val programID = binding.programNameSpinner.selectedProgram?.id!!
            sharedPreferences.programId = programID
            val programPos = binding.programNameSpinner.selectedItemPosition
            sharedPreferences.programPos = programPos
            Log.d(TAG, "updateProgramSharedPref: :program:$program")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setDataForDrawer()
    }

    private fun updateGroupSharedPref() {
        Log.d(TAG, "updateGroupSharedPref: updating group")

        try {
            val group = binding.groupSpinner.selectedGroup?.toObject(Group::class.java)
            val groupId = binding.groupSpinner.selectedGroup?.id
            sharedPreferences.groupId = groupId!!
            val groupPos = binding.groupSpinner.selectedItemPosition
            sharedPreferences.groupPos = groupPos
            Log.d(TAG, "updateGroupSharedPref: group:$group")

        } catch (e: Exception) {
            e.printStackTrace()
        }

        setDataForDrawer()
    }

    private fun updateCampSharedPref() {
        Log.d(TAG, "updateCampSharedPref: updating camp")


        val camp = binding.campSpinner.selectedCamp?.toObject(Camp::class.java)
        val campId = binding.campSpinner.selectedCamp?.id
        sharedPreferences.campId = campId
        val campPos = binding.campSpinner.selectedItemPosition
        sharedPreferences.campPos = campPos
        Log.d(TAG, "updateCampSharedPref: camp:$camp")

        setDataForDrawer()
    }

    private fun updateSharedPreference() {
        updateProgramSharedPref()
        updateGroupSharedPref()
        updateCampSharedPref()
    }

    private fun startFetchingSpecificCamp() {


        val programID = programNames.documents.get(binding.programNameSpinner.selectedItemPosition).id
        val groupID = groupNames.documents.get(binding.groupSpinner.selectedItemPosition).id

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viemModel.startFetchingCamps(programID, groupID)
        }

    }

    private fun campIsEmpty() {
        Log.d(TAG, "campIsEmpty: ")
        binding.campSpinner.adapter = null

    }

    private fun setDataForDrawer() {
/*        val menu = (requireContext()!! as MainActivity2).binding.navView.menu
        FirebaseUtils.firebaseAuth.currentUser.apply {
            menu.findItem(R.id.instructorNameItem).title = "${this?.displayName} "
        }


        try {
            val programName = programNames.toObjects(Program::class.java)[binding.programNameSpinner.selectedItemPosition]
            menu.findItem(R.id.programNameItem).title = "Program ${programName.number}"
        } catch (e: Exception) {
            menu.findItem(R.id.programNameItem).title = "No Program Selected"
            e.printStackTrace()
        }

        try {
            val groupName = groupNames.toObjects(Group::class.java)[binding.groupSpinner.selectedItemPosition]
            menu.findItem(R.id.groupItem).title = "Group ${groupName.number}"

        } catch (e: Exception) {
            menu.findItem(R.id.groupItem).title = "No Group Selected"
            e.printStackTrace()
        }
        try {
            val campName = campNames!!.toObjects(Camp::class.java)[binding.campSpinner.selectedItemPosition]
            Log.d(TAG, "setDataForDrawer:campName:${campName.number} : :campSize:${campNames!!.size()} ::selected position:${binding.campSpinner.selectedItemPosition} ")

            menu.findItem(R.id.campNumberItem).title = "Camp ${campName.number}"

        } catch (e: Exception) {
            menu.findItem(R.id.campNumberItem).title = "No camp selected"
            e.printStackTrace()
        }*/

    }

    private fun startFetchingSpecificGroup() {
        Log.d(TAG, "startFetchingSpecificGroup: ")

        val programID = binding.programNameSpinner.selectedProgram?.id!!
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viemModel.startFetchingGroups(programID)
        }
    }

    private fun groupsIsEmpty() {
        binding.groupSpinner.adapter = null
        campIsEmpty()
    }

    private fun setOnClickListeners() {
        binding.attendanceBtn.setOnClickListener {
            attendanceBtnClicked()
        }
        binding.createFob.setOnClickListener {

            val programPos = sharedPreferences.programPos
            val groupPos = sharedPreferences.groupPos
            val campPos = sharedPreferences.campPos

            Log.d(TAG, "onStop:programPos:$programPos: :groupPos::$groupPos::campPos::$campPos")
            createBtnClicked()

        }
    }

    private fun createBtnClicked() {
        Log.d(TAG, "createBtnClicked: ")
        findNavController().navigate(R.id.action_changeProgramFragment_to_createFragment)
    }

    private fun attendanceBtnClicked() {
        Log.d(TAG, "attendanceBtnClicked: ")
        val attendanceFragment = AttendanceFragment()
        /*      requireContext()!!
                      .supportFragmentManager
                      .beginTransaction().replace(R.id.container, attendanceFragment)
                      .addToBackStack(null).commit()
      */
    }

    /* private fun getInfoBundle(): Bundle? {
         binding.apply {
             if (binding.programNameSpinner.selectedItemPosition == AdapterView.INVALID_POSITION) {
                 showToast("Please First Create A Program")
                 return null
             }
             if (binding.groupSpinner.selectedItemPosition == AdapterView.INVALID_POSITION) {
                 showToast("Please First Create A Program")
                 return null
             }
             if (binding.campSpinner.selectedItemPosition == AdapterView.INVALID_POSITION) {
                 showToast("Please First Create A Program")
                 return null
             }
         }


         val programID = programNames.documents[binding.programNameSpinner.selectedItemPosition].id
         val groupID = groupNames.documents[binding.groupSpinner.selectedItemPosition].id
         val campID = campNames!!.documents[binding.campSpinner.selectedItemPosition].id

         val bundle = bundleOf(Constants.KEY_PROGRAM_ID to programID, Constants.KEY_GROUP_ID to groupID, Constants.KEY_CAMP_ID to campID)
         return bundle
     }*/

    fun setDefaultProgram() {
        Log.d(TAG, "setDefaultProgram: ")
        val programPos = sharedPreferences.programPos
        if (programPos == AdapterView.INVALID_POSITION || programPos >= programNames.size()) {
            updateProgramSharedPref()
        } else {
            //valid position
            binding.programNameSpinner.setSelection(programPos)
            setDataForDrawer()
        }


    }

    fun setDefaultGroup() {
        Log.d(TAG, "setDefaultGroup: ")
        val groupPos = sharedPreferences.groupPos
        Log.d(TAG, "setDefaultGroup: groupPos:$groupPos")
        if (groupPos == AdapterView.INVALID_POSITION || groupPos >= groupNames.size()) {
            updateGroupSharedPref()
        } else {
            binding.groupSpinner.setSelection(groupPos)
            setDataForDrawer()

        }

    }

    fun setDefaultCamp() {
        Log.d(TAG, "setDefaultCamp: ")
        val campPos = sharedPreferences.campPos
        Log.d(TAG, "setDefaultCamp: campPos:$campPos")
        if (campPos == AdapterView.INVALID_POSITION || campPos >= campNames.size()) {
            updateCampSharedPref()
        } else {
            binding.campSpinner.setSelection(campPos)
            setDataForDrawer()

        }
    }


    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called")
        val programPos = sharedPreferences.programPos
        val groupPos = sharedPreferences.groupPos
        val campPos = sharedPreferences.campPos

        Log.d(TAG, "onStop:programPos:$programPos: :groupPos::$groupPos::campPos::$campPos")


        //setting main activity toolbar to invisible
        //  (requireContext())!!.binding.root.findViewById<Toolbar>(R.id.toolbar).isVisible = false

        //  updateProgramSharedPref()
        // updateGroupSharedPref()
        //   updateCampSharedPref()

    }

    fun createAlertDialog(title: String, message: String, documentReference: DocumentReference, organisation: Organisation) {

        val edittext = EditText(requireContext()!!)
        edittext.setTextColor(Color.WHITE)
        edittext.setText(organisation.name)

        MaterialAlertDialogBuilder(requireContext()!!)
                .setBackground(requireContext()!!.getDrawable(R.drawable.bg_dialog)).setIcon(R.drawable.ic_edit)
                .setTitle(title).setMessage(message).setView(edittext)
                .setNegativeButton("Cancel") { dialog, which -> //
                    // what to execute on cancel
                }.setPositiveButton("Save") { dialog, which ->
                    val string = edittext.text.toString()
                    val map = mapOf("name" to string)
                    updateOrganisation(map, documentReference)

                }.show()


    }

    private fun updateOrganisation(map: Map<String, String>, documentReference: DocumentReference) {
        showProgress(true)
        Log.d(TAG, "updateOrganisation: started update")
        documentReference.set(map, SetOptions.merge()).addOnSuccessListener {
            Log.d(TAG, "updateOrganisation: update successfull")
            showProgress(false)
        }
    }

    private fun showProgress(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    val Spinner.selectedProgram: DocumentSnapshot?
        get() {
            if (binding.programNameSpinner.selectedItemPosition == -1) {
                return null
            } else {
                return programNames.documents[binding.programNameSpinner.selectedItemPosition]
            }
        }
    val Spinner.selectedGroup: DocumentSnapshot?
        get() {
            if (binding.groupSpinner.selectedItemPosition == -1) {
                return null
            } else {
                return groupNames.documents[binding.groupSpinner.selectedItemPosition]
            }
        }
    val Spinner.selectedCamp: DocumentSnapshot?
        get() {
            if (binding.campSpinner.selectedItemPosition == -1) {
                return null
            } else {
                return campNames.documents[binding.campSpinner.selectedItemPosition]
            }
        }

}