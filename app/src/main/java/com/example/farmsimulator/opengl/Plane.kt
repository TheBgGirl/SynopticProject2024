package com.example.farmsimulator.opengl

import android.opengl.GLES20
import com.example.farmsimulator.R
//import smile.hash.SimHash
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.content.Context
import kotlin.random.Random

class Plane(var width: Int = 20, var height: Int = 20,context: Context)
{

    private var vertexBuffer: FloatBuffer
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertices: ArrayList<Float>
    private val textureCoordinates: ArrayList<Float>
    private var TextureCoordinates : FloatBuffer
    private var terrain  = emptyArray<Array<Float>>()
    private var square : Square

    /** This will be used to pass in the texture.  */
    var mTextureUniformHandle: Int = 0


    /** This will be used to pass in model texture coordinate information.  */
    var mTextureCoordinateHandle: Int = 0


    /** Size of the texture coordinate data in elements.  */
    val mTextureCoordinateDataSize = 2


    /** This is a handle to our texture data.  */
    var mTextureDataHandle: Int = 0


    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(0.388f, 0.247f, 0.0f, 1.0f)
    val color2 = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    // ----- Terrain Settings ----- //
    private var heightFactor: Float = 1.0f

    init {
        width++
        height++
        vertices = ArrayList(width * height * 18)
        textureCoordinates = ArrayList(10)
        terrain = Array(width) { Array(height) { 0.0f } }
        square = Square(floatArrayOf(0f,0f),1f, floatArrayOf(-1f,-1f,-1f,-1f))

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

        // Load the texture
        mTextureDataHandle = TextureHandler.loadTexture(context, R.drawable.hero)

        TextureCoordinates = ByteBuffer.allocateDirect(textureCoordinates.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        TextureCoordinates.put(textureCoordinates.toFloatArray()).position(0)
    }

    fun setSquare(posX : Float , posZ : Float)
    {
        val correctedX: Float = ((width - 1 - posX) - width / 2f) - 0.5f
        val correctedZ: Float = (posZ - height / 2f) + 0.5f

        val correctedHeightX: Int = (width - 1 - posX).toInt()
        val correctedHeightZ: Int = posZ.toInt()

        val height1: Float = terrain[correctedHeightX][correctedHeightZ]
        val height2: Float = if (correctedHeightZ + 1 < height) terrain[correctedHeightX][correctedHeightZ + 1] else height1
        val height3: Float = if (correctedHeightX + 1 < width) terrain[correctedHeightX + 1][correctedHeightZ] else height1
        val height4: Float = if (correctedHeightX + 1 < width && correctedHeightZ + 1 < height) terrain[correctedHeightX + 1][correctedHeightZ + 1] else height1


        square = Square(floatArrayOf(correctedX,correctedZ),1f, floatArrayOf(height2, height1, height4, height3))
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
        mTextureUniformHandle = GLES20.glGetUniformLocation(shader.getID(), "u_Texture")
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(shader.getID(), "a_TexCoordinate")

        //mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
            0, TextureCoordinates)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0)
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