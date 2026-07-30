<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8">
      <h2 class="text-2xl font-bold text-center text-gray-800 mb-8">Trippy Admin Panel</h2>
      
      <form @submit.prevent="handleLogin" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700">Email</label>
          <input 
            v-model="email" 
            type="email" 
            required 
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700">Hasło</label>
          <input 
            v-model="password" 
            type="password" 
            required 
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          />
        </div>

        <div v-if="errorMessage" class="text-red-600 text-sm text-center">
          {{ errorMessage }}
        </div>

        <button 
          type="submit" 
          class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none"
        >
          Zaloguj się
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const router = useRouter()

const handleLogin = async () => {
  try {
    errorMessage.value = ''

    const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/auth/login`, {
      email: email.value,
      password: password.value
    })

    localStorage.setItem('trippy_token', response.data.accessToken);
    localStorage.setItem('trippy_refresh_token', response.data.refreshToken);
    localStorage.setItem('trippy_role', response.data.role);

    console.log(response.data.role)

    if (response.data.role !== 'ROLE_ADMIN') {
      errorMessage.value = 'Odmowa dostępu. Ten panel jest dostępny wyłącznie dla administratorów.'
      localStorage.clear();
      return
    }    
  
    localStorage.setItem('trippy_token', response.data.accessToken)
    
    router.push('/admin/users')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Nie można się zalogować. Sprawdź dane i spróbuj ponownie.'
  }
}
</script>