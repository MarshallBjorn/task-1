<template>
  <div class="space-y-6">
    
    <div class="border-b border-gray-200 pb-4">
      <h2 class="text-2xl font-bold text-gray-800">Przegląd wszystkich wyjazdów</h2>
      <p class="text-sm text-gray-500 mt-1">Wybierz wyjazd z listy, aby zarządzać jego węzłami (lokalizacjami), postami i zdjęciami.</p>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Pobieranie wyjazdów...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <div v-else class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200 text-gray-600 text-xs uppercase tracking-wider">
              <th class="p-4 font-medium">ID Wyjazdu</th>
              <th class="p-4 font-medium">Nazwa (Tytuł)</th>
              <th class="p-4 font-medium">Organizator</th>
              <th class="p-4 font-medium">Email</th>
              <th class="p-4 font-medium text-right">Akcje</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 text-sm text-gray-700">
            <tr 
              v-for="trip in trips" 
              :key="trip.id" 
              class="hover:bg-gray-50 transition-colors group"
            >
              <td class="p-4 font-mono text-xs text-gray-500" :title="trip.id">#{{ trip.id.split('-')[0] }}...</td>
              <td class="p-4 font-bold text-gray-900">{{ trip.title || trip.name }}</td>
              <td class="p-4 text-gray-600">{{ trip.ownerName || 'Brak danych' }}</td>
              <td class="p-4 text-gray-600">{{ trip.ownerEmail || 'Brak danych' }}</td>
              <td class="p-4 text-right">
                <button 
                  @click="goToTripDetails(trip.id)"
                  class="inline-flex items-center text-indigo-600 hover:text-indigo-900 font-medium text-sm transition-colors"
                >
                  Szczegóły 
                  <svg class="w-4 h-4 ml-1 transform group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                  </svg>
                </button>
              </td>
            </tr>
            <tr v-if="trips.length === 0">
              <td colspan="5" class="p-8 text-center text-gray-500">Brak utworzonych wyjazdów w systemie.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const trips = ref([])
const loading = ref(true)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')

const goToTripDetails = (id) => {
  // Przekierowuje na widok szczegółów. Upewnij się, że masz taki routing w router/index.js!
  router.push(`/admin/trips/${id}`)
}

onMounted(async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/trips`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    })
    trips.value = response.data
  } catch (error) {
    errorMsg.value = 'Nie udało się pobrać listy wyjazdów.'
    console.error(error)
  } finally {
    loading.value = false
  }
})
</script>