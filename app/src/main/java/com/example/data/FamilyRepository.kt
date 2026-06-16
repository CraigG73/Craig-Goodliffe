package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FamilyRepository(private val database: FamilyDatabase) {

    private val choreDao = database.choreDao()
    private val shoppingDao = database.shoppingDao()
    private val calendarDao = database.calendarDao()
    private val locationPinDao = database.locationPinDao()
    private val wishListDao = database.wishListDao()

    val allChores: Flow<List<Chore>> = choreDao.getAllChores()
    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()
    val allEvents: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()
    val allLocationPins: Flow<List<LocationPin>> = locationPinDao.getAllLocationPins()
    val allWishListItems: Flow<List<WishListItem>> = wishListDao.getAllWishListItems()

    // Location Pins
    suspend fun insertLocationPin(pin: LocationPin) = locationPinDao.insertLocationPin(pin)
    suspend fun deleteLocationPin(id: Long) = locationPinDao.deleteLocationPin(id)
    suspend fun clearAllLocationPins() = locationPinDao.clearAll()

    // Chores
    suspend fun insertChore(chore: Chore) = choreDao.insertChore(chore)
    suspend fun updateChore(chore: Chore) = choreDao.updateChore(chore)
    suspend fun deleteChore(id: Long) = choreDao.deleteChore(id)

    // Shopping List
    suspend fun insertShoppingItem(item: ShoppingItem) = shoppingDao.insertItem(item)
    suspend fun updateShoppingItem(item: ShoppingItem) = shoppingDao.updateItem(item)
    suspend fun deleteShoppingItem(id: Long) = shoppingDao.deleteItem(id)

    // Calendar
    suspend fun insertCalendarEvent(event: CalendarEvent) = calendarDao.insertEvent(event)
    suspend fun deleteCalendarEvent(id: Long) = calendarDao.deleteEvent(id)

    // Wishlist
    suspend fun insertWishListItem(item: WishListItem) = wishListDao.insertItem(item)
    suspend fun updateWishListItem(item: WishListItem) = wishListDao.updateItem(item)
    suspend fun deleteWishListItem(id: Long) = wishListDao.deleteItem(id)
    suspend fun clearAllWishListItems() = wishListDao.clearAll()

    // Prepopulate with lovely UK family data if database is brand new
    suspend fun prepopulateIfEmpty() {
        // We evaluate empty states sequentially using .first()
        val choresEmpty = choreDao.getAllChores().first().isEmpty()
        if (choresEmpty) {
            val starterChores = listOf(
                Chore(title = "Mow the back garden lawn", points = 0, assignedMember = "Dad", dueDateString = "Today"),
                Chore(title = "Separate & run the dark laundry load", points = 0, assignedMember = "Mum", dueDateString = "Today"),
                Chore(title = "Empty the dishwasher", points = 0, assignedMember = "Leo", dueDateString = "Tomorrow"),
                Chore(title = "Feed the family dog (Barnaby)", points = 0, assignedMember = "Mia", dueDateString = "Today"),
                Chore(title = "Dust off the skirting boards", points = 0, assignedMember = "Mia", dueDateString = "Friday"),
                Chore(title = "Take out the recycling bins", points = 0, assignedMember = "Leo", dueDateString = "Tomorrow")
            )
            for (chore in starterChores) {
                choreDao.insertChore(chore)
            }
        }

        val shoppingEmpty = shoppingDao.getAllShoppingItems().first().isEmpty()
        if (shoppingEmpty) {
            val starterShopping = listOf(
                ShoppingItem(name = "Semi-skimmed milk", quantity = "2 pints", category = "Dairy & Eggs", addedBy = "Mum", shopName = "Tesco", productLink = "https://www.tesco.com/groceries/en-GB/products/251211111"),
                ShoppingItem(name = "English crumpets", quantity = "1 pack (6)", category = "Bakery", addedBy = "Leo", shopName = "Aldi", productLink = "https://www.aldi.co.uk"),
                ShoppingItem(name = "Yorkshire tea bags", quantity = "1 box (80)", category = "Pantry", addedBy = "Dad", shopName = "Morrisons", productLink = "https://groceries.morrisons.com"),
                ShoppingItem(name = "Strawberries", quantity = "1 punnet", category = "Fruit & Veg", addedBy = "Mia", shopName = "Lidl"),
                ShoppingItem(name = "Washing up liquid", quantity = "1 bottle", category = "Household", addedBy = "Mum", shopName = "Home Bargains"),
                ShoppingItem(name = "Sourdough bread", quantity = "1 loaf", category = "Bakery", addedBy = "Dad", shopName = "M&S", productLink = "https://www.marksandspencer.com")
            )
            for (item in starterShopping) {
                shoppingDao.insertItem(item)
            }
        }

        val calendarEmpty = calendarDao.getAllEvents().first().isEmpty()
        if (calendarEmpty) {
            val starterEvents = listOf(
                CalendarEvent(
                    title = "Leo's Football Match",
                    description = "Playing against Oakfield FC. Bring shin pads and water bottle!",
                    timeString = "10:00",
                    dateLabel = "Saturday",
                    dayOffset = 4,
                    creatorName = "Dad",
                    category = "Activity"
                ),
                CalendarEvent(
                    title = "Sunday Family Roast Dinner",
                    description = "Traditional slow-cooked roast beef with potatoes and gravy. Yorkshire puddings are mandatory!",
                    timeString = "15:00",
                    dateLabel = "Sunday",
                    dayOffset = 5,
                    creatorName = "Mum",
                    category = "Meal"
                ),
                CalendarEvent(
                    title = "Emily's Dental Checkup",
                    description = "Annual routing dental checkup. High Street Dental Practice.",
                    timeString = "14:15",
                    dateLabel = "Tomorrow",
                    dayOffset = 1,
                    creatorName = "Mum",
                    category = "Appointment"
                ),
                CalendarEvent(
                    title = "Parent-Teacher Evening (Leo)",
                    description = "Meet with Mr. Harrison to review school progress.",
                    timeString = "17:30",
                    dateLabel = "Thursday",
                    dayOffset = 2,
                    creatorName = "Dad",
                    category = "School"
                )
            )
            for (event in starterEvents) {
                calendarDao.insertEvent(event)
            }
        }

        val pinsEmpty = locationPinDao.getAllLocationPins().first().isEmpty()
        if (pinsEmpty) {
            val starterPins = listOf(
                LocationPin(
                    memberName = "Mum",
                    avatarEmoji = "👩‍🦰",
                    locationName = "🏡 At Home (Working remotely)",
                    sharedWith = "All",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                LocationPin(
                    memberName = "Dad",
                    avatarEmoji = "👨",
                    locationName = "🛒 Sainsbury's Supermarket",
                    sharedWith = "Mum, Mia",
                    timestamp = System.currentTimeMillis() - 1200000
                ),
                LocationPin(
                    memberName = "Leo",
                    avatarEmoji = "👦",
                    locationName = "🏫 School (In class)",
                    sharedWith = "All",
                    timestamp = System.currentTimeMillis() - 7200000
                )
            )
            for (pin in starterPins) {
                locationPinDao.insertLocationPin(pin)
            }
        }

        val wishlistEmpty = wishListDao.getAllWishListItems().first().isEmpty()
        if (wishlistEmpty) {
            val starterWishList = listOf(
                WishListItem(
                    memberName = "Mum",
                    name = "Luxury Silk Sleep Mask",
                    imageUrl = "https://images.unsplash.com/photo-1614859325968-37233eff594e?auto=format&fit=crop&q=80&w=250",
                    productLink = "https://www.boots.com",
                    description = "Hypoallergenic, 100% pure mulberry silk. Great for deep sleep!"
                ),
                WishListItem(
                    memberName = "Dad",
                    name = "AeroPress Go Coffee Maker",
                    imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&q=80&w=250",
                    productLink = "https://www.amazon.co.uk",
                    description = "Compact travel coffee press. Perfect for mornings at the office or camping."
                ),
                WishListItem(
                    memberName = "Leo",
                    name = "LEGO Technic Monster Truck",
                    imageUrl = "https://images.unsplash.com/photo-1585366119957-e5733f3000cd?auto=format&fit=crop&q=80&w=250",
                    productLink = "https://www.argos.co.uk",
                    description = "The blue and neon green pull-back jam vehicle model!"
                ),
                WishListItem(
                    memberName = "Mia",
                    name = "Soft Over-Ear Headphones",
                    imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=250",
                    productLink = "https://www.amazon.co.uk",
                    description = "Matte black with extra padded ear cups for studying study playlists."
                )
            )
            for (item in starterWishList) {
                wishListDao.insertItem(item)
            }
        }
    }
}
