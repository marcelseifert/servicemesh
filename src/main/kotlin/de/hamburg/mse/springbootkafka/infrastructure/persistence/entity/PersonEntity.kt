package de.hamburg.mse.springbootkafka.infrastructure.persistence.entity

import de.hamburg.mse.springbootkafka.domain.model.Person
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class PersonEntity(@Id var uuid: UUID, var firstname: String, var lastname: String) {

    fun toDomainModel(): Person {
        return Person(uuid, firstname, lastname)
    }
}