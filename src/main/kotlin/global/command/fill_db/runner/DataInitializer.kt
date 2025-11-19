package global.command.fill_db.runner

import global.command.fill_db.generator.*
import global.command.fill_db.repository.UserRepository
import global.command.fill_db.repository.UserTryRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val userTryRepository: UserTryRepository,

    private val studentGroupDataGenerator: StudentGroupDataGenerator,
    private val userDataGenerator: UserDataGenerator,
    private val groupUsersDataGenerator: GroupUsersDataGenerator,
    private val examDataGenerator: ExamDataGenerator,
    private val groupExamDataGenerator: GroupExamDataGenerator,
    private val userTryDataGenerator: UserTryDataGenerator,
    private val userAnswerDataGenerator: UserAnswerDataGenerator,
    private val groupExamMentorDataGenerator: GroupExamMentorDataGenerator,
    private val mentorReplyDataGenerator: MentorReplyDataGenerator
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        println("=== Начало заполнения базы данных ===")
        val startTime = System.currentTimeMillis()

        userAndGroups()
        examAndTrys()


        val endTime = System.currentTimeMillis()
        println("=== Заполнение завершено за ${(endTime - startTime) / 1000} секунд ===")
    }

    fun userAndGroups() {
        if (userRepository.count() > 0) {
            return
        }
        val studentGroups = studentGroupDataGenerator.generate(500)
        userDataGenerator.generate(10_000, studentGroups)
        groupUsersDataGenerator.generate(studentGroups)
        examDataGenerator.generate()
    }

    fun examAndTrys() {
        if (userTryRepository.count() > 0) {
            return
        }
        val groupExams = groupExamDataGenerator.generate(5000)
        userTryDataGenerator.generate(groupExams)
        userAnswerDataGenerator.generate()
        groupExamMentorDataGenerator.generate()
        mentorReplyDataGenerator.generate()
    }
}
