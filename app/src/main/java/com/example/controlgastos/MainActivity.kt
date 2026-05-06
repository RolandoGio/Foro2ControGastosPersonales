package com.example.controlgastos

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.controlgastos.ui.theme.AeroAqua
import com.example.controlgastos.ui.theme.AeroGlow
import com.example.controlgastos.ui.theme.AeroMint
import com.example.controlgastos.ui.theme.AeroSky
import com.example.controlgastos.ui.theme.AeroWater
import com.example.controlgastos.ui.theme.ControlGastosPersonalesTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

data class Expense(
    val id: String = "",
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
                                    auth = auth,
                                    db = db,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (success) {
                        AeroMint.copy(alpha = 0.32f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (success) {
                        AeroAqua.copy(alpha = 0.42f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.22f)
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Text(
                text = message,
                color = if (success) AeroWater else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MoneyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AeroAqua.copy(alpha = 0.94f),
                            AeroWater.copy(alpha = 0.9f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        ExpenseTextField(
            value = value,
            onValueChange = onValueChange,
            label = "Monto",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Comida",
        "Transporte",
        "Estudio",
        "Servicios",
        "Salud",
        "Entretenimiento",
        "Otros"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            listOf("Comida", "Transporte"),
            listOf("Estudio", "Servicios"),
            listOf("Salud", "Otros"),
            listOf("Entretenimiento")
        ).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rowCategories.size == 1) {
                    val category = rowCategories.first()
                    CategoryOptionButton(
                        text = category,
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    rowCategories.forEach { category ->
                        CategoryOptionButton(
                            text = category,
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CategoryOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) AeroWater else Color.White.copy(alpha = 0.56f),
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 4.dp else 0.dp,
            pressedElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color.White.copy(alpha = 0.74f) else AeroSky.copy(alpha = 0.45f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun DateSelector(
    date: String,
    onChooseDate: () -> Unit,
    onUseCurrentDate: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Fecha",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = AeroSky.copy(alpha = 0.48f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (date.isBlank()) "Selecciona una fecha" else date,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (date.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Formato guardado: yyyy-MM-dd",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
                    onClick = onChooseDate,
                    enabled = enabled
                ) {
                    Text("Elegir fecha")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onUseCurrentDate,
                enabled = enabled
            ) {
                Text("Usar fecha actual")
            }
        }
    }
}

@Composable
fun IntegratedAeroPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.46f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.76f),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(18.dp)
    ) {
        content()
    }
}

@Composable
fun LedgerSurface(
    expenses: List<Expense>,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    IntegratedAeroPanel(modifier = modifier) {
        LazyColumn {
            itemsIndexed(expenses) { index, expense ->
                LedgerExpenseRow(
                    expense = expense,
                    onEditExpense = { onEditExpense(expense) },
                    onDeleteExpense = { onDeleteExpense(expense) }
                )

                if (index < expenses.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AeroSky.copy(alpha = 0.22f))
                    )
                }
            }
        }
    }
}

@Composable
fun LedgerExpenseRow(
    expense: Expense,
    onEditExpense: () -> Unit,
    onDeleteExpense: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(10.dp))

                CategoryLabel(text = expense.category)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEditExpense) {
                    Text("Editar")
                }

                TextButton(onClick = onDeleteExpense) {
                    Text("Eliminar")
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "\$${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
            style = MaterialTheme.typography.headlineSmall,
            color = AeroWater,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategoryLabel(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AeroMint.copy(alpha = 0.34f))
            .border(
                width = 1.dp,
                color = AeroAqua.copy(alpha = 0.34f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.ifBlank { "Sin categoría" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EmptyLedgerState(
    message: String,
    modifier: Modifier = Modifier
) {
    IntegratedAeroPanel(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AeroGlow.copy(alpha = 0.58f),
                                AeroAqua.copy(alpha = 0.26f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.72f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AeroWater,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = message.ifBlank { "Todavía no hay gastos registrados." },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Cuando agregues gastos, aparecerán aquí como un registro ordenado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var editedName by remember(expense.id) { mutableStateOf(expense.name) }
    var editedAmount by remember(expense.id) {
        mutableStateOf(String.format(Locale.US, "%.2f", expense.amount))
    }
    var editedCategory by remember(expense.id) { mutableStateOf(expense.category) }
    var editedDate by remember(expense.id) { mutableStateOf(expense.date) }
    var editMessage by remember(expense.id) { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    fun formatDateFromMillis(millis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(millis))
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = {
            Text("Editar gasto")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                MessageText(message = editMessage)

                ExpenseTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = "Nombre del gasto"
                )

                Spacer(modifier = Modifier.height(12.dp))

                MoneyTextField(
                    value = editedAmount,
                    onValueChange = { editedAmount = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CategorySelector(
                    selectedCategory = editedCategory,
                    onCategorySelected = { editedCategory = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                DateSelector(
                    date = editedDate,
                    onChooseDate = { showDatePicker = true },
                    onUseCurrentDate = { editedDate = getCurrentDate() },
                    enabled = !isSaving
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = editedAmount.toDoubleOrNull()

                    editMessage = when {
                        editedName.isBlank() -> "El nombre del gasto no puede estar vacío."
                        editedAmount.isBlank() -> "El monto no puede estar vacío."
                        amountValue == null -> "El monto debe ser numérico."
                        amountValue <= 0.0 -> "El monto debe ser mayor que cero."
                        editedCategory.isBlank() -> "La categoría no puede estar vacía."
                        editedDate.isBlank() -> "La fecha no puede estar vacía."
                        else -> ""
                    }

                    if (editMessage.isBlank()) {
                        onSave(
                            editedName,
                            editedAmount,
                            editedCategory,
                            editedDate
                        )
                    }
                },
                enabled = !isSaving
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancelar")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            editedDate = formatDateFromMillis(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun MonthlyDashboard(
    month: String,
    total: Double,
    expenseCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Mes consultado",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = month,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(22.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AeroWater.copy(alpha = 0.96f),
                            AeroAqua.copy(alpha = 0.88f),
                            AeroMint.copy(alpha = 0.9f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                Text(
                    text = "Total mensual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
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

        IntegratedAeroPanel(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Movimientos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Gastos registrados este mes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = expenseCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = AeroWater,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MonthNavigator(
    selectedMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        IntegratedAeroPanel(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onPreviousMonth) {
                    Text("Mes anterior")
                }

                Text(
                    text = selectedMonth,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onNextMonth) {
                    Text("Mes siguiente")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCurrentMonth) {
                Text("Mes actual")
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showAddGoogleAccountAction by remember { mutableStateOf(false) }

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
        showAddGoogleAccountAction = false

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
        showAddGoogleAccountAction = false

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

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading = false

                if (task.isSuccessful) {
                    onLoginSuccess(auth.currentUser?.email ?: "Cuenta de Google")
                } else {
                    message = task.exception?.message ?: "No se pudo iniciar sesión con Google."
                }
            }
    }

    fun handleGoogleCredential(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (exception: GoogleIdTokenParsingException) {
                isLoading = false
                showAddGoogleAccountAction = false
                message = "No se pudo leer la cuenta de Google. Inténtalo nuevamente."
            }
        } else {
            isLoading = false
            showAddGoogleAccountAction = false
            message = "La credencial recibida no corresponde a una cuenta de Google."
        }
    }

    fun openAddGoogleAccountSettings() {
        try {
            val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra("account_types", arrayOf("com.google"))
            }
            context.startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            message = "No se pudo abrir la configuración de cuentas en este dispositivo."
        }
    }

    fun signInWithGoogle() {
        isLoading = true
        message = ""
        showAddGoogleAccountAction = false

        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = CredentialManager.create(context).getCredential(
                    context = context,
                    request = request
                )

                handleGoogleCredential(result.credential)
            } catch (exception: GetCredentialCancellationException) {
                isLoading = false
                showAddGoogleAccountAction = false
                message = "Inicio con Google cancelado."
            } catch (exception: NoCredentialException) {
                isLoading = false
                showAddGoogleAccountAction = true
                message = "No hay una cuenta de Google disponible en este dispositivo. Agrega una cuenta en el emulador o intenta con correo y contraseña."
            } catch (exception: GetCredentialException) {
                isLoading = false
                showAddGoogleAccountAction = true
                message = "No hay una cuenta de Google disponible en este dispositivo. Agrega una cuenta en el emulador o intenta con correo y contraseña."
            } catch (exception: Exception) {
                isLoading = false
                showAddGoogleAccountAction = false
                message = "Ocurrió un problema al iniciar sesión con Google."
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { signInWithGoogle() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.72f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.White.copy(alpha = 0.36f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, AeroSky.copy(alpha = 0.48f))
                    ) {
                        Text(
                            text = "Continuar con Google",
                            style = MaterialTheme.typography.labelLarge
                        )
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
                        CircularProgressIndicator(color = AeroWater)
                    }

                    MessageText(message = message)

                    if (showAddGoogleAccountAction) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { openAddGoogleAccountSettings() },
                            enabled = !isLoading
                        ) {
                            Text("Agregar cuenta de Google")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    email: String,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onAddExpense: () -> Unit,
    onViewHistory: () -> Unit,
    onViewSummary: () -> Unit,
    onLogout: () -> Unit
) {
    var monthlyTotal by remember { mutableStateOf(0.0) }
    var monthlyCount by remember { mutableStateOf(0) }
    var lastExpense by remember { mutableStateOf<Expense?>(null) }
    var dashboardMessage by remember { mutableStateOf("") }
    var isDashboardLoading by remember { mutableStateOf(true) }

    val currentUser = auth.currentUser
    val displayName = currentUser?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: email.substringBefore("@").replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
        }

    fun getCurrentMonth(): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return formatter.format(Date())
    }

    LaunchedEffect(email) {
        if (currentUser == null) {
            dashboardMessage = "No hay un usuario autenticado."
            isDashboardLoading = false
            return@LaunchedEffect
        }

        isDashboardLoading = true
        dashboardMessage = ""

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .whereEqualTo("month", getCurrentMonth())
            .get()
            .addOnSuccessListener { result ->
                val currentMonthExpenses = result.documents.map { document ->
                    Expense(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        amount = document.getDouble("amount") ?: 0.0,
                        category = document.getString("category") ?: "",
                        date = document.getString("date") ?: "",
                        month = document.getString("month") ?: ""
                    )
                }.sortedByDescending { expense ->
                    expense.date
                }

                monthlyTotal = currentMonthExpenses.sumOf { it.amount }
                monthlyCount = currentMonthExpenses.size
                lastExpense = currentMonthExpenses.firstOrNull()
                dashboardMessage = if (currentMonthExpenses.isEmpty()) {
                    "Aún no hay gastos este mes."
                } else {
                    ""
                }
                isDashboardLoading = false
            }
            .addOnFailureListener { exception ->
                dashboardMessage = exception.message ?: "No se pudo cargar el dashboard."
                isDashboardLoading = false
            }
    }

    AeroBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Hola, $displayName",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(22.dp))

            IntegratedAeroPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mes actual",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isDashboardLoading) {
                        CircularProgressIndicator(color = AeroWater)
                    } else {
                        Text(
                            text = "\$${String.format(Locale.getDefault(), "%.2f", monthlyTotal)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AeroWater,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$monthlyCount movimientos registrados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AeroSky.copy(alpha = 0.24f))
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Último movimiento",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (lastExpense != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lastExpense?.name ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "${lastExpense?.date ?: ""} · ${lastExpense?.category ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "\$${String.format(Locale.getDefault(), "%.2f", lastExpense?.amount ?: 0.0)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = AeroWater,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    MessageText(message = dashboardMessage, success = monthlyCount == 0)

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Acciones rápidas",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryAeroButton(
                        text = "Agregar gasto",
                        onClick = onAddExpense,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PrimaryAeroButton(
                            text = "Historial",
                            onClick = onViewHistory,
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryAeroButton(
                            text = "Resumen",
                            onClick = onViewSummary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = onLogout) {
                Text("Cerrar sesión")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

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

    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    fun formatDateFromMillis(millis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(millis))
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

                    MoneyTextField(
                        value = amount,
                        onValueChange = { amount = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CategorySelector(
                        selectedCategory = category,
                        onCategorySelected = { category = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DateSelector(
                        date = date,
                        onChooseDate = { showDatePicker = true },
                        onUseCurrentDate = { date = getCurrentDate() },
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            date = formatDateFromMillis(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
    var messageIsSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val currentUser = auth.currentUser

    fun getCurrentMonth(): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return formatter.format(Date())
    }

    var selectedMonth by remember { mutableStateOf(getCurrentMonth()) }

    fun shiftMonth(monthText: String, offset: Int): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
        val dateValue = formatter.parse(monthText) ?: Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = dateValue
        calendar.add(java.util.Calendar.MONTH, offset)
        return formatter.format(calendar.time)
    }

    fun getMonthFromDate(dateText: String): String {
        return if (dateText.length >= 7) {
            dateText.substring(0, 7)
        } else {
            "sin_mes"
        }
    }

    fun loadExpenses(clearMessage: Boolean = true) {
        if (currentUser == null) {
            message = "No hay un usuario autenticado."
            messageIsSuccess = false
            isLoading = false
            return
        }

        isLoading = true
        if (clearMessage) {
            message = ""
            messageIsSuccess = false
        }

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .whereEqualTo("month", selectedMonth)
            .get()
            .addOnSuccessListener { result ->
                expenses = result.documents.map { document ->
                    Expense(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        amount = document.getDouble("amount") ?: 0.0,
                        category = document.getString("category") ?: "",
                        date = document.getString("date") ?: "",
                        month = document.getString("month") ?: ""
                    )
                }.sortedByDescending { expense ->
                    expense.date
                }

                isLoading = false

                if (expenses.isEmpty() && message.isBlank()) {
                    message = "No hay gastos registrados para este mes."
                    messageIsSuccess = false
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                message = exception.message ?: "No se pudo cargar el historial."
                messageIsSuccess = false
            }
    }

    fun updateExpense(
        expense: Expense,
        updatedName: String,
        updatedAmount: String,
        updatedCategory: String,
        updatedDate: String
    ) {
        val user = currentUser
        val amountValue = updatedAmount.toDoubleOrNull()

        if (user == null) {
            message = "No hay un usuario autenticado."
            messageIsSuccess = false
            return
        }

        if (expense.id.isBlank()) {
            message = "No se pudo identificar el gasto seleccionado."
            messageIsSuccess = false
            return
        }

        if (updatedName.isBlank()) {
            message = "El nombre del gasto no puede estar vacío."
            messageIsSuccess = false
            return
        }

        if (updatedAmount.isBlank()) {
            message = "El monto no puede estar vacío."
            messageIsSuccess = false
            return
        }

        if (amountValue == null) {
            message = "El monto debe ser numérico."
            messageIsSuccess = false
            return
        }

        if (amountValue <= 0.0) {
            message = "El monto debe ser mayor que cero."
            messageIsSuccess = false
            return
        }

        if (updatedCategory.isBlank()) {
            message = "La categoría no puede estar vacía."
            messageIsSuccess = false
            return
        }

        if (updatedDate.isBlank()) {
            message = "La fecha no puede estar vacía."
            messageIsSuccess = false
            return
        }

        isUpdating = true
        message = ""
        messageIsSuccess = false

        val updates = mapOf(
            "name" to updatedName.trim(),
            "amount" to amountValue,
            "category" to updatedCategory.trim(),
            "date" to updatedDate.trim(),
            "month" to getMonthFromDate(updatedDate.trim())
        )

        db.collection("users")
            .document(user.uid)
            .collection("expenses")
            .document(expense.id)
            .update(updates)
            .addOnSuccessListener {
                isUpdating = false
                expenseToEdit = null
                message = "Gasto actualizado correctamente."
                messageIsSuccess = true
                loadExpenses(clearMessage = false)
            }
            .addOnFailureListener { exception ->
                isUpdating = false
                message = exception.message ?: "No se pudo actualizar el gasto."
                messageIsSuccess = false
            }
    }

    fun deleteExpense(expense: Expense) {
        val user = currentUser

        if (user == null) {
            message = "No hay un usuario autenticado."
            messageIsSuccess = false
            return
        }

        if (expense.id.isBlank()) {
            message = "No se pudo identificar el gasto seleccionado."
            messageIsSuccess = false
            return
        }

        isDeleting = true
        message = ""
        messageIsSuccess = false

        db.collection("users")
            .document(user.uid)
            .collection("expenses")
            .document(expense.id)
            .delete()
            .addOnSuccessListener {
                isDeleting = false
                expenseToDelete = null
                message = "Gasto eliminado correctamente."
                messageIsSuccess = true
                loadExpenses(clearMessage = false)
            }
            .addOnFailureListener { exception ->
                isDeleting = false
                expenseToDelete = null
                message = exception.message ?: "No se pudo eliminar el gasto."
                messageIsSuccess = false
            }
    }

    LaunchedEffect(selectedMonth) {
        loadExpenses()
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

            Spacer(modifier = Modifier.height(14.dp))

            MonthNavigator(
                selectedMonth = selectedMonth,
                onPreviousMonth = {
                    selectedMonth = shiftMonth(selectedMonth, -1)
                },
                onNextMonth = {
                    selectedMonth = shiftMonth(selectedMonth, 1)
                },
                onCurrentMonth = {
                    selectedMonth = getCurrentMonth()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator(color = AeroWater)
                Spacer(modifier = Modifier.height(12.dp))
            }

            MessageText(
                message = message,
                success = messageIsSuccess
            )

            if (!isLoading && expenses.isNotEmpty()) {
                LedgerSurface(
                    expenses = expenses,
                    onEditExpense = { expenseToEdit = it },
                    onDeleteExpense = { expenseToDelete = it },
                    modifier = Modifier.weight(1f)
                )
            } else if (!isLoading) {
                EmptyLedgerState(
                    message = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryAeroButton(
                text = "Volver",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    expenseToDelete = null
                }
            },
            title = {
                Text("Eliminar gasto")
            },
            text = {
                Text("¿Seguro que deseas eliminar este gasto?")
            },
            confirmButton = {
                TextButton(
                    onClick = { deleteExpense(expense) },
                    enabled = !isDeleting
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { expenseToDelete = null },
                    enabled = !isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    expenseToEdit?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            isSaving = isUpdating,
            onDismiss = {
                if (!isUpdating) {
                    expenseToEdit = null
                }
            },
            onSave = { updatedName, updatedAmount, updatedCategory, updatedDate ->
                updateExpense(
                    expense = expense,
                    updatedName = updatedName,
                    updatedAmount = updatedAmount,
                    updatedCategory = updatedCategory,
                    updatedDate = updatedDate
                )
            }
        )
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
    var expenseCount by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser = auth.currentUser

    fun getCurrentMonth(): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return formatter.format(Date())
    }

    var selectedMonth by remember { mutableStateOf(getCurrentMonth()) }

    fun shiftMonth(monthText: String, offset: Int): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
        val dateValue = formatter.parse(monthText) ?: Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = dateValue
        calendar.add(java.util.Calendar.MONTH, offset)
        return formatter.format(calendar.time)
    }

    LaunchedEffect(selectedMonth) {
        if (currentUser == null) {
            message = "No hay un usuario autenticado."
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        total = 0.0
        expenseCount = 0
        message = ""

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .whereEqualTo("month", selectedMonth)
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                ScreenHeader(
                    title = "Resumen mensual",
                    subtitle = "Vista limpia de tu actividad del mes",
                    centered = false
                )

                Spacer(modifier = Modifier.height(18.dp))

                MonthNavigator(
                    selectedMonth = selectedMonth,
                    onPreviousMonth = {
                        selectedMonth = shiftMonth(selectedMonth, -1)
                    },
                    onNextMonth = {
                        selectedMonth = shiftMonth(selectedMonth, 1)
                    },
                    onCurrentMonth = {
                        selectedMonth = getCurrentMonth()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = AeroWater)
                } else {
                    MonthlyDashboard(
                        month = selectedMonth,
                        total = total,
                        expenseCount = expenseCount
                    )
                }

                MessageText(
                    message = message,
                    success = !isLoading && expenseCount == 0
                )

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
