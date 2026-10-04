package com.mayra.assistant

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MayraInterviewAssistantActivity : AppCompatActivity() {
    private lateinit var engine: MayraInterviewAssistantEngine
    private lateinit var questionView: TextView
    private lateinit var answerInput: EditText
    private lateinit var resultView: TextView
    private var questions: List<MayraInterviewAssistantEngine.Question> = emptyList()
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = MayraInterviewAssistantEngine(this)
        showScreen()
    }

    private fun showScreen() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,28,24,28); setBackgroundColor(Color.rgb(7,10,22)) }
        root.addView(label("MAYRA • INTERVIEW ASSISTANT",24f,Color.WHITE))
        root.addView(label("Free, preparation-first interview coach",14f,Color.LTGRAY))
        val role = EditText(this).apply { hint="Target role (e.g. Data Entry / Excel)" }
        val company = EditText(this).apply { hint="Company (optional)" }
        val jd = EditText(this).apply { hint="Job Description (paste here)"; minLines=4; gravity=Gravity.TOP }
        val resume = EditText(this).apply { hint="Resume/Biodata summary (optional)"; minLines=4; gravity=Gravity.TOP }
        root.addView(role); root.addView(company); root.addView(jd); root.addView(resume)
        val start = Button(this).apply { text="START MOCK INTERVIEW" }; root.addView(start)
        questionView=label("Question will appear here.",18f,Color.WHITE); root.addView(questionView)
        answerInput=EditText(this).apply { hint="Type your answer here"; minLines=6; gravity=Gravity.TOP }; root.addView(answerInput)
        val evaluate=Button(this).apply { text="EVALUATE ANSWER" }; val next=Button(this).apply { text="NEXT QUESTION" }
        root.addView(evaluate); root.addView(next)
        resultView=label("",14f,Color.LTGRAY); root.addView(resultView)
        root.addView(label(engine.progressSummary(),13f,Color.GRAY))
        start.setOnClickListener {
            questions=engine.buildQuestionSet(MayraInterviewAssistantEngine.Profile(role.text.toString(),company.text.toString(),jd.text.toString(),resume.text.toString()))
            index=0; showQuestion()
        }
        evaluate.setOnClickListener {
            if (questions.isEmpty()) { resultView.text="আগে START MOCK INTERVIEW চাপুন।"; return@setOnClickListener }
            val q=questions[index]; val e=engine.evaluateAnswer(q.text,answerInput.text.toString()); engine.savePractice(q.text,answerInput.text.toString(),e)
            resultView.text="Score: " + e.score + "/100\n\n✓ " + e.strengths.joinToString("\n✓ ") + "\n\nImprove:\n• " + e.improvements.joinToString("\n• ") + "\n\nNext: " + e.nextStep
        }
        next.setOnClickListener { if (questions.isNotEmpty()) { index=(index+1)%questions.size; showQuestion() } }
        setContentView(ScrollView(this).apply { addView(root) })
    }
    private fun showQuestion() { val q=questions[index]; questionView.text=(index+1).toString()+"/"+questions.size+" • "+q.category+"\n\n"+q.text+"\n\nWhy: "+q.why; answerInput.setText(""); resultView.text="" }
    private fun label(t:String,s:Float,c:Int)=TextView(this).apply{text=t;textSize=s;setTextColor(c);setPadding(0,8,0,8)}
}
