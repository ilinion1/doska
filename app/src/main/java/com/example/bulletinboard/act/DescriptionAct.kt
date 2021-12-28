package com.example.bulletinboard.act

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletinboard.adapters.ImageAdapter
import com.example.bulletinboard.databinding.ActivityDescriptionBinding
import com.example.bulletinboard.model.Ad
import com.example.bulletinboard.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Активити для подробного описания объявление
 */
class DescriptionAct : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter //адаптер для viewPager
    private var ad: Ad? = null //переменная для получения класса с intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        //слушатель нажатий на кнопку телефона
        binding.fbTel.setOnClickListener {
            call() //функция по открытию приложения со звонками и передачи в него номера
        }
        //слушатель нажатий на кнопку email
        binding.fbEmail.setOnClickListener {
            sendEmail() // функция по открытию приложения для отпрвки письма и передачи в него email
        }
    }

    /**
     * Функция для инициализации
     */
    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter //присваиваю адаптер viewPager
        }
        getIntentFromMainAct() //функция для получения intent и внутри функция для передачи их в адаптер в качестве bitmap
        imageChangeCounter() //обновление счетчика в viewPager
    }

    /**
     * Функция для получения intent с mainAct, где будет ссылка на объявление и класс ad
     * запускаю функцию для обновления UI
     */
    private fun getIntentFromMainAct(){
        //получаю интент переданный с mainAct и указываю что получаю не SerializableExtra, а как класс Ad
        ad = intent.getSerializableExtra("AD") as Ad
        if (ad != null) updateUI(ad!!) //запускаю функцию для обновления UI
    }

    /**
     * Функция для обновление UI, а именно после полученного intent
     * Передаю в адаптер картинки в виде bitmap и текст
     */
    private fun updateUI(ad: Ad){
        ImageManager.fillImageArray(ad, adapter) // функция для передачи картинок в адаптер в качестве bitmap
        fillTextView(ad) //функция для передачи текста в адаптер
    }



    /**
     * Функция для заполнения текстовой части
     */
    private fun fillTextView(ad: Ad) = with(binding){
            tvTitle.text = ad.title
            tvDescription.text = ad.description
            tvEmail.text = ad.email
            tvPrice.text = ad.price
            tvTel.text = ad.tel
            tvCountry.text = ad.country
            tvCity.text = ad.city
            tvIndex.text = ad.index
            tvWithSend.text = isWithSend(ad.withSend.toBoolean())
    }

    /**
     * Функция для определения WithSend с отправкой или без
     */
    private fun isWithSend(withSend: Boolean): String{
        return if (withSend) "Да" else "Нет"
    }

    /**
     * Функция по отправке номера телефона в приложение для звонков и открытие его
     */
    private fun call(){
        //создаю uri, номер по которому будем звонить и отправим на приложение
        val callUri = "tel: ${ad?.tel}"
        //запускаю специальный интернт который будет вызывать приложение для звонков
        val intentCall = Intent(Intent.ACTION_DIAL)
        //передаю данные в приложение, через интент
        intentCall.data = callUri.toUri()
        startActivity(intentCall) //запускаю активити с приложением
    }

    /**
     * Функция по отправке email в приложение для email и открытие его
     */
    private fun sendEmail(){
        //intent с помощью которого буду открывать приложение для отправки почты
        val intentEmail = Intent(Intent.ACTION_SEND)
        //указываю тип, с помощью которого буду отправлять сообщения
        intentEmail.type = "message/rfc822"
        //указываю данные которые должны появиться в приложении, уже заполенные поля
        intentEmail.apply {
            //указываю костанды что бы приложение знало в какое поле помещать текст. Именно в виде списка
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            //тема
            putExtra(Intent.EXTRA_SUBJECT, "Объявление")
            //текст
            putExtra(Intent.EXTRA_TEXT, "Меня интересуют Ваше объявление")
        }
        //проверка вдруг нет на телефоне приложений с почтой, что бы не было ошибки
        try {
            //пробую открыть приложение для отправки почты/createChooser - если есть несколько приложений, выбрать
           startActivity(Intent.createChooser(intentEmail,"Открыть с помощью"))
        }catch(e: ActivityNotFoundException) {
            //отслеживаю ошибку, что нет такого активити, для обработки intent данного.Пишу тогда тост
            Toast.makeText(this, "Нет приложения для отправки письма", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Функция для обновления счетчика в viewPager
     */
    private fun imageChangeCounter(){
        //добавляю слушатель viewPager, что бы отслеживать на какой позиции сейчас нахожусь в слайдере
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imagePosition = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                 binding.tvCount.text = "$imagePosition"
            }
        })
    }
}