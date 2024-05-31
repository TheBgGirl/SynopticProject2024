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
//    private val viewMatrix = FloatArray(16)
    private val model = FloatArray(16)
    private val MVPMatrix = FloatArray(16)

    private var choose: Float = 0f
    private var m = 0

    private lateinit var triangle: Triangle
    private lateinit var shader : Shader

    // Camera and settings
    private var camera: Camera = Camera()
    private var moveSpeed : Float = 2.0f
    private val nearClip: Float = 3f
    private val farClip: Float = 7f

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
        camera.setPosition(0f, 0f, 3f)
        camera.lookAt(0f, 0f, 0f)
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig)
    {
        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        shader = Shader(vertexShaderCode,fragmentShaderCode)
        triangle = Triangle()
    }

    override fun onDrawFrame(unused: GL10)
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        shader.use()

//        // Set the camera position (View matrix)
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
//        // Calculate the projection and view transformation
//        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Camera
        val viewMatrix = camera.getViewMatrix()
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)


        Matrix.setIdentityM(model,0)
        Matrix.translateM(model, 0, triangle.position[0], triangle.position[1],triangle.position[2])
        //Matrix.scaleM(model, 0, 1000f, 1000f,1000f)


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

    fun move(dx : Float, dy : Float)
    {
        triangle.position[0] -= (moveSpeed) * dx/choose
        triangle.position[1] += (moveSpeed) * dy/choose
    }

    fun moveCamera(dx : Float, dy : Float){
        camera.move((moveSpeed * dx)/width, (moveSpeed * -dy)/height, 0f)
        updateCameraLookAt(0f, 0f, -1f)
    }

    fun updateCameraLookAt(forwardX: Float, forwardY: Float, forwardZ: Float){
        val position = camera.getPosition()
        val lookAtX = position[0] + forwardX
        val lookAtY = position[1] + forwardY
        val lookAtZ = position[2] + forwardZ
        camera.lookAt(lookAtX, lookAtY, lookAtZ)
    }
}