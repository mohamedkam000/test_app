package com.test.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.test.app.screens.CategoryScreen
import com.test.app.screens.ItemListScreen
import com.test.app.screens.MarketScreen
import com.test.app.screens.PriceDetailScreen
import com.test.app.screens.StateScreen
import com.test.app.ui.theme.PriceViewerTheme
import com.test.app.viewmodel.AppViewModel
import com.test.app.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            PriceViewerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Get the app instance for the ViewModel factory
                    val application = LocalContext.current.applicationContext as android.app.Application
                    // Create the ViewModel, scoped to the app's lifecycle
                    val viewModel: AppViewModel = viewModel(
                        factory = AppViewModelFactory(application)
                    )
                    
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "states") {
        // 1. State Screen
        composable("states") {
            StateScreen(navController = navController, viewModel = viewModel)
        }

        // 2. Market Screen
        composable(
            route = "markets/{stateId}",
            arguments = listOf(navArgument("stateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val stateId = backStackEntry.arguments?.getString("stateId") ?: ""
            MarketScreen(
                navController = navController,
                viewModel = viewModel,
                stateId = stateId
            )
        }

        // 3. Category Screen
        composable(
            route = "categories/{marketId}",
             arguments = listOf(navArgument("marketId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Note: marketId isn't used by the VM, but it's part of the flow
            val marketId = backStackEntry.arguments?.getString("marketId") ?: ""
            CategoryScreen(
                navController = navController,
                viewModel = viewModel,
                marketId = marketId
            )
        }
        
        // 4. Item List Screen
        composable(
            route = "items/{categoryId}",
             arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            ItemListScreen(
                navController = navController,
                viewModel = viewModel,
                categoryId = categoryId
            )
        }
        
        // 5. Price Detail Screen
        composable(
            route = "price/{itemId}",
             arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            PriceDetailScreen(
                navController = navController,
                viewModel = viewModel,
                itemId = itemId
            )
        }
    }
}