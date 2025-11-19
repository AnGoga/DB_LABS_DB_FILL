package global.command.fill_db.generator

import global.command.fill_db.entity.StudentGroup
import global.command.fill_db.repository.UserRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class GroupUsersDataGenerator(
    private val jdbcTemplate: JdbcTemplate,
    private val userRepository: UserRepository
) {

    @Transactional
    fun generate(studentGroups: List<StudentGroup>) {
        println("Создание связей пользователей с группами...")

        val userIds = jdbcTemplate.queryForList("SELECT id FROM users", Int::class.java)
        val batchSize = 1000
        val relations = mutableListOf<Pair<Int, Int>>()

        userIds.forEach { userId ->
            val groupCount = Random.nextInt(1, 6)
            val selectedGroups = studentGroups.shuffled().take(groupCount)

            selectedGroups.forEach { group ->
                relations.add(group.id!! to userId)
            }
        }

        var created = 0
        while (created < relations.size) {
            val batch = relations.drop(created).take(batchSize)

            val sql = "INSERT INTO group_users (group_id, user_id, created_at) VALUES (?, ?, ?)"
            jdbcTemplate.batchUpdate(sql, batch, batch.size) { ps, relation ->
                ps.setInt(1, relation.first)
                ps.setInt(2, relation.second)
                ps.setObject(3, OffsetDateTime.now())
            }

            created += batch.size

            if (created % 10000 == 0) {
                println("Создано связей: $created")
            }
        }

        println("Создано связей: ${relations.size}")
    }
}
