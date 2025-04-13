package com.android.movieappmazaady.util

sealed class ErrorType {
    object Network : ErrorType()
    object NoInternet : ErrorType()
    object EmptyData : ErrorType()
    data class ApiError(val code: Int, val message: String) : ErrorType()
    data class UnknownError(val message: String) : ErrorType()
}

fun ErrorType.getErrorMessage(): String {
    return when (this) {
        is ErrorType.Network -> "Network error occurred. Please check your connection."
        is ErrorType.NoInternet -> "No internet connection. Please check your network settings."
        is ErrorType.EmptyData -> "No data available."
        is ErrorType.ApiError -> "Error: $message"
        is ErrorType.UnknownError -> message
    }
} 