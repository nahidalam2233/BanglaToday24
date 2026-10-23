package com.example.data.model

object NewsCategories {
    const val BASE_URL = "https://banglatoday24.xyz"

    val list = listOf(
        NewsCategory("home", "হোম", BASE_URL),
        NewsCategory("national", "জাতীয়", "$BASE_URL/category/national"),
        NewsCategory("politics", "রাজনীতি", "$BASE_URL/category/politics"),
        NewsCategory("international", "আন্তর্জাতিক", "$BASE_URL/category/international"),
        NewsCategory("sports", "খেলাধুলা", "$BASE_URL/category/sports"),
        NewsCategory("entertainment", "বিনোদন", "$BASE_URL/category/entertainment"),
        NewsCategory("economy", "অর্থনীতি", "$BASE_URL/category/economy"),
        NewsCategory("technology", "তথ্যপ্রযুক্তি", "$BASE_URL/category/technology"),
        NewsCategory("opinion", "মতামত", "$BASE_URL/category/opinion"),
        NewsCategory("lifestyle", "জীবনযাপন", "$BASE_URL/category/lifestyle")
    )
}
