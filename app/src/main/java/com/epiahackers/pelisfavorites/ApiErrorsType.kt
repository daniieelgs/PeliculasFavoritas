package com.epiahackers.pelisfavorites

enum class ApiErrorsType(var message: String? = null, var code: Int? = null) {
    TIME_OUT, INTERNET_CONNECTION, UNKNOWN_ERROR, CUSTOM_ERROR
}