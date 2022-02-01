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
class RsViewDialogSpinnerAdapter(var tvSelection: TextView, var dialog: AlertDialog) :
    RecyclerView.Adapter<RsViewDialogSpinnerAdapter.SpViewHolder>() {

    private val mainList = ArrayList<String>()

    inner class SpViewHolder(itemView: View, var tvSelection: TextView, var dialog: AlertDialog) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var itemText = ""

        fun setData(text: String) {
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)
            tvSpItem.text = text
            itemText = text
            itemView.setOnClickListener(this)
        }


        override fun onClick(p0: View?) {

            tvSelection.text = itemText
            dialog.dismiss()

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item, parent, false)
        return SpViewHolder(view, tvSelection, dialog)
    }


    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {

        holder.setData(mainList[position])
    }


    override fun getItemCount(): Int {
        return mainList.size
    }


    fun updateAdapter(list: ArrayList<String>) {
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }
}