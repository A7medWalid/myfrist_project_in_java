import java.time.*;
import java.util.*;

// Enum for vehicle types
enum VehicleType {
    CAR, BIKE, TRUCK
}

// Custom exception for invalid tickets
class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) {
        super(message);
    }
}

// Class representing a vehicle
class Vehicle {
    VehicleType type;
    String registration;
    String color;

    public Vehicle(VehicleType type, String registration, String color) {
        this.type = type;
        this.registration = registration;
        this.color = color;
    }
}

// Ticket class to manage parking tickets
class ParkingTicket {
    String ticketId;
    String vehiclePlate;
    LocalDateTime entryTime;

    public ParkingTicket(String ticketId, String vehiclePlate) {
        this.ticketId = ticketId;
        this.vehiclePlate = vehiclePlate;
        this.entryTime = LocalDateTime.now();
    }

    public String getDetails() {
        return "Ticket ID: " + ticketId + "\nEntry Time: " + entryTime + "\nVehicle Plate: " + vehiclePlate;
    }
}

// Class representing a single parking slot
class ParkingSlot {
    int floorNumber;
    int slotNumber;
    VehicleType type;
    boolean isOccupied;
    Vehicle vehicle;
    ParkingTicket ticket;

    public ParkingSlot(int floorNumber, int slotNumber, VehicleType type) {
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
        this.type = type;
        this.isOccupied = false;
    }

    public void parkVehicle(Vehicle vehicle, ParkingTicket ticket) {
        this.vehicle = vehicle;
        this.ticket = ticket;
        this.isOccupied = true;
    }

    public void unParkVehicle() {
        this.vehicle = null;
        this.ticket = null;
        this.isOccupied = false;
    }

    public String getDetails() {
        if (isOccupied) {
            return "Floor: " + floorNumber + ", Slot: " + slotNumber +
                   "\nVehicle Type: " + vehicle.type +
                   "\nRegistration: " + vehicle.registration +
                   "\nColor: " + vehicle.color +
                   "\n" + ticket.getDetails();
        } else {
            return "Floor: " + floorNumber + ", Slot: " + slotNumber + " is EMPTY.";
        }
    }
}

// Class to manage all parking slots
class ParkingSlotManager {
    Map<Integer, Map<Integer, ParkingSlot>> slots;

    public ParkingSlotManager(int totalFloors, int slotsPerFloor) {
        slots = new HashMap<>();
        for (int i = 1; i <= totalFloors; i++) {
            Map<Integer, ParkingSlot> floorSlots = new HashMap<>();
            for (int j = 1; j <= slotsPerFloor; j++) {
                VehicleType type = (j == 1) ? VehicleType.TRUCK : (j <= 3) ? VehicleType.BIKE : VehicleType.CAR;
                floorSlots.put(j, new ParkingSlot(i, j, type));
            }
            slots.put(i, floorSlots);
        }
    }

    public ParkingSlot findAvailableSlot(VehicleType type) {
        for (Map<Integer, ParkingSlot> floorSlots : slots.values()) {
            for (ParkingSlot slot : floorSlots.values()) {
                if (slot.type == type && !slot.isOccupied) {
                    return slot;
                }
            }
        }
        return null;
    }

    public ParkingSlot findSlotByTicket(String ticketId) {
        for (Map<Integer, ParkingSlot> floorSlots : slots.values()) {
            for (ParkingSlot slot : floorSlots.values()) {
                if (slot.isOccupied && slot.ticket.ticketId.equals(ticketId)) {
                    return slot;
                }
            }
        }
        throw new InvalidTicketException("Ticket not found: " + ticketId);
    }

    public void displaySlotStatus() {
        for (Map<Integer, ParkingSlot> floorSlots : slots.values()) {
            for (ParkingSlot slot : floorSlots.values()) {
                System.out.println(slot.getDetails());
            }
        }
    }
}

// Main ParkingLot class
class ParkingLot {
    private final String parkingLotId;
    private final ParkingSlotManager slotManager;
    private final Map<VehicleType, Double> rates;

    public ParkingLot(String parkingLotId, int totalFloors, int slotsPerFloor) {
        this.parkingLotId = parkingLotId;
        this.slotManager = new ParkingSlotManager(totalFloors, slotsPerFloor);

        this.rates = Map.of(
            VehicleType.CAR, 10.0,
            VehicleType.BIKE, 5.0,
            VehicleType.TRUCK, 20.0
        );
    }

    public void parkVehicle(VehicleType type, String registration, String color) {
        ParkingSlot slot = slotManager.findAvailableSlot(type);
        if (slot != null) {
            String ticketId = generateTicketId(slot.floorNumber, slot.slotNumber);
            ParkingTicket ticket = new ParkingTicket(ticketId, registration);
            slot.parkVehicle(new Vehicle(type, registration, color), ticket);

            System.out.println("\n✅ Vehicle parked successfully!");
            System.out.println(slot.getDetails());
            System.out.println("Hourly Rate: $" + rates.get(type));
        } else {
            System.out.println("❌ No available slots for vehicle type: " + type);
        }
    }

    public void unParkVehicle(String ticketId) {
        ParkingSlot slot = slotManager.findSlotByTicket(ticketId);
        System.out.println("\n✅ Vehicle details before un-parking:");
        System.out.println(slot.getDetails());

        double fee = Duration.between(slot.ticket.entryTime, LocalDateTime.now())
                          .toHours() * Math.max(1, rates.get(slot.type));
        slot.unParkVehicle();

        System.out.println("Total Fee: $" + fee);
    }
    public void displayOccupiedAndEmptySlots() {
        System.out.println("🚗 Parking Slot Details:");
        for (Map<Integer, ParkingSlot> floorSlots : slotManager.slots.values()) {
           for (ParkingSlot slot : floorSlots.values()) {
            System.out.println(slot.getDetails());
           }
        }
    }

    private String generateTicketId(int floor, int slot) {
        return parkingLotId + "_" + floor + "_" + slot;
    }

    public void displayAllSlots() {
        slotManager.displaySlotStatus();
    }
}

// Main application
public class Project2 {
    public static void main(String[] args) {
        ParkingLot parkingLot = new ParkingLot("PR123", 3, 10);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
            System.out.println("\n1. Park Vehicle");
            System.out.println("2. Unpark Vehicle");
            System.out.println("3. Display All Slots");
            System.out.println("4. Display Slot Details");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            
                if (choice == 1) {
                    System.out.print("Enter vehicle type (CAR/BIKE/TRUCK): ");
                    VehicleType type = VehicleType.valueOf(scanner.nextLine().toUpperCase());
                    System.out.print("Enter vehicle registration number: ");
                    String regNo = scanner.nextLine();
                    System.out.print("Enter vehicle color: ");
                    String color = scanner.nextLine();
                    parkingLot.parkVehicle(type, regNo, color);

                } else if (choice == 2) {
                    System.out.print("Enter ticket ID: ");
                    String ticketId = scanner.nextLine();
                    parkingLot.unParkVehicle(ticketId);

                } else if (choice == 3) {
                    System.out.println("\nDisplaying All Slots:");
                    parkingLot.displayAllSlots();

                } else if (choice == 4) {
                    System.out.println("\nDisplaying Slot Details:");
                    parkingLot.displayOccupiedAndEmptySlots();

                } else if (choice == 5) {
                    System.out.println("Goodbye!");
                    break;

                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Invalid input, please enter a number between 1-5.");
                scanner.nextLine(); // Clear invalid input
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid vehicle type. Please enter CAR, BIKE, or TRUCK.");
            }
             catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
        scanner.close();
    }
}
