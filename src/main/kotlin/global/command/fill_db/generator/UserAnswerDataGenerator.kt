package global.command.fill_db.generator

import global.command.fill_db.entity.*
import global.command.fill_db.entity.enums.QuestionType
import global.command.fill_db.repository.*
import io.github.serpro69.kfaker.Faker
import jakarta.persistence.EntityManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class UserAnswerDataGenerator(
    private val userTryQuestionAnswerRepository: UserTryQuestionAnswerRepository,
    private val userFreeAnsRepository: UserFreeAnsRepository,
    private val userChooseAnsRepository: UserChooseAnsRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val entityManager: EntityManager
) {
    private val faker = Faker()

    @Transactional
    fun generate() {
        println("Создание ответов на вопросы...")

        val tryIds = jdbcTemplate.queryForList(
            "SELECT id FROM user_try ORDER BY id LIMIT 5000",
            Int::class.java
        )

        println("Предзагрузка данных о вопросах и вариантах...")
        val examQuestions = preloadExamQuestions()
        val questionVariants = preloadQuestionVariants()
        val correctVariants = preloadCorrectVariants()

        var answeredCount = 0
        val batchSize = 1000
        val answerBatch = mutableListOf<UserTryQuestionAnswer>()
        val freeAnsBatch = mutableListOf<Pair<Int, UserFreeAns>>()
        val chooseAnsBatch = mutableListOf<Pair<Int, UserChooseAns>>()
        val scoreUpdates = mutableListOf<Triple<Int, Double, Double>>()

        tryIds.forEachIndexed { index, tryId ->
            val tryData = jdbcTemplate.query(
                """
                SELECT ut.id, ut.group_exam_id, ut.start_try, ge.exam_id
                FROM user_try ut
                JOIN group_exam ge ON ut.group_exam_id = ge.id
                WHERE ut.id = ?
                """,
                { rs, _ ->
                    Triple(
                        rs.getInt("exam_id"),
                        rs.getObject("start_try", OffsetDateTime::class.java),
                        rs.getInt("group_exam_id")
                    )
                },
                tryId
            ).firstOrNull() ?: return@forEachIndexed

            val (examId, startTry, groupExamId) = tryData

            val questions = examQuestions[examId] ?: emptyList()
            if (questions.isEmpty()) return@forEachIndexed

            val answerRate = Random.nextDouble(0.7, 1.0)
            val questionsToAnswer = questions.shuffled().take((questions.size * answerRate).toInt())

            var totalScore = 0.0
            var earnedScore = 0.0

            questionsToAnswer.forEach { (questionId, questionType, score) ->
                totalScore += score

                val answeredAt = startTry.plusMinutes(Random.nextLong(1, 180))

                val answer = UserTryQuestionAnswer(
                    userTry = entityManager.getReference(UserTry::class.java, tryId),
                    question = entityManager.getReference(Question::class.java, questionId),
                    answeredAt = answeredAt
                )
                answerBatch.add(answer)

                val tempAnswerId = -(answerBatch.size)

                when (questionType) {
                    QuestionType.ChooseAns -> {
                        val correctScore = createChooseAnswerBatch(
                            tempAnswerId, questionId, score,
                            questionVariants, correctVariants, chooseAnsBatch
                        )
                        earnedScore += correctScore
                    }
                    QuestionType.FreeAns -> {
                        val correctScore = createFreeAnswerBatch(tempAnswerId, score, freeAnsBatch)
                        earnedScore += correctScore
                    }
                    QuestionType.MentorFreeAns -> {
                        createMentorFreeAnswerBatch(tempAnswerId, freeAnsBatch)
                    }
                }

                answeredCount++
            }

            if (questionsToAnswer.any { it.second != QuestionType.MentorFreeAns }) {
                val mark = if (totalScore > 0) (earnedScore / totalScore) * 100 else 0.0
                scoreUpdates.add(Triple(tryId, earnedScore, mark))
            }

            if (answerBatch.size >= batchSize) {
                flushBatches(answerBatch, freeAnsBatch, chooseAnsBatch, scoreUpdates)
                println("Обработано попыток: ${index + 1}, создано ответов: $answeredCount")
            }
        }

        if (answerBatch.isNotEmpty()) {
            flushBatches(answerBatch, freeAnsBatch, chooseAnsBatch, scoreUpdates)
        }

        println("Создано ответов: $answeredCount")
    }

    private fun preloadExamQuestions(): Map<Int, List<Triple<Int, QuestionType, Double>>> {
        val result = mutableMapOf<Int, MutableList<Triple<Int, QuestionType, Double>>>()

        jdbcTemplate.query(
            "SELECT id, exam_id, type, score FROM question"
        ) { rs ->
            val examId = rs.getInt("exam_id")
            val questionId = rs.getInt("id")
            val type = QuestionType.valueOf(rs.getString("type"))
            val score = rs.getDouble("score")

            result.getOrPut(examId) { mutableListOf() }.add(Triple(questionId, type, score))
        }

        return result
    }

    private fun preloadQuestionVariants(): Map<Int, List<Int>> {
        val result = mutableMapOf<Int, MutableList<Int>>()

        jdbcTemplate.query(
            "SELECT id, question_id FROM question_variant"
        ) { rs ->
            val questionId = rs.getInt("question_id")
            val variantId = rs.getInt("id")
            result.getOrPut(questionId) { mutableListOf() }.add(variantId)
        }

        return result
    }

    private fun preloadCorrectVariants(): Map<Int, Set<Int>> {
        val result = mutableMapOf<Int, MutableSet<Int>>()

        jdbcTemplate.query(
            "SELECT question_id, question_variant_id FROM question_variant_correct"
        ) { rs ->
            val questionId = rs.getInt("question_id")
            val variantId = rs.getInt("question_variant_id")
            result.getOrPut(questionId) { mutableSetOf() }.add(variantId)
        }

        return result
    }

    private fun flushBatches(
        answerBatch: MutableList<UserTryQuestionAnswer>,
        freeAnsBatch: MutableList<Pair<Int, UserFreeAns>>,
        chooseAnsBatch: MutableList<Pair<Int, UserChooseAns>>,
        scoreUpdates: MutableList<Triple<Int, Double, Double>>
    ) {
        val savedAnswers = userTryQuestionAnswerRepository.saveAll(answerBatch)
        entityManager.flush()

        val answerIdMap = savedAnswers.mapIndexed { index, answer ->
            -(index + 1) to answer
        }.toMap()

        val freeAnsToSave = freeAnsBatch.map { (tempId, freeAns) ->
            UserFreeAns(
                userTryQuestionAnswer = answerIdMap[tempId]!!,
                ansText = freeAns.ansText,
                filePath = freeAns.filePath
            )
        }
        userFreeAnsRepository.saveAll(freeAnsToSave)

        val chooseAnsToSave = chooseAnsBatch.map { (tempId, chooseAns) ->
            UserChooseAns(
                userTryQuestionAnswer = answerIdMap[tempId]!!,
                questionVariant = chooseAns.questionVariant
            )
        }
        userChooseAnsRepository.saveAll(chooseAnsToSave)

        entityManager.flush()
        entityManager.clear()

        scoreUpdates.forEach { (tryId, earnedScore, mark) ->
            jdbcTemplate.update(
                "UPDATE user_try SET total_score = ?, mark = ? WHERE id = ?",
                earnedScore, mark, tryId
            )
        }

        answerBatch.clear()
        freeAnsBatch.clear()
        chooseAnsBatch.clear()
        scoreUpdates.clear()
    }

    private fun createChooseAnswerBatch(
        tempAnswerId: Int,
        questionId: Int,
        maxScore: Double,
        questionVariants: Map<Int, List<Int>>,
        correctVariants: Map<Int, Set<Int>>,
        chooseAnsBatch: MutableList<Pair<Int, UserChooseAns>>
    ): Double {
        val variants = questionVariants[questionId] ?: return 0.0
        if (variants.isEmpty()) return 0.0

        val correct = correctVariants[questionId] ?: emptySet()

        val answerCount = Random.nextInt(1, minOf(3, variants.size + 1))
        val selectedVariants = variants.shuffled().take(answerCount)

        selectedVariants.forEach { variantId ->
            val dummyAnswer = UserTryQuestionAnswer(
                userTry = entityManager.getReference(UserTry::class.java, 1),
                question = entityManager.getReference(Question::class.java, 1),
                answeredAt = OffsetDateTime.now()
            )
            chooseAnsBatch.add(
                tempAnswerId to UserChooseAns(
                    userTryQuestionAnswer = dummyAnswer,
                    questionVariant = entityManager.getReference(QuestionVariant::class.java, variantId)
                )
            )
        }

        val isCorrect = selectedVariants.all { it in correct } && selectedVariants.size == correct.size
        return if (isCorrect) maxScore else 0.0
    }

    private fun createFreeAnswerBatch(
        tempAnswerId: Int,
        maxScore: Double,
        freeAnsBatch: MutableList<Pair<Int, UserFreeAns>>
    ): Double {
        val answerText = buildString {
            repeat(Random.nextInt(5, 20)) {
                append(faker.lorem.words())
                append(" ")
            }
        }.trim()

        val dummyAnswer = UserTryQuestionAnswer(
            userTry = entityManager.getReference(UserTry::class.java, 1),
            question = entityManager.getReference(Question::class.java, 1),
            answeredAt = OffsetDateTime.now()
        )

        freeAnsBatch.add(
            tempAnswerId to UserFreeAns(
                userTryQuestionAnswer = dummyAnswer,
                ansText = answerText,
                filePath = if (Random.nextDouble() < 0.1) "/uploads/answer_$tempAnswerId.pdf" else null
            )
        )

        return if (Random.nextDouble() < 0.6) maxScore else Random.nextDouble(0.0, maxScore)
    }

    private fun createMentorFreeAnswerBatch(
        tempAnswerId: Int,
        freeAnsBatch: MutableList<Pair<Int, UserFreeAns>>
    ) {
        val answerText = buildString {
            repeat(Random.nextInt(10, 30)) {
                append(faker.lorem.words())
                append(" ")
            }
        }.trim()

        val dummyAnswer = UserTryQuestionAnswer(
            userTry = entityManager.getReference(UserTry::class.java, 1),
            question = entityManager.getReference(Question::class.java, 1),
            answeredAt = OffsetDateTime.now()
        )

        freeAnsBatch.add(
            tempAnswerId to UserFreeAns(
                userTryQuestionAnswer = dummyAnswer,
                ansText = answerText,
                filePath = if (Random.nextDouble() < 0.3) "/uploads/mentor_answer_$tempAnswerId.pdf" else null
            )
        )
    }
}
