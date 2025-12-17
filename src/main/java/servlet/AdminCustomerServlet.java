package servlet;

import dao.CustomerDao;
import model.Customer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 管理员端 - 客户账户与信用等级管理
 */
public class AdminCustomerServlet extends HttpServlet {

    private final CustomerDao customerDao = new CustomerDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/list".equals(path)) {
            handleList(request, response);
        } else if ("/edit".equals(path)) {
            handleEdit(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerIdParam = request.getParameter("customerId");
        String nameParam = request.getParameter("name");

        List<Customer> customers = customerDao.queryForAdmin(customerIdParam, nameParam);

        request.setAttribute("customers", customers);
        request.setAttribute("searchCustomerId", customerIdParam);
        request.setAttribute("searchName", nameParam);

        request.getRequestDispatcher("/jsp/admin/customerList.jsp").forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/customer/list");
            return;
        }

        int customerId;
        try {
            customerId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/customer/list");
            return;
        }

        Customer customer = customerDao.findById(customerId);
        if (customer == null) {
            request.getSession().setAttribute("errorMessage", "未找到指定客户（ID=" + customerId + "）");
            response.sendRedirect(request.getContextPath() + "/admin/customer/list");
            return;
        }

        request.setAttribute("customer", customer);
        request.getRequestDispatcher("/jsp/admin/customerEdit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/update".equals(path)) {
            handleUpdate(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idParam = request.getParameter("customerId");
        String balanceParam = request.getParameter("balance");
        String creditLevelParam = request.getParameter("creditLevel");
        String monthlyLimitParam = request.getParameter("monthlyLimit");

        int customerId;
        try {
            customerId = Integer.parseInt(idParam.trim());
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "客户ID无效");
            response.sendRedirect(request.getContextPath() + "/admin/customer/list");
            return;
        }

        try {
            java.math.BigDecimal balance = new java.math.BigDecimal(balanceParam.trim());
            java.math.BigDecimal monthlyLimit = new java.math.BigDecimal(
                    (monthlyLimitParam == null || monthlyLimitParam.trim().isEmpty())
                            ? "0" : monthlyLimitParam.trim());
            Integer creditLevel = null;
            if (creditLevelParam != null && !creditLevelParam.trim().isEmpty()) {
                creditLevel = Integer.parseInt(creditLevelParam.trim());
            }

            int updated = customerDao.updateAccountAndCredit(customerId, balance, creditLevel, monthlyLimit);
            if (updated > 0) {
                request.getSession().setAttribute("successMessage", "客户账户信息已更新");
            } else {
                request.getSession().setAttribute("errorMessage", "更新客户账户信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "更新客户账户信息时发生错误：" + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/customer/list");
    }
}


