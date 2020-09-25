package com.huawei.codelabs.iap.example.huawei

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.codelabs.iap.example.huawei.Key.REQ_CODE_BUY
import com.huawei.codelabs.iap.example.huawei.Key.REQ_CODE_LOGIN
import com.huawei.codelabs.iap.example.huawei.Key.consumables
import com.huawei.codelabs.iap.example.huawei.Key.nonConsumables
import com.huawei.codelabs.iap.example.huawei.Key.subscriptions
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


const val TAG = "v4-IAP-Demo"

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private var products = arrayListOf<ProductModel>()
    private var isSandbox = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO (1) Проверить доступность сервиса встроенных покупок в текущем регионе
        isEnvironmentReady()
        // TODO () Проверить доступность тестового окружения при необходимости
        checkSandboxing()
        // TODO (3) инициализация компонент активности
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerViewAdapter(products, ::gotoPay)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context,
                DividerItemDecoration.VERTICAL))
    }

    /**
     * Общая проверка окружения на доступность сервиса покупок в приложении
     * и инициализация сервиса
     */
    private fun isEnvironmentReady() {
        // TODO(2) проверяем доступность окружения
        Iap.getIapClient(this)
                .isEnvReady
                .addOnSuccessListener {
                    // TODO(3) Сервис доступен, загружаем доступные продукты
                    initControls()
                }.addOnFailureListener { e ->
                    if (e is IapApiException) {
                        when (e.status.statusCode) {
                            OrderStatusCode.ORDER_HWID_NOT_LOGIN ->
                                // Пользователь не залогинен
                                if (e.status.hasResolution()) {
                                    try {
                                        // Отправляем запрос на логин
                                        e.status.startResolutionForResult(this@MainActivity, REQ_CODE_LOGIN)
                                    } catch (exp: SendIntentException) {
                                    }
                                }
                            OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED ->
                                // Текущее расположение не поддерживается
                                Log.e(TAG, "Current region is not supported by IAP")
                            else ->
                                Log.e(TAG, "Unknown error")
                        }
                    }
                    Log.e(TAG, "Unknown error")
                }
    }

    /**
    Обновляем список товаров и подписок
     */
    private fun initControls() {
        // TODO(4) Формируем список продуктов для получения со стороны AGC. Каждый продукт имеет имя и
        //  тип, по которыму мы и осуществляем запрос.
        val deferredProducts = listOf(
                Pair(IapClient.PriceType.IN_APP_CONSUMABLE, consumables),
                Pair(IapClient.PriceType.IN_APP_SUBSCRIPTION, subscriptions),
                Pair(IapClient.PriceType.IN_APP_NONCONSUMABLE, nonConsumables)
        ).map { pair ->
            CoroutineScope(Dispatchers.IO).async {
                Log.d(TAG, "${pair.first} load started")
                loadProducts(
                        pair.first,
                        pair.second).also { Log.d(TAG, "${pair.first} load finished") }
            }
        }
        // TODO(5) Для получения уже приобретенных товаров формируем запрос по каждому типу соответственно
        val deferredPurchases = listOf(
                IapClient.PriceType.IN_APP_CONSUMABLE,
                IapClient.PriceType.IN_APP_SUBSCRIPTION,
                IapClient.PriceType.IN_APP_NONCONSUMABLE
        ).map { type ->
            CoroutineScope(Dispatchers.IO).async {
                Log.d(TAG, "Product type $type purchase load started")
                queryOwnedPurchases(type).also { Log.d(TAG, "Purchases for type $type load finished") }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            // TODO(6) Отправляем запросы и получаем результаты
            val results = deferredProducts.map { it.await() }.flatten()
            val purchased = deferredPurchases.map { it.await() }
            // TODO(7) показываем результаты в зависимости от наших потребностей в приложении
            Log.d(TAG, "show products")
            //Toast.makeText(this,IapClient.PriceType.IN_APP_CONSUMABLE,Toast.LENGTH_SHORT)
            showProduct(results)
        }
    }

    /**
     * Загружаем информацию о продуктах
     * @param type Тип загружаемых продуктов
     * @param products Список идентификаторов продуктов
     */
    private suspend fun loadProducts(type: Int, products: List<String>): List<ProductInfo> =
        suspendCoroutine { continuation ->
            // TODO используем IAP клиента для формирования запросов
            Iap.getIapClient(this)
                .obtainProductInfo(
                    ProductInfoReq().apply {
                        // запрос продуктов происходит в соответствии с типом продукта
                        priceType = type
                        // Здесь требуется добавить сконфигурированные на странице продуктов идентификаторы
                        productIds = products
                    }
                )
                .also {
                    it.addOnSuccessListener { result ->
                        // при успешном ответе службы обрабатываем результат
                        continuation.resume(result?.productInfoList.orEmpty())
                        Toast.makeText(this, "Success load", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        // а при неуспешном - ошибку
                        Log.e(TAG, "Load products error: $e!")
                        Toast.makeText(this, "Load products error", Toast.LENGTH_SHORT).show()
                        continuation.resume(emptyList())
                    }
                }
        }

    /**
     * Загружаем продукты в список на экране приложения
     * @param list Список продуктов для отображения
     */
    private fun showProduct(list: List<ProductInfo>) {
        products.clear()
        products.addAll(list.map {
            ProductModel(it.productName, it.productDesc, it.price, it.productId, it.priceType, false)
        } as ArrayList<ProductModel>)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * Отправка запроса на покупку в приложении
     * @param productId Идентификатор продукта, сконфигурированный на странице продуктов приложения
     * @param type  Тип продукта(consumable, non-consumable, subscription)
     */
    private fun gotoPay(productId: String, type: Int) {
        Log.i(TAG, "call createPurchaseIntent")
        // TODO(8) Формируем запрос на покупку
        Iap.getIapClient(this)
                .createPurchaseIntent(
                        PurchaseIntentReq().apply {
                            this.productId = productId
                            priceType = type
                            developerPayload = "$productId payload"
                        }
                )
                .addOnSuccessListener { result ->
                    Log.i(TAG, "createPurchaseIntent, onSuccess")
                    if (result?.status?.hasResolution() == true) {
                        try {
                            result.status.startResolutionForResult(this, REQ_CODE_BUY)
                        } catch (exp: SendIntentException) {
                            Log.e(TAG, exp.message ?: "error")
                        }
                    } else {
                        Log.e(TAG, "intent is null")
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, e.message ?: "")
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    if (e is IapApiException) {
                        val returnCode = e.statusCode
                        Log.e(TAG, "createPurchaseIntent, returnCode: $returnCode")
                        // handle error scenarios
                    }
                }
    }

    /**
     * Проверка результатов запросов на покупку
     */
    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // TODO(9) Получаем результат покупки
        // код возврата совпадает с кодом запроса
        if (requestCode == REQ_CODE_BUY) {
            if (data == null) {
                Toast.makeText(this,
                        "Buy product error",
                        Toast.LENGTH_SHORT).show()
                return
            }
            // получаем результаты операции с помощью IAP службы
            val purchaseResultInfo =
                    Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data)
            when (purchaseResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_SUCCESS -> {
                    // TODO(10) проверка подписи результата ( локальная , либо с помощью application-сервера)
                    CipherUtil.doCheck(
                            purchaseResultInfo.inAppPurchaseData,
                            purchaseResultInfo.inAppDataSignature,
                            Key.publicKey
                    ).also {
                        Toast.makeText(this,
                                "Pay successful${if (!it) ", sign failed" else ""}",
                                Toast.LENGTH_SHORT).show()
                        // TODO(11) Для завершения процесса покупки необходимо "употребить" приобретённый продукт
                        if (it) consumePurchase(purchaseResultInfo.inAppPurchaseData)
                    }
                }
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    // Пользователь отменил покупку
                    Toast.makeText(this, "user cancel", Toast.LENGTH_SHORT).show()
                }
                OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                    // Пользователь уже приобрёл продукт
                    Toast.makeText(this, "you have owned the product", Toast.LENGTH_SHORT).show()
                    // тут можно опрелить действия по доставке функционала продукта пользователю
                }
                else -> Toast.makeText(this, "Pay failed", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQ_CODE_LOGIN) {
            if (data != null) {
                //
                val returnCode: Int = data.getIntExtra("returnCode", 1)
                if (returnCode == 0) initControls()
            }
        }
    }

    /**
     * "Потребление" выполненных покупок
     * @param purchaseData JSON-строка, содержащая данные о покупке
     */
    private fun consumePurchase(purchaseData: String) {
        with(InAppPurchaseData(purchaseData)) {
            // TODO(12) подписки не требуют потребления
            if (subscriptions.contains(productId)) return
            Iap.getIapClient(this@MainActivity)
                    .consumeOwnedPurchase(
                            ConsumeOwnedPurchaseReq().apply {
                                purchaseToken = purchaseToken
                                developerChallenge = "consume product"
                            }
                    ).addOnSuccessListener {
                        Log.i(TAG, "Successfully delivered")
                    }.addOnFailureListener {
                        Log.e(TAG, "Deliver failure")
                    }
        }
    }



    /**
     * проверка доступности изолированного окружения для совершения тестовых платежей
     * подробнее здесь: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/iap-sandbox-testing-v4
     */
    private fun checkSandboxing() {
        Iap.getIapClient(this)
                .isSandboxActivated(IsSandboxActivatedReq())
                .addOnSuccessListener { result ->
                    isSandbox = result.isSandboxApk && result.isSandboxUser
                    Log.i(TAG, "isSandboxActivated: $isSandbox")
                }.addOnFailureListener { e ->
                    Log.e(TAG, "isSandboxActivated call fail: ${e.message}")
                    isSandbox = false
                }
    }

    /**
     * запрашиваем предыдущие покупки
     * @param reqPriceType
     */
    private suspend fun queryOwnedPurchases(reqPriceType: Int) {
        suspendCoroutine<List<String>> { continuation ->
            // получаем имеющиеся покупки
            Iap.getIapClient(this)
                    .obtainOwnedPurchases(
                            OwnedPurchasesReq().apply {
                                continuationToken = null
                                priceType = reqPriceType
                            }
                    ).addOnSuccessListener { result ->
                        if (result.inAppPurchaseDataList != null) {
                            val inAppPurchaseDataList = result.inAppPurchaseDataList
                            val inAppSignature = result.inAppSignature
                            // необходимо проверить подпись и тогда можно использовать полученные покупки
                            continuation.resume(
                                    inAppPurchaseDataList.map {
                                        consumePurchase(it)
                                        InAppPurchaseData(it).productId
                                    }
                            )
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "obtainOwnedPurchases, type=$reqPriceType, ${e.message}")
                        continuation.resume(emptyList())
                    }
        }
    }

}