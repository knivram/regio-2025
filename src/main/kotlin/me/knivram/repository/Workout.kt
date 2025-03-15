package me.knivram.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.Duration

object WorkoutTable : Table("Workout") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    val startDatetime = datetime("startDatetime")
    val endDatetime = datetime("endDatetime")
    override val primaryKey = PrimaryKey(id)
}

object WorkoutExerciseTable : Table("WorkoutExercise") {
    val id = integer("id").autoIncrement()
    val workoutId = integer("workoutId") references WorkoutTable.id
    val exerciseId = integer("exerciseId")
    val position = integer("position")
    override val primaryKey = PrimaryKey(id)
}

object WorkoutSetTable : Table("WorkoutSet") {
    val id = integer("id").autoIncrement()
    val workoutExerciseId = integer("workoutExerciseId") references WorkoutExerciseTable.id
    val setNumber = integer("setNumber")
    val weight = double("weight")
    val reps = integer("reps")
    override val primaryKey = PrimaryKey(id)
}

data class Workout(val id: Int, val name: String, val startDatetime: LocalDateTime, val endDatetime: LocalDateTime, val exercises: List<WorkoutExercise>) {
    fun duration(): Duration = Duration.between(startDatetime, endDatetime)
}

data class WorkoutExercise(var id: Int, var workoutId: Int, var exerciseId: Int, var position: Int, var sets: List<WorkoutSet>)
data class WorkoutSet(val id: Int, val workoutExerciseId: Int, val setNumber: Int, val weight: Double, val reps: Int)

object WorkoutRepository {
    fun getAll(): List<Workout> = transaction {
        WorkoutTable.selectAll().map { row ->
            val workoutId = row[WorkoutTable.id]
            val exercises = WorkoutExerciseTable.selectAll().filter { it[WorkoutExerciseTable.workoutId] == workoutId }
                .map { exRow ->
                    val weId = exRow[WorkoutExerciseTable.id]
                    val sets = WorkoutSetTable.selectAll().filter { it[WorkoutSetTable.workoutExerciseId] == weId }
                        .map { setRow ->
                            WorkoutSet(
                                id = setRow[WorkoutSetTable.id],
                                workoutExerciseId = setRow[WorkoutSetTable.workoutExerciseId],
                                setNumber = setRow[WorkoutSetTable.setNumber],
                                weight = setRow[WorkoutSetTable.weight],
                                reps = setRow[WorkoutSetTable.reps]
                            )
                        }.sortedBy { it.setNumber }
                    WorkoutExercise(
                        id = weId,
                        workoutId = exRow[WorkoutExerciseTable.workoutId],
                        exerciseId = exRow[WorkoutExerciseTable.exerciseId],
                        position = exRow[WorkoutExerciseTable.position],
                        sets = sets
                    )
                }.sortedBy { it.position }
            Workout(
                id = workoutId,
                name = row[WorkoutTable.name],
                startDatetime = row[WorkoutTable.startDatetime],
                endDatetime = row[WorkoutTable.endDatetime],
                exercises = exercises
            )
        }
    }
    fun insert(workout: Workout): Int = transaction {
        val workoutId = WorkoutTable.insert {
            it[name] = workout.name
            it[startDatetime] = workout.startDatetime
            it[endDatetime] = workout.endDatetime
        } get WorkoutTable.id
        workout.exercises.forEach { ex ->
            val weId = WorkoutExerciseTable.insert {
                it[WorkoutExerciseTable.workoutId] = workoutId
                it[exerciseId] = ex.exerciseId
                it[position] = ex.position
            } get WorkoutExerciseTable.id
            ex.sets.forEach { set ->
                WorkoutSetTable.insert {
                    it[workoutExerciseId] = weId
                    it[setNumber] = set.setNumber
                    it[weight] = set.weight
                    it[reps] = set.reps
                }
            }
        }
        workoutId
    }
    fun update(workout: Workout) = transaction {
        WorkoutTable.update({ WorkoutTable.id eq workout.id }) {
            it[name] = workout.name
            it[startDatetime] = workout.startDatetime
            it[endDatetime] = workout.endDatetime
        }
        WorkoutExerciseTable.deleteWhere { WorkoutExerciseTable.workoutId eq workout.id }
        workout.exercises.forEach { ex ->
            val weId = WorkoutExerciseTable.insert {
                it[workoutId] = workout.id
                it[exerciseId] = ex.exerciseId
                it[position] = ex.position
            } get WorkoutExerciseTable.id
            ex.sets.forEach { set ->
                WorkoutSetTable.insert {
                    it[workoutExerciseId] = weId
                    it[setNumber] = set.setNumber
                    it[weight] = set.weight
                    it[reps] = set.reps
                }
            }
        }
    }
    fun delete(workoutId: Int) = transaction {
        val weIds = WorkoutExerciseTable.selectAll().filter { it[WorkoutExerciseTable.workoutId] == workoutId }
            .map { it[WorkoutExerciseTable.id] }
        weIds.forEach { weId ->
            WorkoutSetTable.deleteWhere { WorkoutSetTable.workoutExerciseId eq weId }
        }
        WorkoutExerciseTable.deleteWhere { WorkoutExerciseTable.workoutId eq workoutId }
        WorkoutTable.deleteWhere { WorkoutTable.id eq workoutId }
    }
    fun getById(workoutId: Int): Workout? = transaction {
        WorkoutTable.select(WorkoutTable.columns).where { WorkoutTable.id eq workoutId }.firstNotNullOfOrNull { row ->
            val exercises = WorkoutExerciseTable.selectAll().filter { it[WorkoutExerciseTable.workoutId] == workoutId }
                .map { exRow ->
                    val weId = exRow[WorkoutExerciseTable.id]
                    val sets = WorkoutSetTable.selectAll().filter { it[WorkoutSetTable.workoutExerciseId] == weId }
                        .map { setRow ->
                            WorkoutSet(
                                id = setRow[WorkoutSetTable.id],
                                workoutExerciseId = setRow[WorkoutSetTable.workoutExerciseId],
                                setNumber = setRow[WorkoutSetTable.setNumber],
                                weight = setRow[WorkoutSetTable.weight],
                                reps = setRow[WorkoutSetTable.reps]
                            )
                        }.sortedBy { it.setNumber }
                    WorkoutExercise(
                        id = weId,
                        workoutId = exRow[WorkoutExerciseTable.workoutId],
                        exerciseId = exRow[WorkoutExerciseTable.exerciseId],
                        position = exRow[WorkoutExerciseTable.position],
                        sets = sets
                    )
                }.sortedBy { it.position }
            Workout(
                id = row[WorkoutTable.id],
                name = row[WorkoutTable.name],
                startDatetime = row[WorkoutTable.startDatetime],
                endDatetime = row[WorkoutTable.endDatetime],
                exercises = exercises
            )
        }
    }
}
