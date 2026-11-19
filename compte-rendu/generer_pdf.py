"""Génère le compte rendu PDF du TP4 Currency Converter."""

from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    Image,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

BASE = Path(__file__).resolve().parent
RACINE = BASE.parent
SORTIE = BASE / "compte-rendu.pdf"
CAPTURES = RACINE / "screenshots"

BLEU = colors.HexColor("#2BB8E8")
BLEU_FONCE = colors.HexColor("#1BA6C9")
GRIS = colors.HexColor("#546E7A")
CLAIR = colors.HexColor("#F5F7FA")

styles = getSampleStyleSheet()
titre = ParagraphStyle(
    "TitreTP", parent=styles["Title"], fontSize=22, leading=26,
    textColor=BLEU_FONCE, spaceAfter=6,
)
sous_titre = ParagraphStyle(
    "SousTitre", parent=styles["Normal"], fontSize=10, leading=14,
    textColor=GRIS, alignment=TA_CENTER, spaceAfter=16,
)
section = ParagraphStyle(
    "Section", parent=styles["Heading2"], fontSize=13, leading=16,
    textColor=BLEU_FONCE, spaceBefore=12, spaceAfter=6,
)
sous_section = ParagraphStyle(
    "SousSection", parent=styles["Heading3"], fontSize=11, leading=14,
    textColor=colors.HexColor("#1565C0"), spaceBefore=8, spaceAfter=4,
)
corps = ParagraphStyle(
    "Corps", parent=styles["BodyText"], fontSize=9.5, leading=13.5,
    alignment=TA_JUSTIFY, spaceAfter=5,
)
puce = ParagraphStyle(
    "Puce", parent=corps, leftIndent=12, bulletIndent=2, spaceAfter=3,
)
legende = ParagraphStyle(
    "Legende", parent=styles["Normal"], fontSize=7.5, leading=9.5,
    alignment=TA_CENTER, textColor=GRIS, spaceBefore=3,
)
cellule = ParagraphStyle(
    "Cellule", parent=styles["Normal"], fontSize=8.5, leading=11,
)
cellule_gras = ParagraphStyle(
    "CelluleGras", parent=cellule, fontName="Helvetica-Bold",
    textColor=colors.white,
)


def p(text):
    return Paragraph(text, corps)


def bullet(text):
    return Paragraph(text, puce, bulletText="-")


def table(entetes, lignes, largeurs):
    donnees = [[Paragraph(x, cellule_gras) for x in entetes]]
    donnees += [[Paragraph(str(x), cellule) for x in ligne] for ligne in lignes]
    t = Table(donnees, colWidths=largeurs, repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), BLEU_FONCE),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, CLAIR]),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#CFD8DC")),
        ("LEFTPADDING", (0, 0), (-1, -1), 6),
        ("RIGHTPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]))
    return t


def capture(nom, legende_text):
    image = Image(str(CAPTURES / nom), width=86, height=86 * 1280 / 720)
    return [image, Paragraph(legende_text, legende)]


def grille_captures(elements, largeurs):
    lignes = [[colonne[0] for colonne in elements],
              [colonne[1] for colonne in elements]]
    t = Table(lignes, colWidths=largeurs)
    t.setStyle(TableStyle([
        ("ALIGN", (0, 0), (-1, -1), "CENTER"),
        ("VALIGN", (0, 0), (-1, 0), "MIDDLE"),
        ("VALIGN", (0, 1), (-1, 1), "TOP"),
        ("TOPPADDING", (0, 0), (-1, -1), 2),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 2),
    ]))
    return t


def page_decor(canvas, doc):
    canvas.saveState()
    canvas.setFillColor(BLEU)
    canvas.rect(0, A4[1] - 8 * mm, A4[0], 8 * mm, fill=1, stroke=0)
    canvas.setFillColor(GRIS)
    canvas.setFont("Helvetica", 8)
    canvas.drawString(45, 25, "TP4 Android - Currency Converter")
    canvas.drawRightString(A4[0] - 45, 25, "Page %d" % doc.page)
    canvas.setStrokeColor(colors.HexColor("#CFD8DC"))
    canvas.line(45, 32, A4[0] - 45, 32)
    canvas.restoreState()


histoires = []

# ======================== PAGE 1 ========================
histoires += [
    Paragraph("TP4 Android - Currency Converter", titre),
    Paragraph("Compte rendu - Shahed Attari - 19 novembre 2026", sous_titre),
    Paragraph("1. Objectifs du TP", section),
    p("L'objectif est de réaliser une application Android en Kotlin qui convertit un montant "
      "d'une devise vers une autre à partir de taux de change. Le travail porte sur la "
      "construction d'une interface XML, la gestion d'un clavier numerique personnalise, "
      "l'utilisation du View Binding, le calcul du résultat et la gestion des erreurs de saisie."),
]
for element in [
    "Construire l'écran avec un ConstraintLayout et le clavier avec un TableLayout 4 x 3.",
    "Alimenter deux Spinner avec le tableau de chaînes R.array.devises.",
    "Récupérer les vues avec le View Binding et gerer les clics.",
    "Convertir avec l'euro comme devise pivot et afficher deux decimales.",
    "Afficher un Toast lorsqu'aucun montant n'est saisi.",
]:
    histoires.append(bullet(element))

histoires += [
    Paragraph("2. Choix généraux", section),
    p("Le projet utilise Android Studio, Kotlin, le système de build Gradle avec version catalog "
      "(gradle/libs.versions.toml) et le plugin Android. Le SDK minimal est API 24. L'écran est "
      "verrouillé en portrait dans le manifeste. Les taux fixes fournis par l'énoncé sont "
      "utilisés par défaut afin de garantir les valeurs attendues par les tests, même hors ligne."),
    Paragraph("3. Structure du projet", section),
    table(
        ["Fichier", "Rôle"],
        [
            ["MainActivity.kt", "Interface, clavier, conversion, inversion et chargement des taux"],
            ["CurrencyViewModel.kt", "Conservation de l'état après rotation"],
            ["RatesApi.kt", "Service Retrofit vers open.er-api.com"],
            ["activity_main.xml", "ConstraintLayout, Spinner, TableLayout 4 x 3"],
            ["strings.xml / colors.xml", "Toutes les chaînes et toutes les couleurs"],
            ["screenshots/", "Captures de demarrage et des quatre cas de test"],
        ],
        [150, 355],
    ),
    PageBreak(),
]

# ======================== PAGE 2 ========================
histoires += [
    Paragraph("4. Implémentation", section),
    Paragraph("4.1 Interface", sous_section),
    p("Le fichier activity_main.xml contient un ConstraintLayout racine. La zone supérieure "
      "occupe 48 % de la hauteur et utilise le fond #2BB8E8. Elle contient le titre, les deux "
      "Spinner séparés par le bouton d'inversion, les TextView tvMontant et tvResultat soulignes, "
      "et le bouton CONVERTIR. La zone inférieure contient le TableLayout clavier (quatre lignes "
      "de trois touches) et le libellé d'état des taux, cliquable."),
    Paragraph("4.2 View Binding et Spinner", sous_section),
    p("ActivityMainBinding.inflate() remplace findViewById. Les deux Spinner partagent un "
      "ArrayAdapter construit à partir de resources.getStringArray(R.array.devises). Les positions "
      "par défaut sont USD (source) et EUR (cible). Un OnItemSelectedListener déclenche la "
      "conversion automatique des qu'une devise change."),
    Paragraph("4.3 Clavier et règles de saisie", sous_section),
    p("Chaque bouton du TableLayout est associé a son caractere. Le premier chiffre remplace le "
      "zero initial, un seul point décimal est autorisé, la saisie est limitée a 10 caracteres "
      "et la touche C remet le montant à zéro tout en effaçant le résultat."),
    Paragraph("4.4 Conversion", sous_section),
    p("Les taux sont exprimes pour 1 EUR. La conversion passe donc toujours par l'euro : "
      "montantEnEuros = montant / taux[source], puis résultat = montantEnEuros * taux[cible]. "
      "Le résultat est formaté avec String.format(Locale.US, \"%.2f\", résultat) pour conserver "
      "le point décimal. Le bouton CONVERTIR affiché un Toast R.string.montant_vide si la valeur "
      "est vide ou nulle."),
    Paragraph("4.5 Inversion et ressources", sous_section),
    p("Le bouton d'inversion échange les positions des deux Spinner et relance le calcul. L'écran "
      "reste en portrait. Aucune chaine ni couleur n'est ecrite en dur dans le code Kotlin : "
      "tout passe par strings.xml et colors.xml."),
    Paragraph("5. Bonus réalisés", section),
    table(
        ["Bonus", "Implémentation"],
        [
            ["Conversion auto", "Recalcul a chaque touche et a chaque changement de Spinner"],
            ["Taux réels", "Retrofit + Gson + coroutines vers https://open.er-api.com/v6/latest/EUR"],
            ["Repli hors ligne", "Retour automatique aux taux fixes si l'API echoue"],
            ["ViewModel", "Montant, devises, résultat et source des taux conservés après rotation"],
        ],
        [120, 385],
    ),
    PageBreak(),
]

# ======================== PAGE 3 ========================
histoires += [
    Paragraph("6. Tests sur émulateur", section),
    p("Les cas demandés par l'énoncé ont ete vérifiés sur l'émulateur Android, ainsi que les "
      "règles de saisie, l'inversion et les bonus."),
    table(
        ["Cas de test", "Résultat observé", "Statut"],
        [
            ["Démarrage", "0 / vide / USD vers EUR", "OK"],
            ["120 USD vers EUR", "102.56", "OK"],
            ["Changement devise avec 120", "348.72 en TND puis 102.56 en EUR", "OK"],
            ["100 EUR vers TND", "340.00", "OK"],
            ["50 GBP vers USD", "67.24", "OK"],
            ["10 EUR vers EUR", "10.00", "OK"],
            ["Point décimal unique", "0.5 après deux appuis sur le point", "OK"],
            ["Limite de saisie", "1234567890 (10 caracteres)", "OK"],
            ["Touche C et Toast", "Remise à zéro, résultat effacé, Toast affiché", "OK"],
            ["Inversion", "100 USD vers EUR = 85.47 puis 117.00 après inversion", "OK"],
            ["API et repli", "Taux réels chargés puis retour aux taux fixes", "OK"],
        ],
        [175, 255, 75],
    ),
    PageBreak(),

    Paragraph("7. Captures d'écran", section),
    grille_captures([
        capture("00-demarrage.png", "Démarrage"),
        capture("01-120-usd-eur.png", "120 USD vers EUR"),
        capture("02-100-eur-tnd.png", "100 EUR vers TND"),
    ], [168, 168, 168]),
    Spacer(1, 6),
    grille_captures([
        capture("03-50-gbp-usd.png", "50 GBP vers USD"),
        capture("04-10-eur-eur.png", "10 EUR vers EUR"),
    ], [168, 168]),
    Paragraph("8. Conclusion", section),
    p("L'application répond à tous les points du TP et aux trois bonus demandés. Les tests "
      "effectués sur l'émulateur confirment les valeurs de conversion, la robustesse de la saisie "
      "et le fonctionnement du repli réseau. Le projet est publié sur GitHub dans le dépôt "
      "public CurrencyConverter."),
]

doc = SimpleDocTemplate(
    str(SORTIE), pagesize=A4,
    leftMargin=45, rightMargin=45, topMargin=45, bottomMargin=42,
    title="TP4 Android - Currency Converter",
    author="Shahed Attari",
    subject="Compte rendu TP4 Android",
)
doc.build(histoires, onFirstPage=page_decor, onLaterPages=page_decor)
print("PDF généré:", SORTIE)
