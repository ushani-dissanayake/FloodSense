package com.floodsense.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.data.FieldLogCatalog
import com.floodsense.app.ui.theme.Danger
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun StationGoogleMap(
    latitude: Double,
    longitude: Double,
    stationName: String,
    modifier: Modifier = Modifier,
    snapToStation: Boolean = true
) {
    val context = LocalContext.current
    val (lat, lon) = remember(latitude, longitude, stationName, snapToStation) {
        if (snapToStation) {
            resolveMapCoordinates(latitude, longitude, stationName)
        } else {
            latitude to longitude
        }
    }
    val mapsUri = remember(lat, lon) {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
    }
    var zoom by remember(lat, lon) { mutableIntStateOf(14) }
    var reloadToken by remember(lat, lon) { mutableIntStateOf(0) }

    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFD7E6F0))
                .clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, mapsUri))
                }
        ) {
            OsmStationTiles(
                latitude = lat,
                longitude = lon,
                zoom = zoom,
                reloadToken = reloadToken,
                onRetry = { reloadToken++ }
            )
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = stationName,
                tint = Danger,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 18.dp)
                    .size(42.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                MapZoomChip("+") { zoom = (zoom + 1).coerceAtMost(16) }
                Spacer(Modifier.height(6.dp))
                MapZoomChip("−") { zoom = (zoom - 1).coerceAtLeast(12) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(stationName, color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Text("Latitude", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text("%.6f°".format(lat), color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text("Longitude", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text("%.6f°".format(lon), color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            "Open in Google Maps",
            color = DeepBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, mapsUri))
                }
        )
    }
}

@Composable
private fun MapZoomChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun OsmStationTiles(
    latitude: Double,
    longitude: Double,
    zoom: Int,
    reloadToken: Int,
    onRetry: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var failed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.coerceAtLeast(1)
        val heightPx = constraints.maxHeight.coerceAtLeast(1)
        LaunchedEffect(latitude, longitude, zoom, widthPx, heightPx, reloadToken) {
            loading = true
            failed = false
            bitmap = OsmMapTiles.render(latitude, longitude, zoom, widthPx, heightPx)
            loading = false
            failed = bitmap == null
        }
        val map = bitmap
        when {
            map != null -> Image(
                bitmap = map.asImageBitmap(),
                contentDescription = "Station map",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Map could not load",
                        color = Navy,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = onRetry) { Text("Retry", color = Teal, fontWeight = FontWeight.Bold) }
                }
            }
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            }
        }
    }
}

internal fun resolveMapCoordinates(
    latitude: Double,
    longitude: Double,
    stationName: String
): Pair<Double, Double> {
    if (isPlausibleSriLanka(latitude, longitude)) return latitude to longitude
    val fromCatalog = FieldLogCatalog.stations.firstOrNull { station ->
        station.name.equals(stationName, ignoreCase = true) ||
            stationName.contains(station.gauge, ignoreCase = true) ||
            station.gauge.contains(stationName, ignoreCase = true)
    }
    if (fromCatalog != null) return fromCatalog.latitude to fromCatalog.longitude
    return 6.6182 to 80.0047
}

private fun isPlausibleSriLanka(latitude: Double, longitude: Double): Boolean =
    latitude in 5.8..10.0 && longitude in 79.4..82.0

private object OsmMapTiles {
    private const val TILE = 256
    private val cache = LruCache<String, Bitmap>(96)
    private val sources = listOf(
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
        "https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}"
    )

    suspend fun render(lat: Double, lon: Double, zoom: Int, width: Int, height: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                val n = 1 shl zoom
                val worldX = lonToTileX(lon, n) * TILE
                val worldY = latToTileY(lat, n) * TILE
                val left = worldX - width / 2.0
                val top = worldY - height / 2.0
                val out = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                val canvas = Canvas(out)
                canvas.drawColor(0xFFD7E6F0.toInt())
                val minTx = floor(left / TILE).toInt()
                val maxTx = floor((left + width) / TILE).toInt()
                val minTy = floor(top / TILE).toInt()
                val maxTy = floor((top + height) / TILE).toInt()
                var painted = 0
                for (tx in minTx..maxTx) {
                    for (ty in minTy..maxTy) {
                        if (ty !in 0 until n) continue
                        val wrappedX = ((tx % n) + n) % n
                        val tile = loadTile(zoom, wrappedX, ty) ?: continue
                        canvas.drawBitmap(
                            tile,
                            (tx * TILE - left).toFloat(),
                            (ty * TILE - top).toFloat(),
                            null
                        )
                        painted++
                    }
                }
                if (painted == 0) null else out
            }.getOrNull()
        }

    private fun loadTile(zoom: Int, x: Int, y: Int): Bitmap? {
        val key = "$zoom/$x/$y"
        cache.get(key)?.let { return it }
        for (template in sources) {
            val url = template
                .replace("{z}", zoom.toString())
                .replace("{x}", x.toString())
                .replace("{y}", y.toString())
            val bitmap = download(url) ?: continue
            cache.put(key, bitmap)
            return bitmap
        }
        return null
    }

    private fun download(url: String): Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "FloodSense/1.0 (Android; floodsense.app)")
                setRequestProperty("Accept", "image/png")
            }
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun lonToTileX(lon: Double, n: Int): Double = (lon + 180.0) / 360.0 * n

    private fun latToTileY(lat: Double, n: Int): Double {
        val clamped = lat.coerceIn(-85.0511, 85.0511)
        val latRad = clamped * PI / 180.0
        return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n
    }
}
