
package `in`.ganeshpuja.manager

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    private val green = Color.rgb(27, 110, 65)
    private val records = listOf("donation_", "expense_", "member_")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("GaneshPujaData", Context.MODE_PRIVATE)
        showHome()
    }

    private fun makeLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 20)
            setBackgroundColor(Color.rgb(248, 249, 246))
        }
    }

    private fun showHome() {
        root = makeLayout()

        root.addView(
            text("🙏 ଗଣେଶ ପୂଜା ମ୍ୟାନେଜର", 25f, green)
        )

        root.addView(
            text("ଦାନ, ଖର୍ଚ୍ଚ ଓ ସଦସ୍ୟଙ୍କ ହିସାବ", 16f)
        )

        val donation = total("donation_")
        val expense = total("expense_")
        val balance = donation - expense

        root.addView(text("ମୋଟ ଦାନ: ₹$donation", 19f, green))
        root.addView(text("ମୋଟ ଖର୍ଚ୍ଚ: ₹$expense", 19f))
        root.addView(text("ବଳକା ଟଙ୍କା: ₹$balance", 19f, green))

        root.addView(button("💰 ଦାନ ଯୋଡ଼ନ୍ତୁ") {
            donationPage()
        })

        root.addView(button("💸 ଖର୍ଚ୍ଚ ଯୋଡ଼ନ୍ତୁ") {
            expensePage()
        })

        root.addView(button("📊 ରିପୋର୍ଟ") {
            reportPage()
        })

        root.addView(button("👥 ସଦସ୍ୟ ପରିଚାଳନା") {
            membersPage()
        })

        root.addView(button("💾 ବ୍ୟାକଅପ୍") {
            backupPage()
        })

        setContentView(root)
    }

    private fun text(
        value: String,
        size: Float = 17f,
        color: Int = Color.DKGRAY
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(4, 10, 4, 10)
        }
    }

    private fun button(
        label: String,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        }
    }

    private fun startPage(title: String) {
        root = makeLayout()

        root.addView(text(title, 24f, green))

        root.addView(button("⬅ ମୁଖ୍ୟ ମେନୁ") {
            showHome()
        })

        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun donationPage() {
        entryPage("💰 ଦାନ ରେକର୍ଡ", "ଦାନ ଦେଇଥିବା ବ୍ୟକ୍ତିଙ୍କ ନାମ") {
            name, amount ->
            saveRecord("donation_", name, amount)
            donationPage()
        }
    }

    private fun expensePage() {
        entryPage("💸 ଖର୍ଚ୍ଚ ରେକର୍ଡ", "ଖର୍ଚ୍ଚର ବିବରଣୀ") {
            name, amount ->
            saveRecord("expense_", name, amount)
            expensePage()
        }
    }

    private fun entryPage(
        title: String,
        nameHint: String,
        onSave: (String, Long) -> Unit
    ) {
        startPage(title)

        val nameInput = EditText(this).apply {
            hint = nameHint
            setSingleLine(true)
        }

        val amountInput = EditText(this).apply {
            hint = "ଟଙ୍କା ଲେଖନ୍ତୁ"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        content.addView(nameInput)
        content.addView(amountInput)

        content.addView(button("✅ ସେଭ୍ କରନ୍ତୁ") {
            val name = nameInput.text.toString().trim()
            val amount = amountInput.text.toString().toLongOrNull()

            if (name.isBlank()) {
                toast("ନାମ କିମ୍ବା ବିବରଣୀ ଲେଖନ୍ତୁ")
            } else if (amount == null || amount <= 0) {
                toast("ଠିକ୍ ଟଙ୍କା ଲେଖନ୍ତୁ")
            } else {
                onSave(name, amount)
                toast("ସଫଳତାର ସହ ସେଭ୍ ହେଲା")
            }
        })
    }

    private fun saveRecord(
        prefix: String,
        name: String,
        amount: Long
    ) {
        val id = System.currentTimeMillis().toString()
        val date = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())

        prefs.edit().putString(
            prefix + id,
            "$name|$amount|$date"
        ).apply()
    }

    private fun total(prefix: String): Long {
        return prefs.all.entries
            .filter { it.key.startsWith(prefix) }
            .sumOf { entry ->
                val value = entry.value as? String ?: ""
                value.split("|").getOrNull(1)?.toLongOrNull() ?: 0L
            }
    }

    private fun reportPage() {
        startPage("📊 ଆୟ ଓ ବ୍ୟୟ ରିପୋର୍ଟ")

        val donation = total("donation_")
        val expense = total("expense_")

        content.addView(text("ମୋଟ ଦାନ: ₹$donation", 21f, green))
        content.addView(text("ମୋଟ ଖର୍ଚ୍ଚ: ₹$expense", 21f))
        content.addView(
            text("ବଳକା ଟଙ୍କା: ₹${donation - expense}", 21f, green)
        )

        content.addView(text("ଦାନ ତାଲିକା", 20f, green))
        showRecords("donation_")

        content.addView(text("ଖର୍ଚ୍ଚ ତାଲିକା", 20f, green))
        showRecords("expense_")
    }

    private fun showRecords(prefix: String) {
        val entries = prefs.all.entries
            .filter { it.key.startsWith(prefix) }
            .sortedByDescending { it.key }

        if (entries.isEmpty()) {
            content.addView(text("କୌଣସି ରେକର୍ଡ ନାହିଁ"))
            return
        }

        entries.forEach { entry ->
            val parts = (entry.value as? String ?: "").split("|")
            val name = parts.getOrNull(0) ?: ""
            val amount = parts.getOrNull(1) ?: "0"
            val date = parts.getOrNull(2) ?: ""

            content.addView(text("$name — ₹$amount\n$date"))

            content.addView(button("🗑 ଏହି ରେକର୍ଡ ହଟାନ୍ତୁ") {
                AlertDialog.Builder(this)
                    .setTitle("ରେକର୍ଡ ହଟାଇବେ?")
                    .setMessage("$name — ₹$amount")
                    .setNegativeButton("ନା", null)
                    .setPositiveButton("ହଁ") { _, _ ->
                        prefs.edit().remove(entry.key).apply()
                        toast("ରେକର୍ଡ ହଟାଗଲା")
                        reportPage()
                    }
                    .show()
            })
        }
    }

    private fun membersPage() {
        startPage("👥 ସଦସ୍ୟ ପରିଚାଳନା")

        val nameInput = EditText(this).apply {
            hint = "ସଦସ୍ୟଙ୍କ ନାମ"
            setSingleLine(true)
        }

        content.addView(nameInput)

        content.addView(button("➕ ସଦସ୍ୟ ଯୋଡ଼ନ୍ତୁ") {
            val name = nameInput.text.toString().trim()

            if (name.isBlank()) {
                toast("ସଦସ୍ୟଙ୍କ ନାମ ଲେଖନ୍ତୁ")
            } else {
                val id = System.currentTimeMillis().toString()
                prefs.edit().putString("member_$id", name).apply()
                toast("ସଦସ୍ୟ ଯୋଡ଼ାଗଲା")
                membersPage()
            }
        })

        content.addView(text("ସଦସ୍ୟ ତାଲିକା", 20f, green))

        val members = prefs.all.entries
            .filter { it.key.startsWith("member_") }
            .sortedBy { it.key }

        if (members.isEmpty()) {
            content.addView(text("କୌଣସି ସଦସ୍ୟ ନାହାନ୍ତି"))
        }

        members.forEach { entry ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            row.addView(
                text(entry.value as? String ?: ""),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )

            row.addView(button("ହଟାନ୍ତୁ") {
                AlertDialog.Builder(this)
                    .setTitle("ସଦସ୍ୟଙ୍କୁ ହଟାଇବେ?")
                    .setMessage(entry.value as? String ?: "")
                    .setNegativeButton("ନା", null)
                    .setPositiveButton("ହଁ") { _, _ ->
                        prefs.edit().remove(entry.key).apply()
                        membersPage()
                    }
                    .show()
            })

            content.addView(row)
        }
    }

    private fun backupPage() {
        startPage("💾 ବ୍ୟାକଅପ୍")

        content.addView(
            text(
                "ତଳେ ଆପଣଙ୍କ ଆପ୍‌ର ରେକର୍ଡ ଦେଖାଯିବ। " +
                    "ବ୍ୟାକଅପ୍ କପି କରି ସୁରକ୍ଷିତ ସ୍ଥାନରେ ରଖନ୍ତୁ।"
            )
        )

        val data = StringBuilder()
        data.append("ଗଣେଶ ପୂଜା ମ୍ୟାନେଜର ବ୍ୟାକଅପ୍\n\n")
        data.append("ମୋଟ ଦାନ: ₹${total("donation_")}\n")
        data.append("ମୋଟ ଖର୍ଚ୍ଚ: ₹${total("expense_")}\n")
        data.append("ବଳକା: ₹${total("donation_") - total("expense_")}\n\n")

        records.forEach { prefix ->
            val title = when (prefix) {
                "donation_" -> "ଦାନ"
                "expense_" -> "ଖର୍ଚ୍ଚ"
                else -> "ସଦସ୍ୟ"
            }

            data.append("=== $title ===\n")

            prefs.all.entries
                .filter { it.key.startsWith(prefix) }
                .sortedBy { it.key }
                .forEach { data.append("${it.value}\n") }

            data.append("\n")
        }

        content.addView(text(data.toString()))

        content.addView(button("📋 ବ୍ୟାକଅପ୍ କପି କରନ୍ତୁ") {
            val clipboard = getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager

            clipboard.setPrimaryClip(
                ClipData.newPlainText("Ganesh Puja Backup", data.toString())
            )

            toast("ବ୍ୟାକଅପ୍ କପି ହେଲା")
        })
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
