package com.example.farmsimulator.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.random.Random

class Plane(width: Int = 20, height: Int = 20)
{

    private var vertexBuffer: FloatBuffer
    private val rez : Int = 30
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.388f, 0.247f, 0.0f, 1.0f)

    init {
        val vertices = ArrayList<Float>(rez * rez * 18)
        val terrain = Array(rez) { Array(rez) { Random.nextFloat()} }

        // Smooth the terrain
        for (i in 1 until rez -1) {
            for (j in 1 until rez -1) {
                val sum = terrain[i-1][j] + terrain[i+1][j] + terrain[i][j-1] + terrain[i][j+1]
                terrain[i][j] = sum / 4.0f
            }
        }

//        val seedRez = 5 // Number of seed positions
//        val seedTerrain = Array(seedRez) { Array(seedRez) { Random.nextFloat() } }
//
//// Interpolate and smooth the terrain
//        for (i in 0 until rez -1) {
//            for (j in 0 until rez -1) {
//                val seedI = i * (seedRez - 1) / (rez - 1)
//                val seedJ = j * (seedRez - 1) / (rez - 1)
//                val di = i * (seedRez - 1f) / (rez - 1) - seedI
//                val dj = j * (seedRez - 1f) / (rez - 1) - seedJ
//                terrain[i][j] = (1 - di) * ((1 - dj) * seedTerrain[seedI][seedJ] + dj * seedTerrain[seedI][seedJ + 1]) +
//                        di * ((1 - dj) * seedTerrain[seedI + 1][seedJ] + dj * seedTerrain[seedI + 1][seedJ + 1])
//            }
//        }

        for (i in 0 until rez - 1)
        {
            for (j in 0 until rez - 1)
            {
                // First Triangle
                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
//                vertices.add(terrain[i][j]); // v.y
                vertices.add(0.001f * i * j); // v.y
                vertices.add(-height / 2.0f + height * j/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(0.001f * i * j); // v.y
                vertices.add(-height / 2.0f + height * j/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
                vertices.add(0.001f * i * j); // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ rez.toFloat()) // v.z

                // Second Triangle
                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(0.001f * i * j); // v.y
                vertices.add(-height / 2.0f + height * (j)/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
                vertices.add(0.001f * i * j); // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(0.001f * i * j); // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ rez.toFloat()) // v.z
            }
        }

        vertexBuffer =
                // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(vertices.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(vertices.toFloatArray())
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }
    }


    fun draw(shader : Shader)
    {
        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(shader.getID(), "vPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                it,
                3,
                GLES20.GL_FLOAT,
                false,
                3 * 4,
                vertexBuffer
            )
        }

        // get handle to fragment shader's vColor member
        mColorHandle =
            GLES20.glGetUniformLocation(shader.getID(), "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

        // Draw the square
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,rez * rez * 6)
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

}