package com.wales.farmsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.util.Log
import android.view.GestureDetector
import android.view.ScaleGestureDetector
import android.view.MotionEvent

private const val DEBUG_TAG = "Gestures"

class OpenGLActivity : ComponentActivity()
{
    private lateinit var gLView: MyGLSurfaceView
    private lateinit var gestureDetector : GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private var previousScaleFactor = 1f

    public override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.d("OPENGL ACTIVITY", "onCreate!")
        super.onCreate(savedInstanceState)
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)

        gestureDetector = GestureDetector(this,object : GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent): Boolean {
                println("Pressed")
                return true
            }

//            override fun onShowPress(e: MotionEvent) {
//
//            }
//
//            override fun onSingleTapUp(e: MotionEvent): Boolean {
//
//            }
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // Check if it's a two-finger scroll
                if (e2.pointerCount == 2)
                {
                    // Call the rotateCamera function with the distance values
                    gLView.getRender().arcRotateCamera(distanceX, distanceY)
                } else {
                    // gLView.getRender().move(distanceX,distanceY)
                    gLView.getRender().moveCamera(distanceX, distanceY)
                }
                return true
            }

//            override fun onLongPress(e: MotionEvent) {
//
//            }
//
//            override fun onFling(
//                e1: MotionEvent?,
//                e2: MotionEvent,
//                velocityX: Float,
//                velocityY: Float
//            ): Boolean {
//
//            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                //gLView.getRender().onSingleTap()
                Log.d(DEBUG_TAG,"awdawda")
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
//                gLView.getRender().onSingleTap()
                return true
            }
        })

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor

                if (scaleFactor != previousScaleFactor) {
                    gLView.getRender().zoomCamera(scaleFactor)
                    previousScaleFactor = scaleFactor
                }
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                previousScaleFactor = detector.scaleFactor
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                // Reset the previous scale factor to 1 after the scaling ends
                previousScaleFactor = 1f
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return gestureDetector.onTouchEvent(event) ||
                super.onTouchEvent(event)
    }
}