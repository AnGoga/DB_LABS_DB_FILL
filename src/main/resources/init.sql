
CREATE TYPE user_role AS ENUM ('Student', 'Mentor', 'Admin');
CREATE TYPE question_type AS ENUM ('FreeAns', 'ChooseAns', 'MentorFreeAns');

CREATE TABLE IF NOT EXISTS student_group
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users
(
    id                  SERIAL PRIMARY KEY,
    f_name              VARCHAR(255)        NOT NULL,
    l_name              VARCHAR(255)        NOT NULL,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_hash       VARCHAR(255)        NOT NULL,
    salt                VARCHAR(255)        NOT NULL,
    student_group_id    INT,
    role                VARCHAR(255)      NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_group_id) REFERENCES student_group
);


CREATE TABLE IF NOT EXISTS group_users
(
    group_id   INTEGER NOT NULL,
    user_id    INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES student_group (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE TABLE IF NOT EXISTS exam
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS group_exam
(
    id         SERIAL PRIMARY KEY,
    exam_id    INTEGER     NOT NULL,
    group_id   INTEGER     NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time   TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exam (id),
    FOREIGN KEY (group_id) REFERENCES student_group (id)
);

CREATE TABLE IF NOT EXISTS question
(
    id                  SERIAL PRIMARY KEY,
    exam_id             INTEGER            NOT NULL,
    question_text       TEXT               NOT NULL,
    type                VARCHAR(255) NOT NULL,
    score               FLOAT              NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exam (id)
);

CREATE TABLE IF NOT EXISTS question_variant
(
    id              SERIAL PRIMARY KEY,
    question_id     INTEGER NOT NULL,
    variant_text    TEXT    NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question (id)
);
CREATE TABLE IF NOT EXISTS question_variant_correct
(
    id                  SERIAL PRIMARY KEY,
    question_id         INTEGER NOT NULL,
    question_variant_id INTEGER NOT NULL,
    explanation         TEXT,
    FOREIGN KEY (question_id) REFERENCES
        question (id),
    FOREIGN KEY (question_variant_id) REFERENCES
        question_variant (id)
);
CREATE TABLE IF NOT EXISTS questione_correct_free_ans
(
    id           SERIAL PRIMARY KEY,
    question_id  INTEGER NOT NULL,
    correct_text TEXT    NOT NULL,
    explanation  TEXT,
    FOREIGN KEY (question_id) REFERENCES
        question (id)
);
CREATE TABLE IF NOT EXISTS user_try
(
    id            SERIAL PRIMARY KEY,
    group_exam_id INTEGER     NOT NULL,
    user_id       INTEGER     NOT NULL,
    start_try     TIMESTAMPTZ NOT NULL,
    end_try       TIMESTAMPTZ,
    mark          FLOAT,
    total_score   FLOAT,
    created_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_exam_id) REFERENCES group_exam (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE TABLE IF NOT EXISTS user_try_question_answer
(
    id          SERIAL PRIMARY KEY,
    user_try_id INTEGER     NOT NULL,
    question_id INTEGER     NOT NULL,
    answered_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_try_id) REFERENCES
        user_try (id),
    FOREIGN KEY (question_id) REFERENCES
        question (id)
);
CREATE TABLE IF NOT EXISTS user_free_ans
(
    id                          SERIAL PRIMARY KEY,
    user_try_question_answer_id INTEGER NOT NULL,
    ans_text                        TEXT,
    file_path                   TEXT,
    FOREIGN KEY (user_try_question_answer_id) REFERENCES
        user_try_question_answer (id)
);
CREATE TABLE IF NOT EXISTS user_choose_ans
(
    id                          SERIAL PRIMARY KEY,
    user_try_question_answer_id INTEGER NOT NULL,
    question_variant_id         INTEGER NOT NULL,
    FOREIGN KEY (user_try_question_answer_id) REFERENCES
        user_try_question_answer (id),
    FOREIGN KEY (question_variant_id) REFERENCES
        question_variant (id)
);
CREATE TABLE IF NOT EXISTS group_exam_mentor
(
    id            SERIAL PRIMARY KEY,
    group_exam_id INTEGER NOT NULL,
    user_id       INTEGER NOT NULL,
    FOREIGN KEY (group_exam_id) REFERENCES
        group_exam (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS mentor_reply
(
    id                           SERIAL PRIMARY KEY,
    score                        FLOAT       NOT NULL,
    comment                      TEXT,
    group_exam_mentor_id         INTEGER     NOT NULL,
    user_try_question_answer_id  INTEGER     NOT NULL,
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mentor_reply_group_exam_mentor
        FOREIGN KEY (group_exam_mentor_id)
            REFERENCES group_exam_mentor(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_mentor_reply_answer
        FOREIGN KEY (user_try_question_answer_id)
            REFERENCES user_try_question_answer(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_mentor_reply_answer
        UNIQUE (user_try_question_answer_id)
);

-- ============================================================================
-- ИНДЕКСЫ ДЛЯ ОПТИМИЗАЦИИ ЗАПРОСОВ
-- ============================================================================

-- users
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_student_group_id ON users(student_group_id);

-- group_exam
CREATE INDEX IF NOT EXISTS idx_group_exam_exam_id ON group_exam(exam_id);
CREATE INDEX IF NOT EXISTS idx_group_exam_group_id ON group_exam(group_id);
CREATE INDEX IF NOT EXISTS idx_group_exam_start_time ON group_exam(start_time);
CREATE INDEX IF NOT EXISTS idx_group_exam_end_time ON group_exam(end_time);

-- question
CREATE INDEX IF NOT EXISTS idx_question_exam_id ON question(exam_id);
CREATE INDEX IF NOT EXISTS idx_question_type ON question(type);
CREATE INDEX IF NOT EXISTS idx_question_exam_type ON question(exam_id, type);

-- question_variant
CREATE INDEX IF NOT EXISTS idx_question_variant_question_id ON question_variant(question_id);

-- question_variant_correct
CREATE INDEX IF NOT EXISTS idx_question_variant_correct_question_id ON question_variant_correct(question_id);
CREATE INDEX IF NOT EXISTS idx_question_variant_correct_variant_id ON question_variant_correct(question_variant_id);

-- questione_correct_free_ans
CREATE INDEX IF NOT EXISTS idx_questione_correct_free_ans_question_id ON questione_correct_free_ans(question_id);

-- user_try
CREATE INDEX IF NOT EXISTS idx_user_try_group_exam_id ON user_try(group_exam_id);
CREATE INDEX IF NOT EXISTS idx_user_try_user_id ON user_try(user_id);
CREATE INDEX IF NOT EXISTS idx_user_try_start_try ON user_try(start_try);
CREATE INDEX IF NOT EXISTS idx_user_try_group_exam_user ON user_try(group_exam_id, user_id);

-- user_try_question_answer
CREATE INDEX IF NOT EXISTS idx_utqa_user_try_id ON user_try_question_answer(user_try_id);
CREATE INDEX IF NOT EXISTS idx_utqa_question_id ON user_try_question_answer(question_id);
CREATE INDEX IF NOT EXISTS idx_utqa_user_try_question ON user_try_question_answer(user_try_id, question_id);

-- user_free_ans
CREATE INDEX IF NOT EXISTS idx_user_free_ans_utqa_id ON user_free_ans(user_try_question_answer_id);

-- user_choose_ans
CREATE INDEX IF NOT EXISTS idx_user_choose_ans_utqa_id ON user_choose_ans(user_try_question_answer_id);
CREATE INDEX IF NOT EXISTS idx_user_choose_ans_variant_id ON user_choose_ans(question_variant_id);

-- group_exam_mentor
CREATE INDEX IF NOT EXISTS idx_group_exam_mentor_group_exam_id ON group_exam_mentor(group_exam_id);
CREATE INDEX IF NOT EXISTS idx_group_exam_mentor_user_id ON group_exam_mentor(user_id);

-- mentor_reply
CREATE INDEX IF NOT EXISTS idx_mentor_reply_group_exam_mentor_id ON mentor_reply(group_exam_mentor_id);
CREATE INDEX IF NOT EXISTS idx_mentor_reply_utqa_id ON mentor_reply(user_try_question_answer_id);