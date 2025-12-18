# 更新日志 (Changelog)

## 主要功能更新

### 1. 用户认证与权限管理
- **新增登录系统** (`LoginServlet.java`, `jsp/auth/login.jsp`)
  - 统一登录页面，支持客户和管理员两种角色登录
  - 基于Session的认证机制
  - 管理员和客户使用不同的数据表验证（`AdminUser` 和 `Customer`）

- **新增注册功能** (`RegisterServlet.java`, `jsp/auth/register.jsp`)
  - 客户可以注册新账户
  - 自动设置默认余额、信用等级和透支额度

- **权限过滤器** (`AuthFilter.java`)
  - 保护管理员后台路由（`/admin/*`, `/purchase/*`, `/report/*`, `/finance/*`）
  - 未登录用户自动重定向到登录页

- **角色区分导航栏** (`jsp/common/header.jsp`)
  - 根据用户角色动态显示不同的导航菜单
  - 管理员可见：客户管理、库存管理、图书管理、缺书记录、采购管理、供应商管理、物流管理、统计报表、财务统计
  - 客户可见：下单、订单管理、个人信息、订单历史

### 2. 订单管理系统优化

- **订单支付逻辑完善** (`OrdersDao.java`)
  - 下单时自动扣款，考虑信用等级折扣
  - 支持透支功能（信用等级3-5级）
  - 余额不足时创建 `CREATED` 状态订单，允许后续支付
  - 新增 `payOrder()` 方法处理 `CREATED` 订单的继续支付

- **订单状态管理**
  - `CREATED`: 已创建但未支付
  - `PAID`: 已支付
  - `PARTIAL`: 部分发货
  - `SHIPPED`: 已发货
  - 统一状态显示逻辑，修复状态不一致问题

- **订单确认收货功能**
  - 客户可以在订单管理页面确认收货
  - 新增 `Confirmed` 字段到 `Orders` 表
  - 管理员可以查看客户的收货状态

- **订单列表优化** (`jsp/order/orderList.jsp`, `jsp/order/orderDetail.jsp`)
  - 管理员可以看到下单客户信息
  - 客户只能看到自己的订单
  - 发货按钮仅对 `PAID` 和 `PARTIAL` 状态显示
  - `CREATED` 状态订单显示"继续支付"按钮

- **支付确认页面** (`jsp/order/payOrder.jsp`)
  - 显示订单详情、商品明细、账户余额
  - 确认支付后才会真正扣款

### 3. 物流管理优化

- **物流管理页面重构** (`ShipmentServlet.java`, `jsp/shipment/shipmentList.jsp`)
  - 只显示状态为 `PAID`、`PARTIAL`、`SHIPPED` 的订单
  - 根据订单状态控制发货按钮显示
  - 已发货订单显示"已发货"状态，不显示发货按钮
  - 统一订单显示顺序（按订单ID降序）

- **发货逻辑优化** (`ShipmentService.java`)
  - 防止重复扣款（已支付订单不再扣款）
  - 考虑信用等级和透支额度

### 4. 图书管理功能

- **图书上架/下架管理** (`AdminBookServlet.java`, `jsp/admin/bookList.jsp`, `jsp/admin/bookEdit.jsp`)
  - 新增 `IsActive` 字段控制图书上架状态
  - 管理员可以批量上架/下架图书
  - 客户只能看到上架的图书

- **图书信息编辑**
  - 支持编辑图书标题、出版社、价格、库存、位置、分类
  - 支持上传和更新图书封面图片（Base64编码存储）
  - 封面图片预览功能

### 5. 供应商管理功能

- **供应商CRUD操作** (`AdminSupplierServlet.java`)
  - 新增供应商 (`jsp/admin/supplierAdd.jsp`)
  - 删除供应商（带确认提示）
  - 查看供应商供货书目 (`jsp/admin/supplierSupply.jsp`)

- **供应商查询优化** (`SupplierDao.java`)
  - 支持按供应商ID和名称查询
  - 管理员专用查询方法

### 6. 采购管理优化

- **缺书记录详情页** (`PurchaseServlet.java`, `jsp/purchase/shortageDetail.jsp`)
  - 显示缺书记录详细信息
  - 显示关联的采购单列表
  - 支持从缺书记录生成采购单

- **采购单金额修复**
  - 修复从缺书记录创建采购单时金额为0的问题
  - 从 `BookSupplier` 表获取正确的供货价格
  - 自动计算采购单总金额和明细单价

- **状态显示统一**
  - 统一采购单状态显示（不区分大小写）
  - `CREATED` → "已创建"
  - `COMPLETED` → "已完成"

### 7. 财务统计功能（新增）

- **财务统计模块** (`FinanceDao.java`, `FinanceServlet.java`, `jsp/admin/financeStatistics.jsp`)
  - 统计书店的开支、收入和盈利
  - 支持按日、月、年查看数据
  - 使用 Chart.js 展示趋势图表
  - 显示详细数据表格
  - 数据来源：
    - **支出**：状态为 `COMPLETED` 的采购单总金额
    - **收入**：状态为 `PAID`、`PARTIAL`、`SHIPPED` 的订单总金额
    - **盈利**：收入 - 支出

### 8. 首页数据实时化

- **今日概览** (`index.jsp`)
  - 今日新增订单数（实时查询）
  - 今日发货订单数（实时查询）
  - 库存预警书目数（实时查询）
  - 待处理采购单数（实时查询）

### 9. 数据库结构更新

- **新增字段**
  - `Book.IsActive`: 控制图书上架/下架状态
  - `Orders.Confirmed`: 客户是否确认收货
  - `PurchaseOrder.ShortageID`: 关联缺书记录
  - `PurchaseOrder.TotalAmount`: 采购单总金额

- **新增表**
  - `AdminUser`: 管理员账户表

### 10. 其他优化

- **客户信息页面** (`jsp/customer/customerInfo.jsp`)
  - 客户端只能查看信用等级和透支额度，不能修改
  - 只保留"充值余额"功能

- **订单历史** (`CustomerServlet.java`, `jsp/customer/orderHistory.jsp`)
  - 客户只能查看自己的订单历史

- **导航栏优化**
  - 移除冗余的"后台管理"文本
  - "发货管理"更名为"物流管理"
  - 添加"财务统计"入口

- **图片显示优化**
  - 修复图书封面显示问题（使用Base64编码）
  - 优化图书搜索页面的封面显示（完整显示，不裁剪）

## 技术改进

1. **事务管理**
   - 订单创建、支付、发货等关键操作使用数据库事务
   - 确保数据一致性

2. **错误处理**
   - 统一的错误消息显示机制
   - 用户友好的错误提示

3. **代码组织**
   - DAO层方法完善
   - Service层业务逻辑封装
   - Servlet层请求处理分离

## 文件清单

### 新增文件
- `src/main/java/servlet/LoginServlet.java`
- `src/main/java/servlet/RegisterServlet.java`
- `src/main/java/servlet/AuthFilter.java`
- `src/main/java/servlet/AdminBookServlet.java`
- `src/main/java/servlet/FinanceServlet.java`
- `src/main/java/dao/AdminUserDao.java`
- `src/main/java/dao/FinanceDao.java`
- `src/main/java/model/AdminUser.java`
- `src/main/webapp/jsp/auth/login.jsp`
- `src/main/webapp/jsp/auth/register.jsp`
- `src/main/webapp/jsp/admin/bookList.jsp`
- `src/main/webapp/jsp/admin/bookEdit.jsp`
- `src/main/webapp/jsp/admin/supplierAdd.jsp`
- `src/main/webapp/jsp/admin/supplierSupply.jsp`
- `src/main/webapp/jsp/admin/financeStatistics.jsp`
- `src/main/webapp/jsp/purchase/shortageDetail.jsp`
- `src/main/webapp/jsp/order/payOrder.jsp`

### 主要修改文件
- `src/main/java/dao/OrdersDao.java` - 支付逻辑、订单查询
- `src/main/java/dao/BookDao.java` - 图书上架/下架、封面更新
- `src/main/java/dao/PurchaseOrderDao.java` - 采购单查询
- `src/main/java/dao/SupplierDao.java` - 供应商CRUD
- `src/main/java/servlet/OrderServlet.java` - 订单管理、支付确认
- `src/main/java/servlet/ShipmentServlet.java` - 物流管理优化
- `src/main/java/servlet/PurchaseServlet.java` - 采购单金额修复、缺书记录详情
- `src/main/java/servlet/CustomerServlet.java` - 客户订单历史
- `src/main/java/service/ShipmentService.java` - 防止重复扣款
- `src/main/webapp/WEB-INF/web.xml` - Servlet和过滤器配置
- `src/main/webapp/jsp/common/header.jsp` - 角色区分导航栏
- `src/main/webapp/jsp/order/orderList.jsp` - 订单列表优化
- `src/main/webapp/jsp/order/orderDetail.jsp` - 订单详情优化
- `src/main/webapp/jsp/shipment/shipmentList.jsp` - 物流管理重构
- `src/main/webapp/jsp/customer/customerInfo.jsp` - 客户信息页面优化
- `src/main/webapp/index.jsp` - 首页数据实时化

## 注意事项

1. **数据库迁移**：需要执行SQL脚本添加新字段和新表
2. **权限控制**：所有管理员功能都受到 `AuthFilter` 保护
3. **数据一致性**：订单支付、发货等操作都使用事务保证数据一致性
4. **图片存储**：图书封面使用Base64编码存储在数据库中

