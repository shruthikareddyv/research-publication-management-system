
package faculty;

import java.sql.*;
import java.util.ArrayList;

public class FacultyDAO {

    public boolean addFaculty(Faculty f) {
        String sql = "INSERT INTO Faculty " +
            "(Faculty_ID, Faculty_Name, Department, Designation) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, f.getId());
            ps.setString(2, f.getname());
            ps.setString(3, f.getdept());
            ps.setString(4, f.getDes());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Add failed: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Faculty> getAllFaculty() {
        ArrayList<Faculty> list = new ArrayList<>();
        String sql = "SELECT * FROM Faculty";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Faculty(
                    rs.getInt("Faculty_ID"),
                    rs.getString("Faculty_Name"),
                    rs.getString("Designation"),
                    rs.getString("Department")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Display failed: " + e.getMessage());
        }

        return list;
    }

    public Faculty searchById(int id) {
        String sql = "SELECT * FROM Faculty WHERE Faculty_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Faculty(
                        rs.getInt("Faculty_ID"),
                        rs.getString("Faculty_Name"),
                        rs.getString("Designation"),
                        rs.getString("Department")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }

        return null;
    }

    public ArrayList<Faculty> searchByName(String name) {
        ArrayList<Faculty> list = new ArrayList<>();
        String sql = "SELECT * FROM Faculty WHERE Faculty_Name LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Faculty(
                        rs.getInt("Faculty_ID"),
                        rs.getString("Faculty_Name"),
                        rs.getString("Designation"),
                        rs.getString("Department")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }

        return list;
    }

    public ArrayList<Faculty> searchByDepartment(String department) {
        ArrayList<Faculty> list = new ArrayList<>();
        String sql = "SELECT * FROM Faculty WHERE Department LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + department + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Faculty(
                        rs.getInt("Faculty_ID"),
                        rs.getString("Faculty_Name"),
                        rs.getString("Designation"),
                        rs.getString("Department")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }

        return list;
    }

    public boolean updateFaculty(int id, String name,
                                 String department, String designation) {
        String sql = "UPDATE Faculty SET Faculty_Name = ?, " +
            "Department = ?, Designation = ? WHERE Faculty_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, department);
            ps.setString(3, designation);
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFaculty(int id) {
        String sql = "DELETE FROM Faculty WHERE Faculty_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
            return false;
        }
    }
}
