package global.command.fill_db.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "user_try")
data class UserTry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_exam_id", nullable = false)
    val groupExam: GroupExam,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "start_try", nullable = false)
    val startTry: OffsetDateTime,

    @Column(name = "end_try")
    val endTry: OffsetDateTime? = null,

    @Column
    val mark: Double? = null,

    @Column(name = "total_score")
    val totalScore: Double? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
