package global.command.fill_db.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "mentor_reply")
data class MentorReply(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(nullable = false)
    val score: Double,

    @Column(columnDefinition = "TEXT")
    val comment: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_exam_mentor_id", nullable = false)
    val groupExamMentor: GroupExamMentor,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_try_question_answer_id", nullable = false)
    val userTryQuestionAnswer: UserTryQuestionAnswer,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
