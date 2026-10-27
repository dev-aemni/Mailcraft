package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.EmailBlock

val PRESET_COLORS = listOf(
  "#4F46E5", // Indigo
  "#2563EB", // Blue
  "#0F172A", // Slate Dark
  "#059669", // Emerald
  "#D97706", // Amber
  "#DC2626", // Red
  "#7C3AED", // Purple
  "#E11D48"  // Rose
)

@Composable
fun BlockCardWrapper(
  title: String,
  icon: ImageVector,
  canMoveUp: Boolean,
  canMoveDown: Boolean,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
  onDuplicate: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  var isExpanded by remember { mutableStateOf(true) }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { isExpanded = !isExpanded }
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Actions: Up, Down, Duplicate, Delete
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onMoveUp,
            enabled = canMoveUp,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
          }
          IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
          }
          IconButton(
            onClick = onDuplicate,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(18.dp))
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              Icons.Default.DeleteOutline,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
          content()
        }
      }
    }
  }
}

@Composable
fun ButtonBlockEditor(
  block: EmailBlock.Button,
  onUpdate: (EmailBlock.Button) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = block.label,
      onValueChange = { onUpdate(block.copy(label = it)) },
      label = { Text("Button Text") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("button_label_input")
    )
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedTextField(
      value = block.url,
      onValueChange = { onUpdate(block.copy(url = it)) },
      label = { Text("Target Link URL") },
      placeholder = { Text("https://...") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("button_url_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Color presets
    Text("Button Color:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PRESET_COLORS.forEach { hex ->
        val colorInt = android.graphics.Color.parseColor(hex)
        val isSelected = block.backgroundColor.equals(hex, ignoreCase = true)
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(colorInt))
            .border(
              width = if (isSelected) 3.dp else 1.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
              shape = CircleShape
            )
            .clickable { onUpdate(block.copy(backgroundColor = hex)) }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Alignment and Rounded Corners
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Alignment:", style = MaterialTheme.typography.bodySmall)
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("left", "center", "right").forEach { align ->
          FilterChip(
            selected = block.align == align,
            onClick = { onUpdate(block.copy(align = align)) },
            label = { Text(align.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text("Corner Radius: ${block.cornerRadiusDp}dp", style = MaterialTheme.typography.bodySmall)
    Slider(
      value = block.cornerRadiusDp.toFloat(),
      onValueChange = { onUpdate(block.copy(cornerRadiusDp = it.toInt())) },
      valueRange = 0f..28f,
      steps = 6
    )

    // Full width toggle
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Full Width on Mobile", style = MaterialTheme.typography.bodySmall)
      Switch(
        checked = block.isFullWidth,
        onCheckedChange = { onUpdate(block.copy(isFullWidth = it)) }
      )
    }

    // Live mini preview
    Spacer(modifier = Modifier.height(12.dp))
    Text("Preview Button:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = when (block.align) {
        "left" -> Alignment.CenterStart
        "right" -> Alignment.CenterEnd
        else -> Alignment.Center
      }
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(block.cornerRadiusDp.dp))
          .background(Color(android.graphics.Color.parseColor(block.backgroundColor)))
          .padding(
            horizontal = block.paddingHorizontal.dp,
            vertical = block.paddingVertical.dp
          )
      ) {
        Text(
          text = block.label,
          color = Color(android.graphics.Color.parseColor(block.textColor)),
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }
    }
  }
}

@Composable
fun GifImageBlockEditor(
  block: EmailBlock.GifImage,
  onOpenGifPicker: () -> Unit,
  onUpdate: (EmailBlock.GifImage) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    // Thumbnail Preview
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .clip(RoundedCornerShape(block.cornerRadiusDp.dp))
        .background(Color.Black.copy(alpha = 0.05f))
    ) {
      AsyncImage(
        model = block.imageUrl,
        contentDescription = block.altText,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth()
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = onOpenGifPicker,
      modifier = Modifier.fillMaxWidth().testTag("open_gif_library_button"),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
      Icon(Icons.Default.Gif, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Browse GIFs & Images")
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = block.imageUrl,
      onValueChange = { onUpdate(block.copy(imageUrl = it)) },
      label = { Text("Image / GIF URL") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = block.caption,
      onValueChange = { onUpdate(block.copy(caption = it)) },
      label = { Text("Caption (Optional)") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = block.linkUrl,
      onValueChange = { onUpdate(block.copy(linkUrl = it)) },
      label = { Text("Click-through Link URL (Optional)") },
      placeholder = { Text("https://...") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Alignment:", style = MaterialTheme.typography.bodySmall)
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("left", "center", "right").forEach { align ->
          FilterChip(
            selected = block.align == align,
            onClick = { onUpdate(block.copy(align = align)) },
            label = { Text(align.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
          )
        }
      }
    }
  }
}

@Composable
fun EmbedCardBlockEditor(
  block: EmailBlock.EmbedCard,
  onUpdate: (EmailBlock.EmbedCard) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = block.badge,
        onValueChange = { onUpdate(block.copy(badge = it)) },
        label = { Text("Badge Label") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = block.badgeColor,
        onValueChange = { onUpdate(block.copy(badgeColor = it)) },
        label = { Text("Badge Hex") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = block.title,
      onValueChange = { onUpdate(block.copy(title = it)) },
      label = { Text("Embed Title") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = block.body,
      onValueChange = { onUpdate(block.copy(body = it)) },
      label = { Text("Embed Content Body") },
      minLines = 2,
      maxLines = 4,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = block.linkText,
        onValueChange = { onUpdate(block.copy(linkText = it)) },
        label = { Text("Link Text") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = block.linkUrl,
        onValueChange = { onUpdate(block.copy(linkUrl = it)) },
        label = { Text("Link URL") },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun HeaderBlockEditor(
  block: EmailBlock.Header,
  onUpdate: (EmailBlock.Header) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = block.text,
      onValueChange = { onUpdate(block.copy(text = it)) },
      label = { Text("Headline Title") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = block.subtitle,
      onValueChange = { onUpdate(block.copy(subtitle = it)) },
      label = { Text("Subtitle") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Alignment:", style = MaterialTheme.typography.bodySmall)
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("left", "center", "right").forEach { align ->
          FilterChip(
            selected = block.align == align,
            onClick = { onUpdate(block.copy(align = align)) },
            label = { Text(align.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
          )
        }
      }
    }
  }
}

@Composable
fun TextBlockEditor(
  block: EmailBlock.Text,
  onUpdate: (EmailBlock.Text) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = block.content,
      onValueChange = { onUpdate(block.copy(content = it)) },
      label = { Text("Paragraph Text") },
      minLines = 3,
      maxLines = 6,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Style as Quote Callout", style = MaterialTheme.typography.bodySmall)
      Switch(
        checked = block.isQuote,
        onCheckedChange = { onUpdate(block.copy(isQuote = it)) }
      )
    }
  }
}

@Composable
fun FooterBlockEditor(
  block: EmailBlock.Footer,
  onUpdate: (EmailBlock.Footer) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = block.signOff,
      onValueChange = { onUpdate(block.copy(signOff = it)) },
      label = { Text("Sign-off Message") },
      minLines = 2,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = block.companyName,
      onValueChange = { onUpdate(block.copy(companyName = it)) },
      label = { Text("Company or Brand Name") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = block.disclaimer,
      onValueChange = { onUpdate(block.copy(disclaimer = it)) },
      label = { Text("Footer Disclaimer") },
      minLines = 2,
      modifier = Modifier.fillMaxWidth()
    )
  }
}
