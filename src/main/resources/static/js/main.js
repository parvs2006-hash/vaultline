/**
 * EduTrack Main JavaScript
 * Handles dark theme toggling, clipboard copy, interactive marks auto-calc,
 * attendance batch toggles, and toast notifications.
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Theme Initializer
    initTheme();

    // 2. Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });
});

/**
 * Initializes and toggles Dark / Light mode
 */
function initTheme() {
    const savedTheme = localStorage.getItem('edutrack-theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);

    const toggleBtn = document.getElementById('theme-toggle-btn');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            const current = document.documentElement.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('edutrack-theme', next);
            updateThemeIcon(next);
        });
    }
}

function updateThemeIcon(theme) {
    const icon = document.getElementById('theme-icon');
    if (icon) {
        icon.textContent = theme === 'dark' ? '☀️' : '🌙';
    }
}

/**
 * Copies text to user clipboard and displays a toast notification
 */
function copyToClipboard(text, successMessage = 'Copied to clipboard!') {
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
            showToast(successMessage, 'success');
        }).catch(() => {
            fallbackCopy(text, successMessage);
        });
    } else {
        fallbackCopy(text, successMessage);
    }
}

function fallbackCopy(text, message) {
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.position = "fixed";
    textArea.style.left = "-9999px";
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    try {
        document.execCommand('copy');
        showToast(message, 'success');
    } catch (err) {
        showToast('Failed to copy text', 'danger');
    }
    document.body.removeChild(textArea);
}

/**
 * Displays floating toast notification
 */
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${message}</span> <button style="background:none;border:none;color:#fff;cursor:pointer;margin-left:12px;font-size:16px;" onclick="this.parentElement.remove()">×</button>`;
    
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

/**
 * Toggles password visibility in input fields
 */
function togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    if (input.type === 'password') {
        input.type = 'text';
        if (btn) btn.textContent = '👁️ Hide';
    } else {
        input.type = 'password';
        if (btn) btn.textContent = '👁️ Show';
    }
}

/**
 * Batch toggles attendance checkboxes or radios
 */
function setAllAttendance(isPresent) {
    const radios = document.querySelectorAll(`input[type="radio"][value="${isPresent}"]`);
    radios.forEach(r => {
        r.checked = true;
    });
    showToast(isPresent ? 'Marked all students as Present' : 'Marked all students as Absent', 'info');
}

/**
 * Dynamic calculation of row average in marks spreadsheet table
 */
function updateMarksRow(rowId) {
    const row = document.getElementById(rowId);
    if (!row) return;

    const inputs = row.querySelectorAll('.input-mark');
    let total = 0;
    let count = 0;
    let anyFail = false;

    inputs.forEach(input => {
        const val = input.value.trim();
        if (val !== '' && !isNaN(val)) {
            const mark = parseInt(val, 10);
            if (mark < 40) {
                input.classList.add('fail');
                anyFail = true;
            } else {
                input.classList.remove('fail');
            }
            total += mark;
            count++;
        } else {
            input.classList.remove('fail');
        }
    });

    const totalCell = row.querySelector('.row-total');
    const avgCell = row.querySelector('.row-avg');
    const gradeCell = row.querySelector('.row-grade');

    if (count > 0) {
        const avg = total / count;
        if (totalCell) totalCell.textContent = total;
        if (avgCell) avgCell.textContent = avg.toFixed(1) + '%';
        if (gradeCell) gradeCell.textContent = computeGrade(avg);
    } else {
        if (totalCell) totalCell.textContent = '-';
        if (avgCell) avgCell.textContent = '-';
        if (gradeCell) gradeCell.textContent = '-';
    }
}

function computeGrade(avg) {
    if (avg >= 90) return 'A+';
    if (avg >= 80) return 'A';
    if (avg >= 70) return 'B';
    if (avg >= 60) return 'C';
    if (avg >= 40) return 'D';
    return 'F';
}
