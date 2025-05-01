import android.util.Log
import com.example.app_jdp_gestion_gastos.data.model.Expense
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarRepository {
    private val db = FirebaseFirestore.getInstance()

    // Obtener todos los ingresos de un usuario
    fun getAllIncomes(userId: String, callback: (List<Income>) -> Unit) {
        db.collection("incomes")
            .whereEqualTo("userId", userId) // Filtrar por el userId del usuario
            .get()
            .addOnSuccessListener { result ->
                val incomes = result.mapNotNull { it.toObject(Income::class.java) }
                callback(incomes)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Obtener todos los gastos de un usuario
    fun getAllExpenses(userId: String, callback: (List<Expense>) -> Unit) {
        db.collection("expenses")
            .whereEqualTo("userId", userId) // Filtrar por el userId del usuario
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.mapNotNull { it.toObject(Expense::class.java) }
                callback(expenses)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Obtener ingresos para una fecha específica (rango de tiempo completo del día)
    fun getIncomeForDate(date: Timestamp, userId: String, callback: (List<Income>) -> Unit) {
        val startOfDay = getStartOfDay(date) ?: return callback(emptyList())
        val endOfDay = getEndOfDay(date) ?: return callback(emptyList())

        db.collection("incomes")
            .whereEqualTo("userId", userId)  // Filtrar por userId
            .whereGreaterThanOrEqualTo("date", startOfDay)  // Comparar con Timestamp
            .whereLessThan("date", endOfDay)  // Comparar con Timestamp
            .get()
            .addOnSuccessListener { result ->
                val incomes = result.mapNotNull { it.toObject(Income::class.java) }
                Log.d("CalendarRepository", "Ingresos encontrados: ${incomes.size}")
                callback(incomes)
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarRepository", "Error al obtener ingresos: ${exception.message}")
                callback(emptyList())
            }
    }

    // Obtener gastos para una fecha específica (rango de tiempo completo del día)
    fun getExpenseForDate(date: Timestamp, userId: String, callback: (List<Expense>) -> Unit) {
        val startOfDay = getStartOfDay(date) ?: return callback(emptyList())
        val endOfDay = getEndOfDay(date) ?: return callback(emptyList())

        db.collection("expenses")
            .whereEqualTo("userId", userId)  // Filtrar por userId
            .whereGreaterThanOrEqualTo("date", startOfDay)  // Comparar con Timestamp
            .whereLessThan("date", endOfDay)  // Comparar con Timestamp
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.mapNotNull { it.toObject(Expense::class.java) }
                Log.d("CalendarRepository", "Gastos encontrados: ${expenses.size}")
                callback(expenses)
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarRepository", "Error al obtener gastos: ${exception.message}")
                callback(emptyList())
            }
    }

    // Guardar un ingreso
    fun saveIncome(income: Income, callback: (Boolean) -> Unit) {
        val incomeWithTimestamp = income.copy(date = income.date)
        db.collection("incomes").add(incomeWithTimestamp)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // Guardar un gasto
    fun saveExpense(expense: Expense, callback: (Boolean) -> Unit) {
        val expenseWithTimestamp = expense.copy(date = expense.date)
        db.collection("expenses").add(expenseWithTimestamp)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // Convertir la fecha String a Timestamp
    private fun convertToTimestamp(date: String): Timestamp? {
        val parsedDate = parseDate(date) ?: return null
        return Timestamp(parsedDate)
    }

    // Parsear la fecha en diferentes formatos posibles
    private fun parseDate(date: String): Date? {
        val possibleFormats = arrayOf(
            "dd-MM-yyyy", // 20-03-2025
            "EEE MMM dd HH:mm:ss zzz yyyy" // Wed Mar 19 00:00:00 GMT+01:00 2025
        )

        for (format in possibleFormats) {
            try {
                val dateFormat = SimpleDateFormat(format, Locale.ENGLISH)
                return dateFormat.parse(date)
            } catch (e: Exception) {
                // Ignoramos y probamos con el siguiente formato
            }
        }

        Log.e("CalendaryRepository", "Error al parsear la fecha: $date")
        return null
    }

    // Obtener el inicio del día (00:00:00) para una fecha
    private fun getStartOfDay(date: Timestamp): Timestamp? {
        val calendar = Calendar.getInstance()
        calendar.time = date.toDate()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return Timestamp(calendar.time)  // Devolvemos Timestamp
    }

    // Obtener el final del día (23:59:59) para una fecha
    private fun getEndOfDay(date: Timestamp): Timestamp? {
        val calendar = Calendar.getInstance()
        calendar.time = date.toDate()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        return Timestamp(calendar.time)  // Devolvemos Timestamp
    }
}