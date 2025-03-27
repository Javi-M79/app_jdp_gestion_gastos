package com.example.app_jdp_gestion_gastos.data.repository

import com.example.app_jdp_gestion_gastos.data.model.Expense
import com.example.app_jdp_gestion_gastos.data.model.Income
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CalendaryRepository {
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

    // Obtener ingresos por fecha y usuario
    fun getIncomeForDate(date: String, callback: (List<Income>) -> Unit) {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)

        db.collection("incomes")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThan("date", endOfDay)
            .get()
            .addOnSuccessListener { result ->
                val incomes = result.mapNotNull { it.toObject(Income::class.java) }
                callback(incomes)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Obtener gastos por fecha y usuario
    fun getExpenseForDate(date: String, callback: (List<Expense>) -> Unit) {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)

        db.collection("expenses")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThan("date", endOfDay)
            .get()
            .addOnSuccessListener { result ->
                val expenses = result.mapNotNull { it.toObject(Expense::class.java) }
                callback(expenses)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Guardar un ingreso
    fun saveIncome(income: Income, callback: (Boolean) -> Unit) {
        // Aquí utilizamos Timestamp.now() para la fecha actual
        val incomeWithTimestamp = income.copy(date = Timestamp.now())
        db.collection("incomes").add(incomeWithTimestamp)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // Guardar un gasto
    fun saveExpense(expense: Expense, callback: (Boolean) -> Unit) {
        // Aquí utilizamos Timestamp.now() para la fecha actual
        val expenseWithTimestamp = expense.copy(date = Timestamp.now())
        db.collection("expenses").add(expenseWithTimestamp)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // Función para obtener el inicio del día en Timestamp
    private fun getStartOfDay(date: String): Timestamp {
        try {
            val calendar = Calendar.getInstance()
            val day = date.substring(0, 2).toInt() // Día
            val month = date.substring(3, 5).toInt() - 1 // Mes (0-11)
            val year = date.substring(6, 10).toInt() // Año

            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.set(Calendar.HOUR_OF_DAY, 0) // Hora al inicio del día
            calendar.set(Calendar.MINUTE, 0) // Minuto 0
            calendar.set(Calendar.SECOND, 0) // Segundo 0
            calendar.set(Calendar.MILLISECOND, 0) // Milisegundo 0

            return Timestamp(calendar.time)
        } catch (e: Exception) {
            e.printStackTrace()  // Añadir manejo de errores para debugging
            return Timestamp(Date())  // Retorna la fecha actual en caso de error
        }
    }

    // Función para obtener el final del día en Timestamp
    private fun getEndOfDay(date: String): Timestamp {
        try {
            val calendar = Calendar.getInstance()
            val day = date.substring(0, 2).toInt() // Día
            val month = date.substring(3, 5).toInt() - 1 // Mes (0-11)
            val year = date.substring(6, 10).toInt() // Año

            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.set(Calendar.HOUR_OF_DAY, 23) // Hora al final del día
            calendar.set(Calendar.MINUTE, 59) // Minuto 59
            calendar.set(Calendar.SECOND, 59) // Segundo 59
            calendar.set(Calendar.MILLISECOND, 999) // Milisegundo 999

            return Timestamp(calendar.time)
        } catch (e: Exception) {
            e.printStackTrace()  // Añadir manejo de errores para debugging
            return Timestamp(Date())  // Retorna la fecha actual en caso de error
        }
    }
}