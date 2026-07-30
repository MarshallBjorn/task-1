<template>
  <div class="space-y-6 max-w-2xl mx-auto mt-8">
    
    <div class="flex items-center gap-4 border-b border-gray-200 pb-4">
      <button @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &larr; Wróć
      </button>
      <h2 class="text-2xl font-bold text-gray-800">Dodaj nową walutę</h2>
    </div>

    <div v-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <form @submit.prevent="createCurrency" class="bg-white shadow-sm rounded-lg border border-gray-200 p-6 space-y-6">
      
      <div>
        <label for="code" class="block text-sm font-bold text-gray-700 mb-1">Kod waluty (3 znaki) *</label>
        <input 
          id="code" 
          v-model="formData.code" 
          type="text" 
          maxlength="3"
          placeholder="np. PLN, EUR, USD"
          required
          class="w-full uppercase px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
        >
      </div>

      <div>
        <label for="name" class="block text-sm font-bold text-gray-700 mb-1">Pełna nazwa waluty *</label>
        <input 
          id="name" 
          v-model="formData.name" 
          type="text" 
          placeholder="np. Polski Złoty, Euro"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
        >
      </div>

      <div class="pt-4 flex justify-end">
        <button 
          type="submit" 
          :disabled="saving"
          class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg text-sm font-medium shadow-sm disabled:opacity-50 transition-colors"
        >
          {{ saving ? 'Zapisywanie...' : 'Zapisz walutę' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const formData = ref({ code: '', name: '' })
const saving = ref(false)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')

const createCurrency = async () => {
  saving.value = true
  errorMsg.value = ''
  
  formData.value.code = formData.value.code.toUpperCase()

  try {
    await axios.post(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/dictionaries/currencies`, 
      formData.value,
      { headers: { Authorization: `Bearer ${getToken()}` } }
    )
    router.push('/admin/dictionaries')
  } catch (error) {
    errorMsg.value = error.response?.data?.message || 'Wystąpił błąd podczas dodawania waluty.'
  } finally {
    saving.value = false
  }
}
</script>