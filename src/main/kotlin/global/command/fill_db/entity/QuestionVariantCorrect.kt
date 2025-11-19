package global.command.fill_db.entity

import jakarta.persistence.*

@Entity
@Table(name = "question_variant_correct")
data class QuestionVariantCorrect(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_variant_id", nullable = false)
    val questionVariant: QuestionVariant,

    @Column(columnDefinition = "TEXT")
    val explanation: String? = null
)
