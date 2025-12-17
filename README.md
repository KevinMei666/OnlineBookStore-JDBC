# 网上书店管理系统（数据库课程设计）

本项目是《数据库原理》课程的综合实验——“网上书店管理系统”。后端采用 **Java Servlet + JSP + JDBC（MySQL）**，前端基于 **Bootstrap 5 + 自定义样式**，并集成了书目管理、客户/订单/采购/发货管理及统计报表（视图演示）等功能。

---

## 功能概览

- **首页 / 导航**  
  - 系统欢迎页，展示书籍/订单/客户等统计信息。  
  - 快捷入口：书目查询、创建订单、采购管理、客户管理、发货管理、统计报表。  

- **书目管理（book）**  
  - 图书列表与详情：按书名、作者、关键字等查询。  

- **订单与购物车（order）**  
  - 购物车管理、创建订单、订单列表与订单详情。  

- **采购管理与缺书记录（purchase）**  
  - 采购单列表/明细、缺书记录列表（支持从缺书记录生成采购单，待进一步完善）。  

- **客户管理（customer）**  
  - 客户信息展示：ID、姓名、邮箱、地址、余额、信用等级、透支额度。  
  - 客户历史订单查询。  
  - 管理员端可调整客户余额、信用等级与透支额度。  

- **发货管理（shipment）**  
  - 发货记录列表，展示订单发货情况。  

- **统计报表 / 视图演示（report）**  
  - 通过 `ReportServlet` 与 `viewDemo.jsp`，演示以下 6 个数据库视图：  
    - `v_book_basic_info`：书籍基本信息  
    - `v_book_detail`：书籍 + 作者 + 关键词  
    - `v_customer_info`：客户信息视图  
    - `v_customer_orders`：客户订单视图  
    - `v_order_items_detail`：订单明细视图  
    - `v_shipment_detail`：发货明细视图  

- **管理员端（AdminCustomer）**  
  - 入口：导航栏「客户管理」 → `/admin/customer/list`。  
  - 客户列表查询：按客户ID、姓名模糊查询。  
  - 编辑客户账户：调整余额、信用等级（1~5 级）、月度透支额度，并带有信用规则说明。  

---

## 技术栈与项目结构

- 语言与框架：  
  - Java 8+/11+/21（本机示例使用 JDK 21）  
  - Servlet 4.0 (`javax.servlet-api-4.0.1`)  
  - JSP + JSTL (`jstl-1.2`)  
  - JDBC 访问 MySQL (`mysql-connector-java-8.0.20`)  
  - 前端样式：Bootstrap 5 + `src/main/webapp/css/style.css` + Bootstrap Icons  

- 项目结构（关键目录）：  
  - `src/main/java/`  
    - `dao/`：数据访问层（BookDao / CustomerDao / OrderDao / PurchaseDao / ShipmentDao / ...）  
    - `model/`：实体类（Book / Customer / Order / OrderItem / PurchaseOrder / Shipment / ...）  
    - `service/`：业务逻辑封装（如 CustomerQueryService、ShipmentService 等）  
    - `servlet/`：控制器层（`BookServlet`、`OrderServlet`、`PurchaseServlet`、`CustomerServlet`、`ShipmentServlet`、`ReportServlet`、`AdminCustomerServlet` 等）  
    - `util/DBUtil.java`：数据库连接与资源释放工具类。  
  - `src/main/webapp/`  
    - `index.jsp`：系统首页。  
    - `jsp/book/`、`jsp/order/`、`jsp/purchase/`、`jsp/customer/`、`jsp/shipment/`、`jsp/report/`、`jsp/admin/`。  
    - `jsp/common/header.jsp` / `footer.jsp` / `message.jsp`：统一导航栏、页脚与全局消息提示。  
    - `WEB-INF/web.xml`：Servlet 与错误页配置。  
    - `css/style.css`：全站统一样式（按钮 / 表格 / 卡片 / 表单 / 响应式等）。  
    - `js/common.js`：通用 JS 工具（表单验证、金额/日期格式化、AJAX 封装与错误处理）。  

---

## 环境准备

1. **数据库（MySQL）**  
   - 安装并启动 MySQL（建议 5.7+ 或 8.x）。  
   - 创建数据库（默认示例名）：`OnlineBookStore`。  
   - 执行课程提供的建表脚本 / 视图 / 存储过程 / 触发器（见课程资料或 `实验18 数据库设计.md` 中的要求）。  
   - 根据实际情况修改 `src/main/java/util/DBUtil.java` 中的连接配置：  
     ```java
     private static final String JDBC_URL =
             "jdbc:mysql://localhost:3306/OnlineBookStore?useSSL=false&serverTimezone=Asia/Shanghai";
     private static final String JDBC_USER = "root";
     private static final String JDBC_PASSWORD = "你的数据库密码";
     ```

2. **应用服务器（Tomcat）**  
   - 使用项目自带的 `lib/apache-tomcat-9.0.113-windows-x64/apache-tomcat-9.0.113`，或你本地已有的 Tomcat 9。  
   - 在 IntelliJ IDEA 中配置 Tomcat Server，并挂载 `数据库课程设计:war exploded` 构件：  
     - Application context 建议设置为 `/`，这样首页访问地址就是 `http://localhost:8080/`。  

3. **IDE 配置（以 IntelliJ IDEA 为例）**  
   - 使用 `File → Open` 打开本项目根目录。  
   - 确保 JDK 版本设置为 8/11/21 中之一。  
   - 检查 `Project Structure → Modules / Artifacts / Facets`，确保：  
     - Web 资源目录指向 `src/main/webapp`；  
     - `WEB-INF/web.xml` 正确指向 `src/main/webapp/WEB-INF/web.xml`；  
     - 构件 `数据库课程设计:war exploded` 的输出布局中包含：  
       - `<输出根>/WEB-INF/classes` → `数据库课程设计 编译输出`  
       - `<输出根>/WEB-INF/lib` → `jstl-1.2.jar`、`mysql-connector-java-8.0.20.jar` 等依赖  
       - `<输出根>/webapp 目录内容`（即所有 JSP/CSS/JS/WEB-INF）。  

---

## 运行方法

1. **启动数据库**：确保 MySQL 服务已启动且数据库 `OnlineBookStore` 可访问。  
2. **在 IDEA 中构建并部署**：  
   - `Build → Build Artifacts… → 数据库课程设计:war exploded → Rebuild`。  
   - 启动 `Tomcat 9.0.113` 的运行配置。  
3. **通过浏览器访问**：  
   - 首页：`http://localhost:8080/`  
   - 书目查询：`http://localhost:8080/book`  
   - 订单管理：`http://localhost:8080/order/list`  
   - 客户信息（前台）：`http://localhost:8080/customer/info`  
   - 统计报表（视图演示）：`http://localhost:8080/report/views`  
   - 管理员客户管理：`http://localhost:8080/admin/customer/list`（也可通过导航栏「客户管理」进入）。  

> **提示**：如果你在 Tomcat 配置中把 Application context 设为 `/db`，则访问地址前需要加上 `/db` 前缀，例如 `http://localhost:8080/db/`。

---

## 后续扩展建议

- 补充/完善：  
  - 供应商管理（供应商信息、供货信息）前端与对应 Servlet。  
  - 库存管理与从缺书记录自动生成采购单，到货后自动更新库存。  
  - 客户注册 / 登录 / 自助信息维护页面。  
- 优化：  
  - 使用过滤器重新启用全局字符编码（`CharacterEncodingFilter`）。  
  - 将部分业务逻辑下沉到存储过程 / 触发器，以呼应实验18对“存储过程和触发器”的要求。  

---

如需在答辩/演示时使用本项目，可根据课程要求补充截图、用例说明和 ER 图，或在 `README` 中增加“实验报告”与“数据库设计说明书”的链接。  


