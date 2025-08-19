<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">凭证管理</h2>
      <p class="description">统一管理凭证录入、审核、记账、查询和冲销等操作</p>
    </div>
    
    <!-- 凭证管理选项卡 -->
    <el-card class="voucher-management-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="凭证列表" name="list">
          <VoucherList />
        </el-tab-pane>
        <el-tab-pane label="凭证录入" name="entry">
          <VoucherEntry />
        </el-tab-pane>
        <el-tab-pane label="凭证审核" name="review">
          <VoucherReview />
        </el-tab-pane>
        <el-tab-pane label="凭证记账" name="posting">
          <VoucherPosting />
        </el-tab-pane>
        <el-tab-pane label="凭证查询" name="query">
          <VoucherQuery />
        </el-tab-pane>
        <el-tab-pane label="凭证冲销" name="reverse">
          <VoucherReverse />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import VoucherEntry from '../entry/index.vue'
import VoucherReview from '../review/index.vue'
import VoucherPosting from '../posting/index.vue'
import VoucherQuery from '../query/index.vue'
import VoucherReverse from '../reverse/index.vue'
import VoucherList from '../list/index.vue'

const activeTab = ref('list')

const handleTabChange = (name) => {
  console.log('切换到凭证管理类型：', name)
}
</script>

<style scoped lang="scss">
.voucher-management-card {
  :deep(.el-tabs__content) {
    padding-top: 20px;
  }
  
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
}
</style>