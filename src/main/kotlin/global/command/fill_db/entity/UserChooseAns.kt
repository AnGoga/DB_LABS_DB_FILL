package global.command.fill_db.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_choose_ans")
data class UserChooseAns(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_try_question_answer_id", nullable = false)
    val userTryQuestionAnswer: UserTryQuestionAnswer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_variant_id", nullable = false)
    val questionVariant: QuestionVariant
)
