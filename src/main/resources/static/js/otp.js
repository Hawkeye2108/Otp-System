// Get DOM elements
const generateSection = document.getElementById('generateSection');
const verifySection = document.getElementById('verifySection');
const successSection = document.getElementById('successSection');

const generateForm = document.getElementById('generateForm');
const verifyForm = document.getElementById('verifyForm');

const emailInput = document.getElementById('email');
const otpInput = document.getElementById('otp');

const generateBtn = document.getElementById('generateBtn');
const verifyBtn = document.getElementById('verifyBtn');
const backBtn = document.getElementById('backBtn');
const resetBtn = document.getElementById('resetBtn');

const generateMessage = document.getElementById('generateMessage');
const verifyMessage = document.getElementById('verifyMessage');

const displayEmail = document.getElementById('displayEmail');
const validityTimer = document.getElementById('validityTimer');

let currentEmail = '';
let validityCountdown;

// Generate OTP Form Submit
generateForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = emailInput.value.trim();

    if (!email) {
        showMessage(generateMessage, 'Please enter a valid email address', 'error');
        return;
    }

    // Disable button and show loading
    setButtonLoading(generateBtn, true);
    hideMessage(generateMessage);

    try {
        const response = await fetch('/api/otp/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: email })
        });

        const data = await response.json();

        if (data.success) {
            currentEmail = email;
            showMessage(generateMessage, data.message, 'success');

            // Show OTP data if available (for testing)
            if (data.data) {
                showMessage(generateMessage, data.message + '<br><strong>' + data.data + '</strong>', 'info');
            }

            // Wait 2 seconds then switch to verify section
            setTimeout(() => {
                switchToVerifySection();
            }, 2000);
        } else {
            showMessage(generateMessage, data.message, 'error');
        }
    } catch (error) {
        showMessage(generateMessage, 'Failed to connect to server. Please try again.', 'error');
        console.error('Error:', error);
    } finally {
        setButtonLoading(generateBtn, false);
    }
});

// Verify OTP Form Submit
verifyForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const otp = otpInput.value.trim();

    if (!otp || otp.length !== 6) {
        showMessage(verifyMessage, 'Please enter a valid 6-digit OTP', 'error');
        return;
    }

    setButtonLoading(verifyBtn, true);
    hideMessage(verifyMessage);

    try {
        const response = await fetch('/api/otp/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: currentEmail,
                otp: otp
            })
        });

        const data = await response.json();

        if (data.success) {
            showMessage(verifyMessage, data.message, 'success');

            // Clear timer
            clearInterval(validityCountdown);

            // Wait 1 second then show success section
            setTimeout(() => {
                switchToSuccessSection();
            }, 1000);
        } else {
            showMessage(verifyMessage, data.message, 'error');
        }
    } catch (error) {
        showMessage(verifyMessage, 'Failed to connect to server. Please try again.', 'error');
        console.error('Error:', error);
    } finally {
        setButtonLoading(verifyBtn, false);
    }
});

// Back Button - Go back to generate section
backBtn.addEventListener('click', () => {
    clearInterval(validityCountdown);
    switchToGenerateSection();
});

// Reset Button - Start over
resetBtn.addEventListener('click', () => {
    switchToGenerateSection();
});

// Switch to Verify Section
function switchToVerifySection() {
    generateSection.style.display = 'none';
    verifySection.style.display = 'block';
    successSection.style.display = 'none';

    displayEmail.textContent = currentEmail;
    otpInput.value = '';
    hideMessage(verifyMessage);

    // Reset verify button to enabled state
    verifyBtn.disabled = false;

    // Start validity countdown (5 minutes = 300 seconds)
    startValidityCountdown(300);
}

// Switch to Generate Section
function switchToGenerateSection() {
    generateSection.style.display = 'block';
    verifySection.style.display = 'none';
    successSection.style.display = 'none';

    emailInput.value = '';
    otpInput.value = '';
    currentEmail = '';
    hideMessage(generateMessage);
    hideMessage(verifyMessage);
}

// Switch to Success Section
function switchToSuccessSection() {
    generateSection.style.display = 'none';
    verifySection.style.display = 'none';
    successSection.style.display = 'block';
}

// Start OTP Validity Countdown
function startValidityCountdown(seconds) {
    // Clear any existing countdown
    if (validityCountdown) {
        clearInterval(validityCountdown);
    }

    let remaining = seconds;

    // Update display immediately
    updateValidityDisplay(remaining);

    validityCountdown = setInterval(() => {
        remaining--;

        if (remaining > 0) {
            updateValidityDisplay(remaining);
        } else {
            validityTimer.textContent = 'EXPIRED';
            validityTimer.style.color = '#dc3545';
            clearInterval(validityCountdown);
            showMessage(verifyMessage, 'OTP has expired. Please generate a new one.', 'error');
            verifyBtn.disabled = true;
        }
    }, 1000);
}

// Update Validity Timer Display
function updateValidityDisplay(seconds) {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    validityTimer.textContent = `${minutes}:${secs.toString().padStart(2, '0')}`;

    // Change color based on time remaining
    if (seconds < 60) {
        validityTimer.style.color = '#dc3545'; // Red
    } else if (seconds < 120) {
        validityTimer.style.color = '#ffc107'; // Yellow
    } else {
        validityTimer.style.color = '#667eea'; // Blue
    }
}

// Show Message
function showMessage(element, message, type) {
    element.innerHTML = message;
    element.className = 'message ' + type;
    element.style.display = 'block';
}

// Hide Message
function hideMessage(element) {
    element.style.display = 'none';
}

// Set Button Loading State
function setButtonLoading(button, loading) {
    const btnText = button.querySelector('.btn-text');
    const spinner = button.querySelector('.spinner');

    if (loading) {
        button.disabled = true;
        btnText.style.display = 'none';
        spinner.style.display = 'inline-block';
    } else {
        button.disabled = false;
        btnText.style.display = 'inline';
        spinner.style.display = 'none';
    }
}

// Auto-format OTP input (optional enhancement)
otpInput.addEventListener('input', (e) => {
    // Only allow numbers
    e.target.value = e.target.value.replace(/[^0-9]/g, '');
});