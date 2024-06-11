package com.example.farmsimulator.opengl

import android.opengl.GLES20
//import smile.hash.SimHash
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.content.Context
import android.opengl.Matrix
import android.util.Log
import kotlin.random.Random

class Plane(var width: Int = 20, var height: Int = 20,context: Context)
{
    private val vPMatrix = FloatArray(16)
    private val model = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var vertexBuffer: FloatBuffer
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertices: ArrayList<Float>
    private var terrain  = emptyArray<Array<Float>>()
    private var square : Square
    private lateinit var cropSquares : ArrayList<CropSquare>

    private lateinit var theContext: Context


    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.188f, 0.247f, 0.0f, 1.0f)
    val color2 = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    // ----- Terrain Settings ----- //
    private var heightFactor: Float = 1.0f

    init {
        theContext = context

        width++
        height++
        vertices = ArrayList(width * height * 18)
        terrain = Array(width) { Array(height) { 0.0f } }
        square = Square(floatArrayOf(-100f,-100f), 1f, floatArrayOf(-1f,-1f,-1f,-1f))

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

    fun displayFarmData(){
        cropSquares = ArrayList()

        val testPosX: Int = 0
        val testPosZ: Int = 0
        val correctedX: Float = (testPosX - width / 2f) + 0.5f
        val correctedZ: Float = (testPosZ - height / 2f) + 0.5f

        for(i in 0 until width - 1){
            for(j in 0 until height - 1){
                //Log.d("Farm Data: ", i.toString() + j.toString())

                //Thread.sleep(1_000)

                // Iterate through Farm Data[i][j]
                    // Initialize texture for crop type at this position
                        // Different texture for yield %
                    // Might do: change terrain colour depending on precipitation and temperature



                //Testing: initialising all as corn
                cropSquares.add(
                    CropSquare(
                        floatArrayOf(correctedX + i, correctedZ + j),
                        0.75f,
                        floatArrayOf(terrain[i][j] + 0.5f, terrain[i][j] + 0.5f, terrain[i][j] + 0.5f, terrain[i][j] + 0.5f),
                        theContext,
                        CropType.PUMPKIN
                    )
                )
            }
        }

    }

    fun setSquare(posX : Float , posZ : Float)
    {
        val correctedX: Float = ((width - 1 - posX) - width / 2f) - 0.5f
        val correctedZ: Float = (posZ - height / 2f) + 0.5f

        // Flip X because terrain[][] has 0,0 as bottom right
        val terrainX = width - 2 - posX.toInt()

        // Get the heights of the corners
        val height1 = terrain[terrainX][posZ.toInt()]
        val height2 = if (posZ.toInt() + 1 < height) terrain[terrainX][posZ.toInt() + 1] else height1
        val height3 = if (terrainX + 1 < width) terrain[terrainX + 1][posZ.toInt()] else height1
        val height4 = if (terrainX + 1 < width && posZ.toInt() + 1 < height) terrain[terrainX + 1][posZ.toInt() + 1] else height1

        square = Square(
            floatArrayOf(correctedX, correctedZ),
            1f,
            floatArrayOf(height1, height3, height2, height4)
        )
    }

    fun draw(shader : Shader, cropShader: Shader, viewMatrix : FloatArray, projectionMatrix: FloatArray, pitch: Float)
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

        square.draw(shader) // Square for selected tile

        //Draw crop squares
        cropShader.use()
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        drawCropSquares(cropShader, pitch)
        GLES20.glDisable(GLES20.GL_BLEND)

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

    fun drawCropSquares(cropShader: Shader, pitch : Float)
    {
        var originalModel : FloatArray = model
        for (cropSquare in cropSquares) {
            Matrix.setIdentityM(model,0)
            Matrix.translateM(model, 0, originalModel, 0, cropSquare.position[0], cropSquare.heights[0], cropSquare.position[1])
            Matrix.rotateM(model, 0, pitch + 90f, 1f, 0f, 0f)
            Matrix.multiplyMM(mvpMatrix, 0, vPMatrix, 0, model, 0)
            cropShader.setMat4("uMVPMatrix",mvpMatrix)

            cropSquare.draw(cropShader)
        }
    }
}