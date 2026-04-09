import java.io.*;
import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("🔄 System Starting... Attempting Recovery...\n");

        // ✅ Load persisted state
        SystemState state = PersistenceService.loadState();

        RoomInventory inventory;
        BookingHistory history;

        if (state != null) {
            inventory = state.getInventory();
            history = state.getHistory();
            System.out.println("✅ State restored from file\n");
        } else {
            System.out.println("⚠️ No previous state found. Starting fresh.\n");

            inventory = new RoomInventory();
            inventory.addRoomType("SingleRoom", 2);
            inventory.addRoomType("DoubleRoom", 1);

            history = new BookingHistory();
        }

        BookingService bookingService = new BookingService(inventory, history);

        // Queue
        BookingQueue queue = new BookingQueue();
        queue.addRequest(new Reservation("Alice", "SingleRoom"));
        queue.addRequest(new Reservation("Bob", "SingleRoom"));
        queue.addRequest(new Reservation("Charlie", "DoubleRoom"));

        // Threads
        Thread t1 = new Thread(new BookingProcessor(queue, bookingService), "T1");
        Thread t2 = new Thread(new BookingProcessor(queue, bookingService), "T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("\n📜 Booking History:");
        history.displayHistory();

        System.out.println("\n📦 Inventory:");
        inventory.displayInventory();

        // ✅ SAVE STATE BEFORE EXIT
        System.out.println("\n💾 Saving system state...");
        PersistenceService.saveState(new SystemState(inventory, history));

        System.out.println("✅ Shutdown complete. Restart to see recovery.");
    }
}

// ======================= SYSTEM STATE =======================

class SystemState implements Serializable {
    private RoomInventory inventory;
    private BookingHistory history;

    public SystemState(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public RoomInventory getInventory() { return inventory; }
    public BookingHistory getHistory() { return history; }
}

// ======================= PERSISTENCE SERVICE =======================

class PersistenceService {

    private static final String FILE_NAME = "system_state.ser";

    public static void saveState(SystemState state) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            oos.writeObject(state);
            System.out.println("📁 State saved successfully.");

        } catch (IOException e) {
            System.out.println("❌ Error saving state: " + e.getMessage());
        }
    }

    public static SystemState loadState() {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            return (SystemState) ois.readObject();

        } catch (FileNotFoundException e) {
            return null; // first run
        } catch (Exception e) {
            System.out.println("⚠️ Corrupted file. Starting fresh.");
            return null;
        }
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

// ======================= MODELS =======================

class Reservation implements Serializable {
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

// ======================= QUEUE =======================

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

class BookingHistory implements Serializable {
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

    public void safeProcess(Reservation r, String threadName) {
        synchronized (inventory) {

            if (!inventory.hasRoomType(r.getRoomType())) {
                System.out.println(threadName + " ❌ Invalid room");
                return;
            }

            int available = inventory.getAvailability(r.getRoomType());

            if (available <= 0) {
                System.out.println(threadName + " ❌ No availability");
                return;
            }

            String id = generateId(r.getRoomType());

            inventory.updateAvailability(r.getRoomType(), available - 1);
            history.addReservation(r);

            System.out.println(threadName + " ✅ Booked " + r + " ID=" + id);
        }
    }

    private synchronized String generateId(String type) {
        int c = counters.getOrDefault(type, 0) + 1;
        counters.put(type, c);
        return type + "-" + c;
    }
}

// ======================= INVENTORY =======================

class RoomInventory implements Serializable {
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