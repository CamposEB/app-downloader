/* Destiny Downloader — main.js (seguro para todas as páginas) */
const CONFIG = {
  api: 'https://api.github.com/repos/CamposEB/app-downloader/releases?per_page=10',
  cacheKey: 'destiny_releases_v2',
  ttl: 60 * 60 * 1000
};
const $ = (s, c = document) => c.querySelector(s);
const $$ = (s, c = document) => [...c.querySelectorAll(s)];
const esc = s => String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

/* Toast */
function toast(msg, type = 'info') {
  let box = $('.toast-container');
  if (!box) { box = document.createElement('div'); box.className = 'toast-container'; document.body.appendChild(box); }
  const el = document.createElement('div');
  el.className = 'toast ' + type; el.textContent = msg; box.appendChild(el);
  requestAnimationFrame(() => el.classList.add('show'));
  setTimeout(() => { el.classList.remove('show'); setTimeout(() => el.remove(), 400); }, 2800);
}

/* Navbar + menu mobile */
const navbar = $('.navbar');
addEventListener('scroll', () => navbar && navbar.classList.toggle('scrolled', scrollY > 30), { passive: true });
const toggle = $('.nav-toggle'), links = $('.nav-links');
if (toggle && links) {
  const close = () => { toggle.classList.remove('open'); links.classList.remove('mobile-open'); toggle.setAttribute('aria-expanded', 'false'); };
  toggle.addEventListener('click', () => {
    const open = toggle.classList.toggle('open');
    links.classList.toggle('mobile-open', open);
    toggle.setAttribute('aria-expanded', open);
  });
  links.addEventListener('click', e => e.target.tagName === 'A' && close());
}

/* Link ativo conforme a seção */
const navAnchors = $$('.nav-links a[href^="#"]');
if (navAnchors.length) {
  const io = new IntersectionObserver(es => es.forEach(e => {
    if (e.isIntersecting) navAnchors.forEach(a => a.classList.toggle('active', a.getAttribute('href') === '#' + e.target.id));
  }), { rootMargin: '-45% 0px -50% 0px' });
  $$('section[id]').forEach(s => io.observe(s));
}

/* Revelar ao rolar */
const rio = new IntersectionObserver(es => es.forEach(e => {
  if (e.isIntersecting) { e.target.classList.add('visible'); rio.unobserve(e.target); }
}), { threshold: 0.08 });
$$('.reveal, .reveal-right').forEach(el => rio.observe(el));

/* Contador */
$$('[data-counter]').forEach(el => {
  const end = parseInt(el.dataset.counter, 10), suf = el.dataset.suffix || '';
  const o = new IntersectionObserver(([e]) => {
    if (!e.isIntersecting) return; o.disconnect();
    const t0 = performance.now();
    (function step(now) {
      const p = Math.min((now - t0) / 1400, 1);
      el.textContent = Math.round((1 - Math.pow(1 - p, 3)) * end).toLocaleString('pt-BR') + suf;
      if (p < 1) requestAnimationFrame(step);
    })(t0);
  }); o.observe(el);
});

/* Releases do GitHub */
const getCache = () => {
  try { const c = JSON.parse(localStorage.getItem(CONFIG.cacheKey)); return c && Date.now() - c.t < CONFIG.ttl ? c : null; } catch { return null; }
};
async function fetchReleases(force) {
  if (!force) { const c = getCache(); if (c) return c; }
  const r = await fetch(CONFIG.api);
  if (!r.ok) throw new Error('GitHub ' + r.status);
  const c = { d: await r.json(), t: Date.now() };
  try { localStorage.setItem(CONFIG.cacheKey, JSON.stringify(c)); } catch {}
  return c;
}
const size = b => b >= 1048576 ? (b / 1048576).toFixed(1) + ' MB' : Math.round(b / 1024) + ' KB';
function apkLabel(n) {
  n = n.toLowerCase();
  if (n.includes('universal')) return 'Universal';
  if (n.includes('arm64')) return 'ARM64 (a maioria dos celulares)';
  if (n.includes('armeabi') || n.includes('arm')) return 'ARMv7';
  if (n.includes('x86_64')) return 'x86_64';
  if (n.includes('x86')) return 'x86';
  return 'APK';
}
function md(t) {
  return esc(t || '').replace(/^#+ (.+)$/gm, '<strong>$1</strong>').replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>').replace(/^[-*] (.+)$/gm, '<li>$1</li>').replace(/\n/g, '<br>');
}
function card(r, i) {
  const apks = (r.assets || []).filter(a => a.name.endsWith('.apk'));
  const body = r.body || 'Sem notas de lançamento.';
  const date = new Date(r.published_at).toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' });
  return `<article class="glass-card release-card">
    <div class="release-header"><div><div class="release-version grad-text">${esc(r.name || r.tag_name)}</div>
    <div class="release-date">${date}</div></div>
    ${i === 0 ? '<span class="release-badge">Mais recente</span>' : r.prerelease ? '<span class="release-badge">Prévia</span>' : ''}</div>
    <div class="release-changelog${body.length > 260 ? ' long' : ''}">${md(body)}</div>
    ${body.length > 260 ? '<button class="btn-expand" data-expand>Ver mais</button>' : ''}
    <div class="downloads-grid">${apks.length ? apks.map(a =>
      `<a class="btn ${a.name.toLowerCase().includes('universal') ? 'btn-primary' : 'btn-secondary'}" href="${a.browser_download_url}" rel="noopener">⬇ ${apkLabel(a.name)} <small>${size(a.size)}</small></a>`
    ).join('') : '<span class="release-date">Nenhum APK anexado a esta versão.</span>'}</div></article>`;
}
async function loadReleases(force = false) {
  const list = $('.releases-list') || document.createElement('div');
  const info = $('.releases-info'), btn = $('.btn-refresh');
  list.innerHTML = '<div class="skeleton"></div>'; btn && btn.classList.add('loading');
  try {
    const { d, t } = await fetchReleases(force);
    const rel = d.filter(r => !r.draft);
    if (!rel.length) { list.innerHTML = '<div class="glass-card releases-msg">Nenhuma versão publicada ainda.</div>'; return; }
    const [latest, ...older] = rel;
    list.innerHTML = card(latest, 0) + (older.length ? `<div><button class="btn-older-toggle" data-older>Ver ${older.length} versões anteriores</button>
      <div class="older-releases-list">${older.map((r, i) => card(r, i + 1)).join('')}</div></div>` : '');
    if (info) info.textContent = `Última versão: ${latest.tag_name} · atualizado há ${Math.max(0, Math.round((Date.now() - t) / 60000))} min`;
    /* Botão principal baixa direto o APK universal */
    const apks = (latest.assets || []).filter(a => a.name.endsWith('.apk'));
    const best = apks.find(a => /universal/i.test(a.name)) || apks[0];
    $$('[data-apk]').forEach(a => {
      if (best) { a.href = best.browser_download_url; a.removeAttribute('data-needs-scroll'); }
      const v = $('[data-version]', a.parentElement.parentElement); if (v) v.textContent = `${latest.tag_name} · ${size(best ? best.size : 0)} · Android 7.0+`;
    });
    force && toast('Versões atualizadas', 'success');
  } catch (e) {
    console.error(e);
    list.innerHTML = '<div class="glass-card releases-msg"><p>Não foi possível carregar as versões.</p><br><a class="btn btn-secondary" href="https://github.com/CamposEB/app-downloader/releases" target="_blank" rel="noopener">Abrir no GitHub</a></div>';
    if (info) info.textContent = 'Sem conexão com o GitHub.';
  } finally { btn && btn.classList.remove('loading'); }
}
document.addEventListener('click', e => {
  const x = e.target.closest('[data-expand]');
  if (x) { const b = x.previousElementSibling.classList.toggle('expanded'); x.textContent = b ? 'Ver menos' : 'Ver mais'; }
  const o = e.target.closest('[data-older]');
  if (o) { const open = o.nextElementSibling.classList.toggle('open'); o.textContent = open ? 'Ocultar versões anteriores' : o.textContent.replace('Ocultar versões anteriores', 'Ver versões anteriores'); }
});
const refresh = $('.btn-refresh');
refresh && refresh.addEventListener('click', () => { localStorage.removeItem(CONFIG.cacheKey); loadReleases(true); });
loadReleases();

/* FAQ: abre um por vez */
const faq = $$('.faq-item');
faq.forEach(i => i.addEventListener('toggle', () => i.open && faq.forEach(o => o !== i && (o.open = false))));

/* Ano do rodapé */
$$('[data-year]').forEach(el => el.textContent = new Date().getFullYear());