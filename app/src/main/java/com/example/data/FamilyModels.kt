package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chores")
data class Chore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val points: Int,
    val assignedMember: String, // e.g., "Mum", "Dad", "Leo", "Mia"
    val dueDateString: String,   // e.g., "Today", "Tomorrow", "Thursday"
    val isCompleted: Boolean = false,
    val completedBy: String? = null,
    val completedAt: Long = 0L
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String,       // e.g., "1 loaf", "2 pints"
    val category: String,       // "Bakery", "Dairy & Eggs", "Fruit & Veg", "Household", "Pantry"
    val isPurchased: Boolean = false,
    val addedBy: String,
    val purchasedBy: String? = null,
    val imageUrl: String? = null,
    val productLink: String? = null,
    val shopName: String? = null
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val timeString: String,      // e.g., "17:30"
    val dateLabel: String,       // e.g., "Today", "Tomorrow", "Saturday"
    val dayOffset: Int,          // 0 = Today, 1 = Tomorrow, etc.
    val creatorName: String,     // e.g., "Dad"
    val category: String         // "Activity", "Meal", "Appointment", "School"
)

data class FamilyMember(
    val name: String,
    val role: String,            // e.g., "Parent", "Teenager", "Child", "Grandparent"
    val avatarEmoji: String,     // Emoji avatar representing them
    val points: Int = 0,
    val colorHex: String         // Primary brand colour for them
)

@Entity(tableName = "location_pins")
data class LocationPin(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberName: String,
    val avatarEmoji: String,
    val locationName: String,
    val sharedWith: String,      // "All" or comma-separated names e.g., "Dad, Mum"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishListItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberName: String,
    val name: String,
    val imageUrl: String? = null,
    val productLink: String? = null,
    val description: String? = null,
    val isCompleted: Boolean = false
)

