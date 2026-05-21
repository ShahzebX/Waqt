package com.example.waqt.testutil

import com.example.waqt.network.CityInfoData
import com.example.waqt.network.CityInfoResponse

object TestCityInfo {
    fun karachi(): CityInfoResponse {
        return CityInfoResponse(
            code = 200,
            status = "OK",
            data = CityInfoData(
                latitude = 24.8607,
                longitude = 67.0011,
                timezone = "Asia/Karachi"
            )
        )
    }
}
