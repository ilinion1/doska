package com.example.bulletinboard.act

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.bulletinboard.R
import com.example.bulletinboard.databinding.ActivityFilterBinding
import com.example.bulletinboard.dialogs.DialogSpinnerHelper
import com.example.bulletinboard.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private var dialog = DialogSpinnerHelper() //создаем экземпляр класса с диалогом

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings() //запускаю акшинБар
        onClickedSelectCountry() //выбор сраны
        onClickedSelectCity() //выбор города
        onClickedDone() //запуск кнопки передачи фильтра
    }

    /**
     * Активирую actionBar
     */
    fun actionBarSettings(){
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true) //активирую actionBar
    }

    /**
     * Для управления кнопка в actionBar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish() //закрываю активити
        return super.onOptionsItemSelected(item)
    }

    /**
     * Эта функция со слушателем нажатия на textView для выбора страны
     * по нажатию запускает диалог
     */
    private fun onClickedSelectCountry() = with(binding){
        tvCountry.setOnClickListener {
            //создаем список стран, который будем передавать в диалог и там показывать
            val listCounty = CityHelper.getAllCounties(this@FilterActivity) // инициализирую список стран, созданный в CityHelper
            dialog.showSpinnerDialog(this@FilterActivity, listCounty, binding.tvCountry) // запускаю диалог, передавая контекст,список стран и textView
            //проверяем, если был выбран город и текст уже не тот что по умолчанию, устанавливаем что по умолчанию
            if(tvCity.text.toString() != getString(R.string.select_city)){
                tvCity.text = getString(R.string.select_city)
            }
        }
    }

    /**
     * Эта функция со слушателем нажатия на textView для выбора города
     * по нажатию запускает диалог
     */
    private fun onClickedSelectCity() = with(binding){
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString() //нахожу textView где отображает выбранную страну
            //проверяем если textView отображает текст по умолчанию, что значит что страну еще не выбрали, пишу что бы выбрал
            //если страна выбрана, запускает диавлоговое окно для выбора города
            if (selectedCountry != getString(R.string.select_country)){
                //создаем список стран, который будем передавать в диалог и там показывать
                val listCity = CityHelper.getAllCities(selectedCountry,this@FilterActivity) // инициализирую список городов, созданный в CityHelper
                dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity) // запускаю диалог, передавая контекст и список городов и textView

            } else {
                Toast.makeText(this@FilterActivity, R.string.no_select_country, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * В этой функции собираю данные с разметки в кучу, для дальнейшей фильтрации
     */
    private fun createFilter(): String = with(binding){
        //специальный класс который собирает вместе объекты string
        val stringBuilder = StringBuilder()

        //создаю массив в который помещу все данные, дальше буду его перебирать
        val arrayTempFilter = listOf(
            tvCountry.text,
            tvCity.text,
            edIndex.text,
        checkBoxWithSend.isChecked.toString())
        //проверяю, если последний элемент, то нижнее подчеркивание не добавляю
        for ((i,s) in arrayTempFilter.withIndex()){
            //если не значение по умолчанию, значит выбрано пользователем
            if (s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty()){
                stringBuilder.append(s) //добавляю значения в фильтр
                if (i != arrayTempFilter.size -1) stringBuilder.append("_") //если не последнее значение, добавляю
            }
        }
        return stringBuilder.toString()
    }

    /**
     * Эта функция со слушателем нажатия на textView для применения фильтра
     */
    private fun onClickedDone() = with(binding){
        btDone.setOnClickListener {
            Log.d("MyLog", createFilter())
        }
    }
}