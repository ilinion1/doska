package com.example.bulletinboard.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.example.bulletinboard.act.MainActivity
import com.example.bulletinboard.R
import com.example.bulletinboard.accounthelper.AccountHelper
import com.example.bulletinboard.databinding.SignDialogBinding

/**
 * Класс для создания диалога, будет вызываться из mainActivity
 * В конструктор указал act, так как нужно будет взять объекты с активити и контекст, можно было бы
 * сразу это делать на MainActivity, но что бы разделять и не захламлять код, диалог тделается отдельно
 */
class DialogHelper(private val act: MainActivity) {

    val accHelper = AccountHelper(act)


    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(act)
        val binding = SignDialogBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)
        setDialogState(index, binding)

        val dialog = builder.create()


        binding.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, binding, dialog)
        }


        binding.btForgetP.setOnClickListener {
            setOnClickResetPassword(binding, dialog)
        }


        binding.btGoogleSignIn.setOnClickListener {
            accHelper.signInWithGoogle()
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun setOnClickResetPassword(binding: SignDialogBinding, dialog: AlertDialog?) {

        if (binding.edSignEmail.text.isNotEmpty()) {

            act.myAuth.sendPasswordResetEmail(binding.edSignEmail.text.toString())
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        Toast.makeText(
                            act,
                            R.string.email_reset_password_was_send,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            dialog?.dismiss()
        } else {

            binding.tvDialogMessage.text = act.getString(R.string.dialog_reset_message_email)
            binding.tvDialogMessage.visibility = View.VISIBLE
        }
    }


    private fun setOnClickSignUpIn(index: Int, binding: SignDialogBinding, dialog: AlertDialog?) {

        dialog?.dismiss()

        if (index == DialogConst.SIGN_UP_STATE) {
            accHelper.signUpWithEmail(
                binding.edSignEmail.text.toString(),
                binding.edSignPassword.text.toString()
            )
        } else {
            accHelper.signInWithEmail(
                binding.edSignEmail.text.toString(),
                binding.edSignPassword.text.toString()
            )
        }
    }


    private fun setDialogState(index: Int, binding: SignDialogBinding) {

        if (index == DialogConst.SIGN_UP_STATE) {
            binding.tvSignTitle.text = act.getString(R.string.ac_sign_up)
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
        } else {
            binding.tvSignTitle.text = act.getString(R.string.ac_sign_in)
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            binding.btForgetP.visibility = View.VISIBLE
        }

    }
}