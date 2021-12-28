package com.example.bulletinboard.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bulletinboard.model.Ad
import com.example.bulletinboard.model.DbManager

/**
 * Этот класс будет взаимодействовать с данными и view. Посредник для передачи и обновления данных
 *  ViewModel() не разрушается при повороте экрана и тд, продолжает хранить в сее данные
 *  LiveData() оторый следит за изменениями данных и обновляет view которое подписано на обновление
 *  как только view будет доступны
 */
class FirebaseViewModel: ViewModel() {
    private val dbManager = DbManager() //инициализирую класс с данными
    //инициализирую класс liveData, который следит за изменениями данных и обновляет view как только они будут доступны
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()

    /**
     * Эта функция будет считывать с базы данных что нужно и передавать данные в liveData
     * считывает объявления с филтром по времени
     */
    fun loadAllAdsFirstPage(){
        //считываю данные и передаю в конструктор интерфейс
        dbManager.getAllAdsFirstPage(object: DbManager.ReadDataCallBack{
            //метод интерфейса
            override fun readData(list: ArrayList<Ad>) {
                  //в случае с liveAdsData использую value для передачи данных
                liveAdsData.value = list
            }
        })
    }

    /**
     * Эта функция будет считывать с базы данных что нужно и передавать данные в liveData
     * считывает объявления с фильром по времени для скрола
     */
    fun loadAllAdsNextPage(time: String){
        //считываю данные и передаю в конструктор интерфейс
        dbManager.getAllAdsNextPage(time, object: DbManager.ReadDataCallBack{
            //метод интерфейса
            override fun readData(list: ArrayList<Ad>) {
                //в случае с liveAdsData использую value для передачи данных
                liveAdsData.value = list
            }
        })
    }

    /**
     * Эта функция будет считывать с базы данных что нужно и передавать данные в liveData
     * считывает объявления с фильтром по категории и времени
     */
    fun loadAllAdsFromCat(cat: String){
        //считываю данные и передаю в конструктор интерфейс
        dbManager.getAllAdsFromCatFirstPage(cat ,object: DbManager.ReadDataCallBack{
            //метод интерфейса
            override fun readData(list: ArrayList<Ad>) {
                //в случае с liveAdsData использую value для передачи данных
                liveAdsData.value = list
            }
        })
    }

    /**
     * Эта функция будет считывать с базы данных что нужно и передавать данные в liveData
     * считывает объявления с фильтром по категории и времени , следующуу страницу
     */
    fun loadAllAdsFromCatNextPage(catTime: String){
        //считываю данные и передаю в конструктор интерфейс
        dbManager.getAllAdsFromCatNextPage(catTime ,object: DbManager.ReadDataCallBack{
            //метод интерфейса
            override fun readData(list: ArrayList<Ad>) {
                //в случае с liveAdsData использую value для передачи данных
                liveAdsData.value = list
            }
        })
    }

    /**
     * Эта функция будет считывать с базы данных что нужно и передавать данные в liveData
     * считывавает мои объявления
     */
    fun loadMylAds(){
        //считываю данные и передаю в конструктор интерфейс
        dbManager.getMyAds(object: DbManager.ReadDataCallBack{
            //метод интерфейса
            override fun readData(list: ArrayList<Ad>) {
                //в случае с liveAdsData использую value для передачи данных
                liveAdsData.value = list
            }
        })
    }

    /**
     * Эта функция будет считывать с базы данных объявления в избранных и передает данные в liveData
     */
    fun loadMyFavs(){
        //считываю данные и передаю в конструктор интерфейс
        dbManager.getMyFavs(object: DbManager.ReadDataCallBack{
            //метод интерфейса
            override fun readData(list: ArrayList<Ad>) {
                //в случае с liveAdsData использую value для передачи данных
                liveAdsData.value = list
            }
        })
    }

    /**
     * Функция для удаления с базы данных объявления
     * Логика удаления прописана в dbManager
     */
    fun deleteItem(ad: Ad){
        dbManager.deleteAd(ad, object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value //беру старый список с данными
                updatedList?.remove(ad) //указываю какое объявление удалил из базы, что бы удалил из адаптера
                liveAdsData.postValue(updatedList) //указываю что изменились данные
            }

        })
    }

    /**
     * Функция для создания нового пути по просмотрам объявления и записи на него данных
     * Вызываю функцию с DbManager()
     */
    fun adViewed(ad: Ad){
        dbManager.adViewed(ad) // функция по добавлению пути для просмотров и данных +1 к просмотру
    }

    /**
     * Функция для удаления или добавления в избранное
     */
    fun onFavClick(ad: Ad){
        dbManager.onFavClick(ad,object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value //беру старый список с данными
                val pos = updatedList?.indexOf(ad) //ищу позицию объявления с которым будет работа
               //если не нашло позицию, выдаст -1. проверка если все же нашло, то работаем дальше
                if (pos != -1){
                    //если не равно null, код ниже
                    pos?.let {
                        //если пока знаение положительное, то ниже в коде станет отрицательным и нужно отнять значение от счетчика
                        //если отрицательное, тоесть не в избранных, значит станет и потом +1 к счетчику
                        val favCounter = if (ad.isFav) ad.favCounter.toInt() -1 else ad.favCounter.toInt() +1
                        //создаю копию объявления с исправленным fav, что бы обновлися адаптер
                        //!ad.isFav значит записываю противоположное значение. было true, станет false
                        //так же перезиписываю счетчик
                        updatedList[pos] = updatedList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())
                    }

                }
                liveAdsData.postValue(updatedList) //указываю что изменились данные
            }

        })
    }
}