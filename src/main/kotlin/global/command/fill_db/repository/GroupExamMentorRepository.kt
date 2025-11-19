package global.command.fill_db.repository

import global.command.fill_db.entity.GroupExamMentor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupExamMentorRepository : JpaRepository<GroupExamMentor, Int>
