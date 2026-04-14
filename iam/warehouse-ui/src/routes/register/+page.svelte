<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { register, type ApiError } from '$lib/api/auth';
  import { fetchRoles, type RoleType } from '$lib/api/lookup';

  // ── Stato form ───────────────────────────────────────────────────────────────

  let firstName = '';
  let lastName  = '';
  let username  = '';
  let email     = '';
  let password  = '';
  let confirm   = '';

  let loading  = false;
  let apiError = '';

  // ── Ruoli dal backend ─────────────────────────────────────────────────────────

  let roles: RoleType[] = [];
  let rolesLoading  = true;
  let rolesError    = '';

  let selectedRoles = new Set<string>();

  function toggleRole(id: string) {
    if (selectedRoles.has(id)) selectedRoles.delete(id);
    else                        selectedRoles.add(id);
    selectedRoles = new Set(selectedRoles);
    if (selectedRoles.size > 0) { delete errors.roles; errors = { ...errors }; }
  }

  onMount(async () => {
    try {
      roles = await fetchRoles();
    } catch {
      rolesError = 'Impossibile caricare i ruoli. Ricarica la pagina.';
    } finally {
      rolesLoading = false;
    }
  });

  // Errori per campo
  let errors: Partial<Record<'firstName' | 'lastName' | 'username' | 'email' | 'password' | 'confirm' | 'roles', string>> = {};

  // ── Validazione ──────────────────────────────────────────────────────────────

  function validate(): boolean {
    errors = {};

    if (!firstName.trim())
      errors.firstName = 'Il nome è obbligatorio.';

    if (!lastName.trim())
      errors.lastName = 'Il cognome è obbligatorio.';

    if (!username.trim()) {
      errors.username = "L'username è obbligatorio.";
    } else if (!/^[a-zA-Z0-9._-]{3,30}$/.test(username)) {
      errors.username = 'Tra 3 e 30 caratteri: lettere, numeri, punto, trattino, underscore.';
    }

    if (!email.trim()) {
      errors.email = "L'email è obbligatoria.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errors.email = 'Inserisci un indirizzo email valido.';
    }

    if (!password) {
      errors.password = 'La password è obbligatoria.';
    } else if (password.length < 8) {
      errors.password = 'La password deve contenere almeno 8 caratteri.';
    } else if (!/[A-Z]/.test(password) || !/[0-9]/.test(password)) {
      errors.password = 'La password deve contenere almeno una lettera maiuscola e un numero.';
    }

    if (!confirm) {
      errors.confirm = 'Conferma la password.';
    } else if (confirm !== password) {
      errors.confirm = 'Le password non coincidono.';
    }

    if (selectedRoles.size === 0)
      errors.roles = 'Seleziona almeno un ruolo.';

    return Object.keys(errors).length === 0;
  }

  // ── Submit ───────────────────────────────────────────────────────────────────

  async function handleSubmit(e: SubmitEvent) {
    e.preventDefault();
    apiError = '';

    if (!validate()) return;

    loading = true;
    try {
      await register({
        username:       username.trim(),
        email:          email.trim(),
        firstName:      firstName.trim(),
        lastName:       lastName.trim(),
        password,
        requestedRoleIds: [...selectedRoles],
      });
      goto('/pending');
    } catch (err) {
      const apiErr = err as ApiError;
      if (apiErr.status === 409) {
        errors.email = 'Esiste già un account con questa email.';
      } else {
        apiError = apiErr.message ?? 'Si è verificato un errore. Riprova.';
      }
    } finally {
      loading = false;
    }
  }

  // Forza validazione live sul campo confirm quando password cambia
  $: if (confirm && password) {
    if (confirm !== password) {
      errors.confirm = 'Le password non coincidono.';
    } else {
      delete errors.confirm;
      errors = { ...errors };
    }
  }
</script>

<svelte:head>
  <title>Registrazione — Warehouse System</title>
</svelte:head>

<div class="min-h-dvh flex items-center justify-center px-4 py-24">

  <!-- Card -->
  <div class="w-full max-w-md">

    <!-- Header -->
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
      <h1 class="text-2xl font-extrabold tracking-tight mb-1">Crea il tuo account</h1>
      <p class="text-sm text-slate-400">
        Dopo la registrazione il tuo account sarà in attesa di approvazione.
      </p>
    </div>

    <!-- Form card -->
    <div class="bg-surface border border-white/8 rounded-2xl p-8">

      <!-- Errore globale API -->
      {#if apiError}
        <div class="flex items-start gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/25 mb-6">
          <svg class="shrink-0 mt-0.5" width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <circle cx="8" cy="8" r="7" stroke="#f87171" stroke-width="1.5"/>
            <path d="M8 5v3.5M8 11h.01" stroke="#f87171" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p class="text-sm text-red-400">{apiError}</p>
        </div>
      {/if}

      <form on:submit={handleSubmit} novalidate class="flex flex-col gap-5">

        <!-- Nome + Cognome -->
        <div class="grid grid-cols-2 gap-4">
          <div class="flex flex-col gap-1.5">
            <label for="firstName" class="text-xs font-semibold text-slate-400 uppercase tracking-wide">Nome</label>
            <input
              id="firstName"
              type="text"
              autocomplete="given-name"
              bind:value={firstName}
              placeholder="Mario"
              class="field"
              class:field--error={errors.firstName}
              disabled={loading}
            />
            {#if errors.firstName}
              <p class="field-error">{errors.firstName}</p>
            {/if}
          </div>

          <div class="flex flex-col gap-1.5">
            <label for="lastName" class="text-xs font-semibold text-slate-400 uppercase tracking-wide">Cognome</label>
            <input
              id="lastName"
              type="text"
              autocomplete="family-name"
              bind:value={lastName}
              placeholder="Rossi"
              class="field"
              class:field--error={errors.lastName}
              disabled={loading}
            />
            {#if errors.lastName}
              <p class="field-error">{errors.lastName}</p>
            {/if}
          </div>
        </div>

        <!-- Username -->
        <div class="flex flex-col gap-1.5">
          <label for="username" class="text-xs font-semibold text-slate-400 uppercase tracking-wide">Username</label>
          <input
            id="username"
            type="text"
            autocomplete="username"
            bind:value={username}
            placeholder="mario.rossi"
            class="field"
            class:field--error={errors.username}
            disabled={loading}
          />
          {#if errors.username}
            <p class="field-error">{errors.username}</p>
          {/if}
        </div>

        <!-- Email -->
        <div class="flex flex-col gap-1.5">
          <label for="email" class="text-xs font-semibold text-slate-400 uppercase tracking-wide">Email</label>
          <input
            id="email"
            type="email"
            autocomplete="email"
            bind:value={email}
            placeholder="mario.rossi@azienda.it"
            class="field"
            class:field--error={errors.email}
            disabled={loading}
          />
          {#if errors.email}
            <p class="field-error">{errors.email}</p>
          {/if}
        </div>

        <!-- Password -->
        <div class="flex flex-col gap-1.5">
          <label for="password" class="text-xs font-semibold text-slate-400 uppercase tracking-wide">Password</label>
          <input
            id="password"
            type="password"
            autocomplete="new-password"
            bind:value={password}
            placeholder="Min. 8 caratteri, 1 maiuscola, 1 numero"
            class="field"
            class:field--error={errors.password}
            disabled={loading}
          />
          {#if errors.password}
            <p class="field-error">{errors.password}</p>
          {/if}
        </div>

        <!-- Conferma password -->
        <div class="flex flex-col gap-1.5">
          <label for="confirm" class="text-xs font-semibold text-slate-400 uppercase tracking-wide">Conferma password</label>
          <input
            id="confirm"
            type="password"
            autocomplete="new-password"
            bind:value={confirm}
            placeholder="Ripeti la password"
            class="field"
            class:field--error={errors.confirm}
            disabled={loading}
          />
          {#if errors.confirm}
            <p class="field-error">{errors.confirm}</p>
          {/if}
        </div>

        <!-- Ruoli -->
        <div class="flex flex-col gap-2">
          <span class="text-xs font-semibold text-slate-400 uppercase tracking-wide">
            Ruolo richiesto
          </span>

          <div class="flex flex-col gap-2">
            {#if rolesLoading}
              <!-- Skeleton -->
              {#each [1, 2, 3] as _}
                <div class="h-16 rounded-xl border border-white/8 bg-white/[0.02] animate-pulse"></div>
              {/each}

            {:else if rolesError}
              <div class="flex items-start gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/25">
                <svg class="shrink-0 mt-0.5" width="14" height="14" viewBox="0 0 16 16" fill="none" aria-hidden="true">
                  <circle cx="8" cy="8" r="7" stroke="#f87171" stroke-width="1.5"/>
                  <path d="M8 5v3.5M8 11h.01" stroke="#f87171" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <p class="text-xs text-red-400">{rolesError}</p>
              </div>

            {:else}
              {#each roles as role}
                {@const selected = selectedRoles.has(role.id)}
                <button
                  type="button"
                  on:click={() => toggleRole(role.id)}
                  disabled={loading}
                  class="flex items-center gap-4 px-4 py-3.5 rounded-xl border text-left
                         transition-all duration-150 disabled:opacity-50 disabled:cursor-not-allowed
                         {selected
                           ? 'border-blue-500/60 bg-blue-500/8 shadow-[0_0_0_1px_rgba(59,130,246,0.2)]'
                           : 'border-white/8 bg-white/[0.02] hover:border-white/15 hover:bg-white/[0.04]'}"
                >
                  <!-- Testo -->
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold {selected ? 'text-slate-100' : 'text-slate-300'}">
                      {role.label}
                    </p>
                    {#if role.description}
                      <p class="text-xs text-slate-500 mt-0.5 leading-snug">{role.description}</p>
                    {/if}
                  </div>

                  <!-- Checkmark -->
                  <div class="shrink-0 w-5 h-5 rounded-full border flex items-center justify-center transition-all duration-150
                              {selected ? 'border-blue-500 bg-blue-500' : 'border-white/20 bg-transparent'}">
                    {#if selected}
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

          {#if errors.roles}
            <p class="field-error">{errors.roles}</p>
          {/if}
        </div>

        <!-- Submit -->
        <button
          type="submit"
          disabled={loading}
          class="mt-1 w-full flex items-center justify-center gap-2 px-6 py-3 rounded-xl
                 text-sm font-semibold text-white bg-blue-500
                 hover:bg-blue-600 hover:shadow-[0_0_20px_rgba(59,130,246,0.35)]
                 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:bg-blue-500 disabled:hover:shadow-none
                 transition-all duration-200"
        >
          {#if loading}
            <svg class="animate-spin" width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
              <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="2" stroke-dasharray="28" stroke-dashoffset="10"/>
            </svg>
            Registrazione in corso…
          {:else}
            Crea account
          {/if}
        </button>

      </form>
    </div>

    <!-- Link login -->
    <p class="text-center text-sm text-slate-500 mt-6">
      Hai già un account?
      <a href="/login" class="text-blue-400 font-semibold hover:text-blue-300 transition-colors duration-200">
        Accedi
      </a>
    </p>

  </div>
</div>

<style>
  :global(.field) {
    width: 100%;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 0.625rem 0.875rem;
    color: #f1f5f9;
    font-size: 0.875rem;
    font-family: inherit;
    outline: none;
    transition: border-color 150ms, box-shadow 150ms;
  }

  :global(.field::placeholder) {
    color: #475569;
  }

  :global(.field:focus) {
    border-color: rgba(59, 130, 246, 0.6);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
  }

  :global(.field--error) {
    border-color: rgba(239, 68, 68, 0.6) !important;
  }

  :global(.field--error:focus) {
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15) !important;
  }

  :global(.field:disabled) {
    opacity: 0.5;
    cursor: not-allowed;
  }

  :global(.field-error) {
    font-size: 0.75rem;
    color: #f87171;
    margin-top: 0.1rem;
  }
</style>
