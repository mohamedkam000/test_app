package com.test.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.material3.*
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.test.app.ui.theme.TestTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppEntry()
                }
            }
        }
    }
}

@Composable
fun AppEntry() {
    var showOnboarding by remember { mutableStateOf(true) }
    if (showOnboarding) {
        OnboardingPager(onFinish = { showOnboarding = false })
    } else {
        MainScreen()
    }
}

@Composable
fun OnboardingPager(onFinish: () -> Unit) {
    var page by remember { mutableStateOf(0) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (page) {
            0 -> OnboardingPage(title = "Welcome", subtitle = "Discover featured categories.")
            1 -> OnboardingPage(title = "Browse", subtitle = "Beautiful photos and sticky headers.")
            2 -> OnboardingPage(title = "Enjoy", subtitle = "Fast animations and expressive theming.")
        }
        Row(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (i in 0..2) {
                val active = i == page
                Box(modifier = Modifier
                    .size(if (active) 16.dp else 10.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                    .clickable { page = i })
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (page < 2) page++ else onFinish()
            }) {
                Text(if (page < 2) "Next" else "Start")
            }
        }
    }
}

@Composable
fun OnboardingPage(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(80.dp))
        Text(title, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(subtitle)
    }
}

data class Category(val title: String, val items: List<Item>)
data class Item(val id: Int, val title: String, val imageUrl: String)

fun sampleData(): List<Category> {
    return listOf(
        Category("Fruits", List(6) { Item(it, "Fruit #$it", "https://picsum.photos/seed/fruit$it/400/300") }),
        Category("Vegetables", List(5) { Item(it, "Veg #$it", "https://picsum.photos/seed/veg$it/400/300") }),
        Category("Drinks", List(4) { Item(it, "Drink #$it", "https://picsum.photos/seed/drink$it/400/300") })
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val categories = remember { sampleData() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market") },
                actions = {
                    IconButton(onClick = { /* TODO settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            categories.forEach { category ->
                stickyHeader {
                    Surface(
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.title, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                items(category.items) { item ->
                    MarketRow(item)
                }
            }
        }
    }
}

@Composable
fun MarketRow(item: Item) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Row(modifier = Modifier.height(120.dp)) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight()
                )
                Column(modifier = Modifier.padding(12.dp).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text("Description here", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}