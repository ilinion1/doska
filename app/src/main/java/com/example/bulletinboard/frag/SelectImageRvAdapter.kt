package com.example.bulletinboard.frag

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboard.R
import com.example.bulletinboard.act.EditAdsAct
import com.example.bulletinboard.databinding.SelectImageFragItemBinding
import com.example.bulletinboard.utils.AdapterCallBack
import com.example.bulletinboard.utils.ImageManager
import com.example.bulletinboard.utils.ImagePicker
import com.example.bulletinboard.utils.ItemTouchMoveCallback

/**
 * Данный класс - это адаптер для картинок, когда пользователь выбрал больше одной
 * Показывает в ImageListFrag
 * Так же принимает интерфейс ItemTouchAdapter, для изменения позиций в списке mainArray
 * в конструктор принимаю интерфейс
 */
class SelectImageRvAdapter(val adapterCallBack: AdapterCallBack): RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>() //тут будут храниться все элементы которые прийдут. картинки

    /**
     * Задаю разметку
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        // обычный способ надуть разметку на адаптере val view = LayoutInflater.from(parent.context).inflate(R.layout.select_image_frag_item, parent, false)
        //надуваю разметку с помозью binding
        val viewBinding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(viewBinding, parent.context, this) //передаю контекст, что бы добраться до массива стринг, с заголовками
    }

    /**
     * Заполняет данными разметку
     */
    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        // указываю какими данными заполнять разметку. функцию и тип данных я прописал ниже, в mainArray жду эти данные
        holder.setData(mainArray[position])
    }

    /**
     * Указываю количество данных, сколько раз нужно будет отрисовывать
     */
    override fun getItemCount(): Int {
        return mainArray.size //количество елементов которые нужно будет отрисовать холдеру
    }

    /**
     * Метод из интерфейса, когда меняют местами фотографии на фрагменте, в рециклере, обновляется и тут
     * Приходит начальная и конечная позиция и мы меняем местами
     */
    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos] //элемент с позиции на которую мы хотим поместить переносимый элемент
        mainArray[targetPos] = mainArray[startPos] //заменяю элемент. новый меняю на старый
        mainArray[startPos] = targetItem //заменяю элемент который начали перетаскивать, на тот на чье место поставим
        notifyItemMoved(startPos, targetPos) //указываю адаптеру что мы поменяли местами элементы. от куда и куда
    }

    override fun onClear() {
        notifyDataSetChanged() // сообщает адаптеру что изменились данные(заголовки)
    }

    /**
     * Создаю ViewHolder
     * В конструктор принимаю SelectImageFragItemBinding для binding вместо view
     */
    class ImageHolder(val viewBinding: SelectImageFragItemBinding, val context: Context, val adapter: SelectImageRvAdapter): RecyclerView.ViewHolder(viewBinding.root) {

        /**
         * В конструктор буду передавать ссылку на картинку
         * Инициализирую элементы, заголовок и картинку
         */
        fun setData(bitmap: Bitmap){

            //Слушатель нажатий на кнопку редактирования картинки
            viewBinding.imEditImage.setOnClickListener {
                //запускаю функцию получения картинок и указываю сколько максимум можно взять(1шт) и рекест код для 1 картинки
               ImagePicker.getSingleImage(context as EditAdsAct)
               context.editImagePos = adapterPosition //выдает позицию картинки на которую нажато было
            }

            //слушатель нажатий на кнопку удаления картинки одной
            viewBinding.imDeleteImage.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition) //удаляю с главного массива на позиции на которую нажал
                adapter.notifyItemRemoved(adapterPosition) //сообщаю адаптеру об удалнии обекта и указываю с какой позиции
                //цикл по перебору количества позиций в массиве, что бы сказать что данные  в элементе измениились
                for (n in 0 until adapter.mainArray.size){
                    adapter.notifyItemChanged(n) // сообщаю адаптеру что изменились данные в элементе
                }
                //запускаю функцию интерфейса для изменения видимости кнопки добавить, при нажатии на удалить в элементе
                adapter.adapterCallBack.onItemDelete()
            }

            viewBinding.tvTittle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition] // задаю текст заголовка из массива stringArray по позиции
            ImageManager.chooseScaleType(viewBinding.imageContent, bitmap) //задаю шкалу в зависимости от положения картинки
            viewBinding.imageContent.setImageBitmap(bitmap)// задаю  картинку bitmap
        }

    }

    /**
     * Функция обновляет данные из mainArray и в последствии то, что будет обновлять холдер в разметке
     * В конструктор передадим заголовки и ссылку на картинку, а дальше присвоим их mainArray
     */
    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean){
        //если в конструктор передали что хотим очистить список, очищаем
        if (needClear) mainArray.clear() // очищаем наш список
            mainArray.addAll(newList) //заполняю список с которого холдер будет обновлять разметку
            notifyDataSetChanged()//сообщаем адаптерук что данные изменились, что бы он снова запустился


    }


}