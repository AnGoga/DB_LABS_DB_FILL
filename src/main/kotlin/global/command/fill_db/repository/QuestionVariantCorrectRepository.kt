package global.command.fill_db.repository

import global.command.fill_db.entity.QuestionVariantCorrect
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuestionVariantCorrectRepository : JpaRepository<QuestionVariantCorrect, Int>
