package com.example.bulletinboard.frag

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.bulletinboard.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Базовый класс фрагмента, где будет работа с картинками. От него наследуется ImageListFrag
 * Буду настраивать тут рекламный банер adView
 * Разгружаю тут код из ImageListFrag
 */
open class BaseAdsFrag: Fragment(), InterAdsClose {
    lateinit var adView: AdView //для будущих фрагментов которые будут наследоваться, что бы могли добраться до adView
    var interAd: InterstitialAd? = null //для межстраничного объявления


    /**
     * Запускаю функцию загрузки рекламы на всю страницу InterstitialAd
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInterAd() //запускаю функцию загрузки рекламы
    }
    /**
     * Для запускаю initAds(), запроса на рекламу
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initAds() //запускаю функцию запроса на рекламу
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Цикл жизни фрагмента где продолжает работу что-то, в данном случае запрос на рекламу
     */
    override fun onResume() {
        adView.resume() //продолжает работу запрос на рекламу
        super.onResume()
    }

    /**
     * цикл жизни фрагмента во время паузы, тут ставится на паузу запрос на рекламу
     */
    override fun onPause() {
        adView.pause() //ставлю на паузу запрос на рекламу
        super.onPause()
    }

    override fun onDestroy() {
        adView.destroy() //разрушаю запрос на рекламу
        super.onDestroy()
    }

    /**
     * В этой функции инициализирую adView банер для рекламы
     */
    private fun initAds(){
        MobileAds.initialize(activity as Activity) //инициализирует SDK, не знаю о чем речь, но нужно)
        val adRequest = AdRequest.Builder().build()//запрос на рекламу
        adView.loadAd(adRequest) //загружаю рекламу. делаю запрос, если есть доступная, ее загрузят

    }

    /**
     * Функция для загрузки рекламы на всю страницу Intersticial
     */
    fun loadInterAd(){
        val adRequest = AdRequest.Builder().build() //запрос на рекламу
        //загружаю рекламу
        InterstitialAd.load(context as Activity, getString(R.string.ad_inter_id), adRequest, object : InterstitialAdLoadCallback(){
            //если загрузилась реклама. присваиваю загруженную рекламу переменной
            override fun onAdLoaded(ad: InterstitialAd) {
                interAd = ad //присвоил загруженную рекламу переменной
            }
        })
    }

    /**
     * Функция показа рекламы
     */
    fun showInterAd(){
        if (interAd != null){
            //показывает рекламу если загрузилась успешно
            //калл бак который следит за происходящим с рекламой и запускает методы реагирования
            interAd?.fullScreenContentCallback = object : FullScreenContentCallback(){

                //функция как только пользователь закрыл рекламу, произойдет интерфейс,а после уже в зависимости от него
                override fun onAdDismissedFullScreenContent() {
                    onClose() //функция интерфейса
                }

                //если вышла ошибка при показе рекламы, то тоже запускаю функцию интерфейса
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    onClose() //функция интерфейса
                }
            }
            interAd?.show(activity as Activity) //запускаю показ рекламы
        } else {
            //если interAd = null, реклама не загрузилась, выполняю действие на которое нажал, пока просто интерфейс
            onClose()
        }
    }

    /**
     * Функция интерфейса, будет реализована во фрагменте где вызовут ее
     */
    override fun onClose() {

    }
}