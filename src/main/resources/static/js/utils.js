const API_BASE = 'http://localhost:8080/api';

// ── Auth helpers ──────────────────────────────────────────────────────────────
function getToken() { return localStorage.getItem('baladi_token'); }
function getUser()  { const u = localStorage.getItem('baladi_user'); return u ? JSON.parse(u) : null; }
function setAuth(token, user) {
    localStorage.setItem('baladi_token', token);
    localStorage.setItem('baladi_user', JSON.stringify(user));
}
function logout() {
    localStorage.removeItem('baladi_token');
    localStorage.removeItem('baladi_user');
    window.location.href = '/';
}
function requireAuth()  { if (!getToken()) { window.location.href = '/login.html'; return false; } return true; }
function requireAdmin() {
    const u = getUser();
    if (!u || u.role !== 'ADMIN') { window.location.href = '/'; return false; }
    return true;
}

// ── HTTP helpers ──────────────────────────────────────────────────────────────
async function apiGet(path) {
    const res = await fetch(API_BASE + path, {
        headers: getToken() ? { 'Authorization': 'Bearer ' + getToken() } : {}
    });
    if (res.status === 401) { logout(); return null; }
    return res.json();
}
async function apiPost(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...(getToken() ? { 'Authorization': 'Bearer ' + getToken() } : {})
        },
        body: JSON.stringify(data)
    });
    return { status: res.status, data: await res.json() };
}
async function apiPut(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify(data)
    });
    return { status: res.status, data: await res.json() };
}

// ── Domain constants ──────────────────────────────────────────────────────────
const CAT_ICONS   = { VOIRIE:'fa-road', ELECTRICITE:'fa-bolt', DECHETS:'fa-trash', EAU:'fa-tint', AUTRE:'fa-exclamation-circle' };
const CAT_LABELS  = { VOIRIE:'Voirie', ELECTRICITE:'Électricité', DECHETS:'Déchets', EAU:'Eau', AUTRE:'Autre' };
const STAT_LABELS = { EN_ATTENTE:'En attente', EN_COURS:'En cours', RESOLU:'Résolu' };
const STAT_ICONS  = { EN_ATTENTE:'fa-clock', EN_COURS:'fa-spinner fa-spin', RESOLU:'fa-check-circle' };
const STAT_COLORS = { EN_ATTENTE:'#f59e0b', EN_COURS:'#3b82f6', RESOLU:'#10b981' };

function catBadge(cat) {
    const cls = { VOIRIE:'bg-purple-100 text-purple-700', ELECTRICITE:'bg-yellow-100 text-yellow-700',
                  DECHETS:'bg-green-100 text-green-700', EAU:'bg-sky-100 text-sky-700', AUTRE:'bg-gray-100 text-gray-600' };
    return `<span class="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-lg ${cls[cat]||cls.AUTRE}">
        <i class="fas ${CAT_ICONS[cat]||'fa-tag'}"></i>${CAT_LABELS[cat]||cat}</span>`;
}
function statutBadge(s) {
    const cls = { EN_ATTENTE:'bg-amber-100 text-amber-700', EN_COURS:'bg-blue-100 text-blue-700', RESOLU:'bg-emerald-100 text-emerald-700' };
    return `<span class="inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-xl ${cls[s]||'bg-gray-100 text-gray-600'}">
        <i class="fas ${STAT_ICONS[s]||'fa-circle'}"></i>${STAT_LABELS[s]||s}</span>`;
}
function timeAgo(dateStr) {
    const diff = Math.floor((Date.now() - new Date(dateStr)) / 1000);
    if (diff < 60)   return 'il y a quelques secondes';
    if (diff < 3600) return `il y a ${Math.floor(diff/60)} min`;
    if (diff < 86400) return `il y a ${Math.floor(diff/3600)}h`;
    return `il y a ${Math.floor(diff/86400)} jour(s)`;
}

// ── Shared navbar ─────────────────────────────────────────────────────────────
function renderNavbar(activePage = '') {
    const user = getUser();
    const links = [
        { href:'/', label:'Accueil', id:'' },
        { href:'/#signalements', label:'Signalements', id:'signalements' },
        { href:'/#carte', label:'Carte', id:'carte' },
    ];
    const navLinks = links.map(l =>
        `<a href="${l.href}" class="font-medium transition-colors hover:text-blue-600 ${activePage===l.id?'text-blue-600':'text-gray-600'}">${l.label}</a>`
    ).join('');

    let authSection;
    if (user) {
        const dashHref = user.role === 'ADMIN' ? '/admin.html' : '/dashboard.html';
        authSection = `
            <a href="${dashHref}" class="flex items-center gap-2 text-gray-700 hover:text-blue-600 font-medium transition-colors">
                <span class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-cyan-400 flex items-center justify-center text-white text-sm font-bold">
                    ${user.nom.charAt(0).toUpperCase()}
                </span>
                <span class="hidden sm:block">${user.nom.split(' ')[0]}</span>
            </a>
            <button onclick="logout()" class="flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-red-500 transition-colors px-3 py-2 rounded-xl hover:bg-red-50">
                <i class="fas fa-sign-out-alt"></i><span class="hidden sm:block">Déconnexion</span>
            </button>`;
    } else {
        authSection = `
            <a href="/login.html" class="text-gray-600 hover:text-blue-600 font-medium transition-colors">Se connecter</a>
            <a href="/register.html" class="bg-gradient-to-r from-blue-600 to-cyan-500 text-white font-semibold px-5 py-2.5 rounded-xl hover:shadow-lg hover:shadow-blue-200 transition-all duration-200 hover:-translate-y-0.5">
                <i class="fas fa-user-plus mr-1.5"></i>S'inscrire
            </a>`;
    }
    return `
    <nav class="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-gray-100 shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex items-center justify-between h-16">
                <a href="/" class="flex items-center gap-2.5 group">
                    <div class="w-9 h-9 bg-gradient-to-br from-blue-600 to-cyan-500 rounded-xl flex items-center justify-center shadow-md shadow-blue-200 group-hover:scale-105 transition-transform">
                        <i class="fas fa-city text-white text-sm"></i>
                    </div>
                    <span class="text-xl font-black bg-gradient-to-r from-blue-700 to-cyan-500 bg-clip-text text-transparent">BALADI</span>
                </a>
                <div class="hidden md:flex items-center gap-6">${navLinks}</div>
                <div class="flex items-center gap-3">${authSection}</div>
            </div>
        </div>
    </nav>`;
}
