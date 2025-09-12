document.addEventListener('DOMContentLoaded', () => {
    const startQuizBtn = document.getElementById('start-quiz-btn');

    if (startQuizBtn) {
        startQuizBtn.addEventListener('click', async () => {
            const titleName = startQuizBtn.dataset.quizId;
            const selectedDifficultyRadio = document.querySelector('input[name="difficulty"]:checked');
            const difficulty = selectedDifficultyRadio ? selectedDifficultyRadio.dataset.difficulty : 'mixed'; // Default to 'mixed' if none selected

            const questionCountSlider = document.querySelector('.enhanced-slider');
            const questionCount = questionCountSlider ? questionCountSlider.value : 10; // Default to 10 if slider not found

            const url = `/quiz/start/${titleName}`;

            const requestBody = {
                questionCount: parseInt(questionCount),
                difficulty: difficulty
            };

            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestBody)
                });

                if (response.ok) {
                    const responseData = await response.json();
                    // Assuming the response contains a URL to redirect to the quiz page
                    if (responseData.redirectUrl) {
                        window.location.href = responseData.redirectUrl;
                    } else {
                        // Fallback or error handling if no redirectUrl is provided
                        console.error('No redirect URL provided in response.');
                        alert('Quiz started, but no redirect URL was provided.');
                    }
                } else {
                    console.error('Failed to start quiz:', response.status, response.statusText);
                    alert('Failed to start quiz. Please try again.');
                }
            } catch (error) {
                console.error('Error starting quiz:', error);
                alert('An error occurred while trying to start the quiz.');
            }
        });
    }
});
