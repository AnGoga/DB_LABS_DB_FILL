package global.command.fill_db

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FillDbApplication

fun main(args: Array<String>) {
    runApplication<FillDbApplication>(*args)
}
