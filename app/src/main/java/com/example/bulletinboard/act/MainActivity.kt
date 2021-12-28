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


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener{
    lateinit var tvAccount: TextView // будет хранить в себе ссылку на textView из nav_header_main.xml
    lateinit var imAccount: ImageView // будет хранить в себе ссылку на imageView из nav_header_main.xml
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this) // Инициализирую класс с диалоговым окном
    val myAuth = Firebase.auth // инициализирую аутификацию через класс FirebaseAuth
    val adapter = AdsRcAdapter(this) // экземпляр адаптера для recycler
    private val firebaseViewModel: FirebaseViewModel by viewModels() //экземпляр класса viewModel, для соединения с view
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent> //лаунчер для регистрации и входа через гугл
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null //буду записывать на какую категорию нажал

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView() // запускаю функцию по инициализации recyclerView
        initViewModel() //запускаю функцию по обновлению данных в адаптере с помощью viewModel
        bottomMenuOnClick() // запускаю функцию для bottomMenu, слушатель нажатий
        scrollListener() //слушатель на скрол
    }

    /**
     * Функция для выбора в bottomMenu кнопки home, при возврате на активити
     */
    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId =
            R.id.idHome //указываю что бы было выбрано кнопка home
    }

    /**
     * В этом классе буду получать клиент google по результату,
     * что бы мог в дальнейшем с него получить токен для входа
     */
    private fun onActivityResult() {
        //инициализирую лаунчер
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            //взять ак гугл а в который вошел пользователь по интенту
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            //пытаюсь взять гугл ак, проверяю на ошибкиэ
            try {
                //получаю ак, при взятии результата буду следить за ошибками
                val account = task.getResult(ApiException::class.java)
                //если account не равен null, получаю токен
                if (account != null){
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            }catch (e: ApiException){
                Log.d("MyLog", "Api error: ${e.message}")
            }
        }
    }

    /**
     * Функция для инициализации recyclerView
     */
    fun initRecyclerView(){
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)// задаю ориентацию
            mainContent.rcView.adapter = adapter //присваиваю адаптер
        }
    }


    /**
     * Задает сначала UI в header
     */
    override fun onStart() {
        super.onStart()
        //если зарегистрирован, передаст юзер и его данные,
        // если нет, будет null и текст зайти или зарегистрироваться
        uiUpdate(myAuth.currentUser)
    }

    /**
     * Функция для обновления данных в адаптере с помощью viewModel
     */
    private fun initViewModel(){
        //будет наблюдать за изменениями данных и передает новые данные для обновления в адаптер, когда он будет готов принять
        firebaseViewModel.liveAdsData.observe(this, {
            val list = getAdsByCategory(it) //обрабатываю полученное объявление, фильтрую и переворачиваю
            //проверка, если не нажимали на кнопку дом, а по ней меняется статус clearUpdate, значит не очищаю список
            if(!clearUpdate){
                adapter.updateAdapter(list) //передаю данные в адаптер
            } else {
                adapter.updateAdapterWithClear(list) //передаю данные в адаптер и заменяю старый
            }
            //задаю видимость textView в разделе избранные
            binding.mainContent.tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        })
    }

    /**
     * Функция для изменения порядка в объявлениях, в другом порядке считывать
     * Так же фильтрует по категориям
     */
    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad>{
        val tempList = ArrayList<Ad>() //в этот временный список перегружу список со всеми объявлениями
        tempList.addAll(list)
        //если мы находимся не на главной, значит нужно отфильтровать по категориям
        if (currentCategory != getString(R.string.home)){

            tempList.clear() //стираю список где все в перемешку
            //прогоняю список и ищу по категориям
            list.forEach{
                //если объявление которое сейчас берем, соответсвует нашей категории, где мы сейчас, добавляем его
                if (currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse() //переворачиваю список
        return tempList //возвращаю список объявлений или с фильтром или общий
    }

    /**
     * функция для инициализации разных view, кнопок
     * инициализирую и создаю с нуля кнопку(гамбургер) для открытия navigateView(меню)
     * указываю navigationView(navView)будет передавать события(нажатия) именно в OnNavigationItemSelectedListener
     * который указал на уровне класса
     */
    private fun init(){
        currentCategory = getString(R.string.home) //при входе записываю что нахожусь на главной странице
        navViewSetting() //запускаю смену цвета в navigationView в категориях
        setSupportActionBar(binding.mainContent.toolbar) //указываю активити, что toolbar который я добавил в ручную, будет основным, что бы я мог добавить к нему меню
        onActivityResult() //инициализирую лаунчер для гугл входа
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle) // слушатель на drawerLayout, по нажатию на кнопку toggle
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this) //this смог указать благодаря указанию на уровне класса
        // this  еще значит что буду передавать события(клики) именно в этот класс, так как на кровне класса указан
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccauntEmail) //инициализирую и добираюсь до textView с меню, с шапки где пишется почта
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccauntImage) //нахожу imageView с фото пользователя
    }

    /**
     * Функция для слушателя нажатий на bottomMenu
     */
    fun bottomMenuOnClick() = with(binding){
        //добираюсь до кнопок в bottomNavigation
        mainContent.bNavView.setOnNavigationItemSelectedListener { item->
            clearUpdate = true
            //item и есть нажатая кнопка. проверка если нажали на эту кнопку, то это действие)
            when(item.itemId){
                //будет открывать editActivity для создания объявления
                R.id.idNewAd -> {
                    val i = Intent(this@MainActivity, EditAdsAct::class.java) //Создаю intent где указываю на какое активити держу путь
                    startActivity(i) //открываю новое активити, вкладывая intent
                }
                R.id.idMyAds -> {
                    firebaseViewModel.loadMylAds() //запускаю функцию с показом моих объявлений
                    mainContent.toolbar.title = getString(R.string.ad_my_ads) //задаю заголовок в toolbar

                }
                R.id.idFavs -> {
                    mainContent.toolbar.title = getString(R.string.ad_my_fav) //задаю заголовок в toolbar
                    firebaseViewModel.loadMyFavs() //запускаю функцию для подтягивания объявления с избранных
                }
                R.id.idHome -> {
                    currentCategory = getString(R.string.home) //записываю что нахожусь на главной странице
                    firebaseViewModel.loadAllAdsFirstPage() //запускаю функцию с показом моих объявлений с фильтром цены
                    mainContent.toolbar.title = getString(R.string.home) //задаю заголовок в toolbar
                }
            }
            true //возвращает true
        }
    }



    /**
     * слушатель нажатий на пункты в меню navigationView, задаю действия при нажатии
     * на уровне класса добавил "NavigationView.OnNavigationItemSelectedListener", требует реализацию метода
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true

        when(item.itemId){
            R.id.id_my_ads -> {
                binding.mainContent.toolbar.title = getString(R.string.ad_my_ads)
                firebaseViewModel.loadMylAds() //запускаю функцию с показом моих объявлений
            }
            R.id.id_car -> {
                getAdsFromCat1(getString(R.string.ad_car))//запускаю фильтр по категории
                binding.mainContent.toolbar.title = getString(R.string.ad_car)
            }
            R.id.id_pc -> {
                getAdsFromCat1(getString(R.string.ad_pc)) //запускаю фильтр по категории
                binding.mainContent.toolbar.title = getString(R.string.ad_pc)
            }
            R.id.id_smart -> {
                getAdsFromCat1(getString(R.string.ad_smartphone)) //запускаю фильтр по категории
                binding.mainContent.toolbar.title = getString(R.string.ad_smartphone)
            }
            R.id.id_dm -> {
                getAdsFromCat1(getString(R.string.ad_dm)) //запускаю фильтр по категории
                binding.mainContent.toolbar.title = getString(R.string.ad_dm)
            }
            R.id.id_sign_up -> dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)  //вызываю диалоговое окно по нажатию
            R.id.id_sign_in -> dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE) //диалоговое окно дял входа, из-за костанды
            R.id.id_sign_out -> {
                if(myAuth.currentUser?.isAnonymous == true) {
                    //если вход анонимный, по нажатию на кнопку выхода с ак не выйду, что бы не плодить анонимов
                    binding.drawerLayout.closeDrawer(GravityCompat.START) //закрываю navigationView при нажатии на какой-то пункт меню
                    return true
                }
                uiUpdate(null) //если нажмет кнопку выйти, передам в юзер null, что бы в дальнейшем писалось R.string.not_reg
                myAuth.signOut() // выхожу с ак
                dialogHelper.accHelper.signAutG() //функция выхода с джимейл ак, что бы после выбирать почту
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START) //закрываю navigationView при нажатии на какой-то пункт меню
        return true
    }

    /**
     * Функция фильтрации по категории
     */
    private fun getAdsFromCat1(cat: String){
        currentCategory = cat //записываю на какой категории нахожусь
        firebaseViewModel.loadAllAdsFromCat(cat) //запускаю фильтр по категории
    }

    /**
     * Функция для обновления UI, обновилась почта или же входить через анонимный вход
     * Задает картинку пользователя и текст
     */
    fun uiUpdate(user: FirebaseUser?){

        if(user == null){
            //запустится функция и интерфейс для анонимного входа
            dialogHelper.accHelper.signInAnonymously(object : AccountHelper.Listener{
                override fun onComplete() {
                    tvAccount.text = "Гость"
                    imAccount.setImageResource(R.drawable.ic_accaunt)
                }
            })
        }else if(user.isAnonymous) {
            tvAccount.text = "Гость"
            imAccount.setImageResource(R.drawable.ic_accaunt)
        } else {
            tvAccount.text = user.email
            //с помощью пикассо беру ссылку на картинку с почты т указываю где ее отобразить
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }


    /**
     * Функция интерфейса для удаления объявления
     */
    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    /**
     * Функция для записи в базу количества просмотров
     * Intent для открытия активити с описанием
     */
    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        //создаю интент для запуска DescriptionAct
        val i = Intent(this, DescriptionAct::class.java)
        i.putExtra("AD", ad) //указываю что передаю при открытии DescriptionAct
        startActivity(i) //открываю второе активити
    }

    /**
     * Добавляю меню в тулбар
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Слушатель нажатий на меню в тулбаре
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //открываю активити с фильтром
        if (item.itemId == R.id.id_filter) {
            val intent = Intent(this@MainActivity, FilterActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Запуск функции для удаления или добавления в избранные
     */
    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    /**
     * В этой функции задаю цвет категориям в navigationView
     */
    private fun navViewSetting() = with(binding){
        val menu = navView.menu //добираюсь до меню в разметке
        val adsCat = menu.findItem(R.id.ads_cat) //нахожу элемент разметки "объявление"
        //буду менять цвет побуквам, так как всему textView в меню нельзя изменить цвет
        //для начала помещаю в этот класс, для смены цвета
        val spanAdsCat = SpannableString(adsCat.title)
        //меняю цвет
        spanAdsCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.dark_red)),
            0, adsCat.title.length, 0)
        //выбираю покрашенный текст обратно в категорию
        adsCat.title = spanAdsCat

        val acCat = menu.findItem(R.id.ac_cat) //нахожу элемент разметки "аккаунт"
        //буду менять цвет побуквам, так как всему textView в меню нельзя изменить цвет
        //для начала помещаю в этот класс, для смены цвета
        val spanAcCat = SpannableString(acCat.title)
        //меняю цвет
        spanAcCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.dark_red)),
            0, acCat.title.length, 0)
        //выбираю покрашенный текст обратно в категорию
        acCat.title = spanAcCat
    }

    /**
     * Функция для скорола, слушатель как только дошли до конца списка
     */
    private fun scrollListener() = with(binding.mainContent){
        //слушатель на скрол
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            //при изменении состояния прокрутки
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //если больше не может скролиться вниз, запускаю действие и состояние покоя
                //если состояние покоя, запустится только 1 раз, когда дошли до конца
                if (!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    clearUpdate = false //меняю статус что бы стерать в дальнейшем список
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()){
                        //загружаю еще 2 объявления по фильтру категорий или общему
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    /**
     * Проверяет какая категория и исходя из этого в скроле подгружает объявления по категории
     */
    private fun getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {
            //если категория главная, то запускаю скрол для всех объявлений
            if (currentCategory == getString(R.string.home)) {
                firebaseViewModel.loadAllAdsNextPage(it.time)
            } else {
                //если не главная, значит запускаем для категории скрол
                val catTime = "${it.category}_${it.time}"
                firebaseViewModel.loadAllAdsFromCatNextPage(catTime)
            }
        }
    }

    /**
     * Будет хранить костанды для интента
     * При открытии editActivity при нажатии на кнопку редактировать объявление
     * companion object потому что просто const на активити нельзя создать
     */
    companion object{
        const val EDIT_STATE = "edit_state" //костанда для boolean, если будет true значит зашли для редактирования
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }
}