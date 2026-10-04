package com.mayra.assistant

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout

class MayraVoiceLightOverlay(context: Context) : View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE}
    private var progress=0f; private var active=false; private var pattern=0
    private val animator=ValueAnimator.ofFloat(0f,1f).apply{duration=1700;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{progress=it.animatedValue as Float;invalidate()}}
    fun setActive(v:Boolean){active=v;visibility=if(v)VISIBLE else GONE;if(v&&!animator.isStarted)animator.start();if(!v){animator.cancel();progress=0f};invalidate()}
    fun nextPattern(){pattern=(pattern+1)%3;invalidate()}
    override fun onDraw(c:Canvas){if(!active)return;val w=width.toFloat();val h=height.toFloat();paint.strokeWidth=6f;paint.alpha=220;when(pattern%3){0->{val i=8f+7f*progress;c.drawRoundRect(i,i,w-i,h-i,30f,30f,paint)};1->{paint.alpha=55;paint.strokeWidth=22f;c.drawRoundRect(10f,10f,w-10f,h-10f,34f,34f,paint);paint.alpha=230;paint.strokeWidth=5f;c.drawArc(7f,7f,w-7f,h-7f,(progress*360f)%360f,115f,false,paint)};else->{c.drawLine(0f,7f,w,7f,paint);c.drawLine(0f,h-7f,w,h-7f,paint);c.drawLine(7f,0f,7f,h,paint);c.drawLine(w-7f,0f,w-7f,h,paint);paint.strokeWidth=7f;val x=progress*w;c.drawCircle(x,7f,6f,paint);c.drawCircle(w-x,h-7f,6f,paint)}}}
    companion object{fun attach(a:android.app.Activity):MayraVoiceLightOverlay{val g=a.findViewById<ViewGroup>(android.R.id.content);val old=g.findViewWithTag<MayraVoiceLightOverlay>("mayra_voice_light");if(old!=null)return old;val v=MayraVoiceLightOverlay(a).apply{tag="mayra_voice_light";visibility=GONE;isClickable=false;isFocusable=false};g.addView(v,FrameLayout.LayoutParams(-1,-1));return v}}
}