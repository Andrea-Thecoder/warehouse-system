<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { initKeycloak, getRoles } from '$lib/auth/keycloak';
  import { fetchRoles, type RoleType } from '$lib/api/lookup';
  import {
    getUsers,
    updateUserRoles,
    setUserEnabled,
    deleteUser,
    type UserDTO,
  } from '$lib/api/users';

  // ── Stato lista ───────────────────────────────────────────────────────────────

  let users:      UserDTO[] = [];
  let loading     = true;
  let error       = '';
  let currentPage = 1;
  let totalPages  = 1;
  let totalRows   = 0;
  const PAGE_SIZE = 20;

  let processingId: string | null = null;

  // ── Modal cambia ruoli ────────────────────────────────────────────────────────

  let roleModal:       UserDTO | null  = null;
  let allRoles:        RoleType[]      = [];
  let selectedRoleIds: Set<string>     = new Set();
  let rolesLoading     = false;

  async function openRoleModal(user: UserDTO) {
    roleModal       = user;
    selectedRoleIds = new Set(user.roles.map(r => r.id));
    if (allRoles.length === 0) {
      rolesLoading = true;
      try { allRoles = await fetchRoles(); } finally { rolesLoading = false; }
    }
  }

  function closeRoleModal() {
    roleModal       = null;
    selectedRoleIds = new Set();
  }

  function toggleRole(id: string) {
    if (selectedRoleIds.has(id)) selectedRoleIds.delete(id);
    else                          selectedRoleIds.add(id);
    selectedRoleIds = new Set(selectedRoleIds);
  }

  // ── Modal conferma eliminazione ───────────────────────────────────────────────

  let deleteModal: UserDTO | null = null;

  // ── Caricamento ───────────────────────────────────────────────────────────────

  async function loadPage(page: number) {
    loading = true;
    error   = '';
    try {
      const result = await getUsers({ page, size: PAGE_SIZE });
      users      = result.list;
      totalPages = result.totalPages;
      totalRows  = result.totalRows;
      currentPage = page;
    } catch {
      error = 'Impossibile caricare gli utenti. Riprova più tardi.';
    } finally {
      loading = false;
    }
  }

  // ── Azioni ────────────────────────────────────────────────────────────────────

  async function handleUpdateRoles() {
    if (!roleModal) return;
    processingId = roleModal.id;
    try {
      await updateUserRoles(roleModal.id, [...selectedRoleIds]);
      closeRoleModal();
      await loadPage(currentPage);
    } catch {
      error = 'Errore durante l\'aggiornamento dei ruoli.';
    } finally {
      processingId = null;
    }
  }

  async function handleToggleEnabled(user: UserDTO) {
    processingId = user.id;
    try {
      await setUserEnabled(user.id, !user.enabled);
      await loadPage(currentPage);
    } catch {
      error = `Errore durante la ${user.enabled ? 'disabilitazione' : 'riabilitazione'} dell'utente.`;
    } finally {
      processingId = null;
    }
  }

  async function handleDelete() {
    if (!deleteModal) return;
    processingId = deleteModal.id;
    try {
      await deleteUser(deleteModal.id);
      deleteModal = null;
      await loadPage(currentPage);
    } catch {
      error = 'Errore durante l\'eliminazione dell\'utente.';
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

  function initials(u: UserDTO): string {
    return `${u.firstName[0] ?? ''}${u.lastName[0] ?? ''}`.toUpperCase();
  }
</script>

<svelte:head>
  <title>Utenti — Warehouse System</title>
</svelte:head>

<div class="min-h-dvh pt-24 pb-16 px-6">
  <div class="max-w-5xl mx-auto">

    <!-- Header -->
    <div class="mb-10">
      <p class="text-xs font-semibold uppercase tracking-widest text-blue-500 mb-2">Area amministratore</p>
      <h1 class="text-3xl font-extrabold tracking-tight mb-1">Utenti</h1>
      <p class="text-slate-400 text-sm">Gestisci ruoli, stato e accesso degli utenti registrati.</p>
    </div>

    <!-- Errore -->
    {#if error}
      <div class="flex items-center gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/25 mb-6">
        <svg class="shrink-0" width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
          <circle cx="8" cy="8" r="7" stroke="#f87171" stroke-width="1.5"/>
          <path d="M8 5v3.5M8 11h.01" stroke="#f87171" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <p class="text-sm text-red-400">{error}</p>
      </div>
    {/if}

    <!-- Skeleton -->
    {#if loading}
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {#each [1,2,3,4] as _}
          <div class="h-44 rounded-2xl bg-surface border border-white/8 animate-pulse"></div>
        {/each}
      </div>

    <!-- Vuoto -->
    {:else if users.length === 0}
      <div class="flex flex-col items-center gap-3 py-24 text-center">
        <div class="w-14 h-14 rounded-2xl bg-slate-800 border border-white/8 flex items-center justify-center mb-2">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2M9 11a4 4 0 100-8 4 4 0 000 8zM23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"
                  stroke="#475569" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <p class="text-lg font-bold tracking-tight">Nessun utente trovato</p>
        <p class="text-sm text-slate-500">Non ci sono utenti registrati nel sistema.</p>
      </div>

    <!-- Lista -->
    {:else}
      <!-- Counter -->
      <div class="flex items-center justify-between mb-6">
        <span class="text-sm text-slate-400">
          <span class="font-bold text-slate-100">{totalRows}</span>
          {totalRows === 1 ? 'utente' : 'utenti'}
        </span>
        {#if totalPages > 1}
          <span class="text-xs text-slate-500">Pagina {currentPage} di {totalPages}</span>
        {/if}
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {#each users as user (user.id)}
          {@const isProcessing = processingId === user.id}

          <div class="bg-surface border border-white/8 rounded-2xl p-5 flex flex-col gap-4
                      transition-opacity duration-200 {isProcessing ? 'opacity-50 pointer-events-none' : ''}">

            <!-- Top: avatar + info + badge stato -->
            <div class="flex items-start gap-4">
              <div class="shrink-0 w-11 h-11 rounded-xl flex items-center justify-center text-sm font-bold
                          {user.enabled
                            ? 'bg-blue-500/15 border border-blue-500/25 text-blue-400'
                            : 'bg-slate-700/50 border border-white/8 text-slate-500'}">
                {initials(user)}
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 flex-wrap">
                  <p class="font-bold text-slate-100 truncate">{user.username}</p>
                  {#if user.enabled}
                    <span class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full
                                 bg-green-500/10 border border-green-500/20 text-green-400 font-medium shrink-0">
                      <span class="w-1.5 h-1.5 rounded-full bg-green-400"></span>
                      Attivo
                    </span>
                  {:else}
                    <span class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full
                                 bg-slate-700/50 border border-white/8 text-slate-500 font-medium shrink-0">
                      <span class="w-1.5 h-1.5 rounded-full bg-slate-500"></span>
                      Disabilitato
                    </span>
                  {/if}
                </div>
                <p class="text-sm text-slate-400 mt-0.5 truncate">{user.firstName} {user.lastName}</p>
              </div>
            </div>

            <!-- Ruoli -->
            <div class="flex flex-wrap gap-1.5 min-h-7">
              {#if user.roles.length === 0}
                <span class="text-xs text-slate-600 self-center">Nessun ruolo assegnato</span>
              {:else}
                {#each user.roles as role}
                  <span class="text-xs px-2.5 py-1 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 font-medium">
                    {role.label}
                  </span>
                {/each}
              {/if}
            </div>

            <!-- Azioni -->
            <div class="flex items-center gap-2 pt-1 border-t border-white/6 flex-wrap">

              <!-- Cambia ruoli -->
              <button
                on:click={() => openRoleModal(user)}
                disabled={isProcessing}
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                       text-blue-400 border border-blue-500/25 bg-blue-500/8
                       hover:bg-blue-500/15 hover:border-blue-500/40
                       disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150"
              >
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                  <path d="M9.5 1.5l1 1-7 7-1 0 0-1 7-7zM1 10.5h10" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Cambia ruoli
              </button>

              <!-- Disabilita — solo se l'utente è attivo -->
              <button
                on:click={() => handleToggleEnabled(user)}
                disabled={isProcessing}
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                       disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150
                       {user.enabled
                         ? 'text-amber-400 border border-amber-500/25 bg-amber-500/8 hover:bg-amber-500/15 hover:border-amber-500/40'
                         : 'text-emerald-400 border border-emerald-500/25 bg-emerald-500/8 hover:bg-emerald-500/15 hover:border-emerald-500/40'}"
              >
                {#if user.enabled}
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                    <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.4"/>
                    <path d="M4 6h4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                  </svg>
                  Disabilita
                {:else}
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                    <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.4"/>
                    <path d="M4 6h4M6 4v4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                  </svg>
                  Abilita
                {/if}
              </button>

              <!-- Elimina -->
              <button
                on:click={() => deleteModal = user}
                disabled={isProcessing}
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                       text-red-400 border border-red-500/25 bg-red-500/8
                       hover:bg-red-500/15 hover:border-red-500/40
                       disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150 ml-auto"
              >
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                  <path d="M1.5 3h9M4 3V2h4v1M5 5.5v3M7 5.5v3M2 3l.75 7.5h6.5L10 3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Elimina
              </button>

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

<!-- ── Modal Cambia Ruoli ─────────────────────────────────────────────────── -->
{#if roleModal}
  <div
    class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center px-4"
    role="dialog"
    aria-modal="true"
    aria-label="Cambia ruoli utente"
  >
    <button type="button" class="absolute inset-0 w-full h-full cursor-default"
            aria-label="Chiudi" on:click={closeRoleModal} />

    <div class="relative w-full max-w-sm bg-surface border border-white/10 rounded-2xl shadow-2xl overflow-hidden">

      <!-- Header -->
      <div class="flex items-start justify-between gap-4 px-6 pt-6 pb-4 border-b border-white/8">
        <div>
          <h2 class="text-base font-bold tracking-tight">Cambia ruoli</h2>
          <p class="text-xs text-slate-500 mt-0.5">
            {roleModal.firstName} {roleModal.lastName} · <span class="font-mono">{roleModal.username}</span>
          </p>
        </div>
        <button on:click={closeRoleModal}
                class="text-slate-500 hover:text-slate-300 transition-colors shrink-0 mt-0.5"
                aria-label="Chiudi">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
            <path d="M4 4l10 10M14 4L4 14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <!-- Ruoli -->
      <div class="px-6 py-4 flex flex-col gap-2">
        {#if rolesLoading}
          {#each [1,2,3] as _}
            <div class="h-16 rounded-xl border border-white/8 bg-white/2 animate-pulse"></div>
          {/each}
        {:else}
          {#each allRoles as role}
            {@const checked = selectedRoleIds.has(role.id)}
            <button
              type="button"
              on:click={() => toggleRole(role.id)}
              class="flex items-center gap-4 px-4 py-3.5 rounded-xl border text-left
                     transition-all duration-150
                     {checked
                       ? 'border-blue-500/60 bg-blue-500/8 shadow-[0_0_0_1px_rgba(59,130,246,0.2)]'
                       : 'border-white/8 bg-white/2 hover:border-white/15 hover:bg-white/4'}"
            >
              <!-- Testo -->
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold {checked ? 'text-slate-100' : 'text-slate-300'}">{role.label}</p>
                {#if role.description}
                  <p class="text-xs text-slate-500 mt-0.5 leading-snug">{role.description}</p>
                {/if}
              </div>

              <!-- Checkmark circolare (stesso stile register) -->
              <div class="shrink-0 w-5 h-5 rounded-full border flex items-center justify-center transition-all duration-150
                          {checked ? 'border-blue-500 bg-blue-500' : 'border-white/20 bg-transparent'}">
                {#if checked}
                  <svg width="10" height="10" viewBox="0 0 10 10" fill="none" aria-hidden="true">
                    <path d="M2 5l2.5 2.5 3.5-4" stroke="white" stroke-width="1.5"
                          stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                {/if}
              </div>
            </button>
          {/each}
        {/if}
      </div>

      <!-- Footer -->
      <div class="flex items-center gap-2 px-6 pb-6 pt-2">
        <button on:click={closeRoleModal}
                class="flex-1 py-2.5 rounded-xl text-sm font-semibold text-slate-400
                       border border-white/10 hover:bg-white/5 transition-all duration-150">
          Annulla
        </button>
        <button
          on:click={handleUpdateRoles}
          disabled={selectedRoleIds.size === 0 || processingId === roleModal?.id}
          class="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white bg-blue-500
                 hover:bg-blue-600 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-150"
        >
          {processingId === roleModal?.id ? 'Salvataggio…' : 'Salva ruoli'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- ── Modal Conferma Eliminazione ───────────────────────────────────────── -->
{#if deleteModal}
  <div
    class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center px-4"
    role="dialog"
    aria-modal="true"
    aria-label="Conferma eliminazione"
  >
    <button type="button" class="absolute inset-0 w-full h-full cursor-default"
            aria-label="Chiudi" on:click={() => deleteModal = null} />

    <div class="relative w-full max-w-sm bg-surface border border-white/10 rounded-2xl shadow-2xl p-6">

      <!-- Icona warning -->
      <div class="w-12 h-12 rounded-2xl bg-red-500/10 border border-red-500/20
                  flex items-center justify-center mx-auto mb-4">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M12 9v4M12 17h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"
                stroke="#f87171" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>

      <h2 class="text-base font-bold tracking-tight text-center mb-1">Elimina utente</h2>
      <p class="text-sm text-slate-400 text-center mb-1">
        Stai per eliminare <span class="font-semibold text-slate-100">{deleteModal.username}</span>.
      </p>
      <p class="text-xs text-slate-500 text-center mb-6">
        Questa azione è irreversibile e rimuoverà l'utente da Keycloak.
      </p>

      <div class="flex items-center gap-2">
        <button
          on:click={() => deleteModal = null}
          class="flex-1 py-2.5 rounded-xl text-sm font-semibold text-slate-400
                 border border-white/10 hover:bg-white/5 transition-all duration-150"
        >
          Annulla
        </button>
        <button
          on:click={handleDelete}
          disabled={processingId === deleteModal?.id}
          class="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white bg-red-500
                 hover:bg-red-600 disabled:opacity-40 disabled:cursor-not-allowed transition-all duration-150"
        >
          {processingId === deleteModal?.id ? 'Eliminazione…' : 'Elimina'}
        </button>
      </div>
    </div>
  </div>
{/if}
