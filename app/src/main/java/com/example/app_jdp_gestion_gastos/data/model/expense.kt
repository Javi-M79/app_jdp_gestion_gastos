package com.example.app_jdp_gestion_gastos.data.model

import com.google.firebase.firestore.PropertyName


class expense(

    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("importance") @set:PropertyName("importance") var importance: String = "",
    @get:PropertyName("colorCode") @set:PropertyName("colorCode") var colorCode: String = "",
    @get:PropertyName("isRecurring") @set:PropertyName("isRecurring") var isRecurring: Boolean = false,
    @get:PropertyName("recurrence") @set:PropertyName("userId") var recurrence: String = "",


    ) {
}