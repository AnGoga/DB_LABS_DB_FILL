package global.command.fill_db.entity

import jakarta.persistence.*

@Entity
@Table(name = "question_variant")
data class QuestionVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @Column(name = "variant_text", nullable = false, columnDefinition = "TEXT")
    val variantText: String
)
