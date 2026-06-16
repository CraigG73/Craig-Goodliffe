package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalendarEvent
import com.example.data.Chore
import com.example.data.FamilyMember
import com.example.data.LocationPin
import com.example.data.ShoppingItem
import java.util.Calendar

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    viewModel: FamilyViewModel,
    modifier: Modifier = Modifier
) {
    val chores by viewModel.chores.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val activeMember by viewModel.activeMember.collectAsStateWithLifecycle()
    val currentNotification by viewModel.currentNotification.collectAsStateWithLifecycle()
    val notificationHistory by viewModel.notificationHistory.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val locationPins by viewModel.locationPins.collectAsStateWithLifecycle()
    val wishListItems by viewModel.wishListItems.collectAsStateWithLifecycle()

    var showProfileSwitcher by remember { mutableStateOf(false) }
    var showNotificationHistoryDialog by remember { mutableStateOf(false) }
    var showAddChoreDialog by remember { mutableStateOf(false) }
    var showAddCalendarDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showAddWishListDialog by remember { mutableStateOf(false) }
    var showInviteMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("main_screen_layout"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                FamilyTopHeader(
                    activeMember = activeMember,
                    members = members,
                    onSwitcherClick = { showProfileSwitcher = true },
                    onNotificationBellClick = { showNotificationHistoryDialog = true },
                    onLocationClick = { showLocationDialog = true }
                )
            }
        },
        bottomBar = {
            FamilyNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index -> viewModel.selectTab(index) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFEF7FF))
        ) {
            when (selectedTab) {
                0 -> ChoresTab(
                    chores = chores,
                    activeMember = activeMember,
                    members = members,
                    locationPins = locationPins,
                    onDeletePin = { viewModel.deleteLocationPin(it) },
                    onComplete = { viewModel.completeChore(it) },
                    onDelete = { viewModel.deleteChore(it) },
                    onAddClick = { showAddChoreDialog = true }
                )
                1 -> ShoppingTab(
                    shoppingItems = shoppingItems,
                    activeMember = activeMember,
                    onToggle = { viewModel.toggleShoppingItem(it) },
                    onDelete = { viewModel.deleteShoppingItem(it) },
                    onAddItem = { name, qty, cat, imgUrl, prodLink, shop ->
                        viewModel.addShoppingItem(name, qty, cat, imgUrl, prodLink, shop)
                    }
                )
                2 -> CalendarTab(
                    events = calendarEvents,
                    onDelete = { viewModel.deleteCalendarEvent(it) },
                    onAddClick = { showAddCalendarDialog = true }
                )
                3 -> WishListTab(
                    wishListItems = wishListItems,
                    activeMember = activeMember,
                    members = members,
                    onToggle = { viewModel.toggleWishListItem(it) },
                    onDelete = { viewModel.deleteWishListItem(it) },
                    onAddClick = { showAddWishListDialog = true }
                )
            }

            AnimatedVisibility(
                visible = currentNotification != null,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                currentNotification?.let { notification ->
                    SyncNotificationBanner(
                        notification = notification,
                        onDismiss = { viewModel.dismissCurrentNotification() }
                    )
                }
            }
        }
    }

    if (showProfileSwitcher) {
        ProfileSwitcherDialog(
            members = members,
            activeMember = activeMember,
            onSelectMember = { member ->
                viewModel.setActiveMember(member)
                showProfileSwitcher = false
            },
            onInviteClick = {
                showInviteMemberDialog = true
                showProfileSwitcher = false
            },
            onDismiss = { showProfileSwitcher = false }
        )
    }

    if (showAddChoreDialog) {
        AddChoreDialog(
            members = members,
            onConfirm = { title, pts, assigned, due ->
                viewModel.createChore(title, pts, assigned, due)
                showAddChoreDialog = false
            },
            onDismiss = { showAddChoreDialog = false }
        )
    }

    if (showAddWishListDialog) {
        AddWishListItemDialog(
            members = members,
            activeMember = activeMember,
            onDismiss = { showAddWishListDialog = false },
            onConfirm = { name, member, imgUrl, prodLink, desc ->
                viewModel.addWishListItem(name, member, imgUrl, prodLink, desc)
                showAddWishListDialog = false
            }
        )
    }

    if (showInviteMemberDialog) {
        InviteMemberDialog(
            onDismiss = { showInviteMemberDialog = false },
            onConfirmInviteJoined = { newMember ->
                viewModel.addFamilyMember(newMember)
                viewModel.setActiveMember(newMember)
                showInviteMemberDialog = false
            }
        )
    }

    if (showNotificationHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationHistoryDialog = false },
            confirmButton = {
                TextButton(onClick = { showNotificationHistoryDialog = false }) {
                    Text("Dismiss", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.resetActivityLog()
                }) {
                    Text("Clear Log", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = "Family Activity Log 🔔",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF211F26)
                )
            },
            text = {
                if (notificationHistory.isEmpty()) {
                    Text("No recent announcements or actions recorded yet.", fontSize = 13.sp, color = Color(0xFF49454F))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(notificationHistory) { alert ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Text(text = alert.memberEmoji, fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.message,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D1B20)
                                    )
                                    val timeStr = remember(alert.timestamp) {
                                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp))
                                    }
                                    Text(
                                        text = timeStr,
                                        fontSize = 10.sp,
                                        color = Color(0xFF49454F).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    if (showAddCalendarDialog) {
        AddCalendarEventDialog(
            onConfirm = { title, desc, time, date, dayOffset, category ->
                viewModel.createCalendarEvent(title, desc, time, date, dayOffset, category)
                showAddCalendarDialog = false
            },
            onDismiss = { showAddCalendarDialog = false }
        )
    }

    if (showLocationDialog) {
        ShareLocationDialog(
            members = members,
            activeMember = activeMember,
            onConfirm = { locationText, targetMembers ->
                viewModel.shareLocation(locationText, targetMembers)
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
fun FamilyTopHeader(
    activeMember: FamilyMember,
    members: List<FamilyMember>,
    onSwitcherClick: () -> Unit,
    onNotificationBellClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    val todayDateString = remember {
        val cal = Calendar.getInstance()
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Today"
        }
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val month = when (cal.get(Calendar.MONTH)) {
            Calendar.JANUARY -> "June" // Wait, let's use actual month but long name
            Calendar.FEBRUARY -> "February"
            Calendar.MARCH -> "March"
            Calendar.APRIL -> "April"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "June"
            Calendar.JULY -> "July"
            Calendar.AUGUST -> "August"
            Calendar.SEPTEMBER -> "September"
            Calendar.OCTOBER -> "October"
            Calendar.NOVEMBER -> "November"
            Calendar.DECEMBER -> "December"
            else -> ""
        }
        "$dayOfWeek, $dayOfMonth $month"
    }

    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = Color(0xFFFEF7FF),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSwitcherClick() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "The Family Hub",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF211F26),
                        letterSpacing = (-0.5).sp
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Switch Member",
                        tint = Color(0xFF49454F),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = todayDateString,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Member avatar pile overlapping
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-12).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSwitcherClick() }
                ) {
                    members.take(4).forEachIndexed { index, member ->
                        val avatarBg = when(index % 4) {
                            0 -> Color(0xFFD0BCFF)
                            1 -> Color(0xFFCCC2DC)
                            2 -> Color(0xFFEFB8C8)
                            else -> Color(0xFF6750A4)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(avatarBg)
                                .border(2.dp, Color(0xFFFEF7FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.avatarEmoji,
                                fontSize = 16.sp
                            )
                        }
                    }
                    if (members.size > 4) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6750A4))
                                .border(2.dp, Color(0xFFFEF7FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${members.size - 4}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }



                IconButton(
                    onClick = onLocationClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3EDF7))
                        .testTag("share_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Share Location/Pin Point",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onNotificationBellClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3EDF7))
                        .testTag("notification_log_bell")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Sync Logs",
                        tint = Color(0xFF49454F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF3EDF7),
        tonalElevation = 0.dp,
        modifier = Modifier.testTag("family_bottom_navigation")
    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1D192B),
            selectedTextColor = Color(0xFF1D192B),
            unselectedIconColor = Color(0xFF49454F),
            unselectedTextColor = Color(0xFF49454F),
            indicatorColor = Color(0xFFE8DEF8)
        )

        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("To Do") },
            colors = navItemColors,
            icon = {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "To Do"
                )
            }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("Shopping") },
            colors = navItemColors,
            icon = {
                Icon(
                    imageVector = if (selectedTab == 1) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                    contentDescription = "Shopping"
                )
            }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text("Calendar") },
            colors = navItemColors,
            icon = {
                Icon(
                    imageVector = if (selectedTab == 2) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                    contentDescription = "Calendar"
                )
            }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            label = { Text("Wish List") },
            colors = navItemColors,
            icon = {
                Icon(
                    imageVector = if (selectedTab == 3) Icons.Filled.CardGiftcard else Icons.Outlined.CardGiftcard,
                    contentDescription = "Wish List"
                )
            }
        )
    }
}

@Composable
fun SyncNotificationBanner(
    notification: ActivityNotification,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
        border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDismiss() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Text(text = notification.memberEmoji, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF21005D))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE SYNC",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "just now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D).copy(alpha = 0.7f)
                    )
                }

                Text(
                     text = notification.message,
                     fontSize = 14.sp,
                     fontWeight = FontWeight.Bold,
                     color = Color(0xFF21005D)
                )
            }



            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF21005D),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChoresTab(
    chores: List<Chore>,
    activeMember: FamilyMember,
    members: List<FamilyMember>,
    locationPins: List<LocationPin>,
    onDeletePin: (Long) -> Unit,
    onComplete: (Chore) -> Unit,
    onDelete: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All To Do") }
    val filteredChores = remember(chores, selectedFilter, activeMember) {
        when (selectedFilter) {
            "My To Do" -> chores.filter { it.assignedMember == activeMember.name && !it.isCompleted }
            "Not Done" -> chores.filter { !it.isCompleted }
            "Completed" -> chores.filter { it.isCompleted }
            else -> chores
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (locationPins.isNotEmpty()) {
                Text(
                    text = "Family Pin Points 📍",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6750A4),
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    items(locationPins) { pin ->
                        val isMe = pin.memberName == activeMember.name
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) Color(0xFFE8DEF8) else Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isMe) Color(0xFF21005D).copy(alpha = 0.2f) else Color(0xFFCAC4D0).copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.width(135.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isMe) Color.White else Color(0xFFF3EDF7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = pin.avatarEmoji, fontSize = 11.sp)
                                        }
                                        Text(
                                            text = pin.memberName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF211F26),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isMe) {
                                        IconButton(
                                            onClick = { onDeletePin(pin.id) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Pin",
                                                tint = Color(0xFFBA1A1A),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pin.locationName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isMe) Color(0xFF21005D) else Color(0xFF211F26),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "to: ${pin.sharedWith}",
                                    fontSize = 8.sp,
                                    color = Color(0xFF49454F).copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Family To Do's 📋",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF211F26),
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Manage and coordinate daily tasks with your family.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All To Do", "My To Do", "Not Done", "Completed").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF041E49) else Color(0xFFF3EDF7))
                            .border(1.dp, if (isSelected) Color(0xFF041E49) else Color(0xFFCAC4D0), RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF49454F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredChores.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFD3E3FD))
                        .border(1.dp, Color(0xFFABC7F5), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🎉", fontSize = 48.sp)
                        Text(
                            text = "No to-do's found here!",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF041E49)
                        )
                        Text(
                            text = "Add a new to-do or try different filters.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF041E49).copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredChores) { chore ->
                        ChoreItemRow(
                            chore = chore,
                            members = members,
                            onComplete = { onComplete(chore) },
                            onDelete = { onDelete(chore.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF6750A4),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_chore_fab")
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add To Do")
        }
    }
}

@Composable
fun ChoreItemRow(
    chore: Chore,
    members: List<FamilyMember>,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val attendee = members.find { it.name == chore.assignedMember }
    val attendeeEmoji = attendee?.avatarEmoji ?: "👤"

    val isCompleted = chore.isCompleted
    val containerBg = if (isCompleted) Color(0xFFF1F5F9) else Color(0xFFD3E3FD)
    val textMainColor = if (isCompleted) Color(0xFF49454F) else Color(0xFF041E49)
    val textSecondaryColor = if (isCompleted) Color(0xFF49454F).copy(alpha = 0.7f) else Color(0xFF041E49).copy(alpha = 0.8f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, if (isCompleted) Color(0xFFCAC4D0) else Color(0xFFABC7F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = chore.isCompleted,
                onCheckedChange = { if (!chore.isCompleted) onComplete() },
                enabled = !chore.isCompleted,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF22C55E),
                    uncheckedColor = Color(0xFF041E49).copy(alpha = 0.5f),
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("chore_checkbox_${chore.id}")
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chore.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textMainColor,
                    textDecoration = if (chore.isCompleted) TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = attendeeEmoji, fontSize = 10.sp)
                            Text(
                                text = chore.assignedMember,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textMainColor
                            )
                        }
                    }

                    Text(
                        text = "Due: ${chore.dueDateString}",
                        fontSize = 11.sp,
                        color = if (chore.dueDateString == "Today" && !chore.isCompleted) Color(0xFFB91C1C) else textSecondaryColor,
                        fontWeight = FontWeight.Bold
                    )

                    if (chore.isCompleted && chore.completedBy != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Done by ${chore.completedBy}",
                                fontSize = 9.sp,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = textSecondaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingTab(
    shoppingItems: List<ShoppingItem>,
    activeMember: FamilyMember,
    onToggle: (ShoppingItem) -> Unit,
    onDelete: (Long) -> Unit,
    onAddItem: (String, String, String, String?, String?, String?) -> Unit
) {
    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Dairy & Eggs") }
    var selectedShop by remember { mutableStateOf("Any Shop") }
    var productLink by remember { mutableStateOf("") }
    var itemImageUrl by remember { mutableStateOf<String?>(null) }

    var showShopDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAttachPhotoDialog by remember { mutableStateOf(false) }

    val categories = listOf("Dairy & Eggs", "Bakery", "Fruit & Veg", "Pantry", "Household", "Other")
    val SHOP_LIST = listOf(
        "Any Shop", "Sainsbury's", "Aldi", "Lidl", "Asda", "Tesco", "Morrisons", "B&M", "Home Bargains", "Boots",
        "Argos", "M&S", "One Below", "Heron", "Iceland", "Greggs", "Cooplands", "Amazon"
    )

    if (showAttachPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showAttachPhotoDialog = false },
            title = { Text("Attach Product Image 📸", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            confirmButton = {
                TextButton(onClick = { showAttachPhotoDialog = false }) {
                    Text("Done", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select a realistic preset decoration, simulate a device camera snap, or paste a direct product image URL:", fontSize = 12.sp, color = Color(0xFF49454F))
                    
                    Text("Option A: Paste Web URL Link", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF211F26))
                    TextField(
                        value = itemImageUrl ?: "",
                        onValueChange = { itemImageUrl = it },
                        placeholder = { Text("https://example.com/product_pic.jpg", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3EDF7),
                            unfocusedContainerColor = Color(0xFFF3EDF7)
                        )
                    )

                    Text("Option B: Simulate Camera Snap / File Upload", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF211F26))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                itemImageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&q=80&w=250"
                                showAttachPhotoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21005D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simulate Camera 📷", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                itemImageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&q=80&w=250"
                                showAttachPhotoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simulate Upload 📤", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Option C: Quick Food Presets", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF211F26))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = listOf(
                            Pair("🥛 Milk", "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&q=80&w=250"),
                            Pair("🍞 Bread", "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&q=80&w=250"),
                            Pair("🍓 Berry", "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&q=80&w=250"),
                            Pair("🥞 Pancake", "https://images.unsplash.com/photo-1528207776546-365bb710ee93?auto=format&fit=crop&q=80&w=250"),
                            Pair("🧴 Soap", "https://images.unsplash.com/photo-1607006342411-1a90e3d43c22?auto=format&fit=crop&q=80&w=250"),
                            Pair("🍪 Biscuit", "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?auto=format&fit=crop&q=80&w=250"),
                            Pair("☕ Coffee", "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&q=80&w=250")
                        )
                        items(presets) { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8DEF8))
                                    .clickable {
                                        itemImageUrl = preset.second
                                        showAttachPhotoDialog = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(preset.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping List 🛒",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF211F26),
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Add and check off family groceries. Specify shop, product image, and links.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF49454F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Add Item",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        placeholder = { Text("e.g. Milk", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("shopping_input_name"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color(0xFF6750A4),
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    TextField(
                        value = newItemQty,
                        onValueChange = { newItemQty = it },
                        placeholder = { Text("Qty", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("shopping_input_qty"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color(0xFF6750A4),
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(10.dp))
                                .clickable { showShopDropdown = true }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Shop: $selectedShop",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F)
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = showShopDropdown,
                            onDismissRequest = { showShopDropdown = false }
                        ) {
                            SHOP_LIST.forEach { shop ->
                                DropdownMenuItem(
                                    text = { Text(shop, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedShop = shop
                                        showShopDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(10.dp))
                                .clickable { showCategoryDropdown = true }
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cat: $selectedCategory",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F)
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, fontSize = 12.sp) },
                                    onClick = {
                                        selectedCategory = cat
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = productLink,
                        onValueChange = { productLink = it },
                        placeholder = { Text("Product / Ad Web Link (optional)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color(0xFF6750A4),
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = { showAttachPhotoDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (itemImageUrl != null) Color(0xFF16A34A) else Color(0xFF6750A4)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(42.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (itemImageUrl != null) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                            contentDescription = "Attach picture",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (itemImageUrl != null) "Pic On" else "Add Pic",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (itemImageUrl != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            coil.compose.AsyncImage(
                                model = itemImageUrl,
                                contentDescription = "Item preview",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Text("Image Attached!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            Text(
                                "Remove",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Red,
                                modifier = Modifier
                                    .clickable { itemImageUrl = null }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text("No photo attached yet.", fontSize = 10.sp, color = Color(0xFF49454F).copy(alpha = 0.6f))
                    }

                    Button(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                onAddItem(
                                    newItemName.trim(),
                                    newItemQty.ifBlank { "1" }.trim(),
                                    selectedCategory,
                                    itemImageUrl,
                                    productLink.ifBlank { null },
                                    selectedShop
                                )
                                newItemName = ""
                                newItemQty = ""
                                productLink = ""
                                itemImageUrl = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF041E49)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Add to List", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (shoppingItems.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFFBFE))
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🛒", fontSize = 48.sp)
                    Text(
                        text = "Your shopping list is empty!",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D1B20)
                    )
                    Text(
                        text = "Specify shop, pictures, and links to build your list.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F)
                    )
                }
            }
        } else {
            val sortedItems = remember(shoppingItems) {
                shoppingItems.sortedWith(compareBy<ShoppingItem> { it.isPurchased })
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedItems) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = { onToggle(item) },
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
}

fun getShopBadgeColors(shopName: String?): Pair<Color, Color> {
    return when (shopName?.lowercase()) {
        "aldi" -> Pair(Color(0xFF002B6D), Color(0xFFFFCC00))
        "lidl" -> Pair(Color(0xFF0050A1), Color(0xFFFFCC00))
        "asda" -> Pair(Color(0xFF78BE20), Color.White)
        "tesco" -> Pair(Color(0xFF00539F), Color.White)
        "morrisons" -> Pair(Color(0xFF006F3D), Color(0xFFFFD200))
        "b&m" -> Pair(Color(0xFFFF5A00), Color(0xFF00263E))
        "home bargains" -> Pair(Color(0xFFC8102E), Color.White)
        "boots" -> Pair(Color(0xFF003087), Color.White)
        "argos" -> Pair(Color(0xFFF05123), Color.White)
        "m&s" -> Pair(Color(0xFF1E293B), Color(0xFFD9F99D))
        "one below" -> Pair(Color(0xFF3B82F6), Color.White)
        "heron" -> Pair(Color(0xFFFFEA79), Color(0xFF1E293B))
        "iceland" -> Pair(Color(0xFFD61A1C), Color.White)
        "greggs" -> Pair(Color(0xFF005CA9), Color(0xFFFFCC00))
        "cooplands" -> Pair(Color(0xFF7A2048), Color.White)
        "amazon" -> Pair(Color(0xFF232F3E), Color(0xFFFF9900))
        else -> Pair(Color(0xFFF3EDF7), Color(0xFF49454F))
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isPurchased = item.isPurchased
    val containerBg = if (isPurchased) Color(0xFFF1F5F9) else Color.White
    val textMainColor = if (isPurchased) Color(0xFF64748B) else Color(0xFF1E293B)
    val textSecondaryColor = Color(0xFF64748B)

    val shopColors = getShopBadgeColors(item.shopName)
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(
            1.dp,
            if (isPurchased) Color(0xFFE2E8F0) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isPurchased,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF16A34A),
                    uncheckedColor = Color(0xFF1E293B).copy(alpha = 0.4f),
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("shopping_checkbox_${item.id}")
            )

            if (!item.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    coil.compose.AsyncImage(
                        model = item.imageUrl,
                        contentDescription = "Product image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3EDF7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛍️", fontSize = 20.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMainColor,
                        textDecoration = if (isPurchased) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.quantity,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textMainColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!item.shopName.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(shopColors.first)
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = item.shopName.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = shopColors.second
                            )
                        }
                    }

                    Text(
                        text = "by ${item.addedBy}",
                        fontSize = 11.sp,
                        color = textSecondaryColor
                    )
                }

                if (!item.productLink.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri(item.productLink)
                            } catch (e: Exception) {}
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = "Open Link", tint = Color(0xFF6750A4), modifier = Modifier.size(10.dp))
                        Text(
                            text = "View Ad/Shop Page",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

            if (isPurchased && item.purchasedBy != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDCEFE3))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Got by: ${item.purchasedBy}",
                        fontSize = 9.sp,
                        color = Color(0xFF14532D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = textSecondaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CalendarTab(
    events: List<CalendarEvent>,
    onDelete: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val calendarDays = listOf(
        Pair("Today", 0),
        Pair("Tomorrow", 1),
        Pair("Thursday", 2),
        Pair("Friday", 3),
        Pair("Saturday", 4),
        Pair("Sunday", 5)
    )

    var selectedDayFilter by remember { mutableStateOf(0) }
    val filteredEvents = remember(events, selectedDayFilter) {
        events.filter { it.dayOffset == selectedDayFilter }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Family Calendar 📅",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF211F26),
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Coordinated family schedules, visible to everyone.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(calendarDays) { dayPair ->
                    val isSelected = selectedDayFilter == dayPair.second
                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF21005D) else Color.White)
                            .border(1.dp, if (isSelected) Color(0xFF21005D) else Color(0xFFD0BCFF), RoundedCornerShape(16.dp))
                            .clickable { selectedDayFilter = dayPair.second }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayPair.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF21005D)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val count = events.count { it.dayOffset == dayPair.second }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFD0BCFF) else Color(0xFFF3EDF7))
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else Color(0xFF21005D)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Events Schedule for ${calendarDays.first { it.second == selectedDayFilter }.first}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF211F26),
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredEvents.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEADDFF))
                        .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🍃", fontSize = 48.sp)
                        Text(
                            text = "No events scheduled for this day!",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF21005D)
                        )
                        Text(
                            text = "Enjoy an easy day or add a family activity below.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D).copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredEvents) { event ->
                        CalendarEventRow(
                            event = event,
                            onDelete = { onDelete(event.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF6750A4),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_event_fab")
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Event")
        }
    }
}

@Composable
fun CalendarEventRow(
    event: CalendarEvent,
    onDelete: () -> Unit
) {
    val categoryColor = when (event.category) {
        "Meal" -> Color(0xFFC2410C)
        "Appointment" -> Color(0xFFBE185D)
        "School" -> Color(0xFF0E7490)
        else -> Color(0xFF047857)
    }

    val categoryBadgeBg = when (event.category) {
        "Meal" -> Color(0xFFFFEDD5)
        "Appointment" -> Color(0xFFFCE7F3)
        "School" -> Color(0xFFE0F7FA)
        else -> Color(0xFFD1FAE5)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
        border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .width(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = event.timeString,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF21005D)
                )
                Text(
                    text = "GMT",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF21005D).copy(alpha = 0.6f)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = event.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF21005D)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryBadgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.category,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF21005D).copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Scheduled by: ${event.creatorName}",
                    fontSize = 10.sp,
                    color = Color(0xFF21005D).copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete event",
                    tint = Color(0xFF21005D).copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StarsBoardTab(
    members: List<FamilyMember>,
    history: List<ActivityNotification>,
    onResetLog: () -> Unit,
    locationPins: List<com.example.data.LocationPin>,
    onDeletePin: (Long) -> Unit,
    activeMember: FamilyMember
) {
    if (true) return
    val rankedMembers = remember(members) {
        members.sortedByDescending { it.points }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Stars Board 🏆",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF211F26),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Track star points and see live synced completed tasks!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F)
                )
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Where is everyone? 📍",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF211F26),
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (locationPins.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No location pins shared yet.",
                                fontSize = 12.sp,
                                color = Color(0xFF49454F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(locationPins) { pin ->
                            val isMe = pin.memberName == activeMember.name
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) Color(0xFFE8DEF8) else Color.White
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isMe) Color(0xFF6750A4) else Color(0xFFCAC4D0).copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.width(180.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isMe) Color.White else Color(0xFFF3EDF7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = pin.avatarEmoji, fontSize = 14.sp)
                                            }
                                            Text(
                                                text = pin.memberName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF211F26),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (isMe) {
                                            IconButton(
                                                onClick = { onDeletePin(pin.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove Pin",
                                                    tint = Color(0xFFBA1A1A),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = pin.locationName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isMe) Color(0xFF21005D) else Color(0xFF211F26),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.heightIn(min = 36.dp)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = "Shared with",
                                            tint = if (isMe) Color(0xFF49454F) else Color(0xFF49454F).copy(alpha = 0.8f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "to: ${pin.sharedWith}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color(0xFF49454F) else Color(0xFF49454F).copy(alpha = 0.8f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Family Leaderboard",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF21005D)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    rankedMembers.forEachIndexed { index, member ->
                        val isLeader = index == 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(28.dp)
                            ) {
                                if (isLeader) {
                                    Text(text = "👑", fontSize = 16.sp)
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF21005D).copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Text(text = member.avatarEmoji, fontSize = 18.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = member.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF21005D)
                                    )
                                    Text(
                                        text = member.role,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF21005D).copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                val progress = (member.points.toFloat() / 350f).coerceIn(0.1f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isLeader) Color(0xFFB45309) else Color(0xFF6750A4),
                                    trackColor = Color.White.copy(alpha = 0.5f)
                                )
                            }

                            Text(
                                text = "${member.points} pts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLeader) Color(0xFFB45309) else Color(0xFF21005D)
                            )
                        }

                        if (index < rankedMembers.lastIndex) {
                            HorizontalDivider(color = Color(0xFFD0BCFF).copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Real-Time Activity Log ⚡",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF211F26)
                    )

                    if (history.isNotEmpty()) {
                        Text(
                            text = "Clear log",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C),
                            modifier = Modifier.clickable { onResetLog() }
                        )
                    }
                }
                Text(
                    text = "A live transactional record of events synced seamlessly from family updates.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F)
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF3EDF7))
                        .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "📡", fontSize = 28.sp)
                        Text(
                            text = "Awaiting simulated cloud sync updates...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF21005D).copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(history) { record ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                    border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = record.memberEmoji, fontSize = 18.sp)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.message,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                            Text(
                                text = "Synced safely via Family Cloud Gateway",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D).copy(alpha = 0.6f)
                            )
                        }

                        if (record.pointsGained > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+${record.pointsGained} pts",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSwitcherDialog(
    members: List<FamilyMember>,
    activeMember: FamilyMember,
    onSelectMember: (FamilyMember) -> Unit,
    onInviteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Who's using the organiser? 👤",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF211F26),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Switch family profiles instantly to modify checklists and calendar events.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(4.dp))

                members.forEach { member ->
                    val isCurrent = member.name == activeMember.name
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) Color(0xFFD3E3FD) else Color.White
                        ),
                        border = if (isCurrent) BorderStroke(1.5.dp, Color(0xFF041E49)) else BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMember(member) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8DEF8))
                            ) {
                                Text(text = member.avatarEmoji, fontSize = 22.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isCurrent) Color(0xFF041E49) else Color(0xFF211F26)
                                )
                                Text(
                                    text = member.role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color(0xFF041E49).copy(alpha = 0.7f) else Color(0xFF49454F)
                                )
                            }


                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = onInviteClick,
                    border = BorderStroke(1.5.dp, Color(0xFF6750A4)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6750A4)),
                    modifier = Modifier.fillMaxWidth().testTag("invite_member_action_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Invite Icon", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invite New Member ✉️", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF211F26)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddChoreDialog(
    members: List<FamilyMember>,
    onConfirm: (String, Int, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var assignedMember by remember { mutableStateOf(members.firstOrNull()?.name ?: "Mum") }
    var dueDate by remember { mutableStateOf("Today") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add Family To Do 📋",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF211F26),
                    letterSpacing = (-0.5).sp
                )

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Task Title (e.g. Wash windows)", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_chore_input_title"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6750A4),
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Due date",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Today", "Tomorrow").forEach { dueTag ->
                            val isSelected = dueDate == dueTag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF6750A4) else Color.White)
                                    .border(1.dp, if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                    .clickable { dueDate = dueTag }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = dueTag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Assign Member",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D1B20)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(members) { m ->
                        val isSelected = assignedMember == m.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFFD3E3FD) else Color.White)
                                .border(1.dp, if (isSelected) Color(0xFF041E49) else Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                                .clickable { assignedMember = m.name }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${m.avatarEmoji} ${m.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color(0xFF041E49) else Color(0xFF49454F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0xFF211F26)),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF211F26)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(
                                    title.trim(),
                                    0,
                                    assignedMember,
                                    dueDate
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("add_chore_confirm_button"),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCalendarEventDialog(
    onConfirm: (String, String, String, String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var timeString by remember { mutableStateOf("12:00") }
    var selectedDayTag by remember { mutableStateOf("Today") }
    var dayOffset by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf("Activity") }

    val days = listOf(
        Triple("Today", 0, "Today"),
        Triple("Tomorrow", 1, "Tomorrow"),
        Triple("Thursday", 2, "Thursday"),
        Triple("Friday", 3, "Friday"),
        Triple("Saturday", 4, "Saturday"),
        Triple("Sunday", 5, "Sunday")
    )

    val categories = listOf("Activity", "Meal", "Appointment", "School")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add Family Event 📅",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF211F26),
                    letterSpacing = (-0.5).sp
                )

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Event Title (e.g. Grandma visiting)", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_event_input_title"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6750A4),
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Description & Notes", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6750A4),
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextField(
                        value = timeString,
                        onValueChange = { timeString = it },
                        placeholder = { Text("Time (e.g. 15:30)", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_event_input_time"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color(0xFF6750A4),
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "Category",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D1B20)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF6750A4) else Color.White)
                                        .border(1.dp, if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else Color(0xFF49454F)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Choose Date",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D1B20)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(days) { dayTriple ->
                        val isSelected = selectedDayTag == dayTriple.first
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF6750A4) else Color.White)
                                .border(1.dp, if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0), RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedDayTag = dayTriple.first
                                    dayOffset = dayTriple.second
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = dayTriple.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF49454F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0xFF211F26)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF211F26)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(
                                    title.trim(),
                                    description.ifBlank { "Unspecified family activity." }.trim(),
                                    timeString.trim(),
                                    selectedDayTag,
                                    dayOffset,
                                    selectedCategory
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("add_event_confirm_button"),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Schedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLocationDialog(
    members: List<FamilyMember>,
    activeMember: FamilyMember,
    onConfirm: (locationText: String, targetMembers: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var customLocation by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("🏡 At Home") }
    var shareWithAll by remember { mutableStateOf(true) }
    val chosenMembers = remember { mutableStateListOf<String>() }

    val presets = listOf(
        "🏡 At Home",
        "🏫 At School",
        "🛒 Grocery Shopping",
        "🌳 In the Park",
        "🏋️ Gym Workout",
        "🚇 Train Station",
        "🏢 At Work",
        "☕ Coffee Shop"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Share Pin Point 📍",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF211F26),
                    letterSpacing = (-0.5).sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3EDF7))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8DEF8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = activeMember.avatarEmoji, fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "${activeMember.name} (You)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF211F26)
                        )
                        Text(
                            text = "Sharing your location as a shared pin point.",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }

                Text(
                    text = "Where are you now?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1D1B20)
                )

                // Grid of preset location chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val rows = presets.chunked(3)
                    rows.forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { preset ->
                                val isSelected = selectedPreset == preset && customLocation.isEmpty()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF6750A4) else Color.White)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedPreset = preset
                                            customLocation = ""
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF49454F)
                                    )
                                }
                            }
                            if (rowItems.size < 3) {
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                TextField(
                    value = customLocation,
                    onValueChange = {
                        customLocation = it
                        selectedPreset = ""
                    },
                    placeholder = { Text("Or type custom location address...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("custom_location_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6750A4),
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Share with Everyone",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D1B20)
                    )
                    Switch(
                        checked = shareWithAll,
                        onCheckedChange = { shareWithAll = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6750A4),
                            uncheckedThumbColor = Color(0xFF49454F),
                            uncheckedTrackColor = Color(0xFFE8DEF8)
                        )
                    )
                }

                if (!shareWithAll) {
                    Text(
                        text = "Select family members:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F)
                    )

                    val otherMembers = members.filter { it.name != activeMember.name }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(otherMembers) { member ->
                            val isChosen = chosenMembers.contains(member.name)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isChosen) Color(0xFFD3E3FD) else Color.White)
                                    .border(
                                        1.dp,
                                        if (isChosen) Color(0xFF041E49) else Color(0xFFCAC4D0),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        if (isChosen) {
                                            chosenMembers.remove(member.name)
                                        } else {
                                            chosenMembers.add(member.name)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = member.avatarEmoji, fontSize = 12.sp)
                                    Text(
                                        text = member.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isChosen) Color(0xFF041E49) else Color(0xFF49454F)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0xFF211F26)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF211F26)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    val hasSelection = shareWithAll || chosenMembers.isNotEmpty()
                    val locationText = customLocation.ifBlank { selectedPreset }
                    val isEnabled = locationText.isNotBlank() && hasSelection

                    Button(
                        onClick = {
                            if (isEnabled) {
                                val targets = if (shareWithAll) emptyList() else chosenMembers.toList()
                                onConfirm(locationText.trim(), targets)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("add_location_confirm_button"),
                        enabled = isEnabled,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Share Pin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WishListTab(
    wishListItems: List<com.example.data.WishListItem>,
    activeMember: FamilyMember,
    members: List<FamilyMember>,
    onToggle: (com.example.data.WishListItem) -> Unit,
    onDelete: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    var selectedFilterMember by remember { mutableStateOf<String?>("All") }

    val filteredItems = remember(wishListItems, selectedFilterMember) {
        if (selectedFilterMember == "All" || selectedFilterMember == null) {
            wishListItems
        } else {
            wishListItems.filter { it.memberName == selectedFilterMember }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper banner/header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECE6F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🎁 Family Wish Lists",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF21005D),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Share gift ideas, wanted items, and explore what your family members are wishing for!",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        // Horizontal filter list
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                val isSelected = selectedFilterMember == "All"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7))
                        .clickable { selectedFilterMember = "All" }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "✨ All Lists",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF49454F)
                    )
                }
            }

            items(members) { member ->
                val isSelected = selectedFilterMember == member.name
                val colorParsed = try { Color(android.graphics.Color.parseColor(member.colorHex)) } catch (e: Exception) { Color(0xFF6750A4) }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) colorParsed else Color(0xFFF3EDF7))
                        .border(
                            1.dp,
                            if (isSelected) colorParsed else Color(0xFFCAC4D0),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilterMember = member.name }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = member.avatarEmoji, fontSize = 14.sp)
                        Text(
                            text = member.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) {
                                if (member.colorHex == "#FFEB3B") Color.Black else Color.White
                            } else Color(0xFF49454F)
                        )
                    }
                }
            }
        }

        // Add item FAB-style row/button
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("add_wishlist_item_button")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add item", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Something I Want 🎁", fontWeight = FontWeight.Bold)
        }

        // Wishlist items container
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🌌", fontSize = 48.sp)
                    Text(
                        text = "No wishlist items here yet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F)
                    )
                    Text(
                        text = "Click the button above to add the first item!",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F).copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    val itemMember = members.find { it.name == item.memberName } ?: activeMember
                    val memberColor = try { Color(android.graphics.Color.parseColor(itemMember.colorHex)) } catch (e: Exception) { Color(0xFF6750A4) }
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wishlist_item_card_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isCompleted) Color(0xFFF1F5F9) else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (item.isCompleted) Color(0xFFE2E8F0) else Color(0xFFCAC4D0).copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product Image preview
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.imageUrl.isNullOrBlank()) {
                                    Text(text = "🎁", fontSize = 28.sp)
                                } else {
                                    coil.compose.AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = "Wishlist product image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }

                            // Middle Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Name Badge tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(memberColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${itemMember.avatarEmoji} ${item.memberName}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (itemMember.colorHex == "#FFEB3B") Color(0xFF854D0E) else memberColor
                                        )
                                    }

                                    if (item.isCompleted) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFDEF7EC))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "🎁 GOT IT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF03543F)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isCompleted) Color(0xFF64748B) else Color(0xFF1D1B20),
                                    textDecoration = if (item.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )

                                if (!item.description.isNullOrBlank()) {
                                    Text(
                                        text = item.description,
                                        fontSize = 11.sp,
                                        color = if (item.isCompleted) Color(0xFF94A3B8) else Color(0xFF49454F),
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                if (!item.productLink.isNullOrBlank()) {
                                    Text(
                                        text = "🔗 View web page",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6750A4),
                                        modifier = Modifier
                                            .clickable {
                                                try {
                                                    var url = item.productLink.trim()
                                                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                                        url = "https://$url"
                                                    }
                                                    uriHandler.openUri(url)
                                                } catch (e: Exception) {
                                                    // fail-safe
                                                }
                                            }
                                    )
                                }
                            }

                            // Action buttons: toggle purchased status and trash icon
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val tintChecked = if (item.isCompleted) Color(0xFF03543F) else Color(0xFF6750A4)
                                IconButton(
                                    onClick = { onToggle(item) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                        contentDescription = "Mark purchased",
                                        tint = tintChecked,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDelete(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete item",
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddWishListItemDialog(
    members: List<FamilyMember>,
    activeMember: FamilyMember,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var chosenMemberName by remember { mutableStateOf(activeMember.name) }
    var productLink by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var showAttachPhotoDialog by remember { mutableStateOf(false) }

    if (showAttachPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showAttachPhotoDialog = false },
            title = { Text("Product Image / Camera Simulation 📸", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            confirmButton = {
                TextButton(onClick = { showAttachPhotoDialog = false }) {
                    Text("Done", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Paste an image link or select a preset holiday toy/tech look to simulate your camera snap/photo upload:", fontSize = 12.sp, color = Color(0xFF49454F))

                    Text("Option A: Paste Direct Web Image URL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF211F26))
                    TextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        placeholder = { Text("https://example.com/item.jpg", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3EDF7),
                            unfocusedContainerColor = Color(0xFFF3EDF7)
                        )
                    )

                    Text("Option B: Quick Simulated Camera Action", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF211F26))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&q=80&w=250"
                                showAttachPhotoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D1B20)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Click Photo 📸", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                imageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&q=80&w=250"
                                showAttachPhotoDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simulate Upload 📤", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Option C: Preset Wishlist Catagories", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF211F26))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val presets = listOf(
                            Pair("🎮 Gaming / Tech", "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?auto=format&fit=crop&q=80&w=250"),
                            Pair("📚 Reading Books", "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&q=80&w=250"),
                            Pair("🎨 Arts & Crafts", "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&q=80&w=250"),
                            Pair("👗 Wearable Fashion", "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&q=80&w=250"),
                            Pair("💻 Tech / Gadgets", "https://images.unsplash.com/photo-1525373612132-b3e824712ec8?auto=format&fit=crop&q=80&w=250"),
                            Pair("🧸 Toys / Models", "https://images.unsplash.com/photo-1558060370-d644479cb6f7?auto=format&fit=crop&q=80&w=250")
                        )
                        items(presets) { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8DEF8))
                                    .clickable {
                                        imageUrl = preset.second
                                        showAttachPhotoDialog = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(preset.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                            }
                        }
                    }
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = "Add to Wishlist 🎁",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1D1B20)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_wishlist_item_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Item Name
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What item do you want?") },
                    placeholder = { Text("e.g. Cozy Wool Socks") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wish_item_input_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6750A4),
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Member filter/select
                Column {
                    Text(
                        text = "Whose list is this for?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(members) { member ->
                            val isSelected = chosenMemberName == member.name
                            val memberColor = try { Color(android.graphics.Color.parseColor(member.colorHex)) } catch (e: Exception) { Color(0xFF6750A4) }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) memberColor else Color.White)
                                    .border(1.dp, if (isSelected) memberColor else Color(0xFFCAC4D0), RoundedCornerShape(8.dp))
                                    .clickable { chosenMemberName = member.name }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${member.avatarEmoji} ${member.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        if (member.colorHex == "#FFEB3B") Color.Black else Color.White
                                    } else Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }

                // Description
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details or specifications (Optional)") },
                    placeholder = { Text("e.g. Size 6, green color if possible") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6750A4),
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Product page Link
                TextField(
                    value = productLink,
                    onValueChange = { productLink = it },
                    label = { Text("Web page page link (Optional)") },
                    placeholder = { Text("e.g. boots.com/socks") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF6750A4),
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri)
                )

                // Image / Camera Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAttachPhotoDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8DEF8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = if (imageUrl.isNotBlank()) "Change Image 🖼️" else "Attach Photo 📸",
                            color = Color(0xFF21005D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (imageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            coil.compose.AsyncImage(
                                model = imageUrl,
                                contentDescription = "Attached thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0xFF211F26)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF211F26)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name.trim(),
                                    chosenMemberName,
                                    imageUrl.ifBlank { null },
                                    productLink.ifBlank { null },
                                    description.ifBlank { null }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("add_wishlist_confirm_button"),
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Item", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

@Composable
fun InviteMemberDialog(
    onDismiss: () -> Unit,
    onConfirmInviteJoined: (FamilyMember) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Relative") }
    var avatarEmoji by remember { mutableStateOf("🧔") }
    var colorHex by remember { mutableStateOf("#9C27B0") }

    var selectedChannel by remember { mutableStateOf<String?>(null) }
    var showSimulation by remember { mutableStateOf(false) }

    val emojis = listOf("👨", "👩", "👦", "👧", "👶", "👵", "👴", "🧔", "👱‍♂️", "🦊", "🐨", "😺", "🐶")
    val colorsList = listOf(
        Pair("Purple", "#9C27B0"),
        Pair("Pink/Rose", "#FF4081"),
        Pair("Ocean Blue", "#2196F3"),
        Pair("Forest Green", "#4CAF50"),
        Pair("Tangerine", "#FF9800"),
        Pair("Crimson Red", "#E91E63"),
        Pair("Classic Teal", "#009688")
    )

    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = "Invite Family Member ✉️",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1D1B20)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!showSimulation) {
                    Text(
                        text = "To keep the family group private and secure, new members can only join via an official invite link shared through WhatsApp, SMS, Messenger, or Email.",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F),
                        lineHeight = 16.sp
                    )

                    // Member Name
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Visitor / Member Name") },
                        placeholder = { Text("e.g. Uncle Steve") },
                        modifier = Modifier.fillMaxWidth().testTag("invite_input_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3EDF7),
                            unfocusedContainerColor = Color(0xFFF3EDF7)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    // Role
                    TextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Family Role / Relationship") },
                        placeholder = { Text("e.g. Uncle / Teen / Cousin") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3EDF7),
                            unfocusedContainerColor = Color(0xFFF3EDF7)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    // Avatar Selector
                    Column {
                        Text(
                            text = "Choose Avatar Emoji",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(emojis) { emoji ->
                                val isSelected = avatarEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFFE8DEF8) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                        .clickable { avatarEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    // Color Selector
                    Column {
                        Text(
                            text = "Choose Theme Colour",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(colorsList) { cItem ->
                                val isSelected = colorHex == cItem.second
                                val parsedColor = try { Color(android.graphics.Color.parseColor(cItem.second)) } catch (e: Exception) { Color(0xFF6750A4) }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor)
                                        .border(
                                            if (isSelected) 2.5.dp else 1.dp,
                                            if (isSelected) Color.Black else Color.White,
                                            CircleShape
                                        )
                                        .clickable { colorHex = cItem.second }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔒 SHARE LINK TO GENERATE VISITOR INVITE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6750A4)
                    )

                    // Secure Invite Share Channels
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val channels = listOf(
                            Triple("Share via WhatsApp 🟢", "WhatsApp", Color(0xFF25D366)),
                            Triple("Share via SMS Message 💬", "SMS", Color(0xFF007AFF)),
                            Triple("Share via Messenger 🔵", "Facebook Messenger", Color(0xFF0084FF)),
                            Triple("Share via Email Link ✉️", "Email", Color(0xFFE11D48))
                        )

                        channels.forEach { (label, nameVal, colorVal) ->
                            Button(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        selectedChannel = nameVal
                                        showSimulation = true

                                        // Real Android sharing intent execution
                                        val encodedName = java.net.URLEncoder.encode(name.trim(), "UTF-8")
                                        val encodedRole = java.net.URLEncoder.encode(role.trim(), "UTF-8")
                                        val encodedEmoji = java.net.URLEncoder.encode(avatarEmoji, "UTF-8")
                                        val encodedColor = java.net.URLEncoder.encode(colorHex, "UTF-8")
                                        val generatedLink = "https://family-organiser.app/join?name=$encodedName&role=$encodedRole&avatar=$encodedEmoji&color=$encodedColor"

                                        val shareText = when (nameVal) {
                                            "Email" -> "Join my secure Family Organiser! Click here to join: $generatedLink"
                                            "WhatsApp" -> "Hey! Tap here to securely join our shared Family Organiser: $generatedLink"
                                            "Facebook Messenger" -> "Click to accept my invitation to the family organiser app: $generatedLink"
                                            else -> "Accept my secure invite to join the Family Organiser! Tap: $generatedLink"
                                        }

                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                        }
                                        try {
                                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Secure Share"))
                                        } catch (e: Exception) {
                                            // Handle exception quietly
                                        }
                                    }
                                },
                                enabled = name.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = colorVal),
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color(0xFF211F26), fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Simulated Incoming Message Box
                    val primaryColor = when (selectedChannel) {
                        "WhatsApp" -> Color(0xFF25D366)
                        "Facebook Messenger" -> Color(0xFF0084FF)
                        "SMS" -> Color(0xFF007AFF)
                        else -> Color(0xFF6750A4)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "In-App Sandbox Simulator 📲",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                        Text(
                            text = "Since you're running in AI Studio, here's a simulation of the recipient receiving your secure invite link on $selectedChannel. Click the invitation message below to complete the secure join flow!",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F),
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Simulated Messaging Bubble Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (selectedChannel) {
                                                "WhatsApp" -> "💬"
                                                "Facebook Messenger" -> "Ⓜ️"
                                                "SMS" -> "📱"
                                                else -> "✉️"
                                            },
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "$selectedChannel Invitation",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = primaryColor
                                    )
                                }

                                Text(
                                    text = "To: $name ($role)\n\n🎁 You have been invited to securely join our private Family Organiser network! Tap the exclusive secure web reference below to download configuration and join instantly:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1D1B20)
                                )

                                // Simulated Link CTA Button
                                Button(
                                    onClick = {
                                        val newMember = FamilyMember(
                                            name = name.trim(),
                                            role = role.trim(),
                                            avatarEmoji = avatarEmoji,
                                            points = 0,
                                            colorHex = colorHex
                                        )
                                        onConfirmInviteJoined(newMember)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("simulate_accept_invite_link")
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🔗 family-organiser.app/join?user=${name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { showSimulation = false },
                                border = BorderStroke(1.dp, Color(0xFF211F26)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Back to Form", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF211F26))
                            }

                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    )
}


