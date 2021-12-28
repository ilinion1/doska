package com.example.bulletinboard.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * Класс для получения списка стран и списка городов с этих стран
 */
object CityHelper {

    /**
     * Функция для получения стран
     */
    fun getAllCounties(context: Context): ArrayList<String>{
        val tempArray = ArrayList<String>() //масив куда будут добавлять города
        //пытаюсь считать файл с городами
        try {
            //создаю inputStream для получения файла со странами, получаю в байтах с файла
                //ниже несколько строк кода для того, что бы json превраитьь в string
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size = inputStream.available() //для получения размера файла, в байтах какой размер
            val byteArray = ByteArray(size) //создали массив для сохраннения данный в байтах, с файла со странами
            inputStream.read(byteArray) //считываю файл json в созданный масив с байтами
            //превращаю в стринг byteArray массив в который считали данные с файла. через toString() не работало
            val jsonFile = String(byteArray)
            //превращаю в JSONObject класс, что бы можно было считывать с парсиногом, но не самому задавать параметры парсинга
            val jsonObject = JSONObject(jsonFile)
            val countryName = jsonObject.names() // получаю названия стран с файла(название объектов)
            //создаю цикл от 0 до размера массива countryName если countryName не равно null
            if (countryName != null){
                for (n in 0 until countryName.length()){
                    //добавляю в массив который вернет функция города полученные с цикла
                    tempArray.add(countryName.getString(n))
                }
            }

        }catch (e: IOException){

        }
        return tempArray
    }

    /**
     * Функция для получения городов
     * В конктрукторор так же добавляю страну которую выбрали
     */
    fun getAllCities(country: String, context: Context): ArrayList<String>{
        val tempArray = ArrayList<String>() //масив куда будут добавлять города
        //пытаюсь считать файл с городами
        try {
            //создаю inputStream для получения файла со странами, получаю в байтах с файла
            //ниже несколько строк кода для того, что бы json превраитьь в string
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size = inputStream.available() //для получения размера файла, в байтах какой размер
            val byteArray = ByteArray(size) //создали массив для сохраннения данный в байтах, с файла со странами
            inputStream.read(byteArray) //считываю файл json в созданный масив с байтами
            //превращаю в стринг byteArray массив в который считали данные с файла. через toString() не работало
            val jsonFile = String(byteArray)
            //превращаю в JSONObject класс, что бы можно было считывать с парсиногом, но не самому задавать параметры парсинга
            val jsonObject = JSONObject(jsonFile)
            val cityName = jsonObject.getJSONArray(country) // получаю названия городов с файла(массив по переданному объекту country)
            //создаю цикл от 0 до размера массива cityName
                for (n in 0 until cityName.length()){
                    //добавляю в массив который вернет функция города полученные с цикла
                    tempArray.add(cityName.getString(n))
            }

        }catch (e: IOException){

        }
        return tempArray
    }

    /**
     * Эта функция создает фильтр, по которому будем фильтровать все)
     * Принимает в параметры список со всеми городами и текст, который будет идти от searchView по мере печати
     */
    fun filterListData(list: ArrayList<String>, searchText: String?) : ArrayList<String>{
        val tempList = ArrayList<String>() //временный массив для храннения отфильтрованных данных
        tempList.clear() //на всякий случай очищаею массив
        //если searchText == null, то функция дальше не будет испольняться, а просто вернет текст что нет результата
        if (searchText == null) {
            tempList.add("Не найдено совпадений")
            return  tempList
        }
        //цикл для отбора нужных городов со всего списка
        for(selection in list){
            //проверяю, если selection навчинается с букв которые соотвутсвуют searchText(тому что пишет человек)
                //"toLowerCase" означает что все будет прописными, и в таком случае не будем важно большими или маленькими писал человек
            if (selection.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT))){
                tempList.add(selection) // добавляем в список отфильтрованный по поиску город или города
            }
        }
        // если не нашло через поиск подходящих городов(tempList пустой), показывает что ничего не найдено
        if (tempList.size == 0) tempList.add("Не найдено совпадений")
        return tempList
    }
}