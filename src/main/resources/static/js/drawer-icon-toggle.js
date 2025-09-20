document.addEventListener('DOMContentLoaded', () => {
    const drawerBtn = document.getElementById('drawer-btn');
    const drawerBackdrop = document.getElementById('drawer-backdrop');
    const drawerIcon = drawerBtn.querySelector('svg');

    const menuIcon = `<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M1 1h15M1 7h15M1 13h15" />`;
    const closeIcon = `<path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />`;

    const updateDrawerIcon = () => {
        // Check if the drawer is open by looking at its classes
        // Flowbite adds/removes -translate-x-full to show/hide the drawer
        if (drawerBackdrop.classList.contains('-translate-x-full')) {
            // Drawer is closed, show menu icon
            drawerIcon.innerHTML = menuIcon;
        } else {
            // Drawer is open, show close icon
            drawerIcon.innerHTML = closeIcon;
        }
    };

    // Initial icon state
    updateDrawerIcon();

    // Use a MutationObserver to detect changes in the drawer's class list
    // This is more reliable than a click listener due to Flowbite's internal JS
    const observer = new MutationObserver(mutations => {
        mutations.forEach(mutation => {
            if (mutation.attributeName === 'class') {
                updateDrawerIcon();
            }
        });
    });

    if (drawerBackdrop) {
        observer.observe(drawerBackdrop, { attributes: true });
    }
});
