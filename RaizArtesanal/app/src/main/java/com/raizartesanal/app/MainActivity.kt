package com.raizartesanal.app

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.raizartesanal.app.data.CartStore
import com.raizartesanal.app.data.Order
import com.raizartesanal.app.data.OrderStore
import com.raizartesanal.app.data.PaymentMethod
import com.raizartesanal.app.data.Product
import com.raizartesanal.app.data.SampleData
import com.raizartesanal.app.ui.ProductAdapter
import com.raizartesanal.app.ui.normalized
import com.raizartesanal.app.ui.toCop

/**
 * Actividad única que cambia de pantalla inflando layouts (igual que la primera entrega).
 * Flujo: Splash -> Login -> Inicio (comprador) -> Producto -> Carrito -> Pagar -> Confirmación.
 * Vendedor y Administrador siguen usando el panel de demostración.
 */
class MainActivity : Activity() {

    private enum class Tab { HOME, ORDERS, PROFILE }

    private lateinit var container: LinearLayout
    private lateinit var statusScrim: View
    private lateinit var content: FrameLayout
    private val handler = Handler(Looper.getMainLooper())

    private var onBack: () -> Unit = { finish() }
    private var role = "Comprador"
    private var userName = "Comprador"
    private var userEmail = ""
    private var buyerTab = Tab.HOME
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // El manifiesto usa el tema de arranque (fondo terracota); aquí pasamos al tema normal.
        setTheme(R.style.Theme_RaizArtesanal)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        statusScrim = View(this)
        content = FrameLayout(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(statusScrim, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))
            addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        setContentView(container)

        // Pantalla completa (edge-to-edge): la barra de estado se pinta con el color de cada pantalla
        // y el teclado / barra de navegación empujan el contenido hacia arriba.
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottom = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom
            statusScrim.layoutParams.height = bars.top
            statusScrim.requestLayout()
            v.setPadding(0, 0, 0, bottom)
            WindowInsetsCompat.CONSUMED
        }
        showSplash()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        onBack()
    }

    // ------------------------------------------------------------------ utilidades

    /** Muestra un layout. [top] pinta la barra de estado y [bottom] el área bajo el contenido. */
    private fun show(layoutId: Int, top: Int, bottom: Int = Color.WHITE, back: () -> Unit): View {
        handler.removeCallbacksAndMessages(null)
        onBack = back
        statusScrim.setBackgroundColor(top)
        container.setBackgroundColor(bottom)
        val controller = WindowCompat.getInsetsController(window, container)
        controller.isAppearanceLightStatusBars = ColorUtils.calculateLuminance(top) > 0.5
        controller.isAppearanceLightNavigationBars = ColorUtils.calculateLuminance(bottom) > 0.5
        content.removeAllViews()
        val view = layoutInflater.inflate(layoutId, content, false)
        content.addView(view)
        return view
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun logout() {
        CartStore.clear()
        OrderStore.clear()
        role = "Comprador"
        searchQuery = ""
        buyerTab = Tab.HOME
        showLogin()
    }

    private fun enterApp() {
        if (role == "Comprador") showBuyer(Tab.HOME) else showDashboard()
    }

    // ------------------------------------------------------------------ splash

    private fun showSplash() {
        val terracotta = getColor(R.color.terracotta)
        val v = show(R.layout.screen_splash, terracotta, terracotta) { finish() }
        val logo = v.findViewById<View>(R.id.splashContent)
        logo.alpha = 0f
        logo.scaleX = 0.9f
        logo.scaleY = 0.9f
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700).start()
        handler.postDelayed({ showLogin() }, 1800)
    }

    // ------------------------------------------------------------------ autenticación

    private fun showLogin() {
        val v = show(R.layout.screen_login, Color.WHITE) { finish() }
        v.findViewById<TextView>(R.id.toRegister).text = buildSpannedString {
            append("¿No tienes cuenta?  ")
            bold { color(getColor(R.color.terracotta)) { append("Regístrate") } }
        }
        v.findViewById<TextView>(R.id.toRegister).setOnClickListener { showRegister() }
        v.findViewById<TextView>(R.id.forgot).setOnClickListener {
            toast("La recuperación se conectará con Firebase Authentication.")
        }
        v.findViewById<MaterialButton>(R.id.google).setOnClickListener {
            toast("Google Sign-In requiere configuración de Firebase.")
        }
        v.findViewById<MaterialButton>(R.id.biometric).setOnClickListener {
            toast("El ingreso con huella se activará en la etapa de biometría.")
        }
        v.findViewById<MaterialButton>(R.id.submitLogin).setOnClickListener {
            val email = v.findViewById<EditText>(R.id.email).text.toString().trim()
            val password = v.findViewById<EditText>(R.id.password).text.toString()
            when {
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> toast("Ingresa un correo válido.")
                password.isBlank() -> toast("Ingresa tu contraseña.")
                else -> {
                    // Demo: aún no hay backend. Los correos que empiezan por "vendedor" o "admin"
                    // abren esos paneles; cualquier otro entra como comprador.
                    role = when {
                        email.startsWith("admin", ignoreCase = true) -> "Administrador"
                        email.startsWith("vendedor", ignoreCase = true) -> "Vendedor"
                        else -> "Comprador"
                    }
                    userEmail = email
                    userName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    toast("Modo demostración: no se ha validado una cuenta real.")
                    enterApp()
                }
            }
        }
    }

    private fun showRegister() {
        val v = show(R.layout.screen_register, Color.WHITE) { showLogin() }
        v.findViewById<TextView>(R.id.back).setOnClickListener { showLogin() }
        v.findViewById<MaterialButton>(R.id.submitRegister).setOnClickListener {
            val name = v.findViewById<EditText>(R.id.name).text.toString().trim()
            val email = v.findViewById<EditText>(R.id.email).text.toString().trim()
            val password = v.findViewById<EditText>(R.id.password).text.toString()
            val consent = v.findViewById<CheckBox>(R.id.consent).isChecked
            val seller = v.findViewById<RadioButton>(R.id.roleSeller).isChecked
            if (name.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                password.length < 6 || !consent
            ) {
                toast("Completa los datos, usa un correo válido, mínimo 6 caracteres y acepta la política.")
                return@setOnClickListener
            }
            role = if (seller) "Vendedor" else "Comprador"
            userName = name
            userEmail = email
            toast("Registro de demostración completado. Falta conectar Firebase.")
            enterApp()
        }
    }

    // ------------------------------------------------------------------ comprador: inicio / compras / perfil

    private fun showBuyer(tab: Tab) {
        val terracotta = getColor(R.color.terracotta)
        val shell = show(R.layout.screen_buyer_shell, terracotta) { finish() }

        shell.findViewById<View>(R.id.navHome).setOnClickListener { selectTab(shell, Tab.HOME) }
        shell.findViewById<View>(R.id.navOrders).setOnClickListener { selectTab(shell, Tab.ORDERS) }
        shell.findViewById<View>(R.id.navProfile).setOnClickListener { selectTab(shell, Tab.PROFILE) }
        shell.findViewById<View>(R.id.navCart).setOnClickListener { showCart() }
        shell.findViewById<View>(R.id.cartButton).setOnClickListener { showCart() }

        val badge = shell.findViewById<TextView>(R.id.cartBadge)
        val count = CartStore.itemCount()
        badge.visibility = if (count > 0) View.VISIBLE else View.GONE
        badge.text = count.toString()

        selectTab(shell, tab)
    }

    private fun selectTab(shell: View, tab: Tab) {
        buyerTab = tab
        onBack = { if (tab == Tab.HOME) finish() else selectTab(shell, Tab.HOME) }

        val frame = shell.findViewById<FrameLayout>(R.id.tabContent)
        frame.removeAllViews()
        val title = shell.findViewById<TextView>(R.id.headerTitle)
        val layoutId = when (tab) {
            Tab.HOME -> R.layout.tab_home
            Tab.ORDERS -> R.layout.tab_orders
            Tab.PROFILE -> R.layout.tab_profile
        }
        val tabView = layoutInflater.inflate(layoutId, frame, false)
        frame.addView(tabView)
        when (tab) {
            Tab.HOME -> { title.text = "Raíz Artesanal"; bindHome(tabView) }
            Tab.ORDERS -> { title.text = "Mis compras"; bindOrders(tabView) }
            Tab.PROFILE -> { title.text = "Mi perfil"; bindProfile(tabView) }
        }

        val items = listOf(
            Triple(R.id.navHomeIcon, R.id.navHomeLabel, Tab.HOME),
            Triple(R.id.navOrdersIcon, R.id.navOrdersLabel, Tab.ORDERS),
            Triple(R.id.navProfileIcon, R.id.navProfileLabel, Tab.PROFILE)
        )
        items.forEach { (iconId, labelId, itemTab) ->
            val selected = itemTab == tab
            val tint = getColor(if (selected) R.color.terracotta else R.color.text_muted)
            shell.findViewById<ImageView>(iconId).imageTintList = ColorStateList.valueOf(tint)
            shell.findViewById<TextView>(labelId).apply {
                setTextColor(tint)
                setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            }
        }
    }

    private fun bindHome(v: View) {
        val adapter = ProductAdapter { showProduct(it) }
        val list = v.findViewById<RecyclerView>(R.id.productList)
        list.layoutManager = GridLayoutManager(this, 2)
        list.adapter = adapter
        val empty = v.findViewById<TextView>(R.id.emptySearch)
        val search = v.findViewById<EditText>(R.id.searchInput)

        fun refresh() {
            val q = searchQuery.normalized().trim()
            val results = SampleData.products.filter {
                q.isEmpty() || it.name.normalized().contains(q) || it.craftInfo.normalized().contains(q)
            }
            adapter.submit(results)
            empty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }

        search.setText(searchQuery)
        search.doAfterTextChanged {
            searchQuery = it?.toString().orEmpty()
            refresh()
        }
        refresh()
    }

    private fun bindOrders(v: View) {
        val list = v.findViewById<LinearLayout>(R.id.ordersList)
        val orders = OrderStore.all()
        v.findViewById<View>(R.id.ordersEmpty).visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        orders.forEach { order ->
            val row = layoutInflater.inflate(R.layout.item_order, list, false)
            row.findViewById<TextView>(R.id.orderNumber).text = "Pedido ${order.number}"
            row.findViewById<TextView>(R.id.orderStatus).text = order.status
            row.findViewById<TextView>(R.id.orderSummary).text = order.summary
            row.findViewById<TextView>(R.id.orderTotal).text = "${order.total.toCop()} · ${order.method}"
            row.findViewById<TextView>(R.id.orderDate).text = order.date
            list.addView(row)
        }
    }

    private fun bindProfile(v: View) {
        v.findViewById<TextView>(R.id.avatarInitial).text = userName.take(1).uppercase()
        v.findViewById<TextView>(R.id.profileName).text = userName
        v.findViewById<TextView>(R.id.profileEmail).text = userEmail.ifBlank { "Sin correo registrado" }
        val rows = v.findViewById<LinearLayout>(R.id.profileRows)
        listOf("Mis datos", "Dirección de entrega", "Ingreso con huella", "Privacidad y consentimientos")
            .forEach { label ->
                val row = TextView(this).apply {
                    text = label
                    textSize = 15f
                    setTextColor(getColor(R.color.earth_brown))
                    setBackgroundResource(R.drawable.bg_outline)
                    setPadding(dp(16), dp(16), dp(16), dp(16))
                    setOnClickListener { toast("Esta función se implementará en la siguiente etapa.") }
                }
                rows.addView(
                    row,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(10) }
                )
            }
        v.findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener { logout() }
    }

    // ------------------------------------------------------------------ comprador: detalle de producto

    private fun showProduct(product: Product) {
        val v = show(R.layout.screen_product_detail, getColor(R.color.sand_image)) { showBuyer(Tab.HOME) }
        v.findViewById<View>(R.id.back).setOnClickListener { onBack() }
        v.findViewById<TextView>(R.id.productName).text = product.name
        v.findViewById<TextView>(R.id.productPrice).text = product.price.toCop()
        v.findViewById<TextView>(R.id.productInfo).text = product.craftInfo
        v.findViewById<TextView>(R.id.productDescription).text = product.description

        val stockLabel = v.findViewById<TextView>(R.id.stockLabel)
        when {
            product.stock <= 0 -> { stockLabel.text = "Agotado"; stockLabel.setTextColor(getColor(R.color.danger)) }
            product.stock <= 3 -> {
                stockLabel.text = "¡Últimas ${product.stock} unidades!"
                stockLabel.setTextColor(getColor(R.color.danger))
            }
            else -> { stockLabel.text = "Stock: ${product.stock}"; stockLabel.setTextColor(getColor(R.color.success)) }
        }

        var qty = 1
        val qtyView = v.findViewById<TextView>(R.id.qtyValue)
        val addButton = v.findViewById<MaterialButton>(R.id.addToCart)
        addButton.isEnabled = product.stock > 0
        v.findViewById<View>(R.id.qtyMinus).setOnClickListener {
            if (qty > 1) qty--
            qtyView.text = qty.toString()
        }
        v.findViewById<View>(R.id.qtyPlus).setOnClickListener {
            val available = product.stock - CartStore.quantityOf(product.id)
            if (qty < available) qty++ else toast("No hay más unidades disponibles de este producto.")
            qtyView.text = qty.toString()
        }
        addButton.setOnClickListener {
            val added = CartStore.add(product, qty)
            if (added == 0) {
                toast("Ya tienes todo el stock disponible en tu carrito.")
            } else {
                qty = 1
                qtyView.text = "1"
                Snackbar.make(v, "Agregado al carrito", Snackbar.LENGTH_LONG)
                    .setAction("VER CARRITO") { showCart() }
                    .setActionTextColor(getColor(R.color.gold))
                    .show()
            }
        }
    }

    // ------------------------------------------------------------------ comprador: carrito

    private fun showCart() {
        val v = show(R.layout.screen_cart, getColor(R.color.terracotta)) { showBuyer(Tab.HOME) }
        v.findViewById<View>(R.id.back).setOnClickListener { onBack() }
        v.findViewById<View>(R.id.exploreButton).setOnClickListener { showBuyer(Tab.HOME) }
        v.findViewById<View>(R.id.checkoutButton).setOnClickListener { showCheckout() }
        renderCart(v)
    }

    private fun renderCart(v: View) {
        val linesBox = v.findViewById<LinearLayout>(R.id.cartLines)
        linesBox.removeAllViews()
        val lines = CartStore.lines()
        val hasItems = lines.isNotEmpty()
        v.findViewById<View>(R.id.cartScroll).visibility = if (hasItems) View.VISIBLE else View.GONE
        v.findViewById<View>(R.id.checkoutButton).visibility = if (hasItems) View.VISIBLE else View.GONE
        v.findViewById<View>(R.id.emptyCart).visibility = if (hasItems) View.GONE else View.VISIBLE
        v.findViewById<TextView>(R.id.cartTotal).text = CartStore.subtotal().toCop()

        lines.forEach { line ->
            val product = line.product
            val row = layoutInflater.inflate(R.layout.item_cart_line, linesBox, false)
            row.findViewById<TextView>(R.id.lineName).text = product.name
            row.findViewById<TextView>(R.id.linePrice).text = product.price.toCop()
            row.findViewById<TextView>(R.id.lineQty).text = line.quantity.toString()
            row.findViewById<View>(R.id.lineMinus).setOnClickListener {
                if (line.quantity > 1) {
                    CartStore.setQuantity(product, line.quantity - 1)
                    renderCart(v)
                }
            }
            row.findViewById<View>(R.id.linePlus).setOnClickListener {
                if (line.quantity < product.stock) {
                    CartStore.setQuantity(product, line.quantity + 1)
                    renderCart(v)
                } else {
                    toast("Solo hay ${product.stock} unidades disponibles de ${product.name}.")
                }
            }
            row.findViewById<View>(R.id.lineRemove).setOnClickListener {
                CartStore.remove(product.id)
                renderCart(v)
                Snackbar.make(v, "${product.name} eliminado", Snackbar.LENGTH_LONG)
                    .setAction("DESHACER") {
                        CartStore.add(product, line.quantity)
                        renderCart(v)
                    }
                    .setActionTextColor(getColor(R.color.gold))
                    .show()
            }
            linesBox.addView(row)
        }
    }

    // ------------------------------------------------------------------ comprador: pago

    private fun showCheckout() {
        if (CartStore.itemCount() == 0) { showCart(); return }
        val v = show(R.layout.screen_checkout, getColor(R.color.terracotta)) { showCart() }
        v.findViewById<View>(R.id.back).setOnClickListener { onBack() }

        v.findViewById<TextView>(R.id.summarySubtotal).text = CartStore.subtotal().toCop()
        v.findViewById<TextView>(R.id.summaryShipping).text = CartStore.shipping().toCop()
        v.findViewById<TextView>(R.id.summaryTotal).text = CartStore.total().toCop()

        var method = PaymentMethod.CARD
        val options = listOf(
            Triple(PaymentMethod.CARD, R.id.optCard, R.id.radioCard),
            Triple(PaymentMethod.PSE, R.id.optPse, R.id.radioPse),
            Triple(PaymentMethod.NEQUI, R.id.optNequi, R.id.radioNequi)
        )
        fun renderMethods() {
            options.forEach { (m, cardId, radioId) ->
                val selected = m == method
                v.findViewById<View>(cardId).setBackgroundResource(
                    if (selected) R.drawable.bg_option_selected else R.drawable.bg_outline
                )
                v.findViewById<RadioButton>(radioId).isChecked = selected
            }
        }
        options.forEach { (m, cardId, _) ->
            v.findViewById<View>(cardId).setOnClickListener { method = m; renderMethods() }
        }
        renderMethods()

        val pay = v.findViewById<MaterialButton>(R.id.payButton)
        pay.setOnClickListener {
            val address = v.findViewById<EditText>(R.id.addressInput).text.toString().trim()
            if (address.length < 6) {
                toast("Ingresa una dirección de entrega válida.")
                return@setOnClickListener
            }
            // Pago simulado. La integración real (Wompi) tokeniza los datos fuera de la app:
            // nunca se deben guardar números de tarjeta aquí.
            pay.isEnabled = false
            pay.text = "Procesando pago…"
            onBack = {}
            handler.postDelayed({
                showOrderSuccess(OrderStore.create(method, address))
            }, 1200)
        }
    }

    private fun showOrderSuccess(order: Order) {
        val v = show(R.layout.screen_order_success, Color.WHITE) { showBuyer(Tab.HOME) }
        v.findViewById<TextView>(R.id.successOrder).text = "Pedido ${order.number}"
        v.findViewById<TextView>(R.id.successDetail).text =
            "${order.total.toCop()} · ${order.method}\nEnvío a: ${order.address}"
        v.findViewById<View>(R.id.viewOrders).setOnClickListener { showBuyer(Tab.ORDERS) }
        v.findViewById<View>(R.id.keepShopping).setOnClickListener { showBuyer(Tab.HOME) }
    }

    // ------------------------------------------------------------------ vendedor / administrador (demo)

    private fun showDashboard() {
        val cream = getColor(R.color.cream)
        val v = show(R.layout.screen_dashboard, cream, cream) { logout() }
        v.findViewById<TextView>(R.id.back).setOnClickListener { logout() }
        v.findViewById<TextView>(R.id.greeting).text = "Hola, $role"
        val subtitle = v.findViewById<TextView>(R.id.subtitle)
        val a1 = v.findViewById<MaterialButton>(R.id.actionOne)
        val a2 = v.findViewById<MaterialButton>(R.id.actionTwo)
        val a3 = v.findViewById<MaterialButton>(R.id.actionThree)
        val a4 = v.findViewById<MaterialButton>(R.id.actionFour)
        listOf(R.id.actionFive, R.id.actionSix, R.id.actionSeven).forEach {
            v.findViewById<View>(it).visibility = View.GONE
        }

        if (role == "Vendedor") {
            subtitle.text = "Administra tu taller, tus productos y los pedidos recibidos."
            a1.text = "Mis productos"; a2.text = "Crear producto"; a3.text = "Pedidos recibidos"
        } else {
            subtitle.text = "Gestiona usuarios, modera publicaciones y consulta el estado de la tienda."
            a1.text = "Administrar usuarios"; a2.text = "Moderar productos"; a3.text = "Reportes"
        }
        a4.text = "Mi perfil"

        a1.setOnClickListener {
            if (role == "Vendedor") showContent("Mis productos", "Administra las publicaciones de tu taller.",
                listOf("Tus productos aparecerán aquí.", "Puedes crear, editar o pausar publicaciones."))
            else showContent("Administración de usuarios", "Consulta y gestiona las cuentas registradas.",
                listOf("Compradores", "Vendedores pendientes de verificación", "Bloqueos y auditoría"))
        }
        a2.setOnClickListener {
            if (role == "Vendedor") showContent("Crear producto", "Formulario de publicación (versión inicial).",
                listOf("Nombre del producto", "Descripción", "Precio en COP", "Stock", "Categoría",
                    "Fotografías (CameraX / galería)", "Ubicación del taller"))
            else showContent("Moderación de productos", "Revisa y decide sobre las publicaciones.",
                listOf("Pendientes de aprobación", "Aprobados", "Rechazados con motivo"))
        }
        a3.setOnClickListener {
            if (role == "Vendedor") showContent("Pedidos recibidos", "Gestiona los pedidos de tus compradores.",
                listOf("Aceptar pedido", "Rechazar pedido con motivo", "Marcar como enviado y agregar guía"))
            else showContent("Reportes", "Indicadores generales de la plataforma.",
                listOf("Ventas totales", "Usuarios registrados", "Productos publicados", "Pedidos por estado"))
        }
        a4.setOnClickListener {
            showContent("Mi perfil", "Información de la cuenta y preferencias.",
                listOf("Nombre", "Correo electrónico", "Dirección", "Foto de perfil",
                    "Configuración biométrica", "Privacidad y consentimientos"))
        }
    }

    private fun showContent(titleText: String, descriptionText: String, rows: List<String>) {
        val cream = getColor(R.color.cream)
        val v = show(R.layout.screen_content, cream, cream) { showDashboard() }
        v.findViewById<TextView>(R.id.back).setOnClickListener { showDashboard() }
        v.findViewById<TextView>(R.id.title).text = titleText
        v.findViewById<TextView>(R.id.description).text = descriptionText
        val items = v.findViewById<LinearLayout>(R.id.items)
        rows.forEach { row ->
            val item = TextView(this).apply {
                text = row
                textSize = 16f
                setTextColor(getColor(R.color.earth_brown))
                setBackgroundResource(R.drawable.bg_card)
            }
            items.addView(
                item,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            )
        }
        v.findViewById<MaterialButton>(R.id.primaryAction).setOnClickListener {
            Toast.makeText(this, "Esta función se implementará en la siguiente etapa.", Toast.LENGTH_SHORT).show()
        }
    }
}
