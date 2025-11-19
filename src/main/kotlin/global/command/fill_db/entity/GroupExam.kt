package global.command.fill_db.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "group_exam")
data class GroupExam(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    val exam: Exam,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val studentGroup: StudentGroup,

    @Column(name = "start_time", nullable = false)
    val startTime: OffsetDateTime,

    @Column(name = "end_time", nullable = false)
    val endTime: OffsetDateTime,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
