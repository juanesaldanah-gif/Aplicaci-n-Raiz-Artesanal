package com.raizartesanal.app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Carrito en memoria (demo). Más adelante se persistirá con Room. */
object CartStore {
    /** Envío fijo de demostración; luego se calculará por ciudad/peso. */
    const val SHIPPING = 14_000

    private val quantities = LinkedHashMap<Int, Int>()   // productId -> cantidad

    fun lines(): List<CartLine> =
        quantities.mapNotNull { (id, qty) -> SampleData.byId(id)?.let { CartLine(it, qty) } }

    fun quantityOf(productId: Int): Int = quantities[productId] ?: 0

    /** Agrega sin superar el stock. Devuelve cuántas unidades se agregaron realmente. */
    fun add(product: Product, qty: Int): Int {
        val current = quantityOf(product.id)
        val newQty = minOf(current + qty, product.stock)
        if (newQty <= current) return 0
        quantities[product.id] = newQty
        return newQty - current
    }

    fun setQuantity(product: Product, qty: Int) {
        if (product.stock <= 0) return
        quantities[product.id] = qty.coerceIn(1, product.stock)
    }

    fun remove(productId: Int) { quantities.remove(productId) }
    fun clear() { quantities.clear() }

    fun itemCount(): Int = quantities.values.sum()
    fun subtotal(): Int = lines().sumOf { it.subtotal }
    fun shipping(): Int = if (quantities.isEmpty()) 0 else SHIPPING
    fun total(): Int = subtotal() + shipping()
}

/** Historial de compras en memoria (demo). */
object OrderStore {
    private val orders = mutableListOf<Order>()

    fun all(): List<Order> = orders.toList()

    /** Crea el pedido con el contenido actual del carrito y lo vacía. */
    fun create(method: PaymentMethod, address: String): Order {
        val lines = CartStore.lines()
        val order = Order(
            number = String.format(Locale.US, "RA-%04d", 1001 + orders.size),
            date = SimpleDateFormat("d MMM yyyy", Locale("es", "CO")).format(Date()),
            summary = lines.joinToString(", ") { "${it.quantity} × ${it.product.name}" },
            address = address,
            total = CartStore.total(),
            method = method.label,
            status = "Pagado"
        )
        orders.add(0, order)
        CartStore.clear()
        return order
    }

    fun clear() { orders.clear() }
}
