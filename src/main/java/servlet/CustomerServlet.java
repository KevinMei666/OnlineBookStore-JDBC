package servlet;

import dao.BookDao;
import dao.BookSupplierDao;
import dao.CustomerDao;
import dao.ShortageRecordDao;
import model.Customer;
import model.ShortageRecord;
import service.CustomerQueryService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerServlet extends HttpServlet {
    
    private CustomerDao customerDao;
    private CustomerQueryService customerQueryService;
    private BookDao bookDao;
    private ShortageRecordDao shortageRecordDao;
    private BookSupplierDao bookSupplierDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        customerDao = new CustomerDao();
        customerQueryService = new CustomerQueryService();
        bookDao = new BookDao();
        shortageRecordDao = new ShortageRecordDao();
        bookSupplierDao = new BookSupplierDao();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/info") || pathInfo.equals("/wallet")) {
            // 显示客户信息
            handleCustomerInfo(request, response);
        } else if (pathInfo.equals("/profile")) {
            // 编辑个人资料
            handleProfileEdit(request, response);
        } else if (pathInfo.equals("/orders")) {
            // 显示订单历史
            handleOrderHistory(request, response);
        } else if (pathInfo.startsWith("/shortage/register")) {
            // 显示缺书登记页面
            handleShortageRegister(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo.equals("/addBalance")) {
            // 充值余额
            handleAddBalance(request, response);
        } else if (pathInfo.equals("/updateProfile")) {
            // 更新个人资料
            handleUpdateProfile(request, response);
        } else if (pathInfo.equals("/changePassword")) {
            // 修改密码
            handleChangePassword(request, response);
        } else if (pathInfo.equals("/updateCredit")) {
            // 调整信用等级或透支额度
            handleUpdateCredit(request, response);
        } else if (pathInfo.equals("/shortage/register")) {
            // 提交缺书登记
            handleShortageRegisterPost(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示客户信息
     */
    private void handleCustomerInfo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // 仅允许已登录的客户查看自己的信息
        String currentRole = (String) session.getAttribute("currentRole");
        if (!"CUSTOMER".equals(currentRole)) {
            session.setAttribute("warningMessage", "请以客户身份登录后查看个人信息");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "登录已失效，请重新登录后查看个人信息");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        // 查询客户信息
        Customer customer = customerDao.findById(customerId);
        if (customer == null) {
            session.setAttribute("errorMessage", "客户信息不存在");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        request.setAttribute("pageTitle", "用户中心");
        request.setAttribute("pageIcon", "bi-person-square");
        request.setAttribute("customer", customer);
        request.getRequestDispatcher("/jsp/customer/customerInfo.jsp").forward(request, response);
    }

    /**
     * 显示个人资料编辑页
     */
    private void handleProfileEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        String currentRole = (String) session.getAttribute("currentRole");
        if (!"CUSTOMER".equals(currentRole)) {
            session.setAttribute("warningMessage", "请以客户身份登录后再操作");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "登录已失效，请重新登录后操作");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Customer customer = customerDao.findById(customerId);
        if (customer == null) {
            session.setAttribute("errorMessage", "客户信息不存在");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        request.setAttribute("pageTitle", "用户中心");
        request.setAttribute("pageIcon", "bi-person-square");
        request.setAttribute("pageSubtitle", "修改个人信息");
        request.setAttribute("customer", customer);
        request.getRequestDispatcher("/jsp/customer/profileEdit.jsp").forward(request, response);
    }
    
    /**
     * 显示订单历史
     */
    private void handleOrderHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();

        // 仅允许已登录的客户查看自己的订单历史
        String currentRole = (String) session.getAttribute("currentRole");
        if (!"CUSTOMER".equals(currentRole)) {
            session.setAttribute("warningMessage", "请以客户身份登录后查看订单历史");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "登录已失效，请重新登录后查看订单历史");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }
        
        // 查询订单历史
        List<CustomerQueryService.CustomerOrderDetail> orderHistory = 
                customerQueryService.getCustomerOrderHistory(customerId);
        
        request.setAttribute("orderHistory", orderHistory);
        request.getRequestDispatcher("/jsp/customer/orderHistory.jsp").forward(request, response);
    }
    
    /**
     * 充值余额
     */
    private void handleAddBalance(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String customerIdStr = request.getParameter("customerId");
        String amountStr = request.getParameter("amount");
        HttpSession session = request.getSession();
        
        if (customerIdStr == null || amountStr == null) {
            session.setAttribute("errorMessage", "参数不完整");
            response.sendRedirect(request.getContextPath() + "/customer/info");
            return;
        }
        
        try {
            int customerId = Integer.parseInt(customerIdStr);
            BigDecimal amount = new BigDecimal(amountStr);
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                session.setAttribute("errorMessage", "充值金额必须大于0");
                response.sendRedirect(request.getContextPath() + "/customer/info");
                return;
            }
            
            // 调用DAO充值余额
            int result = customerDao.addBalance(customerId, amount);
            
            if (result > 0) {
                session.setAttribute("successMessage", "余额充值成功！充值金额：¥" + amount);
            } else {
                session.setAttribute("errorMessage", "余额充值失败，请重试");
            }
            
            response.sendRedirect(request.getContextPath() + "/customer/info");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的参数格式");
            response.sendRedirect(request.getContextPath() + "/customer/info");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "余额充值失败：" + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/customer/info");
        }
    }

    /**
     * 更新客户基本资料（姓名/邮箱/地址）
     */
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String currentRole = (String) session.getAttribute("currentRole");
        if (!"CUSTOMER".equals(currentRole)) {
            session.setAttribute("warningMessage", "请以客户身份登录后再操作");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "登录已失效，请重新登录后再试");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (email == null || email.trim().isEmpty()) {
            session.setAttribute("errorMessage", "邮箱不能为空");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        // 如果用户填写了密码字段，需要验证并更新密码
        boolean needUpdatePassword = (oldPassword != null && !oldPassword.trim().isEmpty()) ||
                                     (newPassword != null && !newPassword.trim().isEmpty()) ||
                                     (confirmPassword != null && !confirmPassword.trim().isEmpty());

        if (needUpdatePassword) {
            // 验证密码字段是否都填写
            if (oldPassword == null || oldPassword.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
                session.setAttribute("errorMessage", "修改密码时，当前密码、新密码和确认密码都必须填写");
                response.sendRedirect(request.getContextPath() + "/customer/profile");
                return;
            }

            // 验证新密码长度
            if (newPassword.trim().length() < 6) {
                session.setAttribute("errorMessage", "新密码长度至少为6位");
                response.sendRedirect(request.getContextPath() + "/customer/profile");
                return;
            }

            // 验证两次输入的新密码是否一致
            if (!newPassword.trim().equals(confirmPassword.trim())) {
                session.setAttribute("errorMessage", "两次输入的新密码不一致");
                response.sendRedirect(request.getContextPath() + "/customer/profile");
                return;
            }

            // 验证旧密码
            Customer customer = customerDao.findById(customerId);
            if (customer == null) {
                session.setAttribute("errorMessage", "客户信息不存在");
                response.sendRedirect(request.getContextPath() + "/customer/profile");
                return;
            }

            // 当前系统为明文存储，直接比较
            if (!oldPassword.trim().equals(customer.getPasswordHash())) {
                session.setAttribute("errorMessage", "当前密码错误");
                response.sendRedirect(request.getContextPath() + "/customer/profile");
                return;
            }

            // 检查新密码是否与旧密码相同
            if (newPassword.trim().equals(oldPassword.trim())) {
                session.setAttribute("errorMessage", "新密码不能与当前密码相同");
                response.sendRedirect(request.getContextPath() + "/customer/profile");
                return;
            }

            // 更新密码
            int passwordResult = customerDao.updatePassword(customerId, newPassword.trim());
            if (passwordResult <= 0) {
                session.setAttribute("errorMessage", "密码修改失败，请重试");
                response.sendRedirect(request.getContextPath() + "/customer/profile");
                return;
            }
        }

        // 更新基本信息
        int result = customerDao.updateBasicInfo(customerId,
                name != null ? name.trim() : null,
                email.trim(),
                address != null ? address.trim() : null);

        if (result > 0) {
            // 刷新session中的当前用户显示名
            Customer updated = customerDao.findById(customerId);
            session.setAttribute("currentCustomer", updated);
            session.setAttribute("currentUser", updated.getName() != null ? updated.getName() : updated.getEmail());
            if (needUpdatePassword) {
                session.setAttribute("successMessage", "个人信息和密码已更新");
            } else {
                session.setAttribute("successMessage", "个人信息已更新");
            }
        } else {
            session.setAttribute("errorMessage", "个人信息更新失败，请重试");
        }

        response.sendRedirect(request.getContextPath() + "/customer/profile");
    }

    /**
     * 处理客户密码修改
     */
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String currentRole = (String) session.getAttribute("currentRole");
        if (!"CUSTOMER".equals(currentRole)) {
            session.setAttribute("warningMessage", "请以客户身份登录后再操作");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "登录已失效，请重新登录后再试");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // 验证输入
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            session.setAttribute("errorMessage", "请输入当前密码");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            session.setAttribute("errorMessage", "请输入新密码");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        if (newPassword.length() < 6) {
            session.setAttribute("errorMessage", "新密码长度至少为6位");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("errorMessage", "两次输入的新密码不一致");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        // 验证旧密码
        Customer customer = customerDao.findById(customerId);
        if (customer == null) {
            session.setAttribute("errorMessage", "客户信息不存在");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        // 当前系统为明文存储，直接比较
        if (!oldPassword.trim().equals(customer.getPasswordHash())) {
            session.setAttribute("errorMessage", "当前密码错误");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        // 检查新密码是否与旧密码相同
        if (newPassword.trim().equals(oldPassword.trim())) {
            session.setAttribute("errorMessage", "新密码不能与当前密码相同");
            response.sendRedirect(request.getContextPath() + "/customer/profile");
            return;
        }

        // 更新密码
        int result = customerDao.updatePassword(customerId, newPassword.trim());
        if (result > 0) {
            session.setAttribute("successMessage", "密码修改成功");
        } else {
            session.setAttribute("errorMessage", "密码修改失败，请重试");
        }

        response.sendRedirect(request.getContextPath() + "/customer/profile");
    }
    
    /**
     * 调整信用等级或透支额度
     */
    private void handleUpdateCredit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String customerIdStr = request.getParameter("customerId");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        
        if (customerIdStr == null || action == null) {
            session.setAttribute("errorMessage", "参数不完整");
            response.sendRedirect(request.getContextPath() + "/customer/info");
            return;
        }
        
        try {
            int customerId = Integer.parseInt(customerIdStr);
            int result = 0;
            
            if ("updateCreditLevel".equals(action)) {
                // 调整信用等级
                String creditLevelStr = request.getParameter("creditLevel");
                if (creditLevelStr == null) {
                    session.setAttribute("errorMessage", "信用等级不能为空");
                    response.sendRedirect(request.getContextPath() + "/customer/info");
                    return;
                }
                
                int creditLevel = Integer.parseInt(creditLevelStr);
                result = customerDao.updateCreditLevel(customerId, creditLevel);
                
                if (result > 0) {
                    session.setAttribute("successMessage", "信用等级调整成功！新等级：" + creditLevel + "级");
                } else {
                    session.setAttribute("errorMessage", "信用等级调整失败，请重试");
                }
                
            } else if ("updateMonthlyLimit".equals(action)) {
                // 调整透支额度
                String monthlyLimitStr = request.getParameter("monthlyLimit");
                if (monthlyLimitStr == null) {
                    session.setAttribute("errorMessage", "透支额度不能为空");
                    response.sendRedirect(request.getContextPath() + "/customer/info");
                    return;
                }
                
                BigDecimal monthlyLimit = new BigDecimal(monthlyLimitStr);
                if (monthlyLimit.compareTo(BigDecimal.ZERO) < 0) {
                    session.setAttribute("errorMessage", "透支额度必须大于等于0");
                    response.sendRedirect(request.getContextPath() + "/customer/info");
                    return;
                }
                
                result = customerDao.updateMonthlyLimit(customerId, monthlyLimit);
                
                if (result > 0) {
                    session.setAttribute("successMessage", "透支额度调整成功！新额度：¥" + monthlyLimit);
                } else {
                    session.setAttribute("errorMessage", "透支额度调整失败，请重试");
                }
            } else {
                session.setAttribute("errorMessage", "无效的操作类型");
            }
            
            response.sendRedirect(request.getContextPath() + "/customer/info");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的参数格式");
            response.sendRedirect(request.getContextPath() + "/customer/info");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "操作失败：" + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/customer/info");
        }
    }

    /**
     * 显示缺书登记页面
     */
    private void handleShortageRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        String currentRole = (String) session.getAttribute("currentRole");
        if (!"CUSTOMER".equals(currentRole)) {
            session.setAttribute("warningMessage", "请以客户身份登录后再操作");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "登录已失效，请重新登录后再试");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        // 加载所有图书供选择
        List<model.Book> books = bookDao.findAll();
        request.setAttribute("books", books);
        request.setAttribute("pageTitle", "缺书登记");
        request.setAttribute("pageIcon", "bi-exclamation-triangle");
        request.getRequestDispatcher("/jsp/customer/shortageRegister.jsp").forward(request, response);
    }

    /**
     * 处理缺书登记提交
     */
    private void handleShortageRegisterPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        String currentRole = (String) session.getAttribute("currentRole");
        if (!"CUSTOMER".equals(currentRole)) {
            session.setAttribute("warningMessage", "请以客户身份登录后再操作");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "登录已失效，请重新登录后再试");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        String bookIdStr = request.getParameter("bookId");
        String quantityStr = request.getParameter("quantity");

        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "请选择图书");
            response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
            return;
        }

        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "请输入需要数量");
            response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
            return;
        }

        try {
            int bookId = Integer.parseInt(bookIdStr.trim());
            int quantity = Integer.parseInt(quantityStr.trim());

            if (quantity <= 0) {
                session.setAttribute("errorMessage", "需要数量必须大于0");
                response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
                return;
            }

            // 验证图书是否存在
            model.Book book = bookDao.findById(bookId);
            if (book == null) {
                session.setAttribute("errorMessage", "选择的图书不存在");
                response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
                return;
            }

            // 查找该图书的供应商（自动关联第一个供应商）
            Integer supplierId = null;
            var suppliers = bookSupplierDao.findByBookId(bookId);
            if (!suppliers.isEmpty()) {
                supplierId = suppliers.get(0).getSupplierId();
            }

            // 创建缺书记录
            ShortageRecord record = new ShortageRecord();
            record.setBookId(bookId);
            record.setSupplierId(supplierId);
            record.setCustomerId(customerId); // 关联客户ID
            record.setQuantity(quantity);
            record.setDate(LocalDateTime.now());
            record.setSourceType("CUSTOMER"); // 标记为客户登记
            record.setProcessed(false);

            int id = shortageRecordDao.insert(record);
            if (id > 0) {
                session.setAttribute("successMessage", "缺书登记成功！我们会尽快安排采购，感谢您的反馈。");
                response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
            } else {
                session.setAttribute("errorMessage", "缺书登记失败，请重试");
                response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
            }

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的参数格式");
            response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "缺书登记失败：" + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/customer/shortage/register");
        }
    }
}

