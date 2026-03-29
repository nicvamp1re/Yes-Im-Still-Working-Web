// --- DOM ELEMENTS ---
const volSlider = document.getElementById('volumeSlider');
const volValue = document.getElementById('volValue');
const audio = document.getElementById('myAudio');
const status = document.getElementById('status');
const btn = document.getElementById('actionBtn');
const minInput = document.getElementById('min');
const maxInput = document.getElementById('max');

// --- STATE VARIABLES ---
let timerId = null;
let isRunning = false;
let lastConfig = "";

// --- COOKIE MANAGEMENT ---
function saveSettings() {
    const d = new Date();
    d.setTime(d.getTime() + (7 * 24 * 60 * 60 * 1000));
    let expires = "expires=" + d.toUTCString();

    document.cookie = `minTime=${minInput.value};${expires};path=/`;
    document.cookie = `maxTime=${maxInput.value};${expires};path=/`;
    document.cookie = `volume=${volSlider.value};${expires};path=/`; // Save volume
}

function loadSettings() {
    const cookies = document.cookie.split('; ');
    cookies.forEach(c => {
        const [name, value] = c.split('=');
        if (name === 'minTime') minInput.value = value;
        if (name === 'maxTime') maxInput.value = value;
        if (name === 'volume') {
            volSlider.value = value;
            audio.volume = value; // Apply to audio object
            volValue.innerText = Math.round(value * 100) + "%";
        }
    });
}

// --- INPUT VALIDATION ---
// Ensures only positive integers and that Max >= Min
document.querySelectorAll(".input").forEach(input => {
    input.addEventListener("change", function () {
        let val = parseInt(this.value, 10);

        if (isNaN(val) || val < 0) val = 0;
        this.value = val;

        // Logic check: prevent Max being smaller than Min
        const minVal = parseInt(minInput.value, 10);
        const maxVal = parseInt(maxInput.value, 10);

        if (maxVal < minVal) {
            maxInput.value = minVal;
        }

        saveSettings();
    });
});

// --- CORE LOGIC ---
function getIntervalConfig() {
    return minInput.value + "-" + maxInput.value;
}

function playRandomly() {
    if (!isRunning) return;
    const minSec = parseInt(minInput.value, 10);
    const maxSec = parseInt(maxInput.value, 10);
    const randomSecond = Math.floor(Math.random() * (maxSec - minSec + 1) + minSec);
    const delayMs = randomSecond * 1000;
    status.innerText = `Status: Running with intervals between ${minSec} and ${maxSec}`;

    timerId = setTimeout(() => {
        // Reset and play
        audio.pause();
        audio.currentTime = 0;
        audio.play();

        // Loop the logic
        if (isRunning) playRandomly();
    }, delayMs);
}

// --- BUTTON EVENT ---
btn.addEventListener('click', () => {
    const currentConfig = getIntervalConfig();

    // STOP Logic: Running and settings are the same
    if (isRunning && currentConfig === lastConfig) {
        clearTimeout(timerId);
        isRunning = false;
        btn.innerText = "Start";
        btn.style.backgroundColor = ""; // Resets to CSS default
        status.innerText = "Status: Stopped.";
    } 
    // START/UPDATE Logic: Either stopped or settings changed
    else {
        clearTimeout(timerId); // Kill any existing timer
        lastConfig = currentConfig;
        isRunning = true;
        btn.innerText = "Stop/Update";
        btn.style.backgroundColor = "#721c24"; // Visual cue for "Running"
        playRandomly();
        saveSettings();
    }
});
volSlider.addEventListener('input', function() {
    const val = this.value;
    audio.volume = val; // Set the actual audio volume
    volValue.innerText = Math.round(val * 100) + "%";
    saveSettings();
});
// Initialize on Load
loadSettings();