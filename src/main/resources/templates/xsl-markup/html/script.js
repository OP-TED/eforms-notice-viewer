function init() {
    // Tab buttons event listeners - find by class names
    const summaryTab = document.querySelector('.summary-tab');
    const mainTab = document.querySelector('.main-tab');

    if (summaryTab) {
        summaryTab.addEventListener('click', function () {
            showTab('summary');
        });
    }

    if (mainTab) {
        mainTab.addEventListener('click', function () {
            showTab('main');
        });
    }

    // Navigation collapse button event listener
    const collapseBtn = document.querySelector('.nav-collapse-btn');
    if (collapseBtn) {
        collapseBtn.addEventListener('click', function () {
            toggleNav(this);
        });
    }

    // Resizable nav logic initialization
    const nav = document.querySelector('.nav');
    const resizer = document.querySelector('.resizer');
    let isResizing = false;

    if (nav && resizer) {
        resizer.addEventListener('mousedown', function (e) {
            if (nav.classList.contains('collapsed')) return;
            isResizing = true;
            document.body.classList.add('noselect');
            document.body.style.cursor = 'ew-resize';
        });

        document.addEventListener('mousemove', function (e) {
            if (!isResizing || nav.classList.contains('collapsed')) return;
            const minWidth = 50;
            const maxWidth = window.innerWidth * 0.5; // 50% of viewport
            const offsetLeft = nav.parentNode.getBoundingClientRect().left;
            let newWidth = e.clientX - offsetLeft;
            if (newWidth < minWidth) newWidth = minWidth;
            if (newWidth > maxWidth) newWidth = maxWidth;
            nav.style.width = newWidth + 'px';
            lastWidth = newWidth;
        });

        document.addEventListener('mouseup', function (e) {
            if (isResizing) {
                isResizing = false;
                document.body.classList.remove('noselect');
                document.body.style.cursor = '';
            }
        });

        // Adjust max-width dynamically if window resizes
        window.addEventListener('resize', function () {
            if (!nav.classList.contains('collapsed')) {
                const maxWidth = window.innerWidth * 0.5;
                if (nav.offsetWidth > maxWidth) {
                    nav.style.width = maxWidth + 'px';
                    lastWidth = maxWidth;
                }
            }
        });
    }
}

function showTab(tab) {
    document.querySelectorAll('.tab-content').forEach(e => e.classList.remove('active'));

    if (tab === 'summary') {
        document.querySelector('.summary-content').classList.add('active');
    } else if (tab === 'main') {
        document.querySelector('.main-content').classList.add('active');
    }

    document.querySelectorAll('.tab-buttons .button').forEach((b, i) => {
        b.classList.toggle('active', (tab === 'summary' && i === 0) || (tab === 'main' && i === 1));
    });
}

// Collapsible and width restore
let lastWidth = 200;
function toggleNav(btn) {
    var nav = btn.parentElement;
    if (!nav.classList.contains('collapsed')) {
        lastWidth = nav.offsetWidth;
        nav.classList.add('collapsed');
        nav.style.width = ''; // Remove inline width
    } else {
        nav.classList.remove('collapsed');
        nav.style.width = lastWidth + 'px';
    }
}

// Nav scroll-to-anchor logic (scrolls .body not window)
function scrollToSection(id) {
    const body = document.querySelector('.body');
    const section = document.querySelector('#' + id);
    if (!section) return;
    // Compute offset of section relative to .body scroll area
    const bodyRect = body.getBoundingClientRect();
    const sectionRect = section.getBoundingClientRect();
    const offset = sectionRect.top - bodyRect.top + body.scrollTop;
    body.scrollTo({ top: offset, behavior: 'smooth' });
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', init);