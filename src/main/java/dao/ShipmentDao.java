package dao;

import model.Shipment;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDao {

    public int insert(Shipment shipment) {
        String sql = "INSERT INTO Shipment (OrderID, BookID, Quantity, ShipDate, Carrier, TrackingNo) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, shipment.getOrderId());
            ps.setObject(2, shipment.getBookId());
            ps.setObject(3, shipment.getQuantity());

            LocalDateTime shipDate = shipment.getShipDate();
            if (shipDate != null) {
                ps.setTimestamp(4, Timestamp.valueOf(shipDate));
            } else {
                ps.setTimestamp(4, null);
            }

            ps.setString(5, shipment.getCarrier());
            ps.setString(6, shipment.getTrackingNo());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeQuietly(ps);
            DBUtil.closeQuietly(conn);
        }
    }

    public List<Shipment> findByOrderId(int orderId) {
        String sql = "SELECT ShipmentID, OrderID, BookID, Quantity, ShipDate, Carrier, TrackingNo " +
                "FROM Shipment WHERE OrderID = ? ORDER BY ShipDate, ShipmentID";
        List<Shipment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeQuietly(rs, ps, conn);
        }
        return list;
    }

    private Shipment mapRow(ResultSet rs) throws SQLException {
        Shipment shipment = new Shipment();
        shipment.setShipmentId((Integer) rs.getObject("ShipmentID"));
        shipment.setOrderId((Integer) rs.getObject("OrderID"));
        shipment.setBookId((Integer) rs.getObject("BookID"));
        shipment.setQuantity((Integer) rs.getObject("Quantity"));

        Timestamp ts = rs.getTimestamp("ShipDate");
        if (ts != null) {
            shipment.setShipDate(ts.toLocalDateTime());
        }

        shipment.setCarrier(rs.getString("Carrier"));
        shipment.setTrackingNo(rs.getString("TrackingNo"));
        return shipment;
    }
}


