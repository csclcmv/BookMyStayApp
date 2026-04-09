import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to Hotel Booking System\n");

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("SingleRoom", 5);
        inventory.addRoomType("DoubleRoom", 3);
        inventory.addRoomType("SuiteRoom", 2);

        // Booking History
        BookingHistory history = new BookingHistory();

        // Booking Service (with validation)
        BookingService bookingService = new BookingService(inventory, history);

        // Queue
        BookingQueue queue = new BookingQueue();

        // ✅ Valid + Invalid Inputs
        queue.addRequest(new Reservation("Alice", "SingleRoom"));
        queue.addRequest(new Reservation("Bob", "InvalidRoom")); // ❌ invalid
        queue.addRequest(new Reservation("Charlie", "SuiteRoom"));

        bookingService.processBookings(queue);

        // Reporting
        System.out.println("\nBooking History:");
        history.displayHistory();

        BookingReportService report = new BookingReportService(history);
        report.generateSummaryReport();

        System.out.println("\nApplication continues safely after errors ✅");
    }
}

// ======================= EXCEPTIONS =======================

// Base exception
class InvalidBookingException extends Exception {
    public InvalidBookingException(String msg) {
        super(msg);
    }
}

// Invalid room type
class InvalidRoomTypeException extends InvalidBookingException {
    public InvalidRoomTypeException(String roomType) {
        super("Invalid Room Type: " + roomType);
    }
}

// No availability
class NoAvailabilityException extends InvalidBookingException {
    public NoAvailabilityException(String roomType) {
        super("No rooms available for: " + roomType);
    }
}

// ======================= VALIDATOR =======================

class BookingValidator {

    public static void validate(Reservation r, RoomInventory inventory)
            throws InvalidBookingException {

        if (r == null) {
            throw new InvalidBookingException("Reservation cannot be null");
        }

        if (r.getGuestName() == null || r.getGuestName().isEmpty()) {
            throw new InvalidBookingException("Guest name is required");
        }

        if (!inventory.hasRoomType(r.getRoomType())) {
            throw new InvalidRoomTypeException(r.getRoomType());
        }

        int available = inventory.getAvailability(r.getRoomType());
        if (available <= 0) {
            throw new NoAvailabilityException(r.getRoomType());
        }
    }
}

// ======================= CORE CLASSES =======================

class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public String toString() {
        return guestName + " -> " + roomType;
    }
}

class BookingQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation processNext() {
        return queue.poll();
    }
}

// ======================= HISTORY =======================

class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public void addReservation(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllReservations() {
        return history;
    }

    public void displayHistory() {
        for (Reservation r : history) {
            System.out.println(r);
        }
    }
}

// ======================= BOOKING SERVICE =======================

class BookingService {
    private RoomInventory inventory;
    private BookingHistory history;

    private Map<String, Set<String>> allocatedRooms = new HashMap<>();
    private Map<String, Integer> counters = new HashMap<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void processBookings(BookingQueue queue) {
        Reservation r;

        while ((r = queue.processNext()) != null) {
            try {
                // ✅ VALIDATION (fail-fast)
                BookingValidator.validate(r, inventory);

                confirmReservation(r);

            } catch (InvalidBookingException e) {
                // ✅ Graceful failure
                System.out.println("❌ Booking Error: " + e.getMessage());
            }
        }
    }

    private void confirmReservation(Reservation r) {
        String type = r.getRoomType();
        int available = inventory.getAvailability(type);

        String id = generateId(type);

        allocatedRooms.putIfAbsent(type, new HashSet<>());
        allocatedRooms.get(type).add(id);

        // Safe update
        inventory.updateAvailability(type, available - 1);

        history.addReservation(r);

        System.out.println("✅ Confirmed: " + r + " | ID=" + id);
    }

    private String generateId(String type) {
        int c = counters.getOrDefault(type, 0) + 1;
        counters.put(type, c);
        return type + "-" + c;
    }
}

// ======================= REPORT =======================

class BookingReportService {
    private BookingHistory history;

    public BookingReportService(BookingHistory history) {
        this.history = history;
    }

    public void generateSummaryReport() {
        System.out.println("\n📊 Summary Report");

        Map<String, Integer> map = new HashMap<>();

        for (Reservation r : history.getAllReservations()) {
            map.put(r.getRoomType(),
                    map.getOrDefault(r.getRoomType(), 0) + 1);
        }

        for (String type : map.keySet()) {
            System.out.println(type + " booked: " + map.get(type));
        }
    }
}

// ======================= INVENTORY =======================

class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public void addRoomType(String type, int count) {
        inventory.put(type, count);
    }

    public boolean hasRoomType(String type) {
        return inventory.containsKey(type);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void updateAvailability(String type, int newCount) {
        if (newCount < 0) {
            throw new RuntimeException("Inventory cannot be negative!");
        }
        inventory.put(type, newCount);
    }
}