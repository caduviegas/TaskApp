package com.example.taskapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskapp.R
import com.example.taskapp.data.model.Status
import com.example.taskapp.data.model.Task
import com.example.taskapp.databinding.FragmentFormTaskBinding
import com.example.taskapp.util.FirebaseHelper
import com.example.taskapp.util.initToolbar
import com.example.taskapp.util.showBottomSheet

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var task: Task
    private val args: FormTaskFragmentArgs by navArgs()
    private val viewModel: TaskViewModel by activityViewModels()
    private var status: Status = Status.TODO
    private var newTask: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        getArgs()
        initListerners()
    }

    private fun initListerners() {
        binding.btnSave.setOnClickListener {
            validateData()
        }
        binding.rgStatus.setOnCheckedChangeListener { _, id ->
            status = when (id) {
                R.id.rbTodo -> Status.TODO
                R.id.rbDoing -> Status.DOING
                else -> Status.DONE
            }
        }
    }

    private fun validateData() {
        val description = binding.edtDescription.text.toString().trim()

        if (description.isNotEmpty()) {
            binding.progressBar.isVisible = true
            if (newTask) task = Task()
            task.description = description
            task.status = status
            saveTask()

        } else {
            showBottomSheet(message = getString(R.string.description_empty_form_task_fragment))
        }
    }

    private fun saveTask() {
        FirebaseHelper.getDatabase()
            .child("tasks")
            .child(FirebaseHelper.getIdUser())
            .child(task.id)
            .setValue(task).addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        R.string.text_save_success_form_task_fragment,
                        Toast.LENGTH_SHORT
                    ).show()

                    if (newTask) {// Nova tarefa
                        findNavController().popBackStack()
                    } else {
                        // Editando tarefa
                        viewModel.setUpdateTask(task)
                        binding.progressBar.isVisible = false

                    }
                } else {
                    binding.progressBar.isVisible = false
                    showBottomSheet(
                        message = getString(R.string.error_generic)
                    )
                }
            }
    }

    private fun configTask() {
        newTask = false
        status = task.status
        binding.textToolbar.setText(R.string.text_toolbar_update_sucess_form_task_fragment)
        binding.edtDescription.setText(task.description)
        setStatus()

    }

    private fun setStatus() {
        val id = when (task.status) {
            Status.TODO -> R.id.rbTodo
            Status.DOING -> R.id.rbDoing
            else -> R.id.rbDone
        }
        binding.rgStatus.check(id)
    }

    private fun getArgs() {
        args.task.let {
            if (it != null) {
                this.task = it

                configTask()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}