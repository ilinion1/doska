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
class AdsRcAdapter(val act: MainActivity) : RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {
    val adArray = ArrayList<Ad>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {

        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act)
    }


    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(adArray[position])
    }


    override fun getItemCount(): Int {
        return adArray.size
    }


    fun updateAdapter(newList: List<Ad>) {
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(adArray)
        tempArray.addAll(newList)

        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(tempArray)
    }


    fun updateAdapterWithClear(newList: List<Ad>) {

        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(newList)
    }


    class AdHolder(val binding: AdListItemBinding, val act: MainActivity) :
        RecyclerView.ViewHolder(binding.root) {


        fun setData(ad: Ad) = with(binding) {

            tvDescreption.text = ad.description
            tvPrice.text = ad.price
            tvTitle.text = ad.title
            tvViewCounter.text = ad.viewsCounter
            tvFavCounter.text = ad.favCounter
            Picasso.get().load(ad.mainImage).into(mainImage)


            isFan(ad)
            showEditPanel(isOwner(ad))
            mainOnClick(ad)
        }


        private fun isFan(ad: Ad) {

            if (ad.isFav) {
                binding.ibFav.setImageResource(R.drawable.ic_fav_pressed)
            } else {
                binding.ibFav.setImageResource(R.drawable.ic_fav_normal)
            }
        }


        private fun mainOnClick(ad: Ad) = with(binding) {

            itemView.setOnClickListener {
                act.onAdViewed(ad)
            }

            ibEditAd.setOnClickListener(onClickEdit(ad))
            ibDeleteAd.setOnClickListener {
                act.onDeleteItem(ad)
            }
            ibFav.setOnClickListener {
                if (act.myAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
            }
        }


        fun onClickEdit(ad: Ad): View.OnClickListener {
            return View.OnClickListener {

                val editIntent = Intent(act, EditAdsAct::class.java).apply {

                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, ad)
                }
                act.startActivity(editIntent)
            }
        }


        private fun isOwner(ad: Ad): Boolean {
            return ad.uid == act.myAuth.uid
        }


        private fun showEditPanel(isOwner: Boolean) {

            if (!isOwner) {
                binding.editPanel.visibility = View.GONE
            } else binding.editPanel.visibility =
                View.VISIBLE

        }
    }


    interface Listener {
        fun onDeleteItem(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavClicked(ad: Ad)
    }
}