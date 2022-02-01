package com.example.bulletinboard.act

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboard.R
import com.example.bulletinboard.accounthelper.AccountHelper
import com.example.bulletinboard.adapters.AdsRcAdapter
import com.example.bulletinboard.databinding.ActivityMainBinding
import com.example.bulletinboard.dialoghelper.DialogConst
import com.example.bulletinboard.dialoghelper.DialogHelper
import com.example.bulletinboard.model.Ad
import com.example.bulletinboard.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdsRcAdapter.Listener {
    lateinit var tvAccount: TextView
    lateinit var imAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val myAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
    }


    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId =
            R.id.idHome
    }


    private fun onActivityResult() {

        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

                try {

                    val account = task.getResult(ApiException::class.java)

                    if (account != null) {
                        dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Log.d("MyLog", "Api error: ${e.message}")
                }
            }
    }


    fun initRecyclerView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }


    override fun onStart() {
        super.onStart()

        uiUpdate(myAuth.currentUser)
    }


    private fun initViewModel() {

        firebaseViewModel.liveAdsData.observe(this, {
            val list = getAdsByCategory(it)

            if (!clearUpdate) {
                adapter.updateAdapter(list)
            } else {
                adapter.updateAdapterWithClear(list)
            }

            binding.mainContent.tvEmpty.visibility =
                if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        })
    }


    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad> {
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)

        if (currentCategory != getString(R.string.home)) {

            tempList.clear()

            list.forEach {

                if (currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }


    private fun init() {
        currentCategory = getString(R.string.home)
        navViewSetting()
        setSupportActionBar(binding.mainContent.toolbar)
        onActivityResult()
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccauntEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccauntImage)
    }

    fun bottomMenuOnClick() = with(binding) {

        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true

            when (item.itemId) {

                R.id.idNewAd -> {
                    val i = Intent(this@MainActivity, EditAdsAct::class.java)
                    startActivity(i)
                }
                R.id.idMyAds -> {
                    firebaseViewModel.loadMylAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)

                }
                R.id.idFavs -> {
                    mainContent.toolbar.title = getString(R.string.ad_my_fav)
                    firebaseViewModel.loadMyFavs()
                }
                R.id.idHome -> {
                    currentCategory = getString(R.string.home)
                    firebaseViewModel.loadAllAdsFirstPage()
                    mainContent.toolbar.title = getString(R.string.home)
                }
            }
            true
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true

        when (item.itemId) {
            R.id.id_my_ads -> {
                binding.mainContent.toolbar.title = getString(R.string.ad_my_ads)
                firebaseViewModel.loadMylAds()
            }
            R.id.id_car -> {
                getAdsFromCat1(getString(R.string.ad_car))
                binding.mainContent.toolbar.title = getString(R.string.ad_car)
            }
            R.id.id_pc -> {
                getAdsFromCat1(getString(R.string.ad_pc))
                binding.mainContent.toolbar.title = getString(R.string.ad_pc)
            }
            R.id.id_smart -> {
                getAdsFromCat1(getString(R.string.ad_smartphone))
                binding.mainContent.toolbar.title = getString(R.string.ad_smartphone)
            }
            R.id.id_dm -> {
                getAdsFromCat1(getString(R.string.ad_dm))
                binding.mainContent.toolbar.title = getString(R.string.ad_dm)
            }
            R.id.id_sign_up -> dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            R.id.id_sign_in -> dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            R.id.id_sign_out -> {
                if (myAuth.currentUser?.isAnonymous == true) {

                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                myAuth.signOut()
                dialogHelper.accHelper.signAutG()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun getAdsFromCat1(cat: String) {
        currentCategory = cat
        firebaseViewModel.loadAllAdsFromCat(cat)
    }


    fun uiUpdate(user: FirebaseUser?) {

        if (user == null) {

            dialogHelper.accHelper.signInAnonymously(object : AccountHelper.Listener {
                override fun onComplete() {
                    tvAccount.text = "Гость"
                    imAccount.setImageResource(R.drawable.ic_accaunt)
                }
            })
        } else if (user.isAnonymous) {
            tvAccount.text = "Гость"
            imAccount.setImageResource(R.drawable.ic_accaunt)
        } else {
            tvAccount.text = user.email

            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }


    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }


    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)

        val i = Intent(this, DescriptionAct::class.java)
        i.putExtra("AD", ad)
        startActivity(i)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.id_filter) {
            val intent = Intent(this@MainActivity, FilterActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSetting() = with(binding) {
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.ads_cat)

        val spanAdsCat = SpannableString(adsCat.title)

        spanAdsCat.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.dark_red)),
            0, adsCat.title.length, 0
        )

        adsCat.title = spanAdsCat

        val acCat = menu.findItem(R.id.ac_cat)

        val spanAcCat = SpannableString(acCat.title)

        spanAcCat.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.dark_red)),
            0, acCat.title.length, 0
        )

        acCat.title = spanAcCat
    }


    private fun scrollListener() = with(binding.mainContent) {

        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {

                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }


    private fun getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {

            if (currentCategory == getString(R.string.home)) {
                firebaseViewModel.loadAllAdsNextPage(it.time)
            } else {

                val catTime = "${it.category}_${it.time}"
                firebaseViewModel.loadAllAdsFromCatNextPage(catTime)
            }
        }
    }


    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }
}