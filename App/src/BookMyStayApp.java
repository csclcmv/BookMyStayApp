import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to Hotel Booking System\n");

        // Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("SingleRoom", 2);
        inventory.addRoomType("DoubleRoom", 1);

        // History
        BookingHistory history = new BookingHistory();

        // Services
        BookingService bookingService = new BookingService(inventory, history);
        CancellationService cancellationService =
                new CancellationService(inventory, bookingService, history);

        // Queue
        BookingQueue queue = new BookingQueue();
        queue.addRequest(new Reservation("Alice", "SingleRoom"));
        queue.addRequest(new Reservation("Bob", "DoubleRoom"));

        bookingService.processBookings(queue);

        // Capture an ID to cancel
        String cancelId = bookingService.getAnyBookingId();

        // ✅ Perform Cancellation
        if (cancelId != null) {
            System.out.println("\nCancelling Booking ID: " + cancelId);
            cancellationService.cancelBooking(cancelId);
        }

        // Reports
        System.out.println("\nBooking History:");
        history.displayHistory();

        BookingReportService report = new BookingReportService(history);
        report.generateSummaryReport();

        System.out.println("\nSystem stable after cancellation ✅");
    }
}

// ======================= EXCEPTIONS =======================

class InvalidBookingException extends Exception {
    public InvalidBookingException(String msg) { super(msg); }
}

class InvalidRoomTypeException extends InvalidBookingException {
    public InvalidRoomTypeException(String type) {
        super("Invalid Room Type: " + type);
    }
}

class NoAvailabilityException extends InvalidBookingException {
    public NoAvailabilityException(String type) {
        super("No availability for: " + type);
    }
}

class InvalidCancellationException extends Exception {
    public InvalidCancellationException(String msg) { super(msg); }
}

// ======================= VALIDATOR =======================

class BookingValidator {
    public static void validate(Reservation r, RoomInventory inv)
            throws InvalidBookingException {

        if (r == null)
            throw new InvalidBookingException("Reservation is null");

        if (r.getGuestName() == null || r.getGuestName().isEmpty())
            throw new InvalidBookingException("Guest name required");

        if (!inv.hasRoomType(r.getRoomType()))
            throw new InvalidRoomTypeException(r.getRoomType());

        if (inv.getAvailability(r.getRoomType()) <= 0)
            throw new NoAvailabilityException(r.getRoomType());
    }
}

// ======================= MODELS =======================

class Reservation {
    private String guestName;
    private String roomType;
    private boolean cancelled = false;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public void markCancelled() { cancelled = true; }
    public boolean isCancelled() { return cancelled; }

    public String toString() {
        return guestName + " -> " + roomType +
                (cancelled ? " (CANCELLED)" : "");
    }
}

// ======================= QUEUE =======================

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

    private Map<String, Reservation> bookingMap = new HashMap<>();
    private Map<String, String> bookingIdToRoomType = new HashMap<>();
    private Map<String, Integer> counters = new HashMap<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void processBookings(BookingQueue queue) {
        Reservation r;

        while ((r = queue.processNext()) != null) {
            try {
                BookingValidator.validate(r, inventory);
                confirmReservation(r);
            } catch (InvalidBookingException e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
    }

    private void confirmReservation(Reservation r) {
        String type = r.getRoomType();
        int available = inventory.getAvailability(type);

        String id = generateId(type);

        inventory.updateAvailability(type, available - 1);

        bookingMap.put(id, r);
        bookingIdToRoomType.put(id, type);

        history.addReservation(r);

        System.out.println("✅ Confirmed: " + r + " | ID=" + id);
    }

    private String generateId(String type) {
        int c = counters.getOrDefault(type, 0) + 1;
        counters.put(type, c);
        return type + "-" + c;
    }

    public Reservation getReservation(String id) {
        return bookingMap.get(id);
    }

    public String getRoomType(String id) {
        return bookingIdToRoomType.get(id);
    }

    public void removeBooking(String id) {
        bookingMap.remove(id);
        bookingIdToRoomType.remove(id);
    }

    public String getAnyBookingId() {
        for (String id : bookingMap.keySet()) return id;
        return null;
    }
}

// ======================= CANCELLATION =======================

class CancellationService {
    private RoomInventory inventory;
    private BookingService bookingService;
    private BookingHistory history;

    // ✅ Stack for rollback
    private Stack<String> rollbackStack = new Stack<>();

    public CancellationService(RoomInventory inventory,
                               BookingService bookingService,
                               BookingHistory history) {
        this.inventory = inventory;
        this.bookingService = bookingService;
        this.history = history;
    }

    public void cancelBooking(String bookingId) {
        try {
            Reservation r = bookingService.getReservation(bookingId);

            if (r == null)
                throw new InvalidCancellationException("Booking not found");

            if (r.isCancelled())
                throw new InvalidCancellationException("Already cancelled");

            String roomType = bookingService.getRoomType(bookingId);

            // Step 1: Push to rollback stack
            rollbackStack.push(bookingId);

            // Step 2: Restore inventory
            int available = inventory.getAvailability(roomType);
            inventory.updateAvailability(roomType, available + 1);

            // Step 3: Mark cancelled
            r.markCancelled();

            // Step 4: Remove active booking
            bookingService.removeBooking(bookingId);

            System.out.println("↩️ Cancelled: " + bookingId);

        } catch (InvalidCancellationException e) {
            System.out.println("❌ Cancellation Error: " + e.getMessage());
        }
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
            if (!r.isCancelled()) {
                map.put(r.getRoomType(),
                        map.getOrDefault(r.getRoomType(), 0) + 1);
            }
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
        if (newCount < 0)
            throw new RuntimeException("Inventory cannot be negative");
        inventory.put(type, newCount);
    }
}