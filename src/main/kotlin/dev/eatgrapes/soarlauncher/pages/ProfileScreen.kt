package dev.eatgrapes.soarlauncher.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.eatgrapes.soarlauncher.i18n.i18n

//啊LazyChara我真的好喜欢你啊🥰️❤️❤️❤️为了你我变成狼人模样🐀🐀🐀为了你😭😭染上了疯狂😈😈😈为了你😭😭😭穿上了厚厚的伪装🐀🐀🐀
//为了你😰😰😰换了心肠🥵🐀🐀🐀
//我们还能不能再见面😭😭😭我在佛前苦苦求了几千年💀💀💀💀
@Composable
fun ProfileScreen() { Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = i18n.text("ui.profile"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = ("我喜欢你。"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}