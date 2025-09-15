
// Accordion click handler function
function handleCustomAccordionClick(e) {
    e.preventDefault();
    e.stopPropagation();


    const button = this;
    const targetId = button.getAttribute('data-accordion-target');
    const target = document.querySelector(targetId);
    const accordionContainer = button.closest('[data-custom-accordion]');

    if (!target || !accordionContainer) {
        return;
    }

    const isCurrentlyOpen = !target.classList.contains('hidden');

    // Close all accordion items in this container first
    const allButtons = accordionContainer.querySelectorAll('[data-accordion-target]');
    allButtons.forEach(btn => {
        const btnTargetId = btn.getAttribute('data-accordion-target');
        const btnTarget = document.querySelector(btnTargetId);
        const btnIcon = btn.querySelector('[data-accordion-icon]');

        if (btnTarget) {
            btnTarget.classList.add('hidden');
            btn.setAttribute('aria-expanded', 'false');

            if (btnIcon) {
                btnIcon.classList.remove('rotate-180');
            }
        }
    });

    // If the clicked item was closed, open it
    if (!isCurrentlyOpen) {
        target.classList.remove('hidden');
        button.setAttribute('aria-expanded', 'true');

        const currentIcon = button.querySelector('[data-accordion-icon]');
        if (currentIcon) {
            currentIcon.classList.add('rotate-180');
        }
    }
}

// Function to update time estimator - MUST be available immediately for inline handlers
function updateTimeEstimator(quizId, numberOfQuestions) {
    const timeDisplayElement = document.getElementById('time-display-' + quizId);
    const progressCircleElement = document.getElementById('progressCircle-' + quizId);

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
document.addEventListener('click', function (e) {


    // Handle accordion clicks
    const accordionButton = e.target.closest('[data-accordion-target]');
    if (accordionButton && accordionButton.closest('[data-custom-accordion]')) {
        handleCustomAccordionClick.call(accordionButton, e);
        return;
    }

    // Check if the clicked element or its parent has the start-quiz-btn class
    const startQuizBtn = e.target.closest('.start-quiz-btn');
    if (startQuizBtn) {
        e.preventDefault();
        const quizId = startQuizBtn.getAttribute('data-quiz-id');
        const quizTitle = startQuizBtn.getAttribute('data-quiz-title');
        const category = startQuizBtn.getAttribute('data-category');
        const topic = startQuizBtn.getAttribute('data-subcategory');
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

    // Handle difficulty selection (radio buttons)
    const difficultyLabel = e.target.closest('label[for]');
    if (difficultyLabel) {
        const radioId = difficultyLabel.getAttribute('for');
        const radioInput = document.getElementById(radioId);

        if (radioInput && radioInput.classList.contains('difficulty-radio')) {
            // Check the radio button
            radioInput.checked = true;

            // Update visual styling
            updateDifficultySelection(radioInput);
        }
    }
});

// Function to update difficulty selection styling
function updateDifficultySelection(selectedRadio) {
    // Get all difficulty radios in the same group
    const allRadios = document.querySelectorAll('input[name="difficulty"]');

    allRadios.forEach(radio => {
        const label = document.querySelector(`label[for="${radio.id}"]`);
        if (label) {
            const difficulty = radio.getAttribute('data-difficulty');

            // Reset all labels to default state
            label.classList.remove(
                'border-green-500', 'bg-green-50', 'dark:border-green-400', 'dark:bg-green-900/20',
                'border-yellow-500', 'bg-yellow-50', 'dark:border-yellow-400', 'dark:bg-yellow-900/20',
                'border-red-500', 'bg-red-50', 'dark:border-red-400', 'dark:bg-red-900/20',
                'border-blue-500', 'bg-blue-50', 'dark:border-blue-400', 'dark:bg-blue-900/20'
            );
            label.classList.add('border-gray-200', 'dark:border-gray-600');

            // If this is the selected radio, apply active styling
            if (radio.checked) {
                label.classList.remove('border-gray-200', 'dark:border-gray-600');

                switch (difficulty) {
                    case 'easy':
                        label.classList.add('border-green-500', 'bg-green-50', 'dark:border-green-400', 'dark:bg-green-900/20');
                        break;
                    case 'medium':
                        label.classList.add('border-yellow-500', 'bg-yellow-50', 'dark:border-yellow-400', 'dark:bg-yellow-900/20');
                        break;
                    case 'hard':
                        label.classList.add('border-red-500', 'bg-red-50', 'dark:border-red-400', 'dark:bg-red-900/20');
                        break;
                    case 'mixed':
                        label.classList.add('border-blue-500', 'bg-blue-50', 'dark:border-blue-400', 'dark:bg-blue-900/20');
                        break;
                }
            }
        }
    });
}

// Add event listener for range slider
document.addEventListener('input', function (e) {
    // Check if it's any range input
    if (e.target.type === 'range') {
        const quizId = e.target.getAttribute('data-quiz-id');
        const value = e.target.value;

        // Update question count (if element exists)
        updateQuestionCount(quizId, value);

        // Always update time estimator
        updateTimeEstimator(quizId, value);

        // Update slider value display
        updateSliderValueDisplay(e.target, quizId, value);
    } else {
    }
});

// Also try with 'change' event as fallback
document.addEventListener('change', function (e) {
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

// Function to update slider value display
function updateSliderValueDisplay(slider, quizId, value) {
    const valueDisplay = document.getElementById('slider-value-' + quizId);

    if (valueDisplay) {
        // Update the text content
        valueDisplay.textContent = value;

        // Calculate the position of the thumb
        const min = parseFloat(slider.min);
        const max = parseFloat(slider.max);
        const val = parseFloat(value);

        // Calculate percentage position (0-100%)
        const percent = (val - min) / (max - min);

        // Get slider width and account for thumb width (20px)
        const sliderWidth = slider.offsetWidth;
        const thumbWidth = 20;

        // Calculate position accounting for thumb being centered
        const position = percent * (sliderWidth - thumbWidth) + (thumbWidth / 2);

        // Update position
        valueDisplay.style.left = position + 'px';

        // Show the value display when slider is being moved
        valueDisplay.style.opacity = '1';

        // Hide after a delay when not actively moving
        clearTimeout(valueDisplay.hideTimeout);
        valueDisplay.hideTimeout = setTimeout(() => {
            if (!slider.matches(':hover') && !slider.matches(':focus')) {
                valueDisplay.style.opacity = '0';
            }
        }, 1500);
    }
}

// Initialize question counts and time estimators on page load
document.addEventListener('DOMContentLoaded', function () {
    // Look for both old and new slider classes
    const sliders = document.querySelectorAll('.questions-range-slider, .enhanced-slider');
    sliders.forEach(slider => {
        const quizId = slider.getAttribute('data-quiz-id');
        const value = slider.value;

        updateQuestionCount(quizId, value);
        updateTimeEstimator(quizId, value);

        // Initialize slider value display position
        if (slider.classList.contains('enhanced-slider')) {
            updateSliderValueDisplay(slider, quizId, value);

            // Add hover and focus event listeners
            const valueDisplay = document.getElementById('slider-value-' + quizId);
            if (valueDisplay) {
                slider.addEventListener('mouseenter', () => {
                    clearTimeout(valueDisplay.hideTimeout);
                    valueDisplay.style.opacity = '1';
                });

                slider.addEventListener('mouseleave', () => {
                    valueDisplay.hideTimeout = setTimeout(() => {
                        if (!slider.matches(':focus')) {
                            valueDisplay.style.opacity = '0';
                        }
                    }, 500);
                });

                slider.addEventListener('focus', () => {
                    clearTimeout(valueDisplay.hideTimeout);
                    valueDisplay.style.opacity = '1';
                });

                slider.addEventListener('blur', () => {
                    valueDisplay.hideTimeout = setTimeout(() => {
                        valueDisplay.style.opacity = '0';
                    }, 500);
                });
            }
        }
    });



    // Initialize accordion functionality immediately and with a longer delay as fallback
    initializeAccordions();

    // Initialize difficulty selection styling
    const checkedRadio = document.querySelector('input[name="difficulty"]:checked');
    if (checkedRadio) {
        updateDifficultySelection(checkedRadio);
    }
});



// Accordion functionality - now handled by the global click listener above
function initializeAccordions() {
    // Set initial state for accordion items - second accordion item should be hidden
    const customAccordions = document.querySelectorAll('[data-custom-accordion]');

    customAccordions.forEach(accordionContainer => {
        const buttons = accordionContainer.querySelectorAll('[data-accordion-target]');

        buttons.forEach((button, index) => {
            const targetId = button.getAttribute('data-accordion-target');
            const target = document.querySelector(targetId);
            const icon = button.querySelector('[data-accordion-icon]');

            if (target) {
                // All accordion items are closed by default
                target.classList.add('hidden');
                button.setAttribute('aria-expanded', 'false');
                if (icon) {
                    icon.classList.remove('rotate-180');
                }
            }
        });
    });
}

// Quiz start function
function startQuiz(quizTitle, quizId, category, topic) {

    // Get the modal ID by sanitizing the quiz ID (same as HTML template)
    const sanitizedQuizId = quizId.replace(/[^a-zA-Z0-9]/g, '-');
    const modalId = 'quiz-modal-' + sanitizedQuizId;

    // Get selected difficulty from radio buttons
    const modal = document.getElementById(modalId);
    if (!modal) {
        // Use default values if modal is not found
        const selectedDifficulty = 'mixed';
        const numberOfQuestions = 10;
        submitQuizForm(quizId, selectedDifficulty, numberOfQuestions);
        return;
    }

    const selectedDifficultyRadio = modal.querySelector('input[name="difficulty"]:checked');
    const selectedDifficulty = selectedDifficultyRadio ? selectedDifficultyRadio.getAttribute('data-difficulty') : 'mixed';

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