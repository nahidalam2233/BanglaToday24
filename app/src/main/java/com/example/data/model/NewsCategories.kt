package com.example.data.model

object NewsCategories {
    const val BASE_URL = "https://banglatoday24.xyz"

    val list = listOf(
        NewsCategory("home", "হোম", BASE_URL),
        NewsCategory("national", "জাতীয়", "$BASE_URL/category/%e0%a6%9c%e0%a6%be%e0%a6%a4%e0%a7%80%e0%a6%af%e0%a6%bc/"),
        NewsCategory("politics", "রাজনীতি", "$BASE_URL/category/%e0%a6%b0%e0%a6%be%e0%a6%9c%e0%a6%a8%e0%a7%80%e0%a6%a4%e0%a6%bf/"),
        NewsCategory("international", "আন্তর্জাতিক", "$BASE_URL/category/%e0%a6%86%e0%a6%a8%e0%a7%8d%e0%a6%a4%e0%a6%b0%e0%a7%8d%e0%a6%9c%e0%a6%be%e0%a6%a4%e0%a6%bf%e0%a6%95/"),
        NewsCategory("sports", "খেলাধুলা", "$BASE_URL/category/%e0%a6%96%e0%a7%87%e0%a6%b2%e0%a6%be%e0%a6%a7%e0%a7%81%e0%a6%b2%e0%a6%be/"),
        NewsCategory("entertainment", "বিনোদন", "$BASE_URL/category/%e0%a6%ac%e0%a6%bf%e0%a6%a8%e0%a7%8b%e0%a6%a6%e0%a6%a8/"),
        NewsCategory("economy", "অর্থনীতি", "$BASE_URL/category/%e0%a6%85%e0%a6%b0%e0%a7%8d%e0%a6%a5%e0%a6%a8%e0%a7%80%e0%a6%a4%e0%a6%bf/"),
        NewsCategory("technology", "তথ্যপ্রযুক্তি", "$BASE_URL/category/%e0%a6%a4%e0%a6%a5%e0%a7%8d%e0%a6%af%e0%a6%aa%e0%a7%8d%e0%a6%b0%e0%a6%af%e0%a7%81%e0%a6%95%e0%a7%8d%e0%a6%a4%e0%a6%bf/"),
        NewsCategory("crime", "অপরাধ ও দুর্ণীতি", "$BASE_URL/category/%e0%a6%85%e0%a6%aa%e0%a6%b0%e0%a6%be%e0%a6%a7-%e0%a6%93-%e0%a6%a6%e0%a7%81%e0%a6%b0%e0%a7%8d%e0%a6%a3%e0%a7%80%e0%a6%a4%e0%a6%bf/"),
        NewsCategory("special", "বিশেষ সংবাদ", "$BASE_URL/category/%e0%a6%ac%e0%a6%bf%e0%a6%b6%e0%a7%87%e0%a6%b7-%e0%a6%b8%e0%a6%82%e0%a6%ac%e0%a6%be%e0%a6%a6/"),
        NewsCategory("dhaka", "ঢাকা", "$BASE_URL/category/%e0%a6%a2%e0%a6%be%e0%a6%95%e0%a6%be/"),
        NewsCategory("chittagong", "চট্টগ্রাম", "$BASE_URL/category/%e0%a6%9a%e0%a6%9f%e0%a7%8d%e0%a6%9f%e0%a6%97%e0%a7%8d%e0%a6%b0%e0%a6%be%e0%a6%ae/"),
        NewsCategory("sylhet", "সিলেট", "$BASE_URL/category/%e0%a6%b8%e0%a6%bf%e0%a6%b2%e0%a7%87%e0%a6%9f/"),
        NewsCategory("rajshahi", "রাজশাহী", "$BASE_URL/category/%e0%a6%b0%e0%a6%be%e0%a6%9c%e0%a6%b6%e0%a6%be%e0%a6%b9%e0%a7%80/"),
        NewsCategory("khulna", "খুলনা", "$BASE_URL/category/%e0%a6%96%e0%a7%81%e0%a6%b2%e0%a6%a8%e0%a6%be/"),
        NewsCategory("barisal", "বরিশাল", "$BASE_URL/category/%e0%a6%ac%e0%a6%b0%e0%a6%bf%e0%a6%b6%e0%a6%be%e0%a6%b2/"),
        NewsCategory("rangpur", "রংপুর", "$BASE_URL/category/%e0%a6%b0%e0%a6%82%e0%a6%aa%e0%a7%81%e0%a6%b0/"),
        NewsCategory("mymensingh", "ময়মনসিংহ", "$BASE_URL/category/%e0%a6%ae%e0%a6%af%e0%a6%bc%e0%a6%ae%e0%a6%a8%e0%a6%b8%e0%a6%bf%e0%a6%82%e0%a6%b9/"),
        NewsCategory("law", "আইন আদালত", "$BASE_URL/category/%e0%a6%86%e0%a6%87%e0%a6%a8-%e0%a6%86%e0%a6%a6%e0%a6%be%e0%a6%b2%e0%a6%a4/"),
        NewsCategory("jobs", "চাকুরী", "$BASE_URL/category/%e0%a6%9a%e0%a6%be%e0%a6%95%e0%a7%81%e0%a6%b0%e0%a7%80/"),
        NewsCategory("religion", "ধর্ম", "$BASE_URL/category/%e0%a6%a7%e0%a6%b0%e0%a7%8d%e0%a6%ae/"),
        NewsCategory("health", "স্বাস্থ্য", "$BASE_URL/category/%e0%a6%b8%e0%a7%8d%e0%a6%ac%e0%a6%be%e0%a6%b8%e0%a7%8d%e0%a6%a5%e0%a7%8d%e0%a6%af/"),
        NewsCategory("expatriate", "প্রবাসের খবর", "$BASE_URL/category/%e0%a6%aa%e0%a7%8d%e0%a6%b0%e0%a6%ac%e0%a6%be%e0%a6%b8%e0%a7%87%e0%a6%b0-%e0%a6%96%e0%a6%ac%e0%a6%b0/"),
        NewsCategory("lifestyle", "লাইফ স্টাইল", "$BASE_URL/category/%e0%a6%b2%e0%a6%be%e0%a6%87%e0%a6%ab-%e0%a6%b8%e0%a7%8d%e0%a6%9f%e0%a6%be%e0%a6%87%e0%a6%b2/"),
        NewsCategory("opinion", "মতামত", "$BASE_URL/category/%e0%a6%ae%e0%a6%a4%e0%a6%be%e0%a6%ae%e0%a6%a4/"),
        NewsCategory("editorial", "সম্পাদকীয়", "$BASE_URL/category/%e0%a6%b8%e0%a6%ae%e0%a7%8d%e0%a6%aa%e0%a6%be%e0%a6%a6%e0%a6%95%e0%a7%80%e0%a7%9f/")
    )
}
