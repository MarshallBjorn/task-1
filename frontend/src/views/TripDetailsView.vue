<template>
  <div class="space-y-6 max-w-5xl mx-auto">
    
    <div class="flex flex-col sm:flex-row sm:items-center justify-between border-b border-gray-200 pb-4 gap-4">
      <div class="flex items-start gap-4">
        <button @click="router.back()" class="text-gray-500 hover:text-gray-700 transition-colors mt-1">
          &larr; Wróć
        </button>
        <div>
          <h2 class="text-2xl font-bold text-gray-800">{{ trip?.name || 'Szczegóły wyjazdu' }}</h2>
          <div class="text-sm text-gray-500 mt-2 space-y-1" v-if="trip">
            <p>Organizator: <span class="font-medium text-gray-700">{{ trip.ownerName }}</span></p>
            <p>Termin: <span class="font-medium text-gray-700">{{ formatDate(trip.startDate) }} &mdash; {{ formatDate(trip.endDate) }}</span></p>
            <p>Budżet główy: <span class="font-medium text-green-700">{{ trip.budget }} PLN</span></p>
          </div>
        </div>
      </div>
      <div class="flex gap-2">
        <button class="bg-indigo-50 text-indigo-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-100 transition-colors">
          + Dodaj Węzeł
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Pobieranie struktury wyjazdu...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <div v-else class="relative border-l-2 border-indigo-100 ml-4 space-y-12 pb-10">
      
      <div v-if="!trip.nodes || trip.nodes.length === 0" class="pl-8 text-gray-500 italic">
        Ten wyjazd nie ma jeszcze żadnych punktów (węzłów).
      </div>

      <div v-for="node in trip.nodes" :key="node.id" class="relative pl-8">
        <div class="absolute -left-[9px] top-1 w-4 h-4 bg-indigo-600 rounded-full border-4 border-white shadow-sm"></div>
        
        <div class="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          
          <div class="bg-gray-50 px-5 py-3 border-b border-gray-200 flex justify-between items-center flex-wrap gap-2">
            <div class="flex items-center">
              <span class="text-xs font-bold uppercase tracking-wider px-2 py-1 rounded-md mr-3" :class="node.isSeparate ? 'bg-orange-50 text-orange-600' : 'bg-indigo-50 text-indigo-600'">
                {{ node.isSeparate ? 'OSOBNE' : 'WSPÓLNE' }}
              </span>
              <h3 class="text-lg font-bold text-gray-900">{{ node.name }}</h3>
            </div>
            <div class="text-right">
              <span v-if="node.price > 0" class="text-sm font-bold text-green-600 bg-green-50 px-2 py-1 rounded border border-green-100">
                {{ node.price }} PLN
              </span>
            </div>
          </div>

          <div class="p-5 space-y-6">
            
            <p v-if="node.note" class="text-gray-600 text-sm italic border-l-4 border-gray-200 pl-3">
              {{ node.note }}
              <span class="block text-xs text-gray-400 mt-1 font-normal">&mdash; Dodane przez: {{ node.reporterName }}</span>
            </p>

            <div v-if="!node.posts || node.posts.length === 0" class="text-sm text-gray-400 pt-2">
              Brak postów w tym węźle.
            </div>

            <div v-for="post in node.posts" :key="post.id" class="bg-gray-50 rounded-lg p-4 border border-gray-100">
              
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center">
                  <div class="w-6 h-6 bg-indigo-100 text-indigo-700 rounded-full flex items-center justify-center text-xs font-bold mr-2">
                    {{ post.reporterName.charAt(0).toUpperCase() }}
                  </div>
                  <span class="font-medium text-sm text-gray-800">{{ post.reporterName }}</span>
                </div>
                <button @click="deletePost(node.id, post.id)" class="text-xs text-red-600 hover:text-red-900 font-medium px-2 py-1 bg-red-50 hover:bg-red-100 rounded transition-colors">
                  Usuń wpis
                </button>
              </div>
              
              <p class="text-gray-700 text-sm mb-4 whitespace-pre-line">{{ post.note }}</p>

              <div v-if="post.photos && post.photos.length > 0" class="grid grid-cols-2 md:grid-cols-3 gap-2 mt-3">
                <div v-for="photo in post.photos" :key="photo.id" class="relative group aspect-square bg-gray-200 rounded-md overflow-hidden border border-gray-200">
                  <img :src="photo.photoUrl" alt="Zdjęcie z wyjazdu" class="w-full h-full object-cover">
                  
                  <div class="absolute inset-0 bg-black bg-opacity-60 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                    <button @click="deletePhoto(node.id, post.id, photo.id)" class="bg-red-600 text-white text-xs font-bold px-3 py-1.5 rounded hover:bg-red-700 shadow-sm flex items-center gap-1">
                      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                      Usuń
                    </button>
                  </div>

                </div>
              </div>
            </div>

          </div>
        </div>
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
const tripId = route.params.id

const trip = ref(null)
const loading = ref(true)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')
const getAuthHeaders = () => ({ headers: { Authorization: `Bearer ${getToken()}` } })

const formatDate = (dateString) => {
  if (!dateString) return 'Brak'
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('pl-PL', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(date)
}

const fetchTripDetails = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/trips/${tripId}`, getAuthHeaders())
    trip.value = response.data
  } catch (error) {
    errorMsg.value = 'Nie udało się pobrać szczegółów wyjazdu.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTripDetails()
})

const deletePost = async (nodeId, postId) => {
  if (!window.confirm('⚠️ Czy na pewno chcesz trwale usunąć ten wpis ze wszystkimi jego zdjęciami?')) return
  
  try {
    await axios.delete(`${import.meta.env.VITE_API_BASE_URL}/api/admin/moderation/posts/${postId}`, getAuthHeaders())
    
    const targetNode = trip.value.nodes.find(n => n.id === nodeId)
    if (targetNode) {
      targetNode.posts = targetNode.posts.filter(p => p.id !== postId)
    }
  } catch (error) {
    alert('Wystąpił błąd podczas usuwania wpisu.')
  }
}

const deletePhoto = async (nodeId, postId, photoId) => {
  if (!window.confirm('⚠️ Czy na pewno chcesz trwale usunąć to zdjęcie?')) return
  
  try {
    await axios.delete(`${import.meta.env.VITE_API_BASE_URL}/api/admin/moderation/photos/${photoId}`, getAuthHeaders())
    
    const targetNode = trip.value.nodes.find(n => n.id === nodeId)
    if (targetNode) {
      const targetPost = targetNode.posts.find(p => p.id === postId)
      if (targetPost) {
        targetPost.photos = targetPost.photos.filter(p => p.id !== photoId)
      }
    }
  } catch (error) {
    alert('Wystąpił błąd podczas usuwania zdjęcia.')
  }
}
</script>