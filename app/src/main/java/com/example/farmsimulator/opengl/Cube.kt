package com.example.farmsimulator.opengl

class Cube(val minx : Float, val miny : Float, val minz : Float, val maxx : Float, val maxy: Float, val maxz: Float) {

    private val color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f);

    private val squares : Array<Square?> = arrayOfNulls(6)

    init {
        //Construct squares
        var count : Int = 0
        while (count < 6) {
            squares[count] = Square(floatArrayOf(0.0f, 0.0f), 1.0f)
            count++
        }


    }

    fun draw(shader: Shader)
    {
        var count : Int = 0
        while (count < 6) {
            squares[count]?.draw(shader)
            count++
        }
    }

}