document.addEventListener('DOMContentLoaded', () => {
    const numQuestionsSelect = document.getElementById('numQuestions');
    const totalQuestions = parseInt(numQuestionsSelect.dataset.totalQuestions);

    function generateEvenlyDistributedOptions(min, max, desiredCount) {
        const options = new Set(); // Use a Set to avoid duplicates and maintain order for insertion
        console.log(`Generating options between ${min} and ${max} aiming for ${desiredCount} options.`);
        // Always include the minimum and maximum values
        if (min <= max) {
            options.add(min);
            options.add(max);
        }

        // If max is less than 3, just add max (if it's not already there)
        if (max < min) {
            if (!options.has(max)) {
                options.add(max);
            }
            return Array.from(options).sort((a, b) => a - b);
        }

        // Calculate step for even distribution
        // Ensure we have at least 2 points (min and max) for step calculation
        const effectiveDesiredCount = Math.max(2, desiredCount);
        const step = (max - min) / (effectiveDesiredCount - 1);

        for (let i = 1; i < max - 1; i++) {
            options.add(i);
        }

        // Convert Set to Array and sort numerically
        return Array.from(options).sort((a, b) => a - b);
    }

    if (numQuestionsSelect && !isNaN(totalQuestions)) {
        const optionsToGenerate = generateEvenlyDistributedOptions(1, totalQuestions, 10); // Aim for 10 options

        // Clear existing options (if any, though HTML is now empty)
        numQuestionsSelect.innerHTML = '';

        optionsToGenerate.forEach(value => {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = value;
            numQuestionsSelect.appendChild(option);
        });

        // Set the default selected value to totalQuestions if it's one of the options,
        // otherwise, select the largest available option.
        if (optionsToGenerate.includes(totalQuestions)) {
            numQuestionsSelect.value = totalQuestions;
        } else if (optionsToGenerate.length > 0) {
            numQuestionsSelect.value = optionsToGenerate[optionsToGenerate.length - 1];
        }
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