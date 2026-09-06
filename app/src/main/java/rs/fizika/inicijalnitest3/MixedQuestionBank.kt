package rs.fizika.inicijalnitest3

object MixedQuestionBank {
    // Konačna baza: 170 pitanja iz gradiva II razreda gimnazije.
    // 100 standardnih + 35 osnovnih računskih + 35 srednjih računskih.
    // Svaki test: 14 standardnih + 3 osnovna + 3 srednja = 20 pitanja.
    fun buildTest20(): List<Question> {
        val standardPool = FinalQuestionBank.standard
        val basicPool = FinalQuestionBank.basic
        val mediumPool = FinalQuestionBank.medium

        require(standardPool.size == 100) { "Standardna baza mora imati 100 pitanja." }
        require(basicPool.size == 35) { "Baza osnovnih računskih mora imati 35 pitanja." }
        require(mediumPool.size == 35) { "Baza srednjih računskih mora imati 35 pitanja." }

        return (
            standardPool.shuffled().take(14) +
            basicPool.shuffled().take(3) +
            mediumPool.shuffled().take(3)
        ).shuffled()
    }
}
