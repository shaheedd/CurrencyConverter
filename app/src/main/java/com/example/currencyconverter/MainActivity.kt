package com.example.currencyconverter

import android.graphics.Paint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.currencyconverter.databinding.ActivityMainBinding
import java.util.Locale

/**
 * Écran unique du convertisseur : deux Spinner, un clavier numérique
 * personnalisé et le bouton de conversion.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** Taux fixes fournis par l'énoncé : exprimés pour 1 EUR. */
    private val taux = mapOf(
        "EUR" to 1.0,
        "USD" to 1.17,
        "TND" to 3.40,
        "GBP" to 0.87,
        "JPY" to 172.50,
        "CAD" to 1.62
    )

    /** Saisie construite uniquement avec le clavier de l'application. */
    private var montantSaisi = "0"

    /** Résultat affiché, ou null si aucune conversion n'a encore été faite. */
    private var resultatAffiche: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Soulignement du montant et du résultat, comme sur la maquette.
        binding.tvMontant.paintFlags =
            binding.tvMontant.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvResultat.paintFlags =
            binding.tvResultat.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        initialiserSpinners()
        initialiserClavier()

        binding.btnConvertir.setOnClickListener { effectuerConversion(avecToast = true) }
        binding.btnInverser.setOnClickListener { inverserDevises() }

        mettreAJourAffichage()
    }

    /** Remplit les deux Spinner depuis le string-array de strings.xml. */
    private fun initialiserSpinners() {
        val devises = resources.getStringArray(R.array.devises)
        val adaptateur = ArrayAdapter(this, android.R.layout.simple_spinner_item, devises)
        adaptateur.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spDeviseSource.adapter = adaptateur
        binding.spDeviseCible.adapter = adaptateur

        // Par défaut : USD vers EUR.
        binding.spDeviseSource.setSelection(devises.indexOf("USD"))
        binding.spDeviseCible.setSelection(devises.indexOf("EUR"))
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
        if (montantSaisi.length >= 10) return
        if (c == "." && montantSaisi.contains(".")) return

        montantSaisi = if (montantSaisi == "0") {
            if (c == ".") "0." else c
        } else {
            montantSaisi + c
        }
        mettreAJourAffichage()
    }

    /** La touche C remet le montant à zéro et efface le résultat. */
    private fun effacerSaisie() {
        montantSaisi = "0"
        resultatAffiche = null
        mettreAJourAffichage()
    }

    /** Conversion avec l'euro comme devise pivot. */
    private fun convertir(montant: Double, source: String, cible: String): Double {
        val montantEnEuros = montant / (taux[source] ?: 1.0)
        return montantEnEuros * (taux[cible] ?: 1.0)
    }

    /** Lance la conversion et affiche le résultat arrondi à 2 décimales. */
    private fun effectuerConversion(avecToast: Boolean) {
        val valeur = montantSaisi.toDoubleOrNull()
        if (valeur == null || valeur == 0.0) {
            if (avecToast) {
                Toast.makeText(this, R.string.montant_vide, Toast.LENGTH_SHORT).show()
            }
            return
        }

        val source = binding.spDeviseSource.selectedItem as String
        val cible = binding.spDeviseCible.selectedItem as String
        val conversion = convertir(valeur, source, cible)

        resultatAffiche = String.format(
            Locale.US,
            getString(R.string.format_resultat),
            conversion
        )
        mettreAJourAffichage()
    }

    /** Échange les deux devises et recalcule si un résultat était affiché. */
    private fun inverserDevises() {
        val positionSource = binding.spDeviseSource.selectedItemPosition
        val positionCible = binding.spDeviseCible.selectedItemPosition

        binding.spDeviseSource.setSelection(positionCible)
        binding.spDeviseCible.setSelection(positionSource)

        if (resultatAffiche != null) {
            effectuerConversion(avecToast = false)
        }
    }

    /** Rafraîchit les deux TextView d'affichage. */
    private fun mettreAJourAffichage() {
        binding.tvMontant.text = montantSaisi
        binding.tvResultat.text = resultatAffiche.orEmpty()
    }
}
