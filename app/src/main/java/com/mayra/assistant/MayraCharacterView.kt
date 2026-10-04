package com.mayra.assistant

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class MayraCharacterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private val cyan = Color.rgb(20, 210, 255)
    private val blue = Color.rgb(60, 75, 255)
    private val purple = Color.rgb(170, 65, 255)
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(c: Canvas) {
        val cx=width/2f; val cy=height*.46f; val r=minOf(width,height)*.34f
        p.style=Paint.Style.STROKE; p.strokeWidth=5f
        p.shader=SweepGradient(cx,cy,intArrayOf(cyan,blue,purple,cyan),null)
        c.drawCircle(cx,cy,r,p); p.shader=null; p.strokeWidth=2f; c.drawCircle(cx,cy,r+10,p)
        p.style=Paint.Style.FILL; p.color=Color.rgb(35,17,35)
        val hair=Path(); hair.moveTo(cx-r*.55f,cy+r*.65f)
        hair.cubicTo(cx-r*.95f,cy-r*.15f,cx-r*.65f,cy-r*.95f,cx,cy-r*.88f)
        hair.cubicTo(cx+r*.72f,cy-r*.9f,cx+r*.95f,cy-r*.1f,cx+r*.58f,cy+r*.68f)
        hair.cubicTo(cx+r*.3f,cy+r*.42f,cx-r*.3f,cy+r*.42f,cx-r*.55f,cy+r*.65f); hair.close(); c.drawPath(hair,p)
        p.color=Color.rgb(248,205,184); c.drawOval(RectF(cx-r*.42f,cy-r*.62f,cx+r*.42f,cy+r*.34f),p)
        p.color=Color.rgb(25,28,45); c.drawOval(RectF(cx-r*.24f,cy-r*.2f,cx-r*.05f,cy-r*.02f),p); c.drawOval(RectF(cx+r*.05f,cy-r*.2f,cx+r*.24f,cy-r*.02f),p)
        p.color=Color.WHITE; c.drawCircle(cx-r*.145f,cy-r*.13f,3f,p); c.drawCircle(cx+r*.145f,cy-r*.13f,3f,p)
        p.style=Paint.Style.STROKE; p.strokeWidth=3f; p.color=Color.rgb(160,55,85); c.drawArc(RectF(cx-r*.18f,cy+r*.02f,cx+r*.18f,cy+r*.25f),15f,150f,false,p)
        p.style=Paint.Style.FILL; p.color=Color.rgb(18,45,78)
        val body=Path(); body.moveTo(cx-r*.48f,cy+r*.28f); body.lineTo(cx-r*.78f,cy+r*.92f); body.lineTo(cx+r*.78f,cy+r*.92f); body.lineTo(cx+r*.48f,cy+r*.28f); body.close(); c.drawPath(body,p)
        p.style=Paint.Style.STROKE; p.strokeWidth=4f; p.color=cyan
        c.drawLine(cx,cy+r*.35f,cx-r*.2f,cy+r*.78f,p); c.drawLine(cx,cy+r*.35f,cx+r*.2f,cy+r*.78f,p)
    }
}