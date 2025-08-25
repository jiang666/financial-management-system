const mysql = require('mysql2/promise');

// 所有缺失的参数定义
const missingParams = [
  // 基本设置
  { key: 'company.name', value: '', type: 'STRING', category: 'BASIC', description: '公司名称', required: 1, sort_order: 1 },
  { key: 'company.tax_number', value: '', type: 'STRING', category: 'BASIC', description: '纳税人识别号', required: 0, sort_order: 3 },
  { key: 'company.legal_person', value: '', type: 'STRING', category: 'BASIC', description: '法人代表', required: 0, sort_order: 4 },
  { key: 'company.address', value: '', type: 'STRING', category: 'BASIC', description: '注册地址', required: 0, sort_order: 5 },
  { key: 'company.phone', value: '', type: 'STRING', category: 'BASIC', description: '联系电话', required: 0, sort_order: 6 },
  
  // 凭证设置
  { key: 'voucher.number_rule', value: 'MONTHLY', type: 'ENUM', category: 'FINANCIAL', description: '凭证编号规则', required: 1, sort_order: 21, options: '["YEARLY","MONTHLY","CONTINUOUS"]' },
  { key: 'voucher.auto_number', value: 'true', type: 'BOOLEAN', category: 'FINANCIAL', description: '凭证自动编号', required: 1, sort_order: 20 },
  { key: 'voucher.approval_flow', value: 'SINGLE', type: 'ENUM', category: 'FINANCIAL', description: '审核流程', required: 1, sort_order: 22, options: '["NONE","SINGLE","MULTI"]' },
  
  // 权限控制
  { key: 'login.max_attempts', value: '5', type: 'INTEGER', category: 'SYSTEM', description: '最大登录尝试次数', required: 1, sort_order: 43, min_value: '3', max_value: '10' },
  
  // 备份设置
  { key: 'backup.auto_enable', value: 'true', type: 'BOOLEAN', category: 'SYSTEM', description: '启用自动备份', required: 1, sort_order: 60 },
  { key: 'backup.schedule', value: '02:00', type: 'TIME', category: 'SYSTEM', description: '备份时间', required: 1, sort_order: 61 },
  { key: 'backup.retention_days', value: '30', type: 'INTEGER', category: 'SYSTEM', description: '备份保留天数', required: 1, sort_order: 62, min_value: '7', max_value: '365' }
];

async function initMissingParams() {
  let connection;
  
  try {
    connection = await mysql.createConnection({
      host: '127.0.0.1',
      port: 12100,
      user: 'bunny',
      password: 'bunny',
      database: 'financial_db'
    });
    
    console.log('✅ 已连接到数据库\n');
    
    // 开始事务
    await connection.beginTransaction();
    
    console.log('🚀 开始添加缺失的参数...\n');
    
    let successCount = 0;
    let skipCount = 0;
    
    for (const param of missingParams) {
      try {
        // 检查参数是否已存在
        const [existing] = await connection.execute(
          'SELECT 1 FROM system_parameters WHERE param_key = ? AND deleted = 0',
          [param.key]
        );
        
        if (existing.length > 0) {
          console.log(`⏩ 跳过已存在的参数: ${param.key}`);
          skipCount++;
          continue;
        }
        
        // 生成UUID (简单实现)
        const uuid = 'param-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        const now = Date.now();
        
        // 插入参数
        await connection.execute(
          `INSERT INTO system_parameters (
            id, param_key, param_value, param_type, category, description,
            is_system, is_encrypted, required, visible, editable,
            effective_immediately, restart_required, sort_order,
            min_value, max_value, options, default_value,
            created_at, updated_at, created_by, deleted
          ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
          [
            uuid,
            param.key,
            param.value,
            param.type,
            param.category,
            param.description,
            param.is_system || 0,
            0, // is_encrypted
            param.required,
            1, // visible
            1, // editable
            1, // effective_immediately
            0, // restart_required
            param.sort_order,
            param.min_value || null,
            param.max_value || null,
            param.options || null,
            param.default_value || param.value,
            now,
            now,
            'system',
            0
          ]
        );
        
        console.log(`✅ 添加参数: ${param.key}`);
        successCount++;
        
      } catch (error) {
        console.error(`❌ 添加参数失败 ${param.key}:`, error.message);
      }
    }
    
    // 提交事务
    await connection.commit();
    
    console.log('\n📊 执行结果:');
    console.log(`- 成功添加: ${successCount} 个参数`);
    console.log(`- 跳过已存在: ${skipCount} 个参数`);
    
    // 再次统计总数
    const [countResult] = await connection.execute(
      'SELECT COUNT(*) as total FROM system_parameters WHERE deleted = 0'
    );
    
    console.log(`- 系统参数总数: ${countResult[0].total}`);
    
    // 验证关键参数
    console.log('\n✔️ 验证关键参数:');
    const checkKeys = ['company.name', 'voucher.auto_number', 'backup.auto_enable'];
    for (const key of checkKeys) {
      const [rows] = await connection.execute(
        'SELECT param_value FROM system_parameters WHERE param_key = ? AND deleted = 0',
        [key]
      );
      if (rows.length > 0) {
        console.log(`  ${key}: ✅`);
      } else {
        console.log(`  ${key}: ❌`);
      }
    }
    
  } catch (error) {
    if (connection) {
      await connection.rollback();
    }
    console.error('❌ 执行失败:', error.message);
  } finally {
    if (connection) {
      await connection.end();
      console.log('\n🔌 数据库连接已关闭');
    }
  }
}

// 执行初始化
initMissingParams().catch(console.error);