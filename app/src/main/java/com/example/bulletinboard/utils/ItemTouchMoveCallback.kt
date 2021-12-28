package com.example.bulletinboard.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Этот класс для передачи его в touchHelper,
 * что бы определить что делать когда нажал перетащить или отпустил нажатие и тп
 * В конструктор передаем интерфейс
 */
class ItemTouchMoveCallback(val adapter: ItemTouchAdapter): ItemTouchHelper.Callback() {

    /**
     * В этой функции указывает какие именно движения хотим замечать
     * В моем случае вверх и вниз
     */
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN //указываю что отслеживаю движение вверх или вниз
        return makeMovementFlags(dragFlag, 0) //вернули значение функции
    }

    /**
     * Функция запускается когда мы двигаем элемент
     */
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        //запускаю функцию с интерфейса и передается в SelectImageRvAdapter
        adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    /**
     * Функция для свайпа, мы не используем
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    /**
     * Функция запускается когда нажат элемент и что с ним происходит.
     * В нашем случае станет полупрозрачным
     */
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        //если состояние не ACTION_STATE_IDLE то задаем всему элементу прозрачность
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) viewHolder?.itemView?.alpha = 0.5f
        super.onSelectedChanged(viewHolder, actionState)
    }

    /**
     * Изменяет состояние элемента, когда мы его отпускаем
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.alpha = 1.0f //возвращаю непрозрачность
        //запускаю функцию интерфейса, в которой логику инициализировал в адаптере. Обновляет адаптер когда сменился заголовок
        adapter.onClear()
        super.clearView(recyclerView, viewHolder)
    }

    /**
     * Интерфейс, будет передаваться на адаптер, что бы сообщать ему о изменениях положений элементов
     * То что будет происходить в этом классе, будет проиходить и в адаптере
     */
    interface ItemTouchAdapter{
        /**
         * Передает startPos - место какое занимает и targetPos - место элемента чье место хотим занять
         */
        fun onMove(startPos: Int, targetPos: Int)
        /**
         * Что бы адаптер обновился, когда сменились заголовки
         */
        fun onClear()
    }
}