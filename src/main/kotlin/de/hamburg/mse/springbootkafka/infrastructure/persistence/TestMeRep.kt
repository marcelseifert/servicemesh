package de.hamburg.mse.springbootkafka.infrastructure.persistence

import de.hamburg.mse.springbootkafka.domain.model.Person
import de.hamburg.mse.springbootkafka.domain.repository.PersonRepo
import de.hamburg.mse.springbootkafka.infrastructure.persistence.entity.PersonEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TestMeRep : CrudRepository<PersonEntity, UUID>, PersonRepo {

    override fun findId(id: UUID): Person {
        return findById(id).get().toDomainModel()
    }
}