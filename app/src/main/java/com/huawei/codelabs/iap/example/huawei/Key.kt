/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.huawei.codelabs.iap.example.huawei

import com.huawei.agconnect.core.BuildConfig

object Key {
    // !!! в реальном приложении ключ не хранить в открытом виде !!!
    const val publicKey = "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAmWI6tq9OsUI2Tyea45T9CKgQtVOPZJ1CM2N/YDa928JKHj0HinMkFgB+iZszA5xvIdH1O+jGDoyW2ecf5KRssr4ekWeDbf3S3b/05pz5C1a6sOeUv5/8kTZsPVKQPqQlFME3nMfOCRWrom3MnLKpLFXs1YB+QfiGhaPqPpXljkYFMrSvucfasEMa+2fnrQqMmTqBAyGEhBlimN6O2V8eGXa8+VGK9zjNzPwnViSmhz+QQLymyAEo6GznGSxppfkBkVvjsm7kilyd9YVvDwMXyqdMEMBjNQQS4Mgdlh1qnfnpqQnEbWk17jqtMxkSrOq/lDO38T8jnJywBUcmnJZHgzCxIpOM2620p6ks+4GcA/PuFsdN5dJ54M/4AGkyNIBBS0cD9z7Tix6TIH/gv27136Pv0H5BZpLRKRy93q4BRxpsDplknE3kV0klcaQy4nZnyEv9DKessDh7bHEOYexJdTUD4c/O89A7EjmW5FFo68y+OdxKIRAfQK5DGzpz+9pzAgMBAAE=" // здесь нужен публичный ключ , полученный после включения IAP
    //(Мои приложения->[Имя приложения]->Разработка->In-App Purchases)

    // Коды запросов
    const val REQ_CODE_BUY = 1003
    const val REQ_CODE_LOGIN = 1004

    // Коды продуктов, сконфигурированных в консоли разработчика

    // подписки
    const val MONTHLY_PRO = "monthly_pro_id"
    const val SEASON_PRO = "seasonal_pro_id"
    const val ANNUAL_PRO = "annual_pro_id"
    val subscriptions = listOf(MONTHLY_PRO, SEASON_PRO, ANNUAL_PRO)

    // consumable
    const val PRODUCT1 = "product1"
    val consumables = listOf(PRODUCT1)

    // non-consumable
    const val BEGINNER_PACK = "beginner_pack_id"
    const val SKILLED_PACK = "skilled_pack_id"
    val nonConsumables = listOf(BEGINNER_PACK, SKILLED_PACK)
}