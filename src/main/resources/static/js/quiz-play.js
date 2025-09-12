// Quiz Player JavaScript
// This file handles the quiz playing functionality

// Global variables
let questions = [];
let totalQuestions = 0;
let currentQuestionIndex = 0;
let selectedAnswers = [];
let correctAnswers = 0;
let wrongAnswers = [];
let quizStartTime = new Date();
let remainingSeconds = 0;
let totalTimeSeconds = 0;
let timerInterval;
let quizResults = {};
let isTimerPaused = false;
let pausedTime = 0;

// Language filter state
let currentLanguageFilter = 'all'; // 'vietnamese', 'hungarian', 'all'
let speedDialMenuJustOpened = false;

// Initialize quiz data from window object (set by Thymeleaf)
function initializeQuizData(questionsData) {
    questions = questionsData || [];
    totalQuestions = questions.length;
    totalTimeSeconds = totalQuestions * 10; // 10 seconds per question

    quizResults = {
        totalQuestions: totalQuestions,
        correctAnswers: 0,
        wrongAnswers: [],
        completedAt: null
    };
}

// Initialize quiz when page loads
document.addEventListener('DOMContentLoaded', function () {

    // Wait a bit for the language manager to be ready
    function initializeQuiz() {

        // Debug logging to understand what data we have
        

        // Questions data will be passed from the HTML template
        if (window.quizQuestionsData && Array.isArray(window.quizQuestionsData) && window.quizQuestionsData.length > 0) {
            initializeQuizData(window.quizQuestionsData);

            if (questions.length > 0) {
                loadQuestion(0);
                initializeTimer();
            } else {
                const questionText = document.getElementById('questionText');
                questionText.removeAttribute('data-translate');
                if (window.languageManager) {
                    questionText.textContent = window.languageManager.translate('quiz.error') || 'No questions available for this quiz.';
                } else {
                    questionText.textContent = 'No questions available for this quiz.';
                }
            }
        } else {
            const questionText = document.getElementById('questionText');
            questionText.removeAttribute('data-translate');
            if (window.languageManager) {
                questionText.textContent = window.languageManager.translate('quiz.error') || 'Error loading quiz data.';
            } else {
                questionText.textContent = 'Error loading quiz data.';
            }
        }

        // Add event listeners for buttons
        setupEventListeners();

        // Setup language filter event listeners - wait for Flowbite to initialize
        setTimeout(() => {
            setupLanguageFilterListeners();
        }, 100);
    }

    // Check if language manager is ready, otherwise wait longer
    if (window.languageManager && window.languageManager.currentLanguage) {
        initializeQuiz();
    } else {
        // Wait longer for language manager to fully load
        let attempts = 0;
        const waitForLanguageManager = () => {
            attempts++;
            if (window.languageManager && window.languageManager.currentLanguage) {
                initializeQuiz();
            } else if (attempts < 50) { // Wait up to 5 seconds
                setTimeout(waitForLanguageManager, 100);
            } else {
                initializeQuiz();
            }
        };
        setTimeout(waitForLanguageManager, 100);
    }
});

function setupEventListeners() {
    // Action button (toggles between submit/next/evaluate)
    const actionBtn = document.getElementById('actionBtn');
    if (actionBtn) {
        actionBtn.addEventListener('click', handleActionButton);
    }

    // Timer is now controlled by onclick attribute on the timer container div

    // Finish quiz button
    const finishQuizBtn = document.getElementById('finishQuizBtn');
    if (finishQuizBtn) {
        finishQuizBtn.addEventListener('click', finishQuiz);
    }

    // Explanation modal event listeners
    const closeModalBtn = document.getElementById('close-explanation-modal');
    const closeModalFooterBtn = document.getElementById('close-explanation-modal-footer');
    const continueQuizBtn = document.getElementById('continue-quiz-btn');

    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', function (e) {
            e.preventDefault();
            hideExplanationModal();
        });
    }

    if (closeModalFooterBtn) {
        closeModalFooterBtn.addEventListener('click', function (e) {
            e.preventDefault();
            hideExplanationModal();
        });
    }

    if (continueQuizBtn) {
        continueQuizBtn.addEventListener('click', function (e) {
            e.preventDefault();
            hideExplanationModal();
            // Auto-advance to next question or show results
            if (currentQuestionIndex < questions.length - 1) {
                nextQuestion();
            } else {
                showEvaluation();
            }
        });
    }
}

function loadQuestion(index) {
    if (index >= questions.length) {
        showEvaluation();
        return;
    }

    // Hide explanation modal when loading a new question
    hideExplanationModal();

    const question = questions[index];
    currentQuestionIndex = index;


    // Update question counter
    const currentQuestionElement = document.getElementById('currentQuestionNum');
    const totalQuestionsElement = document.getElementById('totalQuestions');

    if (currentQuestionElement) {
        currentQuestionElement.textContent = index + 1;
    } else {
    }

    if (totalQuestionsElement) {
        totalQuestionsElement.textContent = totalQuestions;
    } else {
    }

    // Update question progress circle
    updateQuestionProgress();

    // Update question text
    const questionTextElement = document.getElementById('questionText');
    if (questionTextElement) {
        questionTextElement.textContent = question.question;
        // Remove the translate attribute since we're setting actual question text
        questionTextElement.removeAttribute('data-translate');
    } else {
    }

    // Clear previous options
    const optionsContainer = document.getElementById('optionsContainer');
    optionsContainer.innerHTML = '';

    // Create options with Flowbite styling
    question.options.forEach((option, optionIndex) => {
        const optionCard = document.createElement('div');
        optionCard.className = 'option-card cursor-pointer p-4 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-200';
        optionCard.setAttribute('data-option-index', optionIndex);

        optionCard.innerHTML = `
            <div class="flex items-center">
                <input type="radio" name="answer" value="${optionIndex}" class="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600">
                <label class="ml-3 text-sm text-gray-900 dark:text-white font-medium cursor-pointer">${option}</label>
            </div>
        `;

        // Add click handler
        optionCard.addEventListener('click', function () {
            selectOption(optionIndex);
        });

        // Reset pointer events for new question
        optionCard.style.pointerEvents = 'auto';

        optionsContainer.appendChild(optionCard);
    });

    // Set action button to submit mode
    const actionBtn = document.getElementById('actionBtn');
    if (actionBtn) {
        // Don't change the button text on initial load - it already has the correct translation span
        // The template provides: <span data-translate="quiz.submit_answer">Submit Answer</span>
        actionBtn.className = 'px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors duration-200 focus:ring-4 focus:ring-blue-300 dark:focus:ring-blue-800';
        actionBtn.dataset.mode = 'submit';
    }

    // Hide the bottom banner for new questions
    const bottomBanner = document.getElementById('bottom-banner');
    if (bottomBanner) {
        bottomBanner.classList.add('hidden');
    }
}

function selectOption(optionIndex) {
    // Remove previous selection
    document.querySelectorAll('.option-card').forEach(card => {
        card.classList.remove('bg-blue-100', 'dark:bg-blue-900', 'border-blue-500');
        card.classList.add('border-gray-300', 'dark:border-gray-600');
    });

    // Highlight selected option
    const selectedCard = document.querySelector(`[data-option-index="${optionIndex}"]`);
    selectedCard.classList.remove('border-gray-300', 'dark:border-gray-600');
    selectedCard.classList.add('bg-blue-100', 'dark:bg-blue-900', 'border-blue-500');

    // Check the radio button
    const radioButton = selectedCard.querySelector('input[type="radio"]');
    radioButton.checked = true;
}

function submitAnswer() {
    const selectedOption = document.querySelector('input[name="answer"]:checked');

    if (!selectedOption) {
        alert('Please select an answer before submitting.');
        return;
    }

    const selectedIndex = parseInt(selectedOption.value);
    const currentQuestion = questions[currentQuestionIndex];
    const correctIndex = currentQuestion.correctAnswer || currentQuestion.correct_answer;

    selectedAnswers[currentQuestionIndex] = selectedIndex;

    // Show correct/incorrect styling with enhanced visual feedback
    document.querySelectorAll('.option-card').forEach((card, index) => {
        const isSelected = index === selectedIndex;
        const isCorrect = index === correctIndex;

        // Reset all classes first
        card.classList.remove('bg-blue-100', 'dark:bg-blue-900', 'border-blue-500', 'border-gray-300', 'dark:border-gray-600',
            'bg-green-100', 'dark:bg-green-900', 'border-green-500',
            'bg-red-100', 'dark:bg-red-900', 'border-red-500');

        if (isCorrect) {
            // Always highlight the correct answer in green
            card.classList.add('bg-green-100', 'dark:bg-green-900', 'border-green-500', 'border-2');

            // Add a checkmark icon to the correct answer
            const label = card.querySelector('label');
            if (label && !label.querySelector('.correct-icon')) {
                const checkIcon = document.createElement('span');
                checkIcon.className = 'correct-icon ml-2 text-green-600 dark:text-green-400';
                checkIcon.innerHTML = '✓';
                label.appendChild(checkIcon);
            }
        } else if (isSelected) {
            // Highlight the selected wrong answer in red
            card.classList.add('bg-red-100', 'dark:bg-red-900', 'border-red-500', 'border-2');

            // Add an X icon to the wrong answer
            const label = card.querySelector('label');
            if (label && !label.querySelector('.wrong-icon')) {
                const xIcon = document.createElement('span');
                xIcon.className = 'wrong-icon ml-2 text-red-600 dark:text-red-400';
                xIcon.innerHTML = '✗';
                label.appendChild(xIcon);
            }
        } else {
            // Other options remain neutral
            card.classList.add('border-gray-300', 'dark:border-gray-600');
        }

        // Disable clicking on all options after submission
        card.style.pointerEvents = 'none';
    });

    // Update results
    if (selectedIndex === correctIndex) {
        correctAnswers++;
        quizResults.correctAnswers++;
    } else {
        wrongAnswers.push({
            questionIndex: currentQuestionIndex,
            question: currentQuestion,
            selectedAnswer: selectedIndex,
            correctAnswer: correctIndex
        });
        quizResults.wrongAnswers.push({
            questionIndex: currentQuestionIndex,
            question: currentQuestion,
            selectedAnswer: selectedIndex,
            correctAnswer: correctIndex
        });
    }

    // Show explanation in modal only for wrong answers
    if (selectedIndex !== correctIndex) {
        showExplanationModal(currentQuestion.explanation);
    }

    // Change action button to next/evaluate mode
    const actionBtn = document.getElementById('actionBtn');
    if (actionBtn) {
        if (currentQuestionIndex < questions.length - 1) {
            // Create span with translation attribute for Next button
            actionBtn.innerHTML = `<span data-translate="quiz.next">Next</span>`;
            actionBtn.className = 'px-6 py-3 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors duration-200 focus:ring-4 focus:ring-green-300 dark:focus:ring-green-800';
            actionBtn.dataset.mode = 'next';

            // Manually translate the new content if language manager is available
            if (window.languageManager) {
                const span = actionBtn.querySelector('span');
                if (span) {
                    span.textContent = window.languageManager.translate('quiz.next');
                }
            }
        } else {
            // Create span with translation attribute for Finish button
            actionBtn.innerHTML = `<span data-translate="quiz.finish">Finish Quiz</span>`;
            actionBtn.className = 'px-6 py-3 bg-purple-600 hover:bg-purple-700 text-white font-medium rounded-lg transition-colors duration-200 focus:ring-4 focus:ring-purple-300 dark:focus:ring-purple-800';
            actionBtn.dataset.mode = 'evaluate';

            // Manually translate the new content if language manager is available
            if (window.languageManager) {
                const span = actionBtn.querySelector('span');
                if (span) {
                    span.textContent = window.languageManager.translate('quiz.finish');
                }
            }
        }
    }
}

function handleActionButton(event) {
    // Check if the clicked element is related to speed dial - if so, ignore
    if (event && event.target && (
        event.target.id === 'language-speed-dial-btn' ||
        event.target.closest('#language-speed-dial-btn') ||
        event.target.closest('#helper-speed-dial') ||
        event.target.closest('#speed-dial-menu-bottom-right') ||
        event.target.id === 'vietnamese-filter-btn' ||
        event.target.id === 'hungarian-filter-btn' ||
        event.target.id === 'all-filter-btn'
    )) {
        event.preventDefault();
        event.stopPropagation();
        return;
    }

    const actionBtn = document.getElementById('actionBtn');
    const mode = actionBtn.dataset.mode;

    if (mode === 'submit') {
        submitAnswer();
    } else if (mode === 'next') {
        nextQuestion();
    } else if (mode === 'evaluate') {
        showEvaluation();
    }
}

function nextQuestion() {
    // Hide explanation modal when moving to next question
    hideExplanationModal();
    loadQuestion(currentQuestionIndex + 1);
}

function showExplanationModal(explanation) {
    const modal = document.getElementById('explanation-modal');
    const modalText = document.getElementById('modal-explanation-text');
    const speedDial = document.getElementById('helper-speed-dial');

    if (!modal || !modalText) {
        return;
    }

    // Hide speed dial
    if (speedDial) {
        speedDial.classList.add('hidden');
    }

    // Pause the timer when showing explanation modal
    if (!isTimerPaused && timerInterval) {
        isTimerPaused = true;
        clearInterval(timerInterval);
        timerInterval = null;

        // Hide timer display, show play icon overlay
        const timerDisplay = document.getElementById('timerDisplay');
        const timerPlayIcon = document.getElementById('timerPlayIcon');
        if (timerDisplay) timerDisplay.classList.add('hidden');
        if (timerPlayIcon) timerPlayIcon.classList.remove('hidden');
    }

    // Clear any existing content and translation attributes
    modalText.innerHTML = '';
    modalText.removeAttribute('data-translate');

    if (explanation && explanation.trim()) {
        // Set the explanation text
        modalText.textContent = explanation;
    } else {
        // Set default "no explanation" message with translation
        if (window.languageManager) {
            modalText.textContent = window.languageManager.translate('quiz.no_explanation') || 'No explanation available for this question.';
        } else {
            modalText.innerHTML = '<span data-translate="quiz.no_explanation">No explanation available for this question.</span>';
        }
    }

    // Show the modal
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    modal.setAttribute('aria-hidden', 'false');
    document.body.classList.add('overflow-hidden');
}

function hideExplanationModal() {
    const modal = document.getElementById('explanation-modal');
    const speedDial = document.getElementById('helper-speed-dial');

    if (modal && !modal.classList.contains('hidden')) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('overflow-hidden');

        // Show speed dial
        if (speedDial) {
            speedDial.classList.remove('hidden');
        }

        // Resume the timer when hiding explanation modal
        if (isTimerPaused && remainingSeconds > 0) {
            isTimerPaused = false;
            if (!timerInterval) {
                timerInterval = setInterval(() => {
                    remainingSeconds--;
                    updateTimerDisplay();

                    if (remainingSeconds <= 0) {
                        stopTimer();
                        showEvaluation();
                    }
                }, 1000);
            }

            // Show timer display, hide play icon overlay
            const timerDisplay = document.getElementById('timerDisplay');
            const timerPlayIcon = document.getElementById('timerPlayIcon');
            if (timerDisplay) timerDisplay.classList.remove('hidden');
            if (timerPlayIcon) timerPlayIcon.classList.add('hidden');
        }
    }
}

function showEvaluation() {
    stopTimer();

    // Hide explanation modal when showing results
    hideExplanationModal();

    // Hide speed dial
    const speedDial = document.getElementById('helper-speed-dial');
    if (speedDial) {
        speedDial.classList.add('hidden');
    }

    quizResults.completedAt = new Date();

    // Calculate percentage
    const percentage = Math.round((correctAnswers / totalQuestions) * 100);

    // Hide question container
    document.getElementById('questionContainer').classList.add('hidden');

    // Show results container
    document.getElementById('resultsContainer').classList.remove('hidden');

    // Update results display
    document.getElementById('scorePercentage').textContent = percentage + '%';
    document.getElementById('correctAnswers').textContent = correctAnswers;
    document.getElementById('totalQuestionsResult').textContent = totalQuestions;

    // Show wrong answers if any
    if (wrongAnswers.length > 0) {
        displayWrongAnswers();
    } else {
        document.getElementById('wrongAnswersSection').classList.add('hidden');
    }
}

function displayWrongAnswers() {
    const wrongAnswersList = document.getElementById('wrongAnswersList');
    wrongAnswersList.innerHTML = '';

    wrongAnswers.forEach((wrongAnswer) => {
        const wrongAnswerCard = document.createElement('div');
        wrongAnswerCard.className = 'bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-sm p-6';

        wrongAnswerCard.innerHTML = `
            <div class="mb-4">
                <h4 class="text-base font-semibold text-gray-900 dark:text-white mb-2">
                    ${window.languageManager ? window.languageManager.translate('quiz.question_label') : 'Question'} ${wrongAnswer.questionIndex + 1}:
                </h4>
                <p class="text-gray-700 dark:text-gray-300">${wrongAnswer.question.question}</p>
            </div>
            
            <div class="space-y-3">
                <div class="flex items-start space-x-2">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300">
                        ${window.languageManager ? window.languageManager.translate('quiz.your_answer') : 'Your Answer'}
                    </span>
                    <span class="text-gray-700 dark:text-gray-300">${wrongAnswer.question.options[wrongAnswer.selectedAnswer]}</span>
                </div>
                
                <div class="flex items-start space-x-2">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300">
                        ${window.languageManager ? window.languageManager.translate('quiz.correct_answer') : 'Correct Answer'}
                    </span>
                    <span class="text-gray-700 dark:text-gray-300">${wrongAnswer.question.options[wrongAnswer.correctAnswer]}</span>
                </div>
                
                ${wrongAnswer.question.explanation ? `
                <div class="pt-3 border-t border-gray-200 dark:border-gray-700">
                    <h5 class="text-sm font-medium text-gray-900 dark:text-white mb-1">${window.languageManager ? window.languageManager.translate('quiz.explanation') : 'Explanation:'}</h5>
                    <p class="text-sm text-gray-600 dark:text-gray-400">${wrongAnswer.question.explanation}</p>
                </div>
                ` : ''}
            </div>
        `;

        wrongAnswersList.appendChild(wrongAnswerCard);
    });
}

function initializeTimer() {
    remainingSeconds = totalTimeSeconds;
    isTimerPaused = false;
    pausedTime = 0;

    const timerElement = document.getElementById('timerDisplay');
    const timerCircle = document.getElementById('timerProgressCircle');

    if (timerElement && timerCircle) {
        updateTimerDisplay();
        startTimer();
    }
}

function startTimer() {
    if (!timerInterval && !isTimerPaused) {
        timerInterval = setInterval(() => {
            remainingSeconds--;
            updateTimerDisplay();

            if (remainingSeconds <= 0) {
                stopTimer();
                showEvaluation();
            }
        }, 1000);
    }
}

function updateTimerDisplay() {
    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;
    const timeString = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

    const timerElement = document.getElementById('timerDisplay');
    const timerCircle = document.getElementById('timerProgressCircle');

    if (timerElement) {
        timerElement.textContent = timeString;
    }

    // Update timer progress circle
    if (timerCircle) {
        const progressPercentage = (remainingSeconds / totalTimeSeconds) * 100;
        const circumference = 2 * Math.PI * 15.9155;
        const strokeDashoffset = circumference - (progressPercentage / 100) * circumference;

        timerCircle.style.strokeDasharray = `${circumference} ${circumference}`;
        timerCircle.style.strokeDashoffset = strokeDashoffset;

        // Change color based on time remaining
        if (progressPercentage < 20) {
            timerCircle.style.stroke = '#EF4444'; // red
        } else if (progressPercentage < 50) {
            timerCircle.style.stroke = '#F59E0B'; // yellow
        } else {
            timerCircle.style.stroke = '#3B82F6'; // blue
        }
    }
}

function updateQuestionProgress() {
    const progressBar = document.getElementById('progressBar');

    if (progressBar && totalQuestions > 0) {
        const progressPercentage = Math.round(((currentQuestionIndex + 1) / totalQuestions) * 100);
        progressBar.style.width = `${progressPercentage}%`;
        progressBar.textContent = `${progressPercentage}%`;
    }
}

function stopTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
}

function finishQuiz() {
    // Send results to server
    fetch('/quiz/submit-results', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(quizResults)
    })
        .then(response => response.json())
        .then(data => {
            // Extract category and subcategory from current URL to redirect to titles page
            const urlParams = new URLSearchParams(window.location.search);
            const category = urlParams.get('category');
            const topic = urlParams.get('subcategory');
            const titles = window.currentQuizTitle || null;

            if (category && topic) {
                window.location.href = `/quiz/${category}/${topic}/${titles}`;
            } else {
                // Fallback to categories if params not available
                window.location.href = '/quiz/categories';
            }
        })
        .catch(error => {
            // Extract category and subcategory from current URL for error case too
            const urlParams = new URLSearchParams(window.location.search);
            const category = urlParams.get('category');
            const topic = urlParams.get('subcategory');

            if (category && topic) {
                window.location.href = `/quiz/${category}/${topic}/${titles}`;
            } else {
                // Fallback to categories if params not available
                window.location.href = '/quiz/categories';
            }
        });
}

// Timer control function for embedded control
function toggleTimer() {
    const timerDisplay = document.getElementById('timerDisplay');
    const timerPlayIcon = document.getElementById('timerPlayIcon');

    if (isTimerPaused) {
        // Resume timer - restart the interval
        isTimerPaused = false;
        if (!timerInterval) {
            timerInterval = setInterval(() => {
                remainingSeconds--;
                updateTimerDisplay();

                if (remainingSeconds <= 0) {
                    stopTimer();
                    showEvaluation();
                }
            }, 1000);
        }

        // Show timer display, hide play icon overlay
        if (timerDisplay) timerDisplay.classList.remove('hidden');
        if (timerPlayIcon) timerPlayIcon.classList.add('hidden');
    } else {
        // Pause timer - stop the interval
        isTimerPaused = true;
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }

        // Hide timer display, show play icon overlay
        if (timerDisplay) timerDisplay.classList.add('hidden');
        if (timerPlayIcon) timerPlayIcon.classList.remove('hidden');
    }
}

// Make toggleTimer globally accessible for onclick attributes
window.toggleTimer = toggleTimer;

// Handle page unload to stop timer
window.addEventListener('beforeunload', function () {
    stopTimer();
});

// Language Filter Functions
function setupLanguageFilterListeners() {
    // Setup manual toggle for speed dial main button
    const speedDialToggle = document.getElementById('language-speed-dial-btn');
    const speedDialMenu = document.getElementById('speed-dial-menu-bottom-right');
    const helperSpeedDial = document.getElementById('helper-speed-dial');


    if (speedDialToggle && speedDialMenu) {
        // Ensure menu starts hidden - use inline styles to override any CSS conflicts
        speedDialMenu.classList.remove('flex');
        speedDialMenu.classList.add('hidden');
        speedDialMenu.style.display = 'none';
        speedDialMenu.style.pointerEvents = 'none';
        speedDialToggle.setAttribute('aria-expanded', 'false');

        // Simple click handler for speed dial toggle
        speedDialToggle.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            toggleSpeedDialMenu();
        });

    }

    // Language filter buttons
    const vietnameseBtn = document.getElementById('vietnamese-filter-btn');
    const hungarianBtn = document.getElementById('hungarian-filter-btn');
    const allBtn = document.getElementById('all-filter-btn');

    if (vietnameseBtn) {
        vietnameseBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            setLanguageFilter('vietnamese');
        });
    }

    if (hungarianBtn) {
        hungarianBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            setLanguageFilter('hungarian');
        });
    }

    if (allBtn) {
        allBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            setLanguageFilter('all');
        });
    }
}

function toggleSpeedDialMenu() {
    const speedDialMenu = document.getElementById('speed-dial-menu-bottom-right');
    const speedDialToggle = document.getElementById('language-speed-dial-btn');


    if (speedDialMenu && speedDialToggle) {
        const isHidden = speedDialMenu.classList.contains('hidden');
        const computedStyle = window.getComputedStyle(speedDialMenu);
        const isDisplayNone = computedStyle.display === 'none';


        if (isHidden || isDisplayNone) {
            // Show the menu - remove hidden and add flex with inline style
            speedDialMenu.classList.remove('hidden');
            speedDialMenu.classList.add('flex');
            speedDialMenu.style.display = 'flex';
            speedDialMenu.style.pointerEvents = 'auto';
            speedDialToggle.setAttribute('aria-expanded', 'true');

            // Set flag to prevent immediate clicks
            speedDialMenuJustOpened = true;
            setTimeout(() => {
                speedDialMenuJustOpened = false;
            }, 100); // 100ms delay
        } else {
            // Hide the menu - remove flex and add hidden with inline style
            speedDialMenu.classList.remove('flex');
            speedDialMenu.classList.add('hidden');
            speedDialMenu.style.display = 'none';
            speedDialMenu.style.pointerEvents = 'none';
            speedDialToggle.setAttribute('aria-expanded', 'false');
        }
    }
}

function setLanguageFilter(filterType) {

    // Ignore clicks that happen immediately after menu opens
    if (speedDialMenuJustOpened) {
        return;
    }

    currentLanguageFilter = filterType;

    // Update button states
    updateFilterButtonStates();

    // Apply filter to current question and options
    applyLanguageFilter();

    // Hide the speed dial menu after selection
    hideSpeedDialMenu();
}

function hideSpeedDialMenu() {
    const speedDialMenu = document.getElementById('speed-dial-menu-bottom-right');
    const speedDialToggle = document.getElementById('language-speed-dial-btn');

    if (speedDialMenu && speedDialToggle) {
        // Hide the menu - remove flex and add hidden with inline style
        speedDialMenu.classList.remove('flex');
        speedDialMenu.classList.add('hidden');
        speedDialMenu.style.display = 'none';
        speedDialMenu.style.pointerEvents = 'none';
        // Update aria-expanded attribute
        speedDialToggle.setAttribute('aria-expanded', 'false');
    }
}

function updateFilterButtonStates() {
    const buttons = ['vietnamese-filter-btn', 'hungarian-filter-btn', 'all-filter-btn'];

    // Remove active state from all buttons
    buttons.forEach(buttonId => {
        const button = document.getElementById(buttonId);
        if (button) {
            button.classList.remove('ring-2', 'ring-blue-500');
        }
    });

    // Add active state to current filter
    let activeButtonId = 'all-filter-btn'; // default
    if (currentLanguageFilter === 'vietnamese') {
        activeButtonId = 'vietnamese-filter-btn';
    } else if (currentLanguageFilter === 'hungarian') {
        activeButtonId = 'hungarian-filter-btn';
    }

    const activeButton = document.getElementById(activeButtonId);
    if (activeButton) {
        activeButton.classList.add('ring-2', 'ring-blue-500');
    }
}

function applyLanguageFilter() {
    // Apply filter to question text
    const questionTextElement = document.getElementById('questionText');
    if (questionTextElement && questions[currentQuestionIndex]) {
        const filteredQuestion = filterText(questions[currentQuestionIndex].question);
        questionTextElement.textContent = filteredQuestion;
    }

    // Apply filter to answer options
    const optionCards = document.querySelectorAll('.option-card');
    optionCards.forEach((card, index) => {
        const label = card.querySelector('label');
        if (label && questions[currentQuestionIndex] && questions[currentQuestionIndex].options[index]) {
            const filteredOption = filterText(questions[currentQuestionIndex].options[index]);
            // Update the text content, preserving any icons
            const icons = label.querySelectorAll('.correct-icon, .wrong-icon');
            label.innerHTML = filteredOption;
            icons.forEach(icon => label.appendChild(icon));
        }
    });
}

function filterText(text) {
    if (!text) return '';

    switch (currentLanguageFilter) {
        case 'vietnamese':
            // Show only content within parentheses
            const vietnameseMatch = text.match(/\(([^)]+)\)/g);
            if (vietnameseMatch) {
                return vietnameseMatch.map(match => match.slice(1, -1)).join(' ');
            }
            return text; // Return original if no parentheses found

        case 'hungarian':
            // Show only content outside parentheses
            return text.replace(/\s*\([^)]*\)/g, '').trim();

        case 'all':
        default:
            // Show original content
            return text;
    }
}

// Override loadQuestion to apply current filter
const originalLoadQuestion = loadQuestion;
loadQuestion = function (index) {
    // Call original loadQuestion first
    originalLoadQuestion.call(this, index);

    // Then apply current language filter
    setTimeout(() => {
        applyLanguageFilter();
        updateFilterButtonStates();
    }, 50); // Small delay to ensure DOM is updated
};

