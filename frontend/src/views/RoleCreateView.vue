<template>
  <div class="space-y-6 max-w-2xl mx-auto mt-8">
    
    <div class="flex items-center gap-4 border-b border-gray-200 pb-4">
      <button @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &larr; Wróć
      </button>
      <h2 class="text-2xl font-bold text-gray-800">Dodaj nową rolę w wyjeździe</h2>
    </div>

    <div v-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <form @submit.prevent="createRole" class="bg-white shadow-sm rounded-lg border border-gray-200 p-6 space-y-6">
      
      <div>
        <label for="name" class="block text-sm font-bold text-gray-700 mb-1">Nazwa roli (Wielkie litery) *</label>
        <input 
          id="name" 
          v-model="formData.name" 
          type="text" 
          placeholder="np. ORGANIZER, PARTICIPANT"
          required
          class="w-full uppercase px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
        >
        <p class="text-xs text-gray-500 mt-1">Nazwa roli systemowej powinna być pisana wielkimi literami i bez spacji (użyj podkreślników).</p>
      </div>

      <div class="pt-4 flex justify-end">
        <button 
          type="submit" 
          :disabled="saving"
          class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg text-sm font-medium shadow-sm disabled:opacity-50 transition-colors"
        >
          {{ saving ? 'Zapisywanie...' : 'Zapisz rolę' }}
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
const formData = ref({ name: '' })
const saving = ref(false)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')

const createRole = async () => {
  saving.value = true
  errorMsg.value = ''
  
  formData.value.name = formData.value.name.toUpperCase().trim().replace(/\s+/g, '_')

  try {
    await axios.post(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/dictionaries/roles`, 
      formData.value,
      { headers: { Authorization: `Bearer ${getToken()}` } }
    )
    router.push('/admin/dictionaries')
  } catch (error) {
    errorMsg.value = error.response?.data?.message || 'Wystąpił błąd podczas dodawania roli.'
  } finally {
    saving.value = false
  }
}
</script>