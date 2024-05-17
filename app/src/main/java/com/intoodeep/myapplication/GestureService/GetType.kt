package com.intoodeep.myapplication.GestureService

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GetType {
    fun getType():Int{
        var type = -1
        try {
            val url = URL("http://192.168.10.2")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                bufferedReader.close()
                val jsonObject = JSONObject(response.toString())
                type = jsonObject.getInt("type")
            } else {
                println("Error: $responseCode")
            }

            connection.disconnect()

        }
        catch (t:Throwable){
            t.printStackTrace()
        }
        return type
    }
}