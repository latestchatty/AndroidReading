package com.chrishodge.afternoonreading.ui

data class Channel(
    val id: Int,
    val title: String,
    var content: String,
    var author: String
)

val channelList = listOf(
    Channel(
        1,
        "Miniatures",
        "It is used in the pharmaceutical industry for its healing properties and it can often be found in several cosmetic products as well.",
        "WatcherXP"
    ),
    Channel(
        2,
        "Best Games Played in 2024!",
        "Rose is a valuable natural cosmetic ingredient. It contains antibacterial and antioxidants. It's toning and relaxing.",
        "Rahazar"
    ),
    Channel(
        3,
        "Cymbals from Alchemy!",
        "Calendula helps reducing redness and healing up the skin and it is considered a medicinal plant.",
        "AlvinKlein"
    ),
    Channel(
        4,
        "Phil Collins: Drummer First",
        "The lavender blossoms contain nurturing essential oils for the skin which have a balancing and soothing fragrance.",
        "gaplant"
    ),
    Channel(
        5,
        "blinky music",
        "Jojoba improves skin elasticity and prevents skin dehydration, making it perfect for sensitive or very dry skin.",
        "cwp"
    ),
    Channel(
        6,
        "Happy Metal New Year",
        "Tea tree is aimed to fight against acne and to treat acne prone skin. It’s considered one of the best natural alternatives to chemical substances.",
        "filtysock"
    ),
    Channel(
        7,
        "Like a Virgin",
        "Chamomile is the most extensive applications in natural cosmetics. It is a common flavoring agent in foods and beverages, and other products such as mouthwash, soaps, and cosmetics.",
        "goatjc"
    ),
    Channel(
        8,
        "Electronic Muzak!",
        "Rosemary (Rosmarinus officinalis) is deep green in colour and pungent in fragrance.",
        "Zakko"
    ),
    Channel(
        9,
        "Upcoming metal releases for 2025",
        "It contains high amounts of vitamin C and a potent antioxidant that can protect the skin from free radicals, stimulate collagen production, and reduce hyper pigmentation.",
        "FlatlineDixay"
    ),
    Channel(
        10,
        "Do you make Artificially Intelligent Music?",
        "Cilantro is high in vitamin C, an antioxidant that fights off damage-causing free radicals.",
        "maulla"
    ),
    Channel(
        11,
        "!!! F R I D A Y  M E T A L !!!",
        "Consuming enough vitamin C can help a person maintain skin health and appearance.  Vitamin C contributes to collagen production. Collagen supports the skin, promotes wound healing, and improves skin strength.",
        "Derwins"
    ),
    Channel(
        12,
        "Anime",
        "Cucumber is rich in all most Vitamin (C,A,B,K). Magnesium in cucumbers can promote skin elasticity and retain skin moisture.Potassium in cucumbers is another mineral that can help in hydrating the skin and balancing the electrolytes.",
        "BlackCat9"
    ),
)