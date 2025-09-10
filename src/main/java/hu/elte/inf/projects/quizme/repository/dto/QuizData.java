package hu.elte.inf.projects.quizme.repository.dto;

import java.util.List;

public class QuizData {
    private QuizMetadata quizMetadata;
    private List<Topic> topics;
    private List<Question> questions;

    private Category category;

    public QuizData() {
    }

    public QuizData(QuizMetadata quizMetadata, List<Topic> topics, List<Question> questions) {
        this.quizMetadata = quizMetadata;
        this.topics = topics;
        this.questions = questions;
    }

    public QuizMetadata getQuizMetadata() {
        return quizMetadata;
    }

    public void setQuizMetadata(QuizMetadata quizMetadata) {
        this.quizMetadata = quizMetadata;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
