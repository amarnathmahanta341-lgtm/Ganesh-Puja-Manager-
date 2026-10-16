
package com.example.ganeshpuja

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

data class Member(
    val name: String,
    val phone: String
)

data class MoneyRecord(
    val name: String,
    val amount: Double,
    val note: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GaneshPujaApp()
            }
        }
    }
}

private fun money(value: Double): String =
    "₹" + String.format("%.2f", value)

private fun JSONObject.string(key: String): String =
    optString(key, "")

private fun JSONObject.number(key: String): Double =
    optDouble(key, 0.0)

@Composable
fun GaneshPujaApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("ganesh_puja_data", Context.MODE_PRIVATE)
    }

    val members = remember { mutableStateListOf<Member>() }
    val donations = remember { mutableStateListOf<MoneyRecord>() }
    val expenses = remember { mutableStateListOf<MoneyRecord>() }

    var page by remember { mutableIntStateOf(0) }
    var dialogType by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    fun saveData() {
        val memberArray = JSONArray()
        members.forEach {
            memberArray.put(JSONObject().apply {
                put("name", it.name)
                put("phone", it.phone)
            })
        }

        fun recordArray(records: List<MoneyRecord>): JSONArray {
            val array = JSONArray()
            records.forEach {
                array.put(JSONObject().apply {
                    put("name", it.name)
                    put("amount", it.amount)
                    put("note", it.note)
                })
            }
            return array
        }

        prefs.edit()
            .putString("members", memberArray.toString())
            .putString("donations", recordArray(donations).toString())
            .putString("expenses", recordArray(expenses).toString())
            .apply()
    }

    fun exportData(): String {
        val root = JSONObject()
        val memberArray = JSONArray()
        members.forEach {
            memberArray.put(JSONObject().apply {
                put("name", it.name)
                put("phone", it.phone)
            })
        }

        fun makeArray(records: List<MoneyRecord>): JSONArray {
            val array = JSONArray()
            records.forEach {
                array.put(JSONObject().apply {
                    put("name", it.name)
                    put("amount", it.amount)
                    put("note", it.note)
                })
            }
            return array
        }

        root.put("members", memberArray)
        root.put("donations", makeArray(donations))
        root.put("expenses", makeArray(expenses))
        root.put("version", 1)
        return root.toString(2)
    }

    fun importData(json: String) {
        val root = JSONObject(json)

        fun readRecords(key: String): List<MoneyRecord> {
            val array = root.optJSONArray(key) ?: JSONArray()
            return (0 until array.length()).mapNotNull { index ->
                val obj = array.optJSONObject(index) ?: return@mapNotNull null
                val value = obj.number("amount")
                if (value < 0) return@mapNotNull null
                MoneyRecord(
                    obj.string("name"),
                    value,
                    obj.string("note")
                )
            }
        }

        val memberArray = root.optJSONArray("members") ?: JSONArray()
        val newMembers = (0 until memberArray.length()).mapNotNull { index ->
            val obj = memberArray.optJSONObject(index)
                ?: return@mapNotNull null
            Member(obj.string("name"), obj.string("phone"))
        }

        // Parse all data before replacing existing records.
        val newDonations = readRecords("donations")
        val newExpenses = readRecords("expenses")

        members.clear()
        members.addAll(newMembers)
        donations.clear()
        donations.addAll(newDonations)
        expenses.clear()
        expenses.addAll(newExpenses)
        saveData()
    }

    LaunchedEffect(Unit) {
        try {
            val memberArray = JSONArray(
                prefs.getString("members", "[]")
            )
            for (i in 0 until memberArray.length()) {
                val obj = memberArray.getJSONObject(i)
                members.add(Member(obj.string("name"), obj.string("phone")))
            }

            fun readSaved(key: String): List<MoneyRecord> {
                val array = JSONArray(prefs.getString(key, "[]"))
                return (0 until array.length()).map {
                    val obj = array.getJSONObject(it)
                    MoneyRecord(
                        obj.string("name"),
                        obj.number("amount"),
                        obj.string("note")
                    )
                }
            }

            donations.addAll(readSaved("donations"))
            expenses.addAll(readSaved("expenses"))
        } catch (_: Exception) {
            message = "ଡାଟା ପଢ଼ିବାରେ ତ୍ରୁଟି ହୋଇଛି।"
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()
                    ?.use { it.write(exportData()) }
                message = "ବ୍ୟାକଅପ୍ ସେଭ୍ ହୋଇଛି।"
            } catch (_: Exception) {
                message = "ବ୍ୟାକଅପ୍ ସେଭ୍ ହୋଇପାରିଲା ନାହିଁ।"
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                    ?: error("ଫାଇଲ୍ ପଢ଼ାଯାଇନାହିଁ")
                importData(json)
                message = "ବ୍ୟାକଅପ୍ ପୁନଃସ୍ଥାପନ ହୋଇଛି।"
            } catch (_: Exception) {
                message = "ଫାଇଲ୍ ଭୁଲ୍ ଅଛି କିମ୍ବା ପଢ଼ାଯାଇନାହିଁ।"
            }
        }
    }

    val totalDonation = donations.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val balance = totalDonation - totalExpense

    fun openForm(type: String) {
        name = ""
        phone = ""
        amount = ""
        note = ""
        dialogType = type
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🙏 ଗଣେଶ ପୂଜା ମ୍ୟାନେଜର୍") }
            )
        },
        bottomBar = {
            NavigationBar {
                listOf("ହୋମ୍", "ଚାନ୍ଦା", "ଖର୍ଚ୍ଚ", "ସଦସ୍ୟ", "ରିପୋର୍ଟ")
                    .forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = page == index,
                            onClick = { page = index },
                            icon = {
                                Text(
                                    listOf("🏠", "💰", "💸", "👥", "📊")[index]
                                )
                            },
                            label = { Text(label) }
                        )
                    }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (page) {
                0 -> {
                    Text(
                        "ଜୟ ଶ୍ରୀ ଗଣେଶ 🙏",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    SummaryCard("ମୋଟ ଚାନ୍ଦା", totalDonation)
                    SummaryCard("ମୋଟ ଖର୍ଚ୍ଚ", totalExpense)
                    SummaryCard("ବାକି ଟଙ୍କା", balance)

                    Text("ସମୁଦାୟ ସଦସ୍ୟ: ${members.size}")
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { openForm("donation") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("＋ ନୂଆ ଚାନ୍ଦା ଯୋଡ଼ନ୍ତୁ")
                    }

                    Button(
                        onClick = { openForm("expense") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("＋ ନୂଆ ଖର୍ଚ୍ଚ ଯୋଡ଼ନ୍ତୁ")
                    }
                }

                1 -> {
                    SectionTitle("ଚାନ୍ଦା ଆଦାୟ")
                    Button(
                        onClick = { openForm("donation") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("＋ ଚାନ୍ଦା ଯୋଡ଼ନ୍ତୁ") }

                    LazyColumn {
                        items(donations.toList().reversed()) { item ->
                            RecordCard(item, "ଦାତା") {
                                donations.remove(item)
                                saveData()
                            }
                        }
                    }
                }

                2 -> {
                    SectionTitle("ଖର୍ଚ୍ଚ ହିସାବ")
                    Button(
                        onClick = { openForm("expense") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("＋ ଖର୍ଚ୍ଚ ଯୋଡ଼ନ୍ତୁ") }

                    LazyColumn {
                        items(expenses.toList().reversed()) { item ->
                            RecordCard(item, "ଖର୍ଚ୍ଚ") {
                                expenses.remove(item)
                                saveData()
                            }
                        }
                    }
                }

                3 -> {
                    SectionTitle("ସଦସ୍ୟ ତାଲିକା")
                    Button(
                        onClick = { openForm("member") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("＋ ସଦସ୍ୟ ଯୋଡ଼ନ୍ତୁ") }

                    LazyColumn {
                        items(members.toList()) { member ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(member.name,
                                            fontWeight = FontWeight.Bold)
                                        Text(member.phone)
                                    }
                                    TextButton(onClick = {
                                        members.remove(member)
                                        saveData()
                                    }) { Text("ହଟାନ୍ତୁ") }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    SectionTitle("ପୂଜାର ଆର୍ଥିକ ରିପୋର୍ଟ")
                    SummaryCard("ମୋଟ ଆୟ", totalDonation)
                    SummaryCard("ମୋଟ ବ୍ୟୟ", totalExpense)
                    SummaryCard("ବର୍ତ୍ତମାନ ବାଲାନ୍ସ", balance)
                    Text("ଦାନ ରେକର୍ଡ: ${donations.size}")
                    Text("ଖର୍ଚ୍ଚ ରେକର୍ଡ: ${expenses.size}")
                    Text("ସଦସ୍ୟ ସଂଖ୍ୟା: ${members.size}")

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            backupLauncher.launch("ganesh_puja_backup.json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("💾 ବ୍ୟାକଅପ୍ ସେଭ୍ କରନ୍ତୁ") }

                    Button(
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/json", "text/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("📂 ବ୍ୟାକଅପ୍ ଫେରାନ୍ତୁ") }
                }
            }
        }
    }

    if (dialogType.isNotEmpty()) {
        val title = when (dialogType) {
            "member" -> "ନୂଆ ସଦସ୍ୟ"
            "donation" -> "ନୂଆ ଚାନ୍ଦା"
            else -> "ନୂଆ ଖର୍ଚ୍ଚ"
        }

        AlertDialog(
            onDismissRequest = { dialogType = "" },
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text(
                                if (dialogType == "expense")
                                    "ଖର୍ଚ୍ଚର ନାମ"
                                else if (dialogType == "member")
                                    "ସଦସ୍ୟଙ୍କ ନାମ"
                                else "ଦାତାଙ୍କ ନାମ"
                            )
                        },
                        singleLine = true
                    )

                    if (dialogType == "member") {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("ମୋବାଇଲ୍ ନମ୍ବର") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("ଟଙ୍କା") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("ବିବରଣୀ (ଇଚ୍ଛାଧୀନ)") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when (dialogType) {
                        "member" -> {
                            if (name.isNotBlank()) {
                                members.add(Member(name.trim(), phone.trim()))
                                saveData()
                                dialogType = ""
                            } else message = "ସଦସ୍ୟଙ୍କ ନାମ ଲେଖନ୍ତୁ।"
                        }

                        else -> {
                            val value = amount.toDoubleOrNull()
                            if (name.isBlank() || value == null || value <= 0) {
                                message = "ଠିକ୍ ନାମ ଓ ଟଙ୍କା ଲେଖନ୍ତୁ।"
                            } else {
                                val record = MoneyRecord(
                                    name.trim(), value, note.trim()
                                )
                                if (dialogType == "donation") {
                                    donations.add(record)
                                } else {
                                    expenses.add(record)
                                }
                                saveData()
                                dialogType = ""
                            }
                        }
                    }
                }) { Text("ସେଭ୍ କରନ୍ତୁ") }
            },
            dismissButton = {
                TextButton(onClick = { dialogType = "" }) {
                    Text("ବାତିଲ୍")
                }
            }
        )
    }

    if (message.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { message = "" },
            title = { Text("ସୂଚନା") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { message = "" }) {
                    Text("ଠିକ୍ ଅଛି")
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun SummaryCard(title: String, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                money(amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecordCard(
    item: MoneyRecord,
    label: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                if (item.note.isNotBlank()) Text(item.note)
                Text(money(item.amount))
            }
            TextButton(onClick = onDelete) {
                Text("ହଟାନ୍ତୁ")
            }
        }
    }
}

