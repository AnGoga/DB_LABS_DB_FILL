package global.command.fill_db.generator

import global.command.fill_db.entity.StudentGroup
import global.command.fill_db.entity.User
import global.command.fill_db.entity.enums.UserRole
import global.command.fill_db.repository.UserRepository
import io.github.serpro69.kfaker.Faker
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class UserDataGenerator(
    private val userRepository: UserRepository
) {
    private val faker = Faker()

    fun generate(count: Int, studentGroups: List<StudentGroup>) {
        println("Создание пользователей...")

        val batchSize = 500
        var created = 0

        while (created < count) {
            val batch = mutableListOf<User>()
            val batchCount = minOf(batchSize, count - created)

            repeat(batchCount) {
                val salt = generateSalt()
                val password = "password123"
                val passwordHash = hashPassword(password, salt)

                val role = when (Random.nextInt(100)) {
                    in 0..84 -> UserRole.Student
                    in 85..94 -> UserRole.Mentor
                    else -> UserRole.Admin
                }

                val studentGroup = if (role == UserRole.Student) {
                    studentGroups.random()
                } else null

                val user = User(
                    firstName = faker.name.firstName(),
                    lastName = faker.name.lastName(),
                    email = generateUniqueEmail(created + batch.size),
                    passwordHash = passwordHash,
                    salt = salt,
                    studentGroup = studentGroup,
                    role = role,
                    createdAt = OffsetDateTime.now().minusDays(Random.nextLong(365)),
                    updatedAt = OffsetDateTime.now()
                )
                batch.add(user)
            }

            userRepository.saveAll(batch)
            created += batchCount

            if (created % 1000 == 0) {
                println("Создано пользователей: $created")
            }
        }

        println("Создано пользователей: $created")
    }

    private fun generateSalt(): String {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }

    private fun hashPassword(password: String, salt: String): String {
        val bytes = "$password$salt".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun generateUniqueEmail(index: Int): String {
        val domains = listOf("example.com", "test.com", "mail.com", "email.com")
        return "user$index@${domains.random()}"
    }
}
