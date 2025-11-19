package global.command.fill_db.entity

import jakarta.persistence.*

@Entity
@Table(name = "questione_correct_free_ans")
data class QuestionCorrectFreeAns(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @Column(name = "correct_text", nullable = false, columnDefinition = "TEXT")
    val correctText: String,

    @Column(columnDefinition = "TEXT")
    val explanation: String? = null
)
