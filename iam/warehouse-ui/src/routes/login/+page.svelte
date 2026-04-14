<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { initKeycloak, login, getPostLoginRoute } from '$lib/auth/keycloak';
  import { authStore } from '$lib/stores/authStore';

  let checking = true;  // verifica sessione SSO in corso
  let error    = '';

  onMount(async () => {
    try {
      const authenticated = await initKeycloak();

      if (authenticated) {
        // Sessione Keycloak già attiva → redirect diretto
        goto(getPostLoginRoute(), { replaceState: true });
        return;
      }

      // Non autenticato: mostra il pulsante login
      authStore.setUnauthenticated();
    } catch (e) {
      // Init fallita (es. Keycloak non raggiungibile o realm non configurato).
      // Mostriamo comunque il pulsante: l'errore reale emergerà al click.
      console.warn('[auth] keycloak init failed:', e);
      authStore.setUnauthenticated();
    } finally {
      checking = false;
    }
  });
</script>

<svelte:head>
  <title>Accedi — Warehouse System</title>
</svelte:head>

<div class="min-h-dvh flex items-center justify-center px-4 py-24">
  <div class="w-full max-w-sm">

    <!-- Logo -->
    <div class="text-center mb-8">
      <a href="/" class="inline-flex items-center gap-2 mb-6">
        <svg width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden="true">
          <rect width="28" height="28" rx="7" fill="#3b82f6"/>
          <path d="M6 10.5L14 6L22 10.5V17.5L14 22L6 17.5V10.5Z"
                stroke="white" stroke-width="1.8" stroke-linejoin="round" fill="none"/>
          <path d="M14 6V22M6 10.5L22 17.5M22 10.5L6 17.5"
                stroke="white" stroke-width="1.2" stroke-opacity="0.5"/>
        </svg>
        <span class="text-base font-bold tracking-tight">
          Warehouse<span class="text-blue-500">System</span>
        </span>
      </a>
      <h1 class="text-2xl font-extrabold tracking-tight mb-1">Bentornato</h1>
      <p class="text-sm text-slate-400">Accedi al tuo account per continuare.</p>
    </div>

    <!-- Card -->
    <div class="bg-surface border border-white/8 rounded-2xl p-8 flex flex-col gap-5">

      {#if checking}
        <!-- Verifica sessione in corso -->
        <div class="flex flex-col items-center gap-3 py-4 text-slate-400">
          <svg class="animate-spin" width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"
                    stroke-dasharray="42" stroke-dashoffset="14"/>
          </svg>
          <span class="text-sm">Verifica sessione in corso…</span>
        </div>

      {:else if error}
        <!-- Errore connessione -->
        <div class="flex items-start gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/25">
          <svg class="shrink-0 mt-0.5" width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <circle cx="8" cy="8" r="7" stroke="#f87171" stroke-width="1.5"/>
            <path d="M8 5v3.5M8 11h.01" stroke="#f87171" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p class="text-sm text-red-400">{error}</p>
        </div>

        <button
          on:click={() => { error = ''; checking = true; location.reload(); }}
          class="w-full flex items-center justify-center px-6 py-3 rounded-xl
                 text-sm font-semibold text-slate-300 border border-white/10
                 hover:border-white/20 hover:bg-white/5 transition-all duration-200"
        >
          Riprova
        </button>

      {:else}
        <!-- Pulsante login Keycloak -->
        <div class="flex flex-col gap-3 text-center">
          <p class="text-xs text-slate-500 leading-relaxed">
            L'accesso avviene tramite il sistema di autenticazione centralizzato.
            Verrai reindirizzato in modo sicuro.
          </p>

          <button
            on:click={() => login(`${window.location.origin}/login`)}
            class="w-full flex items-center justify-center gap-3 px-6 py-3.5 rounded-xl
                   text-sm font-semibold text-white bg-blue-500
                   hover:bg-blue-600 hover:shadow-[0_0_20px_rgba(59,130,246,0.35)]
                   transition-all duration-200"
          >
            <!-- Keycloak icon -->
            <svg width="18" height="18" viewBox="0 0 64 64" fill="none" aria-hidden="true">
              <circle cx="32" cy="32" r="30" fill="rgba(255,255,255,0.15)"/>
              <path d="M20 20h10l12 12-12 12H20l12-12L20 20z" fill="white"/>
              <path d="M34 20h10L32 32l12 12H34L22 32l12-12z" fill="rgba(255,255,255,0.6)"/>
            </svg>
            Accedi con Keycloak
          </button>
        </div>

        <!-- Divider -->
        <div class="flex items-center gap-3">
          <div class="flex-1 h-px bg-white/8"></div>
          <span class="text-xs text-slate-600">oppure</span>
          <div class="flex-1 h-px bg-white/8"></div>
        </div>

        <!-- Info sessione -->
        <div class="flex items-start gap-3 px-3 py-3 rounded-xl bg-white/[0.03] border border-white/6">
          <svg class="shrink-0 mt-0.5 text-slate-500" width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5"/>
            <path d="M8 7v4M8 5h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p class="text-xs text-slate-500 leading-relaxed">
            La sessione è legata a questa finestra del browser e termina automaticamente alla chiusura.
          </p>
        </div>
      {/if}

    </div>

    <!-- Link registrazione -->
    <p class="text-center text-sm text-slate-500 mt-6">
      Non hai un account?
      <a href="/register" class="text-blue-400 font-semibold hover:text-blue-300 transition-colors duration-200">
        Registrati
      </a>
    </p>

  </div>
</div>
