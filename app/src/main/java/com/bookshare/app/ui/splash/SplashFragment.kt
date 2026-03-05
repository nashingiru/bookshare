package com.bookshare.app.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bookshare.app.R
import com.bookshare.app.databinding.FragmentSplashBinding
import com.bookshare.app.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateLogo()
        navigateAfterDelay()
    }

    private fun animateLogo() {
        binding.ivLogo.alpha = 0f
        binding.tvAppName.alpha = 0f
        binding.tvTagline.alpha = 0f

        binding.ivLogo.animate().alpha(1f).setDuration(800).start()
        binding.tvAppName.animate().alpha(1f).setDuration(800).setStartDelay(400).start()
        binding.tvTagline.animate().alpha(1f).setDuration(800).setStartDelay(700).start()
    }

    private fun navigateAfterDelay() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            if (authViewModel.isLoggedIn()) {
                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
