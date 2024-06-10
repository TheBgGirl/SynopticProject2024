package com.example.farmsimulator.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

private const val COORDS_PER_VERTEX = 3

class Square(private val position : FloatArray, private val width : Float,private  val heights : FloatArray)
{
    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f)
    private var squareCoords = FloatArray(12)

    private val vertexCount: Int = squareCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private var vertexBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    private val drawOrder = shortArrayOf(1, 2, 3, 1, 2, 0) // order to draw vertices

    init {
        //Bottom right
        squareCoords[0]  = position[0] - width/2
        squareCoords[1]  = heights[0] + 0.01f
        squareCoords[2]  = position[1] - width/2

        //Bottom left
        squareCoords[3]  = position[0] + width/2
        squareCoords[4]  = heights[1] + 0.01f
        squareCoords[5]  = position[1] -width/2

        //Top right
        squareCoords[6]  = position[0] -width/2
        squareCoords[7]  = heights[2] + 0.01f
        squareCoords[8]  = position[1] + width/2

        //Top left
        squareCoords[9]  = position[0] + width/2
        squareCoords[10]  = heights[3] + 0.01f
        squareCoords[11]  = position[1] + width/2

        vertexBuffer =
                // (number of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(squareCoords.size * 4).run {
                // use the device hardware's native byte order
                order(ByteOrder.nativeOrder())

                // create a floating point buffer from the ByteBuffer
                asFloatBuffer().apply {
                    // add the coordinates to the FloatBuffer
                    put(squareCoords)
                    // set the buffer to read the first coordinate
                    position(0)
                }
            }

        // initialize byte buffer for the draw list
        drawListBuffer =
            // (# of coordinate values * 2 bytes per short)
            ByteBuffer.allocateDirect(drawOrder.size * 2).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    put(drawOrder)
                    position(0)
                }
            }
    }
    fun draw(shader: Shader)
    {
        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(shader.getID(), "vPosition").also {

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
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
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}
