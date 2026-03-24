/**
 * BookMyStayApp demonstrates a Hotel Booking Management System.
 * Introduces centralized room inventory management using HashMap.
 *
 * @author YourName
 * @version 1.2
 */
import java.util.HashMap;
import java.util.Map;

public class BookMyStayApp {

    public static void main(String[] args) {

        String appName = "Hotel Booking System";
        String version = "v1.0";

        System.out.println("Welcome to " + appName + " " + version + "!");
        System.out.println("Your gateway to seamless hotel reservations.\n");

        System.out.println("Application started successfully.\n");

        // Initialize centralized inventory
        RoomInventory inventory = new RoomInventory();
        inventory.registerRoomType("SingleRoom", 5);
        inventory.registerRoomType("DoubleRoom", 3);
        inventory.registerRoomType("SuiteRoom", 2);

        // Room objects
        Room single = new SingleRoom(1, 20, 50.0);
        Room dbl = new DoubleRoom(2, 35, 80.0);
        Room suite = new SuiteRoom(3, 60, 150.0);

        // Display room details and availability from inventory
        System.out.println(single);
        System.out.println("Available: " + inventory.getAvailability("SingleRoom") + "\n");

        System.out.println(dbl);
        System.out.println("Available: " + inventory.getAvailability("DoubleRoom") + "\n");

        System.out.println(suite);
        System.out.println("Available: " + inventory.getAvailability("SuiteRoom") + "\n");

        System.out.println("Application terminating...");
    }
}

// Abstract Room class
abstract class Room {
    protected int beds;
    protected int size;
    protected double price;

    public Room(int beds, int size, double price) {
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                " - Beds: " + beds +
                ", Size: " + size + "sqm" +
                ", Price: $" + String.format("%.2f", price);
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

// Centralized room inventory
class RoomInventory {
    private Map<String, Integer> availability;

    public RoomInventory() {
        availability = new HashMap<>();
    }

    public void registerRoomType(String roomType, int count) {
        availability.put(roomType, count);
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int count) {
        if (availability.containsKey(roomType)) {
            availability.put(roomType, count);
        }
    }
}