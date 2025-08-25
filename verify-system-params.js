const mysql = require('mysql2/promise');

// 前端页面需要的所有参数列表
const requiredParams = {
  // 基本设置
  BASIC: [
    'company.name',
    'company.tax_number', 
    'company.legal_person',
    'company.address',
    'company.phone',
    'company.email'
  ],
  
  // 会计期间设置
  PERIOD: [
    'period.fiscal_year',
    'period.start_period',
    'period.current_period',
    'period.is_period_close',
    'period.auto_carry_forward'
  ],
  
  // 凭证设置
  VOUCHER: [
    'voucher.number_rule',
    'voucher.number_length',
    'voucher.allow_edit_after_audit',
    'voucher.types',
    'voucher.auto_number',
    'voucher.approval_flow'
  ],
  
  // 权限控制
  PERMISSION: [
    'permission.creator_can_edit',
    'permission.auditor_can_edit',
    'permission.bookkeeper_can_edit',
    'permission.cross_period_query',
    'permission.export_roles',
    'login.lock_enabled',
    'login.max_attempts'
  ],
  
  // 备份设置
  BACKUP: [
    'backup.auto_enable',
    'backup.frequency',
    'backup.schedule',
    'backup.retention_days'
  ]
};

async function verifySystemParams() {
  let connection;
  
  try {
    // 连接数据库
    connection = await mysql.createConnection({
      host: '127.0.0.1',
      port: 12100,
      user: 'bunny',
      password: 'bunny',
      database: 'financial_db'
    });
    
    console.log('已连接到数据库\n');
    
    // 检查system_parameters表
    const [tables] = await connection.execute(
      "SHOW TABLES LIKE 'system_parameters'"
    );
    
    if (tables.length === 0) {
      console.error('❌ system_parameters表不存在！');
      return;
    }
    
    console.log('✅ system_parameters表存在\n');
    
    // 获取所有已存在的参数
    const [existingParams] = await connection.execute(
      "SELECT param_key, param_value, param_type, category FROM system_parameters WHERE deleted = 0"
    );
    
    const paramMap = new Map();
    existingParams.forEach(p => {
      paramMap.set(p.param_key, p);
    });
    
    console.log(`数据库中共有 ${existingParams.length} 个参数\n`);
    
    // 检查每个分类的参数
    let totalRequired = 0;
    let totalFound = 0;
    let missingParams = [];
    
    for (const [category, params] of Object.entries(requiredParams)) {
      console.log(`\n【${category}】检查结果：`);
      console.log('─'.repeat(50));
      
      for (const paramKey of params) {
        totalRequired++;
        if (paramMap.has(paramKey)) {
          totalFound++;
          const param = paramMap.get(paramKey);
          console.log(`✅ ${paramKey} - ${param.param_type} - 值: ${param.param_value || '(空)'}`);
        } else {
          missingParams.push({ category, paramKey });
          console.log(`❌ ${paramKey} - 缺失`);
        }
      }
    }
    
    // 统计结果
    console.log('\n' + '='.repeat(60));
    console.log('检查统计：');
    console.log(`- 需要的参数总数: ${totalRequired}`);
    console.log(`- 已存在的参数数: ${totalFound}`);
    console.log(`- 缺失的参数数: ${missingParams.length}`);
    console.log(`- 完成率: ${(totalFound / totalRequired * 100).toFixed(2)}%`);
    
    if (missingParams.length > 0) {
      console.log('\n缺失的参数列表：');
      missingParams.forEach(({ category, paramKey }) => {
        console.log(`- [${category}] ${paramKey}`);
      });
      
      console.log('\n⚠️  请执行数据库迁移脚本 V9__add_missing_system_parameters.sql 来添加缺失的参数！');
      console.log('执行命令: cd backend && mvn flyway:migrate');
    } else {
      console.log('\n✅ 所有必需的参数都已存在！');
    }
    
    // 检查额外的参数（已初始化但前端未使用）
    console.log('\n\n额外的参数（已初始化但前端页面未使用）：');
    console.log('─'.repeat(50));
    
    const allRequiredKeys = Object.values(requiredParams).flat();
    existingParams.forEach(param => {
      if (!allRequiredKeys.includes(param.param_key)) {
        console.log(`- ${param.param_key} (${param.category})`);
      }
    });
    
  } catch (error) {
    console.error('错误:', error.message);
  } finally {
    if (connection) {
      await connection.end();
      console.log('\n\n数据库连接已关闭');
    }
  }
}

// 运行验证
verifySystemParams().catch(console.error);