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
    var chooseImageFrag: ImageListFrag? = null //экземпляр класса фрагмента
    private var dialog = DialogSpinnerHelper() //создаем экземпляр класса с диалогом
    lateinit var imageAdapter: ImageAdapter //адаптер для слайдера ViewPager
    var editImagePos = 0 //переменная которая хранит номер позиции картинки которую хотим изменить
    private var imageIndex = 0 //хранит номер позиции картинки которую хотим загрузить
    private val dbManager = DbManager() //создаю экземпляр класса базы данных
    private var isEditState = false
    private var ad: Ad? = null //класс объявления, в него передам данные для редактирования если для этого открыто или будет пустое если для нового

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()//запускаю функцию которая смотрит нажато ли с кнопки редактировать обяъвление и если да, передает данные

        //создаю адаптер, что бы подключить к спинеру, не пригодилось, но вот как создать адаптер для спинера
        //val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, CityHelper.getAllCounties(this))
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) //DropDown для чего-то создаю
        //binding.spContry.adapter = adapter // устанавливаю адаптер к спинеру
    }

    /**
     * Функция на основании проверки открыто активити с кнопки редактировать или создать новое объявление
     * Выполняет действия
     */
    private fun checkEditState(){
        //переменной присваиваю полученный boolean с intent, значит открыто для редактирования или нет
        isEditState = isEditState()
        //если передали true, тоесть нажали с кнопки редактировать, запускаю функцию для передачи данных с объявления
        if (isEditState) {
            //получаю класс с данными переданными через intent
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            //передаю его в функцию для заполнения полей
            if(ad != null) fillViews(ad!!)
        }
    }

    /**
     * Функция проверяет состояние, зашли для создания нового объявления или для редактирования
     */
    private fun isEditState(): Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    /**
     * Функция заполняет view, после нажатия с mainAct на кнопку редактирования обяъвления
     */
    private fun fillViews(ad: Ad) = with(binding){
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


    /**
     * Эта функция инициализирует элементы
     * Подключаю ImageAdapter к слайдеру ViewPager
     */
    private fun init(){
        imageAdapter = ImageAdapter() //инициализирую адаптер
        binding.vpImage.adapter = imageAdapter //подключаю адаптер к ViewPager
        imageChangeCounter() //функция счетчик для viewPager
    }

    /**
     * Эта функция со слушателем нажатия на textView для выбора страны
     * по нажатию запускает диалог
     * передаю в конструктор view, что бы можно было в хml добавить ее как слушатель нажатий к элементу разметки
     */
    fun onClickedSelectCountry(view: View){
        //создаем список стран, который будем передавать в диалог и там показывать
        val listCounty = CityHelper.getAllCounties(this) // инициализирую список стран, созданный в CityHelper
        dialog.showSpinnerDialog(this, listCounty, binding.tvCountry) // запускаю диалог, передавая контекст,список стран и textView
        //проверяем, если был выбран город и текст уже не тот что по умолчанию, устанавливаем что по умолчанию
        if(binding.tvCity.text.toString() != getString(R.string.select_city)){
            binding.tvCity.text = getString(R.string.select_city)
        }
    }

    /**
     * Эта функция со слушателем нажатия на textView для выбора города
     * по нажатию запускает диалог
     * передаю в конструктор view, что бы можно было в хml добавить ее как слушатель нажатий к элементу разметки
     */
    fun onClickedSelectCity(view: View){
        val selectedCountry = binding.tvCountry.text.toString() //нахожу textView где отображает выбранную страну
        //проверяем если textView отображает текст по умолчанию, что значит что страну еще не выбрали, пишу что бы выбрал
        //если страна выбрана, запускает диавлоговое окно для выбора города
        if (selectedCountry != getString(R.string.select_country)){
            //создаем список стран, который будем передавать в диалог и там показывать
            val listCity = CityHelper.getAllCities(selectedCountry,this) // инициализирую список городов, созданный в CityHelper
            dialog.showSpinnerDialog(this, listCity, binding.tvCity) // запускаю диалог, передавая контекст и список городов и textView

        } else {
            Toast.makeText(this, R.string.no_select_country, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Эта функция со слушателем нажатия на textView для выбора категории
     * по нажатию запускает диалог
     */
    fun onClickedSelectCat(view: View){

        //получаю массив с категориями и преварящаю в  ArrayList
        val listCategory = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCategory, binding.tvCat) // запускаю диалог, передавая контекст и список категорий и textView

    }

    /**
     * Функция для слушателя нажатий на кнопку публикация
     */
    fun onClickPublish(view: View){
        ad = fillAd() //переменной присвоил функцию заполнения базы данных данными объявления
        //если это состояние редактирования
        if (isEditState){
            //запускаю функцию по добавлению в базу данных, но с созданием копии и изменением элементов некоторых
                //в данном случае key, что бы не создавалось новое объявление после редактирования
            ad?.copy(key = ad?.key)?.let { dbManager.publishAd(it , onPublishFinish())  } //заменяю на тот же ключ, место автогенирируещего
        } else {
            //если состояние для создания нового объявления
            uploadImages() //запускаю функцию загрузки картинок в базу данных
        }
    }

    /**
     * Функция применения интерфейса для закрытия окна заполнения объявления
     */
    private fun onPublishFinish(): DbManager.FinishWorkListener{
        return object : DbManager.FinishWorkListener{
            override fun onFinish() {
                finish()//закрываю активити
            }
        }
    }

    /**
     * Функция заполнения базы данных
     * Заполняет данными из полей и генерирует ключ уникальный
     * Возвращает обновленный список данных
     */
    fun fillAd(): Ad{
        val ad: Ad //экземпляр класса с объявлениями, нужно будет еще инициализировать
        val key = dbManager.dB.push().key //генерирую уникальный ключ из базы данных
        val uid = dbManager.auth.uid //id пользователя
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
            )// инициализирую класс объявления
        }
        return ad
    }

    /**
     * Эта функция со слушателем нажатия на imageButton для редактирования или добавления картинок
     */
    fun onClickedGetImage(view: View) {
        //если в ViewPager нету картинки, то есть размер массива с адаптера пустой, запускаю код

        if(imageAdapter.mainArray.size == 0){
            ImagePicker.getMultiImages(this, 3) //запускаю launcher
        } else{
            //если не пустой, запускаю фрагмент передавая null говоря о том, что там нет ссылок на картинки, там уже bitmap обработанные
            openChooseItemFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray) //обновляю адаптер битмапом(именно картинками сжатыми уже)
        }

    }

    /**
     * Функция из интерфейса
     * Передадится во фрагмент, что бы по закрытию фрагмента снова отображало элементы на editActivity
     * Получаю выбранные картинки
     */
    override fun onFragClose(list: ArrayList<Bitmap>){
        binding.skrollViewMain.visibility = View.VISIBLE // показываю содержимое EditActivity
        imageAdapter.update(list) //передаю в адаптер полученные картинки с фрагмента
        chooseImageFrag = null //присваиваю экземпляру фрагмента null, что бы срабаттывало условие по открытию фрагмента
    }

    /**
     * Эта функция открывает фрагмент
     */
    fun openChooseItemFragment(newList: ArrayList<Uri>?){
        chooseImageFrag =  ImageListFrag(this) //инициализирую фрагмент
        //функция по сжатию картинки.второго потока  и диалогового окнка с прогресс баром
        if (newList != null )chooseImageFrag?.resizeSelectedImage(newList, true, this)
        //ImagePicker.getImages(this) // запускаю функцию по доступу к картинкам на телефоне
        binding.skrollViewMain.visibility = View.GONE // прячу содержимое EditActivity
        val fm = supportFragmentManager.beginTransaction() //создаю фрагмент менеджера
        // заменяю экран в editAct на экран фрагмента и передаю интерфейс в конструктор фрагмента
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit() // применяю измененния
    }

    /**
     * В этой функции загружаю все картинки на хранилище и загружаю с хранилища для публикации
     */
    private fun uploadImages(){
        if (imageAdapter.mainArray.size == imageIndex){
            dbManager.publishAd(ad!!, onPublishFinish()) //запускаю функцию по добавлению в базу данных текста
            return
        }
        val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex]) //превращаю в byte картинки со слайдера
        //загружаю в хранилище картинку и после получаю ссылку на нее и указываю listener {}
        uploadImage(byteArray) {
            //dbManager.publishAd(ad!!, onPublishFinish()) //запускаю функцию по добавлению в базу данных текста
            nextImage(it.result.toString())
        }
    }

    /**
     * Функция по добавлению картинок по очереди в объявление
     */
    private fun setImageUriToAdd(uri: String){
        //в зависимости от того, что в индексе, такую картинку и загружаю
        when(imageIndex){
            0 -> ad = ad?.copy(mainImage = uri) //копирую все, но переписываю картинку
            1 -> ad = ad?.copy(image2 = uri) //копирую все, но переписываю картинку
            2 -> ad = ad?.copy(image3 = uri) //копирую все, но переписываю картинку
        }
    }

    /**
     * Функция которая меняет index для выбора картинки Ad
     */
    private fun nextImage(uri: String){
        setImageUriToAdd(uri) //добавляю в объяавление картинку
        imageIndex++ //увеличиваю счетчик на 1
        uploadImages() //запускаю снова функцию загрузки картинок на хранилище, но уже индекс в ней увеличен на 1
    }

    /**
     * В этой функции подготавливаю картинки к загрузке на storage
     * Нужно привратить bitmap в массив байтов в котором он пойдет в хранилище
     */
    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray{
        val outStream = ByteArrayOutputStream() //подготавливаю объект OutputStream
        //сжимаю картинку в JPEG и качество в процентах и когда все сожмет превратится в OutputStream
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray() //превращаю в набор байтов
    }

    /**
     * В этой функции загружаю одну картинку на хранилище
     * Второстепенный поток для загрузки идет от firebase, но нужен слушатель(интерфейс) в конструкторе, что бы
     * знать когда закончит загрузку
     */
    private fun uploadImage(biteArray: ByteArray, listener: OnCompleteListener<Uri>){
        //нахожу и создаю новый узел с картинками пользователя и конечная точка имя с временем загрузки
        val imStorageReference = dbManager.dBStorage.child(dbManager.auth.uid!!)
            .child("Image_${System.currentTimeMillis()}")
        val upTask = imStorageReference.putBytes(biteArray) //байты(картинка) запишется в данный путь
        //когда загрузится, выдаст ссылку на картинку в хранилище и по ней можно будет ее получать
        //что делать когда загрузит
        upTask.continueWithTask {
            //скачиваю ссылку картинки которую загрузил
            //addOnCompleteListener что будет когда скачал. когда получу ссылку запускаю интерфейс
            task -> imStorageReference.downloadUrl
        }.addOnCompleteListener(listener)
    }

    /**
     * Функция для обновления счетчика в viewPager
     */
    private fun imageChangeCounter(){
        //добавляю слушатель viewPager, что бы отслеживать на какой позиции сейчас нахожусь в слайдере
        binding.vpImage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imagePosition = "${position + 1}/${binding.vpImage.adapter?.itemCount}"
                binding.tvCountImage.text = "$imagePosition"
            }
        })
    }
}


