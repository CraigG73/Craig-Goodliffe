package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.CalendarEvent
import com.example.data.Chore
import com.example.data.FamilyDatabase
import com.example.data.FamilyMember
import com.example.data.FamilyRepository
import com.example.data.ShoppingItem
import com.example.data.LocationPin
import com.example.data.WishListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ActivityNotification(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val pointsGained: Int = 0,
    val memberEmoji: String,
    val memberName: String,
    val timestamp: Long = System.currentTimeMillis()
)

class FamilyViewModel(application: Application) : AndroidViewModel(application) {

    private val database: FamilyDatabase = Room.databaseBuilder(
        application,
        FamilyDatabase::class.java,
        "family_organiser_v1.db"
    ).fallbackToDestructiveMigration().build()

    private val repository = FamilyRepository(database)

    // Flow Streams from Database
    val chores: StateFlow<List<Chore>> = repository.allChores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.allShoppingItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val calendarEvents: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val locationPins: StateFlow<List<LocationPin>> = repository.allLocationPins
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val wishListItems: StateFlow<List<WishListItem>> = repository.allWishListItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Family Member States (Welcoming list of members with local score adjustments)
    private val initialMembers = listOf(
        FamilyMember(name = "Mum", role = "Parent", avatarEmoji = "👩‍🦰", points = 0, colorHex = "#FF4081"),
        FamilyMember(name = "Dad", role = "Parent", avatarEmoji = "👨", points = 0, colorHex = "#2196F3"),
        FamilyMember(name = "Leo", role = "Child (8 y/o)", avatarEmoji = "👦", points = 0, colorHex = "#FFEB3B"),
        FamilyMember(name = "Mia", role = "Teen (14 y/o)", avatarEmoji = "👧", points = 0, colorHex = "#4CAF50"),
        FamilyMember(name = "Gran", role = "Grandparent", avatarEmoji = "👵", points = 0, colorHex = "#9C27B0")
    )

    private val _members = MutableStateFlow(initialMembers)
    val members: StateFlow<List<FamilyMember>> = _members.asStateFlow()

    private val _activeMember = MutableStateFlow(initialMembers[0]) // Starts as Mum
    val activeMember: StateFlow<FamilyMember> = _activeMember.asStateFlow()

    // Sliding In-App Notifications
    private val _currentNotification = MutableStateFlow<ActivityNotification?>(null)
    val currentNotification: StateFlow<ActivityNotification?> = _currentNotification.asStateFlow()

    // Notification History log
    private val _notificationHistory = MutableStateFlow<List<ActivityNotification>>(emptyList())
    val notificationHistory: StateFlow<List<ActivityNotification>> = _notificationHistory.asStateFlow()

    // Selected Navigation Tab index
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            startRealTimeSyncSimulation()
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setActiveMember(member: FamilyMember) {
        _activeMember.value = member
    }

    fun addFamilyMember(member: FamilyMember) {
        val currentList = _members.value
        if (!currentList.any { it.name.equals(member.name, ignoreCase = true) }) {
            _members.value = currentList + member
            triggerNotification(
                message = "🎉 ${member.name} (${member.role}) joined the family group!",
                pointsGained = 0,
                emoji = member.avatarEmoji,
                name = member.name
            )
        }
    }

    // Add points locally when a family member does work (no longer active - points system removed)
    fun addPointsToMember(memberName: String, pointsAmt: Int) {
        // Points system removed. Do nothing.
    }

    // Actions triggers
    fun createChore(title: String, points: Int, assignedMember: String, dueDate: String) {
        viewModelScope.launch {
            val chore = Chore(
                title = title,
                points = points,
                assignedMember = assignedMember,
                dueDateString = dueDate
            )
            repository.insertChore(chore)
        }
    }

    fun completeChore(chore: Chore) {
        viewModelScope.launch {
            val completer = activeMember.value.name
            val updated = chore.copy(
                isCompleted = true,
                completedBy = completer,
                completedAt = System.currentTimeMillis()
            )
            repository.updateChore(updated)
            // Add points reward
            addPointsToMember(completer, chore.points)

            // Trigger notification immediately for active member action too
            triggerNotification(
                message = "$completer completed '${chore.title}'!",
                pointsGained = chore.points,
                emoji = activeMember.value.avatarEmoji,
                name = completer
            )
        }
    }

    fun deleteChore(id: Long) {
        viewModelScope.launch {
            repository.deleteChore(id)
        }
    }

    fun addShoppingItem(name: String, quantity: String, category: String, imageUrl: String? = null, productLink: String? = null, shopName: String? = null) {
        viewModelScope.launch {
            val item = ShoppingItem(
                name = name,
                quantity = quantity,
                category = category,
                addedBy = activeMember.value.name,
                imageUrl = imageUrl,
                productLink = productLink,
                shopName = shopName
            )
            repository.insertShoppingItem(item)
        }
    }

    fun toggleShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            val isCheckedNow = !item.isPurchased
            val activeName = activeMember.value.name
            val updated = item.copy(
                isPurchased = isCheckedNow,
                purchasedBy = if (isCheckedNow) activeName else null
            )
            repository.updateShoppingItem(updated)

            if (isCheckedNow) {
                // Reward 10 simple points for ticking off a grocery item!
                addPointsToMember(activeName, 10)
                triggerNotification(
                    message = "$activeName purchased '${item.name}'!",
                    pointsGained = 10,
                    emoji = activeMember.value.avatarEmoji,
                    name = activeName
                )
            }
        }
    }

    fun deleteShoppingItem(id: Long) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    fun addWishListItem(name: String, memberName: String, imageUrl: String? = null, productLink: String? = null, description: String? = null) {
        viewModelScope.launch {
            val item = WishListItem(
                name = name,
                memberName = memberName,
                imageUrl = if (imageUrl.isNull_or_blank()) null else imageUrl,
                productLink = if (productLink.isNull_or_blank()) null else productLink,
                description = if (description.isNull_or_blank()) null else description
            )
            repository.insertWishListItem(item)
            triggerNotification(
                message = "${activeMember.value.name} added '$name' to their wishlist!",
                pointsGained = 0,
                emoji = activeMember.value.avatarEmoji,
                name = activeMember.value.name
            )
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    fun toggleWishListItem(item: WishListItem) {
        viewModelScope.launch {
            val updated = item.copy(isCompleted = !item.isCompleted)
            repository.updateWishListItem(updated)
        }
    }

    fun deleteWishListItem(id: Long) {
        viewModelScope.launch {
            repository.deleteWishListItem(id)
        }
    }

    fun createCalendarEvent(title: String, description: String, timeString: String, dateLabel: String, dayOffset: Int, category: String) {
        viewModelScope.launch {
            val event = CalendarEvent(
                title = title,
                description = description,
                timeString = timeString,
                dateLabel = dateLabel,
                dayOffset = dayOffset,
                creatorName = activeMember.value.name,
                category = category
            )
            repository.insertCalendarEvent(event)
        }
    }

    fun deleteCalendarEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(id)
        }
    }

    // High fidelity real-time sync simulation
    private fun startRealTimeSyncSimulation() {
        viewModelScope.launch {
            delay(15000) // wait initially
            while (true) {
                val delayTime = (12000..25000).random().toLong()
                delay(delayTime)

                // Select a simulated actor (any member who is not current active member)
                val currentActive = activeMember.value.name
                val candidates = _members.value.filter { it.name != currentActive }
                if (candidates.isEmpty()) continue
                val actor = candidates.random()

                // Decide which event type to simulate (0 = Chore done, 1 = Shopping add, 2 = Calendar event)
                when ((0..2).random()) {
                    0 -> {
                        // Complete a random uncompleted chore
                        val currentChores = chores.value.filter { !it.isCompleted }
                        if (currentChores.isNotEmpty()) {
                            val targetChore = currentChores.random()
                            val updated = targetChore.copy(
                                isCompleted = true,
                                completedBy = actor.name,
                                completedAt = System.currentTimeMillis()
                            )
                            repository.updateChore(updated)
                            addPointsToMember(actor.name, targetChore.points)
                            triggerNotification(
                                message = "${actor.name} completed '${targetChore.title}'!",
                                pointsGained = targetChore.points,
                                emoji = actor.avatarEmoji,
                                name = actor.name
                            )
                        } else {
                            // If no chores, can simulate the actor creating a chore!
                            val choreTitles = listOf(
                                "Vacuum the living room rugs",
                                "Polish the kitchen taps",
                                "Wipe down the dining table",
                                "Water the indoor house plants",
                                "Wipe down kitchen cabinets",
                                "Refill the hand soaps"
                            )
                            val title = choreTitles.random()
                            val pts = listOf(20, 30, 40, 50).random()
                            val days = listOf("Today", "Tomorrow", "Saturday").random()
                            createSimulatedChore(title, pts, actor.name, days)
                            triggerNotification(
                                message = "${actor.name} added to-do: '$title'",
                                pointsGained = 0,
                                emoji = actor.avatarEmoji,
                                name = actor.name
                            )
                        }
                    }
                    1 -> {
                        // Add a typical British shopping list item
                        val itemsCollection = listOf(
                            Pair("English muffins", "1 pack"),
                            Pair("Double cream", "1 tub"),
                            Pair("Cheddar cheese", "250g"),
                            Pair("Heinz baked beans", "2 tins"),
                            Pair("Digestive biscuits", "1 packet"),
                            Pair("Yorkshire puddings", "1 pack"),
                            Pair("Washing up sponges", "3 pack"),
                            Pair("Earl Grey tea", "1 pack (50)")
                        )
                        val select = itemsCollection.random()
                        val categories = listOf("Bakery", "Dairy & Eggs", "Dairy & Eggs", "Pantry", "Pantry", "Pantry", "Household", "Pantry")
                        val index = itemsCollection.indexOf(select)
                        val category = categories[index]

                        val item = ShoppingItem(
                            name = select.first,
                            quantity = select.second,
                            category = category,
                            addedBy = actor.name
                        )
                        repository.insertShoppingItem(item)

                        triggerNotification(
                            message = "${actor.name} added '${select.first}' to the shopping list.",
                            pointsGained = 0,
                            emoji = actor.avatarEmoji,
                            name = actor.name
                        )
                    }
                    2 -> {
                        // Add a calendar event
                        val familyEventTitles = listOf(
                            Triple("Family Film Night (Paddington)", "With popcorn & treats!", "19:00"),
                            Triple("Grandma's Afternoon Tea", "Gran is popping round", "15:30"),
                            Triple("Mia's GCSE Maths Review", "Study group online", "16:00"),
                            Triple("Dog Training in local park", "Practice 'heel' commands", "11:00"),
                            Triple("Weekly Grocery Delivery", "Expect Sainsbury's van!", "09:00")
                        )
                        val select = familyEventTitles.random()
                        val days = listOf("Tomorrow", "Saturday", "Sunday").random()
                        val dayOffset = if (days == "Tomorrow") 1 else if (days == "Saturday") 4 else 5

                        val event = CalendarEvent(
                            title = select.first,
                            description = select.second,
                            timeString = select.third,
                            dateLabel = days,
                            dayOffset = dayOffset,
                            creatorName = actor.name,
                            category = if (select.first.contains("Maths")) "School" else if (select.first.contains("Grocery")) "Meal" else "Activity"
                        )
                        repository.insertCalendarEvent(event)

                        triggerNotification(
                            message = "${actor.name} scheduled '${select.first}' for $days.",
                            pointsGained = 0,
                            emoji = actor.avatarEmoji,
                            name = actor.name
                        )
                    }
                }
            }
        }
    }

    private fun createSimulatedChore(title: String, points: Int, assigned: String, due: String) {
        viewModelScope.launch {
            repository.insertChore(Chore(title = title, points = points, assignedMember = assigned, dueDateString = due))
        }
    }

    private fun triggerNotification(message: String, pointsGained: Int, emoji: String, name: String) {
        viewModelScope.launch {
            val notif = ActivityNotification(
                message = message,
                pointsGained = 0,
                memberEmoji = emoji,
                memberName = name
            )
            _currentNotification.value = notif
            _notificationHistory.value = (listOf(notif) + _notificationHistory.value).take(20)

            // Auto-hide the popup notification toast after 6 seconds
            delay(6000)
            if (_currentNotification.value?.id == notif.id) {
                _currentNotification.value = null
            }
        }
    }

    fun dismissCurrentNotification() {
        _currentNotification.value = null
    }

    fun shareLocation(locationName: String, sharedWith: List<String>) {
        viewModelScope.launch {
            val active = activeMember.value
            val sharedWithStr = if (sharedWith.isEmpty()) "All" else sharedWith.joinToString(", ")
            val pin = LocationPin(
                memberName = active.name,
                avatarEmoji = active.avatarEmoji,
                locationName = locationName,
                sharedWith = sharedWithStr,
                timestamp = System.currentTimeMillis()
            )
            repository.insertLocationPin(pin)

            val sharedNamesLabel = if (sharedWithStr == "All") "All Members" else sharedWithStr
            triggerNotification(
                message = "${active.name} pinned location: '$locationName' (shared with $sharedNamesLabel)",
                pointsGained = 15,
                emoji = active.avatarEmoji,
                name = active.name
            )
            addPointsToMember(active.name, 15)
        }
    }

    fun deleteLocationPin(id: Long) {
        viewModelScope.launch {
            repository.deleteLocationPin(id)
        }
    }

    // Reset stats or activities helper (useful for cleanups)
    fun resetActivityLog() {
        _notificationHistory.value = emptyList()
    }
}
