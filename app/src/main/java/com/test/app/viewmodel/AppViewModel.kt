package com.test.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.test.app.data.AppDatabase
import com.test.app.data.AppItem
import com.test.app.data.PriceData
import com.test.app.data.PriceRepository
import com.test.app.data.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Simple factory to create the ViewModel with its dependencies
class AppViewModelFactory(private val application: Application) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PriceRepository

    init {
        val priceDao = AppDatabase.getDatabase(application).priceDao()
        repository = PriceRepository(RetrofitClient.api, priceDao)

        // Trigger a background refresh on init
        viewModelScope.launch {
            repository.refreshPrices()
        }
    }

    // --- Functions to get static data ---
    fun getStates() = repository.getStates()
    fun getMarkets(stateId: String) = repository.getMarketsForState(stateId)
    fun getCategories() = repository.getCategories()
    fun getItems(categoryId: String) = repository.getItemsForCategory(categoryId)
    fun getItem(itemId: String) = repository.getItem(itemId)

    // --- Functions to get dynamic price data ---

    /**
     * Gets the price for a specific item (small and large) from the cache.
     * This combines two flows into one Pair(small_price, large_price).
     */
    fun getPricesForItem(item: AppItem): Flow<Pair<PriceData?, PriceData?>> {
        val smallPriceFlow = repository.getPriceFromCache(item.jsonKeySmall)
        val largePriceFlow = repository.getPriceFromCache(item.jsonKeyLarge)

        return combine(smallPriceFlow, largePriceFlow) { small, large ->
            Pair(small, large)
        }
    }

    // Function to manually trigger a refresh from the UI
    fun triggerRefresh() {
        viewModelScope.launch {
            repository.refreshPrices()
        }
    }
}