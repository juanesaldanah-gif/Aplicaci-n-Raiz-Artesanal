package com.raizartesanal.app.data

/** Datos de demostración. Se reemplazarán por Firestore / Room en la siguiente etapa. */
object SampleData {
    val products: List<Product> = listOf(
        Product(1, "Mochila wayuu", 180_000, "Tejida a mano · Taller Sol y Luna",
            "Mochila de lana y algodón con diseños tradicionales. Cada pieza es única.", 6),
        Product(2, "Vasija de barro", 95_000, "Moldeada a mano · Taller Tierra Viva",
            "Vasija de barro cocido, pulida a mano y decorada con motivos ancestrales.", 8),
        Product(3, "Sombrero vueltiao", 120_000, "Tejido en caña flecha · Taller Zenú",
            "Sombrero tejido en caña flecha siguiendo la técnica tradicional de los artesanos zenúes.", 4),
        Product(4, "Ruana de lana", 210_000, "Hilada a mano · Taller Páramo",
            "Ruana de lana virgen, abrigada y suave, tejida en telar de pedal.", 3),
        Product(5, "Canasto de iraca", 65_000, "Trenzado a mano · Taller Fibra Viva",
            "Canasto trenzado en fibra de iraca, ideal para organizar o decorar.", 10),
        Product(6, "Hamaca de algodón", 150_000, "Tejida en telar · Taller Sol y Luna",
            "Hamaca de algodón resistente y fresca, con flecos tejidos a mano.", 5)
    )

    fun byId(id: Int): Product? = products.firstOrNull { it.id == id }
}
