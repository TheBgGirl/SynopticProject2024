package com.wales.farmsimulator

import android.opengl.Matrix
import kotlin.math.*

class Camera {
    private var position = floatArrayOf(0f, 0f, 3f)
    private var target = FloatArray(3) {0f}
    private var up = floatArrayOf(0f,0f,1f)
    private var rotation = FloatArray(3) {0f}

    private var pitch : Float = 0f
    private val yaw : Float = 0f

    fun calculateTarget()
    {
        target[0] = -sin(radians(yaw)) * cos(radians(pitch))
        target[1] = sin(radians(pitch))
        target[2] = -cos(radians(yaw)) * cos(radians(pitch))
    }

    fun radians(value : Float):Float
    {
        return (value/180)*PI.toFloat()
    }

    fun setPitch(value : Float)
    {
        pitch = value
    }

    fun getPitch() :Float
    {
        return pitch
    }


    fun getViewMatrix(): FloatArray
    {
        calculateTarget()
        val viewMatrix = FloatArray(16)
        Matrix.setLookAtM(viewMatrix, 0, position[0]+target[0], position[1]+target[1], position[2]+target[2],
            position[0], position[1], position[2], up[0], up[1], up[2])
        return viewMatrix
    }

    fun move(dx: Float, dy: Float, dz: Float){
        position[0] += dx
        position[1] += dy
        position[2] += dz
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        position[0] = x
        position[1] = y
        position[2] = z
    }
    fun getPosition(): FloatArray{
        return position.copyOf()
    }

    fun rotate(dx: Float, dy: Float, dz: Float){
        rotation[0] += dx
        rotation[1] += dy
        rotation[2] += dz
    }

//    fun lookAt(x: Float, y: Float, z: Float) {
//        target[0] = x
//        target[1] = y
//        target[2] = z
    //}
}