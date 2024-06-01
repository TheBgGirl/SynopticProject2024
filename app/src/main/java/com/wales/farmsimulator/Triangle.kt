import android.opengl.GLES20
import com.wales.farmsimulator.Shader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
//var triangleCoords = floatArrayOf(     // in counterclockwise order:
//    -0.5f, 0.0f, -0.5f,    // bottom left
//    0.5f, 0.0f, -0.5f,     // bottom right
//    0.0f, 0.0f, 0.5f       // top
//)

var triangleCoords = FloatArray(9)

class Triangle(vec1: FloatArray, vec2: FloatArray, vec3: FloatArray) {

    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)

    val position = floatArrayOf(0.0f,0.0f,0.0f)

    private var vertexBuffer: FloatBuffer

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    init {
        triangleCoords[0] = vec1[0]
        triangleCoords[1] = vec1[1]
        triangleCoords[2] = vec1[2]
        triangleCoords[3] = vec2[0]
        triangleCoords[4] = vec2[1]
        triangleCoords[5] = vec2[2]
        triangleCoords[6] = vec3[0]
        triangleCoords[7] = vec3[1]
        triangleCoords[8] = vec3[2]

        vertexBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(triangleCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }
    }

    fun draw(shader : Shader) {
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

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

}