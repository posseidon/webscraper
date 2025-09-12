document.addEventListener('DOMContentLoaded', () => {
    const startQuizBtn = document.getElementById('start-quiz-btn');

    if (startQuizBtn) {
        startQuizBtn.addEventListener('click', async () => {
            const titleName = startQuizBtn.dataset.quizId;
            const selectedDifficultyRadio = document.querySelector('input[name="difficulty"]:checked');
            const difficulty = selectedDifficultyRadio ? selectedDifficultyRadio.dataset.difficulty : 'mixed'; // Default to 'mixed' if none selected

            const questionCountSlider = document.querySelector('.enhanced-slider');
            const questionCount = questionCountSlider ? questionCountSlider.value : 10; // Default to 10 if slider not found

            startTitleQuiz(titleName);
        });
    }
});

// Function to start title quiz (main quiz)
function startTitleQuiz(titleId) {
    // Get selected difficulty
    const selectedDifficulty = document.querySelector('input[name="difficulty"]:checked');
    const difficulty = selectedDifficulty ? selectedDifficulty.getAttribute('data-difficulty') : 'mixed';

    // Get selected question count
    const questionCountSlider = document.querySelector('.enhanced-slider');
    const questionCount = questionCountSlider ? questionCountSlider.value : 10; // Default to 10 if slider not found

    // Create form to submit POST request to start title quiz
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/quiz/start/${encodeURIComponent(titleId)}`;

    // Add form parameters
    const difficultyInput = document.createElement('input');
    difficultyInput.type = 'hidden';
    difficultyInput.name = 'difficulty';
    difficultyInput.value = difficulty;
    form.appendChild(difficultyInput);

    const questionCountInput = document.createElement('input');
    questionCountInput.type = 'hidden';
    questionCountInput.name = 'questionCount';
    questionCountInput.value = questionCount.toString();
    form.appendChild(questionCountInput);

    // Submit form
    document.body.appendChild(form);
    form.submit();
}