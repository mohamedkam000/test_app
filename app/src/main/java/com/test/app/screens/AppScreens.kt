package com.test.app.screens

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.test.app.data.AppCategory
import com.test.app.data.AppItem
import com.test.app.data.AppMarket
import com.test.app.data.AppState
import com.test.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay

// --- Reusable Glassmorphic Card Component ---

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100) // Small delay for staggered animation
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300), initialScale = 0.8f)
    ) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                // Use a semi-transparent color to get the glass effect
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow
        ) {
            Box(
                // This modifier applies the background blur on Android 12+
                // On older versions, it just shows the semi-transparent card
                modifier = Modifier
                    .graphicsLayer {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = RenderEffect
                                .createBlurEffect(
                                    20f, 20f,
                                    Shader.TileMode.CLAMP
                                )
                                .asComposeRenderEffect()
                        }
                    }
            ) {
                content()
            }
        }
    }
}

// --- Reusable Screen Components ---

@Composable
fun ItemCard(
    emoji: String,
    title: String,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        onClick = onClick,
        modifier = Modifier.height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreenScaffold(
    title: String,
    navController: NavController,
    canNavigateBack: Boolean = true,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent, // Transparent for edge-to-edge
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}

// --- 1. State Screen ---

@Composable
fun StateScreen(navController: NavController, viewModel: AppViewModel) {
    val states = viewModel.getStates()
    
    AppScreenScaffold(title = "Select a State", navController = navController, canNavigateBack = false) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            items(states) { state ->
                ItemCard(
                    emoji = state.iconEmoji,
                    title = state.name,
                    onClick = {
                        navController.navigate("markets/${state.id}")
                    }
                )
            }
        }
    }
}

// --- 2. Market Screen ---

@Composable
fun MarketScreen(navController: NavController, viewModel: AppViewModel, stateId: String) {
    val markets = viewModel.getMarkets(stateId)
    val stateName = viewModel.getStates().find { it.id == stateId }?.name ?: "Markets"
    
    AppScreenScaffold(title = stateName, navController = navController) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            items(markets) { market ->
                ItemCard(
                    emoji = "🛒", // Generic market emoji
                    title = market.name,
                    onClick = {
                        navController.navigate("categories/${market.id}")
                    }
                )
            }
        }
    }
}

// --- 3. Category Screen ---

@Composable
fun CategoryScreen(navController: NavController, viewModel: AppViewModel, marketId: String) {
    val categories = viewModel.getCategories()
    
    AppScreenScaffold(title = "Select a Category", navController = navController) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            items(categories) { category ->
                ItemCard(
                    emoji = category.iconEmoji,
                    title = category.name,
                    onClick = {
                        navController.navigate("items/${category.id}")
                    }
                )
            }
        }
    }
}

// --- 4. Item List Screen ---

@Composable
fun ItemListScreen(navController: NavController, viewModel: AppViewModel, categoryId: String) {
    val items = viewModel.getItems(categoryId)
    val categoryName = viewModel.getCategories().find { it.id == categoryId }?.name ?: "Items"
    
    AppScreenScaffold(title = categoryName, navController = navController) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            items(items) { item ->
                ItemCard(
                    emoji = "🍎", // Generic item emoji, you could map this
                    title = item.name,
                    onClick = {
                        navController.navigate("price/${item.id}")
                    }
                )
            }
        }
    }
}

// --- 5. Price Detail Screen ---

@Composable
fun PriceDetailScreen(navController: NavController, viewModel: AppViewModel, itemId: String) {
    val item = viewModel.getItem(itemId)
    
    if (item == null) {
        AppScreenScaffold(title = "Error", navController = navController) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Item not found.")
            }
        }
        return
    }

    // Collect the combined flow of (small_price, large_price)
    val prices by viewModel.getPricesForItem(item).collectAsState(initial = Pair(null, null))
    val smallPrice = prices.first?.price ?: "Loading..."
    val largePrice = prices.second?.price ?: "Loading..."

    AppScreenScaffold(title = item.name, navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassmorphicCard(onClick = { /* No action */ }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Single Item Price",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$smallPrice SDG",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Large Quantity Price",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$largePrice SDG",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}