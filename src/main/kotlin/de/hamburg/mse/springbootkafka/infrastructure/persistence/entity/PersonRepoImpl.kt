package de.hamburg.mse.springbootkafka.infrastructure.persistence.entity

import de.hamburg.mse.springbootkafka.domain.model.Person
import de.hamburg.mse.springbootkafka.domain.repository.PersonRepo
import de.hamburg.mse.springbootkafka.infrastructure.persistence.PersonEntityRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import kotlin.NoSuchElementException

class PersonRepoImpl constructor(@Autowired val personEntityRepository: PersonEntityRepository) : PersonRepo {

    override fun findId(id: UUID): Person {
        val personEntity = personEntityRepository.findByIdOrNull(id);
        if (personEntity != null) {
            return personEntity.toDomainModel()
        } else {
            throw NoSuchElementException()
        }
    }
}