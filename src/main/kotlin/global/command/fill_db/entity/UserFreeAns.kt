package global.command.fill_db.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_free_ans")
data class UserFreeAns(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_try_question_answer_id", nullable = false)
    val userTryQuestionAnswer: UserTryQuestionAnswer,

    @Column(name = "ans_text", columnDefinition = "TEXT")
    val ansText: String? = null,

    @Column(name = "file_path", columnDefinition = "TEXT")
    val filePath: String? = null
)
