/**
 * HotelBookingApp serves as the entry point for the Hotel Booking application.
 * It demonstrates how a Java program starts execution and prints output to the console.
 *
 * <p>Application Flow:</p>
 * <ul>
 *     <li>User runs the application</li>
 *     <li>JVM invokes the main() method</li>
 *     <li>Welcome message is displayed</li>
 *     <li>Application terminates</li>
 * </ul>
 *
 * @author YourName
 * @version 1.0
 */
public class BookMyStayApp {

    /**
     * Main method - entry point of the application.
     * JVM starts execution from here.
     *
     * @param args Command-line arguments (not used in this application)
     */
    public static void main(String[] args) {

        // Application name and version
        String appName = "Hotel Booking System";
        String version = "v1.0";

        // Display welcome message
        System.out.println("Welcome to " + appName + " " + version + "!");
        System.out.println("Your gateway to seamless hotel reservations.");

        // Indicate application start and end
        System.out.println("Application started successfully.");
        System.out.println("Application terminating...");

    }
}