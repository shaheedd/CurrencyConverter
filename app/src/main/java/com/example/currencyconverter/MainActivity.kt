package com.example.currencyconverter

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.currencyconverter.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

/**
 * Écran unique du convertisseur : deux Spinner, un clavier numérique
 * personnalisé, le bouton de conversion et le bouton d'inversion.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CurrencyViewModel

    /** Évite de relancer une conversion pendant la configuration des Spinner. */
    private var spinnersPret = false

    /**
     * Taux fixes fournis par l'énoncé, exprimés pour 1 EUR.
     * Ils servent de référence et de repli hors ligne.
     */
    private val taux = mapOf(
        "EUR" to 1.0,
        "USD" to 1.17,
        "TND" to 3.40,
        "GBP" to 0.87,
        "JPY" to 172.50,
        "CAD" to 1.62
    )

    /** Client Retrofit pour charger les taux réels (bonus). */
    private val api: RatesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RatesApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // L'état est conservé dans le ViewModel (bonus : rotation de l'écran).
        viewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]
        if (viewModel.tauxActuels.isEmpty()) {
            viewModel.tauxActuels = taux
        }

        // Soulignement du montant et du résultat, comme sur la maquette.
        binding.tvMontant.paintFlags =
            binding.tvMontant.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvResultat.paintFlags =
            binding.tvResultat.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        initialiserSpinners()
        initialiserClavier()

        binding.btnConvertir.setOnClickListener { effectuerConversion(avecToast = true) }
        binding.btnInverser.setOnClickListener { inverserDevises() }
        binding.tvEtatTaux.setOnClickListener { basculerSourceTaux() }

        restaurerAffichage()
    }

    /** Remplit les deux Spinner depuis le string-array de strings.xml. */
    private fun initialiserSpinners() {
        val devises = resources.getStringArray(R.array.devises)
        val adaptateur = ArrayAdapter(this, android.R.layout.simple_spinner_item, devises)
        adaptateur.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spDeviseSource.adapter = adaptateur
        binding.spDeviseCible.adapter = adaptateur

        // Par défaut (ou après rotation) : restauration des positions.
        binding.spDeviseSource.setSelection(viewModel.positionSource)
        binding.spDeviseCible.setSelection(viewModel.positionCible)

        val ecouteur = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                vue: View?,
                position: Int,
                id: Long
            ) {
                if (!spinnersPret) return
                val source = binding.spDeviseSource.selectedItemPosition
                val cible = binding.spDeviseCible.selectedItemPosition
                if (source == viewModel.positionSource && cible == viewModel.positionCible) return

                viewModel.positionSource = source
                viewModel.positionCible = cible
                // Bonus : reconversion automatique dès qu'une devise change.
                conversionAuto()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.spDeviseSource.onItemSelectedListener = ecouteur
        binding.spDeviseCible.onItemSelectedListener = ecouteur
        spinnersPret = true
    }

    /** Associe chaque touche du TableLayout à son action. */
    private fun initialiserClavier() {
        val touches = listOf(
            binding.btnKey7 to "7", binding.btnKey8 to "8", binding.btnKey9 to "9",
            binding.btnKey4 to "4", binding.btnKey5 to "5", binding.btnKey6 to "6",
            binding.btnKey1 to "1", binding.btnKey2 to "2", binding.btnKey3 to "3",
            binding.btnKeyC to "C", binding.btnKey0 to "0", binding.btnKeyPoint to "."
        )
        touches.forEach { (bouton, caractere) ->
            bouton.setOnClickListener {
                if (caractere == "C") effacerSaisie() else ajouterCaractere(caractere)
            }
        }
    }

    /**
     * Ajoute un caractère à la saisie.
     * Règles : un seul point décimal, 10 caractères maximum,
     * le premier chiffre remplace le « 0 » initial.
     */
    private fun ajouterCaractere(c: String) {
        val saisie = viewModel.montantSaisi
        if (saisie.length >= 10) return
        if (c == "." && saisie.contains(".")) return

        viewModel.montantSaisi = if (saisie == "0") {
            if (c == ".") "0." else c
        } else {
            saisie + c
        }

        binding.tvMontant.text = viewModel.montantSaisi
        // Bonus : conversion automatique à chaque chiffre saisi.
        conversionAuto()
    }

    /** La touche C remet le montant à zéro et efface le résultat. */
    private fun effacerSaisie() {
        viewModel.montantSaisi = "0"
        viewModel.resultatAffiche = null
        binding.tvMontant.text = viewModel.montantSaisi
        binding.tvResultat.text = ""
    }

    /** Conversion avec l'euro comme devise pivot. */
    private fun convertir(montant: Double, source: String, cible: String): Double {
        val montantEnEuros = montant / (viewModel.tauxActuels[source] ?: 1.0)
        return montantEnEuros * (viewModel.tauxActuels[cible] ?: 1.0)
    }

    /**
     * Calcule le résultat silencieusement (utilisé par le clavier, les Spinner
     * et le changement de source des taux).
     */
    private fun conversionAuto() {
        val valeur = viewModel.montantSaisi.toDoubleOrNull()
        if (valeur == null || valeur == 0.0) {
            viewModel.resultatAffiche = null
        } else {
            val source = binding.spDeviseSource.selectedItem as String
            val cible = binding.spDeviseCible.selectedItem as String
            val resultat = convertir(valeur, source, cible)
            viewModel.resultatAffiche = String.format(
                Locale.US,
                getString(R.string.format_resultat),
                resultat
            )
        }
        binding.tvResultat.text = viewModel.resultatAffiche.orEmpty()
    }

    /** Bouton CONVERTIR : même calcul, avec Toast si le montant est vide. */
    private fun effectuerConversion(avecToast: Boolean) {
        val valeur = viewModel.montantSaisi.toDoubleOrNull()
        if (valeur == null || valeur == 0.0) {
            if (avecToast) {
                Toast.makeText(this, R.string.montant_vide, Toast.LENGTH_SHORT).show()
            }
            return
        }
        conversionAuto()
    }

    /** Échange les deux devises et recalcule si un résultat était affiché. */
    private fun inverserDevises() {
        val ancienneSource = viewModel.positionSource
        val ancienneCible = viewModel.positionCible

        spinnersPret = false
        binding.spDeviseSource.setSelection(ancienneCible)
        binding.spDeviseCible.setSelection(ancienneSource)
        viewModel.positionSource = ancienneCible
        viewModel.positionCible = ancienneSource
        spinnersPret = true

        conversionAuto()
    }

    /** Alterne entre taux fixes du TP et taux réels fournis par l'API. */
    private fun basculerSourceTaux() {
        if (viewModel.utiliseTauxApi) {
            viewModel.tauxActuels = taux
            viewModel.utiliseTauxApi = false
            rafraichirLibelleTaux()
            conversionAuto()
        } else {
            chargerTauxDistants()
        }
    }

    /** Charge les taux réels avec Retrofit et coroutines, avec repli hors ligne. */
    private fun chargerTauxDistants() {
        binding.tvEtatTaux.isEnabled = false
        binding.tvEtatTaux.text = getString(R.string.chargement_taux)

        lifecycleScope.launch {
            try {
                val reponse = withContext(Dispatchers.IO) { api.chargerTaux() }
                val tauxDistants = reponse.rates

                if (reponse.result == "success" &&
                    tauxDistants != null &&
                    taux.keys.all { tauxDistants.containsKey(it) }
                ) {
                    viewModel.tauxActuels = tauxDistants.filterKeys { it in taux }
                    viewModel.utiliseTauxApi = true
                    rafraichirLibelleTaux()
                    conversionAuto()
                } else {
                    repliHorsLigne()
                }
            } catch (exception: Exception) {
                // Pas de réseau ou API indisponible : on garde les taux fixes.
                repliHorsLigne()
            } finally {
                binding.tvEtatTaux.isEnabled = true
            }
        }
    }

    private fun repliHorsLigne() {
        viewModel.tauxActuels = taux
        viewModel.utiliseTauxApi = false
        binding.tvEtatTaux.text = getString(R.string.taux_api_indisponible)
        conversionAuto()
    }

    private fun rafraichirLibelleTaux() {
        binding.tvEtatTaux.text = getString(
            if (viewModel.utiliseTauxApi) R.string.taux_api else R.string.taux_fixes
        )
    }

    /** Réapplique l'état du ViewModel aux vues (premier lancement ou rotation). */
    private fun restaurerAffichage() {
        binding.tvMontant.text = viewModel.montantSaisi
        binding.tvResultat.text = viewModel.resultatAffiche.orEmpty()
        rafraichirLibelleTaux()
    }
}
