package com.example.bulletinboard.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.bulletinboard.model.Ad

/**
 * Класс для создания анимации во время удаления объявления
 * В конструктор передаю старый и новый список
 */
class DiffUtilHelper(val oldList: List<Ad>, val newList: List<Ad>): DiffUtil.Callback() {

    /**
     * Передаю размер старого списка
     */
    override fun getOldListSize(): Int {
        return oldList.size
    }

    /**
     * Передаю размер нового списка
     */
    override fun getNewListSize(): Int {
        return newList.size
    }

    /**
     * В этой функции сравниваю, но по элементу который всегда разный, в данном случае ключ
     * Смотрит одинаковые объявления или нет, одинаковый ли ключ
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        //беру ключ из старого объявления и сравниваю с ключом нового объявления
        return  oldList[oldItemPosition].key == newList[newItemPosition].key
    }

    /**
     * Проверяю похожи или нет, по данным всем, а не только по ключу
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}