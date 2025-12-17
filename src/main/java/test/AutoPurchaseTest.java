package test;

import dao.BookDao;
import dao.OrdersDao;
import dao.PurchaseItemDao;
import dao.PurchaseOrderDao;
import dao.ShortageRecordDao;
import model.Book;
import model.OrderItem;
import model.PurchaseItem;
import model.PurchaseOrder;
import model.ShortageRecord;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoPurchaseTest {

    public static void main(String[] args) throws Exception {
        int bookId = 1;      // 假定存在的图书
        int customerId = 1;  // 假定存在的客户

        BookDao bookDao = new BookDao();
        ShortageRecordDao shortageDao = new ShortageRecordDao();
        PurchaseOrderDao poDao = new PurchaseOrderDao();
        PurchaseItemDao piDao = new PurchaseItemDao();
        OrdersDao ordersDao = new OrdersDao();

        // 1. 人为将某 Book.StockQuantity 设置为 1
        System.out.println("=== Step1: 将 Book.StockQuantity 设置为 1 ===");
        forceSetBookStock(bookId, 1);
        Book bookBefore = bookDao.findById(bookId);
        System.out.println("BookID=" + bookId + " 当前库存=" +
                (bookBefore != null ? bookBefore.getStockQuantity() : null));
        System.out.println();

        // 记录下单前的缺书记录数和采购单数
        List<ShortageRecord> shortageBefore = shortageDao.findUnprocessed();
        int shortageCountBefore = shortageBefore.size();
        List<PurchaseOrder> poBefore = poDao.findAll();
        int poCountBefore = poBefore.size();

        // 2. 创建订单，购买数量为 5
        System.out.println("=== Step2: 创建订单，购买数量为 5（触发自动采购） ===");
        OrderItem orderItem = new OrderItem();
        orderItem.setBookId(bookId);
        orderItem.setQuantity(5);
        orderItem.setUnitPrice(bookBefore != null && bookBefore.getPrice() != null
                ? bookBefore.getPrice()
                : new BigDecimal("50.00"));

        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        int orderId = ordersDao.createOrderWithItems(customerId, items);
        System.out.println("createOrderWithItems 返回的 OrderID = " + orderId);
        System.out.println();

        // 3. 验证：缺书记录、采购单、采购明细、库存
        System.out.println("=== Step3: 验证缺书记录与采购单 ===");

        List<ShortageRecord> shortageAfter = shortageDao.findUnprocessed();
        int shortageCountAfter = shortageAfter.size();
        System.out.println("ShortageRecord 数量：下单前 = " + shortageCountBefore
                + "，下单后 = " + shortageCountAfter
                + "，新增 = " + (shortageCountAfter - shortageCountBefore));

        List<PurchaseOrder> poAfter = poDao.findAll();
        int poCountAfter = poAfter.size();
        System.out.println("PurchaseOrder 数量：下单前 = " + poCountBefore
                + "，下单后 = " + poCountAfter
                + "，新增 = " + (poCountAfter - poCountBefore));

        // 取最新生成的 PurchaseOrder（如果有新增）
        PurchaseOrder latestPo = null;
        if (!poAfter.isEmpty()) {
            latestPo = poAfter.stream()
                    .max(Comparator.comparing(PurchaseOrder::getPoId))
                    .orElse(null);
        }

        if (latestPo != null) {
            System.out.println("最新采购单：POID=" + latestPo.getPoId()
                    + "，SupplierID=" + latestPo.getSupplierId()
                    + "，TotalAmount=" + latestPo.getTotalAmount()
                    + "，Status=" + latestPo.getStatus());

            List<PurchaseItem> purchaseItems = piDao.findByPurchaseOrderId(latestPo.getPoId());
            System.out.println("该采购单的 PurchaseItem 明细：");
            for (PurchaseItem pi : purchaseItems) {
                System.out.println("  POID=" + pi.getPurchaseOrderId()
                        + " | BookID=" + pi.getBookId()
                        + " | Quantity=" + pi.getQuantity()
                        + " | UnitPrice=" + pi.getUnitPrice());
            }
        } else {
            System.out.println("未找到采购单。");
        }

        System.out.println();
        System.out.println("=== Step4: 验证 Book.StockQuantity 未被扣为负数 ===");
        Book bookAfter = bookDao.findById(bookId);
        Integer stockAfter = bookAfter != null ? bookAfter.getStockQuantity() : null;
        System.out.println("BookID=" + bookId + " 订单创建后库存=" + stockAfter);

        System.out.println();
        System.out.println("=== AutoPurchaseTest 结束 ===");
    }

    private static void forceSetBookStock(int bookId, int stockQuantity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement("UPDATE Book SET StockQuantity = ? WHERE BookID = ?");
            ps.setInt(1, stockQuantity);
            ps.setInt(2, bookId);
            int rows = ps.executeUpdate();
            System.out.println("强制更新 Book.StockQuantity 影响行数 = " + rows);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }
}


