package com.example.gam_project.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gam_project.others.Constants.image_view_state
import com.example.gam_project.others.Constants.polyline_color
import com.example.gam_project.LoginActivity
import com.example.gam_project.tracking.R
import com.example.gam_project.tracking.databinding.FragmentMyPageBinding
import com.example.gam_project.viewmodel.MyPageViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.AndroidEntryPoint
import dev.sasikanth.colorsheet.ColorSheet

@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userApiClient: UserApiClient
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageSwitch : Switch

    private val MyPageViewModel: MyPageViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            //Code to display the selected image in the ImageView
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebase_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Inflate the layout for this fragment
        //val view = inflater.inflate(R.layout.fragment_my_page, container, false)

        val acct = GoogleSignIn.getLastSignedInAccount(requireContext())
        val displayName = acct?.displayName
        val email = acct?.email
        val imgProfile = view.findViewById<ImageView>(R.id.img_profile)
        imgProfile.setOnClickListener {onImageViewClicked(it)}
        val button = view.findViewById<Button>(R.id.colorButton)
        button.setOnClickListener {
            showColorSheet()
        }

        // Display the user's name in the TextView
        view.findViewById<TextView>(R.id.text_username).text = "환영합니다. $displayName 님"
        // Display the user's email in the TextView
        view.findViewById<TextView>(R.id.text_email).text = "계정: $email"
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener { onLogoutClicked() }

        MyPageViewModel.totalDistanceTravelled.observe(viewLifecycleOwner){
            it ?: return@observe
            binding.totalDistanceTextView.text = String.format("총 거리: %.2fm", it)
        }

        MyPageViewModel.totalRecord.observe(viewLifecycleOwner){
            it ?: return@observe
            binding.totalRecord.text = String.format("기록한 게시글 수: %d개", it)
        }

        imageSwitch = binding.imageSwitch

        imageSwitch.setOnCheckedChangeListener { _, isChecked ->
            image_view_state = isChecked
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imgProfile = view.findViewById<ImageView>(R.id.img_profile)
        imgProfile.setOnClickListener { onImageViewClicked(it) }
        val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                //Code to display the selected image in the ImageView
            }
        }
    }

    private fun showColorSheet() {
        val palette = resources.getIntArray(R.array.color_palette)
        val colorSheet = ColorSheet().colorPicker(
                colors = palette,
                listener = { color ->
                       /* val mapsFragment =
                            requireActivity().supportFragmentManager.findFragmentById(R.id.mapsFragment) as? MapsFragment
                        mapsFragment?.setPolylineColor(color)*/

                    polyline_color = color

                }
            )
        colorSheet.show(requireFragmentManager())
    }




    // Function that handle the logout click
    private fun onLogoutClicked() {
        //Clear user data and go back to login screen
        clearUserData()
        firebaseAuth.signOut() // Sign out from the firebase account
        googleSignInClient.signOut() // Sign out from the google account
        //UserApiClient.logoutWithKakao() //Sign out from the Kakao account
        goToLoginScreen()
    }

    // Get username from storage
    private fun getUsernameFromStorage(): String {
        //TODO: replace this with the actual implementation of retrieving the user's name from storage
        return "User"
    }

    // Clear user data
    private fun clearUserData() {
        //TODO: replace this with the actual implementation of clearing the user's data from storage
    }

    // Go to login screen
    private fun goToLoginScreen() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun onImageViewClicked(view: View) {
        //Code to open device's gallery or camera to choose a picture
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }

        pickImage.launch(intent)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}