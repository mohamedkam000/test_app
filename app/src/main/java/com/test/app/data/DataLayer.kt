package com.test.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinxserialization.asConverterFactory
import retrofit2.http.GET

// --- 1. DATA MODELS ---

// Hardcoded models for navigation
data class AppState(val id: String, val name: String, val iconEmoji: String)
data class AppMarket(val id: String, val name: String, val stateId: String)
data class AppCategory(val id: String, val name: String, val iconEmoji: String)
data class AppItem(
    val id: String,
    val name: String,
    val categoryId: String,
    val jsonKeySmall: String, // Key to look up in JSON (e.g., "oranges_s")
    val jsonKeyLarge: String  // Key to look up in JSON (e.g., "oranges_m")
)

// Model for cached price data from the network
@Entity(tableName = "price_cache")
@Serializable
data class PriceData(
    @PrimaryKey val id: String,
    val price: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- 2. STATIC (HARDCODED) DATA ---

object StaticData {
    val states = listOf(
        AppState(id = "kh", name = "Khartoum", iconEmoji = "🏙️"),
        AppState(id = "rs", name = "Red Sea", iconEmoji = "🌊"),
        AppState(id = "rn", name = "River Nile", iconEmoji = " Nile")
    )

    val markets = listOf(
        AppMarket(id = "kh_central", name = "Khartoum Central", stateId = "kh"),
        AppMarket(id = "kh_omdurman", name = "Omdurman Souk", stateId = "kh"),
        AppMarket(id = "rs_port", name = "Port Sudan Market", stateId = "rs"),
        AppMarket(id = "rn_atbara", name = "Atbara Market", stateId = "rn")
    )

    val categories = listOf(
        AppCategory(id = "fruits", name = "Fruits", iconEmoji = "🍓"),
        AppCategory(id = "meat", name = "Meat", iconEmoji = "🥩"),
        AppCategory(id = "beverages", name = "Beverages", iconEmoji = "🥤")
    )

    val items = listOf(
        AppItem("oranges", "Oranges", "fruits", "oranges_s", "oranges_m"),
        AppItem("apples", "Apples", "fruits", "apples_s", "apples_m"),
        AppItem("grapes", "Grapes", "fruits", "grapes_s", "grapes_m"),
        // Add more items for other categories
    )
}

// --- 3. NETWORK SERVICE (Retrofit) ---

interface ApiService {
    @GET("mohamedkam000/prices/main/data.json")
    suspend fun getPrices(): Map<String, String> // Fetches the entire JSON as a map
}

object RetrofitClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/")
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}

// --- 4. CACHE (Room Database) ---

@Dao
interface PriceDao {
    @Query("SELECT * FROM price_cache WHERE id = :key")
    fun getPrice(key: String): Flow<PriceData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<PriceData>)
}

@Database(entities = [PriceData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "price_viewer_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- 5. REPOSITORY ---

class PriceRepository(private val apiService: ApiService, private val priceDao: PriceDao) {

    // --- Hardcoded data access ---
    fun getStates(): List<AppState> = StaticData.states
    fun getMarketsForState(stateId: String): List<AppMarket> =
        StaticData.markets.filter { it.stateId == stateId }
    fun getCategories(): List<AppCategory> = StaticData.categories
    fun getItemsForCategory(categoryId: String): List<AppItem> =
        StaticData.items.filter { it.categoryId == categoryId }
    fun getItem(itemId: String): AppItem? =
        StaticData.items.find { it.id == itemId }

    // --- Price data access (with cache) ---

    // Get a specific price from cache
    fun getPriceFromCache(key: String): Flow<PriceData?> = priceDao.getPrice(key)

    // Fetch from network and update cache
    suspend fun refreshPrices() {
        try {
            val priceMap = apiService.getPrices()
            val priceEntities = priceMap.map { (key, value) ->
                PriceData(id = key, price = value)
            }
            priceDao.insertPrices(priceEntities)
        } catch (e: Exception) {
            // Handle network error (e.g., log it)
            e.printStackTrace()
        }
    }
}