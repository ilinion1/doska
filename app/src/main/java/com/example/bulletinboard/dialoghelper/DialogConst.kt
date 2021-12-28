package com.example.bulletinboard.dialoghelper

/**
 * в конструктор функции createSignDialog передаю int(констанду), так как при вызове функции буду передавать
 * в ее конструктор костанду, что бы определить на какую кнопку нажали, регистрация, вход
 * и исходя из этого менять содержимое диалога
*/
object DialogConst {
    const val SIGN_UP_STATE = 0
    const val SIGN_IN_STATE = 1
}