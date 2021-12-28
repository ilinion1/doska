package com.example.bulletinboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboard.act.MainActivity
import com.example.bulletinboard.R
import com.example.bulletinboard.act.DescriptionAct
import com.example.bulletinboard.act.EditAdsAct
import com.example.bulletinboard.model.Ad
import com.example.bulletinboard.databinding.AdListItemBinding
import com.squareup.picasso.Picasso

/**
 * Класс адаптер для recycler на mainActivity
 */
class AdsRcAdapter(val act: MainActivity): RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {
    val adArray = ArrayList<Ad>() //массив где будут храниться данные с базы данных

    /**
     * Надуваю разметку шаблона
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        //надуваю разметку и возвращаю обновленный холдер по классике, не через binding
        //val view = LayoutInflater.from(parent.context).inflate(R.layout.ad_list_item, parent, false)
        //через биндинг надуваю разметку
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act)
    }

    /**
     * Взаимодействую с элементами
     * Заполняю данными разметку
     */
    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(adArray[position])
    }

    /**
     * Указываю количество элементов которые нужно будет отрисовать
     */
    override fun getItemCount(): Int {
        return adArray.size
    }

    /**
     * Функция для обновления адаптера
     */
    fun updateAdapter(newList: List<Ad>) {
        val tempArray = ArrayList<Ad>() //временный список для сохранения данных, перед скролом
        tempArray.addAll(adArray) //добавляем старый список
        tempArray.addAll(newList) //добавляем новый список
        //функция для вычисления разницы в объявлении и что нужно сделать, какой notify применить
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
        diffResult.dispatchUpdatesTo(this) //применяю изменения к адаптеру
        adArray.clear()//стираю список
        adArray.addAll(tempArray) //добавляю полученные данные в массив для работы с холдером
    }

    /**
     * Функция для обновления адаптера с очищением
     */
    fun updateAdapterWithClear(newList: List<Ad>) {

        //функция для вычисления разницы в объявлении и что нужно сделать, какой notify применить
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList))
        diffResult.dispatchUpdatesTo(this) //применяю изменения к адаптеру
        adArray.clear()//стираю список
        adArray.addAll(newList) //добавляю полученные данные в массив для работы с холдером
    }

    /**
     * Класс viewHolder, хранит элементы разметки
     * В конструктор передаю binding и auth для доступа к id пользователя
     */
    class AdHolder(val binding: AdListItemBinding, val act: MainActivity) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * функция для заполнения разметки, будет использована в onBindViewHolder
         */
        fun setData(ad: Ad) = with(binding) {

            tvDescreption.text = ad.description
            tvPrice.text = ad.price
            tvTitle.text = ad.title
            tvViewCounter.text = ad.viewsCounter
            tvFavCounter.text = ad.favCounter
            //с помощью пикассо беру с базы данных картинку и отображаю
            Picasso.get().load(ad.mainImage).into(mainImage)


            isFan(ad) //функция определения картинки на избранном
            showEditPanel(isOwner(ad)) //запускаю функцию по скрыванию панели инструментов
            mainOnClick(ad) //функция с прослушивателями кликов
        }

        /**
         * Функция для setText определяет нажатое сердчеко или нет и картинку ему задает
         */
        private fun isFan(ad: Ad){
            //обновляю сердце в избранных если нажаток
            if(ad.isFav){
                binding.ibFav.setImageResource(R.drawable.ic_fav_pressed)
            } else {
                binding.ibFav.setImageResource(R.drawable.ic_fav_normal)
            }
        }

        /**
         *Функция для setData для прослушивания кликов
         */
        private fun mainOnClick(ad: Ad) = with(binding){
            //слушатель нажатий на весь элемент объявления
            itemView.setOnClickListener {
                //запускаю функцию для записи в базу данных количества просмотров и интент для открытия описания
                act.onAdViewed(ad)
            }
            //слушатель нажатий на кнопку редактирования в списке объявлений
            ibEditAd.setOnClickListener(onClickEdit(ad))//запускаю функцию с логикой при нажатии на редактировать
            ibDeleteAd.setOnClickListener {
                //запускаю интерфейс логикой при нажатии на удалить
                act.onDeleteItem(ad)
            }
            ibFav.setOnClickListener {
                if (act.myAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
            }
        }


        /**
         * В этой функции задаю логику слушателю нажатий на кнопку редактировать объявление
         */
        fun onClickEdit(ad: Ad): View.OnClickListener {
            return View.OnClickListener {
                //создаю интент и  прикрепляю к нему пложенное сообщение
                val editIntent = Intent(act, EditAdsAct::class.java).apply {
                    //передаю boolean состояния. Если true значит зашли для редактирования, а не создания нового
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, ad) //передаю класс с данными
                }
                act.startActivity(editIntent) //запускаю активити
            }
        }

        /**
         * В этой функции проверяю, это владелец объявления или нет, по id
         */
        private fun isOwner(ad: Ad): Boolean {
            return ad.uid == act.myAuth.uid
        }

        /**
         * Эта функция показывает или прячет панель инструментов
         * В зависимости твое объявление или нет
         */
        private fun showEditPanel(isOwner: Boolean) {
            //если не владелец объявления, прячу панель инструментов
            if (!isOwner) {
                binding.editPanel.visibility = View.GONE
            } else binding.editPanel.visibility =
                View.VISIBLE //показываю панель если хозяин смотрит

        }
    }

    /**
     * Интерфейс для запуска функции удаления, запустится на mainActivity
     * И для функции по обновлению количества просмотров
     */
    interface Listener {
        fun onDeleteItem(ad: Ad) //для функции удаления
        fun onAdViewed(ad: Ad) //для записи в базу количества просмотров
        fun onFavClicked(ad: Ad) //для избранных
    }
}