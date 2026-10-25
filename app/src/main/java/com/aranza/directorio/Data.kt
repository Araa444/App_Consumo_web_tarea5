package com.aranza.directorio

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.*
import androidx.room.Query
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query as RetrofitQuery
import java.io.IOException
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("users")
    suspend fun users(): List<User>

    @GET("posts")
    suspend fun posts(
        @RetrofitQuery("userId") userId: Int
    ): List<Post>
}

// Guarda el usuario completo: empresa, dirección y contacto.
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Int,
    val userJson: String
)

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY id")
    fun observe(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(value: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun delete(id: Int)
}

@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favorites(): FavoriteDao
}

class Repository(
    private val api: ApiService,
    private val dao: FavoriteDao
) {
    private val gson = Gson()

    val favorites: Flow<List<User>> = dao.observe().map { rows ->
        rows.map { row ->
            gson.fromJson(row.userJson, User::class.java)
        }
    }

    suspend fun users(): List<User> = api.users()

    suspend fun posts(id: Int): List<Post> = api.posts(id)

    suspend fun save(user: User) {
        dao.save(
            FavoriteEntity(
                id = user.id,
                userJson = gson.toJson(user)
            )
        )
    }

    suspend fun delete(id: Int) {
        dao.delete(id)
    }
}

class NoInternet : IOException()

class NetworkStatus(context: Context) {

    private val manager =
        context.getSystemService(ConnectivityManager::class.java)

    fun online(): Boolean {
        val caps = manager.getNetworkCapabilities(
            manager.activeNetwork
        ) ?: return false

        return caps.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }
}

object Graph {

    @Volatile
    private var instance: Repository? = null

    fun repository(context: Context): Repository =
        instance ?: synchronized(this) {
            instance ?: run {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "directorio.db"
                ).build()

                val client = OkHttpClient.Builder()
                    .callTimeout(25, TimeUnit.SECONDS)
                    .build()

                val api = Retrofit.Builder()
                    .baseUrl("https://jsonplaceholder.typicode.com/")
                    .client(client)
                    .addConverterFactory(
                        GsonConverterFactory.create()
                    )
                    .build()
                    .create(ApiService::class.java)

                Repository(api, db.favorites()).also {
                    instance = it
                }
            }
        }
}