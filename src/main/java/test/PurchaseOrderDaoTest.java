package test;

import dao.PurchaseOrderDao;
import model.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseOrderDaoTest {

    public static void main(String[] args) throws Exception {
        PurchaseOrderDao dao = new PurchaseOrderDao();

        // 假设数据库中已存在 SupplierID=1 的供应商
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(1);
        order.setShortageId(null); // 普通采购单，这里不关联缺书记录
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("Created");
        order.setTotalAmount(new BigDecimal("123.45"));

        System.out.println("=== 测试：插入采购单（PurchaseOrderDao.insert） ===");
        int poId = dao.insert(order);
        System.out.println("insert 返回的 PurchaseOrderID = " + poId);

        System.out.println();
        System.out.println("=== 测试：通过 ID 查询采购单（PurchaseOrderDao.findById） ===");
        PurchaseOrder found = dao.findById(poId);
        if (found != null) {
            System.out.println("PurchaseOrderID = " + found.getPoId());
            System.out.println("SupplierID      = " + found.getSupplierId());
            System.out.println("TotalAmount     = " + found.getTotalAmount());
            System.out.println("Status          = " + found.getStatus());
            System.out.println("CreatedAt       = " + found.getCreateDate());
        } else {
            System.out.println("未找到 PurchaseOrderID = " + poId + " 的记录");
        }

        System.out.println("=== 测试结束 ===");
    }
}


