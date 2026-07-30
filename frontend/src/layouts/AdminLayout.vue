<template>
  <div class="min-h-screen flex flex-col bg-gray-100 font-sans">
    
    <header class="bg-indigo-600 shadow-md z-20">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16 items-center">
          <div class="flex items-center">
            <h1 class="text-white text-xl font-bold tracking-wider">TRIPPY<span class="text-indigo-200">ADMIN</span></h1>
          </div>
          <div class="flex items-center space-x-4">
            <span class="text-indigo-100 text-sm hidden sm:block">Witaj, Administratorze</span>
            <button 
              @click="handleLogout" 
              class="bg-indigo-700 hover:bg-indigo-800 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
            >
              Wyloguj się
            </button>
          </div>
        </div>
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <aside class="w-64 bg-white shadow-lg hidden md:block z-10">
        <nav class="mt-6 px-4 space-y-2">
          <router-link 
            to="/admin/users" 
            class="block px-4 py-3 text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 rounded-lg font-medium transition-colors"
            active-class="bg-indigo-50 text-indigo-600 border-l-4 border-indigo-600"
          >
            👥 Użytkownicy
          </router-link>
          
          <router-link 
            to="/admin/trips" 
            class="block px-4 py-3 text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 rounded-lg font-medium transition-colors"
            active-class="bg-indigo-50 text-indigo-600 border-l-4 border-indigo-600"
          >
            ✈️ Podróże i Posty
          </router-link>
          
          <router-link 
            to="/admin/dictionaries" 
            class="block px-4 py-3 text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 rounded-lg font-medium transition-colors"
            active-class="bg-indigo-50 text-indigo-600 border-l-4 border-indigo-600"
          >
            📚 Słowniki (Waluty)
          </router-link>
        </nav>
      </aside>

      <main class="flex-1 overflow-y-auto p-6 md:p-8">
        <router-view />
      </main>
    </div>

    <footer class="bg-white border-t border-gray-200 py-4 text-center z-20">
      <p class="text-sm text-gray-500">
        &copy; {{ new Date().getFullYear() }} Trippy. Wszystkie prawa zastrzeżone. Panel Administratora.
      </p>
    </footer>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleLogout = () => {
  localStorage.removeItem('trippy_token')
  router.push('/login')
}
</script>