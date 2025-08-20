<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">财务管理系统</h2>
      
      <el-tabs v-model="loginType" stretch>
        <el-tab-pane label="账号密码登录" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules">
            <el-form-item prop="username">
              <el-input
                v-model="passwordForm.username"
                size="large"
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="passwordForm.password"
                type="password"
                size="large"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handlePasswordLogin"
              />
            </el-form-item>
            <el-form-item>
              <div class="login-options">
                <el-checkbox v-model="passwordForm.remember">记住我</el-checkbox>
                <el-link type="primary">忘记密码？</el-link>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handlePasswordLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="手机验证码登录" name="phone">
          <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules">
            <el-form-item prop="phone">
              <el-input
                v-model="phoneForm.phone"
                size="large"
                placeholder="请输入手机号"
                :prefix-icon="Phone"
              />
            </el-form-item>
            <el-form-item prop="code">
              <el-row :gutter="10">
                <el-col :span="16">
                  <el-input
                    v-model="phoneForm.code"
                    size="large"
                    placeholder="请输入验证码"
                    :prefix-icon="Message"
                    @keyup.enter="handlePhoneLogin"
                  />
                </el-col>
                <el-col :span="8">
                  <el-button
                    size="large"
                    class="code-btn"
                    :disabled="codeTimer > 0"
                    @click="sendCode"
                  >
                    {{ codeTimer > 0 ? `${codeTimer}秒后重试` : '获取验证码' }}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handlePhoneLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <div class="third-party">
        <el-divider>其他登录方式</el-divider>
        <div class="third-party-buttons">
          <el-button circle size="large">
            <el-icon><Connection /></el-icon>
          </el-button>
          <el-button circle size="large">
            <el-icon><ChatDotSquare /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message, Connection, ChatDotSquare } from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()

const loginType = ref('password')
const loading = ref(false)
const codeTimer = ref(0)

const passwordFormRef = ref()
const phoneFormRef = ref()

const passwordForm = ref({
  username: 'admin',
  password: 'admin123',
  remember: false
})

const phoneForm = ref({
  phone: '',
  code: ''
})

const passwordRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const handlePasswordLogin = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await store.dispatch('login', passwordForm.value)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}

const handlePhoneLogin = async () => {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await store.dispatch('login', { username: phoneForm.value.phone })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}

const sendCode = () => {
  if (!phoneForm.value.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  codeTimer.value = 60
  const timer = setInterval(() => {
    codeTimer.value--
    if (codeTimer.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  ElMessage.success('验证码已发送')
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .login-card {
    width: 450px;
    background: #fff;
    border-radius: 8px;
    padding: 40px 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .login-title {
      text-align: center;
      font-size: 24px;
      color: #303133;
      margin-bottom: 30px;
    }
    
    .login-options {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .login-btn {
      width: 100%;
    }
    
    .code-btn {
      width: 100%;
    }
    
    .third-party {
      margin-top: 20px;
      
      .third-party-buttons {
        display: flex;
        justify-content: center;
        gap: 20px;
        
        .el-button {
          &:hover {
            transform: scale(1.1);
            transition: transform 0.3s;
          }
        }
      }
    }
  }
}
</style>