package `in`.ganeshpuja.manager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.content.Context

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("GaneshPujaData", Context.MODE_PRIVATE)
    }

    private var totalDonation = 0.0
    private var totalExpense = 0.0
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        totalDonation = prefs.getString("donation", "0")!!.toDoubleOrNull() ?: 0.0
        totalExpense = prefs.getString("expense", "0")!!.toDoubleOrNull() ?: 0.0
        showHome()
    }

    private fun text(value: String, size: Float = 18f): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.BLACK)
            setPadding(12, 14, 12, 14)
        }
    }

    private fun button(value: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = value
            setOnClickListener { action() }
        }
    }

    private fun startPage(title: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 16)
            setBackgroundColor(Color.rgb(255, 248, 235))
        }

        root.addView(text("🙏 ଗଣେଶ ପୂଜା ମ୍ୟାନେଜର", 25f))
        root.addView(text(title, 22f))

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        root.addView(button("🏠 ମୁଖ୍ୟ ମେନୁ") { showHome() })
        setContentView(root)
    }

    private fun showHome() {
        startPage("ମୁଖ୍ୟ ମେନୁ")
        content.addView(text("💰 ମୋଟ ଚାନ୍ଦା: ₹${"%.2f".format(totalDonation)}"))
        content.addView(text("💸 ମୋଟ ଖର୍ଚ୍ଚ: ₹${"%.2f".format(totalExpense)}"))
        content.addView(text("✅ ବାକି ଟଙ୍କା: ₹${"%.2f".format(totalDonation - totalExpense)}"))

        content.addView(button("➕ ଚାନ୍ଦା ଜମା") { donationPage() })
        content.addView(button("➖ ଖର୍ଚ୍ଚ ଲେଖନ୍ତୁ") { expensePage() })
        content.addView(button("📊 ରିପୋର୍ଟ") { reportPage() })
        content.addView(button("👥 ସଦସ୍ୟ") { membersPage() })
        content.addView(button("💾 ବ୍ୟାକଅପ୍") { backupPage() })
        content.addView(button("🔐 Admin") { adminPage() })
    }

    private fun donationPage() {
        startPage("ଚାନ୍ଦା ଜମା")
        val name = EditText(this).apply { hint = "ଦାତାଙ୍କ ନାମ" }
        val amount = EditText(this).apply {
            hint = "ଟଙ୍କା"
            inputType = 8194
        }
        content.addView(name)
        content.addView(amount)
        content.addView(button("ଚାନ୍ଦା ଜମା କରନ୍ତୁ") {
            val n = name.text.toString().trim()
            val a = amount.text.toString().toDoubleOrNull()
            if (n.isEmpty() || a == null || a <= 0) {
                toast("ନାମ ଓ ସଠିକ୍ ଟଙ୍କା ଲେଖନ୍ତୁ")
            } else {
                totalDonation += a
                prefs.edit().putString("donation", totalDonation.toString()).apply()
                prefs.edit().putString(
                    "donation_${System.currentTimeMillis()}",
                    "$n — ₹$a"
                ).apply()
                toast("ଚାନ୍ଦା ଜମା ହେଲା")
                showHome()
            }
        })
    }

    private fun expensePage() {
        startPage("ଖର୍ଚ୍ଚ ଲେଖନ୍ତୁ")
        val detail = EditText(this).apply { hint = "ଖର୍ଚ୍ଚର ବିବରଣୀ" }
        val amount = EditText(this).apply {
            hint = "ଟଙ୍କା"
            inputType = 8194
        }
        content.addView(detail)
        content.addView(amount)
        content.addView(button("ଖର୍ଚ୍ଚ ସଂରକ୍ଷଣ") {
            val d = detail.text.toString().trim()
            val a = amount.text.toString().toDoubleOrNull()
            if (d.isEmpty() || a == null || a <= 0) {
                toast("ବିବରଣୀ ଓ ସଠିକ୍ ଟଙ୍କା ଲେଖନ୍ତୁ")
            } else if (a > totalDonation - totalExpense) {
                toast("ବାକି ଟଙ୍କାଠାରୁ ଅଧିକ ଖର୍ଚ୍ଚ ହୋଇପାରିବ ନାହିଁ")
            } else {
                totalExpense += a
                prefs.edit().putString("expense", totalExpense.toString()).apply()
                prefs.edit().putString(
                    "expense_${System.currentTimeMillis()}",
                    "$d — ₹$a"
                ).apply()
                toast("ଖର୍ଚ୍ଚ ସଂରକ୍ଷଣ ହେଲା")
                showHome()
            }
        })
    }

    private fun reportPage() {
        startPage("ରିପୋର୍ଟ")
        content.addView(text("ମୋଟ ଚାନ୍ଦା: ₹$totalDonation"))
        content.addView(text("ମୋଟ ଖର୍ଚ୍ଚ: ₹$totalExpense"))
        content.addView(text("ବାକି ଟଙ୍କା: ₹${totalDonation - totalExpense}"))
        content.addView(button("ଚାନ୍ଦା ତାଲିକା ଦେଖନ୍ତୁ") {
            listEntries("donation_", "ଚାନ୍ଦା ତାଲିକା")
        })
        content.addView(button("ଖର୍ଚ୍ଚ ତାଲିକା ଦେଖନ୍ତୁ") {
            listEntries("expense_", "ଖର୍ଚ୍ଚ ତାଲିକା")
        })
    }

    private fun listEntries(prefix: String, title: String) {
        startPage(title)
        val all = prefs.all.entries
            .filter { it.key.startsWith(prefix) }
            .sortedBy { it.key }
        if (all.isEmpty()) content.addView(text("କୌଣସି ରେକର୍ଡ ନାହିଁ"))
        all.forEach { content.addView(text(it.value.toString())) }
    }

    private fun membersPage() {
        startPage("ସଦସ୍ୟ ପରିଚାଳନା")
        val name = EditText(this).apply { hint = "ସଦସ୍ୟଙ୍କ ନାମ" }
        content.addView(name)
        content.addView(button("ସଦସ୍ୟ ଯୋଡ଼ନ୍ତୁ") {
            val n = name.text.toString().trim()
            if (n.isEmpty()) {
                toast("ସଦସ୍ୟଙ୍କ ନାମ ଲେଖନ୍ତୁ")
            } else {
                val id = System.currentTimeMillis()
                prefs.edit().putString("member_$id", n).apply()
                toast("ସଦସ୍ୟ ଯୋଡ଼ାଗଲେ")
                membersPage()
            }
        })
        prefs.all.entries.filter { it.key.startsWith("member_") }.forEach { entry ->
            content.addView(text(entry.value.toString()))
            content.addView(button("ହଟାନ୍ତୁ") {
                prefs.edit().remove(entry.key).apply()
                membersPage()
            })
        }
    }

    private fun backupPage() {
        startPage("ବ୍ୟାକଅପ୍")
        content.addView(text("ତଥ୍ୟ ଏହି ଫୋନ୍‌ରେ ସଂରକ୍ଷିତ ହେଉଛି।"))
        content.addView(button("ବ୍ୟାକଅପ୍ ଟେକ୍ସଟ୍ ଦେଖନ୍ତୁ") {
            val data = prefs.all.entries.joinToString("\n") {
                "${it.key} = ${it.value}"
            }
            startPage("ବ୍ୟାକଅପ୍ ତଥ୍ୟ")
            content.addView(text(data.ifEmpty { "କୌଣସି ତଥ୍ୟ ନାହିଁ" }))
        })
    }

    
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
