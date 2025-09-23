class LanguageManager {
    constructor() {
        this.supportedLanguages = ['en', 'hu', 'vi'];
        this.defaultLanguage = 'en';
        this.currentLanguage = this.detectLanguage();
        this.translations = {};
        this.translationVersion = "1.0.0"; // Increment this version to bust cache for translation files
        
        this.init();
    }

    detectLanguage() {
        // Check for saved preference first
        const savedLanguage = localStorage.getItem('quiz-app-language');
        if (savedLanguage && this.supportedLanguages.includes(savedLanguage)) {
            return savedLanguage;
        }

        // Detect from browser settings
        const browserLanguage = navigator.language || navigator.languages[0] || this.defaultLanguage;
        const languageCode = browserLanguage.toLowerCase().split('-')[0];
        
        // Return detected language if supported, otherwise default
        return this.supportedLanguages.includes(languageCode) ? languageCode : this.defaultLanguage;
    }

    async init() {
        await this.loadTranslations();
        this.applyTranslations();
        this.updateLanguageSelector();
        this.updateLanguageButton();
        this.setupEventListeners();
    }

    async loadTranslations() {
        try {
            const response = await fetch(`${window.location.origin}/js/translations/${this.currentLanguage}.json?v=${this.translationVersion}`);
            if (response.ok) {
                this.translations = await response.json();
            } else {
                if (this.currentLanguage !== 'en') {
                    const fallbackResponse = await fetch(`${window.location.origin}/js/translations/en.json?v=${this.translationVersion}`);
                    this.translations = await fallbackResponse.json();
                }
            }
        } catch (error) {
            // Silent fallback to prevent console spam
        }
    }

    translate(key, defaultText = null) {
        return this.translations[key] || defaultText || key;
    }

    async setLanguage(languageCode) {
        if (!this.supportedLanguages.includes(languageCode)) {
            return;
        }

        this.currentLanguage = languageCode;
        localStorage.setItem('quiz-app-language', languageCode);

        await this.loadTranslations();
        this.applyTranslations();
        this.updateLanguageSelector();
        this.updateLanguageButton();

        const dropdown = document.getElementById('language-dropdown-menu');
        if (dropdown) {
            dropdown.classList.add('hidden');
        }
    }

    applyTranslations() {
        // Find all elements with data-translate attribute
        const elements = document.querySelectorAll('[data-translate]');
        elements.forEach(element => {
            const key = element.getAttribute('data-translate');
            const originalText = element.getAttribute('data-original-text') || element.textContent;
            
            // Store original text if not already stored
            if (!element.getAttribute('data-original-text')) {
                element.setAttribute('data-original-text', originalText);
            }
            
            let translatedText = this.translate(key, originalText);
            
            // Handle template strings with {} placeholders
            if (translatedText.includes('{}')) {
                // For category descriptions, extract the category name from the original text
                const categoryMatch = originalText.match(/Explore (\w+) topics/);
                if (categoryMatch) {
                    translatedText = translatedText.replace('{}', categoryMatch[1].toLowerCase());
                }
            }
            
            element.textContent = translatedText;
        });

        // Handle placeholder attributes
        const placeholderElements = document.querySelectorAll('[data-translate-placeholder]');
        placeholderElements.forEach(element => {
            const key = element.getAttribute('data-translate-placeholder');
            const originalPlaceholder = element.getAttribute('data-original-placeholder') || element.placeholder;
            
            if (!element.getAttribute('data-original-placeholder')) {
                element.setAttribute('data-original-placeholder', originalPlaceholder);
            }
            
            element.placeholder = this.translate(key, originalPlaceholder);
        });

        // Handle title attributes
        const titleElements = document.querySelectorAll('[data-translate-title]');
        titleElements.forEach(element => {
            const key = element.getAttribute('data-translate-title');
            const originalTitle = element.getAttribute('data-original-title') || element.title;
            
            if (!element.getAttribute('data-original-title')) {
                element.setAttribute('data-original-title', originalTitle);
            }
            
            element.title = this.translate(key, originalTitle);
        });
    }

    updateLanguageSelector() {
        const selector = document.getElementById('language-selector');
        if (selector) {
            selector.value = this.currentLanguage;
        }
    }

    setupEventListeners() {
        const selector = document.getElementById('language-selector');
        if (selector) {
            selector.addEventListener('change', (event) => {
                this.setLanguage(event.target.value);
            });
        }

        // Handle new language option buttons in user dropdown
        const languageButtons = document.querySelectorAll('.language-option');
        languageButtons.forEach(button => {
            button.addEventListener('click', (event) => {
                event.preventDefault();
                const lang = button.getAttribute('data-lang');
                if (lang) {
                    this.setLanguage(lang);
                }
            });
        });

        // Handle language submenu toggle
        const submenuToggle = document.getElementById('language-submenu-toggle');
        const submenu = document.getElementById('language-submenu');
        const arrow = document.getElementById('language-submenu-arrow');
        
        if (submenuToggle && submenu && arrow) {
            submenuToggle.addEventListener('click', (event) => {
                event.preventDefault();
                const isExpanded = submenuToggle.getAttribute('aria-expanded') === 'true';
                
                if (isExpanded) {
                    submenu.classList.add('hidden');
                    submenuToggle.setAttribute('aria-expanded', 'false');
                    arrow.style.transform = 'rotate(0deg)';
                } else {
                    submenu.classList.remove('hidden');
                    submenuToggle.setAttribute('aria-expanded', 'true');
                    arrow.style.transform = 'rotate(90deg)';
                }
            });
        }
        
        // Listen for modal open events to translate modal content
        document.addEventListener('click', (event) => {
            if (event.target.matches('[data-modal-toggle]') || event.target.closest('[data-modal-toggle]')) {
                // Delay to allow modal to be shown first
                setTimeout(() => {
                    this.applyTranslations();
                }, 100);
            }
        });
        
        // Listen for dynamically added content
        const observer = new MutationObserver((mutations) => {
            let shouldTranslate = false;
            mutations.forEach((mutation) => {
                if (mutation.type === 'childList') {
                    mutation.addedNodes.forEach((node) => {
                        if (node.nodeType === Node.ELEMENT_NODE) {
                            if (node.matches('[data-translate]') || node.querySelector('[data-translate]')) {
                                shouldTranslate = true;
                            }
                        }
                    });
                }
            });
            if (shouldTranslate) {
                setTimeout(() => this.applyTranslations(), 50);
            }
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }
    

    getLanguageInfo() {
        const languageNames = {
            en: "English",
            hu: "Magyar",
            vi: "Tiếng Việt"
        };
        return {
            code: this.currentLanguage,
            name: languageNames[this.currentLanguage] || this.currentLanguage
        };
    }

    updateLanguageButton() {
        const languageInfo = this.getLanguageInfo();
        const flagElement = document.getElementById('selected-language-flag');
        const textElement = document.getElementById('language-span'); // Corrected ID

        if (flagElement && textElement) {
            const flagSVGs = {
                en: `
                    <svg class="w-5 h-5 me-2 rounded-full" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 60 30">
                        <clipPath id="a"><path d="M0 0v30h60V0z"/></clipPath>
                        <clipPath id="b"><path d="M30 15h30v15zV15H0zH0V0h30z"/></clipPath>
                        <g clip-path="url(#a)">
                            <path d="M0 0v30h60V0z" fill="#00247d"/>
                            <path d="M0 0l60 30M0 30L60 0" stroke="#fff" stroke-width="6"/>
                            <path d="M0 0l60 30M0 30L60 0" clip-path="url(#b)" stroke="#cf142b" stroke-width="4"/>
                            <path d="M30 0v30M0 15h60" stroke="#fff" stroke-width="10"/>
                            <path d="M30 0v30M0 15h60" stroke="#cf142b" stroke-width="6"/>
                        </g>
                    </svg>
                `,
                hu: `
                    <svg class="w-5 h-5 me-2 rounded-full" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 600">
                        <path fill="#CE2939" d="M0 0h900v200H0z"/>
                        <path fill="#FFF" d="M0 200h900v200H0z"/>
                        <path fill="#477050" d="M0 400h900v200H0z"/>
                    </svg>
                `,
                vi: `
                    <svg class="w-5 h-5 me-2 rounded-full" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 400">
                        <path fill="#da251d" d="M0 0h600v400H0z"/>
                        <path fill="#ff0" d="M300 117.4l56.4 173.2-147.7-107h182.6L243.6 290.6z"/>
                    </svg>
                `
            };

            flagElement.innerHTML = flagSVGs[languageInfo.code] || flagSVGs['en'];
            textElement.textContent = languageInfo.name;
        }
    }
}

// Initialize language manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.languageManager = new LanguageManager();
});

// Helper function for use in templates
function t(key, defaultText = null) {
    return window.languageManager ? window.languageManager.translate(key, defaultText) : (defaultText || key);
}