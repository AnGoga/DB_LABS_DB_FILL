package global.command.fill_db.generator

import global.command.fill_db.entity.*
import global.command.fill_db.entity.enums.QuestionType
import global.command.fill_db.repository.*
import io.github.serpro69.kfaker.Faker
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class ExamDataGenerator(
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository,
    private val questionVariantRepository: QuestionVariantRepository,
    private val questionVariantCorrectRepository: QuestionVariantCorrectRepository,
    private val questionCorrectFreeAnsRepository: QuestionCorrectFreeAnsRepository
) {
    private val faker = Faker()

    fun generate() {
        println("Создание экзаменов с вопросами...")

        val examCount = Random.nextInt(80, 120)
        val subjects = listOf(
            "Математический анализ", "Линейная алгебра", "Дискретная математика",
            "Математическая статистика", "Теория вероятностей", "Дифференциальные уравнения",
            "Программирование на Java", "Программирование на Python", "Программирование на C++",
            "Программирование на JavaScript", "Программирование на Kotlin", "Программирование на Go",
            "Базы данных", "Проектирование БД", "SQL и NoSQL", "Администрирование БД",
            "Алгоритмы и структуры данных", "Теория алгоритмов", "Анализ алгоритмов",
            "Веб-разработка", "Frontend разработка", "Backend разработка", "Fullstack разработка",
            "Операционные системы", "Архитектура компьютеров", "Компьютерные сети",
            "Информационная безопасность", "Криптография", "Защита информации",
            "Искусственный интеллект", "Машинное обучение", "Нейронные сети", "Deep Learning",
            "Разработка мобильных приложений", "Android разработка", "iOS разработка",
            "Тестирование ПО", "Автоматизация тестирования", "QA процессы",
            "Управление проектами", "Agile и Scrum", "DevOps", "CI/CD",
            "Облачные технологии", "Docker и Kubernetes", "Микросервисная архитектура",
            "Функциональное программирование", "ООП", "Паттерны проектирования",
            "Компиляторы", "Теория формальных языков", "Системное программирование"
        )

        repeat(examCount) { index ->
            val subject = subjects.random()
            val examType = listOf("Экзамен", "Тест", "Контрольная работа", "Зачет", "Коллоквиум").random()

            val exam = examRepository.save(
                Exam(
                    title = "$examType: $subject ${if (Random.nextBoolean()) "- ${faker.educator.courseName()}" else ""}",
                    description = generateExamDescription(),
                    createdAt = OffsetDateTime.now().minusDays(Random.nextLong(365)),
                    updatedAt = OffsetDateTime.now()
                )
            )

            createQuestionsForExam(exam)

            if ((index + 1) % 10 == 0) {
                println("Создано экзаменов: ${index + 1}")
            }
        }

        println("Создано экзаменов: $examCount")
    }

    private fun generateExamDescription(): String {
        val descriptions = listOf(
            faker.lorem.words(),
            "${faker.company.buzzwords()} ${faker.lorem.words()}",
            faker.educator.courseName(),
            "${faker.educator.subject()} - ${faker.lorem.words()}"
        )
        return descriptions.random()
    }

    private fun createQuestionsForExam(exam: Exam) {
        val questionCount = Random.nextInt(50, 150)

        repeat(questionCount) {
            val rand = Random.nextDouble()
            val questionType = when {
                rand < 0.60 -> QuestionType.ChooseAns
                rand < 0.85 -> QuestionType.FreeAns
                else -> QuestionType.MentorFreeAns
            }

            when (questionType) {
                QuestionType.ChooseAns -> createChoiceQuestion(exam)
                QuestionType.FreeAns -> createFreeAnswerQuestion(exam)
                QuestionType.MentorFreeAns -> createMentorFreeAnswerQuestion(exam)
            }
        }
    }

    private fun createChoiceQuestion(exam: Exam) {
        val question = questionRepository.save(
            Question(
                exam = exam,
                questionText = generateQuestionText(),
                type = QuestionType.ChooseAns,
                score = Random.nextDouble(0.5, 10.0),
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        )

        val variantCount = Random.nextInt(3, 8)
        val variants = mutableListOf<QuestionVariant>()

        repeat(variantCount) {
            val variant = questionVariantRepository.save(
                QuestionVariant(
                    question = question,
                    variantText = generateVariantText()
                )
            )
            variants.add(variant)
        }

        val correctCount = Random.nextInt(1, minOf(3, variants.size))
        variants.shuffled().take(correctCount).forEach { variant ->
            questionVariantCorrectRepository.save(
                QuestionVariantCorrect(
                    question = question,
                    questionVariant = variant,
                    explanation = if (Random.nextBoolean()) generateExplanation() else null
                )
            )
        }
    }

    private fun createFreeAnswerQuestion(exam: Exam) {
        val question = questionRepository.save(
            Question(
                exam = exam,
                questionText = generateQuestionText(),
                type = QuestionType.FreeAns,
                score = Random.nextDouble(1.0, 15.0),
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        )

        questionCorrectFreeAnsRepository.save(
            QuestionCorrectFreeAns(
                question = question,
                correctText = generateFreeAnswerText(),
                explanation = if (Random.nextBoolean()) generateExplanation() else null
            )
        )
    }

    private fun generateQuestionText(): String {
        val templates = listOf(
            "${faker.company.buzzwords()} ${faker.lorem.words()}?",
            "Что такое ${faker.hacker.verb()} ${faker.hacker.noun()}?",
            "Как ${faker.hacker.verb()} ${faker.hacker.adjective()} ${faker.hacker.noun()}?",
            "Какой метод используется для ${faker.hacker.verb()} ${faker.hacker.noun()}?",
            "Определите ${faker.hacker.adjective()} ${faker.hacker.noun()}",
            "${faker.verbs.base()} ${faker.lorem.words()}",
            "Объясните концепцию ${faker.hacker.noun()}",
            "Приведите пример ${faker.hacker.adjective()} ${faker.hacker.noun()}",
            "Сравните ${faker.hacker.noun()} и ${faker.hacker.noun()}",
            "Вычислите ${faker.lorem.words()}"
        )
        return templates.random()
    }

    private fun generateVariantText(): String {
        val templates = listOf(
            faker.hacker.verb() + " " + faker.hacker.noun(),
            faker.hacker.adjective() + " " + faker.hacker.noun(),
            faker.company.buzzwords(),
            faker.lorem.words(),
            faker.verbs.base() + " " + faker.lorem.words(),
            Random.nextInt(0, 1000).toString(),
            Random.nextDouble(0.0, 100.0).toString()
        )
        return templates.random()
    }

    private fun generateFreeAnswerText(): String {
        return buildString {
            repeat(Random.nextInt(3, 10)) {
                append(faker.lorem.words())
                append(" ")
            }
        }.trim()
    }

    private fun createMentorFreeAnswerQuestion(exam: Exam) {
        val question = questionRepository.save(
            Question(
                exam = exam,
                questionText = generateMentorQuestionText(),
                type = QuestionType.MentorFreeAns,
                score = Random.nextDouble(5.0, 20.0),
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        )

        questionCorrectFreeAnsRepository.save(
            QuestionCorrectFreeAns(
                question = question,
                correctText = generateMentorAnswerText(),
                explanation = generateExplanation()
            )
        )
    }

    private fun generateMentorQuestionText(): String {
        val templates = listOf(
            "Опишите подробно процесс ${faker.hacker.verb()} ${faker.hacker.noun()}",
            "Разработайте решение для ${faker.hacker.verb()} ${faker.hacker.adjective()} ${faker.hacker.noun()}",
            "Проанализируйте и объясните ${faker.hacker.adjective()} ${faker.hacker.noun()}",
            "Создайте детальный план ${faker.hacker.verb()} ${faker.hacker.noun()}",
            "Обоснуйте выбор методов для ${faker.hacker.verb()} ${faker.hacker.noun()}",
            "Сравните различные подходы к ${faker.hacker.verb()} ${faker.hacker.noun()}",
            "Предложите оптимизацию для ${faker.hacker.adjective()} ${faker.hacker.noun()}"
        )
        return templates.random()
    }

    private fun generateMentorAnswerText(): String {
        return buildString {
            repeat(Random.nextInt(10, 20)) {
                append(faker.lorem.words())
                append(" ")
            }
        }.trim()
    }

    private fun generateExplanation(): String {
        return buildString {
            repeat(Random.nextInt(2, 6)) {
                append(faker.lorem.words())
                append(" ")
            }
        }.trim()
    }
}
