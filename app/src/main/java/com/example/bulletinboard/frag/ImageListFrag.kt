package com.example.bulletinboard.frag

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletinboard.R
import com.example.bulletinboard.act.EditAdsAct
import com.example.bulletinboard.databinding.ListImageFragBinding
import com.example.bulletinboard.dialoghelper.ProgressDialog
import com.example.bulletinboard.utils.AdapterCallBack
import com.example.bulletinboard.utils.ImageManager
import com.example.bulletinboard.utils.ImagePicker
import com.example.bulletinboard.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Класс фрагмента где будет отображаться работа с картинками
 * Этот фрагмент заменит экран EditActivity
 * В конструктор принимаю интерфейс  и массив если пользователь выбрал больше чем 1 картинку
 */
class ImageListFrag(
    private val fragCloseInt : FragmentCloseInterface): BaseAdsFrag(), AdapterCallBack {

    lateinit var binding: ListImageFragBinding
    val adapter = SelectImageRvAdapter(this) //инициализирую адаптер указывая иннтерфейс в параметрах
    //экземпляр класса, который задает параметры движения, в конструктор передал интерфейс
    val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback) //Следит что бы пользователь мог перетаскивать элементы внутри recycler
    var job: Job? = null //переменная для корутины, что бы можно было ее прервать, закрыть
    private var addImageItem: MenuItem? = null

    /**
     * Функция для рисования разметки, экрана фрагмента(надуть)
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ListImageFragBinding.inflate(layoutInflater)
        adView = binding.adView //инициализирую adView из родительского фрагмента из своей разметки
        return binding.root
    }

    /**
     * Интерфуйс, при нажатии на закрытие рекламы
     * Закрывает фрагмент
     */
    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFrag)?.commit() //закрывает фрагмент
        //запускается функция интерфейса, которая делает видимыми элементы editActivity и передаю выбранные картинки
        fragCloseInt.onFragClose(adapter.mainArray)
        job?.cancel() //останавливаю корутину в процессе работы, если пользователь не дождался
    }

    /**
     * В этой функции буду взаимодействовать с view после того как жкран был отрисован в функции onCreateView()
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar() //запускаю функцию с разметкой для ToolBar
        binding.apply {
            touchHelper.attachToRecyclerView(rcViewSelectImage) //подключаю touchHelper к recyclerView
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity) //указал какой будет по структуре recycler с помощью менеджера
            rcViewSelectImage.adapter = adapter //присваиваю адаптер к recyclerView
        }
    }

    /**
     * В этой функции буду принимат ь bitmap для обновления адаптера
     */
    fun updateAdapterFromEdit(bitmapList: List<Bitmap>){
        adapter.updateAdapter(bitmapList, true) //обновляю адаптер
    }


    /**
     * Функция по созданию второго потока для сжатия картинок
     * Так же запускает диалог
     */
    fun resizeSelectedImage(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity){
        //запускаю функцию со второстепенного потока, сжатия размера картинки через корутину на основном потоке, так как функция suspend
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity) //запускаю диалоговое окно с прогресс баром
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()//закрываю диалог
            adapter.updateAdapter(bitmapList, needClear) //обновляю адаптер
            //проверяю нужно ли прятатать кнопку добавяления
            if(adapter.mainArray.size > 2) addImageItem?.isVisible = false //прячу кнопку добавления картинок
        }
    }

    /**
     * Эта функция настраивает toolBar
     */
    private fun setUpToolBar() {
        binding.apply {

            tb.inflateMenu(R.menu.menu_choose_image) //надуваю ToolBar указываю что за разметка внутри
            val deleteItem =
                tb.menu.findItem(R.id.delete_image) //инициализирую кнопку удалить в меню
            addImageItem = tb.menu.findItem(R.id.add_image) //инициализирую кнопку добавить в меню
            if(adapter.mainArray.size > 2) addImageItem?.isVisible = false //прячу кнопку добавления картинок

            //слушатель нажатий для кнопки назад в меню
            tb.setNavigationOnClickListener {
                showInterAd()//показывает рекламу на всю страницу
            }

            //слушатель нажатий на кнопку из меню удалить
            deleteItem.setOnMenuItemClickListener {
                adapter.updateAdapter(
                    ArrayList(),
                    true
                ) //передаю пустой массив в адаптер, тем самым очищая его
                addImageItem?.isVisible = true //делаю видимой кнопку добавить
                true
            }
        }


        //слушатель нажатий на кнопку из меню добавить
        addImageItem?.setOnMenuItemClickListener {
            //максимальное количество минус размер массива. это то число которое можно добавить
            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            //запускает по новой доступ к фотографиям, но теперь нужно с учетом максимального ограничения и того что по факту есть
            ImagePicker.addImages(activity as EditAdsAct, imageCount)
            true
        }
    }

    /**
     * Эта функция обновляет адаптер
     */
    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity){
        //запускаю функицию со вторым потоком для сжатия картинок и прогресс бар
        resizeSelectedImage(newList,false, activity)
    }

    /**
     * В этой функции получаю новую картинку
     */
    fun setSingleImage(uri: Uri,pos: Int){
        //нахожу прогресс бар на разметке recycler для адаптера
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        //запускаю функцию со второстепенного потока, сжатия размера картинки через корутину на основном потоке, так как функция suspend
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE //делаю видимым прогресс бар
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity) //функция сжатия картинки
            pBar.visibility = View.GONE //делаю невидимым прогресс бар
            //указываю позицию на которую нажал и добавляю одну картинку в адаптер
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)// говорю адаптеру что данные  на позиции изменились изменились
        }
    }

    /**
     * Реализация интерфейса, при нажатии кнопки удалить на элементе, показывается кнопка добавить
     */
    override fun onItemDelete() {
        addImageItem?.isVisible = true //делаю видимой кнопку добавить
    }


}