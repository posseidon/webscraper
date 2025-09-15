// Function to update time estimator for topics
function updateTopicTimeEstimator(topicId, numberOfQuestions) {
    const timeDisplayElement = document.getElementById('time-display-' + topicId);
    const progressCircleElement = document.getElementById('progressCircle-' + topicId);

    if (timeDisplayElement) {
        // Calculate time in minutes (10 seconds per question)
        const timeInMinutes = Math.round((numberOfQuestions * 10.0 / 60.0) * 10.0) / 10.0;

        // Update time display
        timeDisplayElement.textContent = timeInMinutes.toFixed(1);

        if (progressCircleElement) {
            // Calculate progress percentage based on selected questions vs max questions
            const rangeSlider = document.querySelector(`[id="topic-questions-range-${topicId}"]`);
            if (rangeSlider) {
                const maxQuestions = parseInt(rangeSlider.max);
                const selectedQuestions = parseInt(numberOfQuestions);
                const progressPercent = Math.round((selectedQuestions / maxQuestions) * 100);
                setProgress(progressCircleElement, progressPercent);
            } else {
                // Fallback: assume 100% if we can't find the slider
                setProgress(progressCircleElement, 100);
            }
        }
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
window.updateTopicTimeEstimator = updateTopicTimeEstimator;

// Add event listeners for topic functionality
document.addEventListener('click', function (e) {
    // Handle play topic quiz button clicks (main quiz start button)
    const playTopicQuizBtn = e.target.closest('.play-topic-quiz-btn');
    if (playTopicQuizBtn) {
        e.preventDefault();
        const topicId = playTopicQuizBtn.getAttribute('data-topic-id');
        const topicName = playTopicQuizBtn.getAttribute('data-topic-name');
        playTopicQuiz(topicId, topicName);
        return;
    }

    // Handle topic content div clicks (direct quiz start)
    const topicContentDiv = e.target.closest('.topic-content-div');
    if (topicContentDiv) {
        e.preventDefault();
        const topicId = topicContentDiv.getAttribute('data-topic-id');
        startTopicQuizDirectly(topicId);
        return;
    }


    // Handle topic quiz modal start button
    const startTopicQuizBtn = e.target.closest('.start-topic-quiz-btn');
    if (startTopicQuizBtn) {
        e.preventDefault();
        const topicId = startTopicQuizBtn.getAttribute('data-topic-id');
        const topicName = startTopicQuizBtn.getAttribute('data-topic-name');
        const category = startTopicQuizBtn.getAttribute('data-category');
        const subcategory = startTopicQuizBtn.getAttribute('data-subcategory');
        const title = startTopicQuizBtn.getAttribute('data-title');
        startTopicQuiz(topicId, topicName, category, subcategory, title);
        return;
    }

    // Handle title quiz play button in header (show main quiz modal)
    const titleQuizPlayBtn = e.target.closest('#title-quiz-play-btn');
    if (titleQuizPlayBtn) {
        e.preventDefault();
        showTitleQuizModal();
        return;
    }

    // Handle start quiz button from main quiz modal
    const startQuizBtn = e.target.closest('.start-quiz-btn');
    if (startQuizBtn) {
        e.preventDefault();
        const quizId = startQuizBtn.getAttribute('data-quiz-id');
        const category = startQuizBtn.getAttribute('data-category');
        startTitleQuiz(quizId, category);
        return;
    }

    // Check if the clicked element or its parent has the cancel-topic-quiz-btn class
    const cancelTopicQuizBtn = e.target.closest('.cancel-topic-quiz-btn');
    if (cancelTopicQuizBtn) {
        e.preventDefault();
        const modalId = cancelTopicQuizBtn.getAttribute('data-modal-hide');
        if (modalId && modalId.startsWith('topic-quiz-modal-')) {
            // Extract topicId from modal ID
            const topicId = modalId.replace('topic-quiz-modal-', '').replace(/-/g, ' ');
            hideTopicQuizModal(topicId);
        } else {
            // Fallback to original modal handling
            const modal = document.getElementById(modalId);
            if (modal) {
                document.activeElement.blur();
                modal.classList.add('hidden');
                modal.setAttribute('aria-hidden', 'true');
                modal.removeAttribute('tabindex');
                document.body.classList.remove('overflow-hidden');
            }
        }
    }

    // Handle modal close buttons (generic data-modal-hide)
    const modalCloseBtn = e.target.closest('[data-modal-hide]');
    if (modalCloseBtn && !cancelTopicQuizBtn) {  // Don't double-handle if already handled by cancel button
        e.preventDefault();
        const modalId = modalCloseBtn.getAttribute('data-modal-hide');
        if (modalId && modalId.startsWith('quiz-modal-')) {
            hideTitleQuizModal();
        } else if (modalId && modalId.startsWith('topic-quiz-modal-')) {
            // Extract topicId from modal ID
            const topicId = modalId.replace('topic-quiz-modal-', '').replace(/-/g, ' ');
            hideTopicQuizModal(topicId);
        } else {
            const modal = document.getElementById(modalId);
            if (modal) {
                document.activeElement.blur();
                modal.classList.add('hidden');
                modal.setAttribute('aria-hidden', 'true');
                modal.removeAttribute('tabindex');
                document.body.classList.remove('overflow-hidden');
            }
        }
    }

    // Handle cancel quiz button
    const cancelQuizBtn = e.target.closest('.cancel-quiz-btn');
    if (cancelQuizBtn) {
        e.preventDefault();
        hideTitleQuizModal();
    }

    // Handle difficulty selection (radio buttons) for both topic and title quizzes
    const difficultyLabel = e.target.closest('label[for]');
    if (difficultyLabel) {
        const radioId = difficultyLabel.getAttribute('for');
        const radioInput = document.getElementById(radioId);

        if (radioInput && radioInput.classList.contains('difficulty-radio')) {
            // Check the radio button
            radioInput.checked = true;

            // Update visual styling for topic quizzes
            if (radioId.includes('topic-')) {
                updateTopicDifficultySelection(radioInput);
            } else {
                // Handle main quiz difficulty selection
                updateMainQuizDifficultySelection(radioInput);
            }
        }
    }
});

// Function to update difficulty selection styling for topics
function updateTopicDifficultySelection(selectedRadio) {
    // Get the topic ID from the radio button ID
    const radioId = selectedRadio.id;
    const topicId = radioId.replace(/^topic-(easy|medium|hard|mixed)-/, '');

    // Get all difficulty radios in the same group (same topic)
    const allRadios = document.querySelectorAll(`input[name="topic-difficulty-${topicId}"]`);

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
    // Check if it's any range input for topics
    if (e.target.type === 'range' && e.target.id.includes('topic-questions-range-')) {
        const topicId = e.target.getAttribute('data-quiz-id');
        const value = e.target.value;

        // Update time estimator
        updateTopicTimeEstimator(topicId, value);

        // Update slider value display
        updateTopicSliderValueDisplay(e.target, topicId, value);
    }

    // Check if it's a range input for main quiz
    if (e.target.type === 'range' && e.target.id.includes('questions-range-')) {
        const titleId = e.target.getAttribute('data-quiz-id');
        const value = e.target.value;

        // Update time estimator for main quiz
        updateMainQuizTimeEstimator(titleId, value);

        // Update slider value display for main quiz
        updateMainQuizSliderValueDisplay(e.target, titleId, value);
    }
});

// Function to update slider value display for topics
function updateTopicSliderValueDisplay(slider, topicId, value) {
    const valueDisplay = document.getElementById('slider-value-' + topicId);

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


// Play topic quiz function - shows the topic quiz configuration modal
function playTopicQuiz(topicId, topicName) {
    // Validate inputs
    if (!topicId) {
        alert('Error: Topic ID is missing. Cannot start quiz.');
        return;
    }

    // Show the topic quiz modal
    showTopicQuizModal(topicId);
}

// Function to show topic quiz modal
function showTopicQuizModal(topicId) {
    // Clean the topicId for use in DOM ID (same as Thymeleaf replaceAll('[^a-zA-Z0-9]', '-'))
    const cleanTopicId = topicId.replace(/[^a-zA-Z0-9]/g, '-');
    const modalId = 'topic-quiz-modal-' + cleanTopicId;
    const modal = document.getElementById(modalId);

    if (modal) {
        modal.classList.remove('hidden');
        modal.setAttribute('aria-hidden', 'false');
        modal.setAttribute('tabindex', '0');
        document.body.classList.add('overflow-hidden');

        // Focus on modal for accessibility
        modal.focus();

        // Add modal backdrop click handler
        modal.addEventListener('click', function (e) {
            if (e.target === modal) {
                hideTopicQuizModal(topicId);
            }
        });

        // Add escape key handler
        const escapeHandler = function (e) {
            if (e.key === 'Escape') {
                hideTopicQuizModal(topicId);
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
    } else {
        // Fallback: start quiz immediately with default settings
        startTopicQuizDirectly(topicId);
    }
}

// Function to hide topic quiz modal
function hideTopicQuizModal(topicId) {
    const cleanTopicId = topicId.replace(/[^a-zA-Z0-9]/g, '-');
    const modalId = 'topic-quiz-modal-' + cleanTopicId;
    const modal = document.getElementById(modalId);

    if (modal) {
        modal.classList.add('hidden');
        modal.setAttribute('aria-hidden', 'true');
        modal.removeAttribute('tabindex');
        document.body.classList.remove('overflow-hidden');
    }
}

// Fallback function to start quiz directly without modal
function startTopicQuizDirectly(topicId) {
    // Create form to submit POST request to the playTopicQuiz endpoint
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/quiz/play/${encodeURIComponent(topicId)}`;

    // Submit form immediately - uses all available questions with mixed difficulty
    document.body.appendChild(form);
    form.submit();
}

// Topic quiz start function (with modal configuration) - calls the startQuiz endpoint
function startTopicQuiz(topicId, topicName, category, subcategory, title) {
    // Get selected difficulty
    const selectedDifficulty = document.querySelector(`input[name="topic-difficulty-${topicId.replace(/[^a-zA-Z0-9]/g, '-')}"]:checked`);
    const difficulty = selectedDifficulty ? selectedDifficulty.getAttribute('data-difficulty') : 'mixed';

    // Get selected question count
    const rangeSlider = document.getElementById(`topic-questions-range-${topicId.replace(/[^a-zA-Z0-9]/g, '-')}`);
    const questionCount = rangeSlider ? parseInt(rangeSlider.value) : 0;

    // Create form to submit POST request to the startQuiz endpoint
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/quiz/start/${encodeURIComponent(topicId)}`;

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




