<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/authStore';
  import { initKeycloak, getRoles } from '$lib/auth/keycloak';

  let loading = true;

  onMount(async () => {
    await initKeycloak();

    const roles = getRoles();

    if (roles.includes('ADMIN')) {
      goto('/admin', { replaceState: true });
      return;
    }

    if (!roles.includes('USER')) {
      goto('/pending', { replaceState: true });
      return;
    }

    loading = false;
  });

  function initials(firstName: string, lastName: string): string {
    return `${firstName[0] ?? ''}${lastName[0] ?? ''}`.toUpperCase();
  }
</script>

<svelte:head>
  <title>Dashboard — Warehouse System</title>
</svelte:head>

{#if !loading}
  <div class="min-h-dvh pt-24 pb-16 px-6">
    <div class="max-w-4xl mx-auto">

      <!-- Header -->
      <div class="mb-10">
        <p class="text-xs font-semibold uppercase tracking-widest text-blue-500 mb-2">Area utente</p>
        <h1 class="text-3xl font-extrabold tracking-tight mb-1">Il mio account</h1>
        <p class="text-slate-400 text-sm">Informazioni sul tuo profilo e sui ruoli assegnati.</p>
      </div>

      {#if $authStore.user}
        {@const user = $authStore.user}

        <!-- Profilo -->
        <div class="bg-surface border border-white/8 rounded-2xl p-6 mb-6">
          <div class="flex items-center gap-5">

            <!-- Avatar grande -->
            <div class="shrink-0 w-16 h-16 rounded-2xl bg-blue-500/15 border border-blue-500/25
                        flex items-center justify-center text-blue-400 text-xl font-bold">
              {initials(user.firstName, user.lastName)}
            </div>

            <!-- Info -->
            <div class="flex-1 min-w-0">
              <p class="text-lg font-bold text-slate-100">{user.firstName} {user.lastName}</p>
              <p class="text-sm text-slate-400 mt-0.5">{user.username}</p>
              <p class="text-sm text-slate-500">{user.email}</p>
            </div>

            <!-- Badge stato -->
            <div class="shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-full
                        bg-green-500/10 border border-green-500/20">
              <span class="w-1.5 h-1.5 rounded-full bg-green-400"></span>
              <span class="text-xs font-semibold text-green-400">Attivo</span>
            </div>
          </div>
        </div>

        <!-- Ruoli assegnati -->
        <div class="bg-surface border border-white/8 rounded-2xl overflow-hidden">
          <div class="px-6 py-4 border-b border-white/8">
            <h2 class="text-sm font-bold tracking-tight">Ruoli assegnati</h2>
            <p class="text-xs text-slate-500 mt-0.5">Permessi attivi sul tuo account.</p>
          </div>

          <div class="px-6 py-4 flex flex-col gap-3">
            {#if user.roles.filter(r => !['default-roles-warehouse-realm', 'offline_access', 'uma_authorization'].includes(r)).length === 0}
              <p class="text-sm text-slate-500 py-4 text-center">Nessun ruolo applicativo assegnato.</p>
            {:else}
              {#each user.roles.filter(r => !['default-roles-warehouse-realm', 'offline_access', 'uma_authorization'].includes(r)) as role}
                <div class="flex items-center gap-3 px-4 py-3 rounded-xl
                            border border-blue-500/20 bg-blue-500/5">
                  <div class="w-8 h-8 rounded-lg bg-blue-500/15 border border-blue-500/25
                              flex items-center justify-center shrink-0">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                      <path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                            stroke="#60a5fa" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold text-slate-100 capitalize">{role}</p>
                  </div>
                  <span class="text-xs px-2 py-0.5 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 font-medium">
                    Attivo
                  </span>
                </div>
              {/each}
            {/if}
          </div>
        </div>

      {/if}
    </div>
  </div>
{/if}
