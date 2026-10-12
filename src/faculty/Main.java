package faculty;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FacultyDAO dao = new FacultyDAO();

        int choice;

        do {
            System.out.println("\nResearch Publication Management System");
            System.out.println("Faculty Management Module");
            System.out.println("1. Add Faculty");
            System.out.println("2. View All Faculty");
            System.out.println("3. Update Faculty");
            System.out.println("4. Delete Faculty");
            System.out.println("5. Search Faculty");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Faculty ID: ");
                    int id = sc.nextInt();
                    sc.nextLine();

                    if (dao.searchById(id) != null) {
                        System.out.println("This ID already exists.");
                        break;
                    }

                    System.out.print("Enter Faculty Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Faculty Designation: ");
                    String designation = sc.nextLine();

                    System.out.print("Enter Faculty Department: ");
                    String department = sc.nextLine();

                    if (name.trim().isEmpty()
                            || designation.trim().isEmpty()
                            || department.trim().isEmpty()) {
                        System.out.println("Fields cannot be empty.");
                        break;
                    }

                    Faculty f = new Faculty(
                        id, name, designation, department
                    );

                    if (dao.addFaculty(f)) {
                        System.out.println("Faculty added successfully!");
                    } else {
                        System.out.println("Faculty could not be added.");
                    }
                    break;

                case 2:
                    ArrayList<Faculty> list = dao.getAllFaculty();

                    if (list.isEmpty()) {
                        System.out.println("No faculty details available.");
                    } else {
                        System.out.println("\nFaculty Details");

                        for (Faculty s : list) {
                            System.out.println(
                                "ID: " + s.getId()
                                + " | Name: " + s.getname()
                                + " | Designation: " + s.getDes()
                                + " | Department: " + s.getdept()
                            );
                        }
                    }
                    break;

                case 3:
                    System.out.print("Enter Faculty ID to update: ");
                    int updateId = sc.nextInt();
                    sc.nextLine();

                    Faculty existing = dao.searchById(updateId);

                    if (existing == null) {
                        System.out.println("Faculty ID not found.");
                        break;
                    }

                    boolean updating = true;

                    while (updating) {
                        System.out.println("\n1. Update Name");
                        System.out.println("2. Update Designation");
                        System.out.println("3. Update Department");
                        System.out.println("4. Update All Details");
                        System.out.println("5. Finish Updating");
                        System.out.print("Enter your choice: ");

                        int updateChoice = sc.nextInt();
                        sc.nextLine();

                        switch (updateChoice) {
                            case 1:
                                System.out.print("Enter new name: ");
                                String newName = sc.nextLine();

                                if (newName.trim().isEmpty()) {
                                    System.out.println("Name cannot be empty.");
                                } else {
                                    existing.setname(newName);
                                }
                                break;

                            case 2:
                                System.out.print("Enter new designation: ");
                                String newDesignation = sc.nextLine();

                                if (newDesignation.trim().isEmpty()) {
                                    System.out.println("Designation cannot be empty.");
                                } else {
                                    existing.setDes(newDesignation);
                                }
                                break;

                            case 3:
                                System.out.print("Enter new department: ");
                                String newDepartment = sc.nextLine();

                                if (newDepartment.trim().isEmpty()) {
                                    System.out.println("Department cannot be empty.");
                                } else {
                                    existing.setdept(newDepartment);
                                }
                                break;

                            case 4:
                                System.out.print("Enter new name: ");
                                String allName = sc.nextLine();

                                System.out.print("Enter new designation: ");
                                String allDesignation = sc.nextLine();

                                System.out.print("Enter new department: ");
                                String allDepartment = sc.nextLine();

                                if (allName.trim().isEmpty()
                                        || allDesignation.trim().isEmpty()
                                        || allDepartment.trim().isEmpty()) {
                                    System.out.println("Fields cannot be empty.");
                                } else {
                                    existing.setname(allName);
                                    existing.setDes(allDesignation);
                                    existing.setdept(allDepartment);
                                }
                                break;

                            case 5:
                                updating = false;
                                break;

                            default:
                                System.out.println("Enter a valid choice.");
                        }
                    }

                    if (dao.updateFaculty(
                            existing.getId(),
                            existing.getname(),
                            existing.getdept(),
                            existing.getDes())) {
                        System.out.println("Faculty updated successfully!");
                    } else {
                        System.out.println("Faculty update failed.");
                    }
                    break;

                case 4:
                    System.out.print("Enter Faculty ID to delete: ");
                    int deleteId = sc.nextInt();
                    sc.nextLine();

                    if (dao.deleteFaculty(deleteId)) {
                        System.out.println("Faculty deleted successfully!");
                    } else {
                        System.out.println("Faculty ID not found or delete failed.");
                    }
                    break;

                case 5:
                    System.out.println("1. Search by ID");
                    System.out.println("2. Search by Name");
                    System.out.println("3. Search by Department");
                    System.out.print("Enter your choice: ");
                    int searchChoice = sc.nextInt();
                    sc.nextLine();

                    if (searchChoice == 1) {
                        System.out.print("Enter Faculty ID: ");
                        int searchId = sc.nextInt();
                        sc.nextLine();

                        Faculty found = dao.searchById(searchId);

                        if (found != null) {
                            System.out.println(
                                found.getId() + " | "
                                + found.getname() + " | "
                                + found.getDes() + " | "
                                + found.getdept()
                            );
                        } else {
                            System.out.println("Faculty not found.");
                        }

                    } else if (searchChoice == 2) {
                        System.out.print("Enter Faculty Name: ");
                        String searchName = sc.nextLine();

                        ArrayList<Faculty> results =
                            dao.searchByName(searchName);

                        if (results.isEmpty()) {
                            System.out.println("Faculty not found.");
                        } else {
                            for (Faculty s : results) {
                                System.out.println(
                                    s.getId() + " | "
                                    + s.getname() + " | "
                                    + s.getDes() + " | "
                                    + s.getdept()
                                );
                            }
                        }

                    } else if (searchChoice == 3) {
                        System.out.print("Enter Department: ");
                        String searchDepartment = sc.nextLine();

                        ArrayList<Faculty> results =
                            dao.searchByDepartment(searchDepartment);

                        if (results.isEmpty()) {
                            System.out.println("Faculty not found.");
                        } else {
                            for (Faculty s : results) {
                                System.out.println(
                                    s.getId() + " | "
                                    + s.getname() + " | "
                                    + s.getDes() + " | "
                                    + s.getdept()
                                );
                            }
                        }

                    } else {
                        System.out.println("Enter a valid choice.");
                    }
                    break;

                case 6:
                    System.out.println("Exiting Faculty Management Module.");
                    break;

                default:
                    System.out.println("Enter a valid choice.");
            }

        } while (choice != 6);

        sc.close();
    }
}