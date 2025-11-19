package global.command.fill_db.repository

import global.command.fill_db.entity.UserTry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserTryRepository : JpaRepository<UserTry, Int>
