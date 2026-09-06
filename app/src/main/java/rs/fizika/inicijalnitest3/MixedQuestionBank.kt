package rs.fizika.inicijalnitest3

object MixedQuestionBank {
    fun buildTest20(): List<Question> = QuestionBank.all.shuffled().take(20)
}
