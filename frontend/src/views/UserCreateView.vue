<template>
  <div class="space-y-6 max-w-3xl mx-auto">
    
    <div class="flex items-center gap-4 border-b border-gray-200 pb-4">
      <button @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &larr; Wróć do listy
      </button>
      <h2 class="text-2xl font-bold text-gray-800">Dodaj nowego użytkownika</h2>
    </div>

    <div v-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200 whitespace-pre-line">
      {{ errorMsg }}
    </div>

    <form @submit.prevent="createUser" class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
      
      <div class="p-6 space-y-6">
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label for="name" class="block text-sm font-bold text-gray-700 mb-1">Imię / Nazwa *</label>
            <input 
              id="name" 
              v-model="formData.name" 
              type="text" 
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            >
          </div>

          <div>
            <label for="email" class="block text-sm font-bold text-gray-700 mb-1">Adres Email *</label>
            <input 
              id="email" 
              v-model="formData.email" 
              type="email" 
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
            >
          </div>
        </div>

        <div>
          <label for="photo" class="block text-sm font-bold text-gray-700 mb-1">Zdjęcie profilowe (opcjonalne)</label>
          <input 
            id="photo" 
            type="file" 
            accept="image/jpeg, image/png, image/webp"
            @change="handleFileUpload"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
          >
          <p class="text-xs text-gray-500 mt-1">Maksymalny rozmiar pliku: 5MB.</p>
        </div>

        <div>
          <label for="password" class="block text-sm font-bold text-gray-700 mb-1">Hasło tymczasowe *</label>
          <input 
            id="password" 
            v-model="formData.password" 
            type="text" 
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
          <p class="text-xs text-gray-500 mt-1">Użytkownik użyje tego hasła do pierwszego logowania.</p>
        </div>

        <div>
          <label for="role" class="block text-sm font-bold text-gray-700 mb-1">Uprawnienia (Rola)</label>
          <select 
            id="role" 
            v-model="formData.role"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
            <option value="USER">Zwykły użytkownik (USER)</option>
            <option value="ADMIN">Administrator (ADMIN)</option>
          </select>
        </div>

        <div class="flex items-start mt-4 pt-4 border-t border-gray-100">
          <div class="flex items-center h-5">
            <input 
              id="isVerified" 
              v-model="formData.isVerified" 
              type="checkbox" 
              class="w-4 h-4 text-green-600 border-gray-300 rounded focus:ring-green-500"
            >
          </div>
          <div class="ml-3 text-sm">
            <label for="isVerified" class="font-bold text-green-700">Konto zweryfikowane</label>
            <p class="text-gray-500">Konta utworzone przez administratora mogą być od razu uznane za zweryfikowane.</p>
          </div>
        </div>
        
      </div>

      <div class="bg-gray-50 px-6 py-4 flex items-center justify-end border-t border-gray-200 gap-3">
        <button 
          type="button"
          @click="router.back()" 
          class="bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium shadow-sm hover:bg-gray-50"
        >
          Anuluj
        </button>
        <button 
          type="submit" 
          :disabled="saving"
          class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg text-sm font-medium shadow-sm disabled:opacity-50 transition-colors"
        >
          {{ saving ? 'Zapisywanie...' : 'Zapisz i zakończ' }}
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

const formData = ref({
  name: '',
  email: '',
  password: '',
  role: 'USER',
  isVerified: true,
  isBlocked: false
})

const selectedFile = ref(null)

const handleFileUpload = (event) => {
  const file = event.target.files[0]

  if (file) {
    selectedFile.value = file
  } else {
    selectedFile.value = null
  }
}

const saving = ref(false)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')
const getAuthHeaders = () => ({ headers: { Authorization: `Bearer ${getToken()}` } })

const createUser = async () => {
  saving.value = true
  errorMsg.value = ''

  try {
    const response = await axios.post(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/users`, 
      formData.value,
      getAuthHeaders()
    )
    
    const newUserId = response.data?.id

    if (!newUserId) {
       throw new Error("Backend nie zwrócił ID nowo utworzonego użytkownika.");
    }

    if (selectedFile.value) {
      const uploadData = new FormData()
      uploadData.append('file', selectedFile.value)

      await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}/photo`, 
        uploadData,
        getAuthHeaders()
      )
    }

    router.push('/admin/users')
  } catch (error) {
    if (error.response?.status === 400 && error.response.data) {
      const errorMap = error.response.data
      errorMsg.value = Object.values(errorMap).join('\n')
    } else {
      errorMsg.value = error.response?.data?.message || 'Wystąpił błąd podczas tworzenia użytkownika.'
    }
  } finally {
    saving.value = false
  }
}
</script>