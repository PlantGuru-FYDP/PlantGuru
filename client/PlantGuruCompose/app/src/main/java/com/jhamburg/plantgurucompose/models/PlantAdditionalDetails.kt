package com.jhamburg.plantgurucompose.models

enum class PlantCategory(val displayName: String) {
    FOLIAGE("Foliage Plant"),
    SUCCULENT_CACTUS("Succulent & Cactus"),
    FLOWERING("Flowering Plant"),
    PALM("Palm"),
    FERN("Fern"),
    VINE("Vine & Climber"),
    HERB("Herb"),
    AIR_PLANT("Air Plant"),
    VEGETABLE("Vegetable"),
    TREE("Tree"),
    ALLIUM("Allium"),
    OTHER("Other");

    override fun toString(): String = displayName
}

enum class PlantSubType(val category: PlantCategory, val displayName: String) {
    // Foliage Plants
    MONSTERA(PlantCategory.FOLIAGE, "Monstera"),
    POTHOS(PlantCategory.FOLIAGE, "Pothos"),
    PHILODENDRON(PlantCategory.FOLIAGE, "Philodendron"),
    SNAKE_PLANT(PlantCategory.FOLIAGE, "Snake Plant"),
    FIDDLE_LEAF_FIG(PlantCategory.FOLIAGE, "Fiddle Leaf Fig"),
    RUBBER_PLANT(PlantCategory.FOLIAGE, "Rubber Plant"),
    CHINESE_EVERGREEN(PlantCategory.FOLIAGE, "Chinese Evergreen"),
    CALATHEA(PlantCategory.FOLIAGE, "Calathea"),
    DRACAENA(PlantCategory.FOLIAGE, "Dracaena"),
    ZZ_PLANT(PlantCategory.FOLIAGE, "ZZ Plant"),
    COLEUS(PlantCategory.FOLIAGE, "Coleus"),
    PURPLE_COLEUS(PlantCategory.FOLIAGE, "Purple Coleus"),
    SPIDER_PLANT(PlantCategory.FOLIAGE, "Spider Plant"),

    // Succulents & Cacti
    ALOE_VERA(PlantCategory.SUCCULENT_CACTUS, "Aloe Vera"),
    JADE_PLANT(PlantCategory.SUCCULENT_CACTUS, "Jade Plant"),
    ECHEVERIA(PlantCategory.SUCCULENT_CACTUS, "Echeveria"),
    HAWORTHIA(PlantCategory.SUCCULENT_CACTUS, "Haworthia"),
    CHRISTMAS_CACTUS(PlantCategory.SUCCULENT_CACTUS, "Christmas Cactus"),
    BARREL_CACTUS(PlantCategory.SUCCULENT_CACTUS, "Barrel Cactus"),
    PRICKLY_PEAR(PlantCategory.SUCCULENT_CACTUS, "Prickly Pear"),
    STRING_OF_PEARLS(PlantCategory.SUCCULENT_CACTUS, "String of Pearls"),

    // Flowering Plants
    PEACE_LILY(PlantCategory.FLOWERING, "Peace Lily"),
    ORCHID(PlantCategory.FLOWERING, "Orchid"),
    AFRICAN_VIOLET(PlantCategory.FLOWERING, "African Violet"),
    ANTHURIUM(PlantCategory.FLOWERING, "Anthurium"),
    BEGONIA(PlantCategory.FLOWERING, "Begonia"),
    CYCLAMEN(PlantCategory.FLOWERING, "Cyclamen"),
    KALANCHOE(PlantCategory.FLOWERING, "Kalanchoe"),

    // Palms
    PARLOR_PALM(PlantCategory.PALM, "Parlor Palm"),
    ARECA_PALM(PlantCategory.PALM, "Areca Palm"),
    KENTIA_PALM(PlantCategory.PALM, "Kentia Palm"),
    MAJESTY_PALM(PlantCategory.PALM, "Majesty Palm"),
    PONYTAIL_PALM(PlantCategory.PALM, "Ponytail Palm"),

    // Ferns
    BOSTON_FERN(PlantCategory.FERN, "Boston Fern"),
    BIRDS_NEST_FERN(PlantCategory.FERN, "Bird's Nest Fern"),
    MAIDENHAIR_FERN(PlantCategory.FERN, "Maidenhair Fern"),
    STAGHORN_FERN(PlantCategory.FERN, "Staghorn Fern"),
    ASPARAGUS_FERN(PlantCategory.FERN, "Asparagus Fern"),

    // Vines & Climbers
    ENGLISH_IVY(PlantCategory.VINE, "English Ivy"),
    DEVILS_IVY(PlantCategory.VINE, "Devil's Ivy"),
    STRING_OF_HEARTS(PlantCategory.VINE, "String of Hearts"),
    HOYA(PlantCategory.VINE, "Hoya"),
    TRADESCANTIA(PlantCategory.VINE, "Tradescantia"),
    MARBLE_QUEEN(PlantCategory.VINE, "Marble Queen Pothos"),

    // Herbs
    BASIL(PlantCategory.HERB, "Basil"),
    MINT(PlantCategory.HERB, "Mint"),
    ROSEMARY(PlantCategory.HERB, "Rosemary"),
    THYME(PlantCategory.HERB, "Thyme"),
    OREGANO(PlantCategory.HERB, "Oregano"),
    SAGE(PlantCategory.HERB, "Sage"),
    PARSLEY(PlantCategory.HERB, "Parsley"),

    // Allium
    GARLIC(PlantCategory.ALLIUM, "Garlic"),

    // Air Plants
    TILLANDSIA(PlantCategory.AIR_PLANT, "Tillandsia"),
    SPANISH_MOSS(PlantCategory.AIR_PLANT, "Spanish Moss"),
    SKY_PLANT(PlantCategory.AIR_PLANT, "Sky Plant"),

    // Vegetables
    TOMATO(PlantCategory.VEGETABLE, "Tomato"),
    PEPPER(PlantCategory.VEGETABLE, "Pepper"),
    LETTUCE(PlantCategory.VEGETABLE, "Lettuce"),
    CARROT(PlantCategory.VEGETABLE, "Carrot"),

    // Trees
    OLIVE_TREE(PlantCategory.TREE, "Olive Tree"),

    // Other
    OTHER(PlantCategory.OTHER, "Other");

    override fun toString(): String = displayName

    companion object {
        fun getSubTypesForCategory(category: PlantCategory): List<PlantSubType> {
            return values().filter { it.category == category }
        }

        fun fromString(value: String): PlantSubType? {
            return values().find { it.displayName.equals(value, ignoreCase = true) }
        }
    }
}

data class PlantAdditionalDetails(
    val scientificName: String = "",
    val plantType: String = "",
    val createdOn: Long,
    val description: String? = null,
    val careInstructions: String? = null,
    var imageUri: String? = null
) {
    // Helper functions to convert between string and enum types
    fun getCategory(): PlantCategory? = PlantCategory.values().find {
        it.displayName.equals(plantType, ignoreCase = true)
    }

    fun getSubType(): PlantSubType? = PlantSubType.fromString(scientificName)

    companion object {
        fun fromCategoryAndSubType(
            category: PlantCategory,
            subType: PlantSubType,
            createdOn: Long,
            imageUri: String? = null,
            description: String? = null,
            careInstructions: String? = null
        ): PlantAdditionalDetails {
            return PlantAdditionalDetails(
                scientificName = subType.displayName,
                plantType = category.displayName,
                createdOn = createdOn,
                imageUri = imageUri,
                description = description,
                careInstructions = careInstructions
            )
        }
    }
}
