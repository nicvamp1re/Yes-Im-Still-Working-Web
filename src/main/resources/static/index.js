let timerId = null;
    let isRunning = false;
    let lastConfig = "";

    const audio = document.getElementById('myAudio');
    const status = document.getElementById('status');
    const btn = document.getElementById('actionBtn');
    const minInput = document.getElementById('min');
    const maxInput = document.getElementById('max');

    // Saves Cookies
    function saveSettings() {
        // Expire in 7 days
        const d = new Date();
        d.setTime(d.getTime() + (7*24*60*60*1000));
        let expires = "expires="+ d.toUTCString();

        document.cookie = `minTime=${minInput.value};${expires};path=/`;
        document.cookie = `maxTime=${maxInput.value};${expires};path=/`;
    }

    // Loads Cookies (is ran on page load)
    function loadSettings() {
        const cookies = document.cookie.split('; ');
        cookies.forEach(c => {
            const [name, value] = c.split('=');
            if (name === 'minTime') minInput.value = value;
            if (name === 'maxTime') maxInput.value = value;
        });
    }

    // Run on page load
    loadSettings();

    // --- CORE LOGIC ---
    function getIntervalConfig() {
        return minInput.value + "-" + maxInput.value;
    }

    function playRandomly() {
        const min = parseInt(minInput.value) * 1000;
        const max = parseInt(maxInput.value) * 1000;
        const delay = Math.floor(Math.random() * (max - min + 1) + min);

        status.innerText = `Status: Next sound in ${delay/1000}s`;

        timerId = setTimeout(() => {
            audio.play();
            if(isRunning) playRandomly();
        }, delay);
    }

    btn.addEventListener('click', () => {
        const currentConfig = getIntervalConfig();

        if (isRunning && currentConfig === lastConfig) {
            clearTimeout(timerId);
            isRunning = false;
            btn.innerText = "Start";
            btn.className = "stopped";
            status.innerText = "Status: Stopped.";
        } else {
            clearTimeout(timerId);
            lastConfig = currentConfig;
            isRunning = true;
            btn.innerText = "Stop/Update";
            btn.className = "running";
            playRandomly();
            saveSettings(); // Double-check save on start
        }
    });