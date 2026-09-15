package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val details: String,
    val isNewUpdateAvailable: Boolean
)

object UpdateChecker {
    private const val API_URL = "https://api.github.com/repos/logichivedev-gif/financeflow-android/releases/latest"

    suspend fun checkForUpdates(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(API_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "FinanceFlow-App")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val tagName = jsonResponse.optString("tag_name", "").trim()
                val body = jsonResponse.optString("body", "")

                // Find browser_download_url for APK inside assets list
                var apkDownloadUrl = jsonResponse.optString("html_url", "")
                val assets = jsonResponse.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            apkDownloadUrl = asset.optString("browser_download_url", apkDownloadUrl)
                            break
                        }
                    }
                }

                if (tagName.isNotEmpty()) {
                    val isAvailable = isNewerVersion(currentVersion, tagName)
                    return@withContext UpdateInfo(
                        version = tagName,
                        downloadUrl = apkDownloadUrl,
                        details = body,
                        isNewUpdateAvailable = isAvailable
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return@withContext null
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        // Clean prefixes like 'v' if present (e.g. v1.1.0 -> 1.1.0)
        val cleanCurrent = current.removePrefix("v").trim()
        val cleanLatest = latest.removePrefix("v").trim()

        if (cleanCurrent == cleanLatest) return false

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        val size = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until size) {
            val currPart = currentParts.getOrElse(i) { 0 }
            val latePart = latestParts.getOrElse(i) { 0 }
            if (latePart > currPart) return true
            if (currPart > latePart) return false
        }
        return false
    }
}