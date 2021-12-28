package com.example.bulletinboard.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.example.bulletinboard.databinding.ProgresDialogLayoutBinding

/**
 * Класс показывает диалог с прогресс баром, анимацией загрузки пока обновляются картинки
 */
object ProgressDialog {

    /**
     * Функция создает диалоговое окно
     */
    fun createProgressDialog(act: Activity): AlertDialog{
        val builder = AlertDialog.Builder(act) //Инициализирую класс, который создает диалог, передаю в него конктекст

        //Инициализирую класс,передаю и раздуваю sign_dialog.xml разметку и с помощью контекста добераюсь до layoutInflater
        val binding = ProgresDialogLayoutBinding.inflate(act.layoutInflater)
        builder.setView(binding.root) // передаю root в builder, чем заканчиваю создание диалогового ока, объеденил

        val dialog = builder.create() //для закрытия диалогового окна
        dialog.setCancelable(false) //указываю что диалог нельзя будет остановить
        dialog.show() //теперь диалоговое окно будет рисоваться на экране
        return dialog //возвращаю диалог, что бы мог его закрыть через код
    }
}