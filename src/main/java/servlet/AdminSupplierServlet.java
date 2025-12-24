package servlet;

import dao.SupplierDao;
import dao.BookSupplierDao;
import dao.BookDao;
import model.Supplier;
import model.SupplierSupply;
import model.Book;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 管理员端 - 供应商管理（基础信息）
 */
public class AdminSupplierServlet extends HttpServlet {

    private final SupplierDao supplierDao = new SupplierDao();
    private final BookSupplierDao bookSupplierDao = new BookSupplierDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/list".equals(path)) {
            handleList(request, response);
        } else if ("/edit".equals(path)) {
            handleEdit(request, response);
        } else if ("/add".equals(path)) {
            handleAdd(request, response);
        } else if ("/supply".equals(path)) {
            handleSupply(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String supplierIdParam = request.getParameter("supplierId");
        String nameParam = request.getParameter("name");

        List<Supplier> suppliers;
        try {
            suppliers = supplierDao.queryForAdmin(supplierIdParam, nameParam);
        } catch (Exception e) {
            suppliers = supplierDao.findAll();
        }

        request.setAttribute("suppliers", suppliers);
        request.setAttribute("searchSupplierId", supplierIdParam);
        request.setAttribute("searchName", nameParam);
        request.getRequestDispatcher("/jsp/admin/supplierList.jsp").forward(request, response);
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        int supplierId;
        try {
            supplierId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        Supplier supplier = supplierDao.findById(supplierId);
        if (supplier == null) {
            request.getSession().setAttribute("errorMessage", "未找到指定供应商（ID=" + supplierId + "）");
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        List<Book> books = bookDao.findAll();
        List<SupplierSupply> supplies = bookSupplierDao.findSupplyBooksBySupplierId(supplierId);
        java.util.Map<Integer, java.math.BigDecimal> supplyPriceMap = new java.util.HashMap<>();
        for (SupplierSupply s : supplies) {
            if (s.getBookId() != null && s.getSupplyPrice() != null) {
                supplyPriceMap.put(s.getBookId(), s.getSupplyPrice());
            }
        }
        request.setAttribute("supplier", supplier);
        request.setAttribute("books", books);
        request.setAttribute("supplies", supplies);
        request.setAttribute("supplyPriceMap", supplyPriceMap);
        request.getRequestDispatcher("/jsp/admin/supplierEdit.jsp").forward(request, response);
    }

    private void handleSupply(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        int supplierId;
        try {
            supplierId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        Supplier supplier = supplierDao.findById(supplierId);
        if (supplier == null) {
            request.getSession().setAttribute("errorMessage", "未找到指定供应商（ID=" + supplierId + "）");
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        List<SupplierSupply> supplyBooks = bookSupplierDao.findSupplyBooksBySupplierId(supplierId);
        request.setAttribute("supplier", supplier);
        request.setAttribute("supplyBooks", supplyBooks);
        request.getRequestDispatcher("/jsp/admin/supplierSupply.jsp").forward(request, response);
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Book> books = bookDao.findAll();
        request.setAttribute("books", books);
        request.setAttribute("supplyPriceMap", new java.util.HashMap<Integer, java.math.BigDecimal>());
        request.getRequestDispatcher("/jsp/admin/supplierAdd.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/update".equals(path)) {
            handleUpdate(request, response);
        } else if ("/save".equals(path)) {
            handleSave(request, response);
        } else if ("/delete".equals(path)) {
            handleDelete(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idParam = request.getParameter("supplierId");
        String nameParam = request.getParameter("name");
        String addressParam = request.getParameter("address");
        String phoneParam = request.getParameter("phone");
        String emailParam = request.getParameter("contactEmail");

        int supplierId;
        try {
            supplierId = Integer.parseInt(idParam.trim());
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "供应商ID无效");
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        if (nameParam == null || nameParam.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "供应商名称不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/supplier/edit?id=" + supplierId);
            return;
        }

        try {
            int updated = supplierDao.updateBasicInfo(
                    supplierId,
                    nameParam.trim(),
                    addressParam == null ? null : addressParam.trim(),
                    phoneParam == null ? null : phoneParam.trim(),
                    emailParam == null ? null : emailParam.trim()
            );

            if (updated > 0) {
                request.getSession().setAttribute("successMessage", "供应商信息已更新");
            } else {
                request.getSession().setAttribute("errorMessage", "更新供应商信息失败");
            }

            // 同步供货书目
            saveSupplierBooks(supplierId, request);
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "更新供应商信息时发生错误：" + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String nameParam = request.getParameter("name");
        String addressParam = request.getParameter("address");
        String phoneParam = request.getParameter("phone");
        String emailParam = request.getParameter("contactEmail");

        if (nameParam == null || nameParam.trim().isEmpty()) {
            request.getSession().setAttribute("errorMessage", "供应商名称不能为空");
            response.sendRedirect(request.getContextPath() + "/admin/supplier/add");
            return;
        }

        Supplier supplier = new Supplier();
        supplier.setName(nameParam.trim());
        supplier.setAddress(addressParam == null ? null : addressParam.trim());
        supplier.setPhone(phoneParam == null ? null : phoneParam.trim());
        supplier.setContactEmail(emailParam == null ? null : emailParam.trim());

        int newId = supplierDao.insert(supplier);
        if (newId > 0) {
            request.getSession().setAttribute("successMessage", "供应商添加成功，ID=" + newId);
            // 同步供货书目
            saveSupplierBooks(newId, request);
        } else {
            request.getSession().setAttribute("errorMessage", "供应商添加失败");
        }
        response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idParam = request.getParameter("id");
        int supplierId;
        try {
            supplierId = Integer.parseInt(idParam.trim());
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "供应商ID无效");
            response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
            return;
        }

        int deleted = supplierDao.deleteById(supplierId);
        if (deleted > 0) {
            request.getSession().setAttribute("successMessage", "供应商已删除");
        } else {
            request.getSession().setAttribute("errorMessage", "删除失败，可能存在关联数据");
        }
        response.sendRedirect(request.getContextPath() + "/admin/supplier/list");
    }

    /**
     * 处理供应商的供货书目绑定
     */
    private void saveSupplierBooks(int supplierId, HttpServletRequest request) {
        String[] bookIds = request.getParameterValues("bookId");
        if (bookIds == null || bookIds.length == 0) {
            // 未选择任何书，直接清空供货关系
            bookSupplierDao.deleteBySupplierId(supplierId);
            return;
        }

        // 先清空旧的，再批量插入/更新
        bookSupplierDao.deleteBySupplierId(supplierId);

        for (String bIdStr : bookIds) {
            if (bIdStr == null || bIdStr.trim().isEmpty()) continue;
            try {
                int bookId = Integer.parseInt(bIdStr.trim());
                String priceStr = request.getParameter("supplyPrice_" + bookId);
                if (priceStr == null || priceStr.trim().isEmpty()) continue;
                java.math.BigDecimal supplyPrice = new java.math.BigDecimal(priceStr.trim());
                if (supplyPrice.compareTo(java.math.BigDecimal.ZERO) < 0) continue;
                bookSupplierDao.bindSupplierToBook(bookId, supplierId, supplyPrice);
            } catch (Exception ignore) {
            }
        }
    }
}


