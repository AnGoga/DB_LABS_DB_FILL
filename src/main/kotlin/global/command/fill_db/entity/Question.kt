package global.command.fill_db.entity

import global.command.fill_db.entity.enums.QuestionType
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "question")
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    val exam: Exam,

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    val questionText: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: QuestionType,

    @Column(nullable = false)
    val score: Double,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
