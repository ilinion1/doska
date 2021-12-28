package com.example.bulletinboard.accounthelper


import android.util.Log
import android.widget.Toast
import com.example.bulletinboard.act.MainActivity
import com.example.bulletinboard.R
import com.example.bulletinboard.constans.FirebaseAuthConstants
import com.example.bulletinboard.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import java.lang.Exception

/**
 * Класс для аутификации, функция регистрации и входа
 * в конструкторе указываю MainActivity, что бы взять FireBaseAuthentication
 */
class AccountHelper(private val act: MainActivity)  {

    //в дальнейшем присвою ей клиент, созданный в функции, но который нужно присвоить переменной
    private lateinit var signInClient: GoogleSignInClient

    /**
     * функция для регистрации с помощью почты
     */
    fun signUpWithEmail(email: String, password: String) {

        //проверка указал ли пользователь почту и пароль
        if (email.isNotEmpty() && password.isNotEmpty()) {
            //удаляю анонимный ак
            act.myAuth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    // регистрирует пользователя, с помощью функции полученной с объекта myAuth(аутификация от Firebase)
                    //но прежде нужно на сайте включить доступ к такой аутификации, выбрав ее в перечне
                    //addOnCompleteListener - проверяет успешно зарегистрировался или нет
                    act.myAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        //если успешная регистрация или не успешная
                        if (it.isSuccessful) {
                            signUpWithEmailSuccessful(it.result.user!!) //если успешная регистрация
                        } else {
                            signUpWithEmailException(it.exception!!,email, password) //если не успешная регистрация
                        }

                    }
                }
            }
        }
    }


    /**
     * Функция для при успешной регистрации с почты
     */
    private fun signUpWithEmailSuccessful(user: FirebaseUser){

        //Если успешная регистрация, отправляет сообщение на почту, для подтверждения email
        sendEmailVerification(user)
        act.uiUpdate(user) //функция из mainActivity обновляю UI в header
    }

    /**
     * Функция для при неуспешной регистрации с почты
     */
    private fun signUpWithEmailException(e: Exception,email: String, password: String){
        //проверяю если ошибка из класса FirebaseAuthUserCollisionException, то после определяю какая именно
        if (e is FirebaseAuthUserCollisionException) {
            //когда убедились через проверку что ошибка с этого класса, присваиваем этот класс ей
            e.errorCode //errorCode  - это костанда где указывает какая конкретно ошибка из категории класса
            //Log.d("MyLog", "Exception: ${exception.errorCode}") //это способ найти текст ошибки

            //какая именно ошибка из этого класса. Если то что такой пользователь  есть, то
            if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                //Toast.makeText(act, FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_LONG).show()
                //Link email
                linkEmailToG(email, password) //Функция для объеденения почты с джимейл ак
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            //какая именно ошибка из этого класса. Если то что такой пользователь  есть, то
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {

            //какая именно ошибка из этого класса.
            //Log.d("MyLog", "Exception: ${e.errorCode}")
            //после как узнали ошибку и создали констанду, пишем если эта ошибка, то показать тост
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD){
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * функция для входа с помощью почты
     */
    fun signInWithEmail(email: String, password: String) {
        //удаляю анонимный ак
        act.myAuth.currentUser?.delete()?.addOnCompleteListener {
            if (it.isSuccessful){
                //проверка указал ли пользователь почту и пароль
                if (email.isNotEmpty() && password.isNotEmpty()) {

                    //делает вход в ак, с помощью функции полученной с объекта myAuth(аутификация от Firebase)
                    //но прежде нужно на сайте включить доступ к такой аутификации, выбрав ее в перечне
                    //addOnCompleteListener - проверяет успешно вошел или нет
                    act.myAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        //если успешный вход или не успешный
                        if (it.isSuccessful) {
                            act.uiUpdate(it.result?.user) //функция из mainActivity обновляю UI в header
                        } else signInWithEmailException(it.exception!!, email, password) //если неудачный вход
                    }
                }
            }
        }
    }

    /**
     * Функция с ошибками при входе в ак через почту
     */
    private fun signInWithEmailException(e:Exception, email: String, password: String){
        if (e is FirebaseAuthInvalidUserException) {
            // если ошибка из класса FirebaseAuthInvalidUserException, проверяю что именно за ошибка
            // Log.d("MyLog", "Email Sign In Exception: ${exception.errorCode}") так узнаю что за ошибка и создаю костанду
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND){
                Toast.makeText(act, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG).show()
            }

        }else {
            // показываю сообщение о ошибке при неправильном эмейле
            if (e is FirebaseAuthInvalidCredentialsException) {
                //какая именно ошибка из этого класса. Если то что такой пользователь  есть, то
                if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Функция для объеденения ак зарегистрированного через почту и через джимейл в один
     */
    private fun linkEmailToG(email: String, password: String){
        val credential = EmailAuthProvider.getCredential(email, password) //создаю credential для подключение почты к гугл ак
        //подсоеденяю если пользователь находится на ак гугле, если вышел, нужно сначала войти
        if(act.myAuth.currentUser != null){
            act.myAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener {it ->
                if (it.isSuccessful) {
                    Toast.makeText(act, act.getString(R.string.link_done), Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(act, act.getString(R.string.entre_to_g), Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Функция создает клиент для входа, по гугл ак
     * GoogleSignInClient - создает специальный intent, что бы отправить с его помощью сообщения системе,
     * на получение данных о гугл ак и токене
     * тут будем настраивать сообщение с запросом
     */
    private fun getSignInClient(): GoogleSignInClient{
        //задаю параметры опции, для получения письма. Нужно что бы было просто для регистрации
        //взять токен с приложения и передать туда, что бы система нам доверяла и выдала ак
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        //возвращаю GoogleSignIn с интентом который передаст системе и после можно будет присовить возвращаеммый клиент
        return GoogleSignIn.getClient(act, gso)
    }

    /**
     * Функция выхода из гугл ак
     */
    fun signAutG(){
        getSignInClient().signOut()
    }


    /**
     * В функции будет присваитьваться переменной "signInClient" клиент возвращаеммый из функции "getSignInClient()"
     * Функция будет запускать по нажатию на кнопку "Войти" под гугл ак
     */
    fun signInWithGoogle(){
        signInClient = getSignInClient() // присвоил возвращаемый системой клиент
        val intent = signInClient.signInIntent // получаю интент, который содержит в себе всю информацию настройки
        act.googleSignInLauncher.launch(intent) //запускаю вход
    }

    /**
     * Функция для получения токена с клиента, что бы добравть до credential
     * Регистрирую в гугл ак и получаю доступ к учетным данным пользователя
     * addOnCompleteListener - проверяю успешность
     * обновил ui данными полученными с клиента
     */
    fun signInFirebaseWithGoogle(token: String){
        val credential = GoogleAuthProvider.getCredential(token, null) //получаю credential(учетные данные)
        //удаляю анонимного пользователя перед тем как создать с гугл ак и проверяю удалил ли, если да, вход по гуглу
        act.myAuth.currentUser?.delete()?.addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                //регистрируюсь в гугл ак через FirebaseAuth и доступ к учетным данным с гугл ак получаю
                act.myAuth.signInWithCredential(credential).addOnCompleteListener {
                    //если все успешно
                    if (it.isSuccessful){
                        Toast.makeText(act, "Sign in done", Toast.LENGTH_SHORT).show()
                        act.uiUpdate(it.result.user) //обновляю информацию в ui
                    }  else {
                        //покажет что за ошибка, если не выйдет зарегистрировать
                        Log.d("MyLog", "Google Sign In Exception: ${it.exception}")
                    }
                }
            }
        }
    }

    /**
     * Функция для отправки письма подтверждения о регистрации на почту
     * В конструкторе юзер из FirebaseUser, с его помощью будет отправлять письмо на почту
     * user появляется после успешной регистрации
     */
    private fun sendEmailVerification(user: FirebaseUser){
        //addOnCompleteListener - слушатель об успешной или не успешной отправке письма
        user.sendEmailVerification().addOnCompleteListener {
            if (it.isSuccessful){
                // если успешно отправило письмо, пишет что все успешно прошло
                Toast.makeText(act, R.string.send_verification_email_done, Toast.LENGTH_LONG).show()
            } else{
                // если не успешно, сообщение об ошибке
                Toast.makeText(act, R.string.send_verification_email_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Функция для создания входа анонимного
     */
    fun signInAnonymously(listener: Listener){
        //signInAnonymously функция для аннонимного входа/ addOnCompleteListener успешно или нет
        act.myAuth.signInAnonymously().addOnCompleteListener {
            task ->
            if (task.isSuccessful) {
                //если успешно зарегистрировался
                listener.onComplete()
                Toast.makeText(act, "Вы вошли как гость", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, "Не удалось войти как гость", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Интерфейс для входа анонимного, что бы дать завершиться входу на ассинхронном потоке
     */
    interface Listener{
        fun onComplete()
    }
}
