package de.hamburg.mse.springbootkafka.model

import java.time.LocalDate

data class Person(
        val firstName: String,
        val lastName: String,
        val birthDate: LocalDate
)
