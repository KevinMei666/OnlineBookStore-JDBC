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

@WebServlet("/admin/inquiry/*")
public class AdminInquiryServlet extends HttpServlet {
    
    private InquiryDao inquiryDao = new InquiryDao();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        String currentRole = (String) session.getAttribute("currentRole");
        
        if (!"ADMIN".equals(currentRole)) {
            session.setAttribute("errorMessage", "权限不足");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        
        if (pathInfo != null && pathInfo.equals("/respond")) {
            handleRespond(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 处理管理员回复询价
     */
    private void handleRespond(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        String inquiryIdStr = request.getParameter("inquiryId");
        String status = request.getParameter("status");
        String adminResponse = request.getParameter("adminResponse");
        
        if (inquiryIdStr == null || inquiryIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "询价ID不能为空");
            response.sendRedirect(request.getContextPath() + "/inquiry");
            return;
        }
        
        try {
            int inquiryId = Integer.parseInt(inquiryIdStr);
            
            if (status == null || status.trim().isEmpty()) {
                status = "PENDING";
            }
            
            int result = inquiryDao.updateResponse(inquiryId, status, 
                    adminResponse != null ? adminResponse.trim() : null);
            
            if (result > 0) {
                session.setAttribute("successMessage", "回复已保存");
            } else {
                session.setAttribute("errorMessage", "保存回复失败，请重试");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的询价ID");
        }
        
        response.sendRedirect(request.getContextPath() + "/inquiry");
    }
}

