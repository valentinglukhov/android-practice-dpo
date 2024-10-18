package com.example.android_practice_dpo.main.view

import com.example.android_practice_dpo.R

sealed class NavigationRoutes(
    val route: String,
    val title: String = "",
    val icon: Int = 0,
    val contentDescription: String = "",
    val uriPattern: String = ""
) {

    object SinglePhoto : NavigationRoutes(
        route = "single_photo",
        uriPattern = "https://unsplash.com/photos/{id}"
    )

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
        "button to switch to the section with collections of photos",
        "collections/photo/{collectionId}"
    ) {
        fun collectionUriBuilder(collectionId: String): String {
            return "collections/photo/$collectionId"
        }
    }

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
