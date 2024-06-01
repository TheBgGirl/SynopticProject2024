package com.wales.farmsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

private const val DEBUG_TAG = "Gestures"

class OpenGLActivity : ComponentActivity()
{
    private lateinit var gLView: MyGLSurfaceView
    private lateinit var gestureDetector : GestureDetector

    public override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.d("OPENGL ACTIVITY", "onCreate!")
        super.onCreate(savedInstanceState)
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = MyGLSurfaceView(this)
        println("Hello World")

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
                    // Calculate the average distance scrolled by both fingers
                    val avgDistanceX = (e2.getX(0) - e2.getX(1) + e2.getY(0) - e2.getY(1)) / 2
                    val avgDistanceY = (e2.getY(0) - e2.getY(1) + e2.getX(0) - e2.getX(1)) / 2

                    // Call the rotateCamera function with the calculated values
                    gLView.getRender().arcRotateCamera(avgDistanceX, avgDistanceY)
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
                gLView.getRender().onSingleTap()
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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }
}