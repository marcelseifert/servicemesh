package de.hamburg.mse.springbootkafka.domain.repository

import de.hamburg.mse.springbootkafka.domain.model.Person
import java.util.*

interface PersonRepo {
    fun findId(id: UUID): Person
}