package com.example.bulletinboard.model
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

/**
 * Класс через который будем работать с базой данных
 * В конструктор передаю калл бак, для работы с интерфейсом. что бы можно было в него передать список считанных данных
 */
class DbManager {
    //инициализирую  dataBase и узел "main" для доступа к базе и работы с ней в узле конкретном
    val dB = Firebase.database.getReference(MAIN_NODE)
    //инициализирую  storage и узел "main" для доступа к хранилищу и работы с ним
    val dBStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth // переменная класса Firebase с данными при аутификации

    /**
     * Функция для записи в базу данных
     * Передаю в конструктор интерфейс который сработает как только закончится загрузка в базу
     */
    fun publishAd(ad: Ad, finishListener: FinishWorkListener) {
        //с помощью child() создаю еще один узел по уникальному идентификатору, дальше добавляю переданные данные
        // могу указать что точно не null через !! или через элвиса, если будет null, пишу текст
        //добавляю еще один узел по id юзера, что бы в дальнейшем только он мог править объявления и ad узел, для удобства
        //addOnCompleteListener слушатель запустится когда данные будут загружены в базу
        if (auth.uid != null) dB.child(ad.key ?: "Empty").child(auth.uid!!).child(AD_NODE)
            .setValue(ad).addOnCompleteListener {
            //беру из объявления время и категорию
            val adFilter = AdFilter(ad.time, "${ad.category}_${ad.time}")

            //как только опубликовали объявление, публикуем фильтр
            if (auth.uid != null) dB.child(ad.key ?: "Empty").child(FILTER_NODE).setValue(adFilter)
                .addOnCompleteListener {

                    finishListener.onFinish() //запуск интерфейса для закрытия окна как только выполнится загрузка в базу
                }

        }
    }

    /**
     * Функция для записи в базу данных новый путь и класс для просмотров объявдления
     * И хаписываю 1 просмотр к объявлению
     * передаю в конструктор ad на который нажали
     */
    fun adViewed(ad: Ad){
        // переменная с количеством просмотров, делаю ее инт что бы мог увеличивать
        var couner = ad.viewsCounter.toInt()
        couner ++ //увеличил на 1
        //если прошел авторизацию  создаю путь для подсчета просмотров и избранных
        //создаю новый путь с обновленным числом просмотров  и старые данные просмотра тел и почты
        if(auth.uid != null) dB.child(ad.key ?: "Empty").child(INFO_NODE)
            .setValue(InfoItem(couner.toString(), ad.emailsCounter, ad.callsCounter))
    }

    /**
     * Функция для удаления данных с базы
     * Listener для того, что бы обновить адаптер только после того как действительно удалится из базы
     */
    fun deleteAd(ad: Ad, listener: FinishWorkListener){
        if (ad.uid == null || ad.key == null) return //если нет ключа или id, ничего не делаю
            //добираюсь до пути в базе который нужно удалить
        //addOnCompleteListener для соверщения действия как только закончит удаление с базы
            dB.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
                listener.onFinish() //запускаю интерфейс
            }


    }

    /**
     * Функция для фильтрации избранных объявлений. считывает с базы данных только мои объявления
     */
    fun getMyFavs(readDataCallBack: ReadDataCallBack?){
        //создаю query, указываю что именно фильтрую
        val query = dB.orderByChild( "/favs/${auth.uid}").equalTo(auth.uid) //id должен быть равен моему id
        readDataFromDb(query, readDataCallBack) //запускаю функцию считывания данных с базы
    }

    /**
     * Функция для фильтрации моих объявлений. считывает с базы данных только мои объявления
     */
    fun getMyAds(readDataCallBack: ReadDataCallBack?){
        //создаю query, указываю что именно фильтрую
        //объявления где есть мой идентификатор
        val query = dB.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack) //запускаю функцию считывания данных с базы
    }

    /**
     * Функция для считывания и фильтрации с базы данных по времени
     * Первый раз когда загружаю страницу в разных
     */
    fun getAllAdsFirstPage(readDataCallBack: ReadDataCallBack?){
        //создаю query, указываю что именно фильтрую
        //limitToLast(ADS_LIMIT) фильтрует по количеству показанных, от последнего не включая его
        val query = dB.orderByChild( "/adFilter/time").limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack) //запускаю функцию считывания данных с базы
    }

    /**
     * Функция для скрола в категории разное
     */
    fun getAllAdsNextPage(time: String, readDataCallBack: ReadDataCallBack?){
        //создаю query, указываю что именно фильтрую
        //limitToLast(ADS_LIMIT) фильтрует по количеству показанных, от последнего не включая его
        val query = dB.orderByChild( "/adFilter/time").endBefore(time).limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack) //запускаю функцию считывания данных с базы
    }

    /**
     * Функция для считывания и фильтрации с базы данных по категории
     */
    fun getAllAdsFromCatFirstPage(cat: String, readDataCallBack: ReadDataCallBack?){
        //создаю query, указываю что именно фильтрую
        //limitToFirst(ADS_LIMIT) фильтрует по количеству показанных, от последнего не включая его
        val query = dB.orderByChild( "/adFilter/catTime")
            .startAfter(cat).endAt(cat + "_\uf8ff").limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack) //запускаю функцию считывания данных с базы
    }

    /**
     * Функция для считывания и фильтрации с базы данных по категории, берет следующую страницу
     */
    fun getAllAdsFromCatNextPage(catTime: String, readDataCallBack: ReadDataCallBack?){
        //создаю query, указываю что именно фильтрую
        //limitToFirst(ADS_LIMIT) фильтрует по количеству показанных, от последнего не включая его
        val query = dB.orderByChild( "/adFilter/catTime")
            .endBefore(catTime).limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack) //запускаю функцию считывания данных с базы
    }

    /**
     * Функция для считывания данных с базы, в конструкторе нужно указать фильтр какой будет и интерфейс
     */
    private fun readDataFromDb(query: Query, readDataCallBack: ReadDataCallBack?){
        //добавляю слушатель изменений для считывания данных с пути. один раз считает как только запустим функцию
        //считывает через dB, просто с пути, через  query с фильтром
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            //в этой функции выдает считанные данные
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>() //массив который будет заполняться считанными данными
                //через цикл добираюсь до объявления, узла объявления
                for (item in snapshot.children){
                    //создаю для проверки, если все еще null, значит нужно пытаться взять с другого узла, пока не найдет
                    var ad: Ad? = null
                    //запускаю проверку,запускаю проверку для всего содержимого, а именно для id и info
                    // it - это содержимое внутри узла с ключом. а именно id узел и info. проверяю циклом если там ad узел
                    item.children.forEach{
                            //если ad == null значит объявление еще не найдено и попытается взять с узла объявление
                        //Ad::class.java указываю для указания в какой формат преобразовать, так как по умолчанию идет json
                            if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    //достаю info на прямую указывая название узла, так как знаю его в отличии от рандомного ключа
                    //InfoItem::class.java указываю для указания в какой формат преобразовать, так как по умолчанию идет json
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    //счетчик избранных
                    val favCounter = item.child(FAVS_NODE).childrenCount
                    //прлверяю есть ли это объявление в избранных, есть ли там мой id
                    val isFav = auth.uid?.let {item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                    //добавляю считанные данные в класс ad. что бы в дальнейшем через один класс работать с адаптером
                    ad?.isFav = isFav != null //выдаст или tru или false, если tru, значит есть в избранных, так как нашли uid
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    //проверка, если считанные данные не пустые, заполняю массив
                    if (ad != null) adArray.add(ad!!) //заполняю массив считанными данными
                }
                readDataCallBack?.readData(adArray) //передаю в интерфейс список с данными
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    /**
     * Функция проверяет объявление удалять из избранных или добавить
     */
    fun onFavClick(ad: Ad, listener: FinishWorkListener){
        //если по нажатию на избранное, там уже было true(нажатие ранее), то удаляет, запускаю функцию удаления
        if (ad.isFav) {
            removeFromFavs(ad, listener)
        }else {
            addToFavs(ad, listener) //если первый раз нажали, добавляю в избранное
         }
    }

    /**
     * Функция добавляет объявление в избранное, создает новый путь.
     * Добавляю в конструктор ad для взятия избранных и интерфейс для совершения действия когда закончило грузить в базу
     */
    private fun addToFavs(ad: Ad, listener: FinishWorkListener){
        //запускаю таким образом путь, в случае если может быть null, не запустится, если не null, дойдет до пути ключ
        ad.key?.let {
            //создаю узел favs для избранных и помещаю туда свой id, что бы потом можно было смотреть есть мой id  или нет
            //и узел uid и добавляю значение, в данном случае тоже мой uid
            auth.uid?.let {
                uid -> dB.child(it).child(FAVS_NODE).child(uid).setValue(uid).addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish() //запуск интерфейса как только загрузилось в базу
                 }
            }
        }
    }

    /**
     * Функция удаляет объявление из избранных
    */
   private fun removeFromFavs(ad: Ad, listener: FinishWorkListener){
        //запускаю таким образом путь, в случае если может быть null, не запустится, если не null, дойдет до пути ключ
        ad.key?.let {
            //добираюсь до узла id в узле fav и очищаю его. тем самым объявление больше не будет в избранных
            auth.uid?.let {
                    uid -> dB.child(it).child(FAVS_NODE).child(uid).removeValue().addOnCompleteListener {
                if (it.isSuccessful) listener.onFinish() //запуск интерфейса как только загрузилось в базу
            }
            }
        }
    }

    /**
     * Интерфейс для передачи на mainActivity данных считанных с базы
     */
    interface ReadDataCallBack {
        fun readData(list: ArrayList<Ad>)
    }

    /**
     * Интерфейс будет запускаться когда данные загрузились в базу,
     * в окне редактирования или создания нового объявления
     */
    interface FinishWorkListener{
        fun onFinish()
    }

    /**
     * Костанлды для путей в базе дынных
     */
    companion object{
        const val AD_NODE = "ad"
        const val FILTER_NODE = "adFilter"
        const val MAIN_NODE = "main"
        const val INFO_NODE = "info"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 2
    }
}