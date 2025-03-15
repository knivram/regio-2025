import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import me.knivram.repository.ExerciseRepository
import me.knivram.repository.TemplateTable
import me.knivram.repository.TemplateExerciseTable
import me.knivram.repository.WorkoutTable
import me.knivram.repository.WorkoutExerciseTable
import me.knivram.repository.WorkoutSetTable
import me.knivram.ui.screen.Home
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
@Preview
fun App() {
    var isUsingH2Mem = false
    try {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/regio",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "password"
        )
        transaction {
            SchemaUtils.create(ExerciseRepository.table)
            SchemaUtils.create(TemplateTable)
            SchemaUtils.create(TemplateExerciseTable)
            SchemaUtils.create(WorkoutTable)
            SchemaUtils.create(WorkoutExerciseTable)
            SchemaUtils.create(WorkoutSetTable)
        }
    } catch (e: Exception) {
        println("MySQL connection failed: ${e.message}. Using H2 in-memory database instead.")
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.create(ExerciseRepository.table)
            SchemaUtils.create(TemplateTable)
            SchemaUtils.create(TemplateExerciseTable)
            SchemaUtils.create(WorkoutTable)
            SchemaUtils.create(WorkoutExerciseTable)
            SchemaUtils.create(WorkoutSetTable)
        }
        isUsingH2Mem = true
    }

    transaction {
        if (ExerciseRepository.getAll().isEmpty()) {
            val resourceStream = object {}.javaClass.classLoader.getResourceAsStream("exercises.json")
            if (resourceStream != null) {
                val jsonText = resourceStream.bufferedReader().readText()
                ExerciseRepository.importFromJson(jsonText)
            }
        }
    }

    MaterialTheme {
        Navigator(Home())
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AthliTrack",
    ) {
        App()
    }
}
