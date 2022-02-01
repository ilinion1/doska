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
    private val fragCloseInt: FragmentCloseInterface
) : BaseAdsFrag(), AdapterCallBack {

    lateinit var binding: ListImageFragBinding
    val adapter = SelectImageRvAdapter(this)

    val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    var job: Job? = null
    private var addImageItem: MenuItem? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListImageFragBinding.inflate(layoutInflater)
        adView = binding.adView
        return binding.root
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFrag)?.commit()

        fragCloseInt.onFragClose(adapter.mainArray)
        job?.cancel()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        binding.apply {
            touchHelper.attachToRecyclerView(rcViewSelectImage)
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
            rcViewSelectImage.adapter = adapter
        }
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }


    fun resizeSelectedImage(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity) {

        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity)
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }
    }


    private fun setUpToolBar() {
        binding.apply {

            tb.inflateMenu(R.menu.menu_choose_image)
            val deleteItem =
                tb.menu.findItem(R.id.delete_image)
            addImageItem = tb.menu.findItem(R.id.add_image)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false

            tb.setNavigationOnClickListener {
                showInterAd()
            }


            deleteItem.setOnMenuItemClickListener {
                adapter.updateAdapter(
                    ArrayList(),
                    true
                )
                addImageItem?.isVisible = true
                true
            }
        }



        addImageItem?.setOnMenuItemClickListener {

            val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.addImages(activity as EditAdsAct, imageCount)
            true
        }
    }


    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity) {
        resizeSelectedImage(newList, false, activity)
    }


    fun setSingleImage(uri: Uri, pos: Int) {
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity)
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }
    }


    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }


}