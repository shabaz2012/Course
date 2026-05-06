package gym;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;


enum MembershipStatus {
    /*
        Membership Status is of three types: BRONZE, SILVER and GOLD.
        BRONZE is the default membership a new member gets.
        SILVER and GOLD are paid memberships for the gym.
    */
    BRONZE,
    SILVER,
    GOLD
}

class Workout {
    /**
     * This class represents a single workout session for a member.
     * Each object of the Workout class has a unique ID, as well as
     * a start time and end time that are represented in the number
     * of minutes spent from the start of the day.
     */

    private int id;
    private int startTime;
    private int endTime;

    public Workout(int id, int startTime, int endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return endTime - startTime;
    }
}

class Member {
    /* Data about a gym member.*/
    public int memberId;
    public String name;
    public MembershipStatus membershipStatus;

    public Member(int memberId, String name, MembershipStatus membershipStatus) {
        this.memberId = memberId;
        this.name = name;
        this.membershipStatus = membershipStatus;
    }

    @Override
    public String toString() {
        return "Member ID: " + memberId + ", Name: " + name + ", Membership Status: " + membershipStatus;
    }
}

class Membership {
    /*
        Data for managing a gym membership, and methods which staff can
        use to perform any queries or updates.
    */
    public List<Member> members;
    public Map<Integer, List<Workout>> workOutMap = new HashMap<>();

    public Membership() {
        members = new ArrayList<>();
    }

    public void addMember(Member member) {
        workOutMap.put(member.memberId, new ArrayList<>());
        members.add(member);
    }

    public void updateMembership(int memberId, MembershipStatus membershipStatus) {
        for (Member member : members) {
            if (member.memberId == memberId) {
                member.membershipStatus = membershipStatus;
                break;
            }
        }
    }

    public MembershipStatistics getMembershipStatistics() {
        int totalMembers = members.size();
        int totalPaidMembers = 0;
        for (Member member : members) {
            if (member.membershipStatus == MembershipStatus.GOLD ||
                    member.membershipStatus == MembershipStatus.SILVER
            ) {
                totalPaidMembers++;
            }
        }
        double conversionRate = (totalPaidMembers / (double) totalMembers) * 100.0;
        return new MembershipStatistics(totalMembers, totalPaidMembers, conversionRate);
    }

    public void addWorkout(int id, Workout workout){
        boolean isMemberExists = !members.stream()
                .filter(m -> m.memberId == id)
                .toList().isEmpty();
        if(isMemberExists){
            workOutMap.computeIfAbsent(id, k -> new ArrayList<>()).add(workout);
        }
    }

    public Map<Integer, Double> getAverageWorkoutDurations(){
        Map<Integer, Double> averageWorkouts = new HashMap<>();

        for(int memberId: workOutMap.keySet()){
            List<Workout> workouts = workOutMap.get(memberId);
            double totalTimeWorkout = workouts.stream()
                    .map(w -> w.getEndTime() - w.getStartTime())
                    .mapToDouble(duration -> duration)
                    .sum();
            averageWorkouts.put(memberId, totalTimeWorkout/ workouts.size());

        }
        return averageWorkouts;
    }
    //Map<Integer, Integer> duePayments = testMembership.getDuePayments();

    public Map<Integer, Integer> getDuePayments(){
        Map<Integer, Integer> duePayments = new HashMap<>();
        for(int memberId: workOutMap.keySet()){
            Member member = members.stream().filter(m -> m.memberId == memberId).toList().get(0);
            List<Workout> workouts = workOutMap.get(memberId);

            if (workouts == null || workouts.isEmpty()) {
                duePayments.put(memberId, 0);
            }

            workouts.sort(Comparator.comparing(Workout::getId));
            int freeWorkouts = 0, costPerHour = 0;
            if(member.membershipStatus.equals(MembershipStatus.BRONZE)){
                freeWorkouts = 1;
                costPerHour = 10;
            } else if(member.membershipStatus.equals(MembershipStatus.SILVER)){
                freeWorkouts = 3;
                costPerHour = 8;
            } else if(member.membershipStatus.equals(MembershipStatus.GOLD)){
                freeWorkouts = 5;
                costPerHour = 6;
            }
            int totalCost = 0;
            if(workouts.size() <= freeWorkouts){
                totalCost = 0;
            } else {
                for(Workout workout: workouts){
                    if(freeWorkouts > 0){
                        freeWorkouts--;
                    } else {
                        double duration = workout.getEndTime() - workout.getStartTime();
                        int ceiledDurationInHours = (int)Math.ceil(duration/60);
                        totalCost += ceiledDurationInHours * costPerHour;
                    }
                }
            }
            duePayments.put(memberId, totalCost);
        }

        return duePayments;
    }
}

class MembershipStatistics {
    /*
        Class for returning the getMembershipStatistics result
    */
    public int totalMembers;
    public int totalPaidMembers;
    public double conversionRate;

    public MembershipStatistics(int totalMembers, int totalPaidMembers, double conversionRate) {
        this.totalMembers = totalMembers;
        this.totalPaidMembers = totalPaidMembers;
        this.conversionRate = conversionRate;
    }
}

public class SolutionGym {
    /*
        This is not a complete test suite, but tests some basic functionality of
        the code and shows how to use it.
    */
    public static void main(String[] args) {
        testMember();
        testMembership();
        testGetAverageWorkoutDurations();
        testGetDuePayments();
    }

    public static void testMember() {
        System.out.println("Running testMember");
        Member testMember = new Member(1, "John Doe", MembershipStatus.BRONZE);
        assert testMember.memberId == 1 :
                "Member ID should be 1, was " + testMember.memberId;
        assert testMember.name.equals("John Doe") :
                "Member name should be \"John Doe\", was \"" + testMember.name + "\"";
        assert testMember.membershipStatus == MembershipStatus.BRONZE :
                "Membership status should be BRONZE, was " + testMember.membershipStatus;
    }

    public static void testMembership() {
        System.out.println("Running testMembership");
        Membership testMembership = new Membership();
        Member testMember = new Member(1, "John Doe", MembershipStatus.BRONZE);
        testMembership.addMember(testMember);
        assert testMembership.members.size() == 1 :
                "Members size should be 1, was " + testMembership.members.size();
        assert testMembership.members.get(0).equals(testMember) :
                "First member should equal testMember";

        testMembership.updateMembership(1, MembershipStatus.SILVER);
        assert testMembership.members.get(0).membershipStatus == MembershipStatus.SILVER :
                "Membership status should be SILVER, was " + testMembership.members.get(0).membershipStatus;

        Member testMember2 = new Member(2, "Alex C", MembershipStatus.BRONZE);
        testMembership.addMember(testMember2);

        Member testMember3 = new Member(3, "Marie C", MembershipStatus.GOLD);
        testMembership.addMember(testMember3);

        Member testMember4 = new Member(4, "Joe D", MembershipStatus.SILVER);
        testMembership.addMember(testMember4);

        Member testMember5 = new Member(5, "June R", MembershipStatus.BRONZE);
        testMembership.addMember(testMember5);

        Member testMember6 = new Member(6, "Westley D", MembershipStatus.SILVER);
        testMembership.addMember(testMember6);

        MembershipStatistics attendanceStats = testMembership.getMembershipStatistics();
        assert attendanceStats.totalMembers == 6 :
                "Total members should be 6, was " + attendanceStats.totalMembers;
        assert attendanceStats.totalPaidMembers == 4 :
                "Total paid members should be 4, was " + attendanceStats.totalPaidMembers;
        assert Math.abs(attendanceStats.conversionRate - 66.67) < 0.1 :
                "Conversion rate should be 66.67, was " + attendanceStats.conversionRate;
    }

    public static void testGetAverageWorkoutDurations() {
        System.out.println("Running testGetAverageWorkoutDurations");
        Membership testMembership = new Membership();
        Member testMember1 = new Member(12, "John Doe", MembershipStatus.SILVER);
        testMembership.addMember(testMember1);

        Member testMember2 = new Member(22, "Alex Cleeve", MembershipStatus.BRONZE);
        testMembership.addMember(testMember2);

        Member testMember3 = new Member(31, "Marie Cardiff", MembershipStatus.GOLD);
        testMembership.addMember(testMember3);

        Member testMember4 = new Member(37, "George Costanza", MembershipStatus.SILVER);
        testMembership.addMember(testMember4);

        Workout testWorkout1 = new Workout(11, 10, 20);
        Workout testWorkout2 = new Workout(24, 15, 35);
        Workout testWorkout3 = new Workout(32, 45, 90);
        Workout testWorkout4 = new Workout(47, 100, 155);
        Workout testWorkout5 = new Workout(56, 120, 200);
        Workout testWorkout6 = new Workout(62, 300, 400);
        Workout testWorkout7 = new Workout(78, 1000, 1010);
        Workout testWorkout8 = new Workout(80, 1010, 1045);

        testMembership.addWorkout(12, testWorkout1);
        testMembership.addWorkout(22, testWorkout2);
        testMembership.addWorkout(31, testWorkout3);
        testMembership.addWorkout(12, testWorkout4);
        testMembership.addWorkout(22, testWorkout5);
        testMembership.addWorkout(31, testWorkout6);
        testMembership.addWorkout(12, testWorkout7);
        testMembership.addWorkout(4, testWorkout8);

        Map<Integer, Double> averageDurations = testMembership.getAverageWorkoutDurations();
        assert Math.abs(averageDurations.get(12) - 25.0) < 0.1 :
                "average duration for member 12 should be 25.0, was " + averageDurations.get(12);
        assert Math.abs(averageDurations.get(22) - 50.0) < 0.1 :
                "average duration for member 22 should be 50.0, was " + averageDurations.get(22);
        assert Math.abs(averageDurations.get(31) - 72.5) < 0.1 :
                "average duration for member 31 should be 72.5, was " + averageDurations.get(31);
        assertFalse(averageDurations.containsKey(4));
    }

    public static void testGetDuePayments() {
        System.out.println("Running testGetDuePayments");
        // Test get_due_payments function
        Membership testMembership = new Membership();
        testMembership.addMember(new Member(1, "John Doe", MembershipStatus.BRONZE));
        testMembership.addMember(new Member(2, "Alex C", MembershipStatus.SILVER));
        testMembership.addMember(new Member(3, "Marie C", MembershipStatus.GOLD));

        // Add workouts for members
        Map<Integer, List<Workout>> memberWorkouts = new HashMap<>();
        memberWorkouts.put(1, Arrays.asList(
                new Workout(1, 500, 700), new Workout(10, 300, 350), new Workout(12, 10, 20),
                new Workout(3, 50, 90), new Workout(6, 130, 150), new Workout(15, 900, 920)
        ));
        memberWorkouts.put(2, Arrays.asList(
                new Workout(13, 510, 540), new Workout(14, 600, 700), new Workout(2, 15, 35),
                new Workout(4, 100, 155), new Workout(18, 200, 225), new Workout(8, 1050, 1155)
        ));
        memberWorkouts.put(3, Arrays.asList(
                new Workout(5, 120, 135), new Workout(17, 140, 190), new Workout(9, 210, 255),
                new Workout(11, 400, 450), new Workout(16, 910, 940), new Workout(7, 1000, 1100)
        ));

        for (Map.Entry<Integer, List<Workout>> entry : memberWorkouts.entrySet()) {
            int memberId = entry.getKey();
            List<Workout> workoutList = entry.getValue();
            for (Workout workout : workoutList) {
                testMembership.addWorkout(memberId, workout);
            }
        }

        Map<Integer, Integer> duePayments = testMembership.getDuePayments();
        assert Math.abs(duePayments.get(1) - 50.0) < 0.1 :
                "due payment for member 1 should be 50.0, was " + duePayments.get(1);
        assert Math.abs(duePayments.get(2) - 32.0) < 0.1 :
                "due payment for member 2 should be 32.0, was " + duePayments.get(2);
        assert Math.abs(duePayments.get(3) - 6.0) < 0.1 :
                "due payment for member 3 should be 6.0, was " + duePayments.get(3);

        // Test member with no workouts
        testMembership.addMember(new Member(4, "Ron Burgundy", MembershipStatus.SILVER));
        Map<Integer, Integer> duePayments2 = testMembership.getDuePayments();
        assert Math.abs(duePayments2.get(4) - 0.0) < 0.1 :
                "due payment for member 4 should be 0.0, was " + duePayments2.get(4);
    }
}