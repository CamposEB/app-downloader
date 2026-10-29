/* ============================
   DESTINY DOWNLOADER — main.js
   ============================ */

(function () {
  'use strict';

  /* ─── Helpers ─────────────────────────────────── */
  const $ = (sel, ctx = document) => ctx.querySelector(sel);
  const $$ = (sel, ctx = document) => [...ctx.querySelectorAll(sel)];

  /* ─── Navbar scroll e Back to Top ─────────────── */
  const navbar = $('.navbar');
  const backToTop = $('#backToTop'); // Movido para cima para evitar erro de referência
  
  const onScroll = () => {
    if (navbar) navbar.classList.toggle('scrolled', window.scrollY > 20);
    if (backToTop) backToTop.classList.toggle('visible', window.scrollY > 300);
  };
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  if (backToTop) {
    backToTop.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
  }

  /* ─── Hamburger menu (Mobile) ─────────────────── */
  const navToggle = $('.nav-toggle');
  const navLinks = $('.nav-links');
  
  if (navToggle && navLinks) {
    navToggle.addEventListener('click', () => {
      navToggle.classList.toggle('open');
      navLinks.classList.toggle('open');
    });
    
    // Fecha o menu ao clicar em um link
    navLinks.addEventListener('click', e => {
      if (e.target.tagName === 'A') {
        navToggle.classList.remove('open');
        navLinks.classList.remove('open');
      }
    });
  }

  /* ─── Scroll Reveal (AOS) ─────────────────────── */
  // Adaptado para as classes '.reveal' e '.reveal-right' usadas no novo HTML
  const revealObserver = new IntersectionObserver((entries) => {
    entries.forEach(e => { 
      if (e.isIntersecting) { 
        e.target.classList.add('visible'); 
        revealObserver.unobserve(e.target); 
      } 
    });
  }, { threshold: 0.08, rootMargin: '0px 0px -20px 0px' });

  $$('.reveal, .reveal-right').forEach(el => revealObserver.observe(el));

  // Trigger imediato para o que já está na tela
  requestAnimationFrame(() => {
    $$('.reveal, .reveal-right').forEach(el => {
      const r = el.getBoundingClientRect();
      if (r.top < window.innerHeight && r.bottom > 0) {
        el.classList.add('visible');
        revealObserver.unobserve(el);
      }
    });
  });

  /* ─── Counter animation (Estatísticas) ────────── */
  function animateCounter(el) {
    if (el.dataset.animated) return;
    el.dataset.animated = '1';
    
    // Puxa o atributo 'data-counter' do HTML que criamos
    const target = parseInt(el.dataset.counter, 10);
    const suffix = el.dataset.suffix || '';
    if (isNaN(target)) return;
    
    const duration = 1800;
    const start = performance.now();
    
    const step = (now) => {
      const elapsed = now - start;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3); // ease-out cubic
      el.textContent = Math.round(eased * target).toLocaleString('pt-BR') + suffix;
      if (progress < 1) requestAnimationFrame(step);
      else el.textContent = target.toLocaleString('pt-BR') + suffix;
    };
    requestAnimationFrame(step);
  }

  const counterObserver = new IntersectionObserver((entries) => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        animateCounter(e.target);
        counterObserver.unobserve(e.target);
      }
    });
  }, { threshold: 0 });

  $$('.stat-number[data-counter]').forEach(el => counterObserver.observe(el));

  /* ─── Carrossel de Screenshots (Slider) ───────── */
  // O HTML usa onclick="ssSlider.prev()", então criamos o objeto global
  let currentSlide = 0;
  const slides = $$('.ss-slide');
  const track = $('#ssTrack');
  const dotsWrap = $('#ssDots');

  window.ssSlider = {
    next: function() {
      if (!slides.length) return;
      currentSlide = (currentSlide + 1) % slides.length;
      updateCarousel();
    },
    prev: function() {
      if (!slides.length) return;
      currentSlide = (currentSlide - 1 + slides.length) % slides.length;
      updateCarousel();
    }
  };

  function updateCarousel() {
    if (!track) return;
    
    // Move o track para o slide correto
    track.style.transform = `translateX(-${currentSlide * 100}%)`;
    
    // Atualiza os pontinhos (dots)
    if (dotsWrap) {
      const dots = $$('.ss-dot', dotsWrap);
      dots.forEach((dot, i) => dot.classList.toggle('active', i === currentSlide));
    }
  }

  // Cria os pontinhos dinamicamente se existir o container
  if (dotsWrap && slides.length > 0) {
    dotsWrap.innerHTML = ''; // Limpa antes
    slides.forEach((_, i) => {
      const dot = document.createElement('button');
      dot.className = 'ss-dot' + (i === 0 ? ' active' : '');
      dot.setAttribute('aria-label', `Ir para imagem ${i + 1}`);
      dot.addEventListener('click', () => {
        currentSlide = i;
        updateCarousel();
      });
      dotsWrap.appendChild(dot);
    });
  }

  /* ─── FAQ accordion (Nativo via HTML <details>) ─ */
  // Como usamos <details> e <summary> no HTML, o navegador já faz a animação.
  // O código abaixo serve apenas para fechar os outros ao abrir um.
  const faqItems = $$('.faq-item');
  faqItems.forEach(item => {
    item.addEventListener('toggle', () => {
      if (item.open) {
        faqItems.forEach(otherItem => {
          if (otherItem !== item) otherItem.open = false;
        });
      }
    });
  });

  /* ─── Smooth anchor nav offset ────────────────── */
  $$('a[href^="#"]').forEach(link => {
    link.addEventListener('click', e => {
      const targetId = link.getAttribute('href').slice(1);
      const target = document.getElementById(targetId);
      if (!target) return;
      
      e.preventDefault();
      // Pega a altura do menu (header) para não cobrir o título da seção
      const offset = parseInt(getComputedStyle(document.documentElement).getPropertyValue('--nav-h'), 10) || 68;
      window.scrollTo({ 
        top: target.getBoundingClientRect().top + window.scrollY - offset - 8, 
        behavior: 'smooth' 
      });
    });
  });

  /* ─── Active nav highlighting ─────────────────── */
  const sections = $$('section[id]');
  const navAnchors = $$('.nav-links a[href^="#"]');

  const sectionObserver = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (!e.isIntersecting) return;
      const id = e.target.id;
      navAnchors.forEach(a => {
        // Muda a cor do link ativo
        if (a.getAttribute('href') === `#${id}`) {
            a.classList.add('active');
            a.style.color = 'var(--accent-blue)';
        } else {
            a.classList.remove('active');
            a.style.color = '';
        }
      });
    });
  }, { rootMargin: '-40% 0px -50% 0px' });

  sections.forEach(s => sectionObserver.observe(s));

  /* ─── GitHub Releases Fetch ───────────────────── */
  // Essa é a parte que puxa os APKs do seu repositório!
  const releasesList = $('.releases-list');
  const releasesInfo = $('.releases-info');
  
  if (releasesList) {
    fetch('https://api.github.com/repos/CamposEB/app-downloader/releases')
      .then(res => {
        if (!res.ok) throw new Error('Falha na rede');
        return res.json();
      })
      .then(data => {
        if (releasesInfo) releasesInfo.style.display = 'none'; // Esconde "Carregando..."
        
        if (data.length === 0) {
          releasesList.innerHTML = '<p style="text-align:center; color:var(--text-muted);">Nenhuma versão encontrada no momento.</p>';
          return;
        }

        // Pega apenas as 3 últimas releases
        const recentReleases = data.slice(0, 3);
        
        recentReleases.forEach(release => {
          // Formata a data (ex: 25/10/2026)
          const date = new Date(release.published_at).toLocaleDateString('pt-BR');
          
          // Filtra apenas os arquivos .apk dos assets
          const apks = release.assets.filter(asset => asset.name.endsWith('.apk'));
          
          let apkButtonsHTML = '';
          if (apks.length > 0) {
            apks.forEach(apk => {
              // Remove o "Destiny-" e ".apk" do nome para ficar mais limpo
              let btnLabel = apk.name.replace('.apk', '').replace('Destiny-', '').replace('universal', 'Universal');
              apkButtonsHTML += `
                <a href="${apk.browser_download_url}" class="btn btn-secondary" style="padding: 8px 16px; font-size: 0.85rem; margin-top: 8px;">
                  ⬇️ ${btnLabel}
                </a>
              `;
            });
          } else {
             apkButtonsHTML = `<span style="font-size:0.85rem; color:#ff4c4c;">Sem APK anexado</span>`;
          }

          // Monta o card
          const cardHTML = `
            <div class="glass-card reveal visible" style="padding: 24px; display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px;">
              <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap;">
                <div style="font-size: 1.2rem; font-weight: 700; color: var(--accent-blue);">
                  ${release.name || release.tag_name}
                </div>
                <div style="font-size: 0.85rem; color: var(--text-muted);">
                  Lançado em: ${date}
                </div>
              </div>
              <p style="font-size: 0.9rem; color: var(--text-muted); white-space: pre-wrap; max-height: 100px; overflow: hidden; text-overflow: ellipsis;">
                ${release.body ? release.body.substring(0, 150) + '...' : 'Sem notas de lançamento.'}
              </p>
              <div style="display: flex; gap: 10px; flex-wrap: wrap; margin-top: 10px;">
                ${apkButtonsHTML}
              </div>
            </div>
          `;
          
          releasesList.insertAdjacentHTML('beforeend', cardHTML);
        });
      })
      .catch(err => {
        console.error('Erro ao buscar versões:', err);
        if (releasesInfo) releasesInfo.textContent = 'Erro ao carregar. Verifique sua conexão.';
      });
  }

  /* ─── Botão de Atualizar Releases ─────────────── */
  const btnRefresh = $('.btn-refresh');
  if (btnRefresh) {
    btnRefresh.addEventListener('click', () => {
      window.location.reload(); // Recarrega a página para buscar novamente do GitHub
    });
  }

  /* ─── Resize handler ──────────────────────────── */
  window.addEventListener('resize', () => {
    // Reajusta o carrossel se a tela mudar de tamanho
    updateCarousel();
  }, { passive: true });

})();