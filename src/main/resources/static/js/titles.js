console.log('🚀 JavaScript is loading!');

// Simple test function available immediately
window.testJS = function() {
    console.log('✅ JavaScript is working!');
    return 'JavaScript is working!';
};

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
                console.log('MaxQuestions:', maxQuestions, ' SelectedQuestions: ', selectedQuestions, ' progressPercent:', progressPercent);
                // Update progress circle (circumference = 50, so progress = percentage/2)
                const strokeLength = Math.round((progressPercent / 100) * 50);
                setProgress(progressCircleElement, progressPercent);
                console.log('✅ Updated progress circle to:', progressPercent + '% (', selectedQuestions, '/', maxQuestions, 'questions), stroke:', strokeLength);
            } else {
                // Fallback: assume 100% if we can't find the slider
                setProgress(progressCircleElement, 100);
                console.log('✅ Range Slider not found: 100% (fallback)');
            }
        } else {
            console.log('❌ Progress circle element not found');
        }
    } else {
        console.log('❌ Time display element not found');
    }
}

function setProgress(circle, percent) {
    if (!circle) {
        console.warn("No circle with id:", id);
        return;
    }

    const radius = circle.r.baseVal.value;
    const circumference = 2 * Math.PI * radius;

    circle.style.strokeDasharray = `${circumference} ${circumference}`;
    circle.style.strokeDashoffset = circumference - (percent / 100) * circumference;
}

// Make it globally available
window.updateTimeEstimator = updateTimeEstimator;

// Note: Removed immediate test to prevent exceptions on page load

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 DOM Content Loaded event fired!');
    
    const allQuizModals = document.querySelectorAll('[id*="quiz-modal"]');
    const allRangeSliders = document.querySelectorAll('.questions-range-slider');
    const allTimeDisplays = document.querySelectorAll('[id*="time-display"]');
    const allProgressCircles = document.querySelectorAll('[id*="progress-circle"]');

    const searchInput = document.getElementById('quizSearch');
    const quizCards = document.querySelectorAll('.quiz-card');

    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();

        quizCards.forEach(card => {
            const quizTitle = card.querySelector('h2').textContent.toLowerCase();
            const shouldShow = quizTitle.includes(searchTerm);
            
            if (shouldShow) {
                card.style.display = 'block';
                card.classList.remove('hidden');
            } else {
                card.style.display = 'none';
                card.classList.add('hidden');
            }
        });
    });
});

// Add event listeners for buttons and controls
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('start-quiz-btn')) {
        e.preventDefault();
        const quizId = e.target.getAttribute('data-quiz-id');
        const quizTitle = e.target.getAttribute('data-quiz-title');
        const category = e.target.getAttribute('data-category');
        const topic = e.target.getAttribute('data-topic');
        startQuiz(quizTitle, quizId, category, topic);
    }
    
    if (e.target.classList.contains('cancel-quiz-btn')) {
        e.preventDefault();
        const modalId = e.target.getAttribute('data-modal-hide');
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
    if (e.target.classList.contains('difficulty-btn')) {
        e.preventDefault();
        const buttonGroup = e.target.parentElement;
        const allButtons = buttonGroup.querySelectorAll('.difficulty-btn');
        
        // Reset all buttons to default state
        allButtons.forEach(btn => {
            btn.classList.remove('active-difficulty', 'text-white', 'bg-blue-700', 'border-blue-700', 'dark:bg-blue-600');
            btn.classList.add('text-gray-900', 'bg-white', 'border-gray-200', 'dark:bg-gray-800', 'dark:border-gray-700', 'dark:text-white');
        });
        
        // Set clicked button to active state
        e.target.classList.remove('text-gray-900', 'bg-white', 'border-gray-200', 'dark:bg-gray-800', 'dark:border-gray-700', 'dark:text-white');
        e.target.classList.add('active-difficulty', 'text-white', 'bg-blue-700', 'border-blue-700', 'dark:bg-blue-600');
    }
});

// Add event listener for range slider
document.addEventListener('input', function(e) {
    // Check if it's any range input
    if (e.target.type === 'range') {
        console.log('✅ Range input detected!');
        const quizId = e.target.getAttribute('data-quiz-id');
        const value = e.target.value;
        console.log('Range slider - quizId:', quizId, 'value:', value);
        
        // Update question count (if element exists)
        updateQuestionCount(quizId, value);
        
        // Always update time estimator
        console.log('About to call updateTimeEstimator with:', quizId, value);
        updateTimeEstimator(quizId, value);
    } else {
        console.log('❌ Not a range input, type is:', e.target.type);
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
        // Let's also try to find all elements with similar IDs
        const allElements = document.querySelectorAll('[id*="question-count"]');
        console.log('All question-count elements found:', allElements);
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
    console.log('Starting quiz:', quizTitle, 'ID:', quizId, category, topic);
    
    // Get the modal ID by sanitizing the quiz ID (same as HTML template)
    const sanitizedQuizId = quizId.replace(/[^a-zA-Z0-9]/g, '-');
    const modalId = 'quiz-modal-' + sanitizedQuizId;
    
    // Get selected difficulty from button group
    const modal = document.getElementById(modalId);
    if (!modal) {
        console.error('Modal not found with ID:', modalId);
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

    console.log('Submitting form:', form.action, 'with params:', {
        questionCount: numberOfQuestions,
        difficulty: selectedDifficulty,
        quizId: quizId
    });
    
    // Submit form
    document.body.appendChild(form);
    form.submit();
}