// breadcrumb-truncation.js

document.addEventListener('DOMContentLoaded', function() {
    const breadcrumb = document.getElementById('breadcrumb');
    if (breadcrumb) {
        const originalTextElement = breadcrumb.querySelector('#title-breadcrumb'); // Assuming the actual title is inside this span
        if (!originalTextElement) return; // Exit if the element is not found

        const originalText = originalTextElement.textContent.trim();
        const words = originalText.split(' ');
        const maxWords = 3; // Keep the first 3 words
        const minLengthForTruncation = 25; // Only truncate if the original text is longer than this

        function truncateBreadcrumb() {
            // Only truncate if the original text is long enough to warrant it
            if (originalText.length > minLengthForTruncation && words.length > maxWords) {
                const truncatedWords = words.slice(0, maxWords);
                originalTextElement.textContent = truncatedWords.join(' ') + ' ...';
            } else {
                originalTextElement.textContent = originalText; // Ensure original text is shown if it fits or is short
            }
        }

        // Initial truncation
        truncateBreadcrumb();

        // Re-truncate on window resize (for responsive behavior)
        // This might be tricky with word-based truncation, as available space changes.
        // For now, we'll re-apply the word-based truncation.
        window.addEventListener('resize', truncateBreadcrumb);
    }
});