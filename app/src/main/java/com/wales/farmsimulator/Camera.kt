package com.wales.farmsimulator

import android.opengl.Matrix

class Camera {
    private var position = FloatArray(3) {0f}
    private var lookAt = FloatArray(3) {0f}
    //private var up = FloatArray(3) {0f,1f,0f}
    private var rotation = FloatArray(3) {0f}

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

    fun rotate(dx: Float, dy: Float, dz: Float){
        rotation[0] += dx
        rotation[1] += dy
        rotation[2] += dz
    }

    fun lookAt(x: Float, y: Float, z: Float) {
        lookAt[0] = x
        lookAt[1] = y
        lookAt[2] = z
    }

    fun getViewMatrix(): FloatArray{
        val matrix = FloatArray(16)
        Matrix.setLookAtM(matrix, 0, position[0], position[1], position[2], lookAt[0], lookAt[1], lookAt[2], 0f, 1f, 0f)
        return matrix
    }
}