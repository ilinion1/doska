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

    // инициализирую класс AccountHelper, для дальнейшей передачи функции с отправкой письма на почту
    val accHelper = AccountHelper(act)

    /**
     * функция создает диалоговое окно
     * запускает функцию с изменением ui диалогового окна, в зависимости от того с какой целью зашли
     * запускает функцию для регистрации, входа и отправки письма на почту
     */
    fun createSignDialog(index: Int){
        val builder = AlertDialog.Builder(act) //Инициализирую класс, который создает диалог, передаю в него конктекст

        //Инициализирую класс,передаю и раздуваю sign_dialog.xml разметку и с помощью контекста добераюсь до layoutInflater
        val binding = SignDialogBinding.inflate(act.layoutInflater)
        builder.setView(binding.root) // передаю root в builder, чем заканчиваю создание диалогового ока, объеденил
        setDialogState(index, binding) //вызываю функцию состояния диалога, в зависимости от того, для регистрации зашел или входа

        val dialog = builder.create() //для закрытия диалогового окна

        //слушатель нажатий на кнопку вход/регистрация, для регистрации и отправки письма на почту
        binding.btSignUpIn.setOnClickListener {
          setOnClickSignUpIn(index, binding, dialog) //запускаю функцию на основе функции из класса AccountHelper для регистрации, входа и отправки письма
        }

        //слушатель нажатий на кнопку "забыли пароль?"
        binding.btForgetP.setOnClickListener {
            setOnClickResetPassword(binding, dialog)
        }

        //слушатель нажатий для кнопки вход через гугл ак
        binding.btGoogleSignIn.setOnClickListener {
            accHelper.signInWithGoogle()
            dialog.dismiss() // закрываю диалоговое окно
        }
            dialog.show() //теперь диалоговое окно будет рисоваться на экране
    }

    /**
     * функция для восстановления пароля и отправки на почту письма
     */
    private fun setOnClickResetPassword(binding: SignDialogBinding, dialog: AlertDialog?) {
        //проверить не пустая ли строка с почтой
        //addOnCompleteListener проверяет отправился эмейл или нет
        if (binding.edSignEmail.text.isNotEmpty()){
            //функция вызванная из mainActivity из FirebaseAuth для восстановления пароля
            act.myAuth.sendPasswordResetEmail(binding.edSignEmail.text.toString()).addOnCompleteListener {
                //если успешно отправлено письмо, показываю тост говорящий об этом
                if (it.isSuccessful){
                    Toast.makeText(act,R.string.email_reset_password_was_send,Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss() //закрываю диалог
        }else{
            //если поле для эмейла пустое, показываю textView говорящий об этом
            binding.tvDialogMessage.text = act.getString(R.string.dialog_reset_message_email)
            binding.tvDialogMessage.visibility = View.VISIBLE
        }
    }


    /**
     * функция в зависимости от цели входа, запускает функции для входа и регистрации из класса AccountHelper
     * и для отправки письма с подтверждением почты на почту(это вложенно в функцию для регистрации/входа из класса AccountHelper)
     */
    private fun setOnClickSignUpIn(index: Int, binding: SignDialogBinding, dialog: AlertDialog?) {

        dialog?.dismiss() //закрывает диалоговое окно
        //проверка сейчас состояние окна для регистрации или для входа
        if (index == DialogConst.SIGN_UP_STATE) {
            //если для регистрации, запускает функцию для регистрации и передаю логин и пароль указанный пользователем
            accHelper.signUpWithEmail(binding.edSignEmail.text.toString(), binding.edSignPassword.text.toString())
        } else{
            //если для входа вошли, пока запускает функцию для входа и передаю логин пароль пользователя
            accHelper.signInWithEmail(binding.edSignEmail.text.toString(), binding.edSignPassword.text.toString())
        }
    }

    /**
     *  функция для состояния диалога что бы определить на какую кнопку нажали, регистрация, вход
     * и исходя из этого менять содержимое диалога
     */
    private fun setDialogState(index: Int, binding: SignDialogBinding) {
        //проверяю какое значение было передано в конструктор, на какую кнопку нажали и от этого задаю внешний вид
        if(index == DialogConst.SIGN_UP_STATE) {
            binding.tvSignTitle.text = act.getString(R.string.ac_sign_up) //сразу через getString получаю ресурс
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_up_action) //с помощью resources.getString. два метода
        }else {
            binding.tvSignTitle.text = act.getString(R.string.ac_sign_in)
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            binding.btForgetP.visibility = View.VISIBLE // делаю видимой кнопку "забыли пароль?"
        }

    }
}