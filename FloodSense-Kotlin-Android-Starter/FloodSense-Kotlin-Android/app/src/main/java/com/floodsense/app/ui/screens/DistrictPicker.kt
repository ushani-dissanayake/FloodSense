package com.floodsense.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.floodsense.app.data.SriLankaDistrict
import com.floodsense.app.data.Station
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary

@Composable
fun DistrictPickerDialog(
    districts: List<SriLankaDistrict>,
    selectedName: String,
    onSelect: (SriLankaDistrict) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, districts) {
        val needle = query.trim()
        if (needle.isBlank()) districts
        else districts.filter {
            it.name.contains(needle, ignoreCase = true) ||
                it.province.contains(needle, ignoreCase = true)
        }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.province } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Select district", color = Navy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Districts in the Kalu Ganga 2020–2025 dataset.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search district or province") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    grouped.forEach { (province, items) ->
                        item(key = "h-$province") {
                            Text(
                                province,
                                color = Teal,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }
                        items(items, key = { it.name }) { district ->
                            val selected = district.name == selectedName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(district) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(district.name, color = Navy, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        when (district.stationIds.size) {
                                            0 -> "No official gauge in dataset"
                                            1 -> "1 station"
                                            else -> "${district.stationIds.size} stations"
                                        },
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                if (selected) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = Teal)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun StationPickerDialog(
    stations: List<Station>,
    selectedId: String?,
    districtName: String,
    onSelect: (Station) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Select station", color = Navy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    if (stations.isEmpty()) {
                        "No Irrigation Department station is published for $districtName in the current datasets."
                    } else {
                        "Stations in $districtName."
                    },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(stations, key = { it.id }) { station ->
                        val selected = station.id == selectedId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(station) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(station.name, color = Navy, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${station.district} · ${"%.2f".format(station.waterLevel)} m",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            if (selected) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = Teal)
                            }
                        }
                        HorizontalDivider()
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}
