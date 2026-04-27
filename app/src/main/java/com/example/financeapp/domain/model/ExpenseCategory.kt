package com.example.financeapp.domain.model

enum class ExpenseCategory(val displayName: String) {
    FOOD("Comida"),
    TRANSPORT("Transporte"),
    SHOPPING("Compras"),
    ENTERTAINMENT("Entretenimiento"),
    HEALTH("Salud"),
    BILLS("Servicios"),
    EDUCATION("Educación"),
    GIFT("Regalos"),
    TRAVEL("Viajes"),
    OTHER("Otros")
}