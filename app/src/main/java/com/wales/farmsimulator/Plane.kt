package com.wales.farmsimulator

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.random.Random

class Plane(width: Int = 10, height: Int = 10)
{

    private var vertexBuffer: FloatBuffer
    private val rez : Int = 10
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
    init {
        var vertices = ArrayList<Float>(rez * rez * 12)

        for (i in 0 until rez)
        {
            for (j in 0 until rez)
            {
                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
                vertices.add(Random.nextFloat() * 0.1f); // v.y
                vertices.add(-height / 2.0f + height * j/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(Random.nextFloat() * 0.1f); // v.y
                vertices.add(-height / 2.0f + height * j/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * i/ rez.toFloat()) // v.x
                vertices.add(Random.nextFloat() * 0.1f); // v.y
                vertices.add(-height / 2.0f + height * (j+1)/ rez.toFloat()) // v.z

                vertices.add(-width / 2.0f + width * (i+1)/ rez.toFloat()) // v.x
                vertices.add(Random.nextFloat() * 0.1f); // v.y
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
                3*4,
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
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,rez * rez * 4)
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

}