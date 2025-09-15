document.addEventListener('DOMContentLoaded', () => {
    const numQuestionsSelect = document.getElementById('numQuestions');
    const totalQuestions = parseInt(numQuestionsSelect.dataset.totalQuestions);

    if (numQuestionsSelect && !isNaN(totalQuestions)) {
        // Clear existing options (if any, though HTML is now empty)
        numQuestionsSelect.innerHTML = '';

        for (let i = 1; i <= totalQuestions; i++) {
            const option = document.createElement('option');
            option.value = i;
            option.textContent = i;
            numQuestionsSelect.appendChild(option);
        }

        // Set the default selected value to totalQuestions
        numQuestionsSelect.value = totalQuestions;
    }

    const startQuizBtn = document.getElementById('start-quiz-btn');

    if (startQuizBtn) {
        startQuizBtn.addEventListener('click', async () => {
            const titleName = startQuizBtn.dataset.quizId;
            const selectedDifficultyRadio = document.querySelector('input[name="difficulty"]:checked');
            const difficulty = selectedDifficultyRadio ? selectedDifficultyRadio.dataset.difficulty : 'mixed'; // Default to 'mixed' if none selected

            const numQuestionsSelect = document.getElementById('numQuestions');
            const questionCount = numQuestionsSelect ? numQuestionsSelect.value : 10; // Default to 10 if select not found

            startTitleQuiz(titleName);
        });
    }
});

// Function to start title quiz (main quiz)
function startTitleQuiz(titleId) {
    // Get selected difficulty
    const difficulty = document.querySelector('select[name="difficulty"]').value;

    // Get selected question count
    const numQuestionsSelect = document.getElementById('numQuestions');
    const questionCount = numQuestionsSelect ? numQuestionsSelect.value : 10; // Default to 10 if select not found

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