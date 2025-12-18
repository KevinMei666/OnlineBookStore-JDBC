package servlet;

import dao.CustomerDao;
import model.Customer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

public class RegisterServlet extends HttpServlet {

    private CustomerDao customerDao;

    @Override
    public void init() throws ServletException {
        super.init();
        customerDao = new CustomerDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 直接展示注册页面
        request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String address = request.getParameter("address");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // 基本校验
        if (email == null || email.trim().isEmpty()
                || name == null || name.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            request.setAttribute("errorMessage", "邮箱、姓名和密码为必填项");
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            return;
        }

        email = email.trim();
        name = name.trim();

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "两次输入的密码不一致");
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("errorMessage", "密码长度至少为6位");
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            return;
        }

        // 检查邮箱是否已被注册
        model.Customer existing = customerDao.findByEmail(email);
        if (existing != null) {
            request.setAttribute("errorMessage", "该邮箱已注册，请直接登录");
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            return;
        }

        // 创建新客户：当前沿用 PasswordHash 字段存明文密码（与 LoginServlet 一致）
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setPasswordHash(password);
        customer.setName(name);
        customer.setAddress(address);
        customer.setBalance(BigDecimal.ZERO);
        customer.setCreditLevel(1); // 默认信用等级1级
        customer.setMonthlyLimit(BigDecimal.ZERO); // 默认无透支额度

        int rows = customerDao.insert(customer);
        if (rows <= 0) {
            request.setAttribute("errorMessage", "注册失败，请稍后重试");
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            return;
        }

        // 注册成功：跳转到登录页，并给出成功提示
        session.setAttribute("successMessage", "注册成功，请使用邮箱和密码登录");
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }
}


