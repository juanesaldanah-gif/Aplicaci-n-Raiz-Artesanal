package com.raizartesanal.app.data

data class Product(
    val id: Int,
    val name: String,
    val price: Int,          // COP, sin decimales
    val craftInfo: String,   // "Tejida a mano · Taller Sol y Luna"
    val description: String,
    val stock: Int
)

data class CartLine(val product: Product, val quantity: Int) {
    val subtotal: Int get() = product.price * quantity
}

enum class PaymentMethod(val label: String) {
    CARD("Tarjeta"),
    PSE("PSE"),
    NEQUI("Nequi")
}

data class Order(
    val number: String,
    val date: String,
    val summary: String,
    val address: String,
    val total: Int,
    val method: String,
    val status: String
)
