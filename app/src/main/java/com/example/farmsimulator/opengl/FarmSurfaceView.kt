package com.example.farmsimulator.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.farmsimulator.ui.farm.CropInfo
import com.wales.FarmElement

@SuppressLint("ViewConstructor")
class MyGLSurfaceView(
    context: Context,
    width: Int,
    height: Int,
    crops: List<CropInfo>,
    ecoMode: Boolean,
    clickCallback: (Pair<Int, Int>) -> Unit,
    yield: List<List<FarmElement>>
) : GLSurfaceView(context) {

    // Renderer instance
    private val renderer: MyGLRenderer

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Initialize the renderer with the provided parameters
        renderer = MyGLRenderer(
            _width = width,
            _height = height,
            crops = crops,
            ecoMode = ecoMode,
            clickCallback = clickCallback,
            context = context
        )

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }

    // Function to get the renderer instance
    fun getRender(): MyGLRenderer {
        return renderer
    }
}

@Composable
fun OpenGLComposeView(
    modifier: Modifier = Modifier,
    width: Int,
    height: Int,
    crops: List<CropInfo>,
    ecoMode: Boolean,
    onClick: (Pair<Int, Int>) -> Unit,
    yield: List<List<FarmElement>>
) {
    var glSurfaceView by remember { mutableStateOf<MyGLSurfaceView?>(null) }
    val currentYield by rememberUpdatedState(newValue = yield)

    AndroidView(
        factory = { ctx ->
            MyGLSurfaceView(
                ctx,
                width = width,
                height = height,
                crops = crops,
                ecoMode = ecoMode,
                clickCallback = onClick,
                yield = yield
            ).apply {
                glSurfaceView = this
                setupGestures(this, ctx)
            }
        },
        modifier = modifier,
        update = {
            // Update the yield in the renderer and request a render
            it.getRender().setYield(currentYield)
            it.requestRender()
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            // Pause the GLSurfaceView when the composable is disposed
            glSurfaceView?.onPause()
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
private fun setupGestures(view: MyGLSurfaceView, context: Context) {
    // Gesture detector for handling single and double taps, and scrolls
    val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Handle camera movement or rotation based on pointer count
            if (e2.pointerCount == 2) {
                view.getRender().arcRotateCamera(distanceX, distanceY)
            } else {
                view.getRender().moveCamera(distanceX, distanceY)
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Handle single tap event
            view.getRender().onSingleTap(e.x, e.y)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return true
        }
    })

    // Scale gesture detector for handling pinch zoom gestures
    val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var previousScaleFactor = 1f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Handle zoom based on scale factor
            val scaleFactor = detector.scaleFactor
            if (scaleFactor != previousScaleFactor) {
                view.getRender().zoomCamera(scaleFactor)
                previousScaleFactor = scaleFactor
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            previousScaleFactor = detector.scaleFactor
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            previousScaleFactor = 1f
        }
    })

    // Set the touch listener for the GLSurfaceView
    view.setOnTouchListener { _, event ->
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        true
    }
}
