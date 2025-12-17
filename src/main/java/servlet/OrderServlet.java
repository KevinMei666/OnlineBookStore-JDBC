package servlet;

import dao.BookDao;
import dao.CustomerDao;
import dao.OrderItemDao;
import dao.OrdersDao;
import dao.ShipmentDao;
import model.Book;
import model.CartItem;
import model.Customer;
import model.OrderItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderServlet extends HttpServlet {
    
    private BookDao bookDao;
    private OrdersDao ordersDao;
    private CustomerDao customerDao;
    private OrderItemDao orderItemDao;
    private ShipmentDao shipmentDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        bookDao = new BookDao();
        ordersDao = new OrdersDao();
        customerDao = new CustomerDao();
        orderItemDao = new OrderItemDao();
        shipmentDao = new ShipmentDao();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/cart")) {
            // 显示购物车
            handleCart(request, response);
        } else if (pathInfo.equals("/checkout")) {
            // 显示结算页
            handleCheckout(request, response);
        } else if (pathInfo.equals("/list")) {
            // 显示订单列表（将在后续实现）
            handleOrderList(request, response);
        } else if (pathInfo.equals("/detail")) {
            // 显示订单详情（将在后续实现）
            handleOrderDetail(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/add")) {
            // 添加商品到购物车
            handleAddToCart(request, response);
        } else if (pathInfo.equals("/create")) {
            // 创建订单
            handleCreateOrder(request, response);
        } else if (pathInfo.equals("/updateQuantity")) {
            // 更新购物车商品数量
            handleUpdateQuantity(request, response);
        } else if (pathInfo.equals("/remove")) {
            // 从购物车删除商品
            handleRemoveFromCart(request, response);
        } else if (pathInfo.equals("/clear")) {
            // 清空购物车
            handleClearCart(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 显示购物车
     */
    private void handleCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/order/cart.jsp").forward(request, response);
    }
    
    /**
     * 显示结算页
     */
    private void handleCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        
        if (cart == null || cart.isEmpty()) {
            session.setAttribute("errorMessage", "购物车为空，无法结算");
            response.sendRedirect(request.getContextPath() + "/order/cart");
            return;
        }
        
        request.getRequestDispatcher("/jsp/order/checkout.jsp").forward(request, response);
    }
    
    /**
     * 添加商品到购物车
     */
    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String bookIdStr = request.getParameter("bookId");
        HttpSession session = request.getSession();
        
        if (bookIdStr == null || bookIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "书籍ID不能为空");
            response.sendRedirect(request.getContextPath() + "/book");
            return;
        }
        
        try {
            int bookId = Integer.parseInt(bookIdStr);
            Book book = bookDao.findById(bookId);
            
            if (book == null) {
                session.setAttribute("errorMessage", "未找到指定的书籍");
                response.sendRedirect(request.getContextPath() + "/book");
                return;
            }
            
            // 获取购物车
            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart == null) {
                cart = new ArrayList<>();
                session.setAttribute("cart", cart);
            }
            
            // 检查购物车中是否已有该书籍
            boolean found = false;
            for (CartItem item : cart) {
                if (item.getBookId() == bookId) {
                    item.setQuantity(item.getQuantity() + 1);
                    found = true;
                    break;
                }
            }
            
            // 如果购物车中没有，添加新项
            if (!found) {
                CartItem item = new CartItem();
                item.setBookId(bookId);
                item.setTitle(book.getTitle());
                item.setPrice(book.getPrice());
                item.setQuantity(1);
                cart.add(item);
            }
            
            session.setAttribute("successMessage", "《" + book.getTitle() + "》已加入购物车");
            response.sendRedirect(request.getContextPath() + "/order/cart");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的书籍ID");
            response.sendRedirect(request.getContextPath() + "/book");
        }
    }
    
    /**
     * 创建订单
     */
    private void handleCreateOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // 获取购物车
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        
        if (cart == null || cart.isEmpty()) {
            session.setAttribute("errorMessage", "购物车为空，无法创建订单");
            response.sendRedirect(request.getContextPath() + "/order/cart");
            return;
        }
        
        // 获取收货地址
        String shippingAddress = request.getParameter("shippingAddress");
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            session.setAttribute("errorMessage", "收货地址不能为空");
            response.sendRedirect(request.getContextPath() + "/order/checkout");
            return;
        }
        
        // 获取当前客户ID（暂时从session获取，或使用默认值）
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            customerId = 1; // 默认客户ID，实际应从登录信息获取
        }
        
        try {
            // 将购物车转换为OrderItem列表
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cart) {
                OrderItem orderItem = new OrderItem();
                orderItem.setBookId(cartItem.getBookId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(cartItem.getPrice());
                orderItems.add(orderItem);
            }
            
            // 调用DAO创建订单
            int orderId = ordersDao.createOrderWithItems(customerId, orderItems);
            
            if (orderId > 0) {
                // 更新订单的收货地址
                updateOrderShippingAddress(orderId, shippingAddress);
                
                // 清空购物车
                session.removeAttribute("cart");
                session.setAttribute("successMessage", "订单创建成功！订单号：" + orderId);
                response.sendRedirect(request.getContextPath() + "/order/list");
            } else {
                session.setAttribute("errorMessage", "订单创建失败，请重试");
                response.sendRedirect(request.getContextPath() + "/order/checkout");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "订单创建失败：" + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/order/checkout");
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    private void handleUpdateQuantity(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String bookIdStr = request.getParameter("bookId");
        String quantityStr = request.getParameter("quantity");
        HttpSession session = request.getSession();
        
        if (bookIdStr == null || quantityStr == null) {
            session.setAttribute("errorMessage", "参数不完整");
            response.sendRedirect(request.getContextPath() + "/order/cart");
            return;
        }
        
        try {
            int bookId = Integer.parseInt(bookIdStr);
            int quantity = Integer.parseInt(quantityStr);
            
            if (quantity <= 0) {
                session.setAttribute("errorMessage", "数量必须大于0");
                response.sendRedirect(request.getContextPath() + "/order/cart");
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart != null) {
                for (CartItem item : cart) {
                    if (item.getBookId() == bookId) {
                        item.setQuantity(quantity);
                        break;
                    }
                }
            }
            
            response.sendRedirect(request.getContextPath() + "/order/cart");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的参数");
            response.sendRedirect(request.getContextPath() + "/order/cart");
        }
    }
    
    /**
     * 从购物车删除商品
     */
    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String bookIdStr = request.getParameter("bookId");
        HttpSession session = request.getSession();
        
        if (bookIdStr == null) {
            session.setAttribute("errorMessage", "书籍ID不能为空");
            response.sendRedirect(request.getContextPath() + "/order/cart");
            return;
        }
        
        try {
            int bookId = Integer.parseInt(bookIdStr);
            
            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart != null) {
                cart.removeIf(item -> item.getBookId() == bookId);
                session.setAttribute("successMessage", "商品已从购物车删除");
            }
            
            response.sendRedirect(request.getContextPath() + "/order/cart");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的书籍ID");
            response.sendRedirect(request.getContextPath() + "/order/cart");
        }
    }
    
    /**
     * 清空购物车
     */
    private void handleClearCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        session.removeAttribute("cart");
        session.setAttribute("successMessage", "购物车已清空");
        response.sendRedirect(request.getContextPath() + "/order/cart");
    }
    
    /**
     * 显示订单列表
     */
    private void handleOrderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // 获取当前客户ID（暂时从session获取，或使用默认值）
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            customerId = 1; // 默认客户ID，实际应从登录信息获取
        }
        
        // 查询订单列表
        List<model.Orders> orders = ordersDao.findByCustomerId(customerId);
        
        request.setAttribute("orders", orders);
        request.getRequestDispatcher("/jsp/order/orderList.jsp").forward(request, response);
    }
    
    /**
     * 显示订单详情
     */
    private void handleOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String orderIdStr = request.getParameter("orderId");
        HttpSession session = request.getSession();
        
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "订单ID不能为空");
            response.sendRedirect(request.getContextPath() + "/order/list");
            return;
        }
        
        try {
            int orderId = Integer.parseInt(orderIdStr);
            
            // 查询订单信息
            model.Orders order = ordersDao.findById(orderId);
            if (order == null) {
                session.setAttribute("errorMessage", "未找到指定的订单");
                response.sendRedirect(request.getContextPath() + "/order/list");
                return;
            }
            
            // 查询订单明细
            List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
            
            // 查询发货记录
            List<model.Shipment> shipments = shipmentDao.findByOrderId(orderId);
            
            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderItems);
            request.setAttribute("shipments", shipments);
            
            request.getRequestDispatcher("/jsp/order/orderDetail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的订单ID");
            response.sendRedirect(request.getContextPath() + "/order/list");
        }
    }
    
    /**
     * 更新订单收货地址
     */
    private void updateOrderShippingAddress(int orderId, String shippingAddress) {
        String sql = "UPDATE Orders SET ShippingAddress = ? WHERE OrderID = ?";
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        try {
            conn = util.DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, shippingAddress);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        } finally {
            util.DBUtil.closeQuietly(ps);
            util.DBUtil.closeQuietly(conn);
        }
    }
}

