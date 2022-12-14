package com.example.gam_project

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.gam_project.MapsActivity
import com.example.gam_project.tracking.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.gam_project.tracking.databinding.ActivityMainBinding

import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.common.model.AuthErrorCause.*
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity(), View.OnClickListener {


    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth
    //google client
    private lateinit var googleSignInClient: GoogleSignInClient

    //private const val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 99
    private lateinit var kakaoLoginButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        kakaoLoginButton = findViewById(R.id.kakao_login_button)
        val context = this
        kakaoLoginButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // ????????? ??????????????? ???????????? ????????? ???????????? oAuthToken ??? ????????? ??? ??????.
                    val oAuthToken = UserApiClient.loginWithKakao(context)
                    Log.d("LoginActivity", "beanbean > $oAuthToken")
                } catch (error: Throwable) {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        Log.d("LoginActivity", "???????????? ??????????????? ??????")
                    } else {
                        Log.e("LoginActivity", "?????? ?????? ??????", error)
                    }
                }
            }
        }




        //***************************************************************************
        //btn_googleSignIn.setOnClickListener (this) // ?????? ????????? ??????
        btn_googleSignIn.setOnClickListener {signIn()}

        //Google ????????? ?????? ??????. requestIdToken ??? Email ??????
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebase_web_client_id))
            //'R.string.default_web_client_id' ?????? ????????? ??????????????? ???????????? ??????????????? ?????????.
            //?????? ???????????? ?????? ?????? ????????? ??????????????? ???????????? ????????? ???????????? ?????????.
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //firebase auth ??????
        firebaseAuth = FirebaseAuth.getInstance()
    }

    // onStart. ????????? ?????? ?????? ?????? ???????????? ????????? ??????
    public override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account!==null){ // ?????? ????????? ??????????????? ?????? ?????? ??????????????? ??????
            toMapsActivity(firebaseAuth.currentUser)
        }
    } //onStart End

    // onActivityResult
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("LoginActivity", "Google sign in failed", e)
            }
        }
    } // onActivityResult End

    // firebaseAuthWithGoogle
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle:" + acct.id!!)

        //Google SignInAccount ???????????? ID ????????? ???????????? Firebase Auth??? ???????????? Firebase??? ??????
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.w("LoginActivity", "firebaseAuthWithGoogle ??????", task.exception)
                    toMapsActivity(firebaseAuth?.currentUser)
                } else {
                    Log.w("LoginActivity", "firebaseAuthWithGoogle ??????", task.exception)
                    Snackbar.make(btn_googleSignIn, "???????????? ?????????????????????.", Snackbar.LENGTH_SHORT).show()
                }
            }
    }// firebaseAuthWithGoogle END


    // toMainActivity
    fun toMapsActivity(user: FirebaseUser?) {
        if(user !=null) { // MapsActivity ??? ??????
            startActivity(Intent(this, MapsActivity::class.java))
            finish()
        }
    } // toMainActivity End

    // signIn
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // signIn End

    override fun onClick(p0: View?) {
    }


    private fun signOut() { // ????????????
        // Firebase sign out
        firebaseAuth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {
            //updateUI(null)
        }
    }

    private fun revokeAccess() { //????????????
        // Firebase sign out
        firebaseAuth.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener(this) {

        }
    }



}