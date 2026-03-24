/**
 * HotelBookingApp serves as the entry point for the Hotel Booking application.
 * It demonstrates how a Java program starts execution and prints output to the console.
 *
 * <p>Application Flow:</p>
 * <ul>
 *     <li>User runs the application</li>
 *     <li>JVM invokes the main() method</li>
 *     <li>Welcome message is displayed</li>
 *     <li>Room types and availability are shown</li>
 *     <li>Application terminates</li>
 * </ul>
 *
 * @author YourName
 * @version 1.1
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        String appName = "Hotel Booking System";
        String version = "v1.0";

        System.out.println("Welcome to " + appName + " " + version + "!");
        System.out.println("Your gateway to seamless hotel reservations.");
        System.out.println("Application started successfully.\n");

        // Static availability
        int singleRoomAvailable = 5;
        int doubleRoomAvailable = 3;
        int suiteRoomAvailable = 2;

        // Room objects
        Room single = new SingleRoom(1, 20, 50.0);
        Room dbl = new DoubleRoom(2, 35, 80.0);
        Room suite = new SuiteRoom(3, 60, 150.0);

        // Display room details and availability
        System.out.println(single);
        System.out.println("Available: " + singleRoomAvailable + "\n");

        System.out.println(dbl);
        System.out.println("Available: " + doubleRoomAvailable + "\n");

        System.out.println(suite);
        System.out.println("Available: " + suiteRoomAvailable + "\n");

        System.out.println("Application terminating...");
    }
}

// Abstract Room class
abstract class Room {
    protected int beds;
    protected int size; // in sq meters
    protected double price; // per night

    public Room(int beds, int size, double price) {
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " - Beds: " + beds + ", Size: " + size + "sqm, Price: $" + price;
    }
}

// Concrete room classes
class SingleRoom extends Room {
    public SingleRoom(int beds, int size, double price) {
        super(beds, size, price);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom(int beds, int size, double price) {
        super(beds, size, price);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom(int beds, int size, double price) {
        super(beds, size, price);
    }
}