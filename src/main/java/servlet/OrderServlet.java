package servlet;

import dao.BookDao;
import dao.CustomerDao;
import dao.OrderItemDao;
import dao.OrdersDao;
import dao.ShipmentDao;
import dao.BookSupplierDao;
import dao.ShortageRecordDao;
import model.Book;
import model.CartItem;
import model.OrderItem;
import model.ShortageRecord;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class OrderServlet extends HttpServlet {
    
    private BookDao bookDao;
    private OrdersDao ordersDao;
    private CustomerDao customerDao;
    private OrderItemDao orderItemDao;
    private ShipmentDao shipmentDao;
    private BookSupplierDao bookSupplierDao;
    private ShortageRecordDao shortageRecordDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        bookDao = new BookDao();
        ordersDao = new OrdersDao();
        customerDao = new CustomerDao();
        orderItemDao = new OrderItemDao();
        shipmentDao = new ShipmentDao();
        bookSupplierDao = new BookSupplierDao();
        shortageRecordDao = new ShortageRecordDao();
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
        } else if (pathInfo.equals("/payPage")) {
            // 显示支付确认页面
            handlePayPage(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
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
        } else if (pathInfo.equals("/confirm")) {
            // 客户确认收货
            handleConfirmOrder(request, response);
        } else if (pathInfo.equals("/pay")) {
            // 执行支付
            handlePayOrder(request, response);
        } else if (pathInfo.equals("/payPage")) {
            // 显示支付确认页面
            handlePayPage(request, response);
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

        // 1) 下单前库存校验：检查库存并记录缺货信息，但不阻止下单
        // 缺书记录将在DAO中通过AutoPurchaseService自动生成
        List<String> shortageMessages = new ArrayList<>();
        for (CartItem cartItem : cart) {
            Book book = bookDao.findById(cartItem.getBookId());
            if (book == null) {
                session.setAttribute("errorMessage", "图书不存在（BookID=" + cartItem.getBookId() + "），请刷新购物车");
                response.sendRedirect(request.getContextPath() + "/order/cart");
                return;
            }
            int stock = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
            int required = cartItem.getQuantity();
            if (required > stock) {
                shortageMessages.add(book.getTitle() + "（库存 " + stock + "，需要 " + required + "）");
            }
        }
        
        // 获取收货地址
        String shippingAddress = request.getParameter("shippingAddress");
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            session.setAttribute("errorMessage", "收货地址不能为空");
            response.sendRedirect(request.getContextPath() + "/order/checkout");
            return;
        }
        
        // 获取当前客户ID（必须登录为客户）
        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("warningMessage", "请先以客户身份登录后再创建订单");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
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
            
            // 调用DAO创建订单（传入是否有库存不足的标志）
            boolean hasShortage = !shortageMessages.isEmpty();
            int orderId = ordersDao.createOrderWithItems(customerId, orderItems, hasShortage);
            
            if (orderId > 0) {
                // 更新订单的收货地址
                updateOrderShippingAddress(orderId, shippingAddress);
                
                // 清空购物车
                session.removeAttribute("cart");
                
                // 构建成功消息
                StringBuilder message = new StringBuilder("订单创建成功！订单号：" + orderId);
                if (hasShortage) {
                    message.append("。注意：部分商品库存不足（");
                    message.append(String.join("、", shortageMessages));
                    message.append("），已生成缺书记录，订单状态为未付款，待库存补充后可继续支付。");
                }
                session.setAttribute("successMessage", message.toString());
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
        String currentRole = (String) session.getAttribute("currentRole");

        List<model.Orders> orders;
        if ("ADMIN".equals(currentRole)) {
            // 管理员查看全部订单
            orders = ordersDao.findAll();
            // 构建客户名称映射，便于在列表中展示下单客户
            java.util.Map<Integer, String> customerNameMap = new java.util.HashMap<>();
            for (model.Orders o : orders) {
                if (o.getCustomerId() != null && !customerNameMap.containsKey(o.getCustomerId())) {
                    model.Customer c = customerDao.findById(o.getCustomerId());
                    if (c != null) {
                        String name = c.getName() != null && !c.getName().isEmpty()
                                ? c.getName()
                                : (c.getEmail() != null ? c.getEmail() : ("客户#" + c.getCustomerId()));
                        customerNameMap.put(o.getCustomerId(), name);
                    }
                }
            }
            request.setAttribute("customerNameMap", customerNameMap);
        } else {
            // 客户：只能查看自己的订单，必须登录
            Integer customerId = (Integer) session.getAttribute("currentCustomerId");
            if (customerId == null) {
                session.setAttribute("warningMessage", "请先登录后查看订单列表");
                response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
                return;
            }
            orders = ordersDao.findByCustomerId(customerId);
        }
        
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

            // 权限控制：非管理员只能查看自己的订单
            String currentRole = (String) session.getAttribute("currentRole");
            if (!"ADMIN".equals(currentRole)) {
                Integer currentCustomerId = (Integer) session.getAttribute("currentCustomerId");
                if (currentCustomerId == null || order.getCustomerId() == null
                        || !order.getCustomerId().equals(currentCustomerId)) {
                    session.setAttribute("errorMessage", "无权查看该订单");
                    response.sendRedirect(request.getContextPath() + "/order/list");
                    return;
                }
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
     * 显示支付确认页面
     */
    private void handlePayPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String orderIdStr = request.getParameter("orderId");

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("errorMessage", "请先登录");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "订单ID不能为空");
            response.sendRedirect(request.getContextPath() + "/order/list");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            model.Orders order = ordersDao.findById(orderId);

            if (order == null || !customerId.equals(order.getCustomerId())) {
                session.setAttribute("errorMessage", "订单不存在或无权访问");
                response.sendRedirect(request.getContextPath() + "/order/list");
                return;
            }

            if (!"CREATED".equalsIgnoreCase(order.getStatus())) {
                session.setAttribute("errorMessage", "该订单不是待支付状态");
                response.sendRedirect(request.getContextPath() + "/order/list");
                return;
            }

            // 查询订单明细
            List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
            // 查询客户信息
            model.Customer customer = customerDao.findById(customerId);

            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderItems);
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/jsp/order/payOrder.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的订单ID");
            response.sendRedirect(request.getContextPath() + "/order/list");
        }
    }

    /**
     * 执行支付订单
     */
    private void handlePayOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String orderIdStr = request.getParameter("orderId");

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("errorMessage", "请先登录");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "订单ID不能为空");
            response.sendRedirect(request.getContextPath() + "/order/list");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            String result = ordersDao.payOrder(orderId, customerId);
            if (result == null) {
                session.setAttribute("successMessage", "支付成功");
            } else {
                session.setAttribute("errorMessage", result);
            }
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的订单ID");
        }
        response.sendRedirect(request.getContextPath() + "/order/list");
    }

    /**
     * 客户确认收货
     */
    private void handleConfirmOrder(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String orderIdStr = request.getParameter("orderId");

        Integer customerId = (Integer) session.getAttribute("currentCustomerId");
        if (customerId == null) {
            session.setAttribute("errorMessage", "请先登录");
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
            return;
        }

        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "订单ID不能为空");
            response.sendRedirect(request.getContextPath() + "/order/list");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            int updated = ordersDao.confirmOrder(orderId, customerId);
            if (updated > 0) {
                session.setAttribute("successMessage", "订单已确认收货");
            } else {
                session.setAttribute("errorMessage", "确认收货失败，请检查订单状态");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "无效的订单ID");
        }
        response.sendRedirect(request.getContextPath() + "/order/list");
    }

    /**
     * 创建缺书记录（下单时库存不足）
     */
    private int createShortageRecord(int bookId, int shortageQty, String sourceType) {
        Integer supplierId = null;
        var suppliers = bookSupplierDao.findByBookId(bookId);
        if (!suppliers.isEmpty()) {
            supplierId = suppliers.get(0).getSupplierId();
        }

        ShortageRecord record = new ShortageRecord();
        record.setBookId(bookId);
        record.setSupplierId(supplierId);
        record.setCustomerId(null);
        record.setQuantity(shortageQty);
        record.setDate(LocalDateTime.now());
        record.setSourceType(sourceType);
        record.setProcessed(false);
        return shortageRecordDao.insert(record);
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

