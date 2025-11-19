package global.command.fill_db.entity

import jakarta.persistence.*

@Entity
@Table(name = "group_exam_mentor")
data class GroupExamMentor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_exam_id", nullable = false)
    val groupExam: GroupExam,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
)
