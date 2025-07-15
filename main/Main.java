package main;

import java.sql.*;
import java.util.*;

public class Main {
    private static Scanner sc = new Scanner(System.in);
    private static String loggedInUserEmail = null;
    private static String loggedInUserName = null;

    public static void main(String[] args) {
        while (true) {
            if (loggedInUserEmail == null) {
                System.out.println("\n----- BUS RESERVATION SYSTEM ----");
                System.out.println("1. Sign Up");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice : ");
                int choice = sc.nextInt();
                sc.nextLine();
                switch (choice) {
                    case 1 -> signUp();
                    case 2 -> login();
                    case 3 -> System.exit(0);
                    default -> System.out.println("Invalid choice! Try again.");
                }
            } else {
                showMainMenu();
            }
        }
    }

    private static void signUp() {
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Email already registered. Try logging in.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void login() {
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT name FROM users WHERE email = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loggedInUserEmail = email;
                loggedInUserName = rs.getString("name");
                System.out.println("Login successful! Welcome, " + loggedInUserName);
            } else {
                System.out.println("Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n----- MAIN MENU ----");
            System.out.println("1. Add a Bus");
            System.out.println("2. View Buses");
            System.out.println("3. Book a Ticket");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> addBus();
                case 2 -> viewBuses();
                case 3 -> bookTicket();
                case 4 -> {
                    loggedInUserEmail = null;
                    loggedInUserName = null;
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void addBus() {
        System.out.print("Enter Bus Number: ");
        String busNumber = sc.nextLine();
        System.out.print("Enter Source: ");
        String source = sc.nextLine();
        System.out.print("Enter Destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter Total Seats: ");
        int totalSeats = sc.nextInt();
        sc.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO buses (bus_number, source, destination, total_seats, available_seats) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, busNumber);
            pstmt.setString(2, source);
            pstmt.setString(3, destination);
            pstmt.setInt(4, totalSeats);
            pstmt.setInt(5, totalSeats);
            pstmt.executeUpdate();
            System.out.println("Bus added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void viewBuses() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM buses";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nAvailable Buses:");
            while (rs.next()) {
                System.out.println("Bus Number: " + rs.getString("bus_number"));
                System.out.println("Route: " + rs.getString("source") + " --> " + rs.getString("destination"));
                System.out.println("Seats: " + rs.getInt("available_seats") + "/" + rs.getInt("total_seats"));
                System.out.println("----------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void bookTicket() {
        System.out.print("Enter Source: ");
        String source = sc.nextLine();
        System.out.print("Enter Destination: ");
        String destination = sc.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM buses WHERE source = ? AND destination = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, source);
            pstmt.setString(2, destination);
            ResultSet rs = pstmt.executeQuery();

            List<Integer> busIds = new ArrayList<>();
            while (rs.next()) {
                System.out.println("Bus ID: " + rs.getInt("id") + " | " + rs.getString("bus_number"));
                busIds.add(rs.getInt("id"));
            }

            if (busIds.isEmpty()) {
                System.out.println("No buses found.");
                return;
            }

            System.out.print("Enter Bus ID to Book: ");
            int busId = sc.nextInt();
            sc.nextLine();

            if (!busIds.contains(busId)) {
                System.out.println("Invalid Bus ID.");
                return;
            }

            System.out.print("Enter Date of Travel (yyyy-mm-dd): ");
            String date = sc.nextLine();
            System.out.print("Enter Phone Number: ");
            String phone = sc.nextLine();
            System.out.print("Number of Passengers: ");
            int numPassengers = sc.nextInt();
            sc.nextLine();

            // Check seat availability
            String checkSeats = "SELECT available_seats FROM buses WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSeats);
            checkStmt.setInt(1, busId);
            ResultSet seatRS = checkStmt.executeQuery();
            if (seatRS.next() && seatRS.getInt("available_seats") < numPassengers) {
                System.out.println("Only " + seatRS.getInt("available_seats") + " seats available.");
                return;
            }

            // Get user ID
            String userIdQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(userIdQuery);
            userStmt.setString(1, loggedInUserEmail);
            ResultSet userRS = userStmt.executeQuery();
            userRS.next();
            int userId = userRS.getInt("id");

            // Insert booking
            String bookingQuery = "INSERT INTO bookings (user_id, bus_id, travel_date, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement bookStmt = conn.prepareStatement(bookingQuery, Statement.RETURN_GENERATED_KEYS);
            bookStmt.setInt(1, userId);
            bookStmt.setInt(2, busId);
            bookStmt.setString(3, date);
            bookStmt.setString(4, phone);
            bookStmt.executeUpdate();
            ResultSet bookKey = bookStmt.getGeneratedKeys();
            bookKey.next();
            int bookingId = bookKey.getInt(1);

            for (int i = 1; i <= numPassengers; i++) {
                System.out.println("Passenger " + i + ":");
                System.out.print("Name: ");
                String name = sc.nextLine();
                System.out.print("Age: ");
                int age = sc.nextInt();
                sc.nextLine();
                System.out.print("Gender: ");
                String gender = sc.nextLine();

                String passQuery = "INSERT INTO passengers (booking_id, name, age, gender) VALUES (?, ?, ?, ?)";
                PreparedStatement passStmt = conn.prepareStatement(passQuery);
                passStmt.setInt(1, bookingId);
                passStmt.setString(2, name);
                passStmt.setInt(3, age);
                passStmt.setString(4, gender);
                passStmt.executeUpdate();
            }

            String updateSeats = "UPDATE buses SET available_seats = available_seats - ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSeats);
            updateStmt.setInt(1, numPassengers);
            updateStmt.setInt(2, busId);
            updateStmt.executeUpdate();

            System.out.println("Tickets booked successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



/*
package main;
import java.util.*;
class Bus {
    private String busNumber;
    private String source;
    private String destination;
    private int totalSeats;
    private int availableSeats;
    //constructor
    public Bus(String busNumber,String source,String destination,int totalSeats) {
        this.busNumber = busNumber;
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }
    public String getBusNumber() {
        return busNumber;
    }
    public String getSource() {
        return source;
    }
    public String getDestination() {
        return destination;
    }
    public int getTotalSeats() {
        return totalSeats;
    }
    public int getAvailableSeats() {
        return availableSeats;
    }
    public boolean bookSeat() {
        if(availableSeats > 0) {
            availableSeats--;
            return true;
        }
        else {
            return false;
        }
    }
    public void displayBusInfo() {
        System.out.println("\nBus Number : "+ busNumber);
        System.out.println("Route : "+source+" --> "+destination);
        System.out.println("Seats Available: " + availableSeats + "/" + totalSeats);
        System.out.println("----------------------------------------");
    }
}
class User {
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }
}
public class Main {
    private static ArrayList<Bus> buses = new ArrayList<>();
    private static ArrayList<User> users = new ArrayList<>();
    private static User loggedInUser = null;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(true) {
            if(loggedInUser == null) {
                System.out.println("\n----- BUS RESERVATION SYSTEM ----");
                System.out.println("1. Sign Up");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice : ");
                int choice = sc.nextInt();
                sc.nextLine();
                switch(choice) {
                    case 1:
                        signUp(sc);
                        break;
                    case 2:
                        login(sc);
                        break;
                    case 3:
                        System.out.println("Existing system...");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            }
            else {
                showMainMenu(sc);
            }
        }
    }
    private static void signUp(Scanner sc) {
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        User user = new User(name,email,password);
        users.add(user);
        System.out.println("User registered successfully! Please login.");
    }
    private static void login(Scanner sc) {
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        for(User user : users) {
            if(user.login(email,password)) {
                loggedInUser = user;
                System.out.println("Login successful! Welcome, "+user.getName());
                return;
            }
        }
        System.out.println("Invalid credentials! try again.");
    }
    private static void showMainMenu(Scanner sc) {
        while(true) {
            System.out.println("\n----- MAIN MENU ----");
            System.out.println("1. Add a Bus");
            System.out.println("2. View Buses");
            System.out.println("3. Book a Ticket");
            System.out.println("4. Logout");
            System.out.println("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();
            switch(choice) {
                case 1:
                    addBus(sc);
                    break;
                case 2:
                    viewBuses();
                    break;
                case 3:
                    bookTicket(sc);
                    break;
                case 4:
                    System.out.println("Logged out successfully.");
                    loggedInUser = null;
                    return;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }
    private static void addBus(Scanner sc) {
        System.out.print("Enter Bus Number: ");
        String busNumber = sc.nextLine();
        System.out.print("Enter Source: ");
        String source = sc.nextLine();
        System.out.print("Enter Destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter Total Seats: ");
        int totalSeats = sc.nextInt();
        Bus bus = new Bus(busNumber,source,destination,totalSeats);
        buses.add(bus);
        System.out.println("Bus added Successfully!");
    }
    private static void viewBuses() {
        if(buses.isEmpty()) {
            System.out.println("No buses available.");
        }
        else {
            System.out.println("\nAvailable Buses: ");
            for(Bus bus : buses) {
                bus.displayBusInfo();
            }
        }
    }
    private static void bookTicket(Scanner sc) {
        if (buses.isEmpty()) {
            System.out.println("No buses available for booking.");
            return;
        }
        System.out.print("Enter Source: ");
        String source = sc.nextLine();
        System.out.print("Enter Destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter Date of Travel (dd-mm-yyyy): ");
        String travelDate = sc.nextLine();
        for (Bus bus : buses) {
            if (bus.getSource().equalsIgnoreCase(source) && bus.getDestination().equalsIgnoreCase(destination)) {
                System.out.print("Enter Number of Passengers: ");
                int numPassengers = sc.nextInt();
                sc.nextLine();
                if(bus.getAvailableSeats() < numPassengers) {
                    System.out.println("Only "+bus.getAvailableSeats()+" seats available. Booking failed.");
                    return;
                }
                ArrayList<String> passengerDetails = new ArrayList<>();
                for(int i=1;i<=numPassengers;i++) {
                    System.out.println("Passenger "+i+" : ");
                    System.out.print("Name : ");
                    String name = sc.nextLine();
                    System.out.print("Age : ");
                    int age = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Gender : ");
                    String gender = sc.nextLine();
                    passengerDetails.add(i+") Name: "+name+",Age: "+age+",Gender: "+gender);
                    bus.bookSeat();
                }
                System.out.print("Mobile Number : ");
                String phone = sc.nextLine();
                System.out.println("------- Tickets Booked Succssfully -------");
                System.out.println("Booking Contact : "+loggedInUser.getName()+" | Phone : "+phone);
                System.out.println("Bus Number : "+bus.getBusNumber()+" "+source+" ---> "+destination+" on "+travelDate);
                System.out.println("Passenger Deatils : ");
                for(String details : passengerDetails) {
                    System.out.println(details);
                }
                System.out.println("---------------------------------------------");
                return;
            }
        }
        System.out.println("Bus not found!");
    }
} */
