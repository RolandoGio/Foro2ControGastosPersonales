package com.example.controlgastos

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.controlgastos.ui.theme.AeroAqua
import com.example.controlgastos.ui.theme.AeroGlow
import com.example.controlgastos.ui.theme.AeroMint
import com.example.controlgastos.ui.theme.AeroSky
import com.example.controlgastos.ui.theme.AeroWater
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
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
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
fun AeroBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEAFBFF),
                        Color(0xFFCFF7F2),
                        Color(0xFFF8FDFF)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-64).dp, y = 44.dp)
                .clip(CircleShape)
                .background(AeroSky.copy(alpha = 0.28f))
                .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .offset(x = 56.dp, y = 96.dp)
                .clip(CircleShape)
                .background(AeroGlow.copy(alpha = 0.36f))
                .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(260.dp)
                .offset(x = 96.dp, y = 70.dp)
                .clip(CircleShape)
                .background(AeroAqua.copy(alpha = 0.22f))
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        )

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.52f),
                            Color.White.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PrimaryAeroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AeroWater,
            contentColor = Color.White,
            disabledContainerColor = AeroWater.copy(alpha = 0.42f),
            disabledContentColor = Color.White.copy(alpha = 0.72f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 5.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun ExpenseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AeroWater,
            unfocusedBorderColor = AeroSky.copy(alpha = 0.58f),
            focusedContainerColor = Color.White.copy(alpha = 0.62f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.46f),
            cursorColor = AeroWater,
            focusedLabelColor = AeroWater
        )
    )
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    centered: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MessageText(message: String, success: Boolean = false) {
    if (message.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = if (success) AeroWater else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
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

    AeroBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScreenHeader(
                        title = "Control de Gastos",
                        subtitle = "Inicia sesión o crea una cuenta"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ExpenseTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExpenseTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryAeroButton(
                        text = "Iniciar sesión",
                        onClick = { loginUser() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { registerUser() },
                        enabled = !isLoading
                    ) {
                        Text("Crear cuenta")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = AeroWater)
                    }

                    MessageText(message = message)
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
    AeroBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScreenHeader(
                        title = "Bienvenido",
                        subtitle = email
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Registra, consulta y resume tus gastos personales desde un espacio simple y claro.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryAeroButton(
                        text = "Agregar gasto",
                        onClick = onAddExpense,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryAeroButton(
                        text = "Ver historial",
                        onClick = onViewHistory,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryAeroButton(
                        text = "Ver resumen mensual",
                        onClick = onViewSummary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onLogout) {
                        Text("Cerrar sesión")
                    }
                }
            }
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

    AeroBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScreenHeader(
                        title = "Registrar gasto",
                        subtitle = "Guarda nombre, monto, categoría y fecha"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ExpenseTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nombre del gasto"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExpenseTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Monto",
                        keyboardType = KeyboardType.Decimal
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExpenseTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = "Categoría"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExpenseTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = "Fecha, ejemplo: 2026-05-04"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryAeroButton(
                        text = "Guardar gasto",
                        onClick = { saveExpense() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onBack,
                        enabled = !isLoading
                    ) {
                        Text("Volver")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = AeroWater)
                    }

                    MessageText(
                        message = message,
                        success = message.contains("correctamente")
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

    AeroBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            ScreenHeader(
                title = "Historial de gastos",
                subtitle = "Gastos registrados por el usuario autenticado.",
                centered = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = AeroWater)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    color = if (expenses.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(expenses) { expense ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = expense.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "\$${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AeroWater,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Categoría: ${expense.category}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Fecha: ${expense.date}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryAeroButton(
                text = "Volver",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
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

    AeroBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScreenHeader(
                        title = "Resumen mensual",
                        subtitle = "Mes: $month"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = AeroWater)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            AeroWater.copy(alpha = 0.92f),
                                            AeroAqua.copy(alpha = 0.86f),
                                            AeroMint.copy(alpha = 0.88f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total gastado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.92f)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "\$${String.format(Locale.getDefault(), "%.2f", total)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Cantidad de gastos: $expenseCount",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    MessageText(message = message)

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryAeroButton(
                        text = "Volver",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
