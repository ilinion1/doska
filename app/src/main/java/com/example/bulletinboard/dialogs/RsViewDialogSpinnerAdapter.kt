package com.example.bulletinboard.dialogs

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboard.R

/**
 * Адаптер для отображения списка городов в диалоге
 * Наследуюсь от RecyclerView.Adapter и создаю подкласс ViewHolder, требуемый для адаптера
 * добавил в конструктор TextView,,  что бы мог передать TextView с выбранным городом или страной с EditActivity, через DialogSpinnerHelper, конкретно до textView
 * добавил в коннструктор диалог, что бы мог закрыть диалог по нажатию на элемент списка
 * и обновить его текст на выбранный элемент списка
 */
class RsViewDialogSpinnerAdapter(var tvSelection: TextView, var dialog: AlertDialog): RecyclerView.Adapter<RsViewDialogSpinnerAdapter.SpViewHolder>() {

    private val mainList = ArrayList<String>() //основной массив элементов от куда будут браться города, пока пустой, заполнит в функции

    /**
     * Эта функуция создает ViewHolder, за счет него будет все происходить
     * Наследуется от RecyclerView.ViewHolder и передаю в конструктор View, которое будет рисоваться и запоминаться в ViewHolder
     * В данном случае добавляю наследование от OnClickListener, что бы суметь прослушивать нажатия на элементы в recyclerView
     * Нахожу элементы view
     *
     */
    inner class SpViewHolder(itemView: View,var tvSelection: TextView, var dialog: AlertDialog): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        //переменная, что бы записать в нее элемент списка, созданный адаптером и после передать в onClick в textView на EditsActivity
        private var itemText = ""

        //Функция которая будет показывать 1 textView с названием страны или города, в RecyclerView
        fun setData(text: String){
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem) // нахожу textView, для дальнейшей работы с ним
            tvSpItem.text = text //передаем текст, который передадим из списка
            itemText = text
            itemView.setOnClickListener(this) // присвоили itemView слушатель нажатий, созданный нами ниже onClick()
        }

        /**
         * Эта функция позволяет прослушивать нажатия на текст отображаемый в recyclerView
         */
        override fun onClick(p0: View?) {
            // будет записываться текст с выбранного элемента в переданный textView для отображения в EditActivity
            tvSelection.text = itemText
            dialog.dismiss()//закрываю диалог по нажатию на элемент списка

        }
    }

    /**
     * В этой функции рисуется элемент и возвращает обновленный ViewHolder, с уже отрисованными элементами
     * создаю 1 элемент view(1 элемент списка) и передаю его обратно в ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
         val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item, parent, false)
        return SpViewHolder(view, tvSelection, dialog) //возвращаю ViewHolder с надутой разметкой
    }

    /**
     * В этой функции подключаем к элементам текст или взаимодействуем с элементами с помощью
     */
    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        //запускаю функцию по смене текста в textView, будут брать элементы со списка по позиции
        holder.setData(mainList[position])
    }

    /**
     * В этой функции указываю сколько элементов будет(размер), сколько нужно будет нарисовать
     */
    override fun getItemCount(): Int {
        //указываю сколько элементов в списке будет(будет проверять после того, как будет запущена функция по добавлению элементов)
        return mainList.size
    }

    /**
     * Эта функция заполняет наш массив "mainList" данными
     */
    fun updateAdapter(list: ArrayList<String>){
        mainList.clear() //очищаем список если там что-то было
        mainList.addAll(list) //добавляем все что есть в списке который передадим
        notifyDataSetChanged() //сказать адаптеру что данные изменились
    }
}