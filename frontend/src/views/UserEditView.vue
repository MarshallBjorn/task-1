<template>
  <div class="space-y-6 max-w-3xl mx-auto">
    
    <div class="flex items-center gap-4 border-b border-gray-200 pb-4">
      <button @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &larr; Wróć do listy
      </button>
      <h2 class="text-2xl font-bold text-gray-800">Edycja użytkownika</h2>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Pobieranie danych...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200 whitespace-pre-line">
      {{ errorMsg }}
    </div>

    <form v-else @submit.prevent="saveUser" class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
      
      <div class="p-6 space-y-6">
        
        <div class="flex flex-col sm:flex-row items-center sm:items-start gap-6 pb-6 border-b border-gray-100">
          
          <div class="shrink-0">
            <img 
              v-if="imagePreview || user.photoUrl" 
              :src="imagePreview || user.photoUrl" 
              alt="Avatar użytkownika" 
              class="w-24 h-24 rounded-full object-cover border border-gray-200 shadow-sm"
            >
            <div v-else class="w-24 h-24 rounded-full bg-gray-100 border border-gray-200 flex items-center justify-center text-gray-400 shadow-sm">
              <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
              </svg>
            </div>
          </div>

          <div class="flex-1 w-full text-center sm:text-left">
            <label for="photo" class="block text-sm font-bold text-gray-700 mb-2">Zdjęcie profilowe</label>
            <input 
              id="photo" 
              type="file" 
              accept="image/jpeg, image/png, image/webp"
              @change="handleFileUpload"
              class="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
            >
            <p class="text-xs text-gray-500 mt-2">Maksymalny rozmiar pliku: 5MB. Dozwolone: JPG, PNG, WEBP.</p>
          </div>
        </div>

        <div>
          <label class="block text-sm font-bold text-gray-700 mb-1">ID Użytkownika</label>
          <div class="text-gray-500 font-mono text-sm bg-gray-50 p-2 rounded border border-gray-200 truncate">
            {{ user.id }}
          </div>
        </div>

        <div>
          <label for="email" class="block text-sm font-bold text-gray-700 mb-1">Adres Email</label>
          <input 
            id="email" 
            v-model="formData.email" 
            type="email" 
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
        </div>

        <div>
          <label for="name" class="block text-sm font-bold text-gray-700 mb-1">Imię / Nazwa</label>
          <input 
            id="name" 
            v-model="formData.name" 
            type="text" 
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
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
            <p class="text-gray-500">Zaznacz, jeśli użytkownik ręcznie potwierdził swój adres email.</p>
          </div>
        </div>

        <div class="flex items-start mt-4 pt-4 border-t border-gray-100">
          <div class="flex items-center h-5">
            <input 
              id="isBlocked" 
              v-model="formData.isBlocked" 
              type="checkbox" 
              class="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
            >
          </div>
          <div class="ml-3 text-sm">
            <label for="isBlocked" class="font-bold text-red-700">Zablokuj konto</label>
            <p class="text-gray-500">Zaznacz, aby uniemożliwić użytkownikowi logowanie się do systemu.</p>
          </div>
        </div>
      </div>

      <div class="bg-gray-50 px-6 py-4 flex items-center justify-end border-t border-gray-200 gap-3">
        <span v-if="saveSuccess" class="text-green-600 font-medium text-sm mr-auto">
          ✓ Zapisano zmiany
        </span>
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
          {{ saving ? 'Zapisywanie...' : 'Zapisz' }}
        </button>
      </div>

    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userId = route.params.id

const user = ref({})
const formData = ref({
  name: '',
  email: '',
  role: 'USER',
  isVerified: false,
  isBlocked: false
})

const selectedFile = ref(null)
const imagePreview = ref(null)

const loading = ref(true)
const saving = ref(false)
const errorMsg = ref('')
const saveSuccess = ref(false)

const getToken = () => localStorage.getItem('trippy_token')
const getAuthHeaders = () => ({ headers: { Authorization: `Bearer ${getToken()}` } })

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    imagePreview.value = URL.createObjectURL(file)
  } else {
    selectedFile.value = null
    imagePreview.value = null
  }
}

onUnmounted(() => {
  try {
    if (imagePreview.value) {
      URL.revokeObjectURL(imagePreview.value)
    }
  } catch (err) {
    console.warn("[DEBUG] Zignorowano błąd czyszczenia URL zdjęcia:", err)
  }
})

onMounted(async () => {
  try {
    const response = await axios.get(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}`, 
      getAuthHeaders()
    )
    user.value = response.data
    
    formData.value = {
      name: user.value.name,
      email: user.value.email,
      role: user.value.role,
      isVerified: user.value.isVerified || false,
      isBlocked: user.value.isBlocked || false
    }
  } catch (error) {
    errorMsg.value = 'Nie udało się pobrać danych użytkownika.'
  } finally {
    loading.value = false
  }
})

const saveUser = async () => {
  saving.value = true
  saveSuccess.value = false
  errorMsg.value = ''

  try {
    await axios.put(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}`, 
      formData.value,
      getAuthHeaders()
    )
    
    if (selectedFile.value) {
      const uploadData = new FormData()
      uploadData.append('file', selectedFile.value)

      const multipartHeaders = {
        headers: { 
          Authorization: `Bearer ${getToken()}`,
          'Content-Type': 'multipart/form-data'
        }
      }

      const photoResponse = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}/photo`, 
        uploadData,
        multipartHeaders
      )

      user.value.photoUrl = photoResponse.data.photoUrl
    }
    saveSuccess.value = true
    
  } catch (error) {
    if (error.response?.status === 400 && error.response.data && !error.response.data.message) {
      const errorMap = error.response.data
      errorMsg.value = Object.values(errorMap).join('\n')
    } else {
      errorMsg.value = error.response?.data?.error || error.response?.data?.message || 'Wystąpił błąd podczas zapisywania.'
    }
  } finally {
    saving.value = false
  }
}
</script>