package global.command.fill_db.generator

import global.command.fill_db.entity.GroupExam
import global.command.fill_db.entity.User
import global.command.fill_db.entity.UserTry
import global.command.fill_db.repository.UserTryRepository
import jakarta.persistence.EntityManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class UserTryDataGenerator(
    private val userTryRepository: UserTryRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val entityManager: EntityManager
) {

    @Transactional
    fun generate(groupExams: List<GroupExam>): List<UserTry> {
        println("Создание попыток прохождения экзаменов...")

        var totalTries = 0
        val batchSize = 1000

        groupExams.forEachIndexed { index, groupExam ->
            val groupId = groupExam.studentGroup.id!!
            val userIds = jdbcTemplate.queryForList(
                "SELECT user_id FROM group_users WHERE group_id = ?",
                Int::class.java,
                groupId
            )

            if (userIds.isEmpty()) return@forEachIndexed

            val participationRate = Random.nextDouble(0.3, 0.9)
            val participatingUsers = userIds.shuffled().take((userIds.size * participationRate).toInt())

            val allowMultipleTries = Random.nextDouble() < 0.15
            val maxTries = if (allowMultipleTries) Random.nextInt(2, 41) else 1

            val batch = mutableListOf<UserTry>()

            participatingUsers.forEach { userId ->
                val triesCount = if (allowMultipleTries) Random.nextInt(1, maxTries + 1) else 1

                repeat(triesCount) { _ ->
                    val startOffset = Random.nextLong(0, 100)
                    val duration = Random.nextLong(10, 240)

                    val startTry = groupExam.startTime.plusMinutes(startOffset)
                    val endTry = startTry.plusMinutes(duration)

                    val userTry = UserTry(
                        groupExam = groupExam,
                        user = entityManager.getReference(User::class.java, userId),
                        startTry = startTry,
                        endTry = endTry,
                        mark = null,
                        totalScore = null,
                        createdAt = startTry,
                        updatedAt = endTry
                    )
                    batch.add(userTry)

                    if (batch.size >= batchSize) {
                        userTryRepository.saveAll(batch)
                        entityManager.flush()
                        entityManager.clear()
                        totalTries += batch.size
                        batch.clear()
                        println("Создано попыток: $totalTries")
                    }
                }
            }

            if (batch.isNotEmpty()) {
                userTryRepository.saveAll(batch)
                entityManager.flush()
                entityManager.clear()
                totalTries += batch.size
            }

            if ((index + 1) % 100 == 0) {
                println("Обработано group_exam: ${index + 1}, создано попыток: $totalTries")
            }
        }

        println("Всего создано попыток: $totalTries")
        return emptyList()
    }
}
