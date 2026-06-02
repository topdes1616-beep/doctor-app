package com.clinic.radar

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class LocalWebServer(private val context: Context, port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        return try {
            val inputStream = context.assets.open("index.html")
            newChunkedResponse(Response.Status.OK, "text/html; charset=utf-8", inputStream)
        } catch (e: IOException) {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found")
        }
    }
}
