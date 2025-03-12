package me.knivram.repository

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object TodoRepository {
    fun insert(todo: Todo): Int {
        return transaction {
            TodoTable.insert {
                it[title] = todo.title
                it[description] = todo.description
                it[done] = todo.done
            } get TodoTable.id
        }
    }

//    fun delete(todoId: Int) {
//        transaction {
//            TodoTable.deleteWhere { id eq todoId }
//        }
//    }

    fun update(todo: Todo) {
        transaction {
            TodoTable.update({ TodoTable.id eq todo.id }) {
                it[title] = todo.title
                it[description] = todo.description
                it[done] = todo.done
            }
        }
    }

    fun getAll(): List<Todo> {
        return transaction {
            TodoTable.selectAll().map {
                Todo(
                    it[TodoTable.id],
                    it[TodoTable.title],
                    it[TodoTable.description],
                    it[TodoTable.done]
                )
            }
        }
    }
}