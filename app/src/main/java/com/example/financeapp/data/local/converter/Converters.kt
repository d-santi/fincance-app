package com.example.financeapp.data.local.converter

import androidx.room.TypeConverter
import com.example.financeapp.domain.model.ExpenseCategory
import com.example.financeapp.domain.model.Participant
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromExpenseCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)

    @TypeConverter
    fun fromParticipantList(participants: List<Participant>): String {
        val array = JSONArray()
        participants.forEach { p ->
            array.put(JSONObject().apply {
                put("name", p.name)
                put("amount", p.amount)
                put("paid", p.paid)
            })
        }
        return array.toString()
    }

    @TypeConverter
    fun toParticipantList(json: String): List<Participant> {
        if (json.isBlank()) return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            array.getJSONObject(i).run {
                Participant(
                    name = getString("name"),
                    amount = getDouble("amount"),
                    paid = getBoolean("paid")
                )
            }
        }
    }
}
