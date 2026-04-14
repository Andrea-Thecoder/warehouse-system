<script lang="ts">
  // Pagina mostrata dopo la registrazione.
  // L'utente è stato creato su Keycloak ma ha ruolo "pending":
  // non può accedere finché un admin non approva il suo account.
</script>

<svelte:head>
  <title>Account in attesa — Warehouse System</title>
</svelte:head>

<div class="min-h-dvh flex items-center justify-center px-4 py-24">
  <div class="w-full max-w-md text-center">

    <!-- Icona -->
    <div class="inline-flex items-center justify-center w-20 h-20 rounded-2xl
                bg-yellow-500/10 border border-yellow-500/25 mb-8 mx-auto">
      <svg width="36" height="36" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="12" cy="12" r="10" stroke="#facc15" stroke-width="1.8"/>
        <path d="M12 7v5.5M12 16h.01" stroke="#facc15" stroke-width="1.8" stroke-linecap="round"/>
      </svg>
    </div>

    <!-- Titolo -->
    <h1 class="text-2xl font-extrabold tracking-tight mb-3">
      Account in attesa di approvazione
    </h1>

    <!-- Messaggio -->
    <p class="text-slate-400 leading-relaxed mb-2">
      La tua registrazione è andata a buon fine. Il tuo account è stato creato
      e sarà attivato da un amministratore nelle prossime ore.
    </p>
    <p class="text-slate-500 text-sm leading-relaxed mb-10">
      Riceverai una notifica via email non appena il tuo account sarà approvato.
      Se non ricevi nessuna comunicazione entro 24 ore, contatta il supporto.
    </p>

    <!-- Steps -->
    <div class="bg-surface border border-white/8 rounded-2xl p-6 text-left mb-8 flex flex-col gap-5">
      <div class="flex items-start gap-4">
        <div class="w-7 h-7 rounded-full bg-green-500/15 border border-green-500/30 flex items-center justify-center shrink-0 mt-0.5">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
            <path d="M2 6l3 3 5-5" stroke="#4ade80" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-200">Registrazione completata</p>
          <p class="text-xs text-slate-500 mt-0.5">Il tuo account è stato creato con successo.</p>
        </div>
      </div>

      <div class="flex items-start gap-4">
        <div class="w-7 h-7 rounded-full bg-yellow-500/15 border border-yellow-500/30 flex items-center justify-center shrink-0 mt-0.5">
          <svg class="[animation:spin_2s_linear_infinite]" width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
            <circle cx="6" cy="6" r="5" stroke="#facc15" stroke-width="1.5" stroke-dasharray="20" stroke-dashoffset="7"/>
          </svg>
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-200">In attesa di approvazione</p>
          <p class="text-xs text-slate-500 mt-0.5">Un amministratore verificherà il tuo account.</p>
        </div>
      </div>

      <div class="flex items-start gap-4">
        <div class="w-7 h-7 rounded-full bg-white/5 border border-white/10 flex items-center justify-center shrink-0 mt-0.5">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
            <path d="M6 2v8M2 6h8" stroke="#475569" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-500">Accesso attivato</p>
          <p class="text-xs text-slate-600 mt-0.5">Potrai effettuare il login e iniziare a lavorare.</p>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex flex-col gap-3">
      <a href="/login"
         class="w-full flex items-center justify-center gap-2 px-6 py-3 rounded-xl
                text-sm font-semibold text-white bg-blue-500
                hover:bg-blue-600 hover:shadow-[0_0_20px_rgba(59,130,246,0.35)]
                transition-all duration-200">
        Prova ad accedere
      </a>
      <a href="/"
         class="w-full flex items-center justify-center gap-2 px-6 py-3 rounded-xl
                text-sm font-semibold text-slate-400 border border-white/10
                hover:text-slate-200 hover:border-white/20 transition-all duration-200">
        Torna alla home
      </a>
    </div>

  </div>
</div>
