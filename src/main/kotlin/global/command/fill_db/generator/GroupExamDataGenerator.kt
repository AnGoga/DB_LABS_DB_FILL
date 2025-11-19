package global.command.fill_db.generator

import global.command.fill_db.entity.GroupExam
import global.command.fill_db.repository.ExamRepository
import global.command.fill_db.repository.GroupExamRepository
import global.command.fill_db.repository.StudentGroupRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class GroupExamDataGenerator(
    private val groupExamRepository: GroupExamRepository,
    private val examRepository: ExamRepository,
    private val studentGroupRepository: StudentGroupRepository
) {

    fun generate(minCount: Int): List<GroupExam> {
        println("Создание назначений экзаменов группам...")

        val exams = examRepository.findAll()
        val groups = studentGroupRepository.findAll()

        val groupExams = mutableListOf<GroupExam>()
        val batchSize = 500

        while (groupExams.size < minCount) {
            val exam = exams.random()
            val group = groups.random()

            val now = OffsetDateTime.now()
            val startOffset = Random.nextLong(1, 365)
            val durationHours = Random.nextLong(1, 5)

            val groupExam = GroupExam(
                exam = exam,
                studentGroup = group,
                startTime = now.minusDays(startOffset),
                endTime = now.minusDays(startOffset).plusHours(durationHours),
                createdAt = now.minusDays(startOffset + 1),
                updatedAt = now
            )
            groupExams.add(groupExam)

            if (groupExams.size % batchSize == 0) {
                groupExamRepository.saveAll(groupExams.takeLast(batchSize))
                println("Создано назначений: ${groupExams.size}")
            }
        }

        if (groupExams.size % batchSize != 0) {
            groupExamRepository.saveAll(groupExams.takeLast(groupExams.size % batchSize))
        }

        println("Создано назначений экзаменов: ${groupExams.size}")
        return groupExamRepository.findAll()
    }
}
