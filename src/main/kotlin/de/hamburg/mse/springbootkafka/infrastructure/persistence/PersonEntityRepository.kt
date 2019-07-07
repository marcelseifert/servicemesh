package de.hamburg.mse.springbootkafka.infrastructure.persistence

import de.hamburg.mse.springbootkafka.infrastructure.persistence.entity.PersonEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PersonEntityRepository : CrudRepository<PersonEntity, UUID>