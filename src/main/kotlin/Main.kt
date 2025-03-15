import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    var isUsingH2Mem = false
    try {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/regio",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "password"
        )
        
        transaction {
            SchemaUtils.create(TodoTable)
        }
    } catch (e: Exception) {
        println("MySQL connection failed: ${e.message}. Using H2 in-memory database instead.")
        
        // Connect to H2 in-memory database as fallback
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        
        transaction {
            SchemaUtils.create(TodoTable)
        }

        isUsingH2Mem = true
    }

    MaterialTheme {
        Column {
            if (isUsingH2Mem) {
                Text(text = "MySQL connection failed. Using H2 in-memory database instead.", color = MaterialTheme.colors.error)
            }
            Navigator(Home())
        }
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
