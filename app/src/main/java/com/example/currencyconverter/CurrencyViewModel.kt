package com.example.currencyconverter

import androidx.lifecycle.ViewModel

/**
 * Conserve l'état du convertisseur : la saisie, les deux devises, le résultat
 * et la source des taux. Le ViewModel survit à la recréation de l'activité
 * (rotation de l'écran ou reprise après suppression du processus).
 */
class CurrencyViewModel : ViewModel() {

    var montantSaisi: String = "0"
    var resultatAffiche: String? = null

    /** Positions des devises dans le string-array : 0 = USD, 1 = EUR. */
    var positionSource: Int = 0
    var positionCible: Int = 1

    /** Remplacé par les taux de l'API si le chargement réussit. */
    var tauxActuels: Map<String, Double> = emptyMap()

    /** Vrai quand les taux réels sont utilisés. */
    var utiliseTauxApi: Boolean = false
}
