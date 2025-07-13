package com.abcg.music.ui.fragment.other

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.abcg.music.R
import com.abcg.music.databinding.FragmentCreditBinding
import com.abcg.music.utils.VersionManager
import dev.chrisbanes.insetter.Insetter

class CreditFragment : Fragment() {
    private var _binding: FragmentCreditBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCreditBinding.inflate(inflater, container, false)
        Insetter
            .builder()
            .margin(
                WindowInsetsCompat.Type.statusBars(),
            ).applyToView(binding.topAppBarLayout)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.versionTextBox.text = getString(R.string.version_format, VersionManager.getVersionName())
        binding.btGithub.setOnClickListener {
            val urlIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/ABCGop/VxMusic"),
                )
            startActivity(urlIntent)
        }
        binding.btIssue.setOnClickListener {
            val urlIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/ABCGop/VxMusic/issues"),
                )
            startActivity(urlIntent)
        }
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}