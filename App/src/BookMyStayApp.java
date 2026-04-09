import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        String appName = "Hotel Booking System";
        String version = "v1.0";

        System.out.println("Welcome to " + appName + " " + version + "!\n");

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("SingleRoom", 5);
        inventory.addRoomType("DoubleRoom", 3);
        inventory.addRoomType("SuiteRoom", 2);

        // Rooms
        Room single = new SingleRoom(1, 20, 50.0);
        Room dbl = new DoubleRoom(2, 35, 80.0);
        Room suite = new SuiteRoom(3, 60, 150.0);

        // Display
        System.out.println("Current Inventory:");
        inventory.displayInventory();

        // Search
        SearchService searchService = new SearchService(inventory);
        searchService.displayAvailableRooms(single, dbl, suite);

        // Queue
        BookingQueue bookingQueue = new BookingQueue();
        bookingQueue.addRequest(new Reservation("Alice", "SingleRoom"));
        bookingQueue.addRequest(new Reservation("Bob", "DoubleRoom"));
        bookingQueue.addRequest(new Reservation("Charlie", "SuiteRoom"));

        // ✅ Booking History
        BookingHistory history = new BookingHistory();

        // Booking Service
        BookingService bookingService = new BookingService(inventory, history);

        bookingService.processBookings(bookingQueue);
        bookingService.displayAllocations();

        // ✅ Add-On Services (Use Case 7)
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        Reservation temp = new Reservation("Eve", "SingleRoom");
        String resId = bookingService.confirmAndReturnId(temp);

        if (resId != null) {
            serviceManager.addService(resId, new AddOnService("Breakfast", 10.0));
            serviceManager.addService(resId, new AddOnService("WiFi", 5.0));
        }

        // ✅ Use Case 8: Reporting
        System.out.println("\n📜 Booking History:");
        history.displayHistory();

        BookingReportService reportService = new BookingReportService(history);
        reportService.generateSummaryReport();

        System.out.println("\nApplication terminating...");
    }
}

// Reservation
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    @Override
    public String toString() {
        return guestName + " -> " + roomType;
    }
}

// Queue
class BookingQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation processNext() {
        return queue.poll();
    }
}

// ✅ Booking History (NEW)
class BookingHistory {
    private List<Reservation> history;

    public BookingHistory() {
        history = new ArrayList<>();
    }

    public void addReservation(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllReservations() {
        return history;
    }

    public void displayHistory() {
        if (history.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Reservation r : history) {
            System.out.println(r);
        }
    }
}

// Booking Service
class BookingService {
    private RoomInventory inventory;
    private BookingHistory history;

    private Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private Set<String> allAllocatedRoomIds = new HashSet<>();
    private Map<String, Integer> counters = new HashMap<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void processBookings(BookingQueue queue) {
        Reservation r;
        while ((r = queue.processNext()) != null) {
            confirmReservation(r);
        }
    }

    private void confirmReservation(Reservation r) {
        int available = inventory.getAvailability(r.getRoomType());

        if (available <= 0) return;

        String id = generateId(r.getRoomType());

        allocatedRooms.putIfAbsent(r.getRoomType(), new HashSet<>());
        allocatedRooms.get(r.getRoomType()).add(id);

        inventory.updateAvailability(r.getRoomType(), available - 1);

        history.addReservation(r); // ✅ store history

        System.out.println("Confirmed: " + r + " ID=" + id);
    }

    public String confirmAndReturnId(Reservation r) {
        int available = inventory.getAvailability(r.getRoomType());
        if (available <= 0) return null;

        String id = generateId(r.getRoomType());

        inventory.updateAvailability(r.getRoomType(), available - 1);
        history.addReservation(r);

        return id;
    }

    private String generateId(String type) {
        int c = counters.getOrDefault(type, 0) + 1;
        counters.put(type, c);
        return type + "-" + c;
    }

    public void displayAllocations() {
        System.out.println("\nAllocations:");
        System.out.println(allocatedRooms);
    }
}

// ✅ Report Service (NEW)
class BookingReportService {
    private BookingHistory history;

    public BookingReportService(BookingHistory history) {
        this.history = history;
    }

    public void generateSummaryReport() {
        System.out.println("\n📊 Booking Summary Report:");

        Map<String, Integer> countMap = new HashMap<>();

        for (Reservation r : history.getAllReservations()) {
            countMap.put(r.getRoomType(),
                    countMap.getOrDefault(r.getRoomType(), 0) + 1);
        }

        for (Map.Entry<String, Integer> e : countMap.entrySet()) {
            System.out.println(e.getKey() + " booked: " + e.getValue());
        }
    }
}

// Inventory
class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void addRoomType(String type, int count) {
        inventory.put(type, count);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void updateAvailability(String type, int newCount) {
        inventory.put(type, newCount);
    }

    public void displayInventory() {
        System.out.println(inventory);
    }
}

// Search
class SearchService {
    private RoomInventory inventory;

    public SearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void displayAvailableRooms(Room... rooms) {
        for (Room r : rooms) {
            int available = inventory.getAvailability(r.getClass().getSimpleName());
            if (available > 0) {
                System.out.println(r + " Available=" + available);
            }
        }
    }
}

// Add-On
class AddOnService {
    private String name;
    private double cost;

    public AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public double getCost() { return cost; }

    @Override
    public String toString() {
        return name + " ($" + cost + ")";
    }
}

// Add-On Manager
class AddOnServiceManager {
    private Map<String, List<AddOnService>> map = new HashMap<>();

    public void addService(String id, AddOnService s) {
        map.putIfAbsent(id, new ArrayList<>());
        map.get(id).add(s);
    }
}

// Rooms
abstract class Room {
    protected int beds, size;
    protected double price;

    public Room(int beds, int size, double price) {
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public String toString() {
        return getClass().getSimpleName() + " $" + price;
    }
}

class SingleRoom extends Room {
    public SingleRoom(int b, int s, double p) { super(b,s,p); }
}
class DoubleRoom extends Room {
    public DoubleRoom(int b, int s, double p) { super(b,s,p); }
}
class SuiteRoom extends Room {
    public SuiteRoom(int b, int s, double p) { super(b,s,p); }
}