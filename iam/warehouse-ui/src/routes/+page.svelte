<!-- ── Hero ─────────────────────────────────────────────────────────────── -->
<section class="relative pt-40 pb-24 overflow-hidden text-center">

  <!-- Grid background -->
  <div class="absolute inset-0 bg-[image:linear-gradient(rgba(59,130,246,0.04)_1px,transparent_1px),linear-gradient(90deg,rgba(59,130,246,0.04)_1px,transparent_1px)] bg-[size:60px_60px] [mask-image:radial-gradient(ellipse_80%_60%_at_50%_0%,black_40%,transparent_100%)]" aria-hidden="true"></div>

  <!-- Glow -->
  <div class="absolute -top-52 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-[radial-gradient(ellipse,rgba(59,130,246,0.15)_0%,transparent_70%)] pointer-events-none" aria-hidden="true"></div>

  <div class="relative max-w-6xl mx-auto px-8">

    <!-- Title -->
    <h1 class="text-5xl md:text-7xl font-extrabold tracking-tight leading-[1.08] mb-6">
      Warehouse System<br />
      <span class="bg-[linear-gradient(135deg,#60a5fa_0%,#a78bfa_50%,#60a5fa_100%)] bg-[size:200%_auto] bg-clip-text text-transparent [animation:shimmer_4s_linear_infinite]">
        Amministrazione
      </span>
    </h1>

    <!-- Subtitle -->
    <p class="text-slate-400 text-lg md:text-xl max-w-xl mx-auto leading-relaxed mb-12">
      Gestisci utenti, ruoli e approvazioni dell'intero ecosistema.<br />
      Autenticazione SSO centralizzata, audit trail completo.
    </p>

    <!-- CTAs -->
    <div class="flex justify-center gap-4 flex-wrap mb-20">
      <a href="#ecosystem"
         class="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm font-semibold text-white bg-blue-500
                hover:bg-blue-600 hover:shadow-[0_0_30px_rgba(59,130,246,0.35)] hover:-translate-y-px transition-all duration-200">
        Scopri Warehouse System
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <path d="M8 3v10M3 8l5 5 5-5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </a>
      <a href="#features"
         class="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm font-semibold text-slate-400
                border border-white/10 bg-white/3 hover:text-slate-100 hover:border-white/20 hover:bg-white/7
                transition-all duration-200">
        Funzionalità del modulo
      </a>
    </div>

    <!-- Admin console mockup -->
    <div class="max-w-2xl mx-auto rounded-2xl border border-white/10 bg-surface overflow-hidden shadow-2xl text-left" role="img" aria-label="Anteprima console amministrazione">
      <!-- Bar -->
      <div class="flex items-center gap-2 px-4 py-3 bg-surface-2 border-b border-white/8">
        <span class="w-3 h-3 rounded-full bg-[#ff5f57]"></span>
        <span class="w-3 h-3 rounded-full bg-[#febc2e]"></span>
        <span class="w-3 h-3 rounded-full bg-[#28c840]"></span>
        <span class="flex-1 text-center text-xs text-slate-600 font-mono">warehouse-system — amministrazione</span>
      </div>
      <!-- Body -->
      <div class="p-6 flex flex-col gap-3 font-mono text-sm">
        <div class="flex items-center gap-3">
          <span class="text-slate-600">●</span>
          <span class="text-slate-400 flex-1">Utenti in attesa di approvazione</span>
          <span class="text-slate-100 font-semibold">5</span>
          <span class="text-xs px-2 py-0.5 rounded-full bg-yellow-500/15 text-yellow-400 font-semibold">in coda</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-slate-600">●</span>
          <span class="text-slate-400 flex-1">Utenti attivi nel sistema</span>
          <span class="text-slate-100 font-semibold">38</span>
          <span class="text-xs px-2 py-0.5 rounded-full bg-green-500/15 text-green-400 font-semibold">online</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-slate-600">●</span>
          <span class="text-slate-400 flex-1">Sessioni autenticate oggi</span>
          <span class="text-slate-100 font-semibold">124</span>
          <span class="text-xs px-2 py-0.5 rounded-full bg-blue-500/15 text-blue-400 font-semibold">OIDC</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-slate-600">●</span>
          <span class="text-slate-400 flex-1">Account disabilitati</span>
          <span class="text-slate-100 font-semibold">2</span>
          <span class="text-xs px-2 py-0.5 rounded-full bg-red-500/15 text-red-400 font-semibold">in revisione</span>
        </div>
        <div class="h-px bg-white/8 my-1"></div>
        <div class="flex items-center gap-3 text-slate-600 text-xs">
          <span>Keycloak SSO operativo — ultimo sync 1 min fa</span>
          <span class="ml-auto text-xs px-2 py-0.5 rounded-full bg-green-500/15 text-green-400 font-semibold">● online</span>
        </div>
      </div>
    </div>

  </div>
</section>

<!-- ── Ecosystem ───────────────────────────────────────────────────────────── -->
<section class="py-28" id="ecosystem">
  <div class="max-w-6xl mx-auto px-8">

    <!-- Header -->
    <div class="text-center mb-20">
      <p class="text-xs font-semibold uppercase tracking-widest text-blue-500 mb-4">La piattaforma</p>
      <h2 class="text-4xl md:text-5xl font-extrabold tracking-tight leading-tight mb-5">
        Cos'è Warehouse System
      </h2>
      <p class="text-slate-400 text-lg max-w-2xl mx-auto leading-relaxed">
        Una suite integrata per la gestione operativa completa della supply chain:
        dai magazzini fisici al trasporto su strada, fino al controllo centralizzato degli accessi.
      </p>
    </div>

    <!-- Ecosystem cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-5 mb-16">

      <!-- Amministrazione — evidenziata come modulo corrente -->
      <div class="relative bg-[linear-gradient(135deg,rgba(59,130,246,0.10)_0%,rgba(167,139,250,0.06)_100%)]
                  border border-blue-500/35 rounded-2xl p-8 flex flex-col gap-5
                  shadow-[0_0_40px_rgba(59,130,246,0.08)]">
        <!-- Badge modulo corrente -->
        <div class="absolute top-4 right-4">
          <span class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full
                       bg-blue-500/20 border border-blue-500/40 text-blue-400 font-semibold">
            <span class="w-1.5 h-1.5 rounded-full bg-blue-400 animate-[pulse-dot_2s_ease-in-out_infinite]"></span>
            questo modulo
          </span>
        </div>
        <div class="w-12 h-12 rounded-xl bg-blue-500/15 flex items-center justify-center shrink-0">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <h3 class="text-xl font-bold tracking-tight mb-2 text-slate-100">Amministrazione</h3>
          <p class="text-slate-400 text-sm leading-relaxed">
            Gestione centralizzata di utenti, ruoli e accessi tramite SSO Keycloak.
            Controlla chi può operare sui moduli Logistica e Trasporti: approva nuovi account,
            assegna permessi granulari e monitora tutte le sessioni attive.
          </p>
        </div>
        <div class="flex gap-2 flex-wrap mt-auto">
          <span class="text-xs px-3 py-1 rounded-full border border-blue-500/25 text-blue-400 bg-blue-500/8">OIDC + PKCE</span>
          <span class="text-xs px-3 py-1 rounded-full border border-blue-500/25 text-blue-400 bg-blue-500/8">Ruoli & permessi</span>
          <span class="text-xs px-3 py-1 rounded-full border border-blue-500/25 text-blue-400 bg-blue-500/8">Audit log</span>
        </div>
      </div>

      <!-- Logistica -->
      <div class="bg-surface border border-white/8 rounded-2xl p-8 flex flex-col gap-5
                  hover:border-white/15 transition-all duration-200">
        <div class="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center shrink-0">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" stroke="#64748b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="9 22 9 12 15 12 15 22" stroke="#64748b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <h3 class="text-xl font-bold tracking-tight mb-2 text-slate-300">Logistica</h3>
          <p class="text-slate-500 text-sm leading-relaxed">
            Gestione operativa dei magazzini fisici: inventario in tempo reale, movimentazioni
            in entrata e uscita, tracciamento ordini e alert su soglie di scorta.
            Il cuore operativo del sistema.
          </p>
        </div>
        <div class="flex gap-2 flex-wrap mt-auto">
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-500 bg-white/2">Ordini I/O</span>
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-500 bg-white/2">Stock real-time</span>
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-500 bg-white/2">Multi-magazzino</span>
        </div>
      </div>

      <!-- Trasporti -->
      <div class="bg-surface border border-white/8 rounded-2xl p-8 flex flex-col gap-5
                  hover:border-white/15 transition-all duration-200">
        <div class="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center shrink-0">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="1" y="3" width="15" height="13" rx="1" stroke="#64748b" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M16 8h4l3 5v4h-7V8z" stroke="#64748b" stroke-width="1.8" stroke-linejoin="round"/>
            <circle cx="5.5" cy="18.5" r="2.5" stroke="#64748b" stroke-width="1.8"/>
            <circle cx="18.5" cy="18.5" r="2.5" stroke="#64748b" stroke-width="1.8"/>
          </svg>
        </div>
        <div>
          <h3 class="text-xl font-bold tracking-tight mb-2 text-slate-300">Trasporti</h3>
          <p class="text-slate-500 text-sm leading-relaxed">
            Ottimizzazione delle rotte di consegna per i corrieri. Calcola automaticamente
            il percorso più efficiente per il trasporto di merce tra magazzini,
            riducendo tempi e costi operativi.
          </p>
        </div>
        <div class="flex gap-2 flex-wrap mt-auto">
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-500 bg-white/2">Routing ottimizzato</span>
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-500 bg-white/2">Flotta corrieri</span>
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-500 bg-white/2">Tracking live</span>
        </div>
      </div>

    </div>

    <!-- Architecture note -->
    <div class="flex items-start gap-4 px-6 py-5 rounded-2xl bg-surface border border-white/8 text-sm text-slate-400 leading-relaxed">
      <div class="shrink-0 mt-0.5">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="10" stroke="#3b82f6" stroke-width="1.8"/>
          <path d="M12 16v-4M12 8h.01" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
      </div>
      <p>
        I tre moduli condividono un'unica infrastruttura di autenticazione basata su
        <span class="text-slate-200 font-semibold">Keycloak</span> e comunicano tramite
        <span class="text-slate-200 font-semibold">API REST</span> indipendenti.
        Il modulo Amministrazione governa l'accesso a tutti gli altri: un utente deve essere
        approvato da un amministratore prima di poter operare su Logistica o Trasporti.
      </p>
    </div>

  </div>
</section>

<!-- ── Features ───────────────────────────────────────────────────────────── -->
<section class="py-28 border-t border-white/8" id="features">
  <div class="max-w-6xl mx-auto px-8">

    <!-- Header -->
    <div class="text-center mb-20">
      <p class="text-xs font-semibold uppercase tracking-widest text-blue-500 mb-4">Funzionalità</p>
      <h2 class="text-4xl md:text-5xl font-extrabold tracking-tight leading-tight mb-5">
        Cosa fa il modulo<br />Amministrazione
      </h2>
      <p class="text-slate-400 text-lg max-w-lg mx-auto leading-relaxed">
        Tutto ciò che serve per governare utenti, accessi e sicurezza dell'intero ecosistema.
      </p>
    </div>

    <!-- Grid -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-5">

      <!-- Large card: gestione utenti -->
      <div class="md:col-span-2 bg-surface border border-white/8 rounded-2xl p-8 flex flex-col gap-5
                  hover:border-blue-500/30 hover:shadow-[0_0_30px_rgba(59,130,246,0.08)] transition-all duration-200">
        <div class="w-11 h-11 rounded-xl bg-blue-500/10 flex items-center justify-center shrink-0">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="9" cy="7" r="4" stroke="#3b82f6" stroke-width="1.8"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <h3 class="text-lg font-bold tracking-tight mb-2">Gestione utenti centralizzata</h3>
          <p class="text-slate-400 text-sm leading-relaxed">
            Visualizza tutti gli account registrati, abilita o disabilita l'accesso, modifica i ruoli
            in qualsiasi momento. Ogni cambio è immediato: il token JWT viene aggiornato alla sessione successiva.
          </p>
        </div>
        <div class="flex gap-2 flex-wrap mt-auto">
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-400 bg-white/3">Abilita / Disabilita</span>
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-400 bg-white/3">Cambio ruoli live</span>
          <span class="text-xs px-3 py-1 rounded-full border border-white/10 text-slate-400 bg-white/3">Paginazione</span>
        </div>
      </div>

      <!-- Card: approvazione account -->
      <div class="bg-surface border border-white/8 rounded-2xl p-8 flex flex-col gap-5
                  hover:border-blue-500/30 hover:shadow-[0_0_30px_rgba(59,130,246,0.08)] transition-all duration-200">
        <div class="w-11 h-11 rounded-xl bg-blue-500/10 flex items-center justify-center shrink-0">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="22 4 12 14.01 9 11.01" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h3 class="text-lg font-bold tracking-tight">Workflow di approvazione</h3>
        <p class="text-slate-400 text-sm leading-relaxed flex-1">
          I nuovi utenti entrano in stato <em>pending</em> e non possono operare finché un admin non approva
          il loro account e assegna il ruolo appropriato.
        </p>
      </div>

      <!-- Card: ruoli e permessi -->
      <div class="bg-surface border border-white/8 rounded-2xl p-8 flex flex-col gap-5
                  hover:border-blue-500/30 hover:shadow-[0_0_30px_rgba(59,130,246,0.08)] transition-all duration-200">
        <div class="w-11 h-11 rounded-xl bg-blue-500/10 flex items-center justify-center shrink-0">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="11" width="18" height="11" rx="2" stroke="#3b82f6" stroke-width="1.8" stroke-linejoin="round"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h3 class="text-lg font-bold tracking-tight">Ruoli & permessi granulari</h3>
        <p class="text-slate-400 text-sm leading-relaxed flex-1">
          Tre ruoli applicativi (Admin, User, Pending) mappati su Keycloak realm roles.
          Il redirect post-login avviene automaticamente in base al ruolo del token JWT.
        </p>
      </div>

      <!-- Card: audit -->
      <div class="bg-surface border border-white/8 rounded-2xl p-8 flex flex-col gap-5
                  hover:border-blue-500/30 hover:shadow-[0_0_30px_rgba(59,130,246,0.08)] transition-all duration-200">
        <div class="w-11 h-11 rounded-xl bg-blue-500/10 flex items-center justify-center shrink-0">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="#3b82f6" stroke-width="1.8" stroke-linejoin="round"/>
            <polyline points="14 2 14 8 20 8" stroke="#3b82f6" stroke-width="1.8" stroke-linejoin="round"/>
            <line x1="16" y1="13" x2="8" y2="13" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round"/>
            <line x1="16" y1="17" x2="8" y2="17" stroke="#3b82f6" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </div>
        <h3 class="text-lg font-bold tracking-tight">Audit trail completo</h3>
        <p class="text-slate-400 text-sm leading-relaxed flex-1">
          Ogni azione amministrativa è tracciata: approvazioni, modifiche di ruolo, disabilitazioni.
          Storico sempre disponibile per compliance e revisione interna.
        </p>
      </div>

      <!-- CTA card -->
      <div class="bg-[linear-gradient(135deg,rgba(59,130,246,0.12)_0%,rgba(167,139,250,0.08)_100%)]
                  border border-blue-500/25 rounded-2xl p-8 flex flex-col gap-4">
        <p class="text-xs font-semibold uppercase tracking-widest text-blue-400">Accesso richiesto</p>
        <h3 class="text-2xl font-extrabold tracking-tight leading-snug flex-1">Hai già un account?</h3>
        <div class="flex flex-col gap-2 mt-auto">
          <a href="/login"
             class="inline-flex items-center gap-2 px-5 py-2.5 rounded-full text-sm font-semibold text-white bg-blue-500
                    hover:bg-blue-600 hover:shadow-[0_0_20px_rgba(59,130,246,0.35)] transition-all duration-200 self-start">
            Accedi
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <path d="M3 8H13M13 8L9 4M13 8L9 12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </a>
          <a href="/register"
             class="text-xs text-slate-500 hover:text-slate-400 transition-colors duration-200 px-1">
            Non hai un account? Registrati →
          </a>
        </div>
      </div>

    </div>
  </div>
</section>

<!-- ── About ───────────────────────────────────────────────────────────────── -->
<section class="border-t border-white/8 py-28" id="about">
  <div class="max-w-6xl mx-auto px-8">
    <div class="max-w-2xl">
      <p class="text-xs font-semibold uppercase tracking-widest text-blue-500 mb-4">Infrastruttura</p>
      <h2 class="text-4xl md:text-5xl font-extrabold tracking-tight leading-tight mb-6">
        Costruito per<br />ambienti critici
      </h2>
      <p class="text-slate-400 leading-relaxed mb-4">
        Warehouse System è progettato per operare in contesti dove un'interruzione del servizio
        ha un impatto diretto sulle operazioni. L'architettura cloud-native garantisce
        alta disponibilità, scalabilità orizzontale e zero downtime during deployments.
      </p>
      <p class="text-slate-400 leading-relaxed mb-8">
        L'autenticazione centralizzata su Keycloak assicura che ogni accesso ai tre moduli
        sia tracciato, revocabile in tempo reale e conforme agli standard OIDC.
      </p>
      <div class="flex flex-wrap gap-3">
        <span class="flex items-center gap-2 px-4 py-1.5 rounded-full border border-white/10 bg-surface text-slate-400 text-sm">🇮🇹 Made in Italy</span>
        <span class="flex items-center gap-2 px-4 py-1.5 rounded-full border border-white/10 bg-surface text-slate-400 text-sm">☁️ Cloud-native</span>
        <span class="flex items-center gap-2 px-4 py-1.5 rounded-full border border-white/10 bg-surface text-slate-400 text-sm">🔒 GDPR compliant</span>
      </div>
    </div>
  </div>
</section>

<!-- ── CTA Banner ─────────────────────────────────────────────────────────── -->
<section class="border-t border-white/8 py-28">
  <div class="max-w-6xl mx-auto px-8">
    <div class="relative text-center py-20 px-8 rounded-3xl border border-blue-500/20
                bg-[linear-gradient(135deg,rgba(59,130,246,0.06)_0%,rgba(167,139,250,0.04)_100%)] overflow-hidden">
      <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_center,rgba(59,130,246,0.1)_0%,transparent_60%)] pointer-events-none" aria-hidden="true"></div>
      <h2 class="relative text-4xl md:text-5xl font-extrabold tracking-tight mb-4">
        Accedi all'area di amministrazione
      </h2>
      <p class="relative text-slate-400 mb-10">Gestisci utenti, ruoli e approvazioni dell'intero ecosistema Warehouse System.</p>
      <div class="relative flex justify-center gap-4 flex-wrap">
        <a href="/login"
           class="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm font-semibold text-white bg-blue-500
                  hover:bg-blue-600 hover:shadow-[0_0_30px_rgba(59,130,246,0.35)] transition-all duration-200">
          Accedi
        </a>
        <a href="/register"
           class="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm font-semibold text-white/75
                  border border-white/20 hover:text-white hover:border-white/40 hover:bg-white/8 transition-all duration-200">
          Non hai un account? Registrati
        </a>
      </div>
    </div>
  </div>
</section>

<!-- ── Footer ─────────────────────────────────────────────────────────────── -->
<footer class="border-t border-white/8 pt-20 pb-10">
  <div class="max-w-6xl mx-auto px-8">
    <div class="flex flex-col md:flex-row gap-16 mb-16">

      <!-- Brand -->
      <div class="flex-[1.5]">
        <a href="/" class="inline-flex items-center gap-2 mb-4">
          <svg width="24" height="24" viewBox="0 0 28 28" fill="none" aria-hidden="true">
            <rect width="28" height="28" rx="7" fill="#3b82f6"/>
            <path d="M6 10.5L14 6L22 10.5V17.5L14 22L6 17.5V10.5Z"
                  stroke="white" stroke-width="1.8" stroke-linejoin="round" fill="none"/>
            <path d="M14 6V22M6 10.5L22 17.5M22 10.5L6 17.5"
                  stroke="white" stroke-width="1.2" stroke-opacity="0.5"/>
          </svg>
          <span class="text-base font-bold tracking-tight">Warehouse<span class="text-blue-500">System</span></span>
        </a>
        <p class="text-sm text-slate-600 leading-relaxed">
          Piattaforma integrata per la gestione<br />di magazzini, ordini e trasporti.
        </p>
      </div>

      <!-- Links -->
      <div class="flex gap-16 flex-2 md:justify-end flex-wrap">
        <div class="flex flex-col gap-3">
          <h4 class="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-1">Piattaforma</h4>
          <a href="#ecosystem" class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Cos'è</a>
          <a href="#features"  class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Funzionalità</a>
          <a href="#about"     class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Infrastruttura</a>
        </div>
        <div class="flex flex-col gap-3">
          <h4 class="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-1">Account</h4>
          <a href="/login"    class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Accedi</a>
          <a href="/register" class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Registrati</a>
        </div>
        <div class="flex flex-col gap-3">
          <h4 class="text-xs font-semibold uppercase tracking-widest text-slate-400 mb-1">Legale</h4>
          <a href="/privacy" class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Privacy Policy</a>
          <a href="/terms"   class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Termini di servizio</a>
          <a href="/cookies" class="text-sm text-slate-600 hover:text-slate-400 transition-colors duration-200">Cookie Policy</a>
        </div>
      </div>

    </div>

    <div class="flex justify-between items-center pt-8 border-t border-white/8 text-xs text-slate-600 flex-wrap gap-4">
      <span>© 2026 WarehouseSystem S.r.l. — P.IVA 12345678901</span>
      <span>Fatto con cura in Italia 🇮🇹</span>
    </div>

  </div>
</footer>
