<template>
  <div class="space-y-6">
    
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <h2 class="text-2xl font-bold text-gray-800">Zarządzanie użytkownikami</h2>
      <button 
        @click="router.push('/admin/users/create')"
        class="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-medium shadow-sm transition-colors"
      >
        + Dodaj użytkownika
      </button>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Ładowanie danych...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <div v-else class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200 text-gray-600 text-sm uppercase tracking-wider">
              <th class="p-4 font-medium">ID</th>
              <th class="p-4 font-medium">Imię / Nazwa</th>
              <th class="p-4 font-medium">Email</th>
              <th class="p-4 font-medium">Status</th>
              <th class="p-4 font-medium text-right">Akcje</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 text-sm text-gray-700">
            <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50 transition-colors">
              <td class="p-4 font-mono text-xs text-gray-500">#{{ user.id }}</td>
              <td class="p-4 font-medium text-gray-900">
                {{ user.name }}
                <span v-if="user.role === 'ADMIN'" class="ml-2 text-[10px] bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full font-bold">ADMIN</span>
              </td>
              <td class="p-4">{{ user.email }}</td>
              <td class="p-4">
                <span v-if="user.isVerified" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  Zweryfikowany
                </span>
                <span v-else class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                  Oczekujący
                </span>
              </td>
              <td class="p-4 flex justify-end gap-2">
                <button 
                  @click="router.push(`/admin/users/${user.id}/edit`)" 
                  class="text-indigo-600 hover:text-indigo-900 transition-colors px-2 py-1 font-medium"
                >
                  Edytuj
                </button>
                <button 
                  @click="deleteUser(user.id, user.name)"
                  class="text-red-600 hover:text-red-900 transition-colors px-2 py-1 font-medium"
                >
                  Usuń
                </button>
              </td>
            </tr>
            
            <tr v-if="users.length === 0">
              <td colspan="5" class="p-8 text-center text-gray-500">Brak użytkowników w bazie.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const users = ref([])
const loading = ref(true)
const errorMsg = ref('')

onMounted(async () => {
  try {
    const token = localStorage.getItem('trippy_token')
    
    if (!token) {
      router.push('/login')
      console.log("test1")
      return
    }

    const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/users`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    users.value = response.data
  } catch (error) {
    if (error.response?.status === 403 || error.response?.status === 401) {
      errorMsg.value = 'Sesja wygasła lub brak uprawnień. Zaloguj się ponownie.'
      localStorage.removeItem('trippy_token')
      console.log("test2")
      router.push('/login')
    } else {
      errorMsg.value = 'Nie udało się pobrać listy użytkowników. Serwer nie odpowiada.'
    }
    console.error("Błąd pobierania użytkowników:", error)
  } finally {
    loading.value = false
  }
})

const deleteUser = async (id, name) => {
  const isConfirmed = window.confirm(`CZY NA PEWNO chcesz trwale usunąć użytkownika: ${name}?\n\nTa operacja jest nieodwracalna! Jeśli ten użytkownik założył wyjazdy, one również mogą zostać usunięte (zależnie od ustawień bazy).`)
  
  if (!isConfirmed) return

  try {
    const token = localStorage.getItem('trippy_token')
    
    await axios.delete(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${id}`, 
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    )

    users.value = users.value.filter(u => u.id !== id)
  } catch (error) {
    if (error.response?.status === 403 || error.response?.status === 401) {
      errorMsg.value = 'Brak uprawnień do usunięcia użytkownika.'
    } else {
      errorMsg.value = 'Nie udało się usunąć użytkownika. Serwer zwrócił błąd.'
    }
    console.error("Błąd usuwania użytkownika:", error)
  }
}

</script>