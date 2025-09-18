document.addEventListener('DOMContentLoaded', function () {
    const breadcrumb = document.getElementById('breadcrumb');

    if (breadcrumb) {
        const elementsToTruncate = breadcrumb.querySelectorAll('#title-breadcrumb span, #topic-breadcrumb span');

        elementsToTruncate.forEach(element => {
            const originalText = element.textContent.trim();
            element.setAttribute('title', originalText); // Add tooltip

            function truncate() {
                const breadcrumbWidth = breadcrumb.offsetWidth;
                const parentWidth = element.parentElement.offsetWidth;
                const elementWidth = element.offsetWidth;

                if (elementWidth > parentWidth) {
                    let truncatedText = originalText;
                    while (element.offsetWidth > parentWidth && truncatedText.length > 0) {
                        truncatedText = truncatedText.slice(0, -1);
                        element.textContent = truncatedText + '...';
                    }
                } else {
                    element.textContent = originalText;
                }
            }

            // Initial truncation
            truncate();

            // Re-truncate on window resize
            window.addEventListener('resize', truncate);
        });
    }
});