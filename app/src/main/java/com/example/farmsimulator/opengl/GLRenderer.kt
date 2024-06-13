package com.example.farmsimulator.opengl

import android.content.Context
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.farmsimulator.ui.farm.CropInfo
import com.wales.FarmElement
import kotlinx.coroutines.yield
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MyGLRenderer(val _width: Int, val _height: Int, val crops: List<CropInfo>, val ecoMode: Boolean, val clickCallback: (Pair<Int, Int>) -> Unit, val context: Context) : GLSurfaceView.Renderer
{
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val model = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)

    private lateinit var triangle: Triangle

    private lateinit var plane : Plane

    private lateinit var planeShader : Shader
    private lateinit var cropShader : Shader

    lateinit var yeild : List<List<FarmElement>>


    fun setYield(yield1 : List<List<FarmElement>>) {
        this.yeild = yield1
        //plane.displayFarmData(yeild)
    }

    //private lateinit var cropSquare : CropSquare

    // ----- CAMERA SETTINGS ----- //
    private var camera: Camera = Camera()

    private val nearClip: Float = 3f
    private val farClip: Float = 500f

    private var maxZoomDistance = 20f
    private var minZoomDistance = maxZoomDistance / 4f

    private var moveSpeed: Float = minZoomDistance
    private var sensitivity: Float = 100f


    private var width: Float = 0f
    private var height: Float = 0f

    // ----- FARM SETTINGS ----- //
    private var farmWidth: Int = _width
    private var farmHeight: Int = _height

    // ----- APP SETTINGS ----- //
    private var lastFrameTime: Long = System.nanoTime()

    private val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex planeShader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "varying float yPosition;"+
                "uniform float isLines;"+
                "attribute vec2 a_TexCoordinate;" +
                "varying vec2 v_TexCoordinate;" +
                "void main() {" +
                "gl_Position = uMVPMatrix * vec4(vPosition.x,vPosition.y + isLines ,vPosition.z,1.0);" +
                "yPosition = vPosition.y * 3.0;"+
                "v_TexCoordinate = a_TexCoordinate;"+
                "}"

    private val planeFragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "varying float yPosition;"+
                "uniform sampler2D u_Texture;" +
                "varying vec2 v_TexCoordinate;" +
                "void main() {" +
                " gl_FragColor = vColor * yPosition;// * texture2D(u_Texture, v_TexCoordinate);\n" +
                "}"

    private val cropFragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "varying float yPosition;"+
                "uniform sampler2D u_Texture;" +
                "varying vec2 v_TexCoordinate;" +
                "void main() {" +
                "gl_FragColor  =  texture2D(u_Texture, v_TexCoordinate);"+
                "}"

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig)
    {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST) // Enable depth testing

        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.5f, 0.8f, 1.0f)

        planeShader = Shader(vertexShaderCode,planeFragmentShaderCode)
        cropShader = Shader(vertexShaderCode, cropFragmentShaderCode)

        triangle = Triangle(floatArrayOf(-0.5f,0f,-0.5f), floatArrayOf(0.5f,0f,-0.5f), floatArrayOf(0f,0f,0.5f))

        plane = Plane(farmWidth, farmHeight, crops, context)

        //cropSquare = CropSquare(floatArrayOf(0.0f, 0.0f), 2.0f, floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f), context, CropType.PUMPKIN)

        // Camera Zoom From Farm Size
        maxZoomDistance = if(farmHeight >= farmWidth) {
            farmHeight.toFloat() * 1.5f
        } else{
            farmWidth.toFloat() * 1.5f
        }

        minZoomDistance = maxZoomDistance / 2f

        moveSpeed = minZoomDistance

        // Default Camera Values
        camera.position = floatArrayOf(0f, 0.5f, 0f)
        camera.radius = minZoomDistance + 1f
        camera.pitch = 90f

        plane.displayFarmData(yeild)

    }

    override fun onDrawFrame(unused: GL10)
    {
        val currentTime = System.nanoTime()
        val deltaTime = currentTime - lastFrameTime

        // Set Frame Rate
        val targetDelay: Long = if (ecoMode) {
            // Eco mode on = 15 FPS
            (1_000_000_000L / 15)
        } else {
            // Eco mode off = 60 FPS
            (1_000_000_000L / 60)
        }

        if (deltaTime < targetDelay) {
            try {
                // Convert nanoseconds to milliseconds
                Thread.sleep((targetDelay - deltaTime) / 1_000_000)
            } catch (e: InterruptedException) {
                // Handle exception
            }
        }

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        planeShader.use()
        planeShader.setFloat("isLines",0.0f)

        // Camera
        viewMatrix = camera.getViewMatrix()
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.setIdentityM(model,0)
        //Matrix.translateM(model, 0, triangle.position[0], triangle.position[1],triangle.position[2])
        Matrix.multiplyMM(mvpMatrix, 0, vPMatrix, 0, model, 0)
        planeShader.setMat4("uMVPMatrix",mvpMatrix)

        plane.draw(planeShader, cropShader, viewMatrix, projectionMatrix, camera.pitch)

        //triangle.draw(planeShader)

        //cropShader.use()
        //cropShader.setMat4("uMVPMatrix", mvpMatrix)
        //cropSquare.draw(cropShader)

        lastFrameTime = System.nanoTime()
    }
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        this.width = width.toFloat()
        this.height = height.toFloat()

        val aspectRatio: Float = width.toFloat() / height.toFloat()

        Matrix.frustumM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, nearClip, farClip)
    }

    fun onSingleTap(x: Float, y: Float) {
        val gridPosition = getWorldCoordinates(x, y)
        Log.d("GridPosition", "Grid Position: $gridPosition")

        if (gridPosition != null) {
            plane.setSquare(gridPosition.first,gridPosition.second)
            clickCallback(Pair(gridPosition.first.toInt(), gridPosition.second.toInt()))

            // ----- TESTING YIELD ----- //

            // Rotate points clockwise
            var YieldPosX = gridPosition.second.toInt()
            var YieldPosY = _height - 1 - gridPosition.first.toInt()

            // Flip Axis
            YieldPosX = _width - 1 - YieldPosX
            YieldPosY = _height - 1 - YieldPosY

            Log.d("YieldPos: ", "X: $YieldPosX, Y: $YieldPosY")

            Log.d("Yield at Pos: ",
                "X: ${gridPosition.first} " +
                        "Y: ${gridPosition.second} " +
                        "Yield: ${yeild[YieldPosX][YieldPosY].yield}")
        }

    }


    private fun getWorldCoordinates(x: Float, y: Float): Pair<Float, Float>? {
        // Convert screen coordinates to normalized device coordinates
        val ndcX = (2.0f * x) / width - 1.0f
        val ndcY = 1.0f - (2.0f * y) / height

        // Convert NDC to eye coordinates
        val invertedProjectionMatrix = FloatArray(16)
        Matrix.invertM(invertedProjectionMatrix, 0, projectionMatrix, 0)
        val eyeCoordinates = floatArrayOf(ndcX, ndcY, -1.0f, 1.0f)
        Matrix.multiplyMV(eyeCoordinates, 0, invertedProjectionMatrix, 0, eyeCoordinates, 0)

        // Convert eye coordinates to world coordinates
        val invertedViewMatrix = FloatArray(16)
        Matrix.invertM(invertedViewMatrix, 0, viewMatrix, 0)
        eyeCoordinates[2] = -1.0f
        eyeCoordinates[3] = 0.0f
        val rayWorld = FloatArray(4)
        Matrix.multiplyMV(rayWorld, 0, invertedViewMatrix, 0, eyeCoordinates, 0)

        // Normalize the ray direction
        val rayDirection = floatArrayOf(rayWorld[0], rayWorld[1], rayWorld[2])
        val length = sqrt(rayDirection[0] * rayDirection[0] + rayDirection[1] * rayDirection[1] + rayDirection[2] * rayDirection[2])
        rayDirection[0] /= length
        rayDirection[1] /= length
        rayDirection[2] /= length

        // Perform ray-plane intersection
        val planeY = 0.0f // Assuming the plane is at y = 0
        val t = (planeY - (camera.position[1] + camera.target[1])) / rayDirection[1]
        if (t < 0) return null

        val intersectionPoint = floatArrayOf(
            camera.position[0] + camera.target[0] + t * rayDirection[0],
            camera.position[1] + camera.target[1] + t * rayDirection[1],
            camera.position[2] + camera.target[2] + t * rayDirection[2]
        )

        // Convert intersection point to grid coordinates
        val gridX = ((intersectionPoint[0] + farmWidth / 2.0f + 1.0f) / farmWidth * (plane.width - 1)).roundToInt()
        val gridZ = ((intersectionPoint[2] + farmHeight / 2.0f) / farmHeight * (plane.height - 1)).roundToInt()

        // Ensure gridX and gridZ are within the valid range
        if(gridX < plane.width && gridX > 0){
            if(gridZ < plane.height - 1 && gridZ >= 0){
                return Pair(plane.width - 1f - gridX.toFloat(), gridZ.toFloat()) // Flip the X coordinate to make 0,0 the bottom left
            }
        }

        return null
    }

    fun moveCamera(dx : Float, dz : Float)
    {
        val newX = camera.position[0] + (moveSpeed * -dx)/width
        val newZ = camera.position[2] + (moveSpeed * -dz)/height

        // Check if the new position is within the boundaries
        if (newX >= -farmWidth/2 - 1 && newX <= farmWidth/2 && newZ >= -farmHeight/2 - 1 && newZ <= farmHeight/2) {
            camera.move((moveSpeed * -dx)/width, 0f, (moveSpeed * -dz)/height)
        }
    }

    fun arcRotateCamera(currentX: Float, currentY: Float){
        // Calculate the difference between current and previous touch positions
        //val deltaX = -currentX
        val deltaY = -currentY

        //val angleX = camera.yaw+deltaX * sensitivity/width
        val angleY = camera.pitch+deltaY * sensitivity/height

        //camera.yaw = angleX
        if(angleY < 90 && angleY > 30)
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
