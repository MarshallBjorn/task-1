<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8 text-center">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">Weryfikacja email</h2>
      
      <div v-if="status === 'loading'" class="space-y-4">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
        <p class="text-gray-600">Trwa weryfikacja Twojego konta. Proszę czekać...</p>
      </div>

      <div v-else-if="status === 'success'" class="space-y-4">
        <div class="text-green-500 text-5xl mb-4">✓</div>
        <h3 class="text-xl font-medium text-green-600">Sukces!</h3>
        <p class="text-gray-600">{{ message }}</p>
        <button 
          @click="goToLogin" 
          class="mt-6 w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none"
        >
          Przejdź do logowania
        </button>
      </div>

      <div v-else class="space-y-4">
        <div class="text-red-500 text-5xl mb-4">✗</div>
        <h3 class="text-xl font-medium text-red-600">Wystąpił błąd</h3>
        <p class="text-gray-600">{{ message }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const status = ref('loading')
const message = ref('')

onMounted(async () => {
  const token = route.query.token

  if (!token) {
    status.value = 'error'
    message.value = 'Brak tokenu weryfikacyjnego w adresie URL.'
    return
  }

  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/auth/verify`, {
      params: { token: token }
    })
    
    status.value = 'success'
    message.value = response.data.message
    
  } catch (error) {
    status.value = 'error'
    message.value = response.data.message
  }
})
</script>