package com.example.bulletinboard.frag

import android.graphics.Bitmap

/**
 * Класс для отслеживания закрылся фрагмент или нет
 */
interface FragmentCloseInterface {

    /**
     * Функция для отслеживания закрытия интерфейса
     */
    fun onFragClose(list: ArrayList<Bitmap>){

    }
}