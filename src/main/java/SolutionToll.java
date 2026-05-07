import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogEntry {

    /**
     * Represents an entry from a single log line. Log lines look like this in the file:
     *
     * 34400.409 SXY288 210E ENTRY
     *
     * Where:
     * * 34400.409 is the timestamp in seconds since the software was started.
     * * SXY288 is the license plate of the vehicle passing through the toll booth.
     * * 210E is the location and traffic direction of the toll booth. Here, the toll
     *     booth is at 210 kilometers from the start of the tollway, and the E indicates
     *     that the toll booth was on the east-bound traffic side. Tollbooths are placed
     *     every ten kilometers.
     * * ENTRY indicates which type of toll booth the vehicle went through. This is one of
     *     "ENTRY", "EXIT", or "MAINROAD".
     **/

    private final float timestamp;
    private final String licensePlate;
    private final String boothType;
    private final int location;
    private final String direction;

    public LogEntry(String logLine) {
        String[] tokens = logLine.split(" ");
        this.timestamp = Float.parseFloat(tokens[0]);
        this.licensePlate = tokens[1];
        this.boothType = tokens[3];
        this.location =
                Integer.parseInt(tokens[2].substring(0, tokens[2].length() - 1));
        String directionLetter = tokens[2].substring(tokens[2].length() - 1);
        if (directionLetter.equals("E")) {
            this.direction = "EAST";
        } else if (directionLetter.equals("W")) {
            this.direction = "WEST";
        } else {
            throw new IllegalArgumentException();
        }
    }

    public float getTimestamp() {
        return timestamp;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getBoothType() {
        return boothType;
    }

    public int getLocation() {
        return location;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return String.format(
                "<LogEntry timestamp: %f  license: %s  location: %d  direction: %s  booth type: %s>",
                timestamp,
                licensePlate,
                location,
                direction,
                boothType
        );
    }
}

class LogFile {

    /*
     * Represents a file containing a number of log lines, converted to LogEntry
     * objects.
     */

    List<LogEntry> logEntries;

    public LogFile(BufferedReader reader) throws IOException {
        this.logEntries = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            LogEntry logEntry = new LogEntry(line.strip());
            this.logEntries.add(logEntry);
            line = reader.readLine();
        }
    }

    public LogEntry get(int index) {
        return this.logEntries.get(index);
    }

    public int size() {
        return this.logEntries.size();
    }

    public int countJourneys(){
        Set<String> set=new HashSet<>();
        int totalJourneys = 0;
        for(LogEntry entry:logEntries){
            String booth1=entry.getBoothType();
            String carNo=entry.getLicensePlate();
            if(booth1.equals("ENTRY")){
                set.add(carNo);
            }else if(booth1.equals("EXIT")){
                if(set.contains(carNo)){
                    totalJourneys++;
                }
            }
        }
        System.out.println("total Journeys: "+totalJourneys);
        return totalJourneys;

    }

    public List<String> catchSpeeders() {
        List<LogEntry> logEntrySorted = logEntries.stream().sorted(Comparator.comparing(LogEntry::getTimestamp)).toList();

        return null;
    }
}

public class SolutionToll {

    public static void main(String[] argv) throws IOException {
        testLogFile();
        testLogEntry();
        testCountJourneys();
    }

    public static void testLogFile() throws IOException {
        System.out.println("Running testLogFile");
        try (
                BufferedReader reader = new BufferedReader(
                        new FileReader("src/main/resources/content/test/tollbooth_small.log")
                );
        ) {
            LogFile logFile = new LogFile(reader);
            assertEquals(9, logFile.size());
            for (LogEntry entry : logFile.logEntries) {
                assert (entry instanceof LogEntry);
            }
        }
    }

    public static void testLogEntry() {
        System.out.println("Running testLogEntry");
        String logLine = "44776.619 KTB918 310E MAINROAD";
        LogEntry logEntry = new LogEntry(logLine);
        assertEquals(44776.619f, logEntry.getTimestamp(), 0.0001);
        assertEquals("KTB918", logEntry.getLicensePlate());
        assertEquals(310, logEntry.getLocation());
        assertEquals("EAST", logEntry.getDirection());
        assertEquals("MAINROAD", logEntry.getBoothType());
        logLine = "52160.132 ABC123 400W ENTRY";
        logEntry = new LogEntry(logLine);
        assertEquals(52160.132f, logEntry.getTimestamp(), 0.0001);
        assertEquals("ABC123", logEntry.getLicensePlate());
        assertEquals(400, logEntry.getLocation());
        assertEquals("WEST", logEntry.getDirection());
        assertEquals("ENTRY", logEntry.getBoothType());
    }

    public static void testCountJourneys() throws IOException {
        System.out.println("Running testCountJourneys");
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/content/test/tollbooth_small.log"))) {
            LogFile logFile = new LogFile(reader);
            assertEquals(3, logFile.countJourneys());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/content/test/tollbooth_medium.log"))) {
            LogFile logFile = new LogFile(reader);
            assertEquals(3, logFile.countJourneys());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void testCatchSpeeders() throws IOException {
        System.out.println("Running testCatchSpeeders");
        try (BufferedReader reader = new BufferedReader(new FileReader("/content/test/tollbooth_speeders.log"))) {
            LogFile logFile = new LogFile(reader);
            List<String> ticketList = logFile.catchSpeeders();
            // ticketList should be a list similar to
            // ["TST002", "TST003", "TST003"]
            // In this case, TST002 had one journey with unsafe driving, and
            // TST003 had two journeys with unsafe driving. The license plates
            // may be in any order.
            Map<String, Integer> ticketCounts = new HashMap<>();
            for (String ticket : ticketList) {
                ticketCounts.put(ticket, ticketCounts.getOrDefault(ticket, 0) + 1);
            }
            assertEquals(1, (int) ticketCounts.get("TST002"));
            assertEquals(2, (int) ticketCounts.get("TST003"));
            assertEquals(2, ticketCounts.size());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader("/content/test/tollbooth_medium.log"))) {
            LogFile logFile = new LogFile(reader);
            List<String> ticketList = logFile.catchSpeeders();
            assertEquals(10, ticketList.size());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("/content/test/tollbooth_long.log"))) {
            LogFile logFile = new LogFile(reader);
            List<String> ticketList = logFile.catchSpeeders();
            assertEquals(129, ticketList.size());
        }
    }
}