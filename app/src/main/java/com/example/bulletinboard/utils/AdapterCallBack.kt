package com.example.bulletinboard.utils

/**
 * Интерфейс для адаптера во фрагменте EditAct
 * С функцией изменения видимости кнопки добавить, при нажатии удалить на элементе
 */
interface AdapterCallBack {

    /**
     * Функция делает видимой кнопку добавить в тулбаре, при нажатии на удалить в элементе адаптера
     */
    fun onItemDelete()
}