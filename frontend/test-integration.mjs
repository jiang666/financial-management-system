#!/usr/bin/env node

import axios from 'axios';

const API_BASE = 'http://localhost:8086/api';
let authToken = '';

// 测试登录
async function testLogin() {
  console.log('=== 测试登录 ===');
  try {
    const response = await axios.post(`${API_BASE}/v1/auth/login`, {
      username: 'admin',
      password: 'admin123'
    });
    
    if (response.data.code === 200) {
      authToken = response.data.data.token;
      console.log('✅ 登录成功');
      console.log('用户信息:', response.data.data.userInfo.username);
      console.log('角色:', response.data.data.authorities);
      console.log('资源数量:', response.data.data.resources.length);
      return true;
    }
  } catch (error) {
    console.error('❌ 登录失败:', error.message);
    return false;
  }
}

// 测试获取用户列表
async function testGetUsers() {
  console.log('\n=== 测试获取用户列表 ===');
  try {
    const response = await axios.get(`${API_BASE}/v1/users?page=0&size=10`, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    if (response.data.code === 200) {
      console.log('✅ 获取用户列表成功');
      console.log('用户数量:', response.data.data.totalElements);
      console.log('第一个用户:', response.data.data.content[0].username);
      return true;
    }
  } catch (error) {
    console.error('❌ 获取用户列表失败:', error.message);
    return false;
  }
}

// 测试获取角色列表
async function testGetRoles() {
  console.log('\n=== 测试获取角色列表 ===');
  try {
    const response = await axios.get(`${API_BASE}/v1/roles?page=0&size=10`, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    if (response.data.code === 200) {
      console.log('✅ 获取角色列表成功');
      console.log('角色数量:', response.data.data.totalElements);
      console.log('第一个角色:', response.data.data.content[0].name);
      return true;
    }
  } catch (error) {
    console.error('❌ 获取角色列表失败:', error.message);
    return false;
  }
}

// 测试获取资源树
async function testGetResourceTree() {
  console.log('\n=== 测试获取资源树 ===');
  try {
    const response = await axios.get(`${API_BASE}/v1/resources/tree`, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    if (response.data.code === 200) {
      console.log('✅ 获取资源树成功');
      console.log('顶级资源数量:', response.data.data.length);
      console.log('第一个资源:', response.data.data[0].resourceName);
      return true;
    }
  } catch (error) {
    console.error('❌ 获取资源树失败:', error.message);
    return false;
  }
}

// 测试登出
async function testLogout() {
  console.log('\n=== 测试登出 ===');
  try {
    const response = await axios.post(`${API_BASE}/v1/auth/logout`, {}, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    
    if (response.data.code === 200) {
      console.log('✅ 登出成功');
      return true;
    }
  } catch (error) {
    console.error('❌ 登出失败:', error.message);
    return false;
  }
}

// 主测试函数
async function runTests() {
  console.log('开始测试前后端集成...\n');
  
  const tests = [
    testLogin,
    testGetUsers,
    testGetRoles,
    testGetResourceTree,
    testLogout
  ];
  
  let passed = 0;
  for (const test of tests) {
    const result = await test();
    if (result) passed++;
  }
  
  console.log(`\n测试完成: ${passed}/${tests.length} 通过`);
  
  if (passed === tests.length) {
    console.log('✅ 所有测试通过！前后端集成正常。');
  } else {
    console.log('❌ 部分测试失败，请检查日志。');
  }
}

// 运行测试
runTests().catch(console.error);