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
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing quiz...');
    
    // Wait a bit for the language manager to be ready
    function initializeQuiz() {
        console.log('=== INITIALIZING QUIZ ===');
        console.log('window.quizQuestionsData:', window.quizQuestionsData);
        console.log('Type of quizQuestionsData:', typeof window.quizQuestionsData);
        
        // Questions data will be passed from the HTML template
        if (window.quizQuestionsData && window.quizQuestionsData.length > 0) {
            console.log('Quiz data found:', window.quizQuestionsData.length, 'questions');
            console.log('First question sample:', window.quizQuestionsData[0]);
            initializeQuizData(window.quizQuestionsData);
            
            if (questions.length > 0) {
                console.log('Loading first question and starting timer...');
                console.log('Questions array:', questions);
                loadQuestion(0);
                initializeTimer();
            } else {
                console.error('Questions array is empty after initialization');
                const questionText = document.getElementById('questionText');
                questionText.removeAttribute('data-translate');
                if (window.languageManager) {
                    questionText.textContent = window.languageManager.translate('quiz.error') || 'No questions available for this quiz.';
                } else {
                    questionText.textContent = 'No questions available for this quiz.';
                }
            }
        } else {
            console.error('Quiz questions data not found or empty');
            console.log('window.quizQuestionsData is:', window.quizQuestionsData);
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
    }
    
    // Check if language manager is ready, otherwise wait longer
    if (window.languageManager && window.languageManager.currentLanguage) {
        console.log('Language manager ready, initializing quiz...');
        initializeQuiz();
    } else {
        console.log('Language manager not ready, waiting...');
        // Wait longer for language manager to fully load
        let attempts = 0;
        const waitForLanguageManager = () => {
            attempts++;
            if (window.languageManager && window.languageManager.currentLanguage) {
                console.log('Language manager ready after', attempts, 'attempts');
                initializeQuiz();
            } else if (attempts < 50) { // Wait up to 5 seconds
                setTimeout(waitForLanguageManager, 100);
            } else {
                console.warn('Language manager not available, initializing without translations');
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
    
    // Timer control button (embedded in timer circle)
    const timerControl = document.getElementById('timerControl');
    const timerIcon = document.getElementById('timerIcon');
    const timerSvg = document.querySelector('.w-24.h-24');
    
    // Multiple approaches to ensure click detection works
    if (timerControl) {
        timerControl.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleTimer();
        });
    }
    
    if (timerIcon) {
        timerIcon.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleTimer();
        });
    }
    
    // Fallback: listen on entire SVG and detect timer control area
    if (timerSvg) {
        timerSvg.addEventListener('click', function(e) {
            const rect = timerSvg.getBoundingClientRect();
            const centerX = rect.left + rect.width / 2;
            const centerY = rect.top + rect.height / 2;
            const clickX = e.clientX;
            const clickY = e.clientY;
            
            // Check if click is within 20px radius of center (timer control area)
            const distance = Math.sqrt(Math.pow(clickX - centerX, 2) + Math.pow(clickY - centerY, 2));
            if (distance <= 20) {
                toggleTimer();
            }
        });
    }
    
    // Finish quiz button
    const finishQuizBtn = document.getElementById('finishQuizBtn');
    if (finishQuizBtn) {
        finishQuizBtn.addEventListener('click', finishQuiz);
    }
    
    // Explanation toast close button
    const toastCloseBtn = document.querySelector('#explanation-toast button[data-dismiss-target]');
    if (toastCloseBtn) {
        toastCloseBtn.addEventListener('click', function(e) {
            e.preventDefault();
            hideExplanationToast();
        });
    }
}

function loadQuestion(index) {
    if (index >= questions.length) {
        showEvaluation();
        return;
    }

    // Hide explanation toast when loading a new question
    hideExplanationToast();

    const question = questions[index];
    currentQuestionIndex = index;

    console.log('Loading question', index + 1, 'of', questions.length);

    // Update question counter
    const currentQuestionElement = document.getElementById('currentQuestionNum');
    const totalQuestionsElement = document.getElementById('totalQuestions');
    
    if (currentQuestionElement) {
        currentQuestionElement.textContent = index + 1;
        console.log('Updated current question number to:', index + 1);
    } else {
        console.error('Current question number element not found');
    }
    
    if (totalQuestionsElement) {
        totalQuestionsElement.textContent = totalQuestions;
        console.log('Updated total questions to:', totalQuestions);
    } else {
        console.error('Total questions element not found');
    }
    
    // Update question progress circle
    updateQuestionProgress();
    
    // Update question text
    const questionTextElement = document.getElementById('questionText');
    if (questionTextElement) {
        questionTextElement.textContent = question.question;
        // Remove the translate attribute since we're setting actual question text
        questionTextElement.removeAttribute('data-translate');
        console.log('Updated question text:', question.question);
    } else {
        console.error('Question text element not found');
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
                <label class="ml-3 text-gray-900 dark:text-white font-medium cursor-pointer">${option}</label>
            </div>
        `;
        
        // Add click handler
        optionCard.addEventListener('click', function() {
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
        console.log('Action button set to submit mode (keeping original translation span)');
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
    const correctIndex = currentQuestion.correct_answer;
    
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

    // Show explanation in toast near the question
    showExplanationToast(currentQuestion.explanation);

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

function handleActionButton() {
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
    // Hide explanation toast when moving to next question
    hideExplanationToast();
    loadQuestion(currentQuestionIndex + 1);
}

function showExplanationToast(explanation) {
    const toastContainer = document.getElementById('explanation-toast');
    const toastText = document.getElementById('toast-explanation-text');
    
    if (!toastContainer || !toastText) {
        console.error('Explanation toast elements not found');
        return;
    }
    
    // Clear any existing content and translation attributes
    toastText.innerHTML = '';
    toastText.removeAttribute('data-translate');
    
    if (explanation && explanation.trim()) {
        // Set the explanation text
        toastText.textContent = explanation;
        
        // Show the toast with animation
        toastContainer.classList.remove('hidden');
        
        // Add smooth slide-down animation
        toastContainer.style.opacity = '0';
        toastContainer.style.transform = 'translateY(-20px)';
        
        requestAnimationFrame(() => {
            toastContainer.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            toastContainer.style.opacity = '1';
            toastContainer.style.transform = 'translateY(0)';
        });
        
        console.log('Explanation toast shown');
    } else {
        // Set default "no explanation" message with translation
        if (window.languageManager) {
            toastText.textContent = window.languageManager.translate('quiz.no_explanation') || 'No explanation available for this question.';
        } else {
            toastText.innerHTML = '<span data-translate="quiz.no_explanation">No explanation available for this question.</span>';
        }
        
        // Show the toast
        toastContainer.classList.remove('hidden');
        toastContainer.style.opacity = '1';
        toastContainer.style.transform = 'translateY(0)';
        
        console.log('No explanation toast shown');
    }
}

function hideExplanationToast() {
    const toastContainer = document.getElementById('explanation-toast');
    
    if (toastContainer && !toastContainer.classList.contains('hidden')) {
        // Animate out
        toastContainer.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
        toastContainer.style.opacity = '0';
        toastContainer.style.transform = 'translateY(-20px)';
        
        setTimeout(() => {
            toastContainer.classList.add('hidden');
            toastContainer.style.transition = '';
        }, 300);
        
        console.log('Explanation toast hidden');
    }
}

function showEvaluation() {
    stopTimer();
    
    // Hide explanation toast when showing results
    hideExplanationToast();
    
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
                <h4 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">
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
    const questionCircle = document.getElementById('questionProgressCircle');
    
    if (questionCircle && totalQuestions > 0) {
        const progressPercentage = ((currentQuestionIndex + 1) / totalQuestions) * 100;
        const circumference = 2 * Math.PI * 15.9155;
        const strokeDashoffset = circumference - (progressPercentage / 100) * circumference;
        
        questionCircle.style.strokeDasharray = `${circumference} ${circumference}`;
        questionCircle.style.strokeDashoffset = strokeDashoffset;
        
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
        // Redirect to categories page
        window.location.href = '/quiz/categories';
    })
    .catch(error => {
        console.error('Error submitting results:', error);
        // Still redirect on error
        window.location.href = '/quiz/categories';
    });
}

// Timer control function for embedded control
function toggleTimer() {
    const timerIcon = document.getElementById('timerIcon');
    
    if (!timerIcon) {
        return;
    }
    
    if (isTimerPaused) {
        // Resume timer - restart the interval and show pause icon
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
        timerIcon.innerHTML = '<rect x="0.5" y="0.5" width="0.8" height="2" fill="#3B82F6"/><rect x="1.7" y="0.5" width="0.8" height="2" fill="#3B82F6"/>';
    } else {
        // Pause timer - stop the interval and show play icon
        isTimerPaused = true;
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }
        timerIcon.innerHTML = '<polygon points="0.5,0.5 0.5,2.5 2.5,1.5" fill="#3B82F6"/>';
    }
}

// Make toggleTimer globally accessible for onclick attributes
window.toggleTimer = toggleTimer;

// Handle page unload to stop timer
window.addEventListener('beforeunload', function() {
    stopTimer();
});

