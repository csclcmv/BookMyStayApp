import java.util.*; // covers HashMap, Map, Queue, LinkedList

public class BookMyStayApp {

    public static void main(String[] args) {

        String appName = "Hotel Booking System";
        String version = "v1.0";

        System.out.println("Welcome to " + appName + " " + version + "!");
        System.out.println("Your gateway to seamless hotel reservations.\n");
        System.out.println("Application started successfully.\n");

        // ✅ Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("SingleRoom", 5);
        inventory.addRoomType("DoubleRoom", 3);
        inventory.addRoomType("SuiteRoom", 2);

        // Rooms
        Room single = new SingleRoom(1, 20, 50.0);
        Room dbl = new DoubleRoom(2, 35, 80.0);
        Room suite = new SuiteRoom(3, 60, 150.0);

        System.out.println(single);
        System.out.println("Available: " + inventory.getAvailability("SingleRoom") + "\n");

        System.out.println(dbl);
        System.out.println("Available: " + inventory.getAvailability("DoubleRoom") + "\n");

        System.out.println(suite);
        System.out.println("Available: " + inventory.getAvailability("SuiteRoom") + "\n");

        System.out.println("Current Inventory Status:");
        inventory.displayInventory();

        // Search
        SearchService searchService = new SearchService(inventory);
        System.out.println("\nAvailable Rooms for Booking:");
        searchService.displayAvailableRooms(single, dbl, suite);

        // Queue
        BookingQueue bookingQueue = new BookingQueue();

        System.out.println("\nSubmitting Booking Requests...");
        bookingQueue.addRequest(new Reservation("Alice", "SingleRoom"));
        bookingQueue.addRequest(new Reservation("Bob", "DoubleRoom"));
        bookingQueue.addRequest(new Reservation("Charlie", "SuiteRoom"));
        bookingQueue.addRequest(new Reservation("Diana", "SingleRoom"));

        System.out.println("\nCurrent Booking Queue:");
        bookingQueue.displayQueue();

        // ✅ Use Case 6
        BookingService bookingService = new BookingService(inventory);
        bookingService.processBookings(bookingQueue);
        bookingService.displayAllocations();

        System.out.println("\nUpdated Inventory After Booking:");
        inventory.displayInventory();

        // ✅ Use Case 7: Add-On Services
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        Reservation temp = new Reservation("Eve", "SingleRoom");
        String resId = bookingService.confirmAndReturnId(temp);

        if (resId != null) {
            serviceManager.addService(resId, new AddOnService("Breakfast", 10.0));
            serviceManager.addService(resId, new AddOnService("WiFi", 5.0));
            serviceManager.addService(resId, new AddOnService("Airport Pickup", 20.0));

            serviceManager.displayServices(resId);

            double total = serviceManager.calculateTotalCost(resId);
            System.out.println("Total Add-On Cost: $" + total);
        }

        System.out.println("\nApplication terminating...");
    }
}

// ✅ Reservation
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

// ✅ Booking Queue
class BookingQueue {
    private Queue<Reservation> queue;

    public BookingQueue() {
        queue = new LinkedList<>();
    }

    public void addRequest(Reservation reservation) {
        queue.offer(reservation);
        System.out.println("Request added -> " + reservation);
    }

    public Reservation processNext() {
        return queue.poll();
    }

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

// ✅ Booking Service
class BookingService {
    private RoomInventory inventory;
    private Map<String, Set<String>> allocatedRooms;
    private Set<String> allAllocatedRoomIds;
    private Map<String, Integer> roomCounters;

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
        this.allocatedRooms = new HashMap<>();
        this.allAllocatedRoomIds = new HashSet<>();
        this.roomCounters = new HashMap<>();
    }

    public void processBookings(BookingQueue queue) {
        System.out.println("\nProcessing Booking Requests...\n");
        Reservation request;

        while ((request = queue.processNext()) != null) {
            confirmReservation(request);
        }
    }

    private void confirmReservation(Reservation request) {
        String roomType = request.getRoomType();
        int available = inventory.getAvailability(roomType);

        if (available <= 0) {
            System.out.println("❌ Booking Failed for " + request.getGuestName());
            return;
        }

        String roomId = generateUniqueRoomId(roomType);

        allocatedRooms.putIfAbsent(roomType, new HashSet<>());
        allocatedRooms.get(roomType).add(roomId);
        allAllocatedRoomIds.add(roomId);

        inventory.updateAvailability(roomType, available - 1);

        System.out.println("✅ Booking Confirmed -> Guest: " +
                request.getGuestName() + ", Room ID: " + roomId);
    }

    // 🔥 NEW METHOD for Use Case 7
    public String confirmAndReturnId(Reservation request) {
        String roomType = request.getRoomType();
        int available = inventory.getAvailability(roomType);

        if (available <= 0) return null;

        String roomId = generateUniqueRoomId(roomType);

        allocatedRooms.putIfAbsent(roomType, new HashSet<>());
        allocatedRooms.get(roomType).add(roomId);
        allAllocatedRoomIds.add(roomId);

        inventory.updateAvailability(roomType, available - 1);

        System.out.println("✅ Booking Confirmed -> Guest: " +
                request.getGuestName() + ", Room ID: " + roomId);

        return roomId;
    }

    private String generateUniqueRoomId(String roomType) {
        int count = roomCounters.getOrDefault(roomType, 0) + 1;
        roomCounters.put(roomType, count);

        String roomId = roomType + "-" + count;

        while (allAllocatedRoomIds.contains(roomId)) {
            count++;
            roomCounters.put(roomType, count);
            roomId = roomType + "-" + count;
        }

        return roomId;
    }

    public void displayAllocations() {
        System.out.println("\nAllocated Rooms:");
        for (Map.Entry<String, Set<String>> entry : allocatedRooms.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}

// ✅ Add-On Service
class AddOnService {
    private String serviceName;
    private double cost;

    public AddOnService(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return serviceName + " ($" + cost + ")";
    }
}

// ✅ Add-On Service Manager
class AddOnServiceManager {
    private Map<String, List<AddOnService>> serviceMap;

    public AddOnServiceManager() {
        serviceMap = new HashMap<>();
    }

    public void addService(String reservationId, AddOnService service) {
        serviceMap.putIfAbsent(reservationId, new ArrayList<>());
        serviceMap.get(reservationId).add(service);

        System.out.println("Service added -> " + service +
                " for Reservation: " + reservationId);
    }

    public double calculateTotalCost(String reservationId) {
        List<AddOnService> services = serviceMap.get(reservationId);
        if (services == null) return 0.0;

        double total = 0;
        for (AddOnService s : services) {
            total += s.getCost();
        }
        return total;
    }

    public void displayServices(String reservationId) {
        List<AddOnService> services = serviceMap.get(reservationId);

        if (services == null || services.isEmpty()) {
            System.out.println("No add-on services for " + reservationId);
            return;
        }

        System.out.println("Services for " + reservationId + ":");
        for (AddOnService s : services) {
            System.out.println("- " + s);
        }
    }
}

// ✅ Inventory
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
        }
    }

    public void displayInventory() {
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(entry.getKey() + " -> Available: " + entry.getValue());
        }
    }
}

// ✅ Search Service
class SearchService {
    private RoomInventory inventory;

    public SearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

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

// Abstract Room
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