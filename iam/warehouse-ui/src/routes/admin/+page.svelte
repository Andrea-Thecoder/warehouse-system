<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { initKeycloak, getRoles } from '$lib/auth/keycloak';
  import {
    getPendingRegistrations,
    approveRegistration,
    rejectRegistration,
    type PendingUser,
  } from '$lib/api/admin';

  // ── Stato lista ───────────────────────────────────────────────────────────────

  let users:   PendingUser[] = [];
  let loading  = true;
  let error    = '';

  // Paginazione
  let currentPage = 1;
  let totalPages  = 1;
  let totalRows   = 0;
  const PAGE_SIZE = 20;

  // ID dell'utente su cui è in corso un'azione
  let processingId: string | null = null;

  // ── Stato modal "Accetta alcuni" ──────────────────────────────────────────────

  let modalUser:      PendingUser | null = null;
  let selectedRoleIds = new Set<string>();

  function openModal(user: PendingUser) {
    modalUser       = user;
    selectedRoleIds = new Set(user.roles.map(r => r.id));
  }

  function closeModal() {
    modalUser       = null;
    selectedRoleIds = new Set();
  }

  function toggleModalRole(id: string) {
    if (selectedRoleIds.has(id)) selectedRoleIds.delete(id);
    else                          selectedRoleIds.add(id);
    selectedRoleIds = new Set(selectedRoleIds);
  }

  // ── Caricamento pagina ────────────────────────────────────────────────────────

  async function loadPage(page: number) {
    loading = true;
    error   = '';
    try {
      const result  = await getPendingRegistrations({ page, size: PAGE_SIZE });
      users         = result.list;
      totalPages    = result.totalPages;
      totalRows     = result.totalRows;
      currentPage   = page;
    } catch {
      error = 'Impossibile caricare le registrazioni. Riprova più tardi.';
    } finally {
      loading = false;
    }
  }

  // ── Azioni ────────────────────────────────────────────────────────────────────

  async function handleApprove(user: PendingUser, roleIds: string[]) {
    processingId = user.id;
    try {
      await approveRegistration(user.id, roleIds);
      users = users.filter(u => u.id !== user.id);
      totalRows = Math.max(0, totalRows - 1);
      closeModal();
    } catch {
      error = 'Errore durante l\'approvazione. Riprova.';
    } finally {
      processingId = null;
    }
  }

  async function handleReject(user: PendingUser) {
    processingId = user.id;
    try {
      await rejectRegistration(user.id);
      users = users.filter(u => u.id !== user.id);
      totalRows = Math.max(0, totalRows - 1);
    } catch {
      error = 'Errore durante il rifiuto. Riprova.';
    } finally {
      processingId = null;
    }
  }

  // ── Mount ─────────────────────────────────────────────────────────────────────

  onMount(async () => {
    await initKeycloak();

    if (!getRoles().includes('ADMIN')) {
      goto('/', { replaceState: true });
      return;
    }

    await loadPage(1);
  });

  // ── Helpers ───────────────────────────────────────────────────────────────────

  /** Iniziali leggibili dall'UUID: prime 2 cifre hex uppercase */
  function avatarLabel(id: string): string {
    return id.replace(/-/g, '').slice(0, 2).toUpperCase();
  }

  /** Colore avatar deterministico basato sul primo byte dell'UUID */
  function avatarColor(id: string): string {
    const colors = [
      'bg-blue-500/15 border-blue-500/25 text-blue-400',
      'bg-violet-500/15 border-violet-500/25 text-violet-400',
      'bg-emerald-500/15 border-emerald-500/25 text-emerald-400',
      'bg-amber-500/15 border-amber-500/25 text-amber-400',
      'bg-rose-500/15 border-rose-500/25 text-rose-400',
    ];
    const byte = parseInt(id.replace(/-/g, '').slice(0, 2), 16);
    return colors[byte % colors.length];
  }
</script>

<svelte:head>
  <title>Gestione accessi — Warehouse System</title>
</svelte:head>

<div class="min-h-dvh pt-24 pb-16 px-6">
  <div class="max-w-4xl mx-auto">

    <!-- Header -->
    <div class="mb-10">
      <p class="text-xs font-semibold uppercase tracking-widest text-blue-500 mb-2">Area amministratore</p>
      <h1 class="text-3xl font-extrabold tracking-tight mb-1">Gestione accessi</h1>
      <p class="text-slate-400 text-sm">Approva o rifiuta le richieste di registrazione in attesa.</p>
    </div>

    <!-- Errore globale -->
    {#if error}
      <div class="flex items-center gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/25 mb-6">
        <svg class="shrink-0" width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <circle cx="8" cy="8" r="7" stroke="#f87171" stroke-width="1.5"/>
          <path d="M8 5v3.5M8 11h.01" stroke="#f87171" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <p class="text-sm text-red-400">{error}</p>
      </div>
    {/if}

    <!-- Loading skeleton -->
    {#if loading}
      <div class="flex flex-col gap-4">
        {#each [1,2,3] as _}
          <div class="h-28 rounded-2xl bg-surface border border-white/8 animate-pulse"></div>
        {/each}
      </div>

    <!-- Nessun pending -->
    {:else if users.length === 0}
      <div class="flex flex-col items-center gap-3 py-24 text-center">
        <div class="w-14 h-14 rounded-2xl bg-green-500/10 border border-green-500/20 flex items-center justify-center mb-2">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M20 6L9 17l-5-5" stroke="#4ade80" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <p class="text-lg font-bold tracking-tight">Nessuna richiesta in attesa</p>
        <p class="text-sm text-slate-500">Tutte le registrazioni sono state gestite.</p>
      </div>

    <!-- Lista utenti -->
    {:else}
      <!-- Counter -->
      <div class="flex items-center justify-between mb-5">
        <span class="text-sm text-slate-400">
          <span class="font-bold text-slate-100">{totalRows}</span>
          {totalRows === 1 ? 'richiesta in attesa' : 'richieste in attesa'}
        </span>
        {#if totalPages > 1}
          <span class="text-xs text-slate-500">Pagina {currentPage} di {totalPages}</span>
        {/if}
      </div>

      <div class="flex flex-col gap-4">
        {#each users as user (user.id)}
          {@const isProcessing = processingId === user.id}

          <div class="bg-surface border border-white/8 rounded-2xl p-6
                      transition-opacity duration-200 {isProcessing ? 'opacity-50 pointer-events-none' : ''}">

            <div class="flex items-start gap-5">

              <!-- Avatar -->
              <div class="shrink-0 w-11 h-11 rounded-xl border
                          flex items-center justify-center text-sm font-bold
                          {avatarColor(user.id)}">
                {avatarLabel(user.id)}
              </div>

              <!-- Info -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-4 flex-wrap">
                  <div>
                    <p class="font-semibold text-slate-100 font-mono text-sm">{user.id}</p>
                  </div>
                </div>

                <!-- Ruoli richiesti -->
                <div class="flex flex-wrap gap-2 mt-3">
                  <span class="text-xs text-slate-600 self-center">Ruoli richiesti:</span>
                  {#each user.roles as role}
                    <span class="text-xs px-2.5 py-1 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 font-medium">
                      {role.label}
                    </span>
                  {/each}
                </div>

                <!-- Azioni -->
                <div class="flex items-center gap-2 mt-5 flex-wrap">
                  <!-- Rifiuta -->
                  <button
                    on:click={() => handleReject(user)}
                    disabled={isProcessing}
                    class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold
                           text-red-400 border border-red-500/25 bg-red-500/8
                           hover:bg-red-500/15 hover:border-red-500/40
                           disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150"
                  >
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                      <path d="M2 2l8 8M10 2l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Rifiuta
                  </button>

                  <!-- Accetta alcuni -->
                  <button
                    on:click={() => openModal(user)}
                    disabled={isProcessing}
                    class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold
                           text-slate-300 border border-white/12 bg-white/4
                           hover:bg-white/8 hover:border-white/20
                           disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150"
                  >
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                      <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M4 6h4M6 4v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    Accetta alcuni
                  </button>

                  <!-- Accetta tutto -->
                  <button
                    on:click={() => handleApprove(user, user.roles.map(r => r.id))}
                    disabled={isProcessing}
                    class="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold
                           text-green-400 border border-green-500/25 bg-green-500/8
                           hover:bg-green-500/15 hover:border-green-500/40
                           disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150"
                  >
                    {#if isProcessing}
                      <svg class="animate-spin" width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                        <circle cx="6" cy="6" r="4.5" stroke="currentColor" stroke-width="1.5" stroke-dasharray="20" stroke-dashoffset="7"/>
                      </svg>
                    {:else}
                      <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                        <path d="M2 6l3 3 5-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                    {/if}
                    Accetta tutto
                  </button>
                </div>
              </div>
            </div>
          </div>
        {/each}
      </div>

      <!-- Paginazione -->
      {#if totalPages > 1}
        <div class="flex items-center justify-center gap-2 mt-8">
          <button
            on:click={() => loadPage(currentPage - 1)}
            disabled={currentPage <= 1 || loading}
            class="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400
                   border border-white/8 hover:border-white/20 hover:bg-white/5
                   disabled:opacity-30 disabled:cursor-not-allowed transition-all duration-150"
          >
            ← Precedente
          </button>

          <span class="text-xs text-slate-500 px-2">{currentPage} / {totalPages}</span>

          <button
            on:click={() => loadPage(currentPage + 1)}
            disabled={currentPage >= totalPages || loading}
            class="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400
                   border border-white/8 hover:border-white/20 hover:bg-white/5
                   disabled:opacity-30 disabled:cursor-not-allowed transition-all duration-150"
          >
            Successiva →
          </button>
        </div>
      {/if}
    {/if}

  </div>
</div>

<!-- ── Modal "Accetta alcuni" ─────────────────────────────────────────────── -->
{#if modalUser}
  <div
    class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center px-4"
    role="dialog"
    aria-modal="true"
    aria-label="Seleziona ruoli da approvare"
  >
    <button
      type="button"
      class="absolute inset-0 w-full h-full cursor-default"
      aria-label="Chiudi dialogo"
      on:click={closeModal}
    />

    <div class="relative w-full max-w-sm bg-surface border border-white/10 rounded-2xl shadow-2xl overflow-hidden">

      <!-- Header modal -->
      <div class="flex items-start justify-between gap-4 px-6 pt-6 pb-4 border-b border-white/8">
        <div>
          <h2 class="text-base font-bold tracking-tight">Seleziona ruoli da approvare</h2>
          <p class="text-xs text-slate-500 mt-0.5 font-mono">{modalUser.id}</p>
        </div>
        <button
          on:click={closeModal}
          class="text-slate-500 hover:text-slate-300 transition-colors shrink-0 mt-0.5"
          aria-label="Chiudi"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
            <path d="M4 4l10 10M14 4L4 14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <!-- Ruoli -->
      <div class="px-6 py-4 flex flex-col gap-2">
        <p class="text-xs text-slate-500 mb-1">Deseleziona i ruoli che non vuoi approvare.</p>

        {#each modalUser.roles as role}
          {@const checked = selectedRoleIds.has(role.id)}
          <button
            type="button"
            on:click={() => toggleModalRole(role.id)}
            class="flex items-center gap-3 px-4 py-3 rounded-xl border text-left transition-all duration-150
                   {checked
                     ? 'border-blue-500/50 bg-blue-500/8'
                     : 'border-white/8 bg-white/2 opacity-50'}"
          >
            <div class="shrink-0 w-5 h-5 rounded-md border flex items-center justify-center transition-all duration-150
                        {checked ? 'border-blue-500 bg-blue-500' : 'border-white/25 bg-transparent'}">
              {#if checked}
                <svg width="10" height="10" viewBox="0 0 10 10" fill="none" aria-hidden="true">
                  <path d="M2 5l2.5 2.5 3.5-4" stroke="white" stroke-width="1.5"
                        stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              {/if}
            </div>
            <p class="text-sm font-semibold {checked ? 'text-slate-100' : 'text-slate-400'}">{role.label}</p>
          </button>
        {/each}
      </div>

      <!-- Footer modal -->
      <div class="flex items-center gap-2 px-6 pb-6 pt-2">
        <button
          on:click={closeModal}
          class="flex-1 py-2.5 rounded-xl text-sm font-semibold text-slate-400
                 border border-white/10 hover:bg-white/5 transition-all duration-150"
        >
          Annulla
        </button>
        <button
          on:click={() => modalUser && handleApprove(modalUser, [...selectedRoleIds])}
          disabled={selectedRoleIds.size === 0 || processingId === modalUser?.id}
          class="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white bg-blue-500
                 hover:bg-blue-600 disabled:opacity-40 disabled:cursor-not-allowed
                 transition-all duration-150"
        >
          {#if processingId === modalUser?.id}
            Approvazione…
          {:else}
            Conferma approvazione
          {/if}
        </button>
      </div>

    </div>
  </div>
{/if}
