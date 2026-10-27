package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class CuratedGif(
  val title: String,
  val url: String,
  val category: String,
  val tag: String
)

val CURATED_GIFS = listOf(
  CuratedGif("Party Popper", "https://media.giphy.com/media/26AHONQ79FdWZhAI0/giphy.gif", "Celebration", "Party"),
  CuratedGif("Confetti Blast", "https://media.giphy.com/media/3o7TKSjRrfIPjeiVyM/giphy.gif", "Celebration", "Confetti"),
  CuratedGif("Thank You Hearts", "https://media.giphy.com/media/osAcIGJnyeBq30qDCM/giphy.gif", "Gratitude", "Thanks"),
  CuratedGif("High Five", "https://media.giphy.com/media/3oEjHV0z8S7WM4MwnK/giphy.gif", "Greetings", "High Five"),
  CuratedGif("Rocket Launch", "https://media.giphy.com/media/mi6DsSSNKDbUY/giphy.gif", "Action", "Launch"),
  CuratedGif("Thumbs Up Nice", "https://media.giphy.com/media/111ebonMs90YLu/giphy.gif", "Greetings", "Thumbs Up"),
  CuratedGif("Excited Dance", "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif", "Celebration", "Excited"),
  CuratedGif("Idea Bulb", "https://media.giphy.com/media/3o7btPCcdNniyf0ArS/giphy.gif", "Tech", "Idea"),
  CuratedGif("Welcome Wave", "https://media.giphy.com/media/xT9IgG50Fb7Mi0prBC/giphy.gif", "Greetings", "Welcome"),
  CuratedGif("Clapping Hands", "https://media.giphy.com/media/l4q8cJzGdR9J8w3hS/giphy.gif", "Gratitude", "Bravo")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GifPickerSheet(
  currentUrl: String,
  onDismiss: () -> Unit,
  onSelectGif: (url: String, caption: String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedCategory by remember { mutableStateOf("All") }
  var customUrlInput by remember { mutableStateOf("") }

  val categories = listOf("All", "Celebration", "Greetings", "Gratitude", "Action", "Tech")
  val filteredGifs = if (selectedCategory == "All") {
    CURATED_GIFS
  } else {
    CURATED_GIFS.filter { it.category == selectedCategory }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Pick a GIF or Image",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Embed lively animated GIFs directly into your email",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Custom URL Input
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = customUrlInput,
          onValueChange = { customUrlInput = it },
          placeholder = { Text("Paste custom GIF / Image URL...") },
          modifier = Modifier.weight(1f).testTag("custom_gif_url_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (customUrlInput.isNotBlank()) {
              onSelectGif(customUrlInput.trim(), "Custom image")
              onDismiss()
            }
          },
          enabled = customUrlInput.isNotBlank(),
          modifier = Modifier.testTag("apply_custom_gif_button")
        ) {
          Text("Use")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = cat },
            label = { Text(cat, fontSize = 12.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // GIF Grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredGifs) { gif ->
          val isSelected = currentUrl == gif.url
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1.3f)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
              )
              .clickable {
                onSelectGif(gif.url, gif.title)
                onDismiss()
              }
          ) {
            AsyncImage(
              model = gif.url,
              contentDescription = gif.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth()
            )
            // Overlay tag
            Box(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = gif.tag,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
            if (isSelected) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(6.dp)
                  .size(24.dp)
                  .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}
