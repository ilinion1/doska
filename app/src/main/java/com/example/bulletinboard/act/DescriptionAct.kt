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
    lateinit var adapter: ImageAdapter
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fbTel.setOnClickListener {
            call()
        }

        binding.fbEmail.setOnClickListener {
            sendEmail()
        }
    }


    private fun init() {
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainAct()
        imageChangeCounter()
    }


    private fun getIntentFromMainAct() {
        ad = intent.getSerializableExtra("AD") as Ad
        if (ad != null) updateUI(ad!!) //запускаю функцию для обновления UI
    }

    private fun updateUI(ad: Ad) {
        ImageManager.fillImageArray(ad, adapter)
        fillTextView(ad)
    }


    private fun fillTextView(ad: Ad) = with(binding) {
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

    private fun isWithSend(withSend: Boolean): String {
        return if (withSend) "Да" else "Нет"
    }


    private fun call() {

        val callUri = "tel: ${ad?.tel}"

        val intentCall = Intent(Intent.ACTION_DIAL)

        intentCall.data = callUri.toUri()
        startActivity(intentCall)
    }

    private fun sendEmail() {

        val intentEmail = Intent(Intent.ACTION_SEND)

        intentEmail.type = "message/rfc822"

        intentEmail.apply {

            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))

            putExtra(Intent.EXTRA_SUBJECT, "Объявление")

            putExtra(Intent.EXTRA_TEXT, "Меня интересуют Ваше объявление")
        }

        try {

            startActivity(Intent.createChooser(intentEmail, "Открыть с помощью"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Нет приложения для отправки письма", Toast.LENGTH_SHORT).show()
        }
    }


    private fun imageChangeCounter() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imagePosition = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvCount.text = "$imagePosition"
            }
        })
    }
}