package com.example.server

object DesktopWebPortalHtml {

    fun getPortalHtml(deviceName: String, port: Int): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AirBridge - Offline Backup & Sync</title>
    <style>
        :root {
            --bg-base: #0a0f1d;
            --bg-surface: #111827;
            --bg-card: #1f2937;
            --border-color: #374151;
            --accent-cyan: #00e5ff;
            --accent-cyan-hover: #00b4d8;
            --accent-indigo: #6366f1;
            --accent-emerald: #10b981;
            --accent-amber: #f59e0b;
            --accent-rose: #ef4444;
            --text-primary: #f9fafb;
            --text-secondary: #9ca3af;
            --text-muted: #6b7280;
            --font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }

        [data-theme="light"] {
            --bg-base: #f8fafc;
            --bg-surface: #ffffff;
            --bg-card: #f1f5f9;
            --border-color: #e2e8f0;
            --text-primary: #0f172a;
            --text-secondary: #475569;
            --text-muted: #94a3b8;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: var(--font-family);
            background-color: var(--bg-base);
            color: var(--text-primary);
            line-height: 1.5;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            transition: background-color 0.2s, color 0.2s;
        }

        header {
            background-color: var(--bg-surface);
            border-bottom: 1px solid var(--border-color);
            padding: 1rem 2rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .brand-container {
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }

        .brand-logo {
            width: 36px;
            height: 36px;
            background: linear-gradient(135deg, var(--accent-cyan), var(--accent-indigo));
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            color: #0a0f1d;
            font-size: 1.25rem;
        }

        .brand-title {
            font-size: 1.25rem;
            font-weight: 700;
            letter-spacing: -0.02em;
        }

        .brand-badge {
            font-size: 0.75rem;
            background: rgba(0, 229, 255, 0.15);
            color: var(--accent-cyan);
            padding: 0.2rem 0.5rem;
            border-radius: 9999px;
            font-weight: 600;
            border: 1px solid rgba(0, 229, 255, 0.3);
        }

        .header-actions {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .device-pill {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            background: var(--bg-card);
            padding: 0.4rem 0.8rem;
            border-radius: 9999px;
            font-size: 0.85rem;
            border: 1px solid var(--border-color);
        }

        .dot-indicator {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: var(--accent-emerald);
            box-shadow: 0 0 8px var(--accent-emerald);
        }

        .btn {
            background-color: var(--bg-card);
            color: var(--text-primary);
            border: 1px solid var(--border-color);
            padding: 0.5rem 1rem;
            border-radius: 8px;
            font-size: 0.875rem;
            font-weight: 500;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            transition: all 0.15s ease;
        }

        .btn:hover {
            border-color: var(--text-muted);
            transform: translateY(-1px);
        }

        .btn-primary {
            background: linear-gradient(135deg, var(--accent-cyan), #0099cc);
            color: #0a0f1d;
            border: none;
            font-weight: 600;
        }

        .btn-primary:hover {
            filter: brightness(1.1);
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 2rem;
            width: 100%;
            flex: 1;
        }

        .pairing-overlay {
            position: fixed;
            inset: 0;
            background: rgba(10, 15, 29, 0.95);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
            backdrop-filter: blur(10px);
        }

        .pairing-modal {
            background: var(--bg-surface);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 2.5rem;
            max-width: 440px;
            width: 90%;
            text-align: center;
            box-shadow: 0 20px 40px rgba(0,0,0,0.5);
        }

        .pin-inputs {
            display: flex;
            gap: 0.5rem;
            justify-content: center;
            margin: 1.5rem 0;
        }

        .pin-input {
            width: 44px;
            height: 52px;
            font-size: 1.5rem;
            text-align: center;
            border-radius: 8px;
            border: 2px solid var(--border-color);
            background: var(--bg-card);
            color: var(--text-primary);
            font-weight: 700;
        }

        .pin-input:focus {
            outline: none;
            border-color: var(--accent-cyan);
            box-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
        }

        .hero-sync-card {
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.12), rgba(0, 229, 255, 0.12));
            border: 1px solid rgba(0, 229, 255, 0.3);
            border-radius: 16px;
            padding: 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
            gap: 1.5rem;
            flex-wrap: wrap;
        }

        .hero-info h2 {
            font-size: 1.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .hero-info p {
            color: var(--text-secondary);
            font-size: 0.95rem;
        }

        .hero-actions {
            display: flex;
            gap: 0.75rem;
            flex-wrap: wrap;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 1.25rem;
            margin-bottom: 2rem;
        }

        .stat-card {
            background: var(--bg-surface);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 1.25rem;
        }

        .stat-label {
            font-size: 0.8rem;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 0.5rem;
        }

        .stat-value {
            font-size: 1.5rem;
            font-weight: 700;
        }

        .tabs {
            display: flex;
            gap: 0.5rem;
            border-bottom: 1px solid var(--border-color);
            margin-bottom: 1.5rem;
            overflow-x: auto;
            padding-bottom: 0.5rem;
        }

        .tab-btn {
            background: none;
            border: none;
            color: var(--text-secondary);
            padding: 0.5rem 1rem;
            font-size: 0.9rem;
            font-weight: 500;
            cursor: pointer;
            border-radius: 6px;
            white-space: nowrap;
        }

        .tab-btn.active {
            background: var(--bg-card);
            color: var(--accent-cyan);
            font-weight: 600;
        }

        .search-row {
            display: flex;
            gap: 1rem;
            margin-bottom: 1.5rem;
            align-items: center;
        }

        .search-input {
            flex: 1;
            background: var(--bg-surface);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 0.6rem 1rem;
            color: var(--text-primary);
            font-size: 0.9rem;
        }

        .search-input:focus {
            outline: none;
            border-color: var(--accent-cyan);
        }

        .files-table {
            width: 100%;
            border-collapse: collapse;
            background: var(--bg-surface);
            border-radius: 12px;
            overflow: hidden;
            border: 1px solid var(--border-color);
        }

        .files-table th, .files-table td {
            padding: 0.85rem 1rem;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
            font-size: 0.9rem;
        }

        .files-table th {
            background: var(--bg-card);
            color: var(--text-secondary);
            font-weight: 600;
            font-size: 0.8rem;
            text-transform: uppercase;
        }

        .files-table tr:hover td {
            background: rgba(255, 255, 255, 0.02);
        }

        .badge {
            display: inline-block;
            padding: 0.2rem 0.5rem;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: 600;
        }

        .badge-synced {
            background: rgba(16, 185, 129, 0.15);
            color: var(--accent-emerald);
        }

        .badge-pending {
            background: rgba(245, 158, 11, 0.15);
            color: var(--accent-amber);
        }

        .badge-new {
            background: rgba(0, 229, 255, 0.15);
            color: var(--accent-cyan);
        }

        .dropzone {
            border: 2px dashed var(--border-color);
            background: rgba(31, 41, 55, 0.3);
            border-radius: 12px;
            padding: 2rem;
            text-align: center;
            margin-top: 2rem;
            cursor: pointer;
            transition: all 0.2s;
        }

        .dropzone.dragover {
            border-color: var(--accent-cyan);
            background: rgba(0, 229, 255, 0.05);
        }

        .progress-hud {
            position: fixed;
            bottom: 2rem;
            right: 2rem;
            background: var(--bg-surface);
            border: 1px solid var(--accent-cyan);
            border-radius: 12px;
            padding: 1.25rem;
            width: 320px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.5);
            display: none;
            z-index: 99;
        }

        .progress-bar-bg {
            background: var(--bg-card);
            border-radius: 6px;
            height: 8px;
            overflow: hidden;
            margin: 0.75rem 0;
        }

        .progress-bar-fill {
            background: linear-gradient(90deg, var(--accent-cyan), var(--accent-indigo));
            height: 100%;
            width: 0%;
            transition: width 0.2s;
        }
    </style>
</head>
<body data-theme="dark">

<div class="pairing-overlay" id="pairingOverlay">
    <div class="pairing-modal">
        <div class="brand-logo" style="margin: 0 auto 1rem; width: 48px; height: 48px;">AB</div>
        <h2>Authenticate Connection</h2>
        <p style="color: var(--text-secondary); font-size: 0.9rem; margin-top: 0.5rem;">
            Enter the 6-digit PIN displayed on your Android device to secure this session.
        </p>
        <div class="pin-inputs">
            <input type="text" maxlength="1" class="pin-input" autofocus data-idx="0">
            <input type="text" maxlength="1" class="pin-input" data-idx="1">
            <input type="text" maxlength="1" class="pin-input" data-idx="2">
            <input type="text" maxlength="1" class="pin-input" data-idx="3">
            <input type="text" maxlength="1" class="pin-input" data-idx="4">
            <input type="text" maxlength="1" class="pin-input" data-idx="5">
        </div>
        <button class="btn btn-primary" style="width: 100%; justify-content: center; padding: 0.75rem;" onclick="submitPin()">
            Connect Securely
        </button>
        <div id="pairError" style="color: var(--accent-rose); font-size: 0.85rem; margin-top: 0.75rem; display: none;">
            Incorrect PIN. Please check the code on your phone screen.
        </div>
    </div>
</div>

<header>
    <div class="brand-container">
        <div class="brand-logo">AB</div>
        <span class="brand-title">AirBridge</span>
        <span class="brand-badge">Direct Offline</span>
    </div>
    <div class="header-actions">
        <div class="device-pill">
            <div class="dot-indicator"></div>
            <span id="phoneName">$deviceName</span>
        </div>
        <button class="btn" onclick="toggleTheme()" title="Toggle Dark/Light">🌓</button>
    </div>
</header>

<main class="container">
    <div class="hero-sync-card">
        <div class="hero-info">
            <h2>Instant Laptop Backup</h2>
            <p id="heroSubtitle">Zero cloud. Full LAN transfer speed with SHA-256 cryptographic verification.</p>
        </div>
        <div class="hero-actions">
            <button class="btn btn-primary" onclick="startIncrementalBackup()">
                ⚡ Incremental Sync (<span id="newFilesCount">0</span> new)
            </button>
            <button class="btn" onclick="downloadAllZip()">
                📦 Download All (.ZIP)
            </button>
        </div>
    </div>

    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-label">Total Files Indexed</div>
            <div class="stat-value" id="statFiles">0</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Total Mobile Media</div>
            <div class="stat-value" id="statMediaSize">0 MB</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">New / Modified</div>
            <div class="stat-value" style="color: var(--accent-cyan);" id="statNewCount">0 files</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Encryption Mode</div>
            <div class="stat-value" style="color: var(--accent-emerald);">AES-256-GCM</div>
        </div>
    </div>

    <div class="tabs">
        <button class="tab-btn active" onclick="switchCategory('ALL', this)">All Files</button>
        <button class="tab-btn" onclick="switchCategory('PHOTOS', this)">📷 Photos</button>
        <button class="tab-btn" onclick="switchCategory('VIDEOS', this)">🎬 Videos</button>
        <button class="tab-btn" onclick="switchCategory('DOCUMENTS', this)">📄 Documents</button>
        <button class="tab-btn" onclick="switchCategory('AUDIO', this)">🎵 Audio</button>
        <button class="tab-btn" onclick="switchCategory('DOWNLOADS', this)">📥 Downloads</button>
    </div>

    <div class="search-row">
        <input type="text" class="search-input" id="searchBox" placeholder="Search by filename or extension..." oninput="filterFiles()">
        <button class="btn" onclick="loadFiles()">🔄 Refresh</button>
    </div>

    <table class="files-table">
        <thead>
            <tr>
                <th>File Name</th>
                <th>Category</th>
                <th>Size</th>
                <th>Status</th>
                <th>Hash (SHA-256)</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody id="filesTableBody">
            <tr><td colspan="6" style="text-align: center; color: var(--text-muted);">Loading files...</td></tr>
        </tbody>
    </table>

    <div class="dropzone" id="dropzone" onclick="document.getElementById('fileUploader').click()">
        <input type="file" id="fileUploader" multiple style="display: none;" onchange="handleFileUpload(this.files)">
        <p style="font-weight: 600; font-size: 1rem; margin-bottom: 0.25rem;">Drag & drop files here from Laptop to Phone</p>
        <p style="color: var(--text-secondary); font-size: 0.85rem;">Or click to browse files to upload directly into Android storage</p>
    </div>
</main>

<div class="progress-hud" id="progressHud">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 600; font-size: 0.9rem;" id="hudTitle">Transferring...</span>
        <span style="font-size: 0.75rem; color: var(--accent-cyan);" id="hudSpeed">0 MB/s</span>
    </div>
    <div class="progress-bar-bg">
        <div class="progress-bar-fill" id="hudBar"></div>
    </div>
    <div style="display: flex; justify-content: space-between; font-size: 0.75rem; color: var(--text-secondary);">
        <span id="hudEta">ETA: calculating</span>
        <span id="hudPercent">0%</span>
    </div>
</div>

<script>
    let authToken = localStorage.getItem("airbridge_token") || "";
    let allFiles = [];
    let currentCategory = 'ALL';

    // PIN inputs navigation
    const inputs = document.querySelectorAll('.pin-input');
    inputs.forEach((input, index) => {
        input.addEventListener('input', (e) => {
            if (e.target.value.length === 1 && index < inputs.length - 1) {
                inputs[index + 1].focus();
            }
        });
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && !e.target.value && index > 0) {
                inputs[index - 1].focus();
            }
        });
    });

    async function checkAuth() {
        if (authToken) {
            try {
                const res = await fetch('/api/status', {
                    headers: { 'Authorization': 'Bearer ' + authToken }
                });
                if (res.ok) {
                    document.getElementById('pairingOverlay').style.display = 'none';
                    loadFiles();
                    startStatusPolling();
                    return;
                }
            } catch(e) {}
        }
        document.getElementById('pairingOverlay').style.display = 'flex';
    }

    async function submitPin() {
        let pin = '';
        inputs.forEach(i => pin += i.value.trim());
        if (pin.length !== 6) return;

        try {
            const res = await fetch('/api/pair', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ pin: pin, deviceName: navigator.userAgent.includes('Mac') ? 'MacBook' : 'Laptop PC' })
            });
            const data = await res.json();
            if (data.success && data.token) {
                authToken = data.token;
                localStorage.setItem("airbridge_token", authToken);
                document.getElementById('pairingOverlay').style.display = 'none';
                loadFiles();
                startStatusPolling();
            } else {
                document.getElementById('pairError').style.display = 'block';
            }
        } catch(e) {
            document.getElementById('pairError').style.display = 'block';
        }
    }

    async function loadFiles() {
        try {
            const res = await fetch('/api/files', {
                headers: { 'Authorization': 'Bearer ' + authToken }
            });
            allFiles = await res.json();
            updateStats();
            renderTable();
        } catch(e) {
            console.error(e);
        }
    }

    function updateStats() {
        document.getElementById('statFiles').innerText = allFiles.length;
        let totalBytes = allFiles.reduce((acc, f) => acc + (f.size || 0), 0);
        document.getElementById('statMediaSize').innerText = (totalBytes / (1024 * 1024)).toFixed(1) + ' MB';
        let newCount = allFiles.filter(f => f.syncStatus !== 'SYNCED').length;
        document.getElementById('statNewCount').innerText = newCount + ' files';
        document.getElementById('newFilesCount').innerText = newCount;
    }

    function switchCategory(cat, btn) {
        currentCategory = cat;
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        renderTable();
    }

    function filterFiles() {
        renderTable();
    }

    function renderTable() {
        const query = document.getElementById('searchBox').value.toLowerCase();
        const tbody = document.getElementById('filesTableBody');
        tbody.innerHTML = '';

        const filtered = allFiles.filter(f => {
            const matchCat = currentCategory === 'ALL' || f.category === currentCategory;
            const matchQuery = !query || f.name.toLowerCase().includes(query);
            return matchCat && matchQuery;
        });

        if (filtered.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No files found</td></tr>';
            return;
        }

        filtered.forEach(f => {
            const tr = document.createElement('tr');
            const sizeStr = (f.size / (1024 * 1024)).toFixed(2) + ' MB';
            let badgeHtml = '<span class="badge badge-synced">Synced</span>';
            if (f.syncStatus === 'NOT_SYNCED') {
                badgeHtml = '<span class="badge badge-new">New</span>';
            } else if (f.syncStatus === 'PENDING_INCREMENTAL') {
                badgeHtml = '<span class="badge badge-pending">Modified</span>';
            }

            const shortHash = f.sha256 ? f.sha256.substring(0, 10) + '...' : 'Available';

            tr.innerHTML = `
                <td style="font-weight: 500;">${'$'}{escapeHtml(f.name)}</td>
                <td><span style="color: var(--text-secondary);">${'$'}{f.category}</span></td>
                <td>${'$'}{sizeStr}</td>
                <td>${'$'}{badgeHtml}</td>
                <td><code style="font-size: 0.75rem; background: var(--bg-card); padding: 0.2rem 0.4rem; border-radius: 4px;">${'$'}{shortHash}</code></td>
                <td>
                    <a href="/api/download?id=${'$'}{encodeURIComponent(f.id)}&token=${'$'}{encodeURIComponent(authToken)}" class="btn" style="padding: 0.25rem 0.6rem; font-size: 0.8rem;" download>
                        ⬇ Download
                    </a>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    function escapeHtml(str) {
        return str.replace(/[&<>"']/g, function(m) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
        });
    }

    function startIncrementalBackup() {
        window.location.href = '/api/backup/bundle?type=incremental&token=' + encodeURIComponent(authToken);
    }

    function downloadAllZip() {
        window.location.href = '/api/backup/bundle?type=all&token=' + encodeURIComponent(authToken);
    }

    // Drag and Drop Upload
    const dropzone = document.getElementById('dropzone');
    ['dragenter', 'dragover'].forEach(name => {
        dropzone.addEventListener(name, (e) => {
            e.preventDefault();
            dropzone.classList.add('dragover');
        });
    });
    ['dragleave', 'drop'].forEach(name => {
        dropzone.addEventListener(name, (e) => {
            e.preventDefault();
            dropzone.classList.remove('dragover');
        });
    });
    dropzone.addEventListener('drop', (e) => {
        const files = e.dataTransfer.files;
        if (files && files.length > 0) handleFileUpload(files);
    });

    async function handleFileUpload(files) {
        if (!files || files.length === 0) return;
        const hud = document.getElementById('progressHud');
        hud.style.display = 'block';

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            document.getElementById('hudTitle').innerText = 'Uploading ' + file.name;

            const formData = new FormData();
            formData.append('file', file);

            const xhr = new XMLHttpRequest();
            xhr.open('POST', '/api/upload', true);
            xhr.setRequestHeader('Authorization', 'Bearer ' + authToken);

            xhr.upload.onprogress = (e) => {
                if (e.lengthComputable) {
                    const percent = Math.round((e.loaded / e.total) * 100);
                    document.getElementById('hudBar').style.width = percent + '%';
                    document.getElementById('hudPercent').innerText = percent + '%';
                    document.getElementById('hudSpeed').innerText = (e.loaded / (1024 * 1024)).toFixed(1) + ' MB';
                }
            };

            await new Promise((resolve) => {
                xhr.onload = () => {
                    resolve();
                };
                xhr.onerror = () => {
                    resolve();
                };
                xhr.send(formData);
            });
        }

        setTimeout(() => {
            hud.style.display = 'none';
            loadFiles();
        }, 1000);
    }

    function toggleTheme() {
        const current = document.body.getAttribute('data-theme');
        const next = current === 'dark' ? 'light' : 'dark';
        document.body.setAttribute('data-theme', next);
    }

    function startStatusPolling() {
        setInterval(async () => {
            try {
                const res = await fetch('/api/status', {
                    headers: { 'Authorization': 'Bearer ' + authToken }
                });
                if (res.ok) {
                    const data = await res.json();
                    if (data.phoneName) document.getElementById('phoneName').innerText = data.phoneName;
                }
            } catch(e) {}
        }, 5000);
    }

    checkAuth();
</script>
</body>
</html>
        """.trimIndent()
    }
}
