package global.command.fill_db.generator

import global.command.fill_db.entity.StudentGroup
import global.command.fill_db.repository.StudentGroupRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import kotlin.random.Random

@Component
class StudentGroupDataGenerator(
    private val studentGroupRepository: StudentGroupRepository
) {
    fun generate(count: Int): List<StudentGroup> {
        println("Создание студенческих групп...")

        val groups = mutableListOf<StudentGroup>()
        val faculties = listOf("ИИКС", "ИЯФИТ", "ИМО", "ЛаПЛАЗ", "ФИИТ", "ФЭУ", "ФМФ", "ФТФ", "ФХФ")
        val courses = listOf(1, 2, 3, 4)

        repeat(count) {
            val faculty = faculties.random()
            val course = courses.random()
            val groupNumber = Random.nextInt(1, 20)

            val group = StudentGroup(
                name = "$faculty-$course-$groupNumber",
                createdAt = OffsetDateTime.now().minusDays(Random.nextLong(365)),
                updatedAt = OffsetDateTime.now()
            )
            groups.add(group)
        }

        val saved = studentGroupRepository.saveAll(groups)
        println("Создано групп: ${saved.size}")
        return saved
    }
}
