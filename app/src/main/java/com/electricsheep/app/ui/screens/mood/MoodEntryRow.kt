package com.electricsheep.app.ui.screens.mood

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.util.DateFormatter

/**
 * Composable for displaying a single mood entry in a table row.
 */
@Composable
fun MoodEntryRow(mood: Mood) {
    val formattedDate = DateFormatter.formatDateTime(mood.timestamp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                contentDescription = "Mood entry: score ${mood.score} out of 10, recorded on $formattedDate"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timestamp column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Score column
        Text(
            text = mood.score.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics {
                contentDescription = "Score: ${mood.score} out of 10"
            }
        )
    }
    
    Divider(modifier = Modifier.padding(horizontal = 12.dp))
}

