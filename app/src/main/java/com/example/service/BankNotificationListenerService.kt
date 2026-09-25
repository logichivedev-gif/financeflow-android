package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.Normalizer
import java.util.regex.Pattern

class BankNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val transactionMutex = Mutex()

    companion object {
        private const val TAG = "BankNotifListener"
        private const val CHANNEL_ID = "bank_interceptor_channel"

        /**
         * Reintenta o asegura la vinculación activa del NotificationListenerService con el sistema operativo Android.
         * Útil cuando el SO pierde el enlace tras reinstalaciones o optimizaciones de batería.
         */
        fun ensureServiceBound(context: Context) {
            val component = ComponentName(context, BankNotificationListenerService::class.java)
            Log.d("SERVICE_LIFECYCLE", "ensureServiceBound ejecutado con componente: ${component.flattenToShortString()}")
            Log.d(TAG, "Garantizando vinculación de BankNotificationListenerService...")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestRebind(component)
                    Log.i(TAG, "requestRebind invocado exitosamente para ${component.flattenToShortString()}")
                } catch (e: Exception) {
                    Log.w(TAG, "requestRebind no disponible o error: ${e.message}")
                }
            }

            
            try {
                val pm = context.packageManager
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Reinicio de componente NotificationListener ejecutado")
            } catch (e: Exception) {
                Log.e(TAG, "Error al reiniciar estado del componente: ${e.message}")
            }
        }

        
        private val ALLOWED_BANK_PACKAGES = setOf(
            
            "es.caixabank.caixabanknow",
            "es.imagin.app",
            "es.caixabank.pay",
            "es.caixabank.empresas",
            "es.lacaixa.mobile.android.newportal",
            
            "es.bbva.mobileP2P",
            "es.bbva.españa",
            "es.bbva.mobile",
            "es.bbva.netcash",
            "com.bbva.bbvacontigo",
            
            "es.bancosantander.apps",
            "es.bancosantander.empresas",
            "com.santander.app",
            "es.gruposantander.santanderpymes",
            
            "com.bancsabadell.bsapp",
            "com.bancsabadell.bsempresas",
            "com.bancsabadell.wallet",
            
            "es.ing.direct.app",
            "com.ing.mobile",
            
            "com.revolut.revolut",
            
            "de.number26.android",
            
            "com.klarna.mosaik",
            
            "com.paypal.android.p2pmobile",
            
            "es.redsys.bizum",
            
            "es.unaja.unicaja",
            "com.unicajabanco.unicaja",
            
            "es.kutxabank.android",
            "es.cajasur.android",
            
            "es.abanca.android",
            
            "es.bankinter.mobile",
            "es.bankinter.wallet",
            "es.bankinter.broker",
            
            "com.openbank.mobile",
            
            "es.cajamar.handybank",
            "com.rsi",
            "es.ruralvia.movil",
            
            "es.ibercaja.mobile",
            "es.ibercaja.pay",
            
            "es.laboralkutxa.mobile",
            
            "es.starmobile.waylet",
            
            "com.wise.android",
            
            "com.traderepublic.app",
            "com.trade_republic.app",
            
            "com.scalable.capital",
            
            "es.triodos.mobile",
            
            "com.myandbank.app",
            
            "es.evobanco.bancamovil",
            
            "es.pibank.app",
            
            "com.db.pbc.dbspain",
            
            "com.imaginecurve.curve.prd",
            
            "money.vivid.app",
            
            "com.bunq.android",
            
            "com.google.android.apps.walletnfcrel"
        )

        
        
        private val CURRENCY_AMOUNT_PATTERN: Pattern = Pattern.compile(
            """(?:[€$£]|EUR|USD|GBP)\s*([+-]?\s*\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{1,2})?|[+-]?\s*\d+(?:[.,]\d{1,2})?)|([+-]?\s*\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{1,2})?|[+-]?\s*\d+(?:[.,]\d{1,2})?)\s*(?:[€$£]|EUR|USD|GBP)""",
            Pattern.CASE_INSENSITIVE
        )

        
        
        private val KEYWORD_AMOUNT_PATTERN: Pattern = Pattern.compile(
            """(?:IMPORTE|CARGO|ABONO|MONTO|TOTAL|PRECIO|VALOR|CANTIDAD|SALDO|PAGO|COBRO|GASTO|BIZUM|COMPRA|TRANSFERENCIA|TRASPASO PROPIO|TRASPASO|DEVOLUCION|REEMBOLSO|ENVIO|ENVIADO|RECIBIDO|ADEUDO|ADEUDO DIRECTO|ADEUDO SEPA|CUOTA|FINANCIACION|INGRESO|POR VALOR DE|POR IMPORTE DE)\s*(?:DE|POR|:)?\s*([+-]?\s*\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{1,2})?|[+-]?\s*\d+(?:[.,]\d{1,2})?)""",
            Pattern.CASE_INSENSITIVE
        )

        private val UNIVERSAL_INCOME_KEYWORDS = listOf(
            "ABONO DE TRASPASO", "TRASPASO PROPIO", "ABONO DE", "BIZUM RECIBIDO",
            "BIZUM DE", "TRANSFERENCIA RECIBIDA", "INGRESO DE", "A SU FAVOR",
            "HAS RECIBIDO UN BIZUM", "TE HA ENVIADO UN BIZUM",
            "TE HA ENVIADO", "HAS RECIBIDO", "DINERO RECIBIDO", "PAGO RECIBIDO",
            "TRANSFERENCIA A TU FAVOR", "ABONO EN CUENTA",
            "INGRESO EN CUENTA", "INGRESO EN EFECTIVO", "INGRESO CAJERO", "NOMINA",
            "DEVOLUCION", "REEMBOLSO", "ABONO", "INGRESO", "RECIBIDO", "RECEPCION",
            "A FAVOR", "SALDO A FAVOR", "CREDITO", "ABONO CAJERO", "INGRESO EN CAJERO",
            "ABONO EN CAJERO", "INGRESO DE EFECTIVO", "DEPOSITO"
        )

        private val UNIVERSAL_EXPENSE_KEYWORDS = listOf(
            "CUOTA", "FINANCIACION", "ADEUDO SEPA", "ADEUDO DIRECTO",
            "COBRO DE", "RECIBO DE", "CARGO POR",
            "BIZUM ENVIADO", "HAS ENVIADO UN BIZUM", "HAS ENVIADO", "PAGO CON TARJETA",
            "COMPRA CON TARJETA", "PAGO EN", "COMPRA EN", "CARGO EN CUENTA", "ADEUDO",
            "TRANSFERENCIA EMITIDA", "TRANSFERENCIA REALIZADA", "TRANSFERENCIA ENVIADA",
            "RETIRADA EN CAJERO", "RETIRADA DE EFECTIVO", "RETIRADA CAJERO",
            "REINTEGRO CAJERO", "REINTEGRO EN CAJERO", "REINTEGRO", "REINT. CAJERO",
            "REINT.CAJERO", "REINT CAJERO", "DISPOSICION CAJERO", "DISPOSICION",
            "PAGO", "COMPRA", "CARGO", "TRANSFERENCIA", "COBRO",
            "ENVIO", "ENVIADO", "LIQUIDACION", "RECIBO", "GASTO",
            "CARTA DE PAGO", "SUSCRIPCION", "OPERACION CON TARJETA", "AUTORIZACION",
            "PAGO AUTORIZADO"
        )
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("SERVICE_LIFECYCLE", "onCreate ejecutado")
        createNotificationChannel()
        Log.i(TAG, "BankNotificationListenerService onCreate()")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("SERVICE_LIFECYCLE", "onListenerConnected ejecutado - Servicio enlazado con éxito")
        Log.i(TAG, "🟢 onListenerConnected: Servicio de notificaciones vinculado y escuchando activamente")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("SERVICE_LIFECYCLE", "onListenerDisconnected ejecutado - Servicio desenlazado")
        Log.w(TAG, "⚠️ onListenerDisconnected: Servicio desconectado por el sistema. Solicitando rebind...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                requestRebind(ComponentName(this, BankNotificationListenerService::class.java))
                Log.i(TAG, "requestRebind invocado en onListenerDisconnected")
            } catch (e: Exception) {
                Log.e(TAG, "Fallo al solicitar requestRebind: ${e.message}", e)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d("BANK_TEST", "Notificación recibida de: ${sbn?.packageName}")
        super.onNotificationPosted(sbn)
        sbn ?: return

        val pkg = sbn.packageName ?: "unknown"
        val extras = sbn.notification?.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        
        Log.d("BANK_TEST", "$pkg | Title: $title | Text: $text | BigText: $bigText")

        
        if (pkg == packageName) return

        
        val fullRawText = extractAllNotificationText(sbn)
        val normalizedText = cleanAndNormalizeText(fullRawText)

        
        if (!isAllowedPackageOrContent(pkg, normalizedText)) {
            Log.d(TAG, "Notificación ignorada: app no bancaria/financiera autorizada ($pkg)")
            return
        }

        
        val amount = extractAmount(fullRawText, normalizedText)
        if (amount == null || amount <= 0.0) {
            Log.d(TAG, "No se detectó importe válido en la notificación de $pkg: '$fullRawText'")
            return
        }

        
        val (isIncome, isExpense) = detectFinancialIntent(normalizedText)

        if (!isIncome && !isExpense) {
            Log.d(TAG, "Importe detectado ($amount €) pero no se identificó intención clara en: '$normalizedText'")
            return
        }

        val finalIsIncome = isIncome && !isExpense

        Log.i(TAG, "✅ Movimiento interceptado con éxito desde $pkg: $amount € (Ingreso: $finalIsIncome) | Texto: '$fullRawText'")
        processTransaction(amount, finalIsIncome, normalizedText)
    }

    /**
     * Extrae y concatena todos los textos accesibles de la notificación:
     * EXTRA_TITLE, EXTRA_TITLE_BIG, EXTRA_TEXT, EXTRA_BIG_TEXT, EXTRA_SUB_TEXT,
     * EXTRA_SUMMARY_TEXT, EXTRA_INFO_TEXT, EXTRA_TEXT_LINES y tickerText.
     */
    private fun extractAllNotificationText(sbn: StatusBarNotification): String {
        val notification = sbn.notification ?: return ""
        val extras = notification.extras ?: return notification.tickerText?.toString() ?: ""

        val sb = StringBuilder()

        fun appendIfNotEmpty(cs: CharSequence?) {
            if (!cs.isNullOrBlank()) {
                val str = cs.toString().trim()
                if (str.isNotEmpty()) {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(str)
                }
            }
        }

        appendIfNotEmpty(extras.getCharSequence(Notification.EXTRA_TITLE))
        appendIfNotEmpty(extras.getCharSequence(Notification.EXTRA_TITLE_BIG))
        appendIfNotEmpty(extras.getCharSequence(Notification.EXTRA_TEXT))
        appendIfNotEmpty(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
        appendIfNotEmpty(extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
        appendIfNotEmpty(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))
        appendIfNotEmpty(extras.getCharSequence(Notification.EXTRA_INFO_TEXT))
        appendIfNotEmpty(notification.tickerText)

        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (textLines != null) {
            for (line in textLines) {
                appendIfNotEmpty(line)
            }
        }

        return sb.toString()
    }

    private fun isAllowedPackageOrContent(pkg: String, normalizedText: String): Boolean {
        if (pkg.isEmpty()) return false

        
        val blockedPackages = listOf(
            "kdeconnect", "whatsapp", "telegram", "android", "systemui",
            "chrome", "google.android.googlequicksearchbox", "instagram",
            "facebook", "twitter", "tiktok", "youtube", "spotify", "netflix",
            "launcher", "settings"
        )
        if (blockedPackages.any { pkg.contains(it, ignoreCase = true) }) {
            return false
        }

        
        if (pkg in ALLOWED_BANK_PACKAGES) return true

        
        val lowercasePkg = pkg.lowercase()
        val bankKeywords = listOf(
            "caixabank", "imagin", "bbva", "santander", "sabadell", "bankinter",
            "unicaja", "kutxabank", "cajasur", "abanca", "cajamar", "revolut",
            "klarna", "paypal", "bizum", "waylet", "n26", "openbank",
            "triodos", "myandbank", "wise", "traderepublic", "trade_republic",
            "scalable", "vivid", "curve", "bunq", "pibank", "evobanco",
            "fintech", "banco", "bank", "caja", "wallet", "pay"
        )
        if (bankKeywords.any { lowercasePkg.contains(it) }) return true

        
        val explicitFinancialPhrases = listOf(
            "BIZUM", "PAGO CON TARJETA", "COMPRA CON TARJETA", "CARGO EN CUENTA",
            "ABONO EN CUENTA", "ABONO DE TRASPASO", "TRASPASO PROPIO", "ABONO",
            "TRANSFERENCIA RECIBIDA", "TRANSFERENCIA EMITIDA",
            "RETIRADA DE EFECTIVO", "REINTEGRO EN CAJERO", "SALDO DISPONIBLE",
            "COMPRA EN", "PAGO EN", "PAGO DE", "IMPORTE DE", "ADEUDO SEPA", "CUOTA"
        )
        return explicitFinancialPhrases.any { normalizedText.contains(it) }
    }

    /**
     * Limpieza exhaustiva:
     * - Remueve espacios invisibles / zero-width spaces (\u200B, \u200C, \u200D, \uFEFF, \u00A0, \u202F, etc.)
     * - Convierte a mayúsculas y remueve tildes/marcas diacríticas
     * - Normaliza múltiples espacios a uno solo
     */
    private fun cleanAndNormalizeText(input: String): String {
        val cleanInvisible = input.replace("[\u200B\u200C\u200D\uFEFF\u00A0\u202F\u2007\u2008\u2009\u200A\u205F\u3000]".toRegex(), " ")
        val normalized = Normalizer.normalize(cleanInvisible.uppercase(), Normalizer.Form.NFD)
        val withoutAccents = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return withoutAccents.replace("\\s+".toRegex(), " ").trim()
    }

    private fun parseRawAmount(rawInput: String): Double? {
        var clean = rawInput.replace("[^0-9,.]".toRegex(), "").trim()
        if (clean.isEmpty()) return null

        if (clean.contains(",") && clean.contains(".")) {
            if (clean.lastIndexOf(",") > clean.lastIndexOf(".")) {
                
                clean = clean.replace(".", "").replace(",", ".")
            } else {
                
                clean = clean.replace(",", "")
            }
        } else if (clean.contains(",")) {
            
            clean = clean.replace(",", ".")
        }

        val parsed = clean.toDoubleOrNull()
        return if (parsed != null && parsed > 0.0) parsed else null
    }

    private fun extractAmount(rawText: String, normalizedText: String): Double? {
        
        val currencyMatcher = CURRENCY_AMOUNT_PATTERN.matcher(rawText)
        while (currencyMatcher.find()) {
            val group = currencyMatcher.group(1) ?: currencyMatcher.group(2)
            if (group != null) {
                val parsed = parseRawAmount(group)
                if (parsed != null) return parsed
            }
        }

        
        val normCurrencyMatcher = CURRENCY_AMOUNT_PATTERN.matcher(normalizedText)
        while (normCurrencyMatcher.find()) {
            val group = normCurrencyMatcher.group(1) ?: normCurrencyMatcher.group(2)
            if (group != null) {
                val parsed = parseRawAmount(group)
                if (parsed != null) return parsed
            }
        }

        
        val keywordMatcher = KEYWORD_AMOUNT_PATTERN.matcher(normalizedText)
        while (keywordMatcher.find()) {
            val group = keywordMatcher.group(1)
            if (group != null) {
                val parsed = parseRawAmount(group)
                if (parsed != null) return parsed
            }
        }

        return null
    }

    private fun detectFinancialIntent(normalizedText: String): Pair<Boolean, Boolean> {
        val isExplicitIncome = listOf(
            "ABONO", "TRASPASO PROPIO", "ABONO DE TRASPASO", "ABONO DE",
            "BIZUM RECIBIDO", "BIZUM DE", "TRANSFERENCIA RECIBIDA", "INGRESO",
            "INGRESO DE", "A SU FAVOR", "NOMINA", "REEMBOLSO", "RECIBIDO"
        ).any { normalizedText.contains(it) }

        if (isExplicitIncome) {
            return Pair(true, false) 
        }

        val isExpense = UNIVERSAL_EXPENSE_KEYWORDS.any { normalizedText.contains(it) }
        if (isExpense) {
            return Pair(false, true)
        }

        val isIncome = UNIVERSAL_INCOME_KEYWORDS.any { normalizedText.contains(it) }
        return Pair(isIncome, false)
    }

    private fun processTransaction(amount: Double, isIncome: Boolean, normalizedText: String) {
        val database = FinanceDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(database.financeDao())

        scope.launch {
            transactionMutex.withLock {
                val profile = repository.getProfileDirect() ?: return@withLock
                if (!profile.isBankNotificationInterceptorEnabled) return@withLock

                val currentBalance = if (profile.currentBankBalance >= 0.0) profile.currentBankBalance else profile.monthlyIncome
                val newBalance = if (isIncome) {
                    currentBalance + amount
                } else {
                    maxOf(0.0, currentBalance - amount)
                }

                repository.saveFinancialProfile(profile.copy(currentBankBalance = newBalance))

                var matchedCategoryName: String? = null

                
                val containsCajero = normalizedText.contains("CAJERO")
                val containsReint = normalizedText.contains("REINT") || normalizedText.contains("REINTEGRO")
                val containsRetiradaEfectivo = normalizedText.contains("RETIRADA DE EFECTIVO") || normalizedText.contains("RETIRADA EN CAJERO")
                val containsRetiradoCajero = normalizedText.contains("RETIRADO") && containsCajero
                val containsCajeroOp = containsCajero && (
                        normalizedText.contains("RETIRADA") ||
                                normalizedText.contains("CARGO") ||
                                normalizedText.contains("DISPOSICION") ||
                                normalizedText.contains("EFECTIVO") ||
                                normalizedText.contains("EXTRAER") ||
                                normalizedText.contains("EXTRACCION")
                        )

                val isCashWithdrawal = !isIncome && (containsReint || containsRetiradaEfectivo || containsRetiradoCajero || containsCajeroOp)

                if (!isIncome) {
                    val categories = repository.getAllCategoriesDirect().filter { !it.isArchived }

                    if (isCashWithdrawal) {
                        val unpaidCashCategories = categories.filter { it.isCashPayment && !it.isPaid }
                        var matchingCashCat = unpaidCashCategories.find { cat ->
                            kotlin.math.abs(cat.limitAmount - amount) < 0.05 ||
                                    (cat.rawAmount > 0.0 && kotlin.math.abs(cat.rawAmount - amount) < 0.05)
                        }
                        if (matchingCashCat == null && unpaidCashCategories.size == 1) {
                            matchingCashCat = unpaidCashCategories.first()
                        }

                        if (matchingCashCat != null) {
                            repository.setCategoryPaid(matchingCashCat.id, true)
                            matchedCategoryName = matchingCashCat.name
                            Log.i(TAG, "Categoría en efectivo emparejada y marcada como pagada: ${matchingCashCat.name}")
                        }
                    } else {
                        
                        val unpaidBankCategories = categories.filter { !it.isCashPayment && !it.isPaid }

                        var matchingBankCat = unpaidBankCategories.find { cat ->
                            val normCatName = cleanAndNormalizeText(cat.name)
                            val normCatId = cleanAndNormalizeText(cat.id)
                            (normCatName.length >= 3 && normalizedText.contains(normCatName)) ||
                                    (normCatId.length >= 3 && normalizedText.contains(normCatId))
                        }

                        if (matchingBankCat == null) {
                            matchingBankCat = unpaidBankCategories.find { cat ->
                                kotlin.math.abs(cat.limitAmount - amount) < 0.05 ||
                                        (cat.rawAmount > 0.0 && kotlin.math.abs(cat.rawAmount - amount) < 0.05)
                            }
                        }

                        if (matchingBankCat != null) {
                            repository.setCategoryPaid(matchingBankCat.id, true)
                            matchedCategoryName = matchingBankCat.name
                            Log.i(TAG, "Categoría bancaria emparejada y marcada como pagada: ${matchingBankCat.name}")
                        }
                    }
                }

                
                try {
                    WidgetUpdateHelper.updateAllWidgets(applicationContext)
                } catch (e: Exception) {
                    Log.e(TAG, "Error actualizando widgets tras notificación bancaria: ${e.message}")
                }

                
                showConfirmationNotification(amount, newBalance, isIncome, matchedCategoryName, isCashWithdrawal)
            }
        }
    }

    private fun showConfirmationNotification(
        amount: Double,
        newBalance: Double,
        isIncome: Boolean,
        matchedCategoryName: String? = null,
        isCashWithdrawal: Boolean = false
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = if (isIncome) "🟢 Ingreso detectado" else "🔴 Gasto detectado"
        val formattedAmount = String.format("%.2f €", amount)
        val formattedBalance = String.format("%.2f €", newBalance)

        val message = when {
            isIncome -> "Se han añadido $formattedAmount. Nuevo saldo: $formattedBalance"
            matchedCategoryName != null && isCashWithdrawal -> "Retirada en cajero ($formattedAmount): $matchedCategoryName marcado como pagado. Nuevo saldo: $formattedBalance"
            matchedCategoryName != null -> "Gasto de $formattedAmount: $matchedCategoryName marcado como pagado. Nuevo saldo: $formattedBalance"
            else -> "Se han restado $formattedAmount. Nuevo saldo: $formattedBalance"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val dynamicNotificationId = (System.currentTimeMillis() % 100000).toInt() + 8000
        notificationManager.notify(dynamicNotificationId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sincronización Bancaria"
            val descriptionText = "Notificaciones de actualización automática de saldo"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SERVICE_LIFECYCLE", "onDestroy ejecutado")
        job.cancel()
    }
}
