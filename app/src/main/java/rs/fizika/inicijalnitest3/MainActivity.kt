package rs.fizika.inicijalnitest3

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class Question(
    val text: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String
)

class MainActivity : AppCompatActivity() {
    private val dark = Color.parseColor("#29265F")
    private val primary = Color.parseColor("#4C4AA7")
    private val accent = Color.parseColor("#D7A54A")
    private val bg = Color.parseColor("#F6F6FB")
    private val textColor = Color.parseColor("#252544")
    private val muted = Color.parseColor("#74748C")
    private val line = Color.parseColor("#D8D8EA")
    private val prefsName = "fizika3_premium_stats"

    private val handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var elapsed = 0L
    private var running = false
    private var questions: List<Question> = emptyList()
    private var current = 0
    private var score = 0
    private var checked = false

    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var questionText: TextView
    private lateinit var options: RadioGroup
    private lateinit var explanation: TextView
    private lateinit var checkButton: Button
    private lateinit var nextButton: Button
    private lateinit var timer: TextView

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!running) return
            elapsed = SystemClock.elapsedRealtime() - startTime
            timer.text = "Vreme ${formatTime(elapsed)}"
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark
        showHome()
    }

    private fun showHome() {
        stopTimer()
        val scroll = ScrollView(this).apply { isFillViewport = true; setBackgroundColor(bg) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        scroll.addView(root)

        val hero = ImageView(this).apply {
            setImageResource(R.drawable.fizika3_hero)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "Fizika 3 – Gimnazija Inđija"
        }
        root.addView(hero, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(455)))

        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(14), dp(18), dp(28)) }
        root.addView(body)
        body.addView(primaryButton("▶   ZAPOČNI TEST    →").apply { setOnClickListener { startTest() } }, LinearLayout.LayoutParams(-1, dp(62)))
        gap(body, 14)

        val tiles = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        addTile(tiles, "▤", "OBLASTI", "Ponovi ključne teme") { showAreas() }
        addTile(tiles, "▥", "STATISTIKA", "Prati svoj napredak") { showStatistics() }
        addTile(tiles, "⚙", "OPCIJE", "Prilagodi aplikaciju") { showOptions() }
        body.addView(tiles, LinearLayout.LayoutParams(-1, -2)); gap(body, 14)

        val p = getSharedPreferences(prefsName, MODE_PRIVATE); val attempts = p.getInt("attempts", 0); val best = p.getInt("best", 0)
        val status = card(); status.addView(text("TVOJ NAPREDAK", 12, primary).apply { setTypeface(null, Typeface.BOLD); letterSpacing = .08f })
        status.addView(text(if (attempts == 0) "Još nema urađenih testova. Prvi rezultat će se sačuvati ovde." else "Urađenih testova: $attempts   •   Najbolji rezultat: $best%", 14, textColor).apply { setPadding(0, dp(7), 0, 0) })
        body.addView(status, LinearLayout.LayoutParams(-1, -2))
        body.addView(centered("ISTRAŽUJ  •  MISLI  •  NAPREDUJ", 11, muted).apply { letterSpacing = .08f; setPadding(0, dp(20), 0, dp(8)) })
        setContentView(scroll)
    }

    private fun showAreas() {
        showInfo("Oblasti", """MOLEKULSKA FIZIKA I TERMODINAMIKA
• idealni gas i gasni zakoni
• unutrašnja energija, toplota i rad
• prvi i drugi zakon termodinamike

FLUIDI
• pritisak i hidrostatički pritisak
• Arhimedov zakon
• strujanje fluida i Bernulijeva jednačina

ELEKTROSTATIKA
• Kulonov zakon, električno polje i potencijal
• kondenzatori i energija električnog polja

JEDNOSMERNA STRUJA
• napon, jačina struje i otpor
• Omov zakon i vezivanje otpornika
• Kirhofova pravila i električna snaga""".trimIndent())
    }

    private fun showStatistics() {
        val p = getSharedPreferences(prefsName, MODE_PRIVATE)
        val attempts = p.getInt("attempts", 0); val best = p.getInt("best", 0); val sum = p.getInt("sum", 0); val last = p.getInt("last", 0)
        val avg = if (attempts == 0) 0 else (sum.toFloat() / attempts).toInt()
        showInfo("Statistika", "Urađenih testova: $attempts\nPoslednji rezultat: $last%\nNajbolji rezultat: $best%\nProsečan rezultat: $avg%\n\nRezultati se čuvaju samo na ovom uređaju.")
    }

    private fun showOptions() {
        AlertDialog.Builder(this).setTitle("Opcije").setItems(arrayOf("Resetuj statistiku", "O aplikaciji")) { _, which ->
            if (which == 0) { getSharedPreferences(prefsName, MODE_PRIVATE).edit().clear().apply(); Toast.makeText(this, "Statistika je resetovana.", Toast.LENGTH_SHORT).show(); showHome() }
            else AlertDialog.Builder(this).setTitle("Fizika 3").setMessage("Inicijalni test za obnavljanje gradiva drugog razreda.\n\nGimnazija Inđija").setPositiveButton("U redu", null).show()
        }.setNegativeButton("Zatvori", null).show()
    }

    private fun showInfo(title: String, bodyText: String) {
        val scroll = ScrollView(this).apply { isFillViewport = true; setBackgroundColor(bg) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(22), dp(20), dp(26)) }; scroll.addView(root)
        root.addView(text(title, 30, dark).apply { setTypeface(null, Typeface.BOLD) }); gap(root, 14)
        val c = card(); c.addView(text(bodyText, 15, textColor)); root.addView(c, LinearLayout.LayoutParams(-1, -2)); gap(root, 18)
        root.addView(primaryButton("←  Nazad na početnu").apply { setOnClickListener { showHome() } }, LinearLayout.LayoutParams(-1, dp(56))); setContentView(scroll)
    }

    private fun startTest() { questions = MixedQuestionBank.buildTest20(); current = 0; score = 0; checked = false; elapsed = 0L; startTime = SystemClock.elapsedRealtime(); running = true; buildTestUi(); showQuestion(); handler.post(timerRunnable) }

    private fun buildTestUi() {
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(22)) }; scroll.addView(content); page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        content.addView(text("Fizika 3", 25, dark).apply { setTypeface(null, Typeface.BOLD) }); content.addView(text("Inicijalni test • gradivo drugog razreda", 13, muted).apply { setPadding(0, dp(2), 0, dp(13)) })
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        progressText = text("", 13, muted); scoreText = text("", 13, dark).apply { setTypeface(null, Typeface.BOLD) }; top.addView(progressText, LinearLayout.LayoutParams(0, dp(34), 1f)); top.addView(scoreText); content.addView(top)
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 20; progressTintList = ColorStateList.valueOf(primary) }; content.addView(progressBar, LinearLayout.LayoutParams(-1, dp(9))); gap(content, 14)
        val qcard = card(); questionText = text("", 20, textColor).apply { setTypeface(null, Typeface.BOLD); setPadding(0, dp(4), 0, dp(10)) }; qcard.addView(questionText); options = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }; qcard.addView(options); content.addView(qcard, LinearLayout.LayoutParams(-1, -2))
        explanation = text("", 15, textColor).apply { setPadding(dp(14), dp(12), dp(14), dp(12)); visibility = android.view.View.GONE }; content.addView(explanation, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(12), 0, 0) })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(15), 0, 0) }
        checkButton = primaryButton("Proveri odgovor").apply { setOnClickListener { checkAnswer() } }; actions.addView(checkButton, LinearLayout.LayoutParams(0, dp(54), 1f))
        nextButton = Button(this).apply { isAllCaps = false; text = "Sledeće pitanje"; textSize = 15f; setTextColor(dark); setTypeface(null, Typeface.BOLD); background = round(Color.WHITE, line, 20, 1); visibility = android.view.View.GONE; setOnClickListener { nextQuestion() } }; actions.addView(nextButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { setMargins(dp(10), 0, 0, 0) }); content.addView(actions)
        val timerBar = LinearLayout(this).apply { gravity = Gravity.CENTER; setBackgroundColor(dark) }; timer = centered("Vreme 00:00", 14, Color.WHITE).apply { setTypeface(null, Typeface.BOLD) }; timerBar.addView(timer); page.addView(timerBar, LinearLayout.LayoutParams(-1, dp(50)))
        setContentView(page)
    }

    private fun showQuestion() {
        if (current >= questions.size) { showResult(); return }
        checked = false; val q = questions[current]; progressText.text = "Pitanje ${current + 1} od ${questions.size}"; scoreText.text = "Tačno: $score"; progressBar.progress = current + 1; questionText.text = q.text; options.removeAllViews()
        q.options.forEachIndexed { index, option -> options.addView(optionButton(option, 1000 + index)) }
        explanation.visibility = android.view.View.GONE; checkButton.isEnabled = true; checkButton.alpha = 1f; nextButton.visibility = android.view.View.GONE
    }

    private fun checkAnswer() {
        if (checked) return
        val selected = options.checkedRadioButtonId; if (selected == -1) { Toast.makeText(this, "Izaberi jedan odgovor.", Toast.LENGTH_SHORT).show(); return }
        checked = true; val chosen = selected - 1000; val q = questions[current]; val ok = chosen == q.correct; if (ok) score++
        explanation.text = if (ok) "✓ Tačno!\n${q.explanation}" else "✗ Netačno. Tačan odgovor: ${q.options[q.correct]}\n${q.explanation}"
        explanation.background = if (ok) round(Color.parseColor("#E7F7EF"), Color.parseColor("#69BE91"), 14, 1) else round(Color.parseColor("#FFF7E6"), accent, 14, 1)
        explanation.visibility = android.view.View.VISIBLE; scoreText.text = "Tačno: $score"; for (i in 0 until options.childCount) options.getChildAt(i).isEnabled = false; checkButton.isEnabled = false; checkButton.alpha = .5f; nextButton.text = if (current == questions.lastIndex) "Rezultat" else "Sledeće pitanje"; nextButton.visibility = android.view.View.VISIBLE
    }

    private fun nextQuestion() { if (!checked) return; current++; if (current >= questions.size) showResult() else showQuestion() }

    private fun showResult() {
        stopTimer(); val total = questions.size; val percent = if (total == 0) 0 else ((100f * score) / total).toInt(); saveStats(percent)
        val scroll = ScrollView(this).apply { isFillViewport = true; setBackgroundColor(bg) }; val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(dp(20), dp(32), dp(20), dp(28)) }; scroll.addView(root)
        root.addView(centered("REZULTAT", 13, primary).apply { setTypeface(null, Typeface.BOLD); letterSpacing = .12f }); gap(root, 10); root.addView(centered("$percent%", 54, dark).apply { setTypeface(null, Typeface.BOLD) }); root.addView(centered("$score / $total tačnih odgovora", 18, textColor)); root.addView(centered("Vreme izrade: ${formatTime(elapsed)}", 14, muted).apply { setPadding(0, dp(8), 0, 0) }); gap(root, 22)
        root.addView(primaryButton("↻  Ponovi test").apply { setOnClickListener { startTest() } }, LinearLayout.LayoutParams(-1, dp(58))); gap(root, 12)
        root.addView(Button(this).apply { isAllCaps = false; text = "←  Početna"; textSize = 16f; setTextColor(dark); setTypeface(null, Typeface.BOLD); background = round(Color.WHITE, line, 20, 1); setOnClickListener { showHome() } }, LinearLayout.LayoutParams(-1, dp(56))); setContentView(scroll)
    }

    private fun saveStats(percent: Int) { val p = getSharedPreferences(prefsName, MODE_PRIVATE); val attempts = p.getInt("attempts", 0) + 1; val best = maxOf(percent, p.getInt("best", 0)); val sum = p.getInt("sum", 0) + percent; p.edit().putInt("attempts", attempts).putInt("best", best).putInt("sum", sum).putInt("last", percent).apply() }
    private fun stopTimer() { if (running) elapsed = SystemClock.elapsedRealtime() - startTime; running = false; handler.removeCallbacks(timerRunnable) }
    private fun formatTime(ms: Long): String { val total = ms / 1000; return String.format("%02d:%02d", total / 60, total % 60) }
    override fun onDestroy() { handler.removeCallbacks(timerRunnable); super.onDestroy() }
    private fun primaryButton(label: String) = Button(this).apply { text = label; textSize = 17f; isAllCaps = false; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD); background = round(primary, accent, 25, 1); elevation = dp(4).toFloat() }
    private fun addTile(row: LinearLayout, icon: String, title: String, sub: String, click: () -> Unit) { val c = card().apply { gravity = Gravity.CENTER; isClickable = true; isFocusable = true; setOnClickListener { click() } }; c.addView(centered(icon, 27, primary).apply { setTypeface(null, Typeface.BOLD) }); c.addView(centered(title, 12, dark).apply { setTypeface(null, Typeface.BOLD); setPadding(0, dp(3), 0, 0) }); c.addView(centered(sub, 10, muted).apply { setPadding(0, dp(4), 0, 0) }); row.addView(c, LinearLayout.LayoutParams(0, dp(120), 1f).apply { setMargins(dp(4), 0, dp(4), 0) }) }
    private fun card() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(15), dp(16), dp(15)); background = round(Color.WHITE, line, 20, 1); elevation = dp(2).toFloat() }
    private fun optionButton(value: String, idValue: Int) = RadioButton(this).apply { id = idValue; text = value; textSize = 16f; setTextColor(textColor); buttonTintList = ColorStateList.valueOf(primary); setPadding(dp(10), dp(10), dp(10), dp(10)); background = round(Color.parseColor("#F9FCFB"), line, 14, 1); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(5), 0, dp(5)) } }
    private fun text(value: String, sp: Int, color: Int) = TextView(this).apply { text = value; textSize = sp.toFloat(); setTextColor(color); setLineSpacing(0f, 1.12f) }
    private fun centered(value: String, sp: Int, color: Int) = text(value, sp, color).apply { gravity = Gravity.CENTER }
    private fun gap(root: LinearLayout, h: Int) { root.addView(Space(this), LinearLayout.LayoutParams(1, dp(h))) }
    private fun round(fill: Int, stroke: Int, radius: Int, strokeWidth: Int) = GradientDrawable().apply { shape = GradientDrawable.RECTANGLE; setColor(fill); cornerRadius = dp(radius).toFloat(); if (strokeWidth > 0) setStroke(dp(strokeWidth), stroke) }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density + .5f).toInt()
}
