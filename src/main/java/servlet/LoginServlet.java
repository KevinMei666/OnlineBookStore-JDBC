package servlet;

import dao.AdminUserDao;
import dao.CustomerDao;
import model.AdminUser;
import model.Customer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private CustomerDao customerDao;
    private AdminUserDao adminUserDao;

    @Override
    public void init() throws ServletException {
        super.init();
        customerDao = new CustomerDao();
        adminUserDao = new AdminUserDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "/login".equals(pathInfo)) {
            // 显示登录页面
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "/login".equals(pathInfo)) {
            handleLogin(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 处理登录
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession();
        String role = request.getParameter("role"); // CUSTOMER / ADMIN
        String username = request.getParameter("username"); // 客户使用 Email，管理员使用 Username
        String password = request.getParameter("password");

        if (role == null || role.trim().isEmpty()) {
            role = "CUSTOMER";
        } else {
            role = role.trim().toUpperCase();
        }

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "用户名和密码不能为空");
            request.setAttribute("role", role);
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
            return;
        }

        if ("ADMIN".equals(role)) {
            // 管理员登录
            AdminUser admin = adminUserDao.findByUsername(username.trim());
            if (admin == null || admin.getPasswordHash() == null ||
                    !admin.getPasswordHash().equals(password)) {
                request.setAttribute("errorMessage", "管理员用户名或密码错误");
                request.setAttribute("role", role);
                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
                return;
            }

            session.setAttribute("currentUser", admin.getName() != null ? admin.getName() : admin.getUsername());
            session.setAttribute("currentRole", "ADMIN");
            session.setAttribute("currentAdminId", admin.getAdminId());
            session.setAttribute("currentAdmin", admin);

            session.setAttribute("successMessage", "管理员登录成功");
            // 管理员登录后同样先进入系统首页
            response.sendRedirect(request.getContextPath() + "/index.jsp");

        } else {
            // 客户登录（使用 Email + PasswordHash 字段，当前按明文比较）
            Customer customer = customerDao.findByEmail(username.trim());
            if (customer == null || customer.getPasswordHash() == null ||
                    !customer.getPasswordHash().equals(password)) {
                request.setAttribute("errorMessage", "邮箱或密码错误");
                request.setAttribute("role", role);
                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
                return;
            }

            session.setAttribute("currentUser", customer.getName() != null ? customer.getName() : customer.getEmail());
            session.setAttribute("currentRole", "CUSTOMER");
            session.setAttribute("currentCustomerId", customer.getCustomerId());
            session.setAttribute("currentCustomer", customer);

            session.setAttribute("successMessage", "登录成功");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }

    /**
     * 处理退出登录
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
    }
}


