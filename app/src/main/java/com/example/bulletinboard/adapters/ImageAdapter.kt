package com.example.bulletinboard.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboard.R

/**
 * Адаптер для ViewPager, слайдера бокового в EditsActAct
 */
class ImageAdapter: RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val mainArray = ArrayList<Bitmap>() //массив который принимает данные для адаптера

    /**
     * Нахожу и надуваю разметку
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageHolder(view)
    }

    /**
     * Достаем элементы которые приходят и заполняем ими разметку
     */
    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position]) //заполняю элемент с разметки данными которые передам в адаптер
    }

    override fun getItemCount(): Int {
        return mainArray.size //указываю сколько будет элементов которые нужно будет рисовать
    }

    /**
     * Класс ViewHolder
     */
    class ImageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var imItem: ImageView //буду позже инициализировать картинку с разметки
        /**
         * Буду находить элементы с разметки
         */
        fun setData(bitmap: Bitmap){
            imItem = itemView.findViewById(R.id.imItem) //нахожу картинку с разметки
            imItem.setImageBitmap(bitmap)// заполняю элемент входными данными
        }
    }

    /**
     * Функция обновляет адаптер, когда в нее передают данные
     */
    fun update(newList: ArrayList<Bitmap>){
        mainArray.clear() //очищаю список
        // переданные данные добавляю в список с которого будут заполняться элементы разметки
        mainArray.addAll(newList)
        notifyDataSetChanged() //сказать адаптеру что данные обновились
    }

}