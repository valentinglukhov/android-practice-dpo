package com.example.android_practice_dpo.main.view

import com.example.android_practice_dpo.R

sealed class NavigationRoutes(
    val route: String,
    val title: String,
    val icon: Int,
    val contentDescription: String = ""
) {
    object Photos : NavigationRoutes(
        "Photos",
        "Photos",
        R.drawable.icon_photos,
        "Button to switch to the section with photos"
    )

    object Collections : NavigationRoutes(
        "Collections",
        "Collections",
        R.drawable.icon_collections,
        "button to switch to the section with collections of photos"
    )

    object Profile : NavigationRoutes(
        "Profile",
        "Profile",
        R.drawable.icon_profile,
        "Button to switch to the user profile section"
    )

    object SearchQuery : NavigationRoutes(
        "searchQuery",
        "searchQuery/{searchQuery}",
        R.drawable.icon_profile
    ) {
        fun buildSearchQuery(searchQuery: String): String {
            return "${SearchQuery.route}/{$searchQuery}"
        }
    }
}
