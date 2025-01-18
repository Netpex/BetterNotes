package me.netpex.betternotes;

import java.sql.*;
import java.util.UUID;

public class Database {

    private Connection connection;
    private final String databasePath;

    public Database(String databasePath) {
        this.databasePath = databasePath;
    }

    public void initialize() {
        try {
            // Create database file if it doesn't exist
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS banknotes ("
                + "uuid TEXT PRIMARY KEY, "
                + "amount DOUBLE NOT NULL, "
                + "creator TEXT NOT NULL, "
                + "created_at TIMESTAMP NOT NULL, "
                + "claimed BOOLEAN DEFAULT FALSE, "
                + "claimed_by TEXT, "
                + "claimed_at TIMESTAMP"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public double getTotalUnclaimedAmount() {
        String sql = "SELECT SUM(amount) as total FROM banknotes WHERE claimed = FALSE";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getPlayerClaimedBanknotes(UUID uuid) {
        String sql = "SELECT COUNT(*) as total FROM banknotes WHERE claimed_by = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set the UUID parameter
            stmt.setObject(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPlayerCreatedBanknotes(UUID uuid) {
        String sql = "SELECT COUNT(*) as total FROM banknotes WHERE creator = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set the UUID parameter
            stmt.setObject(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPlayerCreatedBanknotesUnclaimed(UUID uuid) {
        String sql = "SELECT COUNT(*) as total FROM banknotes WHERE claimed = FALSE AND creator = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set the UUID parameter
            stmt.setObject(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean insert(String table, String columns, Object... values) {
        String placeholders = String.join(", ", "?".repeat(values.length).split(""));
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                stmt.setObject(i + 1, values[i]);
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(String table, String setClause, String whereClause, Object... values) {
        String sql = "UPDATE " + table + " SET " + setClause + " WHERE " + whereClause;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                stmt.setObject(i + 1, values[i]);
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet select(String table, String columns, String whereClause, Object... values) {
        String sql = "SELECT " + columns + " FROM " + table + (whereClause.isEmpty() ? "" : " WHERE " + whereClause);

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            for (int i = 0; i < values.length; i++) {
                stmt.setObject(i + 1, values[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(String table, String whereClause, Object... values) {
        String sql = "DELETE FROM " + table + " WHERE " + whereClause;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                stmt.setObject(i + 1, values[i]);
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAll(String table) {
        String sql = "DELETE FROM " + table;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
