<template>
  <div class="min-h-screen bg-white text-gray-800 antialiased">
    <!-- ============== HEADER ============== -->
    <header class="sticky top-0 z-40 backdrop-blur bg-white/80 border-b border-gray-100">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <a href="#hero" class="flex items-center gap-2 group">
          <span class="inline-flex items-center justify-center w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-600 to-sky-500 text-white shadow-sm">
            <svg viewBox="0 0 24 24" class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12l18-9-4 9 4 9z"/>
            </svg>
          </span>
          <span class="font-bold text-lg tracking-tight group-hover:text-indigo-600 transition">Trippy</span>
        </a>

        <nav class="hidden md:flex items-center gap-8 text-sm font-medium text-gray-600">
          <a href="#features" class="hover:text-indigo-600 transition">Funkcje</a>
          <a href="#how" class="hover:text-indigo-600 transition">Jak to działa</a>
          <a href="#screens" class="hover:text-indigo-600 transition">Ekrany</a>
          <a href="#faq" class="hover:text-indigo-600 transition">FAQ</a>
        </nav>

        <div class="flex items-center gap-3">
          <router-link
            to="/login"
            class="hidden sm:inline-flex text-xs text-gray-500 hover:text-indigo-600 transition"
            title="Panel administratora"
          >
            Panel admina
          </router-link>
          <a
            href="#download"
            class="inline-flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium px-4 py-2 rounded-lg shadow-sm transition"
          >
            Pobierz
          </a>
        </div>
      </div>
    </header>

    <!-- ============== HERO ============== -->
    <section id="hero" class="relative overflow-hidden">
      <div class="absolute inset-0 -z-10 bg-gradient-to-br from-indigo-50 via-white to-sky-50"></div>
      <div class="absolute -top-24 -right-24 w-96 h-96 bg-indigo-200/40 rounded-full blur-3xl -z-10"></div>
      <div class="absolute -bottom-24 -left-24 w-96 h-96 bg-sky-200/40 rounded-full blur-3xl -z-10"></div>

      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20 lg:py-28 grid lg:grid-cols-2 gap-12 items-center">
        <div>
          <span class="inline-flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-indigo-700 bg-indigo-100 px-3 py-1 rounded-full">
            <span class="w-1.5 h-1.5 rounded-full bg-indigo-600"></span>
            Aplikacja na Androida
          </span>
          <h1 class="mt-5 text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-gray-900 leading-tight">
            Wspólne podróże.<br/>
            <span class="bg-gradient-to-r from-indigo-600 to-sky-500 bg-clip-text text-transparent">Czyste rozliczenia.</span>
          </h1>
          <p class="mt-6 text-lg text-gray-600 max-w-xl leading-relaxed">
            Trippy to kompleksowa aplikacja mobilna dla osób podróżujących w grupach.
            Transparentnie zarządzaj budżetem wyjazdu, dodawaj wydatki na bieżąco
            i automatycznie rozliczaj koszty między uczestnikami — bez arkuszy kalkulacyjnych.
          </p>
          <div class="mt-8 flex flex-wrap gap-3">
            <a
              href="#download"
              class="inline-flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium px-6 py-3 rounded-xl shadow-md hover:shadow-lg transition"
            >
              <svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">
                <path d="M17.523 15.341c-.354 0-.642-.288-.642-.642s.288-.642.642-.642.642.288.642.642-.288.642-.642.642m-11.046 0c-.354 0-.642-.288-.642-.642s.288-.642.642-.642.642.288.642.642-.288.642-.642.642m11.277-6.155l1.281-2.219a.266.266 0 00-.097-.363.266.266 0 00-.363.097l-1.297 2.247a8.084 8.084 0 00-3.31-.703c-1.182 0-2.302.252-3.31.703L9.36 6.701a.266.266 0 00-.363-.097.266.266 0 00-.097.363l1.281 2.219C7.973 10.387 6.18 12.789 6 15.6h12c-.18-2.811-1.973-5.213-4.246-6.414"/>
              </svg>
              Pobierz na Androida
            </a>
            <a
              href="#features"
              class="inline-flex items-center gap-2 bg-white hover:bg-gray-50 text-gray-800 font-medium px-6 py-3 rounded-xl border border-gray-200 transition"
            >
              Zobacz funkcje
            </a>
          </div>
          <div class="mt-8 flex items-center gap-6 text-sm text-gray-500">
            <div class="flex items-center gap-2">
              <svg viewBox="0 0 24 24" class="w-4 h-4 text-emerald-500" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
              Android 8.0+
            </div>
            <div class="flex items-center gap-2">
              <svg viewBox="0 0 24 24" class="w-4 h-4 text-emerald-500" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
              Bezpieczne
            </div>
          </div>
        </div>

        <!-- Mockup telefonu -->
        <div class="relative flex justify-center lg:justify-end">
          <div class="relative w-[280px] h-[560px] rounded-[2.5rem] border-[10px] border-gray-900 bg-gray-900 shadow-2xl">
            <div class="absolute top-0 left-1/2 -translate-x-1/2 w-32 h-6 bg-gray-900 rounded-b-2xl z-10"></div>
            <div class="w-full h-full rounded-[1.8rem] overflow-hidden bg-gradient-to-br from-indigo-500 via-indigo-600 to-sky-600 p-5 text-white flex flex-col">
              <div class="flex justify-between text-xs opacity-70 pt-2">
                <span>9:41</span>
                <span>100%</span>
              </div>
              <div class="mt-8">
                <p class="text-xs opacity-80 uppercase tracking-wider">Aktualna podróż</p>
                <h3 class="text-2xl font-bold mt-1">Włochy 2025</h3>
                <p class="text-sm opacity-80">12 — 22 czerwca · 4 osoby</p>
              </div>
              <div class="mt-6 bg-white/15 backdrop-blur rounded-2xl p-4">
                <p class="text-xs opacity-80">Twój bilans</p>
                <p class="text-3xl font-bold mt-1">+ 142,50 €</p>
                <p class="text-xs opacity-80 mt-1">Grupa jest Ci winna</p>
              </div>
              <div class="mt-4 space-y-2">
                <div class="bg-white/10 rounded-xl p-3 flex justify-between text-sm">
                  <span>Paliwo</span><span class="font-semibold">80 €</span>
                </div>
                <div class="bg-white/10 rounded-xl p-3 flex justify-between text-sm">
                  <span>Hotel Roma</span><span class="font-semibold">320 €</span>
                </div>
                <div class="bg-white/10 rounded-xl p-3 flex justify-between text-sm">
                  <span>Kolacja</span><span class="font-semibold">62,50 €</span>
                </div>
              </div>
              <div class="mt-auto bg-white text-indigo-600 text-center py-3 rounded-xl font-semibold text-sm">
                + Dodaj wydatek
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============== FEATURES ============== -->
    <section id="features" class="py-20 lg:py-28">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="max-w-2xl">
          <h2 class="text-3xl sm:text-4xl font-bold tracking-tight text-gray-900">Wszystko czego potrzebujecie na wspólny wyjazd</h2>
          <p class="mt-4 text-gray-600 text-lg">Od planowania po końcowe rozliczenie — Trippy ogarnia całą organizacyjną stronę grupowej podróży.</p>
        </div>

        <div class="mt-12 grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="f in features" :key="f.title" class="group p-6 rounded-2xl border border-gray-100 bg-white hover:border-indigo-200 hover:shadow-lg transition">
            <div class="w-11 h-11 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center group-hover:bg-indigo-600 group-hover:text-white transition" v-html="f.icon"></div>
            <h3 class="mt-5 font-semibold text-lg text-gray-900">{{ f.title }}</h3>
            <p class="mt-2 text-gray-600 text-sm leading-relaxed">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ============== HOW IT WORKS ============== -->
    <section id="how" class="py-20 lg:py-28 bg-gray-50 border-y border-gray-100">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="max-w-2xl">
          <h2 class="text-3xl sm:text-4xl font-bold tracking-tight text-gray-900">Jak to działa</h2>
          <p class="mt-4 text-gray-600 text-lg">Trzy proste kroki dzielą Was od bezstresowego wyjazdu.</p>
        </div>

        <div class="mt-12 grid md:grid-cols-3 gap-6">
          <div v-for="(s, idx) in steps" :key="s.title" class="relative bg-white rounded-2xl p-6 border border-gray-100">
            <div class="absolute -top-4 left-6 w-9 h-9 rounded-full bg-gradient-to-br from-indigo-600 to-sky-500 text-white flex items-center justify-center font-bold shadow-md">{{ idx + 1 }}</div>
            <h3 class="mt-4 font-semibold text-lg text-gray-900">{{ s.title }}</h3>
            <p class="mt-2 text-gray-600 text-sm leading-relaxed">{{ s.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ============== SCREENS ============== -->
    <section id="screens" class="py-20 lg:py-28">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="max-w-2xl">
          <h2 class="text-3xl sm:text-4xl font-bold tracking-tight text-gray-900">Zobacz aplikację w akcji</h2>
          <p class="mt-4 text-gray-600 text-lg">Czysty, intuicyjny interfejs zaprojektowany pod codzienne użycie podczas podróży.</p>
        </div>

        <div class="mt-12 grid sm:grid-cols-2 lg:grid-cols-3 gap-8 justify-items-center">
          <div v-for="(scr, i) in screens" :key="i" class="relative w-[230px] h-[460px] rounded-[2rem] border-[8px] border-gray-900 bg-gray-900 shadow-xl">
            <div class="absolute top-0 left-1/2 -translate-x-1/2 w-24 h-5 bg-gray-900 rounded-b-2xl z-10"></div>
            <div class="w-full h-full rounded-[1.4rem] overflow-hidden p-4 text-white flex flex-col" :class="scr.bg">
              <div class="flex justify-between text-[10px] opacity-70 pt-1">
                <span>9:41</span><span>100%</span>
              </div>
              <p class="text-[10px] uppercase tracking-wider opacity-70 mt-6">{{ scr.kicker }}</p>
              <h4 class="text-xl font-bold mt-1">{{ scr.title }}</h4>
              <div class="mt-4 space-y-2">
                <div v-for="(item, j) in scr.items" :key="j" class="bg-white/15 backdrop-blur rounded-lg p-2.5 text-xs flex justify-between">
                  <span>{{ item.label }}</span>
                  <span class="font-semibold opacity-90">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============== DOWNLOAD WIDGET ============== -->
    <section id="download" class="py-20 lg:py-28 bg-gradient-to-br from-indigo-600 to-sky-600 relative overflow-hidden">
      <div class="absolute inset-0 opacity-20" style="background-image: radial-gradient(circle at 20% 20%, white 1px, transparent 1px), radial-gradient(circle at 80% 60%, white 1px, transparent 1px); background-size: 40px 40px;"></div>
      <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 relative">
        <div class="bg-white rounded-3xl shadow-2xl p-8 lg:p-12 grid lg:grid-cols-[1fr_auto] gap-10 items-center">
          <div>
            <span class="inline-flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-indigo-700 bg-indigo-100 px-3 py-1 rounded-full">Dostępne wkrótce</span>
            <h2 class="mt-5 text-3xl sm:text-4xl font-bold tracking-tight text-gray-900">Pobierz Trippy na swój telefon</h2>
            <p class="mt-4 text-gray-600 text-lg">Zainstaluj aplikację bezpośrednio z naszej strony lub poczekaj na publikację w Google Play.</p>

            <div class="mt-8 flex flex-wrap gap-3">
              <button
                type="button"
                disabled
                class="inline-flex items-center gap-3 bg-gray-900 text-white px-5 py-3 rounded-xl shadow-md cursor-not-allowed opacity-90"
                title="Wkrótce dostępne"
              >
                <svg viewBox="0 0 24 24" class="w-7 h-7" fill="currentColor">
                  <path d="M17.523 15.341c-.354 0-.642-.288-.642-.642s.288-.642.642-.642.642.288.642.642-.288.642-.642.642m-11.046 0c-.354 0-.642-.288-.642-.642s.288-.642.642-.642.642.288.642.642-.288.642-.642.642m11.277-6.155l1.281-2.219a.266.266 0 00-.097-.363.266.266 0 00-.363.097l-1.297 2.247a8.084 8.084 0 00-3.31-.703c-1.182 0-2.302.252-3.31.703L9.36 6.701a.266.266 0 00-.363-.097.266.266 0 00-.097.363l1.281 2.219C7.973 10.387 6.18 12.789 6 15.6h12c-.18-2.811-1.973-5.213-4.246-6.414"/>
                </svg>
                <span class="text-left leading-tight">
                  <span class="block text-[10px] opacity-70">Pobierz</span>
                  <span class="block font-semibold">Trippy.apk</span>
                </span>
              </button>

              <button
                type="button"
                disabled
                class="inline-flex items-center gap-3 bg-white text-gray-900 border border-gray-200 px-5 py-3 rounded-xl shadow-sm cursor-not-allowed"
                title="Wkrótce w Google Play"
              >
                <svg viewBox="0 0 24 24" class="w-7 h-7" fill="currentColor">
                  <path d="M3.609 1.814L13.792 12 3.61 22.186a.996.996 0 01-.61-.92V2.734a1 1 0 01.609-.92zM14.5 12.707l2.585 2.585-11.063 6.32 8.478-8.905zm3.793-3.793l-2.586 2.586L6.33 2.527 17.082 8.66l1.21.255zM20.39 11.93l-2.59 1.481-2.79-2.79 2.79-2.79 2.59 1.482a1 1 0 010 1.616z"/>
                </svg>
                <span class="text-left leading-tight">
                  <span class="block text-[10px] opacity-70">Już wkrótce w</span>
                  <span class="block font-semibold">Google Play</span>
                </span>
              </button>
            </div>

            <div class="mt-6 text-sm text-gray-500 space-y-1">
              <p>Wymagania: Android 8.0 (API 26) lub nowszy.</p>
              <p>Plik APK ze sprawdzonego źródła — podpisany cyfrowo.</p>
            </div>
          </div>

          <!-- QR mockup -->
          <div class="hidden lg:flex flex-col items-center">
            <div class="w-44 h-44 rounded-2xl bg-white border-2 border-gray-100 p-3 shadow-md">
              <svg viewBox="0 0 100 100" class="w-full h-full" aria-hidden="true">
                <rect width="100" height="100" fill="#fff"/>
                <g fill="#111827">
                  <rect x="5" y="5" width="22" height="22"/>
                  <rect x="9" y="9" width="14" height="14" fill="#fff"/>
                  <rect x="13" y="13" width="6" height="6"/>
                  <rect x="73" y="5" width="22" height="22"/>
                  <rect x="77" y="9" width="14" height="14" fill="#fff"/>
                  <rect x="81" y="13" width="6" height="6"/>
                  <rect x="5" y="73" width="22" height="22"/>
                  <rect x="9" y="77" width="14" height="14" fill="#fff"/>
                  <rect x="13" y="81" width="6" height="6"/>
                  <rect x="35" y="10" width="5" height="5"/><rect x="45" y="10" width="5" height="5"/>
                  <rect x="55" y="15" width="5" height="5"/><rect x="40" y="20" width="5" height="5"/>
                  <rect x="60" y="25" width="5" height="5"/><rect x="35" y="30" width="5" height="5"/>
                  <rect x="50" y="30" width="5" height="5"/><rect x="65" y="35" width="5" height="5"/>
                  <rect x="30" y="40" width="5" height="5"/><rect x="45" y="40" width="5" height="5"/>
                  <rect x="55" y="45" width="5" height="5"/><rect x="35" y="50" width="5" height="5"/>
                  <rect x="65" y="50" width="5" height="5"/><rect x="40" y="55" width="5" height="5"/>
                  <rect x="55" y="60" width="5" height="5"/><rect x="35" y="65" width="5" height="5"/>
                  <rect x="50" y="65" width="5" height="5"/><rect x="65" y="70" width="5" height="5"/>
                  <rect x="40" y="75" width="5" height="5"/><rect x="55" y="80" width="5" height="5"/>
                  <rect x="65" y="85" width="5" height="5"/><rect x="80" y="35" width="5" height="5"/>
                  <rect x="85" y="50" width="5" height="5"/><rect x="80" y="65" width="5" height="5"/>
                  <rect x="90" y="80" width="5" height="5"/>
                </g>
              </svg>
            </div>
            <p class="mt-3 text-sm text-gray-500 text-center max-w-[180px]">Zeskanuj kod, aby pobrać apkę na telefon</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ============== FAQ ============== -->
    <section id="faq" class="py-20 lg:py-28">
      <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl sm:text-4xl font-bold tracking-tight text-gray-900 text-center">Najczęściej zadawane pytania</h2>
        <div class="mt-12 space-y-3">
          <details v-for="(q, i) in faq" :key="i" class="group bg-white rounded-xl border border-gray-100 p-5 open:shadow-md transition">
            <summary class="flex items-center justify-between cursor-pointer font-medium text-gray-900 list-none">
              {{ q.q }}
              <svg viewBox="0 0 24 24" class="w-5 h-5 text-gray-400 group-open:rotate-180 transition" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </summary>
            <p class="mt-3 text-gray-600 text-sm leading-relaxed">{{ q.a }}</p>
          </details>
        </div>
      </div>
    </section>

    <!-- ============== FOOTER ============== -->
    <footer class="border-t border-gray-100 bg-gray-50">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12 grid sm:grid-cols-2 lg:grid-cols-4 gap-8">
        <div>
          <div class="flex items-center gap-2">
            <span class="inline-flex items-center justify-center w-8 h-8 rounded-lg bg-gradient-to-br from-indigo-600 to-sky-500 text-white">
              <svg viewBox="0 0 24 24" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12l18-9-4 9 4 9z"/></svg>
            </span>
            <span class="font-bold tracking-tight">Trippy</span>
          </div>
          <p class="mt-3 text-sm text-gray-500">Wspólne podróże i sprawiedliwe rozliczenia w jednej aplikacji.</p>
        </div>
        <div>
          <h4 class="text-xs font-semibold uppercase tracking-wider text-gray-500">Produkt</h4>
          <ul class="mt-3 space-y-2 text-sm">
            <li><a href="#features" class="text-gray-600 hover:text-indigo-600 transition">Funkcje</a></li>
            <li><a href="#how" class="text-gray-600 hover:text-indigo-600 transition">Jak to działa</a></li>
            <li><a href="#download" class="text-gray-600 hover:text-indigo-600 transition">Pobierz</a></li>
          </ul>
        </div>
        <div>
          <h4 class="text-xs font-semibold uppercase tracking-wider text-gray-500">Pomoc</h4>
          <ul class="mt-3 space-y-2 text-sm">
            <li><a href="#faq" class="text-gray-600 hover:text-indigo-600 transition">FAQ</a></li>
            <li><a href="mailto:kontakt@trippy.app" class="text-gray-600 hover:text-indigo-600 transition">Kontakt</a></li>
          </ul>
        </div>
        <div>
          <h4 class="text-xs font-semibold uppercase tracking-wider text-gray-500">Dla zespołu</h4>
          <ul class="mt-3 space-y-2 text-sm">
            <li>
              <router-link to="/login" class="text-gray-600 hover:text-indigo-600 transition inline-flex items-center gap-1.5">
                <svg viewBox="0 0 24 24" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0110 0v4"/>
                </svg>
                Panel administratora
              </router-link>
            </li>
          </ul>
        </div>
      </div>
      <div class="border-t border-gray-100">
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-gray-500">
          <p>&copy; {{ new Date().getFullYear() }} Trippy. Wszelkie prawa zastrzeżone.</p>
          <p>Made with care for group travelers.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
const features = [
  {
    title: 'Zarządzanie podróżą',
    desc: 'Twórz wyjazdy, ustalaj ramy czasowe, główny budżet i walutę. Zapraszaj uczestników i przydzielaj im role.',
    icon: '<svg viewBox="0 0 24 24" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>'
  },
  {
    title: 'Ewidencja wydatków',
    desc: 'Każdy uczestnik dodaje koszty na bieżąco — z kwotą, tytułem i informacją kto za co zapłacił.',
    icon: '<svg viewBox="0 0 24 24" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>'
  },
  {
    title: 'Automatyczny bilans',
    desc: 'Trippy w czasie rzeczywistym pokazuje, kto ma nadpłatę, a kto i ile jest winien grupie.',
    icon: '<svg viewBox="0 0 24 24" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18.7 8l-5.1 5.2-2.8-2.7L7 14.3"/></svg>'
  },
  {
    title: 'Uczestnicy i role',
    desc: 'Organizator, uczestnik — każdy ma swoje uprawnienia. Zaproszenia z potwierdzeniem dołączenia.',
    icon: '<svg viewBox="0 0 24 24" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>'
  },
  {
    title: 'Tablica wspomnień',
    desc: 'Notatki, przemyślenia i zdjęcia powiązane z konkretnymi punktami wyjazdu — wirtualny album podróży.',
    icon: '<svg viewBox="0 0 24 24" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="9" cy="9" r="2"/><path d="M21 15l-5-5L5 21"/></svg>'
  },
  {
    title: 'Bezpieczeństwo',
    desc: 'Nowoczesne systemy ochrony waszych danych i bezpieczeństwa.',
    icon: '<svg viewBox="0 0 24 24" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>'
  }
]

const steps = [
  { title: 'Zarejestruj się', desc: 'Załóż konto e-mailem i hasłem. Weryfikacja e-mail i jesteś gotów do startu.' },
  { title: 'Stwórz podróż', desc: 'Nazwij wyjazd, ustaw daty, walutę i zaproś znajomych. Każdy z uczestników widzi te same dane.' },
  { title: 'Rozliczcie się', desc: 'Dodawajcie wydatki w trakcie wyjazdu. Po powrocie Trippy pokaże dokładny bilans grupy.' }
]

const screens = [
  {
    bg: 'bg-gradient-to-br from-indigo-600 to-violet-600',
    kicker: 'Moje podróże',
    title: 'Twoje wyjazdy',
    items: [
      { label: 'Włochy 2025', value: '4 os.' },
      { label: 'Grecja 2024', value: '6 os.' },
      { label: 'Tatry zimowe', value: '3 os.' },
      { label: 'Bałtyk weekend', value: '5 os.' }
    ]
  },
  {
    bg: 'bg-gradient-to-br from-sky-500 to-cyan-600',
    kicker: 'Bilans',
    title: 'Rozliczenie',
    items: [
      { label: 'Anna', value: '+ 80 €' },
      { label: 'Marek', value: '− 45 €' },
      { label: 'Karolina', value: '+ 22 €' },
      { label: 'Tomasz', value: '− 57 €' }
    ]
  },
  {
    bg: 'bg-gradient-to-br from-emerald-500 to-teal-600',
    kicker: 'Wydatki',
    title: 'Ostatnie koszty',
    items: [
      { label: 'Paliwo', value: '80 €' },
      { label: 'Hotel Roma', value: '320 €' },
      { label: 'Bilety muzeum', value: '48 €' },
      { label: 'Kolacja', value: '62,50 €' }
    ]
  }
]

const faq = [
  {
    q: 'Na jakich urządzeniach działa Trippy?',
    a: 'Aplikacja jest dostępna na urządzenia z systemem Android 8.0 (API 26) lub nowszym. Wersja na iOS nie jest obecnie planowana.'
  },
  {
    q: 'Czy mogę używać Trippy w wielu walutach?',
    a: 'Wersja MVP zakłada jedną walutę dla całego wyjazdu — wybierasz ją podczas tworzenia podróży. Obsługa wielowalutowa pojawi się w kolejnych wydaniach.'
  },
  {
    q: 'Jak Trippy oblicza bilanse?',
    a: 'Każdy wydatek można oznaczyć jako współdzielony albo indywidualny. Współdzielone koszty są dzielone po równo między uczestników wyjazdu, a aplikacja na bieżąco pokazuje, kto ma nadpłatę, a kto niedopłatę.'
  },
  {
    q: 'Czy moje dane są bezpieczne?',
    a: 'Tak. Hasła są hashowane algorytmem BCrypt, a komunikacja z API zabezpieczona tokenem JWT. Dane są przechowywane w bazie PostgreSQL z kontrolą dostępu na poziomie aplikacji.'
  },
  {
    q: 'Czy aplikacja jest darmowa?',
    a: 'Tak — Trippy jest projektem zespołowym i obecnie udostępniana jest bezpłatnie.'
  }
]
</script>