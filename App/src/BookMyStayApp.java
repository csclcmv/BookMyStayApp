import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("🚀 Concurrent Booking Simulation Started\n");

        // Shared Inventory
        RoomInventory inventory = new RoomInventory();
        inventory.addRoomType("SingleRoom", 2);
        inventory.addRoomType("DoubleRoom", 1);

        BookingHistory history = new BookingHistory();

        BookingService bookingService = new BookingService(inventory, history);

        // Shared Queue
        BookingQueue queue = new BookingQueue();

        // Simulating multiple guests
        queue.addRequest(new Reservation("Alice", "SingleRoom"));
        queue.addRequest(new Reservation("Bob", "SingleRoom"));
        queue.addRequest(new Reservation("Charlie", "SingleRoom")); // may fail
        queue.addRequest(new Reservation("David", "DoubleRoom"));
        queue.addRequest(new Reservation("Eve", "DoubleRoom")); // may fail

        // ✅ Multiple Threads (Concurrent Users)
        Thread t1 = new Thread(new BookingProcessor(queue, bookingService), "T1");
        Thread t2 = new Thread(new BookingProcessor(queue, bookingService), "T2");
        Thread t3 = new Thread(new BookingProcessor(queue, bookingService), "T3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("\n📜 Final Booking History:");
        history.displayHistory();

        System.out.println("\n📊 Final Inventory:");
        inventory.displayInventory();

        System.out.println("\n✅ System remained consistent under concurrency!");
    }
}

// ======================= THREAD PROCESSOR =======================

class BookingProcessor implements Runnable {
    private BookingQueue queue;
    private BookingService service;

    public BookingProcessor(BookingQueue queue, BookingService service) {
        this.queue = queue;
        this.service = service;
    }

    public void run() {
        while (true) {
            Reservation r = queue.getNextRequest();

            if (r == null) break;

            service.safeProcess(r, Thread.currentThread().getName());
        }
    }
}

// ======================= VALIDATOR =======================

class BookingValidator {
    public static void validate(Reservation r, RoomInventory inv)
            throws Exception {

        if (r == null)
            throw new Exception("Reservation null");

        if (!inv.hasRoomType(r.getRoomType()))
            throw new Exception("Invalid Room Type");

        if (inv.getAvailability(r.getRoomType()) <= 0)
            throw new Exception("No Availability");
    }
}

// ======================= MODELS =======================

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

// ======================= THREAD-SAFE QUEUE =======================

class BookingQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public synchronized void addRequest(Reservation r) {
        queue.offer(r);
    }

    public synchronized Reservation getNextRequest() {
        return queue.poll();
    }
}

// ======================= HISTORY =======================

class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public synchronized void addReservation(Reservation r) {
        history.add(r);
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

    private Map<String, Integer> counters = new HashMap<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    // ✅ THREAD-SAFE PROCESSING
    public void safeProcess(Reservation r, String threadName) {
        try {
            BookingValidator.validate(r, inventory);

            // ✅ CRITICAL SECTION
            synchronized (inventory) {

                int available = inventory.getAvailability(r.getRoomType());

                if (available <= 0) {
                    System.out.println(threadName + " ❌ No rooms for " + r);
                    return;
                }

                String id = generateId(r.getRoomType());

                inventory.updateAvailability(r.getRoomType(), available - 1);

                history.addReservation(r);

                System.out.println(threadName + " ✅ Booked " + r + " ID=" + id);
            }

        } catch (Exception e) {
            System.out.println(threadName + " ❌ Error: " + e.getMessage());
        }
    }

    private synchronized String generateId(String type) {
        int c = counters.getOrDefault(type, 0) + 1;
        counters.put(type, c);
        return type + "-" + c;
    }
}

// ======================= INVENTORY =======================

class RoomInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public synchronized void addRoomType(String type, int count) {
        inventory.put(type, count);
    }

    public synchronized boolean hasRoomType(String type) {
        return inventory.containsKey(type);
    }

    public synchronized int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public synchronized void updateAvailability(String type, int newCount) {
        if (newCount < 0)
            throw new RuntimeException("Negative inventory!");
        inventory.put(type, newCount);
    }

    public void displayInventory() {
        System.out.println(inventory);
    }
}