const axios = require('axios');

async function debugSystemParamError() {
    console.log('🔍 调试系统参数API 500错误\n');
    
    const api = axios.create({
        baseURL: 'http://localhost:8086',
        timeout: 10000,
        validateStatus: () => true // 接受所有状态码
    });

    try {
        // 1. 登录获取token
        console.log('1. 登录系统...');
        const loginRes = await api.post('/api/v1/auth/login', {
            username: 'admin',
            password: 'admin123'
        });
        
        if (loginRes.data.code === 200 && loginRes.data.data) {
            const token = loginRes.data.data.token;
            api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            console.log('✅ 登录成功\n');
        }

        // 2. 测试系统参数API
        console.log('2. 测试系统参数API...');
        console.log('   请求: GET /api/v1/system/parameters');
        
        const res = await api.get('/api/v1/system/parameters');
        
        console.log('   状态码:', res.status);
        console.log('   响应头:', JSON.stringify(res.headers, null, 2));
        console.log('   响应体:', JSON.stringify(res.data, null, 2));
        
        if (res.status === 500) {
            console.log('\n❌ 500错误详细信息:');
            if (res.data) {
                console.log('   错误消息:', res.data.message || res.data.error || '无具体错误信息');
                console.log('   错误路径:', res.data.path);
                console.log('   时间戳:', res.data.timestamp);
                
                if (res.data.trace) {
                    console.log('\n   堆栈跟踪（前500字符）:');
                    console.log(res.data.trace.substring(0, 500) + '...');
                }
            }
            
            console.log('\n💡 可能的原因:');
            console.log('   1. SystemParameterService 注入失败');
            console.log('   2. 数据库连接问题');
            console.log('   3. 权限验证失败');
            console.log('   4. 数据查询异常');
            console.log('   5. DTO转换错误');
            
            console.log('\n🔧 建议检查:');
            console.log('   - 后端日志文件查看详细错误堆栈');
            console.log('   - SystemParameterService是否正确注入');
            console.log('   - 数据库表system_parameters是否存在');
            console.log('   - 用户是否有SYSTEM_PARAM_VIEW权限');
        }
        
        // 3. 测试其他接口对比
        console.log('\n3. 测试其他接口对比...');
        const testEndpoints = [
            { path: '/api/v1/roles', name: '角色接口' },
            { path: '/api/v1/users', name: '用户接口' },
            { path: '/api/v1/departments', name: '部门接口' },
            { path: '/api/v1/positions', name: '岗位接口' }
        ];
        
        for (const endpoint of testEndpoints) {
            const testRes = await api.get(endpoint.path);
            console.log(`   ${endpoint.name}: ${testRes.status} ${testRes.status === 200 ? '✅' : '❌'}`);
        }
        
        // 4. 检查数据库
        console.log('\n4. 使用其他方式验证数据...');
        
        // 尝试获取单个参数
        const singleParamRes = await api.get('/api/v1/system/parameters/company.name');
        console.log('   获取单个参数(company.name):', singleParamRes.status);
        if (singleParamRes.status === 500) {
            console.log('   错误:', singleParamRes.data?.message);
        }
        
    } catch (error) {
        console.error('\n❌ 调试过程出错:', error.message);
    }
}

// 运行调试
debugSystemParamError().catch(console.error);