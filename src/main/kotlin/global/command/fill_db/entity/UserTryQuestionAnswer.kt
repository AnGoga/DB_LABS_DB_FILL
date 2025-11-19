package global.command.fill_db.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "user_try_question_answer")
data class UserTryQuestionAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_try_id", nullable = false)
    val userTry: UserTry,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @Column(name = "answered_at", nullable = false)
    val answeredAt: OffsetDateTime = OffsetDateTime.now()
)
