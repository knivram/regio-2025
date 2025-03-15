package me.knivram.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.update

object ExerciseTable : Table("Exercise") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    val description = varchar("description", 1024)
    val type = varchar("type", 255)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Exercise(val id: Int, val name: String, val description: String, val type: String) {
    companion object {
        fun fromRow(row: ResultRow): Exercise = Exercise(
            id = row[ExerciseTable.id],
            name = row[ExerciseTable.name],
            description = row[ExerciseTable.description],
            type = row[ExerciseTable.type]
        )
    }
}

@Serializable
data class ExerciseJson(val name: String, val description: String, val type: String)

object ExerciseRepository {
    val table = ExerciseTable
    fun getAll(): List<Exercise> = transaction {
        ExerciseTable.selectAll().map { Exercise.fromRow(it) }
    }
    fun insert(exercise: Exercise): Int = transaction {
        ExerciseTable.insert {
            it[name] = exercise.name
            it[description] = exercise.description
            it[type] = exercise.type
        } get ExerciseTable.id
    }
    fun update(exercise: Exercise) = transaction {
        ExerciseTable.update({ ExerciseTable.id eq exercise.id }) {
            it[name] = exercise.name
            it[description] = exercise.description
            it[type] = exercise.type
        }
    }
    fun delete(exerciseId: Int) = transaction {
        ExerciseTable.deleteWhere { ExerciseTable.id eq exerciseId }
    }
    fun importFromJson(jsonText: String) {
        val exercises = Json.decodeFromString<List<ExerciseJson>>(jsonText)
        exercises.forEach {
            insert(Exercise(0, it.name, it.description, it.type))
        }
    }
}
