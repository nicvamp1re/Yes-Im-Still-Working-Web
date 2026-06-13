// Global configuration variable helper
const API_BASE = "/api/patches";

// Helper to extract query parameters from URL (e.g., ?id=5)
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

// ==========================================
// HOME PAGE LOGIC
// ==========================================

// Trigger parsing/scraping from Klei Forums
async function syncPatches() {
    const btn = document.getElementById('actionBtn');
    btn.innerText = "Syncing...";
    try {
        await fetch(`${API_BASE}/sync`);
        alert("Synchronization finished successfully!");
        loadTimer();
        loadPatchList();
    } catch (err) {
        console.error("Sync error:", err);
    } finally {
        btn.innerText = "Sync Forums";
    }
}

// Fetch and calculate days since last patch
async function loadTimer() {
    const status = document.getElementById('status');
    try {
        const response = await fetch(`${API_BASE}/time-since-last`);
        const data = await response.json();

        if (data.daysSince !== undefined && data.daysSince !== -1) {
            status.innerText = `Days since last patch (${data.patchTitle}): ${data.daysSince} days`;
        } else {
            status.innerText = "No patches tracked yet. Hit 'Sync Forums' to parse data.";
        }
    } catch (err) {
        status.innerText = "Failed to load update timeline.";
    }
}

// Fetch all patches and build UI cards
async function loadPatchList() {
    const listContainer = document.getElementById('patchList');
    try {
        const response = await fetch(API_BASE);
        const patches = await response.json();

        listContainer.innerHTML = ""; // Clear loader
        patches.forEach(patch => {
            const card = document.createElement('div');
            card.className = "patch-card";
            card.innerHTML = `
                <h3>${patch.title}</h3>
                <p>Released: ${patch.releaseDate}</p>
                <a href="patch-details.html?id=${patch.id}" class="view-link">View Details & Comments</a>
            `;
            listContainer.appendChild(card);
        });
    } catch (err) {
        listContainer.innerHTML = "<p>Error loading patches from database.</p>";
    }
}

// ==========================================
// DETAILS PAGE LOGIC
// ==========================================

// Load a single patch and its associated comments
async function loadPatchDetails() {
    const patchId = getQueryParam('id');
    if (!patchId) return;

    try {
        // Fetch specific patch details
        const response = await fetch(`${API_BASE}/${patchId}`);
        const patch = await response.json();

      document.getElementById('patchTitle').innerText = patch.title;
      document.getElementById('patchDate').innerText = `Released on: ${patch.releaseDate}`;

      document.getElementById('patchDesc').innerHTML = patch.description;
        // Render Comments
        renderComments(patch.comments);
    } catch (err) {
        console.error("Error fetching patch specifications:", err);
    }
}

function renderComments(comments) {
    const container = document.getElementById('commentsContainer');
    container.innerHTML = "";

    if (!comments || comments.length === 0) {
        container.innerHTML = "<p style='color: #aaa;'>No comments left yet. Be the first!</p>";
        return;
    }

    comments.forEach(comment => {
        const date = new Date(comment.createdAt).toLocaleDateString();
        const element = document.createElement('div');
        element.className = "comment-box";
        element.innerHTML = `
            <div class="comment-header">
                <strong>${comment.username}</strong> <span style="font-size:14px; color:#888;">on ${date}</span>
            </div>
            <p class="comment-text">${comment.text}</p>
        `;
        container.appendChild(element);
    });
}

// Send user comment to database (CRUD: Create)
async function submitComment() {
    const patchId = getQueryParam('id');
    const usernameInput = document.getElementById('username');
    const textInput = document.getElementById('commentText');

    if (!usernameInput.value || !textInput.value) {
        alert("Please complete both form fields!");
        return;
    }

    const payload = {
        username: usernameInput.value,
        text: textInput.value
    };

    try {
        const response = await fetch(`${API_BASE}/${patchId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            textInput.value = ""; // Clear writing pad on success
            loadPatchDetails(); // Refresh view
        } else {
            alert("Could not post comment to server.");
        }
    } catch (err) {
        console.error("Submission failed:", err);
    }
}
// Auth UI State Manager
function updateAuthUI() {
    const loggedInUser = localStorage.getItem("username");
    const loggedOutUI = document.getElementById("loggedOutUI");
    const loggedInUI = document.getElementById("loggedInUI");
    const displayUsername = document.getElementById("displayUsername");

    if (loggedInUser) {
        loggedOutUI.classList.add("d-none");
        loggedInUI.classList.remove("d-none");
        displayUsername.innerText = loggedInUser;
    } else {
        loggedOutUI.classList.remove("d-none");
        loggedInUI.classList.add("d-none");
    }
}

// 1. REGISTER ACCOUNT
function handleRegister() {
    const u = document.getElementById("authUsername").value.trim();
    const p = document.getElementById("authPassword").value.trim();

    if (!u || !p) return alert("Please fill out both credentials fields.");

    fetch("/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: u, password: p })
    })
    .then(res => {
        if (!res.ok) return res.text().then(text => { throw new Error(text || "Registration failed") });
        return res.json();
    })
    .then(data => {
        alert(`Account '${data.username}' created successfully! You can now log in.`);
    })
    .catch(err => alert("Error: " + err.message));
}

// 2. LOG IN
function handleLogin() {
    const u = document.getElementById("authUsername").value.trim();
    const p = document.getElementById("authPassword").value.trim();

    if (!u || !p) return alert("Please fill out both credentials fields.");

    fetch("/api/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: u, password: p })
    })
    .then(res => {
        if (!res.ok) throw new Error("Invalid username or password selection.");
        return res.json();
    })
    .then(data => {
        localStorage.setItem("username", u); // Save client-side visual state
        updateAuthUI();
        // Optional: clear out input fields
        document.getElementById("authUsername").value = "";
        document.getElementById("authPassword").value = "";
    })
    .catch(err => alert("Error: " + err.message));
}

// 3. LOG OUT
function handleLogout() {
    fetch("/api/users/logout")
        .then(() => {
            localStorage.removeItem("username");
            updateAuthUI();
            alert("Logged out successfully.");
        });
}

// 4. UPDATE USERNAME
function promptChangeUsername() {
    const newName = prompt("Enter your new desired username:");
    if (!newName || newName.trim() === "") return;

    fetch(`/api/users/change-username?newUsername=${encodeURIComponent(newName.trim())}`, {
        method: "PUT"
    })
    .then(res => {
        if (!res.ok) throw new Error("Could not update username. Might be taken.");
        return res.json();
    })
    .then(data => {
        localStorage.setItem("username", data.username);
        updateAuthUI();
        alert(`Username successfully updated to: ${data.username}`);
    })
    .catch(err => alert("Error: " + err.message));
}

// 5. DELETE ACCOUNT
function handleDeleteAccount() {
    if (!confirm("CRITICAL WARNING: Are you absolutely sure you want to permanently delete your account? Your comments will be preserved under an anonymous tag.")) return;

    fetch("/api/users/delete-account", {
        method: "DELETE"
    })
    .then(res => {
        if (!res.ok) throw new Error("Account removal handshake rejected.");
        return res.json();
    })
    .then(data => {
        localStorage.removeItem("username");
        updateAuthUI();
        alert(data.message);
    })
    .catch(err => alert("Error: " + err.message));
}

// Run state-check instantly when application dashboard resource triggers
document.addEventListener("DOMContentLoaded", () => {
    updateAuthUI();
});