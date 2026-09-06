package rs.fizika.inicijalnitest3

object MixedQuestionBank {
    fun buildTest20(): List<Question> {
        // Ustanovljeni princip: 14 standardnih + 6 računskih,
        // od računskih 3 osnovna + 3 srednja, zatim nasumičan redosled.
        val originalCalculationTexts = setOf(
            "Gas primi 700 J toplote i izvrši rad 250 J. Kolika je promena unutrašnje energije?",
            "Gas se izobarski širi pri p = 2·10⁵ Pa za ΔV = 3·10⁻³ m³. Koliki rad izvrši?",
            "Cev se suzi sa 8 cm² na 2 cm². Ako je v1 = 1 m/s, kolika je v2?",
            "Na dubini 5 m u vodi, bez atmosferskog pritiska, koliki je hidrostatički pritisak? Uzmi ρ=1000 kg/m³ i g=10 m/s².",
            "Dva naboja 2 μC i 3 μC udaljena su 0,30 m. Približna Kulonova sila je:",
            "Između ploča je U = 300 V i d = 1,5 cm. Koliko je E?",
            "Kondenzator C = 10 μF priključen je na U = 100 V. Kolika je energija?",
            "Otpornik R = 46 Ω priključen je na 230 V. Kolika je snaga?",
            "Izvor ε = 12 V i r = 1 Ω priključen je na R = 5 Ω. Kolika je struja?"
        )
        val originalStandard = QuestionBank.all.filterNot { it.text in originalCalculationTexts }
        val standardPool = (originalStandard + ExpandedQuestionBank.standard).distinctBy { it.text }
        val basicPool = ExpandedQuestionBank.basicCalculations.distinctBy { it.text }
        val mediumPool = ExpandedQuestionBank.mediumCalculations.distinctBy { it.text }

        return (standardPool.shuffled().take(14) +
                basicPool.shuffled().take(3) +
                mediumPool.shuffled().take(3)).shuffled()
    }
}
