package rs.fizika.inicijalnitest3

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class Question(
    val text: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String
)

class MainActivity : AppCompatActivity() {
    private val blue = Color.parseColor("#1267D8")
    private val darkBlue = Color.parseColor("#123A78")
    private val lightBlue = Color.parseColor("#EAF4FF")
    private val borderBlue = Color.parseColor("#B9D9F7")
    private val textDark = Color.parseColor("#14345E")
    private val muted = Color.parseColor("#58779E")

    private lateinit var hero: ImageView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var intro: TextView
    private lateinit var start: Button
    private lateinit var quote: TextView
    private lateinit var luck: TextView
    private lateinit var progress: TextView
    private lateinit var question: TextView
    private lateinit var options: RadioGroup
    private lateinit var optionButtons: List<RadioButton>
    private lateinit var explanation: TextView
    private lateinit var next: Button
    private lateinit var result: TextView
    private lateinit var restart: Button
    private lateinit var timer: TextView

    private var questions: List<Question> = emptyList()
    private var current = 0
    private var score = 0
    private var checked = false
    private val handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var elapsed = 0L
    private var running = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            elapsed = SystemClock.elapsedRealtime() - startTime
            timer.text = "⏱  Vreme: ${formatTime(elapsed)}"
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(20), dp(22), dp(28))
            setBackgroundColor(Color.WHITE)
        }

        hero = ImageView(this).apply {
            setImageResource(R.drawable.hero_fizika3)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = rounded(Color.WHITE, Color.TRANSPARENT, 28f, 0)
            clipToOutline = true
            elevation = dp(7).toFloat()
            contentDescription = "Ilustracija fizike"
        }
        root.addView(hero, LinearLayout.LayoutParams(dp(220), dp(220)).apply { bottomMargin = dp(20) })

        title = TextView(this).apply {
            text = "Fizika 3"
            textSize = 34f
            setTextColor(darkBlue)
            setTypeface(Typeface.DEFAULT_BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWrap(2))

        subtitle = TextView(this).apply {
            text = "Inicijalni test"
            textSize = 23f
            setTextColor(blue)
            setTypeface(Typeface.DEFAULT_BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(subtitle, matchWrap(14))

        intro = TextView(this).apply {
            text = "20 nasumično izabranih pitanja iz gradiva drugog razreda"
            textSize = 16f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setLineSpacing(0f, 1.15f)
        }
        root.addView(intro, matchWrap(22))

        start = Button(this).apply {
            text = "Počni test  →"
            textSize = 19f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            isAllCaps = false
            background = rounded(blue, blue, 18f, 1)
            elevation = dp(5).toFloat()
        }
        root.addView(start, matchHeight(dp(64), 22))

        quote = TextView(this).apply {
            text = "„Energija sveta je konstantna; entropija sveta teži maksimumu.“\n\nRudolf Clausius"
            textSize = 15f
            setTextColor(darkBlue)
            setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC))
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(lightBlue, Color.TRANSPARENT, 20f, 0)
        }
        root.addView(quote, matchWrap(14))

        luck = TextView(this).apply {
            text = "Srećno! Poveži pojavu, zakon, formulu i fizičko značenje."
            textSize = 15f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(lightBlue, Color.TRANSPARENT, 18f, 0)
        }
        root.addView(luck, matchWrap(8))

        progress = TextView(this).apply {
            textSize = 17f
            setTextColor(darkBlue)
            setTypeface(Typeface.DEFAULT_BOLD)
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(lightBlue, borderBlue, 16f, 1)
        }
        root.addView(progress, matchWrap(14))

        question = TextView(this).apply {
            textSize = 21f
            setTextColor(textDark)
            setTypeface(Typeface.DEFAULT_BOLD)
            visibility = View.GONE
            setPadding(dp(20), dp(22), dp(20), dp(22))
            background = cardBackground()
            elevation = dp(5).toFloat()
        }
        root.addView(question, matchWrap(14))

        options = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            visibility = View.GONE
        }
        root.addView(options, matchWrap(10))

        optionButtons = List(4) { index ->
            RadioButton(this).apply {
                id = View.generateViewId()
                textSize = 17f
                setTextColor(textDark)
                buttonTintList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(blue, blue)
                )
                setPadding(dp(15), dp(12), dp(15), dp(12))
                background = answerBackground()
                setOnClickListener { checkAnswer() }
                options.addView(this, RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT).apply {
                    bottomMargin = if (index == 3) 0 else dp(10)
                })
            }
        }

        explanation = TextView(this).apply {
            textSize = 16f
            setTextColor(textDark)
            visibility = View.GONE
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(lightBlue, borderBlue, 16f, 1)
        }
        root.addView(explanation, matchWrap(14))

        next = Button(this).apply {
            text = "Sledeće  →"
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            isAllCaps = false
            visibility = View.GONE
            background = rounded(blue, blue, 18f, 1)
            elevation = dp(5).toFloat()
        }
        root.addView(next, matchHeight(dp(58), 10))

        result = TextView(this).apply {
            textSize = 22f
            setTextColor(darkBlue)
            setTypeface(Typeface.DEFAULT_BOLD)
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(dp(20), dp(28), dp(20), dp(28))
            background = cardBackground()
            elevation = dp(6).toFloat()
        }
        root.addView(result, matchWrap(16))

        restart = Button(this).apply {
            text = "↻  Ponovi test"
            textSize = 17f
            setTextColor(blue)
            setTypeface(Typeface.DEFAULT_BOLD)
            isAllCaps = false
            visibility = View.GONE
            background = rounded(Color.WHITE, blue, 18f, 2)
        }
        root.addView(restart, matchHeight(dp(58), 10))

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
            addView(root)
        }

        timer = TextView(this).apply {
            text = "⏱  Vreme: 00:00"
            textSize = 17f
            setTextColor(darkBlue)
            setTypeface(Typeface.DEFAULT_BOLD)
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(dp(12), dp(13), dp(12), dp(13))
            background = rounded(lightBlue, borderBlue, 0f, 1)
        }

        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(timer, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        setContentView(screen)

        start.setOnClickListener { startTest() }
        next.setOnClickListener { goNext() }
        restart.setOnClickListener { showStart() }
    }

    private fun startTest() {
        questions = MixedQuestionBank.buildTest20()
        current = 0
        score = 0
        hero.visibility = View.GONE
        intro.visibility = View.GONE
        start.visibility = View.GONE
        quote.visibility = View.GONE
        luck.visibility = View.GONE
        subtitle.text = "Test u toku"
        result.visibility = View.GONE
        restart.visibility = View.GONE
        progress.visibility = View.VISIBLE
        question.visibility = View.VISIBLE
        options.visibility = View.VISIBLE
        timer.visibility = View.VISIBLE
        startTimer()
        showQuestion()
    }

    private fun showQuestion() {
        val q = questions[current]
        checked = false
        val percent = ((current + 1) * 100) / questions.size
        progress.text = "Pitanje ${current + 1} / ${questions.size}     •     $percent%"
        question.text = q.text
        options.clearCheck()
        optionButtons.forEachIndexed { i, button ->
            val letter = ('A'.code + i).toChar()
            button.text = "   $letter     ${q.options[i]}"
            button.isEnabled = true
        }
        explanation.text = ""
        explanation.visibility = View.GONE
        next.visibility = View.GONE
    }

    private fun checkAnswer() {
        if (checked) return
        val selectedId = options.checkedRadioButtonId
        if (selectedId == -1) return
        val selected = optionButtons.indexOfFirst { it.id == selectedId }
        val q = questions[current]
        val correct = selected == q.correct
        if (correct) score++
        checked = true
        optionButtons.forEach { it.isEnabled = false }
        explanation.text = if (correct) {
            "✓  Tačan odgovor!\n\n${q.explanation}"
        } else {
            "✗  Netačan odgovor.\n\nTačan odgovor: ${q.options[q.correct]}\n\n${q.explanation}"
        }
        explanation.visibility = View.VISIBLE
        next.text = if (current == questions.lastIndex) "Prikaži rezultat  →" else "Sledeće  →"
        next.visibility = View.VISIBLE
    }

    private fun goNext() {
        if (!checked) return
        if (current < questions.lastIndex) {
            current++
            showQuestion()
        } else showResult()
    }

    private fun showResult() {
        stopTimer()
        progress.visibility = View.GONE
        question.visibility = View.GONE
        options.visibility = View.GONE
        explanation.visibility = View.GONE
        next.visibility = View.GONE
        timer.visibility = View.GONE
        val percent = score * 100 / questions.size
        val grade = when {
            percent >= 90 -> "Odlično!"
            percent >= 75 -> "Vrlo dobro!"
            percent >= 60 -> "Dobro!"
            percent >= 45 -> "Solidno!"
            else -> "Pokušaj ponovo!"
        }
        subtitle.text = "Inicijalni test"
        result.text = "🏆\n\n$grade\n\n$percent%\n\n$score / ${questions.size} tačnih odgovora\n\n⏱ Vreme izrade: ${formatTime(elapsed)}"
        result.visibility = View.VISIBLE
        restart.visibility = View.VISIBLE
    }

    private fun showStart() {
        stopTimer()
        subtitle.text = "Inicijalni test"
        hero.visibility = View.VISIBLE
        intro.visibility = View.VISIBLE
        start.visibility = View.VISIBLE
        quote.visibility = View.VISIBLE
        luck.visibility = View.VISIBLE
        progress.visibility = View.GONE
        question.visibility = View.GONE
        options.visibility = View.GONE
        explanation.visibility = View.GONE
        next.visibility = View.GONE
        result.visibility = View.GONE
        restart.visibility = View.GONE
        timer.visibility = View.GONE
    }

    private fun startTimer() {
        handler.removeCallbacks(timerRunnable)
        elapsed = 0L
        startTime = SystemClock.elapsedRealtime()
        running = true
        timer.text = "⏱  Vreme: 00:00"
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        if (running) elapsed = SystemClock.elapsedRealtime() - startTime
        running = false
        handler.removeCallbacks(timerRunnable)
    }

    private fun formatTime(ms: Long): String {
        val total = ms / 1000
        return String.format("%02d:%02d", total / 60, total % 60)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timerRunnable)
        super.onDestroy()
    }

    private fun cardBackground() = rounded(Color.WHITE, borderBlue, 20f, 1)

    private fun answerBackground(): StateListDrawable {
        val selected = rounded(lightBlue, blue, 18f, 2)
        val normal = rounded(Color.WHITE, borderBlue, 18f, 1)
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), selected)
            addState(intArrayOf(), normal)
        }
    }

    private fun rounded(fill: Int, stroke: Int, radiusDp: Float, strokeDp: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fill)
        cornerRadius = dp(radiusDp.toInt()).toFloat()
        if (strokeDp > 0 && stroke != Color.TRANSPARENT) setStroke(dp(strokeDp), stroke)
    }

    private fun matchWrap(bottom: Int = 0) = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
        bottomMargin = dp(bottom)
    }

    private fun matchHeight(height: Int, bottom: Int = 0) = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height).apply {
        bottomMargin = dp(bottom)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
