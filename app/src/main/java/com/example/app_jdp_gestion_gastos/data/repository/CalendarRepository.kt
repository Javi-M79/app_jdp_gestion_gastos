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

    fun getIncomeForDate(date: String, callback: (List<Income>) -> Unit) {
        val startOfDay = getStartOfDay(date)
        if (startOfDay == null) {
            Log.e("CalendaryRepository", "Error al convertir la fecha a Timestamp")
            callback(emptyList())
            return
        }

        val endOfDay = getEndOfDay(date)
        if (endOfDay == null){
            Log.e("CalendaryRepository", "Error al convertir la fecha a Timestamp")
            callback(emptyList())
            return
        }

        db.collection("incomes")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThan("date", endOfDay)
            .get()
            .addOnSuccessListener { result ->
                val incomes = result.mapNotNull { it.toObject(Income::class.java) }
                Log.d("CalendaryRepository", "Ingresos encontrados: ${incomes.size}")
                callback(incomes)
            }
            .addOnFailureListener { exception ->
                Log.e("CalendaryRepository", "Error al obtener ingresos: ${exception.message}")
                callback(emptyList())
            }
    }

    fun getExpenseForDate(date: String, callback: (List<Expense>) -> Unit) {
        val startOfDay = getStartOfDay(date)
        if (startOfDay == null) {
            Log.e("CalendaryRepository", "Error al convertir la fecha a Timestamp")
            callback(emptyList())
            return
        }

        val endOfDay = getEndOfDay(date)
        if (endOfDay == null){
            Log.e("CalendaryRepository", "Error al convertir la fecha a Timestamp")
            callback(emptyList())
            return
        }

        db.collection("expenses")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThan("date", endOfDay)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.mapNotNull { it.toObject(Expense::class.java) }
                Log.d("CalendaryRepository", "Gastos encontrados: ${expenses.size}")
                callback(expenses)
            }
            .addOnFailureListener { exception ->
                Log.e("CalendaryRepository", "Error al obtener gastos: ${exception.message}")
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

    private fun getStartOfDay(date: String): Timestamp? {
        val calendar = Calendar.getInstance()
        val parsedDate = parseDate(date) ?: return null

        return try {
            calendar.time = parsedDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            Timestamp(calendar.time)
        } catch (e: Exception) {
            Log.e("CalendaryRepository", "Error al convertir la fecha a Timestamp")
            null
        }
    }

    private fun getEndOfDay(date: String): Timestamp? {
        val calendar = Calendar.getInstance()
        val parsedDate = parseDate(date) ?: return null

        return try {
            calendar.time = parsedDate
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            Timestamp(calendar.time)
        } catch (e: Exception) {
            Log.e("CalendaryRepository", "Error al convertir la fecha a Timestamp")
            null
        }
    }
}