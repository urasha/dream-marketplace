<script setup>
import { ref } from 'vue'
import Header from './components/Header.vue'
import HomePage from './components/pages/HomePage.vue'
import DreamDetailPage from './components/pages/DreamDetailPage.vue'
import CreateDreamPage from './components/pages/CreateDreamPage.vue'
import CreateLotPage from './components/pages/CreateLotPage.vue'
import LotDetailPage from './components/pages/LotDetailPage.vue'
import PurchaseFlow from './components/pages/PurchaseFlow.vue'
import ProfilePage from './components/pages/ProfilePage.vue'
import NotificationsPage from './components/pages/NotificationsPage.vue'
import AdminPage from './components/pages/AdminPage.vue'

const currentPage = ref('home')
const selectedDreamId = ref(null)
const selectedLotId = ref(null)
const userBalance = ref(750)
const userRole = ref('admin')

const navigate = (page, id) => {
  currentPage.value = page
  if (page === 'dream-detail' && id !== undefined) {
    selectedDreamId.value = id
  }
  if (page === 'lot-detail' && id !== undefined) {
    selectedLotId.value = id
  }
  if (page === 'purchase' && id !== undefined) {
    selectedLotId.value = id
  }
}

const updateBalance = (value) => {
  userBalance.value = value
}
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <Header
      :current-page="currentPage"
      :user-balance="userBalance"
      :user-role="userRole"
      @navigate="navigate"
    />

    <main class="pt-16">
      <HomePage v-if="currentPage === 'home'" @navigate="navigate" />

      <DreamDetailPage
        v-else-if="currentPage === 'dream-detail'"
        :dream-id="selectedDreamId"
        @navigate="navigate"
      />

      <CreateDreamPage v-else-if="currentPage === 'create-dream'" @navigate="navigate" />

      <CreateLotPage v-else-if="currentPage === 'create-lot'" @navigate="navigate" />

      <LotDetailPage
        v-else-if="currentPage === 'lot-detail'"
        :lot-id="selectedLotId"
        @navigate="navigate"
      />

      <PurchaseFlow
        v-else-if="currentPage === 'purchase'"
        :lot-id="selectedLotId"
        :user-balance="userBalance"
        @navigate="navigate"
        @update-balance="updateBalance"
      />

      <ProfilePage
        v-else-if="currentPage === 'profile'"
        :user-balance="userBalance"
        @navigate="navigate"
      />

      <NotificationsPage v-else-if="currentPage === 'notifications'" @navigate="navigate" />

      <AdminPage v-else-if="currentPage === 'admin'" @navigate="navigate" />

      <HomePage v-else @navigate="navigate" />
    </main>
  </div>
</template>
