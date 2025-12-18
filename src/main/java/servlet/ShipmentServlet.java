package servlet;

import dao.OrderItemDao;
import dao.OrdersDao;
import dao.ShipmentDao;
import model.OrderItem;
import model.Orders;
import model.Shipment;
import service.ShipmentService;
import util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipmentServlet extends HttpServlet {
    
    private OrderItemDao orderItemDao;
    private ShipmentDao shipmentDao;
    private OrdersDao ordersDao;
    private ShipmentService shipmentService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        orderItemDao = new OrderItemDao();
        shipmentDao = new ShipmentDao();
        ordersDao = new OrdersDao();
        shipmentService = new ShipmentService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            // 显示待发货列表
            handleShipmentList(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo.equals("/ship")) {
            // 执行发货
            handleShip(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示待发货列表
     */
    private void handleShipmentList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 查询状态为 PAID 或 SHIPPED 的订单及其订单项
        List<Map<String, Object>> pendingShipments = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // 查询状态为 PAID 或 SHIPPED 的订单项及其已发货数量
            String sql = "SELECT o.OrderID, o.Status AS OrderStatus, oi.BookID, oi.Quantity AS OrderQuantity, " +
                    "COALESCE(SUM(s.Quantity), 0) AS ShippedQuantity " +
                    "FROM Orders o " +
                    "INNER JOIN OrderItem oi ON o.OrderID = oi.OrderID " +
                    "LEFT JOIN Shipment s ON oi.OrderID = s.OrderID AND oi.BookID = s.BookID " +
                    "WHERE o.Status IN ('PAID', 'SHIPPED', 'PARTIAL') " +
                    "GROUP BY o.OrderID, o.Status, oi.BookID, oi.Quantity " +
                    "ORDER BY o.OrderID DESC, oi.BookID";
            
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                int orderId = rs.getInt("OrderID");
                String orderStatus = rs.getString("OrderStatus");
                int bookId = rs.getInt("BookID");
                int orderQuantity = rs.getInt("OrderQuantity");
                int shippedQuantity = rs.getInt("ShippedQuantity");
                int pendingQuantity = orderQuantity - shippedQuantity;
                
                item.put("orderId", orderId);
                item.put("orderStatus", orderStatus);
                item.put("bookId", bookId);
                item.put("orderQuantity", orderQuantity);
                item.put("shippedQuantity", shippedQuantity);
                item.put("pendingQuantity", pendingQuantity);
                
                pendingShipments.add(item);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        
        request.setAttribute("pendingShipments", pendingShipments);
        request.getRequestDispatcher("/jsp/shipment/shipmentList.jsp").forward(request, response);
    }
    
    /**
     * 执行发货
     */
    private void handleShip(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String orderIdStr = request.getParameter("orderId");
        String bookIdStr = request.getParameter("bookId");
        String shipQuantityStr = request.getParameter("shipQuantity");
        String carrier = request.getParameter("carrier");
        String trackingNo = request.getParameter("trackingNo");
        
        HttpSession session = request.getSession();
        
        if (orderIdStr == null || bookIdStr == null || shipQuantityStr == null) {
            session.setAttribute("errorMessage", "参数不完整");
            response.sendRedirect(request.getContextPath() + "/shipment/list");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(orderIdStr);
            int bookId = Integer.parseInt(bookIdStr);
            int shipQuantity = Integer.parseInt(shipQuantityStr);
            
            if (shipQuantity <= 0) {
                session.setAttribute("errorMessage", "发货数量必须大于0");
                response.sendRedirect(request.getContextPath() + "/shipment/list");
                return;
            }
            
            // 调用 ShipmentService 发货（这会处理扣款逻辑）
            // 注意：ShipmentService.shipOrderItem 会使用默认的承运商和运单号
            // 如果需要自定义承运商和运单号，需要修改 ShipmentService 或创建新的方法
            shipmentService.shipOrderItem(orderId, bookId, shipQuantity);
            
            String successMsg = "发货成功！订单ID：" + orderId + "，图书ID：" + bookId + "，发货数量：" + shipQuantity;
            if (carrier != null && !carrier.trim().isEmpty()) {
                successMsg += "，承运商：" + carrier;
            }
            if (trackingNo != null && !trackingNo.trim().isEmpty()) {
                successMsg += "，运单号：" + trackingNo;
            }
            
            session.setAttribute("successMessage", successMsg);
            response.sendRedirect(request.getContextPath() + "/shipment/list");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的参数格式");
            response.sendRedirect(request.getContextPath() + "/shipment/list");
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("余额不足") || errorMsg.contains("Insufficient balance")) {
                    session.setAttribute("errorMessage", "发货失败：客户余额不足");
                } else if (errorMsg.contains("透支额度") || errorMsg.contains("Monthly limit")) {
                    session.setAttribute("errorMessage", "发货失败：客户透支额度超限");
                } else {
                    session.setAttribute("errorMessage", "发货失败：" + errorMsg);
                }
            } else {
                session.setAttribute("errorMessage", "发货失败，请重试");
            }
            response.sendRedirect(request.getContextPath() + "/shipment/list");
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("余额不足") || errorMsg.contains("Insufficient"))) {
                session.setAttribute("errorMessage", "发货失败：客户余额不足或透支额度超限");
            } else {
                session.setAttribute("errorMessage", "发货失败：" + (errorMsg != null ? errorMsg : "未知错误"));
            }
            response.sendRedirect(request.getContextPath() + "/shipment/list");
        }
    }
}

