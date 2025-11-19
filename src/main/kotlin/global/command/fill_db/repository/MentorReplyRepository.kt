package global.command.fill_db.repository

import global.command.fill_db.entity.MentorReply
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MentorReplyRepository : JpaRepository<MentorReply, Int>
