package com.wales

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileWriter
import java.util.Date

data class Key(
    val longitude: Double,
    val latitude: Double
)

class WeatherDataFetcher {
    private val meteoService: MeteoService =
        RetrofitClient.instance.create(MeteoService::class.java)
    val weatherData: HashMap<Key, List<Double?>> = HashMap()
    val date: HashMap<Key, List<String?>> = HashMap()
    fun fetchWeatherData(
        boundingBox: String,
        startDate: String,
        endDate: String,
        callback: (Boolean) -> Unit
    ) {
        val call = meteoService.getWeatherData(
            boundingBox,
            startDate,
            endDate,
            "soil_moisture_0_to_7cm",
            "temperature_2m_mean",
            "era5_land"
        )

        call.enqueue(object : Callback<List<WeatherResponse>> {
            override fun onResponse(
                call: Call<List<WeatherResponse>>,
                response: Response<List<WeatherResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    for (res in response.body()!!) {
                        weatherData[Key(res.longitude, res.latitude)] = res.daily.temperature2mMean
                        date[Key(res.longitude, res.latitude)] = res.daily.time
                    }
                    callback(true)
                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<List<WeatherResponse>>, t: Throwable) {
                println("Error fetching weather data: ${t.message}")
                callback(false)
            }
        })
    }


}