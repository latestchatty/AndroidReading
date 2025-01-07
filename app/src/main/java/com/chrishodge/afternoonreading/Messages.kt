package com.chrishodge.afternoonreading.ui

data class Channel(
    val id: Int,
    val title: String,
    var content: String,
)

val channeList = listOf(
    Channel(
        1,
        "Aloe Vera",
        "It is used in the pharmaceutical industry for its healing properties and it can often be found in several cosmetic products as well.",
    ),
    Channel(
        2,
        "Rose",
        "Rose is a valuable natural cosmetic ingredient. It contains antibacterial and antioxidants. It's toning and relaxing.",
    ),
    Channel(
        3,
        "Calendula",
        "Calendula helps reducing redness and healing up the skin and it is considered a medicinal plant."
    ),
    Channel(
        4,
        "Lavender",
        "The lavender blossoms contain nurturing essential oils for the skin which have a balancing and soothing fragrance.",
    ),
    Channel(
        5,
        "Jojoba",
        "Jojoba improves skin elasticity and prevents skin dehydration, making it perfect for sensitive or very dry skin.",
    ),
    Channel(
        6,
        "Tea Tree",
        "Tea tree is aimed to fight against acne and to treat acne prone skin. It’s considered one of the best natural alternatives to chemical substances.",
    ),
    Channel(
        7,
        "Chamomile",
        "Chamomile is the most extensive applications in natural cosmetics. It is a common flavoring agent in foods and beverages, and other products such as mouthwash, soaps, and cosmetics.",
    ),
    Channel(
        8,
        "Rosemary",
        "Rosemary (Rosmarinus officinalis) is deep green in colour and pungent in fragrance.",
    ),
    Channel(
        9,
        "Kiwi",
        "It contains high amounts of vitamin C and a potent antioxidant that can protect the skin from free radicals, stimulate collagen production, and reduce hyper pigmentation.",
    ),
    Channel(
        10,
        "Cilantro",
        "Cilantro is high in vitamin C, an antioxidant that fights off damage-causing free radicals.",
    ),
    Channel(
        11,
        "Orange",
        "Consuming enough vitamin C can help a person maintain skin health and appearance.  Vitamin C contributes to collagen production. Collagen supports the skin, promotes wound healing, and improves skin strength.",
    ),
    Channel(
        12,
        "Cucumber",
        "Cucumber is rich in all most Vitamin (C,A,B,K). Magnesium in cucumbers can promote skin elasticity and retain skin moisture.Potassium in cucumbers is another mineral that can help in hydrating the skin and balancing the electrolytes.",
    ),
)