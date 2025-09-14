// breadcrumb-truncation.js

document.addEventListener('DOMContentLoaded', function () {
    const breadcrumb = document.getElementById('breadcrumb');

    if (breadcrumb) {
        const elementsToTruncate = breadcrumb.querySelectorAll('#title-breadcrumb span, #topic-breadcrumb span');

        elementsToTruncate.forEach(element => {
            const originalText = element.textContent.trim();
            const words = originalText.split(' ');
            const maxWords = 2; // Keep the first 3 words
            const minLengthForTruncation = 20; // Only truncate if the original text is longer than this

            function truncate() {
                if (originalText.length > minLengthForTruncation && words.length > maxWords) {
                    const truncatedWords = words.slice(0, maxWords);
                    element.textContent = truncatedWords.join(' ') + ' ...';
                } else {
                    element.textContent = originalText;
                }
            }

            truncate();
            window.addEventListener('resize', truncate);
        });
    }
});