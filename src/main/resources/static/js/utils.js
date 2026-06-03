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
    try {
        const res = await fetch(API_BASE + path, {
            headers: getToken() ? { 'Authorization': 'Bearer ' + getToken() } : {}
        });
        if (res.status === 401) { 
            if (getToken()) logout(); 
            return null; 
        }
        if (!res.ok) return null;
        const text = await res.text();
        if (!text) return null;
        return JSON.parse(text);
    } catch(e) {
        return null;
    }
}
async function apiPost(path, data, isFormData = false) {
    let options = {
        method: 'POST',
        headers: getToken() ? { 'Authorization': 'Bearer ' + getToken() } : {}
    };
    if (isFormData) {
        options.body = data;
    } else {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(data);
    }
    try {
        const res = await fetch(API_BASE + path, options);
        if (res.status === 401) { 
            if (getToken()) logout(); 
            return { status: 401, data: {} }; 
        }
        const text = await res.text();
        const json = text ? JSON.parse(text) : {};
        return { status: res.status, data: json };
    } catch(e) {
        return { status: 0, data: { erreur: 'Erreur réseau ou serveur inaccessible' } };
    }
}
async function apiPut(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
        body: JSON.stringify(data)
    });
    return { status: res.status, data: await res.json() };
}
async function apiDelete(path) {
    const res = await fetch(API_BASE + path, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + getToken() }
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
    const cls = { 
        VOIRIE:'bg-purple-50 text-purple-700 border-purple-100/80', 
        ELECTRICITE:'bg-amber-50 text-amber-700 border-amber-100/80',
        DECHETS:'bg-emerald-50 text-emerald-700 border-emerald-100/80', 
        EAU:'bg-sky-50 text-sky-700 border-sky-100/80', 
        AUTRE:'bg-slate-50 text-slate-600 border-slate-100/80' 
    };
    return `<span class="inline-flex items-center gap-1.5 text-xs font-semibold px-2.5 py-1 rounded-lg border ${cls[cat]||cls.AUTRE} shadow-sm transition-all duration-300">
        <i class="fas ${CAT_ICONS[cat]||'fa-tag'}"></i>${CAT_LABELS[cat]||cat}</span>`;
}
function statutBadge(s) {
    const cls = { 
        EN_ATTENTE:'bg-amber-50 text-amber-600 border-amber-100/80', 
        EN_COURS:'bg-blue-50 text-blue-600 border-blue-100/80', 
        RESOLU:'bg-emerald-50 text-emerald-600 border-emerald-100/80' 
    };
    return `<span class="inline-flex items-center gap-1.5 text-xs font-bold px-3 py-1.5 rounded-xl border ${cls[s]||'bg-slate-50 text-slate-600 border-slate-100/80'} shadow-sm transition-all duration-300">
        <i class="fas ${STAT_ICONS[s]||'fa-circle'}"></i>${STAT_LABELS[s]||s}</span>`;
}
function timeAgo(dateStr) {
    if (!dateStr) return '—';
    const diff = Math.floor((Date.now() - new Date(dateStr)) / 1000);
    if (diff < 60)   return 'il y a quelques secondes';
    if (diff < 3600) return `il y a ${Math.floor(diff/60)} min`;
    if (diff < 86400) return `il y a ${Math.floor(diff/3600)}h`;
    return `il y a ${Math.floor(diff/86400)} jour(s)`;
}

// ── Shared navbar ─────────────────────────────────────────────────────────────
function renderNavbar(activePage = '') {
    const user = getUser();
    
    let links = [];
    if (!user) {
        links = [
            { href:'/', label:'Accueil', id:'' },
            { href:'/#signalements', label:'Signalements', id:'signalements' },
        ];
    } else if (user.role === 'ADMIN') {
        links = [
            { href:'/admin.html#view-stats', label:'Tableau de Bord', id:'admin-dash' },
            { href:'/admin.html#view-signalements', label:'Signalements', id:'admin-sig' },
            { href:'/admin.html#view-users', label:'Utilisateurs', id:'admin-users' },
            { href:'/admin.html#view-carte', label:'Carte Admin', id:'admin-carte' },
        ];
    } else {
        links = [
            { href:'/', label:'Accueil', id:'' },
            { href:'/dashboard.html', label:'Mon Espace', id:'dashboard' },
            { href:'/#signalements', label:'Signalements', id:'signalements' },
            { href:'/#carte', label:'Carte', id:'carte' },
        ];
    }

    const navLinks = links.map(l =>
        `<a href="${l.href}" class="font-semibold transition-all duration-200 hover:text-blue-600 ${activePage===l.id?'text-blue-600 bg-blue-50/50 px-3 py-1.5 rounded-lg':'text-slate-600 hover:bg-slate-50 px-3 py-1.5 rounded-lg'}">${l.label}</a>`
    ).join('');

    const mobileNavLinks = links.map(l =>
        `<a href="${l.href}" class="block font-semibold py-2.5 px-4 rounded-xl transition-all duration-200 ${activePage===l.id?'text-blue-600 bg-blue-50':'text-slate-600 hover:bg-slate-50'}" onclick="document.getElementById('mobile-menu').classList.add('hidden')">${l.label}</a>`
    ).join('');

    let authSection;
    let mobileAuthSection;
    if (user) {
        const dashHref = user.role === 'ADMIN' ? '/admin.html#view-carte' : '/dashboard.html';
        const profileInitial = user.nom.charAt(0).toUpperCase();
        const profileName = user.nom.split(' ')[0];
        
        authSection = `
            <a href="${dashHref}" class="flex items-center gap-2 text-slate-700 hover:text-blue-600 font-semibold transition-all duration-200">
                <span class="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-600 to-cyan-500 flex items-center justify-center text-white text-sm font-bold shadow-md shadow-blue-100">
                    ${profileInitial}
                </span>
                <span class="hidden sm:block">${profileName}</span>
            </a>
            <button onclick="logout()" class="flex items-center gap-2 text-sm font-semibold text-slate-400 hover:text-red-500 transition-colors px-3 py-2 rounded-xl hover:bg-red-50">
                <i class="fas fa-sign-out-alt"></i><span class="hidden sm:block">Déconnexion</span>
            </button>`;

        mobileAuthSection = `
            <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between gap-3">
                <a href="${dashHref}" class="flex items-center gap-2 text-slate-700 font-bold" onclick="document.getElementById('mobile-menu').classList.add('hidden')">
                    <span class="w-9 h-9 rounded-full bg-gradient-to-tr from-blue-600 to-cyan-500 flex items-center justify-center text-white text-sm font-bold">
                        ${profileInitial}
                    </span>
                    <span>${user.nom}</span>
                </a>
                <button onclick="logout()" class="flex items-center gap-2 text-sm font-bold text-red-500 bg-red-50 px-3 py-2 rounded-xl">
                    <i class="fas fa-sign-out-alt"></i>Déconnexion
                </button>
            </div>`;
    } else {
        authSection = `
            <a href="/login.html" class="text-slate-600 hover:text-blue-600 font-semibold transition-colors">Se connecter</a>
            <a href="/register.html" class="bg-blue-600 text-white font-semibold px-5 py-2.5 rounded-xl hover:bg-blue-700 transition-all duration-200 shadow-md shadow-blue-100 flex items-center gap-1.5">
                <i class="fas fa-user-plus text-xs"></i>S'inscrire
            </a>`;

        mobileAuthSection = `
            <div class="grid grid-cols-2 gap-2 px-4 py-4 border-t border-slate-100">
                <a href="/login.html" class="text-center font-bold text-slate-600 border border-slate-200 py-2.5 rounded-xl hover:bg-slate-50" onclick="document.getElementById('mobile-menu').classList.add('hidden')">Connexion</a>
                <a href="/register.html" class="text-center font-bold text-white bg-blue-600 py-2.5 rounded-xl shadow-md hover:bg-blue-700" onclick="document.getElementById('mobile-menu').classList.add('hidden')">S'inscrire</a>
            </div>`;
    }

    return `
    <nav class="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-100 shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex items-center justify-between h-16">
                <!-- Logo -->
                <a href="/" class="flex items-center gap-2.5 group">
                    <div class="w-9 h-9 bg-gradient-to-tr from-blue-600 to-cyan-500 rounded-xl flex items-center justify-center group-hover:shadow-md transition-all duration-300">
                        <i class="fas fa-city text-white text-sm"></i>
                    </div>
                    <span class="text-xl font-black text-slate-900 tracking-tight">BELEDI</span>
                </a>
                
                <!-- Desktop Nav -->
                <div class="hidden md:flex items-center gap-2">${navLinks}</div>
                <div class="hidden md:flex items-center gap-3">${authSection}</div>

                <!-- Mobile Burger Button -->
                <div class="flex items-center md:hidden">
                    <button onclick="document.getElementById('mobile-menu').classList.toggle('hidden')" aria-label="Ouvrir le menu mobile"
                            class="p-2 rounded-xl text-slate-500 hover:bg-slate-50 focus:outline-none transition-colors">
                        <i class="fas fa-bars text-xl"></i>
                    </button>
                </div>
            </div>
        </div>

        <!-- Mobile Menu -->
        <div id="mobile-menu" class="hidden md:hidden bg-white border-t border-slate-100 absolute top-16 left-0 w-full shadow-lg z-50">
            <div class="px-2 pt-2 pb-3 space-y-1">${mobileNavLinks}</div>
            ${mobileAuthSection}
        </div>
    </nav>`;
}

// ── Interactive Detail Modal with Comments Timeline ───────────────────────────
let globalModalCallback = null;

function renderModalSkeleton() {
    return `
    <div id="detailModalContainer" class="fixed inset-0 z-[60] hidden">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" onclick="closeDetailsModal()"></div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl px-4 my-8">
        <div class="bg-white rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col transition-all transform scale-100">
          
          <!-- Header -->
          <div class="px-6 py-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div>
              <h3 class="text-lg font-bold text-slate-900" id="detailModalTitle">Détails du Signalement</h3>
              <p class="text-xs text-slate-400 mt-0.5" id="detailModalSub"></p>
            </div>
            <button onclick="closeDetailsModal()" class="w-9 h-9 flex items-center justify-center rounded-xl bg-white border border-slate-200 text-slate-400 hover:text-slate-600 hover:shadow-sm transition-all focus:outline-none">
              <i class="fas fa-times"></i>
            </button>
          </div>

          <!-- Body -->
          <div class="overflow-y-auto p-6 space-y-6 flex-1" id="detailModalBody">
            <div class="flex items-center justify-center py-12 text-slate-400">
              <i class="fas fa-spinner fa-spin text-3xl mr-3"></i> Chargement...
            </div>
          </div>

        </div>
      </div>
    </div>
    `;
}

function closeDetailsModal() {
    const el = document.getElementById('detailModalContainer');
    if (el) {
        el.classList.add('hidden');
        document.body.style.overflow = '';
    }
}

async function showSignalementDetails(id, onActionCallback) {
    globalModalCallback = onActionCallback;
    let modalEl = document.getElementById('detailModalContainer');
    if (!modalEl) {
        document.body.insertAdjacentHTML('beforeend', renderModalSkeleton());
        modalEl = document.getElementById('detailModalContainer');
    }
    
    modalEl.classList.remove('hidden');
    document.body.style.overflow = 'hidden';

    const bodyEl = document.getElementById('detailModalBody');
    bodyEl.innerHTML = `
        <div class="flex flex-col items-center justify-center py-20 text-slate-400">
            <i class="fas fa-circle-notch fa-spin text-4xl mb-4 text-blue-500"></i>
            <span class="font-semibold text-sm">Chargement du signalement #${id}...</span>
        </div>
    `;

    try {
        const s = await apiGet('/signalements/' + id);
        if (!s) {
            bodyEl.innerHTML = `
                <div class="text-center py-12 text-red-500">
                    <i class="fas fa-exclamation-triangle text-4xl mb-3"></i>
                    <p class="font-bold">Signalement introuvable ou erreur réseau.</p>
                </div>
            `;
            return;
        }

        document.getElementById('detailModalSub').textContent = `ID: #${s.id} · Créé ${timeAgo(s.dateCreation)}`;
        
        const currentUser = getUser();
        const isOwner = currentUser && s.user && currentUser.email === s.user.email;
        const canDelete = isOwner && s.statut === 'EN_ATTENTE';
        
        let deleteBtnHTML = '';
        if (canDelete) {
            deleteBtnHTML = `
                <button onclick="deleteSignalement(${s.id})" 
                        class="w-full bg-red-50 hover:bg-red-100 border border-red-100 hover:border-red-200 text-red-600 font-bold py-3 px-4 rounded-xl transition-all duration-200 flex items-center justify-center gap-2 text-sm mt-4">
                    <i class="fas fa-trash-alt"></i> Supprimer mon signalement
                </button>
            `;
        }

        const mapBtn = s.latitude && s.longitude ? `
            <a href="https://www.google.com/maps?q=${s.latitude},${s.longitude}" target="_blank" 
               class="inline-flex items-center gap-1.5 text-xs text-blue-500 bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg font-bold border border-blue-100/50 mt-1 transition-colors">
                <i class="fas fa-map-marker-alt text-red-400"></i> ${s.latitude.toFixed(5)}, ${s.longitude.toFixed(5)} · Google Maps
            </a>
        ` : '';

        bodyEl.innerHTML = `
            <div class="space-y-6">
                <!-- Image header -->
                ${s.photoUrl ? `
                    <div class="w-full h-64 rounded-2xl overflow-hidden shadow-sm relative group border border-slate-100">
                        <img src="${s.photoUrl}" class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" alt="Signalement Image">
                        <div class="absolute inset-0 bg-gradient-to-t from-slate-900/40 to-transparent"></div>
                    </div>
                ` : `
                    <div class="w-full h-44 bg-slate-50 border-2 border-dashed border-slate-200 rounded-2xl flex flex-col items-center justify-center text-slate-400">
                        <i class="fas fa-image text-3xl mb-2 opacity-50"></i>
                        <span class="text-xs font-semibold">Aucune image fournie</span>
                    </div>
                `}

                <!-- Badge Row -->
                <div class="flex items-center gap-3">
                    ${catBadge(s.categorie)}
                    ${statutBadge(s.statut)}
                </div>

                <!-- Info Details -->
                <div class="space-y-2">
                    <h4 class="text-xl font-extrabold text-slate-900 leading-snug">${s.titre}</h4>
                    <p class="text-slate-600 leading-relaxed text-sm whitespace-pre-line">${s.description}</p>
                </div>

                <!-- Metadata details -->
                <div class="bg-slate-50/80 rounded-2xl p-4 border border-slate-100 grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                    <div class="space-y-1.5">
                        <span class="text-slate-400 block uppercase tracking-wider font-semibold">Auteur</span>
                        <span class="font-bold text-slate-800 text-sm flex items-center gap-1.5">
                            <i class="fas fa-user-circle text-slate-400 text-base"></i> ${s.user?.nom || 'Anonyme'}
                        </span>
                        <span class="text-slate-400 block">${s.user?.email || ''}</span>
                    </div>
                    <div class="space-y-1.5">
                        <span class="text-slate-400 block uppercase tracking-wider font-semibold">Localisation</span>
                        <span class="font-bold text-slate-800 text-sm block">Nouakchott, Mauritanie</span>
                        ${mapBtn}
                    </div>
                </div>

                ${deleteBtnHTML}

                <!-- Comments Timeline Container -->
                <div class="pt-6 border-t border-slate-100">
                    <div class="flex items-center justify-between mb-4">
                        <h5 class="font-bold text-slate-950 flex items-center gap-2">
                            <i class="fas fa-comments text-blue-500"></i> Commentaires 
                            <span id="commentsCount" class="bg-blue-50 text-blue-600 text-xs px-2 py-0.5 rounded-full font-bold">0</span>
                        </h5>
                    </div>

                    <!-- List of comments -->
                    <div id="modalCommentsList" class="space-y-4 mb-5">
                        <!-- Spinner while comments load -->
                        <div class="flex justify-center py-4"><i class="fas fa-spinner fa-spin text-slate-300"></i></div>
                    </div>

                    <!-- Post Form -->
                    ${currentUser ? `
                        <form id="commentPostForm" onsubmit="submitComment(event, ${s.id})" class="flex gap-3 items-start mt-4">
                            <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-xs font-bold shadow-md shadow-blue-100 mt-1 flex-shrink-0">
                                ${currentUser.nom.charAt(0).toUpperCase()}
                            </div>
                            <div class="flex-1 space-y-2">
                                <textarea id="commentInputText" rows="2" required placeholder="Votre commentaire..." 
                                          class="w-full border border-slate-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-50/50 resize-none bg-slate-50 focus:bg-white transition-all"></textarea>
                                <div class="flex justify-end">
                                    <button type="submit" id="commentSubmitBtn" 
                                            class="bg-blue-600 text-white font-bold text-xs px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors shadow-sm flex items-center gap-1">
                                        <i class="fas fa-paper-plane"></i> Commenter
                                    </button>
                                </div>
                            </div>
                        </form>
                    ` : `
                        <div class="bg-slate-50 border border-dashed border-slate-200 rounded-2xl p-4 text-center text-xs text-slate-400 font-medium">
                            <i class="fas fa-lock mr-1.5 text-slate-300"></i> Connectez-vous pour ajouter un commentaire
                        </div>
                    `}
                </div>
            </div>
        `;

        // Trigger comments load
        loadComments(s.id);

    } catch (e) {
        bodyEl.innerHTML = `<div class="text-center py-12 text-red-500">Erreur : ${e.message}</div>`;
    }
}

async function loadComments(signalementId) {
    const listEl = document.getElementById('modalCommentsList');
    if (!listEl) return;

    try {
        const res = await apiGet(`/signalements/${signalementId}/commentaires`);
        if (!res || !Array.isArray(res.commentaires)) {
            listEl.innerHTML = `<p class="text-xs text-slate-400 text-center py-3">Impossible de charger les commentaires</p>`;
            return;
        }

        document.getElementById('commentsCount').textContent = res.commentaires.length;
        
        if (res.commentaires.length === 0) {
            listEl.innerHTML = `<p class="text-xs text-slate-400 text-center py-6 border border-dashed border-slate-100 rounded-2xl bg-slate-50/30">Aucun commentaire pour le moment. Soyez le premier à réagir !</p>`;
            return;
        }

        listEl.innerHTML = res.commentaires.map(c => {
            const roleBadge = c.user?.role === 'ADMIN' 
                ? `<span class="bg-purple-100 text-purple-700 text-[9px] font-bold px-1.5 py-0.5 rounded ml-1">Admin</span>` 
                : '';
            const initial = c.user?.nom?.charAt(0).toUpperCase() || '?';
            
            return `
                <div class="flex items-start gap-3 bg-slate-50/50 border border-slate-100/50 p-3 rounded-2xl transition-all hover:bg-slate-50">
                    <div class="w-8 h-8 rounded-full bg-slate-200 text-slate-600 flex items-center justify-center text-xs font-bold flex-shrink-0">
                        ${initial}
                    </div>
                    <div class="flex-1 min-w-0">
                        <div class="flex items-center justify-between gap-2 mb-1">
                            <span class="font-bold text-slate-800 text-xs truncate">${c.user?.nom || 'Anonyme'} ${roleBadge}</span>
                            <span class="text-[10px] text-slate-400 flex-shrink-0">${timeAgo(c.date)}</span>
                        </div>
                        <p class="text-xs text-slate-600 leading-relaxed whitespace-pre-wrap">${c.contenu}</p>
                    </div>
                </div>
            `;
        }).join('');

    } catch (e) {
        listEl.innerHTML = `<p class="text-xs text-red-400 text-center py-3">Erreur lors de la récupération des commentaires</p>`;
    }
}

async function submitComment(e, signalementId) {
    e.preventDefault();
    const input = document.getElementById('commentInputText');
    const text = input.value.trim();
    if (!text) return;

    const btn = document.getElementById('commentSubmitBtn');
    btn.disabled = true;
    btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> Envoi...`;

    const res = await apiPost(`/signalements/${signalementId}/commentaires`, { contenu: text });
    
    btn.disabled = false;
    btn.innerHTML = `<i class="fas fa-paper-plane"></i> Commenter`;

    if (res.status === 201) {
        input.value = '';
        loadComments(signalementId);
    } else {
        alert(res.data?.erreur || "Une erreur est survenue lors de l'ajout du commentaire.");
    }
}

async function deleteSignalement(id) {
    if (!confirm("Voulez-vous vraiment supprimer définitivement ce signalement ?")) return;
    
    const btn = document.querySelector('[onclick^="deleteSignalement"]');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> Suppression...`;
    }

    const res = await apiDelete('/signalements/' + id);
    if (res.status === 200) {
        closeDetailsModal();
        if (globalModalCallback) globalModalCallback();
    } else {
        alert("Erreur lors de la suppression : " + (res.data?.erreur || "inconnue"));
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = `<i class="fas fa-trash-alt"></i> Supprimer mon signalement`;
        }
    }
}

