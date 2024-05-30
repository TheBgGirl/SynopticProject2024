package com.wales.farmsimulator

import android.opengl.GLES20

class Shader(vertexShaderCode: String, fragmentShaderCode: String)
{

    private var ID: Int = 0

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        ID = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }

    fun loadShader(type: Int, shaderCode: String): Int
    {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun use()
    {
        GLES20.glUseProgram(ID)
    }
    fun getID() : Int
    {
        return ID
    }

    fun setMat4(name:String,value:FloatArray)
    {
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(        // get handle to shape's transformation matrix
            GLES20.glGetUniformLocation(ID, name), 1, false, value, 0)

    }
}