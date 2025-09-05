
// Function to update time estimator - MUST be available immediately for inline handlers
function updateTimeEstimator(quizId, numberOfQuestions) {
    const timeDisplayElement = document.getElementById('time-display-' + quizId);
    const progressCircleElement = document.getElementById('progressCircle-' + quizId);

    // Also try to find all time-related elements
    const allTimeElements = document.querySelectorAll('[id*="time-display"]');
    const allProgressElements = document.querySelectorAll('[id*="progress-circle"]');

    if (timeDisplayElement) {
        // Calculate time in minutes (10 seconds per question)
        const timeInMinutes = Math.round((numberOfQuestions * 10.0 / 60.0) * 10.0) / 10.0;
        
        // Update time display
        timeDisplayElement.textContent = timeInMinutes.toFixed(1);

        if (progressCircleElement) {
            // Calculate progress percentage based on selected questions vs max questions
            // We need to get the max questions from the range slider
            const rangeSlider = document.querySelector(`[id="questions-range-${quizId}"]`);
            if (rangeSlider) {
                const maxQuestions = parseInt(rangeSlider.max);
                const selectedQuestions = parseInt(numberOfQuestions);
                const progressPercent = Math.round((selectedQuestions / maxQuestions) * 100);
                // Update progress circle (circumference = 50, so progress = percentage/2)
                const strokeLength = Math.round((progressPercent / 100) * 50);
                setProgress(progressCircleElement, progressPercent);
            } else {
                // Fallback: assume 100% if we can't find the slider
                setProgress(progressCircleElement, 100);
            }
        } else {
        }
    } else {
    }
}

function setProgress(circle, percent) {
    if (!circle) {
        return;
    }

    const radius = circle.r.baseVal.value;
    const circumference = 2 * Math.PI * radius;

    circle.style.strokeDasharray = `${circumference} ${circumference}`;
    circle.style.strokeDashoffset = circumference - (percent / 100) * circumference;
}

// Make it globally available
window.updateTimeEstimator = updateTimeEstimator;

// Add event listeners for buttons and controls
document.addEventListener('click', function(e) {
    // Check if the clicked element or its parent has the start-quiz-btn class
    const startQuizBtn = e.target.closest('.start-quiz-btn');
    if (startQuizBtn) {
        e.preventDefault();
        const quizId = startQuizBtn.getAttribute('data-quiz-id');
        const quizTitle = startQuizBtn.getAttribute('data-quiz-title');
        const category = startQuizBtn.getAttribute('data-category');
        const topic = startQuizBtn.getAttribute('data-topic');
        startQuiz(quizTitle, quizId, category, topic);
    }
    
    // Check if the clicked element or its parent has the cancel-quiz-btn class
    const cancelQuizBtn = e.target.closest('.cancel-quiz-btn');
    if (cancelQuizBtn) {
        e.preventDefault();
        const modalId = cancelQuizBtn.getAttribute('data-modal-hide');
        const modal = document.getElementById(modalId);
        if (modal) {
            // Clear any focused elements to prevent aria-hidden focus issues
            document.activeElement.blur();
            modal.classList.add('hidden');
            modal.setAttribute('aria-hidden', 'true');
            // Remove focus trap
            modal.removeAttribute('tabindex');
            document.body.classList.remove('overflow-hidden');
        }
    }

    // Handle difficulty button selection
    const difficultyBtn = e.target.closest('.difficulty-btn');
    if (difficultyBtn) {
        e.preventDefault();
        const buttonGroup = difficultyBtn.parentElement;
        const allButtons = buttonGroup.querySelectorAll('.difficulty-btn');
        
        // Reset all buttons to their default state by removing active classes
        allButtons.forEach(btn => {
            btn.classList.remove('active-difficulty');
            const difficulty = btn.getAttribute('data-difficulty');
            
            // Reset each button to its original color scheme
            if (difficulty === 'easy') {
                btn.className = 'difficulty-btn flex items-center justify-center px-4 py-3 text-sm font-medium transition-all duration-200 bg-green-50 text-green-700 border border-green-200 dark:bg-green-900/20 dark:text-green-400 dark:border-green-800 hover:bg-green-100 dark:hover:bg-green-900/40 focus:z-10 focus:ring-2 focus:ring-green-500 data-[active=true]:bg-green-600 data-[active=true]:text-white data-[active=true]:border-green-600';
            } else if (difficulty === 'medium') {
                btn.className = 'difficulty-btn flex items-center justify-center px-4 py-3 text-sm font-medium transition-all duration-200 bg-yellow-50 text-yellow-700 border border-yellow-200 dark:bg-yellow-900/20 dark:text-yellow-400 dark:border-yellow-800 hover:bg-yellow-100 dark:hover:bg-yellow-900/40 focus:z-10 focus:ring-2 focus:ring-yellow-500 data-[active=true]:bg-yellow-600 data-[active=true]:text-white data-[active=true]:border-yellow-600';
            } else if (difficulty === 'hard') {
                btn.className = 'difficulty-btn flex items-center justify-center px-4 py-3 text-sm font-medium transition-all duration-200 bg-red-50 text-red-700 border border-red-200 dark:bg-red-900/20 dark:text-red-400 dark:border-red-800 hover:bg-red-100 dark:hover:bg-red-900/40 focus:z-10 focus:ring-2 focus:ring-red-500 data-[active=true]:bg-red-600 data-[active=true]:text-white data-[active=true]:border-red-600';
            } else if (difficulty === 'mixed') {
                btn.className = 'difficulty-btn flex items-center justify-center px-4 py-3 text-sm font-medium transition-all duration-200 bg-blue-600 text-white border border-blue-600 hover:bg-blue-700 hover:border-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-500';
            }
        });
        
        // Set clicked button to active state
        difficultyBtn.classList.add('active-difficulty');
        const selectedDifficulty = difficultyBtn.getAttribute('data-difficulty');
        
        // Apply active styling based on the button's difficulty level
        if (selectedDifficulty === 'easy') {
            difficultyBtn.classList.remove('bg-green-50', 'text-green-700', 'border-green-200');
            difficultyBtn.classList.add('bg-green-600', 'text-white', 'border-green-600');
        } else if (selectedDifficulty === 'medium') {
            difficultyBtn.classList.remove('bg-yellow-50', 'text-yellow-700', 'border-yellow-200');
            difficultyBtn.classList.add('bg-yellow-600', 'text-white', 'border-yellow-600');
        } else if (selectedDifficulty === 'hard') {
            difficultyBtn.classList.remove('bg-red-50', 'text-red-700', 'border-red-200');
            difficultyBtn.classList.add('bg-red-600', 'text-white', 'border-red-600');
        }
        // Mixed button is already styled as active by default
    }
});

// Add event listener for range slider
document.addEventListener('input', function(e) {
    // Check if it's any range input
    if (e.target.type === 'range') {
        const quizId = e.target.getAttribute('data-quiz-id');
        const value = e.target.value;
        
        // Update question count (if element exists)
        updateQuestionCount(quizId, value);
        
        // Always update time estimator
        updateTimeEstimator(quizId, value);
    } else {
    }
});

// Also try with 'change' event as fallback
document.addEventListener('change', function(e) {
    if (e.target.classList.contains('questions-range-slider')) {
        const quizId = e.target.getAttribute('data-quiz-id');
        const value = e.target.value;

        // Update question count (if element exists)
        updateQuestionCount(quizId, value);
        
        // Always update time estimator
        updateTimeEstimator(quizId, value);
    }
});

// Function to update question count display
function updateQuestionCount(quizId, value) {
    const countElement = document.getElementById('question-count-' + quizId);

    if (countElement) {
        countElement.textContent = value;
    } else {
    }
}

// Initialize question counts and time estimators on page load
document.addEventListener('DOMContentLoaded', function() {
    const sliders = document.querySelectorAll('.questions-range-slider');
    sliders.forEach(slider => {
        const quizId = slider.getAttribute('data-quiz-id');
        const value = slider.value;

        updateQuestionCount(quizId, value);
        updateTimeEstimator(quizId, value);
    });
});

// Quiz start function
function startQuiz(quizTitle, quizId, category, topic) {
    
    // Get the modal ID by sanitizing the quiz ID (same as HTML template)
    const sanitizedQuizId = quizId.replace(/[^a-zA-Z0-9]/g, '-');
    const modalId = 'quiz-modal-' + sanitizedQuizId;
    
    // Get selected difficulty from button group
    const modal = document.getElementById(modalId);
    if (!modal) {
        // Use default values if modal is not found
        const selectedDifficulty = 'mixed';
        const numberOfQuestions = 10;
        submitQuizForm(quizId, selectedDifficulty, numberOfQuestions);
        return;
    }
    
    const activeDifficultyBtn = modal.querySelector('.difficulty-btn.active-difficulty');
    const selectedDifficulty = activeDifficultyBtn ? activeDifficultyBtn.getAttribute('data-difficulty') : 'mixed';
    
    // Get number of questions from range slider  
    const rangeSlider = document.getElementById('questions-range-' + sanitizedQuizId);
    const numberOfQuestions = rangeSlider ? rangeSlider.value : 10;
    
    submitQuizForm(quizId, selectedDifficulty, numberOfQuestions);
}

function submitQuizForm(quizId, selectedDifficulty, numberOfQuestions) {
    // Create form to submit POST request to QuizController's startQuiz endpoint
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/quiz/start/${encodeURIComponent(quizId)}`;
    
    // Add form parameters
    const questionCountInput = document.createElement('input');
    questionCountInput.type = 'hidden';
    questionCountInput.name = 'questionCount';
    questionCountInput.value = numberOfQuestions;
    form.appendChild(questionCountInput);
    
    const difficultyInput = document.createElement('input');
    difficultyInput.type = 'hidden';
    difficultyInput.name = 'difficulty';
    difficultyInput.value = selectedDifficulty;
    form.appendChild(difficultyInput);

    
    // Submit form
    document.body.appendChild(form);
    form.submit();
}