package com.example.bulletinboard.accounthelper


import android.util.Log
import android.widget.Toast
import com.example.bulletinboard.R
import com.example.bulletinboard.act.MainActivity
import com.example.bulletinboard.constans.FirebaseAuthConstants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*

/**
 * Класс для аутификации, функция регистрации и входа
 */
class AccountHelper(private val act: MainActivity) {

    private lateinit var signInClient: GoogleSignInClient


    fun signUpWithEmail(email: String, password: String) {


        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.myAuth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    act.myAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                signUpWithEmailSuccessful(it.result.user!!)
                            } else {
                                signUpWithEmailException(it.exception!!, email, password)
                            }

                        }
                }
            }
        }
    }


    private fun signUpWithEmailSuccessful(user: FirebaseUser) {


        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailException(e: Exception, email: String, password: String) {

        if (e is FirebaseAuthUserCollisionException) {
            e.errorCode
            if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                linkEmailToG(email, password)
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {

            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG)
                    .show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    fun signInWithEmail(email: String, password: String) {
        act.myAuth.currentUser?.delete()?.addOnCompleteListener {
            if (it.isSuccessful) {
                if (email.isNotEmpty() && password.isNotEmpty()) {

                    act.myAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            act.uiUpdate(it.result?.user)
                        } else signInWithEmailException(it.exception!!, email, password)
                    }
                }
            }
        }
    }

    private fun signInWithEmailException(e: Exception, email: String, password: String) {
        if (e is FirebaseAuthInvalidUserException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG)
                    .show()
            }

        } else {
            if (e is FirebaseAuthInvalidCredentialsException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    Toast.makeText(
                        act,
                        FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun linkEmailToG(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.myAuth.currentUser != null) {
            act.myAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    Toast.makeText(act, act.getString(R.string.link_done), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(act, act.getString(R.string.entre_to_g), Toast.LENGTH_LONG).show()
        }

    }


    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun signAutG() {
        getSignInClient().signOut()
    }

    fun signInWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        act.googleSignInLauncher.launch(intent)
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.myAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                act.myAuth.signInWithCredential(credential).addOnCompleteListener {

                    if (it.isSuccessful) {
                        Toast.makeText(act, "Sign in done", Toast.LENGTH_SHORT).show()
                        act.uiUpdate(it.result.user)
                    } else {

                        Log.d("MyLog", "Google Sign In Exception: ${it.exception}")
                    }
                }
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {

        user.sendEmailVerification().addOnCompleteListener {
            if (it.isSuccessful) {

                Toast.makeText(act, R.string.send_verification_email_done, Toast.LENGTH_LONG).show()
            } else {

                Toast.makeText(act, R.string.send_verification_email_error, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    fun signInAnonymously(listener: Listener) {
        act.myAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                listener.onComplete()
                Toast.makeText(act, "Вы вошли как гость", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, "Не удалось войти как гость", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}
