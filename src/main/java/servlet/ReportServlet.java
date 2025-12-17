package servlet;

import util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "ReportServlet", urlPatterns = "/report/views")
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 书籍基本信息视图
        loadView("v_book_basic_info",
                "bookBasicColumns",
                "bookBasicData",
                request);

        // 书籍详细信息视图
        loadView("v_book_detail",
                "bookDetailColumns",
                "bookDetailData",
                request);

        // 客户信息视图
        loadView("v_customer_info",
                "customerInfoColumns",
                "customerInfoData",
                request);

        // 客户订单视图
        loadView("v_customer_orders",
                "customerOrdersColumns",
                "customerOrdersData",
                request);

        // 订单明细视图
        loadView("v_order_items_detail",
                "orderItemsColumns",
                "orderItemsData",
                request);

        // 发货明细视图
        loadView("v_shipment_detail",
                "shipmentDetailColumns",
                "shipmentDetailData",
                request);

        request.getRequestDispatcher("/jsp/report/viewDemo.jsp").forward(request, response);
    }

    /**
     * 通用视图查询：SELECT * FROM viewName
     */
    private void loadView(String viewName,
                          String columnsAttr,
                          String dataAttr,
                          HttpServletRequest request) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM " + viewName);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnLabel(i));
            }

            List<List<Object>> rows = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                rows.add(row);
            }

            request.setAttribute(columnsAttr, columns);
            request.setAttribute(dataAttr, rows);
        } catch (SQLException e) {
            // 出错时仍然设置空数据，避免 JSP 报错
            request.setAttribute(columnsAttr, new ArrayList<String>());
            request.setAttribute(dataAttr, new ArrayList<List<Object>>());
        } finally {
            DBUtil.closeQuietly(rs);
            DBUtil.closeQuietly(stmt);
            DBUtil.closeQuietly(conn);
        }
    }
}


