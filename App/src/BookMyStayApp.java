import java.util.HashMap;
import java.util.Map;

public class BookMyStayApp {

    public static void main(String[] args) {

        String appName = "Hotel Booking System";
        String version = "v1.0";

        System.out.println("Welcome to " + appName + " " + version + "!");
        System.out.println("Your gateway to seamless hotel reservations.\n");
        System.out.println("Application started successfully.\n");

        // ✅ Centralized Inventory Initialization
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("SingleRoom", 5);
        inventory.addRoomType("DoubleRoom", 3);
        inventory.addRoomType("SuiteRoom", 2);

        Room single = new SingleRoom(1, 20, 50.0);
        Room dbl = new DoubleRoom(2, 35, 80.0);
        Room suite = new SuiteRoom(3, 60, 150.0);

        System.out.println(single);
        System.out.println("Available: " + inventory.getAvailability("SingleRoom") + "\n");

        System.out.println(dbl);
        System.out.println("Available: " + inventory.getAvailability("DoubleRoom") + "\n");

        System.out.println(suite);
        System.out.println("Available: " + inventory.getAvailability("SuiteRoom") + "\n");

        // ✅ Display full inventory
        System.out.println("Current Inventory Status:");
        inventory.displayInventory();

        System.out.println("\nApplication terminating...");
    }
}

// ✅ Centralized Inventory Manager
class RoomInventory {
    private Map<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
    }

    public void addRoomType(String roomType, int count) {
        inventory.put(roomType, count);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int newCount) {
        if (inventory.containsKey(roomType)) {
            inventory.put(roomType, newCount);
        } else {
            System.out.println("Room type not found: " + roomType);
        }
    }

    public void displayInventory() {
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " -> Available: " + entry.getValue());
        }
    }
}

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