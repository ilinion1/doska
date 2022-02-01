package com.example.bulletinboard.act

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.bulletinboard.R
import com.example.bulletinboard.databinding.ActivityFilterBinding
import com.example.bulletinboard.dialogs.DialogSpinnerHelper
import com.example.bulletinboard.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private var dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings()
        onClickedSelectCountry()
        onClickedSelectCity()
        onClickedDone()
    }


    fun actionBarSettings() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }


    private fun onClickedSelectCountry() = with(binding) {
        tvCountry.setOnClickListener {

            val listCounty = CityHelper.getAllCounties(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCounty, binding.tvCountry)

            if (tvCity.text.toString() != getString(R.string.select_city)) {
                tvCity.text = getString(R.string.select_city)
            }
        }
    }


    private fun onClickedSelectCity() = with(binding) {
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {

                val listCity = CityHelper.getAllCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity)

            } else {
                Toast.makeText(this@FilterActivity, R.string.no_select_country, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    private fun createFilter(): String = with(binding) {

        val stringBuilder = StringBuilder()

        val arrayTempFilter = listOf(
            tvCountry.text,
            tvCity.text,
            edIndex.text,
            checkBoxWithSend.isChecked.toString()
        )

        for ((i, s) in arrayTempFilter.withIndex()) {

            if (s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty()) {
                stringBuilder.append(s)
                if (i != arrayTempFilter.size - 1) stringBuilder.append("_")
            }
        }
        return stringBuilder.toString()
    }


    private fun onClickedDone() = with(binding) {
        btDone.setOnClickListener {
            Log.d("MyLog", createFilter())
        }
    }
}