package me.knivram.repository

import org.jetbrains.exposed.sql.Table


object TodoTable : Table("Todo") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = varchar("description", 255)
    val done = bool("done").default(false)

    override val primaryKey = PrimaryKey(id)
}

data class Todo(
    val id: Int,
    val title: String,
    val description: String,
    val done: Boolean,
) {
    companion object {
        fun new(title: String) : Todo{
            return Todo(0, title, "", false)
        }
    }
}
