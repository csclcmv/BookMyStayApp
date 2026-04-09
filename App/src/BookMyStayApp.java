import java.util.*; // covers HashMap, Map, Queue, LinkedList

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

        // Room objects
        Room single = new SingleRoom(1, 20, 50.0);
        Room dbl = new DoubleRoom(2, 35, 80.0);
        Room suite = new SuiteRoom(3, 60, 150.0);

        // Display all rooms
        System.out.println(single);
        System.out.println("Available: " + inventory.getAvailability("SingleRoom") + "\n");

        System.out.println(dbl);
        System.out.println("Available: " + inventory.getAvailability("DoubleRoom") + "\n");

        System.out.println(suite);
        System.out.println("Available: " + inventory.getAvailability("SuiteRoom") + "\n");

        // ✅ Display full inventory
        System.out.println("Current Inventory Status:");
        inventory.displayInventory();

        // ✅ Use Case 4: Search Service (Read-Only)
        SearchService searchService = new SearchService(inventory);

        System.out.println("\nAvailable Rooms for Booking:");
        searchService.displayAvailableRooms(single, dbl, suite);

        // ✅ Use Case 5: Booking Request Queue (FIFO)
        BookingQueue bookingQueue = new BookingQueue();

        System.out.println("\nSubmitting Booking Requests...");

        bookingQueue.addRequest(new Reservation("Alice", "SingleRoom"));
        bookingQueue.addRequest(new Reservation("Bob", "DoubleRoom"));
        bookingQueue.addRequest(new Reservation("Charlie", "SuiteRoom"));
        bookingQueue.addRequest(new Reservation("Diana", "SingleRoom"));

        System.out.println("\nCurrent Booking Queue:");
        bookingQueue.displayQueue();

        System.out.println("\nApplication terminating...");
    }
}

// ✅ Reservation (Booking Request)
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    @Override
    public String toString() {
        return "Guest: " + guestName + ", Requested Room: " + roomType;
    }
}

// ✅ FIFO Booking Queue
class BookingQueue {
    private Queue<Reservation> queue;

    public BookingQueue() {
        queue = new LinkedList<>();
    }

    // Add booking request
    public void addRequest(Reservation reservation) {
        queue.offer(reservation);
        System.out.println("Request added -> " + reservation);
    }

    // View next request (without removing)
    public Reservation peekNext() {
        return queue.peek();
    }

    // Remove next request (for future use case)
    public Reservation processNext() {
        return queue.poll();
    }

    // Display all queued requests
    public void displayQueue() {
        if (queue.isEmpty()) {
            System.out.println("No pending booking requests.");
            return;
        }

        for (Reservation r : queue) {
            System.out.println(r);
        }
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

// ✅ Read-Only Search Service
class SearchService {
    private RoomInventory inventory;

    public SearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    // Displays only rooms with availability > 0
    public void displayAvailableRooms(Room... rooms) {
        for (Room room : rooms) {
            String roomType = room.getClass().getSimpleName();
            int available = inventory.getAvailability(roomType);

            if (available > 0) {
                System.out.println(room);
                System.out.println("Available: " + available + "\n");
            }
        }
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

// Room types
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