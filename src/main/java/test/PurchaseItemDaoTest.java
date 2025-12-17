package test;

import dao.PurchaseItemDao;
import model.PurchaseItem;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseItemDaoTest {

    public static void main(String[] args) throws Exception {
        PurchaseItemDao dao = new PurchaseItemDao();

        // 假设数据库中已存在的采购单号和图书编号
        int purchaseOrderId = 1; // 对应 PurchaseOrder.POID
        int bookId = 1;          // 对应 Book.BookID

        PurchaseItem item = new PurchaseItem();
        item.setPurchaseOrderId(purchaseOrderId);
        item.setBookId(bookId);
        item.setQuantity(5);
        item.setUnitPrice(new BigDecimal("30.00"));

        System.out.println("=== 测试：插入采购明细（PurchaseItemDao.insert） ===");
        int rows = dao.insert(item);
        System.out.println("insert 影响行数 = " + rows);

        System.out.println();
        System.out.println("=== 测试：按采购单号查询明细（PurchaseItemDao.findByPurchaseOrderId） ===");
        List<PurchaseItem> items = dao.findByPurchaseOrderId(purchaseOrderId);
        for (PurchaseItem pi : items) {
            System.out.println("PurchaseOrderID = " + pi.getPurchaseOrderId()
                    + " | BookID = " + pi.getBookId()
                    + " | Quantity = " + pi.getQuantity()
                    + " | UnitPrice = " + pi.getUnitPrice());
        }
        System.out.println("明细总数 = " + items.size());
        System.out.println("=== 测试结束 ===");
    }
}


