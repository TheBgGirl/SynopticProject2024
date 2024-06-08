package com.example.farmsimulator.opengl

import android.opengl.GLES20
import smile.hash.SimHash
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.roundToInt
import kotlin.random.Random

class Plane(var width: Int = 20, var height: Int = 20)
{

    private var vertexBuffer: FloatBuffer
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertices: ArrayList<Float>
    private var terrain  = emptyArray<Array<Float>>()
    private lateinit var square : Square

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.388f, 0.247f, 0.0f, 1.0f)
    val color2 = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    // ----- Terrain Settings ----- //
    private var heightFactor: Float = 1.0f

    init {
        width++
        height++
        vertices = ArrayList<Float>(width * height * 18)
        terrain = Array(width) { kotlin.Array(height) { 0.0f } }
        square = Square(floatArrayOf(0f,0f),1f, floatArrayOf(-1f,-1f,-1f,-1f))

//        // Basic Terrain
//        for (i in terrain.indices) {
//            for (j in terrain[i].indices) {
//                terrain[i][j] = 0.001f * i * j // or any function of i and j
//            }
//        }


        val seedRez = 10 // Number of seed positions
        val seedTerrain = Array(seedRez) { Array(seedRez) { (Random.nextFloat() * heightFactor) + 0.1f } }

        // Bilinear Interpolate and smooth the terrain
        for (i in 0 until width -1) {
            for (j in 0 until height -1) {
                val seedI = i * (seedRez - 1) / (width - 1)
                val seedJ = j * (seedRez - 1) / (height - 1)
                val di = i * (seedRez - 1f) / (width - 1) - seedI
                val dj = j * (seedRez - 1f) / (height - 1) - seedJ
                terrain[i][j] = (1 - di) * ((1 - dj) * seedTerrain[seedI][seedJ] + dj * seedTerrain[seedI][seedJ + 1]) +
                        di * ((1 - dj) * seedTerrain[seedI + 1][seedJ] + dj * seedTerrain[seedI + 1][seedJ + 1])
            }
        }

        // Smooth the vertex heights
        for (i in 0 until width) {
            for (j in 0 until height) {
                val neighbors = mutableListOf<Float>()

                if (i > 0) neighbors.add(terrain[i-1][j])
                if (i < width - 1) neighbors.add(terrain[i+1][j])
                if (j > 0) neighbors.add(terrain[i][j-1])
                if (j < height - 1) neighbors.add(terrain[i][j+1])

                terrain[i][j] = neighbors.sum() / neighbors.size
            }
        }

        for (i in 0 until width - 1)
        {
            for (j in 0 until height - 1)
            {
                // First Triangle
                vertices.add(-width / 2.0f + width * i/ width.toFloat()) // v.x
                vertices.add(terrain[i][j]) // v.y
                vertices.add(-height / 2.0f + height * j/ height.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * i/ width.toFloat()) // v.x
                vertices.add(terrain[i][j+1]) // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ height.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ width.toFloat()) // v.x
                vertices.add(terrain[i+1][j]) // v.y
                vertices.add(-height / 2.0f + height * j/ height.toFloat()) // v.z

                // Second Triangle
                vertices.add(-width / 2.0f + width * (i+1)/ width.toFloat()) // v.x
                vertices.add(terrain[i+1][j]) // v.y
                vertices.add(-height / 2.0f + height * (j)/ height.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * i/ width.toFloat()) // v.x
                vertices.add(terrain[i][j+1]) // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ height.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ width.toFloat()) // v.x
                vertices.add(terrain[i+1][j+1]) // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ height.toFloat()) // v.z
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

    fun setSquare(posX : Float , posZ : Float)
    {
        // Using posX and posZ, find height values from terrain[][]
        // Pass position and heights of each vertex
        val correctedX: Float = (width - 4.5f - posX)
        val correctedZ: Float = ((posZ) - height/2) + 0.5f

        var height1: Float = terrain[posX.toInt()][posZ.toInt()]
        var height2: Float = terrain[posX.toInt()][posZ.toInt() + 1]
        var height3: Float = terrain[posX.toInt() + 1][posZ.toInt()]
        var height4: Float = terrain[posX.toInt() + 1][posZ .toInt() + 1]


        square = Square(floatArrayOf(correctedX,correctedZ),1f, floatArrayOf(height1, height3, height2, height4))
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
        drawTerrain(shader)
        drawLines(shader)
        drawSquares(shader)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    fun drawTerrain(shader: Shader)
    {
        // get handle to fragment shader's vColor member
        mColorHandle =
            GLES20.glGetUniformLocation(shader.getID(), "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

        // Draw the square
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertices.size/3)
    }

    fun drawLines(shader : Shader)
    {
        mColorHandle =
            GLES20.glGetUniformLocation(shader.getID(), "vColor").also { colorHandle ->

                // Set color for drawing the triangle
                GLES20.glUniform4fv(colorHandle, 1, color2, 0)
            }

        shader.setFloat("isLines",0.01f)
        // Draw the Outlines
        GLES20.glLineWidth(3.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES,0,vertices.size/3)
    }

    fun drawSquares(shader : Shader)
    {
        square.draw(shader)
    }

}