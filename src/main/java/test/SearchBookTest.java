package test;

import dao.BookDao;
import model.Book;

import java.util.List;

public class SearchBookTest {

    public static void main(String[] args) throws Exception {
        BookDao bookDao = new BookDao();

        // 1) 按书名模糊查询（匹配优先级）
        String titleKeyword = "数据库系统概论";
        List<Book> byTitle = bookDao.searchByTitleWithRank(titleKeyword);
        System.out.println("=== 按书名模糊查询（带优先级） ===");
        System.out.println("关键字：" + titleKeyword);
        printBooks(byTitle);

        // 2) 按关键字查询
        String keyword = "数据库";
        List<Book> byKeyword = bookDao.searchByKeyword(keyword);
        System.out.println("\n=== 按关键字查询 ===");
        System.out.println("关键字：" + keyword);
        printBooks(byKeyword);

        // 3) 按作者查询（任意作者顺位）
        String authorName = "王珊";
        List<Book> byAuthor = bookDao.searchByAuthor(authorName);
        System.out.println("\n=== 按作者查询 ===");
        System.out.println("作者名：" + authorName);
        printBooks(byAuthor);

        // 4) 按出版社查询
        String publisher = "高等教育出版社";
        List<Book> byPublisher = bookDao.findByPublisher(publisher);
        System.out.println("\n=== 按出版社查询 ===");
        System.out.println("出版社：" + publisher);
        printBooks(byPublisher);

        System.out.println("\n=== 结束 ===");
    }

    private static void printBooks(List<Book> books) {
        if (books == null || books.isEmpty()) {
            System.out.println("无结果");
            return;
        }
        for (Book book : books) {
            System.out.println("BookID=" + book.getBookId()
                    + " | Title=" + book.getTitle()
                    + " | Publisher=" + book.getPublisher());
        }
    }
}


