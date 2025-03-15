package me.knivram.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

object TemplateTable : Table("Template") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}

object TemplateExerciseTable : Table("TemplateExercise") {
    val id = integer("id").autoIncrement()
    val templateId = integer("templateId") references TemplateTable.id
    val exerciseId = integer("exerciseId")
    val sets = integer("sets")
    val reps = integer("reps")
    val position = integer("position")
    override val primaryKey = PrimaryKey(id)
}

data class Template(val id: Int, val name: String, val exercises: List<TemplateExercise>)
data class TemplateExercise(val id: Int, val templateId: Int, val exerciseId: Int, val sets: Int, val reps: Int, val position: Int)

object TemplateRepository {
    fun getAll(): List<Template> = transaction {
        TemplateTable.selectAll().map { row ->
            val tmplId = row[TemplateTable.id]
            val exercises = TemplateExerciseTable.selectAll().filter { it[TemplateExerciseTable.templateId] == tmplId }
                .map { TemplateExercise(
                    id = it[TemplateExerciseTable.id],
                    templateId = it[TemplateExerciseTable.templateId],
                    exerciseId = it[TemplateExerciseTable.exerciseId],
                    sets = it[TemplateExerciseTable.sets],
                    reps = it[TemplateExerciseTable.reps],
                    position = it[TemplateExerciseTable.position]
                ) }.sortedBy { it.position }
            Template(
                id = tmplId,
                name = row[TemplateTable.name],
                exercises = exercises
            )
        }
    }
    fun insert(template: Template): Int = transaction {
        val tmplId = TemplateTable.insert {
            it[name] = template.name
        } get TemplateTable.id
        template.exercises.forEach { ex ->
            TemplateExerciseTable.insert {
                it[templateId] = tmplId
                it[exerciseId] = ex.exerciseId
                it[sets] = ex.sets
                it[reps] = ex.reps
                it[position] = ex.position
            }
        }
        tmplId
    }
    fun update(template: Template) = transaction {
        TemplateTable.update({ TemplateTable.id eq template.id }) {
            it[name] = template.name
        }
        TemplateExerciseTable.deleteWhere { TemplateExerciseTable.templateId eq template.id }
        template.exercises.forEach { ex ->
            TemplateExerciseTable.insert {
                it[templateId] = template.id
                it[exerciseId] = ex.exerciseId
                it[sets] = ex.sets
                it[reps] = ex.reps
                it[position] = ex.position
            }
        }
    }
    fun delete(templateId: Int) = transaction {
        TemplateExerciseTable.deleteWhere { TemplateExerciseTable.templateId eq templateId }
        TemplateTable.deleteWhere { TemplateTable.id eq templateId }
    }
    fun getById(templateId: Int): Template? = transaction {
        TemplateTable.select(TemplateTable.id, TemplateTable.name).where { TemplateTable.id eq templateId }
            .firstNotNullOfOrNull { row ->
                val exercises =
                    TemplateExerciseTable.selectAll().filter { it[TemplateExerciseTable.templateId] == templateId }
                        .map {
                            TemplateExercise(
                                id = it[TemplateExerciseTable.id],
                                templateId = it[TemplateExerciseTable.templateId],
                                exerciseId = it[TemplateExerciseTable.exerciseId],
                                sets = it[TemplateExerciseTable.sets],
                                reps = it[TemplateExerciseTable.reps],
                                position = it[TemplateExerciseTable.position]
                            )
                        }.sortedBy { it.position }
                Template(
                    id = row[TemplateTable.id],
                    name = row[TemplateTable.name],
                    exercises = exercises
                )
            }
    }
}
