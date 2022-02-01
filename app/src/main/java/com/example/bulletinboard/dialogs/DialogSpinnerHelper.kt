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

    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)
        val adapter = RsViewDialogSpinnerAdapter(tvSelection, dialog)
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView)
        val searchView = rootView.findViewById<SearchView>(R.id.svSpinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        adapter.updateAdapter(list)
        dialog.setView(rootView)
        setSearchViewListener(adapter, list, searchView)
        dialog.show()

    }


    private fun setSearchViewListener(
        adapter: RsViewDialogSpinnerAdapter,
        list: ArrayList<String>,
        searchView: SearchView?
    ) {

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {


            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                val tempList = CityHelper.filterListData(list, p0)

                adapter.updateAdapter(tempList)
                return true
            }
        })
    }


}