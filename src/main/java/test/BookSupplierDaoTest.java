package test;

import dao.BookSupplierDao;
import model.BookSupplier;

import java.math.BigDecimal;
import java.util.List;

public class BookSupplierDaoTest {

    public static void main(String[] args) throws Exception {
        // 假定已按初始化 SQL 插入了 BookID=1 与 SupplierID=1 的测试数据
        int bookId = 1;
        int supplierId = 1;

        BigDecimal supplyPrice = new BigDecimal("45.50");

        BookSupplierDao dao = new BookSupplierDao();

        System.out.println("=== 测试：绑定图书与供应商（BookSupplierDao.bindSupplierToBook） ===");
        int rows = dao.bindSupplierToBook(bookId, supplierId, supplyPrice);
        System.out.println("bindSupplierToBook 影响行数：" + rows);

        System.out.println();
        System.out.println("=== 测试：按 BookID 查询供应商列表（BookSupplierDao.findByBookId） ===");
        List<BookSupplier> list = dao.findByBookId(bookId);
        for (BookSupplier bs : list) {
            System.out.println("BookID=" + bs.getBookId()
                    + " | SupplierID=" + bs.getSupplierId()
                    + " | SupplyPrice=" + bs.getSupplyPrice());
        }
        System.out.println("记录总数：" + list.size());
        System.out.println("=== 测试结束 ===");
    }
}


