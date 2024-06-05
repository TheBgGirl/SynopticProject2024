package com.wales
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherDataFetcher {
    private val meteoService: MeteoService = RetrofitClient.instance.create(MeteoService::class.java)
    fun fetchWeatherData(boundingBox: String, startDate: String, endDate: String)  {
        val call = meteoService.getWeatherData(
            boundingBox,
            startDate,
            endDate,
            "soil_moisture_0_to_7cm,temperature_2m",
            "temperature_2m_mean,sunshine_duration,rain_sum",
            "era5_land"
        )

        call.enqueue(object : Callback<List<WeatherResponse>> {
            override fun onResponse(call: Call<List<WeatherResponse>>, response: Response<List<WeatherResponse>>) {
                println(response.body()?.get(0)?.daily)
            }

            override fun onFailure(call: Call<List<WeatherResponse>>, t: Throwable) {
            }
        })
    }
}
