package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoreDao {
    @Query("SELECT * FROM chores ORDER BY id DESC")
    fun getAllChores(): Flow<List<Chore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChore(chore: Chore)

    @Update
    suspend fun updateChore(chore: Chore)

    @Query("DELETE FROM chores WHERE id = :id")
    suspend fun deleteChore(id: Long)

    @Query("DELETE FROM chores")
    suspend fun clearAll()
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, id DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY dayOffset ASC, timeString ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()
}

@Dao
interface LocationPinDao {
    @Query("SELECT * FROM location_pins ORDER BY timestamp DESC")
    fun getAllLocationPins(): Flow<List<LocationPin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPin(pin: LocationPin)

    @Query("DELETE FROM location_pins WHERE id = :id")
    suspend fun deleteLocationPin(id: Long)

    @Query("DELETE FROM location_pins")
    suspend fun clearAll()
}

@Dao
interface WishListDao {
    @Query("SELECT * FROM wishlist_items ORDER BY id DESC")
    fun getAllWishListItems(): Flow<List<WishListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WishListItem)

    @Update
    suspend fun updateItem(item: WishListItem)

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM wishlist_items")
    suspend fun clearAll()
}

@Database(entities = [Chore::class, ShoppingItem::class, CalendarEvent::class, LocationPin::class, WishListItem::class], version = 4, exportSchema = false)
abstract class FamilyDatabase : RoomDatabase() {
    abstract fun choreDao(): ChoreDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun calendarDao(): CalendarDao
    abstract fun locationPinDao(): LocationPinDao
    abstract fun wishListDao(): WishListDao
}
