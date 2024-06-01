package com.wales.farmsimulator

import Triangle
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
    private val MVPMatrix = FloatArray(16)

    private var m = 0

    private lateinit var triangle: Triangle

    private lateinit var shader : Shader

    // ----- CAMERA SETTING ----- //
    private var camera: Camera = Camera()

    private val nearClip: Float = 1f
    private val farClip: Float = 15f

    private var previousX: Float = 0f
    private var previousY: Float = 0f

    private var moveSpeed: Float = 2.0f
    private var sensitivity: Float = 100f


    private var width: Float = 0f
    private var height: Float = 0f

    private val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"


    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    init {
        // Set Camera to 0,0,0 and radius 5
        camera.setPosition(0f, 0.5f, 0f)
        camera.setRadius(5f)
        camera.setPitch(90f)
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig)
    {
        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        shader = Shader(vertexShaderCode,fragmentShaderCode)

        triangle = Triangle(floatArrayOf(-0.5f,0f,-0.5f), floatArrayOf(0.5f,0f,-0.5f), floatArrayOf(0f,0f,0.5f))
    }

    override fun onDrawFrame(unused: GL10)
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        shader.use()

        // Camera
        val viewMatrix = camera.getViewMatrix()
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)


        Matrix.setIdentityM(model,0)
        Matrix.translateM(model, 0, triangle.position[0], triangle.position[1],triangle.position[2])

        Matrix.multiplyMM(MVPMatrix, 0, vPMatrix, 0, model, 0)

        shader.setMat4("uMVPMatrix",MVPMatrix)
        triangle.draw(shader)

    }
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        //val ratio : Float = width.toFloat() / height.toFloat()
//        choose = when(width.toFloat() > height.toFloat())
//        {true->width.toFloat()false->height.toFloat()}

        this.width = width.toFloat()
        this.height = height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
//        Matrix.orthoM(projectionMatrix, 0, -width.toFloat(), width.toFloat(),
//            -height.toFloat(), height.toFloat(), 3f, 7f)
        Matrix.frustumM(projectionMatrix, 0, -1f, 1f, -1f, 1f, nearClip, farClip)
    }

    fun onSingleTap()
    {
        m++
    }

    fun moveCamera(dx : Float, dz : Float)
    {
        camera.move((moveSpeed * -dx)/width, 0f, (moveSpeed * -dz)/height)
    }

    fun arcRotateCamera(currentX: Float, currentY: Float){
        // Calculate the difference between current and previous touch positions
        //val deltaX = -currentX - previousX
        val deltaY = -currentY - previousY

        //val angleX = deltaX * sensitivity
        val angleY = camera.getPitch()+deltaY * sensitivity/height

        if(angleY < 90 && angleY > 15)
            camera.setPitch(angleY)
    }
}