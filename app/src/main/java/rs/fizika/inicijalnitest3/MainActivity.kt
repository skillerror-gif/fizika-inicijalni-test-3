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
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class Question(val text:String,val options:List<String>,val correct:Int,val explanation:String)

class MainActivity:AppCompatActivity(){
    private val dark=Color.parseColor("#17345C")
    private val primary=Color.parseColor("#365FA8")
    private val accent=Color.parseColor("#D3A94B")
    private val bg=Color.parseColor("#F4F7FC")
    private val text=Color.parseColor("#24364F")
    private val muted=Color.parseColor("#697B96")
    private val line=Color.parseColor("#CFD9EA")
    private val prefsName="fizika3_premium_stats"
    private val handler=Handler(Looper.getMainLooper())
    private var startTime=0L; private var elapsed=0L; private var running=false
    private var questions:List<Question> = emptyList(); private var current=0; private var score=0; private var checked=false
    private val errorsByArea=linkedMapOf<String,Int>()
    private lateinit var progressText:TextView; private lateinit var scoreText:TextView; private lateinit var progressBar:ProgressBar
    private lateinit var questionText:TextView; private lateinit var options:RadioGroup; private lateinit var explanation:TextView
    private lateinit var checkButton:Button; private lateinit var nextButton:Button; private lateinit var timer:TextView
    private val tick=object:Runnable{override fun run(){if(!running)return;elapsed=SystemClock.elapsedRealtime()-startTime;timer.text="Vreme ${format(elapsed)}";handler.postDelayed(this,1000)}}

    override fun onCreate(s:Bundle?){super.onCreate(s);window.statusBarColor=dark;window.navigationBarColor=dark;showHome()}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun round(fill:Int,stroke:Int,r:Int,sw:Int)=GradientDrawable().apply{shape=GradientDrawable.RECTANGLE;setColor(fill);cornerRadius=dp(r).toFloat();if(sw>0)setStroke(dp(sw),stroke)}
    private fun tv(s:String,sp:Float,c:Int)=TextView(this).apply{text=s;textSize=sp;setTextColor(c);setLineSpacing(0f,1.12f)}
    private fun center(s:String,sp:Float,c:Int)=tv(s,sp,c).apply{gravity=Gravity.CENTER}
    private fun button(label:String)=Button(this).apply{text=label;textSize=17f;isAllCaps=false;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);background=round(primary,accent,24,1);elevation=dp(4).toFloat()}
    private fun card()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(15),dp(16),dp(15));background=round(Color.WHITE,line,20,1);elevation=dp(2).toFloat()}
    private fun gap(r:LinearLayout,h:Int){r.addView(Space(this),LinearLayout.LayoutParams(1,dp(h)))}

    private fun showHome(){
        stopTimer();val scroll=ScrollView(this).apply{isFillViewport=true;setBackgroundColor(bg)};val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(bg)};scroll.addView(root)
        val hero=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(24),dp(28),dp(24),dp(28));background=round(dark,dark,0,0)}
        hero.addView(center("GIMNAZIJA INĐIJA",14f,Color.WHITE).apply{letterSpacing=.12f;setTypeface(null,Typeface.BOLD)})
        hero.addView(center("FIZIKA 3",38f,Color.WHITE).apply{setTypeface(null,Typeface.BOLD);setPadding(0,dp(12),0,0)})
        hero.addView(center("PREMIUM • Inicijalni test",16f,Color.parseColor("#E5ECFF")).apply{setTypeface(null,Typeface.BOLD);setPadding(0,dp(4),0,0)})
        hero.addView(center("pV = nRT   •   Q = ΔU + A   •   I = U/R",17f,Color.parseColor("#B7CBFF")).apply{setPadding(0,dp(20),0,0)})
        hero.addView(center("TERMODINAMIKA  •  FLUIDI  •  ELEKTROSTATIKA  •  STRUJA",11f,Color.parseColor("#D3A94B")).apply{letterSpacing=.04f;setTypeface(null,Typeface.BOLD);setPadding(0,dp(10),0,0)})
        hero.addView(center("„Energija sveta je konstantna; entropija sveta teži maksimumu.“\nRudolf Klauzijus",14f,Color.WHITE).apply{setTypeface(Typeface.create(Typeface.SERIF,Typeface.ITALIC));setPadding(dp(8),dp(20),dp(8),0)})
        root.addView(hero,LinearLayout.LayoutParams(-1,dp(352)))
        val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(14),dp(18),dp(28))};root.addView(body)
        body.addView(button("▶   ZAPOČNI TEST    →").apply{setOnClickListener{startTest()}},LinearLayout.LayoutParams(-1,dp(62)));gap(body,14)
        val tiles=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER};addTile(tiles,"▤","GRADIVO","Ponovi ključne teme"){showAreas()};addTile(tiles,"▥","STATISTIKA","Prati svoj napredak"){showStats()};addTile(tiles,"⚙","OPCIJE","Prilagodi aplikaciju"){showOptions()};body.addView(tiles)
        gap(body,14);val p=getSharedPreferences(prefsName,MODE_PRIVATE);val attempts=p.getInt("attempts",0);val best=p.getInt("best",0);val status=card();status.addView(tv("TVOJ NAPREDAK",12f,primary).apply{setTypeface(null,Typeface.BOLD);letterSpacing=.08f});status.addView(tv(if(attempts==0)"Još nema urađenih testova. Prvi rezultat će se sačuvati ovde." else "Urađenih testova: $attempts   •   Najbolji rezultat: $best%",14f,text).apply{setPadding(0,dp(7),0,0)});body.addView(status)
        body.addView(center("ISTRAŽUJ  •  MISLI  •  NAPREDUJ",11f,muted).apply{letterSpacing=.08f;setPadding(0,dp(20),0,dp(8))});setContentView(scroll)
    }
    private fun addTile(row:LinearLayout,icon:String,title:String,sub:String,go:()->Unit){val c=card();c.gravity=Gravity.CENTER;c.isClickable=true;c.isFocusable=true;c.setOnClickListener{go()};c.addView(center(icon,26f,primary).apply{setTypeface(null,Typeface.BOLD)});c.addView(center(title,12f,dark).apply{setTypeface(null,Typeface.BOLD);setPadding(0,dp(3),0,0)});c.addView(center(sub,10f,muted).apply{setPadding(0,dp(4),0,0)});row.addView(c,LinearLayout.LayoutParams(0,dp(120),1f).apply{setMargins(dp(4),0,dp(4),0)})}
    private fun showAreas()=showInfo("Gradivo","MOLEKULARNA FIZIKA I TERMODINAMIKA\n• temperatura, gasni zakoni i idealni gas\n• unutrašnja energija, prvi i drugi zakon termodinamike\n• fazni prelazi i entropija\n\nFLUIDI I ELASTIČNOST\n• pritisak, Pascalov i Arhimedov zakon\n• kontinuitet, Bernulijeva jednačina i površinski napon\n\nELEKTROSTATIKA\n• električni naboj, Kulonov zakon i električno polje\n• potencijal, napon, kapacitet i kondenzatori\n\nJEDNOSMERNA STRUJA\n• jačina struje, Omov zakon i električni otpor\n• redna i paralelna veza, Kirhofova pravila\n• rad, energija i snaga električne struje")
    private fun showStats(){val p=getSharedPreferences(prefsName,MODE_PRIVATE);val a=p.getInt("attempts",0);val best=p.getInt("best",0);val sum=p.getInt("sum",0);val last=p.getInt("last",0);val avg=if(a==0)0 else Math.round(sum/a.toFloat());val weak=p.getString("weak","Još nema podataka.");showInfo("Statistika","Urađenih testova: $a\nPoslednji rezultat: $last%\nNajbolji rezultat: $best%\nProsečan rezultat: $avg%\n\nOblasti za dodatno ponavljanje:\n$weak")}
    private fun showOptions(){AlertDialog.Builder(this).setTitle("Opcije").setItems(arrayOf("Resetuj statistiku","O aplikaciji")){_,w->if(w==0){getSharedPreferences(prefsName,MODE_PRIVATE).edit().clear().apply();Toast.makeText(this,"Statistika je resetovana.",Toast.LENGTH_SHORT).show()}else AlertDialog.Builder(this).setTitle("Fizika 3 Premium").setMessage("Inicijalni test iz fizike za obnavljanje gradiva drugog razreda gimnazije.\n\n20 nasumično izabranih pitanja, automatsko bodovanje, objašnjenja, vreme izrade i statistika napretka.\n\nGimnazija Inđija").setPositiveButton("U redu",null).show()}.setNegativeButton("Zatvori",null).show()}
    private fun showInfo(t:String,b:String){val sc=ScrollView(this).apply{isFillViewport=true;setBackgroundColor(bg)};val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(22),dp(20),dp(26))};sc.addView(r);r.addView(tv(t,30f,dark).apply{setTypeface(null,Typeface.BOLD)});gap(r,14);val c=card();c.addView(tv(b,15f,text));r.addView(c);gap(r,18);r.addView(button("←  Nazad na početnu").apply{setOnClickListener{showHome()}},LinearLayout.LayoutParams(-1,dp(56)));setContentView(sc)}

    private fun startTest(){questions=MixedQuestionBank.buildTest20();current=0;score=0;checked=false;errorsByArea.clear();startTime=SystemClock.elapsedRealtime();running=true;buildTestUi();showQuestion();handler.post(tick)}
    private fun buildTestUi(){val page=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(bg)};val scroll=ScrollView(this).apply{isFillViewport=true};val c=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(22))};scroll.addView(c);page.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));c.addView(tv("Fizika 3",25f,dark).apply{setTypeface(null,Typeface.BOLD)});c.addView(tv("Inicijalni test • gradivo drugog razreda",13f,muted).apply{setPadding(0,dp(2),0,dp(13))});val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};progressText=tv("",13f,muted);scoreText=tv("",13f,dark).apply{setTypeface(null,Typeface.BOLD)};top.addView(progressText,LinearLayout.LayoutParams(0,dp(34),1f));top.addView(scoreText);c.addView(top);progressBar=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{max=20;progressTintList=ColorStateList.valueOf(primary)};c.addView(progressBar,LinearLayout.LayoutParams(-1,dp(9)));gap(c,14);val qc=card();questionText=tv("",20f,text).apply{setTypeface(null,Typeface.BOLD)};qc.addView(questionText);options=RadioGroup(this).apply{orientation=RadioGroup.VERTICAL};qc.addView(options);c.addView(qc);explanation=tv("",15f,text).apply{setPadding(dp(14),dp(12),dp(14),dp(12));visibility=View.GONE};c.addView(explanation);val act=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(0,dp(15),0,0)};checkButton=button("Proveri odgovor").apply{setOnClickListener{checkAnswer()}};nextButton=Button(this).apply{text="Sledeće pitanje";isAllCaps=false;setTextColor(dark);background=round(Color.WHITE,line,20,1);visibility=View.GONE;setOnClickListener{nextQuestion()}};act.addView(checkButton,LinearLayout.LayoutParams(0,dp(54),1f));act.addView(nextButton,LinearLayout.LayoutParams(0,dp(54),1f).apply{setMargins(dp(10),0,0,0)});c.addView(act);timer=center("Vreme 00:00",14f,Color.WHITE).apply{setTypeface(null,Typeface.BOLD);setBackgroundColor(dark)};page.addView(timer,LinearLayout.LayoutParams(-1,dp(50)));setContentView(page)}
    private fun showQuestion(){val q=questions[current];checked=false;progressText.text="Pitanje ${current+1} od ${questions.size}";scoreText.text="Tačno: $score";progressBar.progress=current+1;questionText.text=q.text;options.removeAllViews();q.options.forEachIndexed{i,s->options.addView(RadioButton(this).apply{id=1000+i;text=s;textSize=16f;setTextColor(text);buttonTintList=ColorStateList.valueOf(primary);setPadding(dp(10),dp(10),dp(10),dp(10));background=round(Color.WHITE,line,14,1)},RadioGroup.LayoutParams(-1,-2).apply{setMargins(0,dp(5),0,dp(5))})};explanation.visibility=View.GONE;checkButton.isEnabled=true;nextButton.visibility=View.GONE}
    private fun inferArea(q:String):String{val s=q.lowercase();return when{listOf("gas","temperatur","toplot","entrop","adijab","izohor","izobar","kondenz","topljen").any{s.contains(it)}->"Termodinamika";listOf("fluid","pritisak","arhimed","bernul","protok","površinski","elastič","hukov").any{s.contains(it)}->"Fluidi i elastičnost";listOf("naboj","kulon","električno polje","potencijal","kondenzator","kapacitet").any{s.contains(it)}->"Elektrostatika";listOf("struj","napon","otpor","omov","kirhof","amper","voltmetar","ampermetar","provodnik").any{s.contains(it)}->"Jednosmerna struja";else->"Ostalo gradivo"}}
    private fun checkAnswer(){if(checked)return;val id=options.checkedRadioButtonId;if(id==-1){Toast.makeText(this,"Izaberi jedan odgovor.",Toast.LENGTH_SHORT).show();return};checked=true;val q=questions[current];val chosen=id-1000;val ok=chosen==q.correct;if(ok)score++ else{val a=inferArea(q.text);errorsByArea[a]=(errorsByArea[a]?:0)+1};explanation.text=if(ok)"✓ Tačno!\n${q.explanation}" else "✗ Netačno. Tačan odgovor: ${q.options[q.correct]}\n${q.explanation}";explanation.background=round(if(ok)Color.parseColor("#E7F7EF") else Color.parseColor("#FFF7E6"),if(ok)Color.parseColor("#69BE91") else Color.parseColor("#E9BC59"),14,1);explanation.visibility=View.VISIBLE;for(i in 0 until options.childCount)options.getChildAt(i).isEnabled=false;checkButton.isEnabled=false;nextButton.text=if(current==questions.lastIndex)"Rezultat" else "Sledeće pitanje";nextButton.visibility=View.VISIBLE}
    private fun nextQuestion(){if(!checked)return;current++;if(current>=questions.size)showResults() else showQuestion()}
    private fun showResults(){stopTimer();val pct=if(questions.isEmpty())0 else Math.round(score*100f/questions.size);val p=getSharedPreferences(prefsName,MODE_PRIVATE);val a=p.getInt("attempts",0)+1;val best=maxOf(p.getInt("best",0),pct);val sum=p.getInt("sum",0)+pct;val weak=if(errorsByArea.isEmpty())"Nema izdvojene slabe oblasti u poslednjem testu." else errorsByArea.entries.sortedByDescending{it.value}.take(3).joinToString("\n"){"• ${it.key}: ${it.value} grešaka"};p.edit().putInt("attempts",a).putInt("best",best).putInt("sum",sum).putInt("last",pct).putString("weak",weak).apply();showInfo("Rezultat","$pct%\n\n$score / ${questions.size} tačnih odgovora\n\nVreme izrade: ${format(elapsed)}\n\nZa dodatno ponavljanje:\n$weak")}
    private fun stopTimer(){if(running)elapsed=SystemClock.elapsedRealtime()-startTime;running=false;handler.removeCallbacks(tick)}
    private fun format(ms:Long):String{val s=ms/1000;return String.format("%02d:%02d",s/60,s%60)}
    override fun onDestroy(){handler.removeCallbacks(tick);super.onDestroy()}
}
