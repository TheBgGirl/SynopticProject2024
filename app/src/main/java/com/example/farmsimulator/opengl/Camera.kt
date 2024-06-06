package com.example.farmsimulator.opengl

import android.opengl.Matrix
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class Camera {
    var position = floatArrayOf(0f, 0f, 3f)

    private var target = FloatArray(3) {0f}
    private var up = floatArrayOf(0f,1f,0f)

    var pitch : Float = 0f
        set(value) {
            field = value
            if(field > 89f)
                field = 89f
            if(field < -89f)
                field = -89f
        }


    private val yaw : Float = 0f

    var radius : Float = 1f

    private fun calculateTarget()
    {
        target[0] = -sin(radians(yaw)) * cos(radians(pitch)) * radius
        target[1] = sin(radians(pitch)) * radius
        target[2] = -cos(radians(yaw)) * cos(radians(pitch)) * radius
    }

    private fun radians(value : Float):Float
    {
        return (value/180)*PI.toFloat()
    }

    fun getViewMatrix(): FloatArray
    {
        calculateTarget()
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(
            viewMatrix, 0,
            position[0]+target[0], position[1]+target[1], position[2]+target[2],
            position[0], position[1], position[2], up[0], up[1], up[2])
        return viewMatrix
    }

    fun move(dx: Float, dy: Float, dz: Float){
        position[0] += (dx * cos(radians(yaw)) + dz * sin(radians(yaw)))
        position[1] += dy
        position[2] += (dz * cos(radians(yaw)) - dx * sin(radians(yaw)))
    }
}
