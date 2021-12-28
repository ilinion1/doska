package com.example.bulletinboard.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboard.R
import com.example.bulletinboard.utils.CityHelper

/**
 * Класс для создания диалогового окна, в спинер(выбор городов и стран)
 * Отображение RecyclerView
 */
class DialogSpinnerHelper {

    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){
        val builder = AlertDialog.Builder(context) //создаю диалоговое окно
        //оно же, только что бы можно было его передать на адаптер, нужно с помощью builder.create(), иначе не будет функции закрыть диалог
        val dialog = builder.create()
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null) //надумаю разметку
        val adapter = RsViewDialogSpinnerAdapter(tvSelection, dialog) //создаю адаптер для recycler
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView) //нахожу recyclerView
        val searchView = rootView.findViewById<SearchView>(R.id.svSpinner) //нахожу searchView
        rcView.layoutManager = LinearLayoutManager(context) //указываю какой будет LayoutManager
        rcView.adapter = adapter //присваиваю recyclerView адаптер
        adapter.updateAdapter(list) //передаю список городов, который мы передаем с EditAdsAct
        dialog.setView(rootView) //указываю какую разметку нужно использовать в диалоговом окне
        setSearchViewListener(adapter, list, searchView)
        dialog.show() // отображаю диалог

    }

    /**
     * Эта функция отображает в recyclerView результаты исходя из написанного пользователем в searchView
     */
    private fun setSearchViewListener(adapter: RsViewDialogSpinnerAdapter, list: ArrayList<String>, searchView: SearchView?) {
        //добавляю searchView слушатель изменений в тексте
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            //методы добавленные для "object" прослушавателя изменения текста
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false //это использовать не будем
            }
            //методы добавленные для "object" прослушавателя изменения текста
            override fun onQueryTextChange(p0: String?): Boolean {
                //создаем список, который хранит в себе список с функцией фильтра и вводимый текст с searchView
                val tempList = CityHelper.filterListData(list, p0)
                //обновляю адаптер и recyclerView новым списком, отфильтрованным по вводу текста
                adapter.updateAdapter(tempList)
                return true //запускаем отслеживание изменение текста
            }
        })
    }


}