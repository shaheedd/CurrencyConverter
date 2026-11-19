package com.example.currencyconverter

import retrofit2.http.GET

/** Réponse de l'API open.er-api.com : les taux sont exprimés pour 1 EUR. */
data class ReponseTaux(
    val result: String?,
    val rates: Map<String, Double>?,
    val time_last_update_utc: String?
)

/** Service Retrofit : aucun clé d'API n'est nécessaire. */
interface RatesApi {
    @GET("v6/latest/EUR")
    suspend fun chargerTaux(): ReponseTaux
}
