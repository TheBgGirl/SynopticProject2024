package com.wales.farmsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity

import android.opengl.GLSurfaceView
import android.util.Log

class OpenGLActivity : ComponentActivity()
{
    private lateinit var gLView: GLSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.d("OPENGL ACTIVITY", "onCreate!")
        super.onCreate(savedInstanceState)
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)
    }
}