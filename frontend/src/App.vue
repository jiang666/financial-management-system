<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// App组件负责渲染路由视图
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

#app {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

/* 全局修复 Element Plus 布局 */
.el-container {
  height: 100%;
  
  &.main-layout {
    display: flex !important;
    flex-direction: row !important;
    gap: 0 !important;
    
    > .el-aside {
      flex: 0 0 240px !important;
      width: 240px !important;
      border: none !important;
      margin: 0 !important;
      padding: 0 !important;
      transition: all 0.3s !important;
      
      &.is-collapse {
        flex: 0 0 64px !important;
        width: 64px !important;
      }
    }
    
    > .el-container {
      flex: 1 1 auto !important;
      margin: 0 !important;
      padding: 0 !important;
      min-width: 0 !important;
      overflow: hidden !important;
    }
  }
}

/* 修复菜单样式 */
.el-menu {
  border: none !important;
  width: 100% !important;
  
  &.el-menu-vertical:not(.el-menu--collapse) {
    width: 100% !important;
  }
  
  .el-sub-menu {
    .el-menu {
      background-color: #1f2d3d !important;
    }
  }
  
  .el-menu-item,
  .el-sub-menu__title {
    overflow: hidden !important;
    text-overflow: ellipsis !important;
  }
}

/* 防止布局抖动 */
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 240px !important;
  min-width: 240px !important;
}

.el-menu--collapse {
  width: 64px !important;
}
</style>
