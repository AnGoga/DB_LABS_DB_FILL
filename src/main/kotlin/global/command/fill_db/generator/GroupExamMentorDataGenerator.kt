package global.command.fill_db.generator

import global.command.fill_db.entity.GroupExamMentor
import global.command.fill_db.entity.User
import global.command.fill_db.repository.GroupExamMentorRepository
import global.command.fill_db.repository.GroupExamRepository
import jakarta.persistence.EntityManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Component
class GroupExamMentorDataGenerator(
    private val groupExamMentorRepository: GroupExamMentorRepository,
    private val groupExamRepository: GroupExamRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val entityManager: EntityManager
) {

    @Transactional
    fun generate(): List<GroupExamMentor> {
        println("Создание назначений менторов на проверку экзаменов...")

        val groupExams = groupExamRepository.findAll()
        val mentorIds = jdbcTemplate.queryForList(
            "SELECT id FROM users WHERE role = 'Mentor'",
            Int::class.java
        )

        if (mentorIds.isEmpty()) {
            println("Менторов не найдено в системе")
            return emptyList()
        }

        val assignmentRate = Random.nextDouble(0.5, 0.8)
        val groupExamsToAssign = groupExams.shuffled().take((groupExams.size * assignmentRate).toInt())

        val assignments = mutableListOf<GroupExamMentor>()
        val batchSize = 500

        groupExamsToAssign.forEach { groupExam ->
            val mentorCount = Random.nextInt(1, 4)
            val assignedMentors = mentorIds.shuffled().take(mentorCount)

            assignedMentors.forEach { mentorId ->
                val assignment = GroupExamMentor(
                    groupExam = groupExam,
                    user = entityManager.getReference(User::class.java, mentorId)
                )
                assignments.add(assignment)

                if (assignments.size % batchSize == 0) {
                    groupExamMentorRepository.saveAll(assignments.takeLast(batchSize))
                    entityManager.flush()
                    entityManager.clear()
                    println("Создано назначений: ${assignments.size}")
                }
            }
        }

        if (assignments.size % batchSize != 0) {
            groupExamMentorRepository.saveAll(assignments.takeLast(assignments.size % batchSize))
            entityManager.flush()
        }

        println("Создано назначений менторов: ${assignments.size}")
        return groupExamMentorRepository.findAll()
    }
}
