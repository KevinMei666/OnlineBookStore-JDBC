package servlet;

import dao.CustomerDao;
import model.Customer;
import service.CustomerQueryService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class CustomerServlet extends HttpServlet {
    
    private CustomerDao customerDao;
    private CustomerQueryService customerQueryService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        customerDao = new CustomerDao();
        customerQueryService = new CustomerQueryService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/info")) {
            // 显示客户信息
            handleCustomerInfo(request, response);
        } else if (pathInfo.equals("/orders")) {
            // 显示订单历史
            handleOrderHistory(request, response);
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
        } else if (pathInfo.equals("/updateCredit")) {
            // 调整信用等级或透支额度
            handleUpdateCredit(request, response);
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

        request.setAttribute("customer", customer);
        request.getRequestDispatcher("/jsp/customer/customerInfo.jsp").forward(request, response);
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
}

