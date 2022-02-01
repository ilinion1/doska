package com.example.bulletinboard.act


import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.bulletinboard.R
import com.example.bulletinboard.adapters.ImageAdapter
import com.example.bulletinboard.model.Ad
import com.example.bulletinboard.model.DbManager
import com.example.bulletinboard.databinding.ActivityEditAdsBinding
import com.example.bulletinboard.dialogs.DialogSpinnerHelper
import com.example.bulletinboard.utils.CityHelper
import com.example.bulletinboard.utils.ImagePicker
import com.example.bulletinboard.frag.FragmentCloseInterface
import com.example.bulletinboard.frag.ImageListFrag
import com.example.bulletinboard.utils.ImageManager
import com.google.android.gms.tasks.OnCompleteListener
import java.io.ByteArrayOutputStream


/**
 * Второе активити, где открываются страницы с созданием товара
 * Так же присваиваю интерфейс созданный для отслеживания закрыт фрагмент или нет
 */
class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {

    lateinit var binding: ActivityEditAdsBinding
    var chooseImageFrag: ImageListFrag? = null
    private var dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var editImagePos = 0
    private var imageIndex = 0
    private val dbManager = DbManager()
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
    }


    private fun checkEditState() {
        isEditState = isEditState()
        if (isEditState) {

            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad

            if (ad != null) fillViews(ad!!)
        }
    }


    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }


    private fun fillViews(ad: Ad) = with(binding) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        edTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.category
        edPrice.setText(ad.price)
        edTitle.setText(ad.title)
        edDescription.setText(ad.description)
        edEmail.setText(ad.email)
        ImageManager.fillImageArray(ad, imageAdapter)
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImage.adapter = imageAdapter
        imageChangeCounter()
    }

    fun onClickedSelectCountry(view: View) {

        val listCounty = CityHelper.getAllCounties(this)
        dialog.showSpinnerDialog(this, listCounty, binding.tvCountry)

        if (binding.tvCity.text.toString() != getString(R.string.select_city)) {
            binding.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickedSelectCity(view: View) {
        val selectedCountry = binding.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) {

            val listCity = CityHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)

        } else {
            Toast.makeText(this, R.string.no_select_country, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickedSelectCat(view: View) {

        val listCategory = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCategory, binding.tvCat)

    }

    fun onClickPublish(view: View) {
        ad = fillAd()

        if (isEditState) {

            ad?.copy(key = ad?.key)?.let { dbManager.publishAd(it, onPublishFinish()) }
        } else {

            uploadImages()
        }
    }


    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish() {
                finish()
            }
        }
    }


    fun fillAd(): Ad {
        val ad: Ad
        val key = dbManager.dB.push().key
        val uid = dbManager.auth.uid
        binding.apply {
            ad = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                edTel.text.toString(),
                edIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                edEmail.text.toString(),
                "empty",
                "empty",
                "empty",
                key,
                "0",
                uid,
                System.currentTimeMillis().toString()
            )
        }
        return ad
    }


    fun onClickedGetImage(view: View) {

        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this, 3)
        } else {

            openChooseItemFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray) //обновляю адаптер битмапом(именно картинками сжатыми уже)
        }

    }


    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.skrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }


    fun openChooseItemFragment(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFrag(this)

        if (newList != null) chooseImageFrag?.resizeSelectedImage(newList, true, this)

        binding.skrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()

        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()
    }

    private fun uploadImages() {
        if (imageAdapter.mainArray.size == imageIndex) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])

        uploadImage(byteArray) {
            nextImage(it.result.toString())
        }
    }


    private fun setImageUriToAdd(uri: String) {

        when (imageIndex) {
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
        }
    }


    private fun nextImage(uri: String) {
        setImageUriToAdd(uri)
        imageIndex++
        uploadImages()
    }


    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
        val outStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray() //превращаю в набор байтов
    }


    private fun uploadImage(biteArray: ByteArray, listener: OnCompleteListener<Uri>) {

        val imStorageReference = dbManager.dBStorage.child(dbManager.auth.uid!!)
            .child("Image_${System.currentTimeMillis()}")
        val upTask = imStorageReference.putBytes(biteArray)

        upTask.continueWithTask {

                task ->
            imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }


    private fun imageChangeCounter() {
        binding.vpImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imagePosition = "${position + 1}/${binding.vpImage.adapter?.itemCount}"
                binding.tvCountImage.text = "$imagePosition"
            }
        })
    }
}


