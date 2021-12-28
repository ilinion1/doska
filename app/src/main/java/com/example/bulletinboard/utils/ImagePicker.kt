package com.example.bulletinboard.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import com.example.bulletinboard.R
import com.example.bulletinboard.act.EditAdsAct
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * В этом классе будем получать картинки, что бы дальше с ними работать
 */
object ImagePicker {
    const val MAX_IMAGE_COUNT = 3 //Максимальное количество картинок

    /**
     * В функции указываются опции, для доступа к памяти или камере, для получения от туда картинок
     */
    private fun getOptions(imageCounter: Int): Options {
        //Настраиваю параметры
        val options = Options().apply {
            count =
                imageCounter    //Количество изображений, для которых нужно ограничить количество выделенных фрагментов
            isFrontFacing = false   //Передняя камера при запуске
            mode = Mode.Picture    //Возможность выбрать только изображения
            path = "/pix/images"   //Пользовательский путь для хранилища мультимедиа
        }
        return options
    }

    /**
     * Функция для выбора нескольких картинок
     */
    fun getMultiImages(edAct: EditAdsAct, imageCount: Int) {
        //указываю где появится камера с картинками из хранилища.это фрагмент
        //getOptions(imageCount)) количесвто фото которые разрешаем сделать
        edAct.addPixToActivity(R.id.placeHolder, getOptions(imageCount)) { result ->
            when (result.status) {
                //если успешно получены картинки,то
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImage(
                        edAct,
                        result.data
                    ) //запускаю функцию получения многих картинок и работы с ними
                }
            }
        }
    }

    /**
     * Функция для выбора нескольких картинок, повторно при наличии выбранных
     */
    fun addImages(edAct: EditAdsAct, imageCount: Int) {

        //указываю где появится камера с картинками из хранилища.это фрагмент
        //getOptions(imageCount)) количесвто фото которые разрешаем сделать
        edAct.addPixToActivity(R.id.placeHolder, getOptions(imageCount)) { result ->
            when (result.status) {
                //если успешно получены картинки,то
                PixEventCallback.Status.SUCCESS -> {

                    //возвращаю инстанцию фрагмента, что бы заменить фрагмент от pix
                    openChooseImageFrag(edAct)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct) //обновляю фрагмент
                }
            }
        }
    }

    /**
     * Функция для выбора одной картинки
     */
    fun getSingleImage(edAct: EditAdsAct) {
        //указываю где появится камера с картинками из хранилища.это фрагмент
        //getOptions(imageCount)) количесвто фото которые разрешаем сделать
        edAct.addPixToActivity(R.id.placeHolder, getOptions(1)) { result ->
            when (result.status) {
                //если успешно получены картинки,то
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct) //показываю фрагмент
                    singleImage(edAct, result.data[0]) //обновляю фрагмент с одной картинкой
                }
            }
        }
    }

    /**
     * Функция для замены фрагмента, от pix на тот что был с редактированием фото
     */
    private fun openChooseImageFrag(edAct: EditAdsAct){
        //заменяю фрагмент
        edAct.supportFragmentManager.beginTransaction().replace(R.id.placeHolder, edAct.chooseImageFrag!!).commit()
    }

    /**
     * Функция для закрытия фрагмента
     */
    private fun closePixFragment(edAct: EditAdsAct) {
        //создаю переменную с фрагментом который открвается, для его закрытия
        val fList = edAct.supportFragmentManager.fragments //получаю все фрагменты с активити
        //прогоняю список фрагментов
        fList.forEach {
            //если фрагмент видимый, удаляю его
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }


    /**
     * Функция обрабатывает сколько картинок было выбрано и что с ними нужно сделать
     */
    fun getMultiSelectImage(edAct: EditAdsAct, uris: List<Uri>) {


        //если получено с телефона больше 1 картинки и фрагмент еще не открыл, равен нулю,
        // запускаю новый фрагмент
        if (uris.size > 1 && edAct.chooseImageFrag == null) {

            edAct.openChooseItemFragment(uris as ArrayList<Uri>) //запускаю фрагмент

        }  else if (uris.size == 1 && edAct.chooseImageFrag == null) {
            //если получена одна картинка, фрагмент ранее не создавался, обновляю адаптер в слайдере и все
            //создаю корутину для основного потока, где запущу функцию для получения фото уже в битмап
            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pBarLoad.visibility = View.VISIBLE //делаю видимым прогресс бар
                val bitmapArray = ImageManager.imageResize(
                    uris,
                    edAct
                ) as ArrayList<Bitmap> //запускаю функцию по изменению размера фото
                edAct.binding.pBarLoad.visibility = View.GONE //прячу прогресс бар
                edAct.imageAdapter.update(bitmapArray) //обновляю адаптер для слайдера
                closePixFragment(edAct) //закрываю фрагмент
            }
        }
    }



    /**
     * Функция для получения результата при изменении одной картинки
     * Ожидаю результат полученный
     */
    private fun singleImage(edAct: EditAdsAct, uri: Uri) {

        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos)
    }
}