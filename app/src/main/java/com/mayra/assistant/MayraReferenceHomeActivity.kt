package com.mayra.assistant

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.Locale

class MayraReferenceHomeActivity : FragmentActivity() {
    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }
    private lateinit var status: TextView
    private var tts: TextToSpeech? = null
    private val bg=Color.rgb(3,8,24)
    private val blue=Color.rgb(20,92,190)
    private val cyan=Color.rgb(15,210,255)
    private val purple=Color.rgb(105,50,190)
    private val green=Color.rgb(20,175,110)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        prefs.edit().putBoolean("owner_command_authorized",false).apply()
        MayraFeatureCheckManager(this).enforceUnavailableFeaturesOff()
        if(!MayraFeatureCheckManager.isSetupCompleted(this)){
            startActivity(Intent(this,MayraFirstRunSetupActivity::class.java)); finish(); return
        }
        verifyOwner()
    }

    private fun verifyOwner(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(26,30,26,30);setBackgroundColor(bg)}
        root.addView(MayraCharacterView(this).apply{layoutParams=LinearLayout.LayoutParams(-1,330)})
        root.addView(label("MAYRA",38f,Color.WHITE))
        root.addView(label("Your Personal AI Assistant",16f,Color.rgb(170,210,240)))
        root.addView(space(12))
        root.addView(label("Owner verification required",19f,Color.WHITE))
        root.addView(label("Password নেই। Face/Fingerprint বা secure device credential ব্যবহার হবে।",14f,Color.rgb(180,200,225)))
        val b=button("VERIFY OWNER  ›",blue); b.setOnClickListener{authenticateOwner()}; root.addView(b)
        root.addView(label("Founder & Owner: Gopal Basak",13f,Color.rgb(130,170,215)))
        setContentView(ScrollView(this).apply{setBackgroundColor(bg);addView(root)})
    }

    private fun authenticateOwner(){
        val a=BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if(BiometricManager.from(this).canAuthenticate(a)!=BiometricManager.BIOMETRIC_SUCCESS){
            toast("Face/Fingerprint বা secure device credential সেটআপ করুন।"); return
        }
        val prompt=BiometricPrompt(this,ContextCompat.getMainExecutor(this),object:BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult){
                prefs.edit().putBoolean("owner_verified",true).putBoolean("owner_command_authorized",true).apply()
                MayraFounderIdentity(object:MayraFounderIdentity.Store{
                    override fun get(key:String)=prefs.getString(key,null)
                    override fun put(key:String,value:String){prefs.edit().putString(key,value).apply()}
                }).recognizeVerifiedOwner(MayraFounderIdentity.VerificationMethod.ANDROID_BIOMETRIC)
                showHome(); speak("রাধে রাধে বস, বলুন কী সাহায্য করতে পারি")
            }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Mayra Owner Verification").setSubtitle("Face / Fingerprint অথবা secure device credential").setAllowedAuthenticators(a).build())
    }

    private fun showHome(){
        val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,18,14,24)}
        val head=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val title=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        title.addView(label("Mayra",29f,Color.WHITE)); title.addView(label("● Online  •  Owner controlled",12f,Color.rgb(50,235,140))); head.addView(title)
        val gear=button("⚙",purple); gear.layoutParams=LinearLayout.LayoutParams(62,54); gear.setOnClickListener{showSettings()}; head.addView(gear); body.addView(head)
        body.addView(space(8))
        val hero=cardBox(); val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        row.addView(MayraCharacterView(this).apply{layoutParams=LinearLayout.LayoutParams(0,235,1f)})
        val welcome=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        welcome.addView(label("রাধে রাধে বস,",23f,Color.WHITE)); welcome.addView(label("বলুন কী সাহায্য করতে পারি",16f,Color.rgb(205,220,245)))
        status=label(if(prefs.getBoolean("master_on",false))"Mayra is ON and ready." else "Mayra is paused — memory/state preserved.",12f,Color.rgb(150,190,220)); welcome.addView(status); row.addView(welcome); hero.addView(row); body.addView(hero)
        body.addView(space(8))
        val master=button("⏻  Mayra "+if(prefs.getBoolean("master_on",false))"ON" else "OFF",if(prefs.getBoolean("master_on",false))green:blue)
        master.setOnClickListener{
            val on=!prefs.getBoolean("master_on",false); prefs.edit().putBoolean("master_on",on).apply()
            if(on && prefs.getBoolean("owner_command_authorized",false) && FeatureToggleRegistry.isEnabled(prefs,FeatureToggleRegistry.VOICE_COMMAND)) MayraBackgroundVoiceServiceStarter.start(this)
            if(!on) MayraBackgroundVoiceServiceStarter.stop(this)
            master.text="⏻  Mayra "+if(on)"ON" else "OFF"; master.background=gradient(if(on)green else blue)
            status.text=if(on)"Mayra ON — voice command ready." else "Mayra OFF — memory/state preserved."
        }; body.addView(master); body.addView(space(8))
        val grid=GridLayout(this).apply{columnCount=2;useDefaultMargins=true}
        addFeature(grid,"▣","Camera","Camera access",blue){if(FeatureToggleRegistry.isEnabled(prefs,FeatureToggleRegistry.CAMERA))startActivity(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))else toast("Camera setup/permission check required.")}
        addFeature(grid,"☎","Call Assist","Approved calls",green){toast("Call Assist remains OFF until end-to-end call handling is implemented.")}
        addFeature(grid,"✦","3D Animation","Character & mood",purple){showCharacterDialog()}
        addFeature(grid,"●","WhatsApp","Send voice message",green){showWhatsApp()}
        addFeature(grid,"🎙","Voice Command","বাংলা / हिन्दी / English",purple){showVoiceCommand()}
        addFeature(grid,"☼","Light Ring","Speaking indicator",blue){val n=!prefs.getBoolean("mayra_voice_light_enabled",false);prefs.edit().putBoolean("mayra_voice_light_enabled",n).apply();status.text=if(n)"Voice Light ON." else "Voice Light OFF."}
        body.addView(grid); body.addView(space(8))
        body.addView(toggleRow("🔒","Mayra Active While Locked",prefs.getBoolean("mayra_active_while_locked",false)){v->prefs.edit().putBoolean("mayra_active_while_locked",v).apply()})
        body.addView(toggleRow("🔇","Silent Mode — Only Listen",prefs.getBoolean("mayra_silent_mode_behavior",true)){v->prefs.edit().putBoolean("mayra_silent_mode_behavior",v).apply()})
        body.addView(space(8))
        val menu=button("☰  MAIN MENU / KNOWLEDGE HUB",blue); menu.setOnClickListener{showMainMenu()}; body.addView(menu)
        val pc=button("▣  PHONE ↔ WINDOWS 10",purple); pc.setOnClickListener{showPairing()}; body.addView(pc)
        body.addView(label("No Password  •  Owner approval required for self-development changes",11f,Color.rgb(115,155,195)))
        setContentView(ScrollView(this).apply{setBackgroundColor(bg);isFillViewport=true;addView(body)})
    }

    private fun showMainMenu(){screen("Main Menu / Knowledge Hub"){b->
        val items=arrayOf(
            "👤  Biodata & Career Profile" to "Profile • CV • career growth",
            "💼  Job Watcher" to "Jobs • freelancing • OneForma • auto search",
            "📊  Excel / Data Analysis" to "Sheets • formulas • charts • reports",
            "🎓  Learning & Education" to "Study • courses • certificates",
            "📄  Documents & PDF" to "Read • write • translate • edit",
            "💰  Income & Earning" to "Freelance • remote • skills",
            "🧵  Textile & Design" to "Saree design • textile ideas",
            "🏛  Government Schemes" to "India & West Bengal",
            "📰  Market & News" to "Jobs • business • finance",
            "📍  Local Info" to "Weather • emergency • travel")
        for(x in items){val c=infoCard(x.first,x.second); c.setOnClickListener{openAssistant()}; b.addView(c)}
    }}

    private fun showVoiceCommand(){screen("Voice Command"){b->
        val c=cardBox(); c.addView(label("🎙  আপনার কমান্ড শুনছি…",22f,Color.WHITE)); c.addView(label("বাংলা • हिन्दी • English",14f,Color.rgb(130,200,235)))
        val n=label("5",46f,cyan); n.gravity=Gravity.CENTER; c.addView(n,LinearLayout.LayoutParams(-1,90))
        c.addView(label("✓ Command received\n→ Response in 5 seconds\n→ Working…",14f,Color.LTGRAY))
        val start=button("START VOICE ASSISTANT",blue); start.setOnClickListener{openAssistant()}; c.addView(start); b.addView(c)
    }}

    private fun showWhatsApp(){screen("WhatsApp Voice Message"){b->
        val c=cardBox(); c.addView(label("🟢 WhatsApp Voice Message",20f,Color.WHITE))
        val input=EditText(this).apply{hint="আপনি যা বলতে চান লিখুন…";minLines=4;setTextColor(Color.WHITE);setHintTextColor(Color.LTGRAY)}; c.addView(input)
        val send=button("PREPARE VOICE MESSAGE",green); send.setOnClickListener{
            val t=input.text.toString().trim(); if(t.isBlank()){toast("বার্তাটি লিখুন।");return@setOnClickListener}
            MayraWhatsAppVoiceMessage.createAndShare(this,t){m->runOnUiThread{toast(m)}}
        }; c.addView(send); b.addView(c)
    }}

    private fun showCharacterDialog(){
        val names=MayraCharacterSystem.all().map{it.name}.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Mayra 3D Character / Mood")
            .setSingleChoiceItems(names,names.indexOf(MayraCharacterSystem.current(prefs).name)){d,w->MayraCharacterSystem.select(prefs,MayraCharacterSystem.all()[w].id);d.dismiss();status.text="Character selected: "+names[w]}.show()
    }

    private fun showSettings(){screen("Settings"){b->
        b.addView(infoCard("👤  Gopal Basak","Owner • Verified"))
        b.addView(infoCard("🎙  Voice & Language","বাংলা / हिन्दी / English"))
        b.addView(infoCard("🔐  Security & Privacy","Owner-only commands • Password disabled"))
        val repair=infoCard("🛠  Self-Repair & Update","Suggest → Diagnose → Test → Ask Approval → Apply"); repair.setOnClickListener{showSelfRepair()}; b.addView(repair)
        b.addView(infoCard("ℹ  App Information","Mayra • Android reference UI"))
    }}

    private fun showSelfRepair(){screen("Self-Repair & Update"){b->
        b.addView(infoCard("✓  System Health","All available local modules can be checked"))
        arrayOf("1. Diagnose","2. Create Fix Proposal","3. Test & Validate","4. Ask Your Approval","5. Apply Update").forEach{b.addView(infoCard("→  "+it,"Owner-controlled workflow"))}
        val check=button("CHECK FOR ISSUES",blue); check.setOnClickListener{
            val r=MayraSelfDevelopmentCommand.request(prefs,"UI/system health improvement review"); toast(r.response)
        }; b.addView(check)
    }}

    private fun showPairing(){screen("Phone ↔ Windows 10"){b->
        b.addView(infoCard("🔗  One-time pairing","Trusted Wi-Fi / hotspot • Owner approval"))
        val host=EditText(this).apply{hint="Windows IP address";setTextColor(Color.WHITE);setHintTextColor(Color.LTGRAY)}
        val port=EditText(this).apply{hint="Port";setText("8765");setTextColor(Color.WHITE);setHintTextColor(Color.LTGRAY)}
        val code=EditText(this).apply{hint="6-digit pairing code";inputType=2;setTextColor(Color.WHITE);setHintTextColor(Color.LTGRAY)}
        b.addView(host);b.addView(port);b.addView(code)
        val pair=button("SEND PAIR REQUEST",purple); pair.setOnClickListener{
            val h=host.text.toString().trim(); val p=port.text.toString().toIntOrNull()?:8765; val c=code.text.toString().trim()
            if(h.isBlank()||c.length!=6){toast("Windows IP ও 6-digit pairing code দিন।");return@setOnClickListener}
            Thread{val r=LocalDeviceLinkCoordinator().requestPair(LocalDeviceLinkCoordinator.Endpoint(h,p),c);runOnUiThread{toast(if(r.ok)"Pair request sent ✓" else "Pair failed: "+r.error)}}.start()
        }; b.addView(pair)
    }}

    private fun screen(titleText:String,fill:(LinearLayout)->Unit){
        val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,18,14,28)}
        val head=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val back=button("‹",purple); back.layoutParams=LinearLayout.LayoutParams(58,54); back.setOnClickListener{showHome()}
        head.addView(back); head.addView(label(titleText,24f,Color.WHITE)); body.addView(head); body.addView(space(10)); fill(body)
        setContentView(ScrollView(this).apply{setBackgroundColor(bg);isFillViewport=true;addView(body)})
    }

    private fun addFeature(g:GridLayout,icon:String,title:String,sub:String,color:Int,action:()->Unit){
        val v=button(icon+"\n"+title+"\n"+sub,color); v.setOnClickListener{action()}
        val lp=GridLayout.LayoutParams(); lp.width=0; lp.height=118; lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); lp.setMargins(5,5,5,5); v.layoutParams=lp; g.addView(v)
    }
    private fun toggleRow(icon:String,textValue:String,checked:Boolean,change:(Boolean)->Unit):View{
        val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(12,8,8,8);background=gradient(blue)}
        row.addView(label(icon+"  "+textValue,14f,Color.WHITE),LinearLayout.LayoutParams(0,-2,1f))
        row.addView(Switch(this).apply{isChecked=checked;setOnCheckedChangeListener{_,v->change(v)}}); return row
    }
    private fun infoCard(title:String,sub:String)=cardBox().apply{addView(label(title,16f,Color.WHITE));addView(label(sub,12f,Color.rgb(145,185,220)))}
    private fun cardBox()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,12,14,12);background=gradient(blue);elevation=8f}
    private fun button(t:String,color:Int)=Button(this).apply{text=t;textSize=14f;setTextColor(Color.WHITE);isAllCaps=false;background=gradient(color);setPadding(10,10,10,10)}
    private fun gradient(color:Int)=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(Color.rgb(8,20,50),color,Color.rgb(18,10,55))).apply{cornerRadius=22f;setStroke(2,Color.rgb(20,175,255))}
    private fun label(t:String,s:Float,c:Int)=TextView(this).apply{text=t;textSize=s;setTextColor(c);setPadding(0,5,0,5)}
    private fun space(h:Int)=Space(this).apply{layoutParams=LinearLayout.LayoutParams(1,h)}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun openAssistant(){
        if(!prefs.getBoolean("master_on",false)){toast("আগে Mayra ON করুন।");return}
        if(!prefs.getBoolean("owner_command_authorized",false)){toast("Owner verification required.");return}
        startActivity(Intent(this,MainActivity::class.java))
    }
    private fun speak(text:String){
        tts?.shutdown(); tts=TextToSpeech(this){r->if(r==TextToSpeech.SUCCESS){tts?.language=Locale("bn","IN");tts?.speak(text,TextToSpeech.QUEUE_FLUSH,null,"mayra_welcome")}}
    }
    override fun onDestroy(){tts?.shutdown();super.onDestroy()}
}