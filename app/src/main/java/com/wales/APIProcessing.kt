package com.wales

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




data class WeatherResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("generationtime_ms") val generationTimeMs: Double,
    @SerializedName("utc_offset_seconds") val utcOffsetSeconds: Int,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("timezone_abbreviation") val timezoneAbbreviation: String,
    @SerializedName("hourly_units") val hourlyUnits: HourlyUnits,
    @SerializedName("hourly") val hourly: Hourly,
    @SerializedName("daily") val daily: Daily
) {
    data class HourlyUnits(
        @SerializedName("time") val timeUnit: String,
        @SerializedName("temperature_2m") val temperature2mUnit: String
    )

    data class Hourly(
        @SerializedName("time") val time: List<String>,
        @SerializedName("soil_moisture_0_to_7cm") val soilMoisture0To7cm: List<Double>,
        @SerializedName("temperature_2m") val temperature2m: List<Double>
    )

    data class Daily(
        @SerializedName("time") val time: List<String>,
        @SerializedName("temperature_2m_mean") val temperature2mMean: List<Double>
    )
}

interface MeteoService {
    @GET("v1/archive")
    fun getWeatherData(
        @Query("bounding_box") boundingBox: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("models") models: String
    ): Call<List<WeatherResponse>>
}
object RetrofitClient {
    private const val BASE_URL = "https://archive-api.open-meteo.com/"
    private var retrofit: Retrofit? = null

    val instance: Retrofit
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!
        }
}