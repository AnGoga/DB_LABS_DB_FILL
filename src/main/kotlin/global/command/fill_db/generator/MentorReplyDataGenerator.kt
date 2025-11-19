package global.command.fill_db.generator

import global.command.fill_db.entity.GroupExamMentor
import global.command.fill_db.entity.MentorReply
import global.command.fill_db.entity.UserTryQuestionAnswer
import global.command.fill_db.repository.GroupExamMentorRepository
import global.command.fill_db.repository.MentorReplyRepository
import io.github.serpro69.kfaker.Faker
import jakarta.persistence.EntityManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class MentorReplyDataGenerator(
    private val mentorReplyRepository: MentorReplyRepository,
    private val groupExamMentorRepository: GroupExamMentorRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val entityManager: EntityManager
) {
    private val faker = Faker()

    @Transactional
    fun generate() {
        println("Создание менторских проверок...")

        val groupExamIdsWithAnswers = jdbcTemplate.queryForList(
            """
            SELECT DISTINCT ut.group_exam_id
            FROM user_try_question_answer utqa
            JOIN user_try ut ON utqa.user_try_id = ut.id
            JOIN question q ON utqa.question_id = q.id
            WHERE q.type = 'MentorFreeAns'
            """,
            Int::class.java
        ).toSet()

        println("Найдено group_exam с MentorFreeAns ответами: ${groupExamIdsWithAnswers.size}")

        if (groupExamIdsWithAnswers.isEmpty()) {
            println("Нет ответов типа MentorFreeAns для проверки")
            return
        }

        val groupExamMentors = groupExamMentorRepository.findAll()
            .filter { it.groupExam.id in groupExamIdsWithAnswers }

        println("Назначений менторов для обработки: ${groupExamMentors.size}")

        val groupExamToMentors = groupExamMentors.groupBy { it.groupExam.id!! }
        val processedGroupExams = mutableSetOf<Int>()

        var totalReplies = 0
        var processedCount = 0
        val batchSize = 1000
        val replies = mutableListOf<MentorReply>()

        groupExamToMentors.forEach { (groupExamId, mentorsForExam) ->
            val mentorFreeAnswers = jdbcTemplate.query(
                """
                SELECT utqa.id, utqa.answered_at, q.score
                FROM user_try_question_answer utqa
                JOIN user_try ut ON utqa.user_try_id = ut.id
                JOIN question q ON utqa.question_id = q.id
                WHERE ut.group_exam_id = ? AND q.type = 'MentorFreeAns'
                """,
                { rs, _ ->
                    Triple(
                        rs.getInt("id"),
                        rs.getObject("answered_at", OffsetDateTime::class.java),
                        rs.getDouble("score")
                    )
                },
                groupExamId
            )

            if (mentorFreeAnswers.isEmpty()) return@forEach
            processedCount++
            processedGroupExams.add(groupExamId)

            val checkRate = when (Random.nextDouble()) {
                in 0.0..0.3 -> 1.0
                in 0.3..0.6 -> Random.nextDouble(0.7, 1.0)
                else -> Random.nextDouble(0.3, 0.7)
            }

            val answersToCheck = mentorFreeAnswers.shuffled().take((mentorFreeAnswers.size * checkRate).toInt())

            val answersPerMentor = answersToCheck.chunked((answersToCheck.size + mentorsForExam.size - 1) / mentorsForExam.size)

            mentorsForExam.forEachIndexed { mentorIndex, groupExamMentor ->
                val mentorAnswers = answersPerMentor.getOrNull(mentorIndex) ?: return@forEachIndexed

                mentorAnswers.forEach { (answerId, answeredAt, maxScore) ->
                    val earnedScore = when (Random.nextDouble()) {
                        in 0.0..0.6 -> maxScore
                        in 0.6..0.8 -> Random.nextDouble(maxScore * 0.5, maxScore)
                        else -> Random.nextDouble(0.0, maxScore * 0.5)
                    }

                    val comment = generateComment(earnedScore, maxScore)

                    val reply = MentorReply(
                        score = earnedScore,
                        comment = comment,
                        groupExamMentor = groupExamMentor,
                        userTryQuestionAnswer = entityManager.getReference(UserTryQuestionAnswer::class.java, answerId),
                        createdAt = answeredAt.plusHours(Random.nextLong(1, 72)),
                        updatedAt = OffsetDateTime.now()
                    )
                    replies.add(reply)

                    if (replies.size >= batchSize) {
                        mentorReplyRepository.saveAll(replies)
                        entityManager.flush()
                        entityManager.clear()
                        totalReplies += replies.size
                        replies.clear()
                    }
                }
            }

            if (processedCount % 50 == 0 || processedCount == groupExamToMentors.size) {
                println("Обработано group_exam: $processedCount/${groupExamToMentors.size}, создано проверок: $totalReplies")
            }
        }

        if (replies.isNotEmpty()) {
            mentorReplyRepository.saveAll(replies)
            entityManager.flush()
            entityManager.clear()
            totalReplies += replies.size
        }

        println("Обработано group_exam с ответами: $processedCount из ${groupExamToMentors.size}")
        println("Создано менторских проверок: $totalReplies")
    }

    private fun generateComment(earnedScore: Double, maxScore: Double): String? {
        if (Random.nextDouble() < 0.3) return null

        val percentage = (earnedScore / maxScore) * 100

        val templates = when {
            percentage >= 90 -> listOf(
                "Отличная работа! ${faker.lorem.words()}",
                "Превосходный ответ, все критерии выполнены. ${faker.lorem.words()}",
                "Глубокое понимание темы. ${faker.lorem.words()}"
            )
            percentage >= 70 -> listOf(
                "Хорошая работа, но есть недочеты: ${faker.lorem.words()}",
                "В целом правильно, стоит доработать ${faker.lorem.words()}",
                "Неплохой результат. ${faker.lorem.words()}"
            )
            percentage >= 50 -> listOf(
                "Требуется доработка. ${faker.lorem.words()}",
                "Ответ неполный, не хватает ${faker.lorem.words()}",
                "Необходимо улучшить ${faker.lorem.words()}"
            )
            else -> listOf(
                "Ответ не соответствует требованиям. ${faker.lorem.words()}",
                "Необходимо полностью переделать. ${faker.lorem.words()}",
                "Тема не раскрыта. ${faker.lorem.words()}"
            )
        }

        return templates.random()
    }
}
