package com.example.momentum.model

import androidx.annotation.DrawableRes

data class Habit(
    val name: String,
    @DrawableRes val iconRes: Int,
    var isCompleted: Boolean
)