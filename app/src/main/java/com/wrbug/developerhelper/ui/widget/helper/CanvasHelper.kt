package com.wrbug.developerhelper.ui.widget.helper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

object CanvasHelper {

    /**
     * 画箭头
     * @param sx
     * @param sy
     * @param ex
     * @param ey
     */
    fun drawAL(sx: Float, ex: Float, sy: Float, ey: Float, canvas: Canvas?, paint: Paint) {
        canvas?.run {
            val H = 8 // 箭头高度
            val L = 3.5 // 底边的一半
            val awrad = Math.atan(L / H) // 箭头角度
            val arrowLen = Math.sqrt(L * L + H * H) // 箭头的长度
            val arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arrowLen)
            val arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arrowLen)
            val x3 = ex - arrXY_1[0] // (x3,y3)是第一端点
            val y3 = ey - arrXY_1[1]
            val x4 = ex - arrXY_2[0] // (x4,y4)是第二端点
            val y4 = ey - arrXY_2[1]
            // 画线
            drawLine(sx, sy, ex, ey, paint)
            val triangle = Path()
            triangle.moveTo(ex, ey)
            triangle.lineTo(x3, y3)
            triangle.lineTo(x4, y4)
            triangle.close()
            drawPath(triangle, paint)
        }


    }

    fun drawAL(sx: Int, ex: Int, sy: Int, ey: Int, canvas: Canvas?, paint: Paint) {
        drawAL(sx.toFloat(), ex.toFloat(), sy.toFloat(), ey.toFloat(), canvas, paint)
    }

    // 计算
    private fun rotateVec(px: Float, py: Float, ang: Double, isChLen: Boolean, newLen: Double): FloatArray {
        val mathstr = FloatArray(2)
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        var vx = px * Math.cos(ang) - py * Math.sin(ang)
        var vy = px * Math.sin(ang) + py * Math.cos(ang)
        if (isChLen) {
            val d = Math.sqrt(vx * vx + vy * vy)
            vx = vx / d * newLen
            vy = vy / d * newLen
            mathstr[0] = vx.toFloat()
            mathstr[1] = vy.toFloat()
        }
        return mathstr
    }
}