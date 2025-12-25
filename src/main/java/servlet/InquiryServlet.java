package servlet;

import dao.InquiryDao;
import model.Inquiry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/inquiry/*")
public class InquiryServlet extends HttpServlet {
    
    private InquiryDao inquiryDao = new InquiryDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            handleList(request, response);
        } else if (pathInfo.equals("/create")) {
            handleCreateForm(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            handleCreate(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示询价列表（客户查看自己的询价记录）
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        String currentRole = (String) session.getAttribute("currentRole");
        
        if (customerId == null && !"ADMIN".equals(currentRole)) {
            session.setAttribute("errorMessage", "请先登录");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        if ("ADMIN".equals(currentRole)) {
            // 管理员查看所有询价记录
            request.setAttribute("inquiries", inquiryDao.findAll());
            request.getRequestDispatcher("/jsp/admin/inquiryList.jsp").forward(request, response);
        } else {
            // 客户查看自己的询价记录
            request.setAttribute("inquiries", inquiryDao.findByCustomerId(customerId));
            request.getRequestDispatcher("/jsp/customer/inquiryList.jsp").forward(request, response);
        }
    }
    
    /**
     * 显示询价表单
     */
    private void handleCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 从请求参数中获取搜索条件，用于预填充表单
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String publisher = request.getParameter("publisher");
        
        request.setAttribute("prefillTitle", title);
        request.setAttribute("prefillAuthor", author);
        request.setAttribute("prefillPublisher", publisher);
        
        request.getRequestDispatcher("/jsp/customer/inquiryCreate.jsp").forward(request, response);
    }
    
    /**
     * 创建询价记录
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        
        if (customerId == null) {
            session.setAttribute("errorMessage", "请先登录");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        String bookTitle = request.getParameter("bookTitle");
        String author = request.getParameter("author");
        String publisher = request.getParameter("publisher");
        String quantityStr = request.getParameter("quantity");
        
        // 验证必填字段
        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            session.setAttribute("errorMessage", "书名不能为空");
            response.sendRedirect(request.getContextPath() + "/inquiry/create");
            return;
        }
        
        int quantity = 1;
        try {
            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    quantity = 1;
                }
            }
        } catch (NumberFormatException e) {
            quantity = 1;
        }
        
        Inquiry inquiry = new Inquiry();
        inquiry.setCustomerId(customerId);
        inquiry.setBookTitle(bookTitle.trim());
        inquiry.setAuthor(author != null && !author.trim().isEmpty() ? author.trim() : null);
        inquiry.setPublisher(publisher != null && !publisher.trim().isEmpty() ? publisher.trim() : null);
        inquiry.setQuantity(quantity);
        inquiry.setInquiryDate(LocalDateTime.now());
        inquiry.setStatus("PENDING");
        
        int result = inquiryDao.insert(inquiry);
        if (result > 0) {
            session.setAttribute("successMessage", "询价请求已提交，我们会尽快处理并回复您！");
            response.sendRedirect(request.getContextPath() + "/inquiry");
        } else {
            session.setAttribute("errorMessage", "提交询价请求失败，请重试");
            response.sendRedirect(request.getContextPath() + "/inquiry/create");
        }
    }
}

