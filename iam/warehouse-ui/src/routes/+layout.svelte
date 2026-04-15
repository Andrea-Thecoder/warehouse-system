<script lang="ts">
  import '../app.css';
  import { onMount } from 'svelte';
  import { goto, afterNavigate } from '$app/navigation';
  import { initKeycloak, logout } from '$lib/auth/keycloak';
  import { authStore } from '$lib/stores/authStore';
  import { fetchRoles, type RoleType } from '$lib/api/lookup';

  // Route che non richiedono auth check al boot
  const PUBLIC_ROUTES = ['/', '/login', '/register', '/pending'];

  let scrolled    = false;
  let currentPath = '/';

  // Aggiorna currentPath ad ogni navigazione
  afterNavigate(({ to }) => {
    currentPath = to?.url.pathname ?? window.location.pathname;
  });

  function onScroll() {
    scrolled = window.scrollY > 10;
  }

  onMount(async () => {
    currentPath = window.location.pathname;
    const isPublic = PUBLIC_ROUTES.some(r => currentPath === r);

    // Init sempre passivo (no redirect automatici): risolve isLoading in ogni caso.
    // Se l'utente ha una sessione attiva → popola authStore.
    // Se non autenticato → isLoading = false (i bottoni Accedi/Registrati appaiono).
    const authenticated = await initKeycloak();

    if (!authenticated) {
      authStore.setUnauthenticated();
      // Redirect a /login solo sulle route protette
      if (!isPublic) {
        goto('/login', { replaceState: true });
      }
    }
  });

  // Iniziali utente per l'avatar
  function initials(firstName: string, lastName: string): string {
    return `${firstName[0] ?? ''}${lastName[0] ?? ''}`.toUpperCase();
  }

  // Ruoli disponibili dal backend (caricati una volta sola al login)
  let allRoles: RoleType[] = [];
  let userRoleLabel = '';

  async function loadRoleLabel(userRoleIds: string[]) {
    if (allRoles.length === 0) {
      try { allRoles = await fetchRoles(); } catch { return; }
    }
    // Primo ruolo dell'utente che esiste nel catalogo del backend
    const match = userRoleIds
      .map(id => allRoles.find(r => r.id === id))
      .find(Boolean);
    userRoleLabel = match?.label ?? '';
  }

  // Carica la label ogni volta che cambia lo stato auth
  $: if ($authStore.isAuthenticated && $authStore.user) {
    loadRoleLabel($authStore.user.roles);
  } else {
    userRoleLabel = '';
  }

  // Link di navigazione per route protette in base al ruolo
  $: appNavLinks = $authStore.user?.roles.includes('ADMIN')
    ? [
        { href: '/admin',       label: 'Gestione accessi' },
        { href: '/admin/users', label: 'Utenti' },
      ]
    : [{ href: '/dashboard', label: 'Dashboard' }];

  $: isHomepage = currentPath === '/';
</script>

<svelte:window on:scroll={onScroll} />

<header
  class="fixed top-0 left-0 right-0 z-50 px-8 transition-all duration-200
    {scrolled ? 'bg-bg/85 backdrop-blur-lg border-b border-white/10' : ''}"
>
  <div class="max-w-6xl mx-auto h-17 flex items-center gap-8">

    <!-- Logo -->
    <a href="/" class="flex items-center gap-2 shrink-0">
      <svg width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden="true">
        <rect width="28" height="28" rx="7" fill="#3b82f6"/>
        <path d="M6 10.5L14 6L22 10.5V17.5L14 22L6 17.5V10.5Z"
              stroke="white" stroke-width="1.8" stroke-linejoin="round" fill="none"/>
        <path d="M14 6V22M6 10.5L22 17.5M22 10.5L6 17.5"
              stroke="white" stroke-width="1.2" stroke-opacity="0.5"/>
      </svg>
      <span class="text-[1.05rem] font-bold tracking-tight text-slate-100">
        Warehouse<span class="text-blue-500">System</span>
      </span>
    </a>

    <!-- Nav links: homepage anchors se non autenticato, app links se autenticato -->
    <nav class="hidden md:flex items-center gap-6 flex-1" aria-label="Navigazione principale">
      {#if $authStore.isAuthenticated}
        {#each appNavLinks as link}
          <a href={link.href}
             class="text-sm font-medium transition-colors duration-200
                    {currentPath.startsWith(link.href)
                      ? 'text-slate-100'
                      : 'text-slate-400 hover:text-slate-100'}">
            {link.label}
          </a>
        {/each}
      {:else if isHomepage}
        <a href="#ecosystem" class="text-sm font-medium text-slate-400 hover:text-slate-100 transition-colors duration-200">La piattaforma</a>
        <a href="#features"  class="text-sm font-medium text-slate-400 hover:text-slate-100 transition-colors duration-200">Funzionalità</a>
        <a href="#about"     class="text-sm font-medium text-slate-400 hover:text-slate-100 transition-colors duration-200">Infrastruttura</a>
      {/if}
    </nav>

    <!-- Actions: chip utente + logout se autenticato, altrimenti Accedi/Registrati -->
    <div class="flex items-center gap-3 shrink-0 ml-auto">
      {#if $authStore.isAuthenticated && $authStore.user}
        {@const user = $authStore.user}

        <!-- Avatar + nome -->
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-lg bg-blue-500/20 border border-blue-500/30
                      flex items-center justify-center text-blue-400 text-xs font-bold shrink-0">
            {initials(user.firstName, user.lastName)}
          </div>
          <div class="hidden sm:block leading-tight">
            <p class="text-sm font-semibold text-slate-100 leading-none">{user.firstName} {user.lastName}</p>
            {#if userRoleLabel}
              <p class="text-xs text-blue-400/80 mt-0.5">{userRoleLabel}</p>
            {:else}
              <p class="text-xs text-slate-500 mt-0.5">{user.username}</p>
            {/if}
          </div>
        </div>

        <!-- Separatore -->
        <div class="w-px h-5 bg-white/10"></div>

        <!-- Logout -->
        <button
          on:click={logout}
          class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold
                 text-slate-400 border border-white/8 hover:text-red-400 hover:border-red-500/25
                 hover:bg-red-500/8 transition-all duration-200"
          aria-label="Esci dall'account"
        >
          <svg width="13" height="13" viewBox="0 0 13 13" fill="none" aria-hidden="true">
            <path d="M5 2H2.5A1.5 1.5 0 001 3.5v6A1.5 1.5 0 002.5 11H5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
            <path d="M8.5 9L11.5 6.5 8.5 4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M11.5 6.5H5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
          Esci
        </button>

      {:else if !$authStore.isLoading}
        <a href="/login"
           class="px-4 py-2 rounded-full text-sm font-semibold text-slate-400 border border-white/10
                  hover:text-slate-100 hover:border-white/25 hover:bg-white/5 transition-all duration-200">
          Accedi
        </a>
        <a href="/register"
           class="px-4 py-2 rounded-full text-sm font-semibold text-white bg-blue-500
                  hover:bg-blue-600 hover:shadow-[0_0_20px_rgba(59,130,246,0.35)]
                  transition-all duration-200">
          Registrati
        </a>
      {/if}
    </div>

  </div>
</header>

<main class="min-h-dvh">
  <slot />
</main>
