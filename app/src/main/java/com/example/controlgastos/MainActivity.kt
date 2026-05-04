package com.example.controlgastos

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.controlgastos.ui.theme.ControlGastosPersonalesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Expense(
    val name: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: String = "",
    val month: String = ""
)

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ControlGastosPersonalesTheme {
                var userEmail by remember {
                    mutableStateOf(auth.currentUser?.email)
                }

                var currentScreen by remember {
                    mutableStateOf("home")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    if (userEmail == null) {
                        AuthScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLoginSuccess = { email ->
                                userEmail = email
                                currentScreen = "home"
                            },
                            auth = auth
                        )
                    } else {
                        when (currentScreen) {
                            "addExpense" -> {
                                AddExpenseScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    auth = auth,
                                    db = db,
                                    onBack = {
                                        currentScreen = "home"
                                    }
                                )
                            }

                            "history" -> {
                                HistoryScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    auth = auth,
                                    db = db,
                                    onBack = {
                                        currentScreen = "home"
                                    }
                                )
                            }

                            "summary" -> {
                                MonthlySummaryScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    auth = auth,
                                    db = db,
                                    onBack = {
                                        currentScreen = "home"
                                    }
                                )
                            }

                            else -> {
                                HomeScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    email = userEmail ?: "",
                                    onAddExpense = {
                                        currentScreen = "addExpense"
                                    },
                                    onViewHistory = {
                                        currentScreen = "history"
                                    },
                                    onViewSummary = {
                                        currentScreen = "summary"
                                    },
                                    onLogout = {
                                        auth.signOut()
                                        userEmail = null
                                        currentScreen = "home"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    auth: FirebaseAuth,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun validateFields(): Boolean {
        if (email.isBlank()) {
            message = "El correo electrónico no puede estar vacío."
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            message = "Ingrese un correo electrónico válido."
            return false
        }

        if (password.isBlank()) {
            message = "La contraseña no puede estar vacía."
            return false
        }

        if (password.length < 6) {
            message = "La contraseña debe tener al menos 6 caracteres."
            return false
        }

        return true
    }

    fun loginUser() {
        if (!validateFields()) return

        isLoading = true
        message = ""

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false

                if (task.isSuccessful) {
                    onLoginSuccess(auth.currentUser?.email ?: email.trim())
                } else {
                    message = task.exception?.message ?: "No se pudo iniciar sesión."
                }
            }
    }

    fun registerUser() {
        if (!validateFields()) return

        isLoading = true
        message = ""

        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading = false

                if (task.isSuccessful) {
                    onLoginSuccess(auth.currentUser?.email ?: email.trim())
                } else {
                    message = task.exception?.message ?: "No se pudo registrar el usuario."
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Control de Gastos",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Inicia sesión o crea una cuenta",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { loginUser() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Iniciar sesión")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { registerUser() },
                    enabled = !isLoading
                ) {
                    Text("Crear cuenta")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                }

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    email: String,
    onAddExpense: () -> Unit,
    onViewHistory: () -> Unit,
    onViewSummary: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Desde esta pantalla puedes registrar, consultar y resumir tus gastos personales."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddExpense,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar gasto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver historial")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onViewSummary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver resumen mensual")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun AddExpenseScreen(
    modifier: Modifier = Modifier,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun validateExpense(): Boolean {
        val amountValue = amount.toDoubleOrNull()

        if (name.isBlank()) {
            message = "El nombre del gasto no puede estar vacío."
            return false
        }

        if (amount.isBlank()) {
            message = "El monto no puede estar vacío."
            return false
        }

        if (amountValue == null) {
            message = "El monto debe ser numérico."
            return false
        }

        if (amountValue <= 0.0) {
            message = "El monto debe ser mayor que cero."
            return false
        }

        if (category.isBlank()) {
            message = "La categoría no puede estar vacía."
            return false
        }

        if (date.isBlank()) {
            message = "La fecha no puede estar vacía."
            return false
        }

        return true
    }

    fun getMonthFromDate(dateText: String): String {
        return if (dateText.length >= 7) {
            dateText.substring(0, 7)
        } else {
            "sin_mes"
        }
    }

    fun saveExpense() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            message = "No hay un usuario autenticado."
            return
        }

        if (!validateExpense()) return

        isLoading = true
        message = ""

        val amountValue = amount.toDouble()

        val expense = hashMapOf(
            "name" to name.trim(),
            "amount" to amountValue,
            "category" to category.trim(),
            "date" to date.trim(),
            "month" to getMonthFromDate(date.trim()),
            "createdAt" to FieldValue.serverTimestamp(),
            "userId" to currentUser.uid
        )

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                isLoading = false
                name = ""
                amount = ""
                category = ""
                date = ""
                message = "Gasto guardado correctamente."
            }
            .addOnFailureListener { exception ->
                isLoading = false
                message = exception.message ?: "No se pudo guardar el gasto."
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registrar gasto",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del gasto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha, ejemplo: 2026-05-04") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { saveExpense() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Guardar gasto")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onBack,
                    enabled = !isLoading
                ) {
                    Text("Volver")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                }

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = if (message.contains("correctamente")) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onBack: () -> Unit
) {
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            message = "No hay un usuario autenticado."
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                expenses = result.documents.map { document ->
                    Expense(
                        name = document.getString("name") ?: "",
                        amount = document.getDouble("amount") ?: 0.0,
                        category = document.getString("category") ?: "",
                        date = document.getString("date") ?: "",
                        month = document.getString("month") ?: ""
                    )
                }

                isLoading = false

                if (expenses.isEmpty()) {
                    message = "No hay gastos registrados."
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                message = exception.message ?: "No se pudo cargar el historial."
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Historial de gastos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gastos registrados por el usuario autenticado.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(expenses) { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = expense.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = "Monto: \$${expense.amount}")
                        Text(text = "Categoría: ${expense.category}")
                        Text(text = "Fecha: ${expense.date}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun MonthlySummaryScreen(
    modifier: Modifier = Modifier,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onBack: () -> Unit
) {
    var total by remember { mutableStateOf(0.0) }
    var month by remember { mutableStateOf("") }
    var expenseCount by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser = auth.currentUser

    fun getCurrentMonth(): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return formatter.format(Date())
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            message = "No hay un usuario autenticado."
            isLoading = false
            return@LaunchedEffect
        }

        val currentMonth = getCurrentMonth()
        month = currentMonth

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .whereEqualTo("month", currentMonth)
            .get()
            .addOnSuccessListener { result ->
                val amounts = result.documents.mapNotNull { document ->
                    document.getDouble("amount")
                }

                total = amounts.sum()
                expenseCount = amounts.size
                isLoading = false

                if (expenseCount == 0) {
                    message = "No hay gastos registrados para este mes."
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                message = exception.message ?: "No se pudo calcular el resumen mensual."
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Resumen mensual",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Mes: $month",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "Total gastado: \$${String.format(Locale.getDefault(), "%.2f", total)}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Cantidad de gastos: $expenseCount",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }
        }
    }
}