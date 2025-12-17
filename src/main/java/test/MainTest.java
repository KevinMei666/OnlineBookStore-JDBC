package test;

import dao.BookDao;
import dao.CustomerDao;
import dao.OrdersDao;
import model.Book;
import model.Customer;
import model.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainTest {

    public static void main(String[] args) throws Exception {
        // ==============================
        // 1. 查询所有书籍（BookDao.findAll）
        // ==============================
        System.out.println("=== 测试：查询所有书籍 ===");
        BookDao bookDao = new BookDao();
        List<Book> books = bookDao.findAll();
        for (Book book : books) {
            System.out.println("BookID=" + book.getBookId()
                    + ", Title=" + book.getTitle()
                    + ", Publisher=" + book.getPublisher()
                    + ", Price=" + book.getPrice()
                    + ", Stock=" + book.getStockQuantity());
        }
        System.out.println("书籍总数：" + books.size());
        System.out.println();

        // ==============================
        // 2. 新增一个 Customer（CustomerDao.insert）
        // ==============================
        System.out.println("=== 测试：新增 Customer ===");
        CustomerDao customerDao = new CustomerDao();

        Customer newCustomer = new Customer();
        newCustomer.setEmail("test_user@example.com");
        newCustomer.setPasswordHash("dummy-hash-for-test");
        newCustomer.setName("测试用户");
        newCustomer.setAddress("测试地址");
        newCustomer.setBalance(new BigDecimal("0.00"));
        newCustomer.setCreditLevel(1);
        newCustomer.setMonthlyLimit(new BigDecimal("0.00"));

        int rows = customerDao.insert(newCustomer);
        System.out.println("插入 Customer 影响行数：" + rows);
        System.out.println();

        // ==============================
        // 3. 通过 email 查询 Customer（CustomerDao.findByEmail）
        // ==============================
        System.out.println("=== 测试：通过 Email 查询 Customer ===");
        String queryEmail = "test_user@example.com";
        Customer found = customerDao.findByEmail(queryEmail);
        if (found != null) {
            System.out.println("查询到的用户：");
            System.out.println("CustomerID=" + found.getCustomerId());
            System.out.println("Email=" + found.getEmail());
            System.out.println("Name=" + found.getName());
            System.out.println("Address=" + found.getAddress());
            System.out.println("Balance=" + found.getBalance());
            System.out.println("CreditLevel=" + found.getCreditLevel());
            System.out.println("MonthlyLimit=" + found.getMonthlyLimit());
        } else {
            System.out.println("未找到 Email 为 " + queryEmail + " 的用户");
        }

        System.out.println();

        // ==============================
        // 4. 测试下单事务（失败分支：包含库存不足的订单项）
        // ==============================
        System.out.println("=== 测试：下单事务（createOrderWithItems，包含库存不足的订单项） ===");

        if (found == null) {
            System.out.println("无法进行下单测试：测试 Customer 未找到，请先确保上面插入成功。");
        } else if (books.isEmpty()) {
            System.out.println("无法进行下单测试：Book 表为空，请先插入测试图书。");
        } else {
            int customerId = found.getCustomerId();
            Book book = books.get(0); // 选取第一本书作为测试对象

            // 构造订单明细：一个库存充足，一个库存不足
            List<OrderItem> orderItems = new ArrayList<>();

            // 库存充足的订单项（假定购买 1 本一定 <= 当前库存）
            OrderItem enoughItem = new OrderItem();
            enoughItem.setBookId(book.getBookId());
            enoughItem.setQuantity(1);
            enoughItem.setUnitPrice(book.getPrice());
            orderItems.add(enoughItem);

            // 库存不足的订单项（故意设置为远大于当前库存）
            OrderItem lackItem = new OrderItem();
            lackItem.setBookId(book.getBookId());
            lackItem.setQuantity(
                    (book.getStockQuantity() == null ? 0 : book.getStockQuantity()) + 1_000_000
            );
            lackItem.setUnitPrice(book.getPrice());
            orderItems.add(lackItem);

            OrdersDao ordersDao = new OrdersDao();

            System.out.println("准备下单（包含库存不足的订单项），CustomerID=" + customerId
                    + "，BookID=" + book.getBookId()
                    + "，当前库存=" + book.getStockQuantity());

            int orderId = ordersDao.createOrderWithItems(customerId, orderItems);
            if (orderId > 0) {
                System.out.println("【异常】预期失败的下单居然成功，OrderID = " + orderId);
            } else {
                System.out.println("下单失败，事务已回滚（返回的 OrderID = " + orderId + "）。");
            }
        }

        System.out.println();

        // ==============================
        // 5. 测试下单事务（成功分支：所有订单项库存充足）
        // ==============================
        System.out.println("=== 测试：下单事务成功场景（所有订单项库存充足） ===");

        if (found == null) {
            System.out.println("无法进行下单成功测试：测试 Customer 未找到。");
        } else if (books.isEmpty()) {
            System.out.println("无法进行下单成功测试：Book 表为空。");
        } else {
            // 重新从数据库查询最新库存
            books = bookDao.findAll();
            Book book = books.get(0);
            if (book.getStockQuantity() == null || book.getStockQuantity() < 1) {
                System.out.println("当前库存不足以做成功下单测试，请先为 BookID="
                        + book.getBookId() + " 增加库存（至少 1 本）。当前库存=" + book.getStockQuantity());
            } else {
                int customerId = found.getCustomerId();

                // 构造全部库存充足的订单项（这里使用单一订单项即可）
                List<OrderItem> successItems = new ArrayList<>();
                OrderItem item1 = new OrderItem();
                item1.setBookId(book.getBookId());
                item1.setQuantity(1);
                item1.setUnitPrice(book.getPrice());
                successItems.add(item1);

                OrdersDao ordersDao = new OrdersDao();

                System.out.println("下单前库存：BookID=" + book.getBookId()
                        + "，Stock=" + book.getStockQuantity());

                int orderId = ordersDao.createOrderWithItems(customerId, successItems);
                if (orderId <= 0) {
                    System.out.println("【错误】期望成功的下单返回了失败的 OrderID = " + orderId);
                } else {
                    System.out.println("下单成功，生成的 OrderID = " + orderId);

                    // 再次查询库存
                    Book refreshed = bookDao.findAll().stream()
                            .filter(b -> b.getBookId().equals(book.getBookId()))
                            .findFirst()
                            .orElse(null);
                    if (refreshed != null) {
                        System.out.println("下单后库存：BookID=" + refreshed.getBookId()
                                + "，Stock=" + refreshed.getStockQuantity());
                    } else {
                        System.out.println("下单后无法在 Book 表中找到 BookID=" + book.getBookId());
                    }
                }
            }
        }

        System.out.println();
        System.out.println("=== MainTest 结束 ===");
    }
}


