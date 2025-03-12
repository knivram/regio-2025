import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import me.knivram.repository.TodoTable
import me.knivram.ui.screen.Home
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@Composable
@Preview
fun App() {
    Database.connect(
        url = "jdbc:mysql://localhost:3306/regio",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "password"
    )
    transaction {
        SchemaUtils.create(TodoTable)
    }

    MaterialTheme {
        Navigator(Home())
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Todolist",
    ) {
        App()
    }
}
