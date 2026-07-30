<template>
  <div class="space-y-8">
    
    <div class="flex justify-between items-center border-b border-gray-200 pb-4">
      <h2 class="text-2xl font-bold text-gray-800">Słowniki systemowe</h2>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Ładowanie słowników...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      
      <div class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden self-start">
        <div class="bg-gray-50 px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h3 class="text-lg font-bold text-gray-700">Waluty</h3>
          <button @click="$router.push('/admin/dictionaries/currencies/create')" class="bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-1.5 rounded text-sm font-medium transition-colors shadow-sm">
            + Dodaj
          </button>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-200 text-gray-600 text-xs uppercase tracking-wider">
                <th class="p-4 font-medium w-24">Kod</th>
                <th class="p-4 font-medium">Nazwa</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 text-sm text-gray-700">
              <tr v-for="currency in currencies" :key="currency.code" class="hover:bg-gray-50 transition-colors">
                <td class="p-4 font-mono font-bold text-indigo-600">{{ currency.code }}</td>
                <td class="p-4 font-medium text-gray-900">{{ currency.name }}</td>
              </tr>
              <tr v-if="currencies.length === 0">
                <td colspan="2" class="p-8 text-center text-gray-500">Brak zdefiniowanych walut.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden self-start">
        <div class="bg-gray-50 px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h3 class="text-lg font-bold text-gray-700">Role w wyjeździe</h3>
          <button @click="$router.push('/admin/dictionaries/roles/create')"class="bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-1.5 rounded text-sm font-medium transition-colors shadow-sm">
            + Dodaj
          </button>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-200 text-gray-600 text-xs uppercase tracking-wider">
                <th class="p-4 font-medium">ID</th>
                <th class="p-4 font-medium">Nazwa Roli</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 text-sm text-gray-700">
              <tr v-for="role in roles" :key="role.id" class="hover:bg-gray-50 transition-colors">
                <td class="p-4 font-mono text-xs text-gray-500" :title="role.id">
                  #{{ role.id.split('-')[0] }}...
                </td>
                <td class="p-4 font-bold text-gray-900">
                  <span class="bg-gray-100 text-gray-800 px-2 py-1 rounded text-xs tracking-wide">
                    {{ role.name }}
                  </span>
                </td>
              </tr>
              <tr v-if="roles.length === 0">
                <td colspan="2" class="p-8 text-center text-gray-500">Brak zdefiniowanych ról.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const currencies = ref([])
const roles = ref([])
const loading = ref(true)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')
const getAuthHeaders = () => ({ headers: { Authorization: `Bearer ${getToken()}` } })

onMounted(async () => {
  try {
    const [currenciesRes, rolesRes] = await Promise.all([
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/dictionaries/currencies`, getAuthHeaders()),
      axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/dictionaries/roles`, getAuthHeaders())
    ])

    currencies.value = currenciesRes.data
    roles.value = rolesRes.data
  } catch (error) {
    console.error("Błąd pobierania słowników:", error)
    errorMsg.value = 'Nie udało się pobrać słowników z serwera. Sprawdź konsolę.'
  } finally {
    loading.value = false
  }
})
</script>