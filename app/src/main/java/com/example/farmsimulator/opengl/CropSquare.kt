package com.example.farmsimulator.opengl

import android.content.Context
import android.opengl.GLES20
import com.example.farmsimulator.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

private const val COORDS_PER_VERTEX = 3

class CropSquare(
    private val position : FloatArray,
    private val width : Float,
    private  val heights : FloatArray,
    context: Context,
    cropType: CropType
)
{
    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    private var squareCoords = FloatArray(12)

    private val vertexCount: Int = squareCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private var vertexBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    private val drawOrder = shortArrayOf(1, 2, 3, 1, 2, 0) // order to draw vertices

    private val dataCoordinates: ArrayList<Float>
    private var textureCoordinates : FloatBuffer
    /** This will be used to pass in the texture.  */
    var mTextureUniformHandle: Int = 0

    /** This will be used to pass in model texture coordinate information.  */
    var mTextureCoordinateHandle: Int = 0

    /** Size of the texture coordinate data in elements.  */
    val mTextureCoordinateDataSize = 2

    /** This is a handle to our texture data.  */
    var mTextureDataHandle: Int = 0

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

        dataCoordinates = arrayListOf(
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f
        )

        // Load the texture
        mTextureDataHandle = when (cropType) {
            CropType.PUMPKIN -> TextureHandler.loadTexture(context, R.drawable.pumpkin)

            //TODO: CHANGE 2 BELOW TO RESPECTIVE TEXTURES
            CropType.LEAFYGREEN -> TextureHandler.loadTexture(context, R.drawable.corn)
            CropType.RICE -> TextureHandler.loadTexture(context, R.drawable.corn)
        }
        //mTextureDataHandle = TextureHandler.loadTexture(context, R.drawable.corn)

        textureCoordinates = ByteBuffer.allocateDirect(dataCoordinates.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureCoordinates.put(dataCoordinates.toFloatArray()).position(0)
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

        mTextureUniformHandle = GLES20.glGetUniformLocation(shader.getID(), "u_Texture")
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(shader.getID(), "a_TexCoordinate")

        //mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(
            mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
            0, textureCoordinates
        )

        //Enable
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0)

        // Draw the square
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT, drawListBuffer
        )
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}