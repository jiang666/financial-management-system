<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">账簿查询</h2>
      <p class="description">查看总账、明细账、多栏账和日记账等各类账簿</p>
    </div>
    
    <!-- 账簿类型选择 -->
    <el-card class="book-type-card" shadow="never">
      <el-tabs v-model="activeBookType" @tab-change="handleBookTypeChange">
        <el-tab-pane label="总账" name="general">
          <GeneralLedger />
        </el-tab-pane>
        <el-tab-pane label="明细账" name="detail">
          <DetailLedger />
        </el-tab-pane>
        <el-tab-pane label="多栏账" name="multi">
          <MultiColumnLedger />
        </el-tab-pane>
        <el-tab-pane label="日记账" name="journal">
          <JournalLedger />
        </el-tab-pane>
        <el-tab-pane label="科目余额" name="balance">
          <AccountBalance />
        </el-tab-pane>
        <el-tab-pane label="序时账" name="chronological">
          <ChronologicalBook />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import GeneralLedger from './components/GeneralLedger.vue'
import DetailLedger from './components/DetailLedger.vue'
import MultiColumnLedger from './components/MultiColumnLedger.vue'
import JournalLedger from './components/JournalLedger.vue'
import AccountBalance from './components/AccountBalance.vue'
import ChronologicalBook from './components/ChronologicalBook.vue'

const activeBookType = ref('general')

const handleBookTypeChange = (name) => {
  console.log('切换到账簿类型：', name)
}
</script>

<style scoped lang="scss">
.book-type-card {
  :deep(.el-tabs__content) {
    padding-top: 20px;
  }
}
</style>