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

    private val vertices: ArrayList<Float>

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.388f, 0.247f, 0.0f, 1.0f)
    val color2 = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    // ----- Terrain Settings ----- //
    private var heightFactor: Float = 0.7f

    init {
        vertices = ArrayList<Float>(rez * rez * 18)
        val terrain = Array(rez) { Array(rez) { 0.0f } }

//        heightFactor = width.toFloat()/height.toFloat()

//        // Basic Terrain
//        for (i in terrain.indices) {
//            for (j in terrain[i].indices) {
//                terrain[i][j] = 0.001f * i * j // or any function of i and j
//            }
//        }


        val seedRez = 10 // Number of seed positions
        val seedTerrain = Array(seedRez) { Array(seedRez) { (Random.nextFloat() * heightFactor) + 0.1f } }

        // Bilinear Interpolate and smooth the terrain
        for (i in 0 until rez -1) {
            for (j in 0 until rez -1) {
                val seedI = i * (seedRez - 1) / (rez - 1)
                val seedJ = j * (seedRez - 1) / (rez - 1)
                val di = i * (seedRez - 1f) / (rez - 1) - seedI
                val dj = j * (seedRez - 1f) / (rez - 1) - seedJ
                terrain[i][j] = (1 - di) * ((1 - dj) * seedTerrain[seedI][seedJ] + dj * seedTerrain[seedI][seedJ + 1]) +
                        di * ((1 - dj) * seedTerrain[seedI + 1][seedJ] + dj * seedTerrain[seedI + 1][seedJ + 1])
            }
        }

        // Smooth the vertex heights
        for (i in 0 until rez) {
            for (j in 0 until rez) {
                val neighbors = mutableListOf<Float>()

                if (i > 0) neighbors.add(terrain[i-1][j])
                if (i < rez - 1) neighbors.add(terrain[i+1][j])
                if (j > 0) neighbors.add(terrain[i][j-1])
                if (j < rez - 1) neighbors.add(terrain[i][j+1])

                terrain[i][j] = neighbors.sum() / neighbors.size
            }
        }

        for (i in 0 until rez - 1)
        {
            for (j in 0 until rez - 1)
            {
                // First Triangle
                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
                vertices.add(terrain[i][j]) // v.y
                vertices.add(-height / 2.0f + height * j/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(terrain[i+1][j]) // v.y
                vertices.add(-height / 2.0f + height * j/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
                vertices.add(terrain[i][j+1]) // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ rez.toFloat()) // v.z

                // Second Triangle
                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(terrain[i+1][j]) // v.y
                vertices.add(-height / 2.0f + height * (j)/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
                vertices.add(terrain[i][j+1]) // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(terrain[i+1][j+1]) // v.y
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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertices.size/3)

        mColorHandle =
            GLES20.glGetUniformLocation(shader.getID(), "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color2, 0)
            }

        shader.setInt("isLines",1)
        // Draw the Outlines
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP,0,vertices.size/3)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

}