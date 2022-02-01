package com.example.bulletinboard.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.example.bulletinboard.databinding.ProgresDialogLayoutBinding

/**
 * Класс показывает диалог с прогресс баром, анимацией загрузки пока обновляются картинки
 */
object ProgressDialog {


    fun createProgressDialog(act: Activity): AlertDialog{
        val builder = AlertDialog.Builder(act)

        val binding = ProgresDialogLayoutBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}