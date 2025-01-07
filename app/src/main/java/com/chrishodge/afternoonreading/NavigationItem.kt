package com.chrishodge.afternoonreading.ui

import com.chrishodge.afternoonreading.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Chat : NavigationItem("chat", R.drawable.ic_chat_black_24dp, "Chat")
    object Account : NavigationItem("account", R.drawable.ic_person_black_24dp, "Account")
    object Settings : NavigationItem("settings", R.drawable.ic_settings_black_24dp, "Settings")
}