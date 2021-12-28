package com.example.bulletinboard.utils


import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.example.bulletinboard.adapters.ImageAdapter
import com.example.bulletinboard.model.Ad
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Класс конвертирует размер картинки в необходимый для чтения размер, если она сильно большая
 */
object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000 //максимальный размер картинки
    private const val WIDTH= 0 //ширина
    private const val HEIGHT = 1 //высота

    /**
     * Функция выдает размер картинки
     * В конструктор передается ссылка картинки которую выбрали из хранилища смартфона
     * Возвращаем список из двух позиций, высоты и ширины
     */
    fun getImageSize(uri: Uri, act: Activity): List<Int>{
        val inStream = act.contentResolver.openInputStream(uri) //получаю inputStream

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true //указываю что бы с картинки которую получаем, взяли только края
        }
        BitmapFactory.decodeStream(inStream, null, options) //указываю что получаю из файла картинку

        //проверяем, если наклон равен  90, меняю высоту и ширину местами
        return listOf(options.outWidth, options.outHeight) //возвращаю список с высотой и шириной
    }


    /**
     * Эта функция сжимает размер картинки
     * указал что функция   suspend и withContext(Dispatchers.IO), для запуска в фоновом режиме
     */
    suspend fun imageResize(uris: List<Uri>, act: Activity): List<Bitmap> = withContext(Dispatchers.IO){
        //создаю массив списков который принимает высоту и ширину картинки до которых хотим уменьшить
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>() //список Bitmap картинок, полученных после сжатия
        //с помощью цикла перебираем массив переданный в функцию
        for (n in uris.indices){
            val size = getImageSize(uris[n], act) //берем ширину и высоту с переданного списка, с позиции n
            //вычисляем пропорцию. ширина деленная на высоту и перевожу в Float() с плавающей точкой число
            val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()
            // вычесляем размер который хотим для картинки задать. для этого нужно выбрать самую большую сторону
            //если ширина больше высоты, пропорция будет > 1. если ширина меньше высоту, будет меньше 1
            if (imageRatio>1){

                //проверяю если ширина картинки больше максимального размера, то уменьшаю размер
                if (size[WIDTH] > MAX_IMAGE_SIZE){
                    //задаю в новый список новую максимальную ширину картинки и высоту по пропорции(ширина делится на полученную пропорцию)
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                } else{
                    //если картинка не привышает максимальный размер, тогда в список записываем ее размеры
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            }else{
                //если картинка вертикальная
                //проверяю если высота картинки больше максимального размера, то уменьшаю размер
                if (size[HEIGHT] > MAX_IMAGE_SIZE){
                    //задаю в новый список ширину по пропорции(высота умножается на полученную пропорцию) и новую максимальную высоту картинки
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else{
                    //если картинка не привышает максимальный размер, тогда в список записываем ее размеры
                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                }
            }
        }
       //создаю цикл, что бы проверить сколько размеров передано и соответсвенно сколько позиций в нем и передаю в лист позицию
        for (i in uris.indices){
            //блок для нахождения ошибки в трудоемком потоке
            //val e =
                kotlin.runCatching {
                //с помощью Picasso добираюсь до файла картинки и добавляю его в лист как bitmap и указываю размер каким должен быть
                bitmapList.add(Picasso.get().load(uris[i]).resize(tempList[i][WIDTH], tempList[i][HEIGHT]).get())

            }
            //в этом разделе с помощью $e.isSuccess проверяю успешно tru или false прошел блок выше. без ошибок ли.
            //если ошибка, могу показать тост к примеру с ошибкой. может ссылка на картинку не верная или тп


        }
        return@withContext bitmapList
    }

    /**
     * Функция задает тип шкалы, в зависимости от вертикальная или горизонтальная картинка
     */
    fun chooseScaleType(im: ImageView, bitmap: Bitmap){
        //проверяю если битмап ширина больше высоты, картинка горизонтальная и задаем шкалу
        if (bitmap.width > bitmap.height){
            im.scaleType = ImageView.ScaleType.CENTER_CROP //задаю шкалу для горизонтальной
        }else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE //задаю шкалу для вертикальной
        }
    }

    /**
     * Получаю bitmap с firebase без сжатия размера
     */
    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO){

        val bitmapList = ArrayList<Bitmap>() //список Bitmap картинок

        //создаю цикл, что бы проверить сколько размеров передано и соответсвенно сколько позиций в нем и передаю в лист позицию
        for (i in uris.indices){
            kotlin.runCatching {
                //с помощью Picasso добираюсь до файла картинки и добавляю его в лист как bitmap
                bitmapList.add(Picasso.get().load(uris[i]).get())

            }
            //в этом разделе с помощью $e.isSuccess проверяю успешно tru или false прошел блок выше. без ошибок ли.
            //если ошибка, могу показать тост к примеру с ошибкой. может ссылка на картинку не верная или тп


        }
        return@withContext bitmapList
    }

    /**
     * Получение ссылкок на картинки с базы и передаю их в список для получения bitmap
     */
    fun fillImageArray(ad: Ad, adapter: ImageAdapter){
        //достаю из объявоения все ссылки на картинки
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        //создаю корутину для второго потока, где будет в битмап превращаться ссылка на картинку
        //Dispatchers.Main указываю что на основном потоке будет работать. но саму функция сделает работу на второстепенном
        CoroutineScope(Dispatchers.Main).launch {
            //запускаю второпоточную функцию по преобразованию с ссылки в битмап
            val bitmapList = getBitmapFromUris(listUris)
            adapter.update(bitmapList as ArrayList<Bitmap>)//обновляю адаптер
        }
    }
}