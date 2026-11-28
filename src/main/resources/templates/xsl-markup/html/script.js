// Three icons of the design system's icon set, Phosphor Regular (MIT licence), drawn from its font as SVG paths on a
// 1024 grid, so that the page needs no font: "caret-down" and "caret-up", the design system's "chevron-down" and
// "chevron-up" of EuiSidebarMenu, and "sidebar-simple".
const PHOSPHOR_CARET_DOWN = 'M855 407 535 727Q530 731 524.5 733.5Q519 736 512 736Q505 736 499.5 733.5Q494 731 489 727L169 407'
    + 'Q165 402 162.5 396.5Q160 391 160 384Q160 371 169.5 361.5Q179 352 192 352Q199 352 204.5 354.5Q210 357 215 361'
    + 'L512 659L809 361Q814 357 819.5 354.5Q825 352 832 352Q845 352 854.5 361.5Q864 371 864 384Q864 391 861.5 396.5'
    + 'Q859 402 855 407Z';
const PHOSPHOR_CARET_UP = 'M855 663Q850 667 844.5 669.5Q839 672 832 672Q825 672 819.5 669.5Q814 667 809 663L512 365L215 663'
    + 'Q210 667 204.5 669.5Q199 672 192 672Q179 672 169.5 662.5Q160 653 160 640Q160 633 162.5 627.5Q165 622 169 617'
    + 'L489 297Q494 293 499.5 290.5Q505 288 512 288Q519 288 524.5 290.5Q530 293 535 297L855 617Q859 622 861.5 627.5'
    + 'Q864 633 864 640Q864 647 861.5 652.5Q859 658 855 663Z';
const PHOSPHOR_SIDEBAR_SIMPLE = 'M864 160H160Q133 160 114.5 178.5Q96 197 96 224V800Q96 827 114.5 845.5Q133 864 160 864H864'
    + 'Q891 864 909.5 845.5Q928 827 928 800V224Q928 197 909.5 178.5Q891 160 864 160ZM160 224H320V800H160ZM864 800H384'
    + 'V224H864Z';

function phosphorIcon(path) {
    const svgNs = 'http://www.w3.org/2000/svg';
    const svg = document.createElementNS(svgNs, 'svg');
    svg.setAttribute('viewBox', '0 0 1024 1024');
    svg.setAttribute('fill', 'currentColor');
    svg.setAttribute('aria-hidden', 'true');
    svg.setAttribute('focusable', 'false');
    const shape = document.createElementNS(svgNs, 'path');
    shape.setAttribute('d', path);
    svg.appendChild(shape);
    return svg;
}

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

    // The navigation follows the design system's EuiSidebarMenu, the menu that MDM uses for the same entries (see
    // style.css). An entry is a heading of the navigation, and its link is the one that section_roles.ftl writes for
    // what the heading itself displays, as the heading's own child; a hyperlink in the text of a line is not an entry.
    // The link goes in a row, with, for an entry with entries under it, a chevron at the end that shows or hides them;
    // they are hidden at first. The chevron points down while they are hidden and up while they are shown, says
    // whether they are shown, and is named "Expand" or "Collapse" and the entry.
    const navigation = document.querySelector('.navigation');
    if (navigation) {
        navigation.setAttribute('role', 'navigation');
        navigation.setAttribute('aria-label', 'Notice contents');
    }
    const navEntries = [];
    document.querySelectorAll('.navigation section.heading').forEach(function (section) {
        const link = section.querySelector(':scope > a[href^="#"]');
        if (!link) return;
        const row = document.createElement('div');
        row.className = 'nav-entry';
        section.insertBefore(row, link);
        row.appendChild(link);
        if (section.querySelector(':scope > section')) {
            section.classList.add('collapsible');
            const name = link.textContent.replace(/\s+/g, ' ').trim();
            const toggle = document.createElement('button');
            toggle.type = 'button';
            toggle.className = 'nav-toggle';
            const showState = function (open) {
                toggle.setAttribute('aria-expanded', String(open));
                toggle.setAttribute('aria-label', (open ? 'Collapse ' : 'Expand ') + name);
                toggle.replaceChildren(phosphorIcon(open ? PHOSPHOR_CARET_UP : PHOSPHOR_CARET_DOWN));
            };
            showState(false);
            toggle.addEventListener('click', function () {
                showState(section.classList.toggle('open'));
                section.dispatchEvent(new CustomEvent('nav-toggle', { bubbles: true }));
            });
            row.appendChild(toggle);
        }
        const target = document.getElementById(link.getAttribute('href').slice(1));
        if (target) navEntries.push({ entry: section, link: link, target: target });
    });

    // The navigation marks the entry of the part of the notice being read, with the class "current": the entry whose
    // target is the nearest above the top quarter of the main area (the deepest one when several are), or, once the
    // main area is scrolled to its end, the nearest above its bottom. When that entry is inside a closed entry, the
    // closed entry takes the mark. An entry that is clicked stays marked until the reader scrolls again, even when its
    // target cannot reach the top. The mark is updated when the main area scrolls or changes size (window resized,
    // navigation folded or resized, tab shown), when an entry is opened or closed, and not while the main area is
    // hidden.
    const main = document.querySelector('.body');
    if (main && navEntries.length > 0) {
        let current = null;
        let read = null;
        let clicked = false;
        const shown = function (item) {
            let closed = null;
            for (let up = item.entry.parentElement; up && !up.classList.contains('navigation'); up = up.parentElement) {
                if (up.classList.contains('collapsible') && !up.classList.contains('open')) closed = up;
            }
            return closed ? navEntries.find(function (other) { return other.entry === closed; }) || item : item;
        };
        const mark = function (item) {
            read = item;
            const marked = shown(item);
            if (marked === current) return;
            if (current) {
                current.entry.classList.remove('current');
                current.link.removeAttribute('aria-current');
            }
            current = marked;
            current.entry.classList.add('current');
            current.link.setAttribute('aria-current', 'location');
        };
        const markWhatIsRead = function () {
            if (main.clientHeight === 0) return;
            const top = main.getBoundingClientRect().top;
            const atEnd = main.scrollTop + main.clientHeight >= main.scrollHeight - 1;
            const limit = atEnd ? main.clientHeight : main.clientHeight / 4;
            let nearest = null;
            let first = null;
            navEntries.forEach(function (item) {
                const offset = item.target.getBoundingClientRect().top - top;
                if (offset <= limit && (!nearest || offset >= nearest.offset)) nearest = { item: item, offset: offset };
                if (!first || offset < first.offset) first = { item: item, offset: offset };
            });
            mark((nearest || first).item);
        };
        navEntries.forEach(function (item) {
            item.link.addEventListener('click', function () {
                clicked = true;
                mark(item);
            });
        });
        document.querySelector('.navigation').addEventListener('nav-toggle', function () {
            if (read) mark(read);
        });
        ['wheel', 'touchstart', 'pointerdown'].forEach(function (type) {
            main.addEventListener(type, function () { clicked = false; }, { passive: true });
        });
        document.addEventListener('keydown', function () { clicked = false; });
        let pending = false;
        const update = function () {
            if (clicked || pending) return;
            pending = true;
            window.requestAnimationFrame(function () {
                pending = false;
                if (!clicked) markWhatIsRead();
            });
        };
        main.addEventListener('scroll', update, { passive: true });
        if (window.ResizeObserver) {
            new ResizeObserver(update).observe(main);
        } else {
            window.addEventListener('resize', update);
        }
        markWhatIsRead();
    }

    // In debug mode, each section and each label also gets a tooltip that says where it comes from: for a section,
    // its XSL template, its style and its levels; for a label, the same for its line, then its key. The tooltips
    // are prepared here and only set while the debug colours are on, so that the page shows no tooltip of its own
    // otherwise.
    const debugTooltips = new Map();
    document.querySelectorAll('section[title]').forEach(function (section) {
        const lines = ['XSL template: ' + section.getAttribute('title')];
        const style = ['heading', 'normal'].find(function (name) { return section.classList.contains(name); });
        if (style) lines.push('Style: ' + style);
        section.classList.forEach(function (name) {
            const nesting = /^nesting-(\d+)$/.exec(name);
            if (nesting) lines.push('Nesting level: ' + nesting[1]);
            const heading = /^heading-(\d+)$/.exec(name);
            if (heading) lines.push('Heading level: ' + heading[1]);
        });
        debugTooltips.set(section, lines.join('\n'));
        section.removeAttribute('title');
    });
    // A browser shows only the tooltip of the innermost element under the mouse, so the tooltip of a label also
    // repeats that of its line, before the label key.
    document.querySelectorAll('[data-label-key]').forEach(function (label) {
        const keys = label.getAttribute('data-label-key').split(/\s+/).filter(Boolean);
        if (keys.length === 0) return;
        const line = label.closest('section');
        const lines = line && debugTooltips.has(line) ? [debugTooltips.get(line)] : [];
        keys.forEach(function (key) { lines.push('Label key: ' + key); });
        debugTooltips.set(label, lines.join('\n'));
    });
    const showDebugTooltips = function (on) {
        debugTooltips.forEach(function (tooltip, element) {
            if (on) {
                element.setAttribute('title', tooltip);
            } else {
                element.removeAttribute('title');
            }
        });
    };

    // The "Debug colours" button shows where each piece of text comes from: its colours (see style.css) and the
    // tooltips above. It is switched on by the button, or by "debug" in the query of the address, as in
    // "notice.html?debug"; the browser remembers the choice made with the button.
    const tabButtons = document.querySelector('.tab-buttons');
    if (tabButtons) {
        const debugToggle = document.createElement('button');
        debugToggle.className = 'debug-toggle';
        debugToggle.type = 'button';
        debugToggle.textContent = 'Debug colours';
        const showDebugColours = function (on) {
            document.body.classList.toggle('debug-colours', on);
            debugToggle.setAttribute('aria-pressed', String(on));
            showDebugTooltips(on);
        };
        let remembered = null;
        try {
            remembered = window.localStorage.getItem('debugColours');
        } catch (e) {
            // No storage, as in a private window: the choice is not remembered
        }
        showDebugColours(new URLSearchParams(window.location.search).has('debug') || remembered === 'on');
        debugToggle.addEventListener('click', function () {
            const on = !document.body.classList.contains('debug-colours');
            showDebugColours(on);
            try {
                window.localStorage.setItem('debugColours', on ? 'on' : 'off');
            } catch (e) {
                // No storage: the choice holds until the page is closed
            }
        });
        tabButtons.appendChild(debugToggle);

        // The key of the colours, under the tab bar; style.css shows it only while the debug colours are on.
        const debugKey = document.createElement('div');
        debugKey.className = 'debug-key';
        [['', 'Colours show where each piece of text comes from:'],
         ['label', 'Label with a fixed key'],
         ['dynamic-label', 'Label of a code'],
         ['value', 'Value'],
         ['text', 'Free text'],
         ['', 'Hover over a line or a label for its details.']].forEach(function (item) {
            const span = document.createElement('span');
            if (item[0]) span.className = item[0];
            span.textContent = item[1];
            debugKey.appendChild(span);
        });
        tabButtons.insertAdjacentElement('afterend', debugKey);
    }

    // The button that folds the navigation away becomes the design system's ghost icon button (see style.css), named
    // after what it does and saying whether the navigation is shown.
    const foldPlace = document.querySelector('.nav-collapse-btn');
    if (foldPlace) {
        const fold = document.createElement('button');
        fold.type = 'button';
        fold.className = foldPlace.className;
        fold.appendChild(phosphorIcon(PHOSPHOR_SIDEBAR_SIMPLE));
        const showFoldState = function (open) {
            fold.setAttribute('aria-expanded', String(open));
            fold.setAttribute('aria-label', open ? 'Hide the notice contents' : 'Show the notice contents');
        };
        showFoldState(true);
        fold.addEventListener('click', function () {
            toggleNav(this);
            showFoldState(!this.parentElement.classList.contains('collapsed'));
        });
        foldPlace.replaceWith(fold);
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