package org.example.aapanam.util

object Logger {
    private const val TAG = "Aapanam"

    fun d(message: String) {
        println("[$TAG][DEBUG] $message")
    }

    fun i(message: String) {
        println("[$TAG][INFO] $message")
    }

    fun w(message: String, throwable: Throwable? = null) {
        println("[$TAG][WARN] $message")
        throwable?.printStackTrace()
    }

    fun e(message: String, throwable: Throwable? = null) {
        println("[$TAG][ERROR] $message")
        throwable?.printStackTrace()
    }
}
