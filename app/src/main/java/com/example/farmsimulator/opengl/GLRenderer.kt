package com.example.farmsimulator.opengl

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix

class MyGLRenderer : GLSurfaceView.Renderer
{
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val model = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private lateinit var triangle: Triangle

    private lateinit var square : Square

    private lateinit var plane : Plane

    private lateinit var shader : Shader

    // ----- CAMERA SETTING ----- //
    private var camera: Camera = Camera()

    private val nearClip: Float = 3f
    private val farClip: Float = 100f

    private val maxZoomDistance = 20f
    private val minZoomDistance = 3f

    private var moveSpeed: Float = 2.0f
    private var sensitivity: Float = 100f


    private var width: Float = 0f
    private var height: Float = 0f

    private val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "varying float yPosition;"+
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "yPosition = vPosition.y * 3.0;"+
                "}"


    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "varying float yPosition;"+
                "void main() {" +
                "  gl_FragColor = vColor * yPosition;" +
                "}"

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig)
    {
//        GLES20.glEnable(GLES20.GL_CULL_FACE) // Enable culling
//        GLES20.glCullFace(GLES20.GL_BACK) // Specify the type of faces to cull

        GLES20.glEnable(GLES20.GL_DEPTH_TEST) // Enable depth testing

        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        shader = Shader(vertexShaderCode,fragmentShaderCode)

        triangle = Triangle(floatArrayOf(-0.5f,0f,-0.5f), floatArrayOf(0.5f,0f,-0.5f), floatArrayOf(0f,0f,0.5f))

        square = Square(floatArrayOf(0f,0f,0f),1f)

        plane = Plane()

        camera.position = floatArrayOf(0f, 0.5f, 0f)
        camera.radius = 5f
        camera.pitch = 90f

    }

    override fun onDrawFrame(unused: GL10)
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        shader.use()

        // Camera
        val viewMatrix = camera.getViewMatrix()
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        Matrix.setIdentityM(model,0)
        Matrix.translateM(model, 0, triangle.position[0], triangle.position[1],triangle.position[2])

        Matrix.multiplyMM(mvpMatrix, 0, vPMatrix, 0, model, 0)

        shader.setMat4("uMVPMatrix",mvpMatrix)
        //triangle.draw(shader)

        plane.draw(shader)

    }
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        this.width = width.toFloat()
        this.height = height.toFloat()

        Matrix.frustumM(projectionMatrix, 0, -1f, 1f, -1f, 1f, nearClip, farClip)
    }

//    fun onSingleTap()
//    {
//
//    }

    fun moveCamera(dx : Float, dz : Float)
    {
        camera.move((moveSpeed * -dx)/width, 0f, (moveSpeed * -dz)/height)
    }

    fun arcRotateCamera(currentX: Float, currentY: Float){
        // Calculate the difference between current and previous touch positions
        //val deltaX = -currentX
        val deltaY = -currentY

        //val angleX = camera.yaw+deltaX * sensitivity/width
        val angleY = camera.pitch+deltaY * sensitivity/height

        //camera.yaw = angleX
        if(angleY < 90 && angleY > 15)
            camera.pitch = angleY
    }

    fun zoomCamera(scaleFactor: Float) {
        if(camera.radius / scaleFactor < maxZoomDistance && camera.radius / scaleFactor > minZoomDistance){
            camera.radius /= scaleFactor
            moveSpeed /= scaleFactor
            sensitivity /= scaleFactor
        }
    }
}
