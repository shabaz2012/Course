import java.util.*;

class Course {
    /* Data about a particular course. */
    public String title;  // The name of the obstacle course
    public int obstacleCount;  // The number of obstacles in the course

    public Course(String courseTitle, int obstacles) {
        title = courseTitle;
        obstacleCount = obstacles;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Course)) { return false; }
        Course c = (Course) o;
        return c.title == this.title && c.obstacleCount == this.obstacleCount;
    }

    @Override
    public int hashCode() {
        return (title == null ? 0 : title.hashCode()) * obstacleCount;
    }
}

class Run {
    /* Data and methods about a single run of the obstacle course */
    public Course course; // The Course object this run is for
    public boolean complete; // true if the run is a full run of the course
    // false if the run is in progress or was aborted
    public List<Integer> obstacleTimes; // The times it took to complete each obstacle

    public Run(Course runCourse) {
        course = runCourse;
        complete = false;
        obstacleTimes = new ArrayList<>();
    }

    public void addObstacleTime(int obstacleTime) {
        // When an obstacle is completed, add the time to the current run.
        // Params:
        //   obstacleTime: the time in seconds it took to complete the obstacle
        if(complete) {
            throw new IllegalStateException("Cannot add obstacle to complete run");
        }
        obstacleTimes.add(obstacleTime);
        if(obstacleTimes.size() == course.obstacleCount) {
            complete = true;
        }
    }

    public int getRunTime() {
        // Returns the total time this run has taken.
        // If the run is not complete, it returns the time taken so far.
        System.out.println("Total run : "+obstacleTimes.stream().mapToInt(Integer::intValue).sum());
        return obstacleTimes.stream().mapToInt(Integer::intValue).sum();
    }
}

class RunCollection {
    public Course course; // the Course this RunCollection is for
    public List<Run> runs;  // the Run objects for this particular course

    public RunCollection(Course collectionCourse) {
        course = collectionCourse;
        runs = new ArrayList<>();
    }

    public int getNumRuns() {
        // Returns the number of runs in this collection
        return runs.size();
    }

    public void addRun(Run run) {
        // Adds a run to this collection
        if(!run.course.equals(course)) {
            throw new IllegalArgumentException("run's Course is not the same as the RunCollection's");
        }
        runs.add(run);
    }

    public int personalBest() {
        // Returns the best finish time achieved in this RunCollection
        return runs.stream().filter(v -> v.complete).mapToInt(Run::getRunTime).min().orElse(Integer.MAX_VALUE);
    }

    public int bestOfBests()
    {
        //bestOfBests(
        int cumulativeTime = 0;

        for (int i=0; i<(course.obstacleCount); i++) {
            int minTime = Integer.MAX_VALUE;
            for(Run run: runs) {
                if (i > (run.obstacleTimes.size()- 1)) {
                    continue;
                }
                if (run.obstacleTimes.get(i) < minTime) {
                    minTime = run.obstacleTimes.get(i);
                }
            }
            cumulativeTime += minTime;
        }
        return cumulativeTime;
    }

    public double chanceOfPersonalBest1(Run testRun) {
        double averageTimeCumulative = 0;

        for (int i=testRun.obstacleTimes.size(); i<(course.obstacleCount); i++) {
            double averageTime = 0;
            for(Run run: runs) {
                if (i > (run.obstacleTimes.size()- 1)) {
                    continue;
                }
                averageTime += (double) run.obstacleTimes.get(i) /runs.size();
            }
            averageTimeCumulative += averageTime;
        }
        averageTimeCumulative = averageTimeCumulative/ ((course.obstacleCount - testRun.obstacleTimes.size()) + 1);
        return averageTimeCumulative;
    }

    public double chanceOfPersonalBest(Run currentRun) {
        Random random = new Random();
        int successes = 0;
        int personalBest = personalBest();
        int trials = 10000;
        for (int i=0; i<trials; i++) {
            int totalTime = currentRun.getRunTime();
            for (int obstacleIndex = currentRun.obstacleTimes.size(); obstacleIndex < course.obstacleCount; obstacleIndex++) {
                List<Integer> possibleTimes = new ArrayList<>();
                for (Run run : runs) {
                    if (run.obstacleTimes.size() > obstacleIndex) {
                        possibleTimes.add(run.obstacleTimes.get(obstacleIndex));
                    }
                }
                int randomIndex = random.nextInt(possibleTimes.size());
                totalTime += possibleTimes.get(randomIndex);
            }
            if (totalTime <= personalBest) {
                successes++;
            }
        }
        return (double) successes / trials;
    }
}

public class Solution {
    public static void main(String[] argv) {
        testRun();
        testRunCollection();
        testChanceOfPersonalBest();
    }

    // This is not a complete test suite, but tests some basic functionality of the above code, and
    // shows some examples of using the code.
    public static void testRun() {
        System.out.println("Running testRun");
        Course testCourse = new Course("Test course", 2);
        Run testRun = new Run(testCourse);
        testRun.addObstacleTime(3);
        assert !testRun.complete : "Test run should not be complete";
        testRun.addObstacleTime(5);
        assert testRun.complete : "Test run should be complete";
        assert testRun.obstacleTimes.equals(new ArrayList<Integer>(List.of(3, 5))) :
                "obstacleTimes should be [3, 5], was " + testRun.obstacleTimes;
        assert testRun.getRunTime() == 8 : "getRunTime should return 8, returned " + testRun.getRunTime();
        try {
            testRun.addObstacleTime(4);
            assert false : "adding obstacle time to complete run should throw";
        } catch(IllegalStateException e) {
            // expected
        }
    }

    public static RunCollection makeRunCollection(Course course, int[][] obstacleData) {
        // Create a new RunCollection for test purposes.
        // Params:
        //   course: the Course object this RunCollection is for
        //   obstacleData: an int[][]. Each int[] represents obstacle times for a single
        //                 run of the course.
        RunCollection runCollection = new RunCollection(course);
        for(int[] runData : obstacleData) {
            Run run = new Run(course);
            for(int obstacleTime : runData) {
                run.addObstacleTime(obstacleTime);
            }
            runCollection.addRun(run);
        }
        return runCollection;
    }

    public static void testRunCollection() {
        // Tests basic RunCollection functionality

        //    Obstacles: O1  O2  O3  O4
        //    Run 1:      3   4   5   6    (total: 18 seconds)
        //    Run 2:      4   4   4   5    (total: 17 seconds)
        //    Run 3:      4   5   4   6    (total: 19 seconds)
        //    Run 4:      5   5   3        (13 seconds, but run is incomplete)
        System.out.println("Running testRunCollection");
        int[][] obstacleData = new int[][] {{3, 4, 5, 6},
                {4, 4, 4, 5},
                {4, 5, 4, 6},
                {5, 5, 3}};
        Course testCourse = new Course("Test course", 4);
        RunCollection runCollection = makeRunCollection(testCourse, obstacleData);

        int numRuns = obstacleData.length;
        assert runCollection.getNumRuns() == numRuns : "number of runs should be " + numRuns + ", was " + runCollection.getNumRuns();
        assert runCollection.personalBest() == 17 :
                "personalBest should be 17, was " + runCollection.personalBest();
        assert runCollection.bestOfBests() == 15 :
                "bestOfBests should be 15, was " + runCollection.bestOfBests();
    }

    public static void testChanceOfPersonalBest() {
        System.out.println("Running testChanceOfPersonalBest");
        // Test 1
        int[][] obstacleData = new int[][] {{3, 3, 2},
                {3, 3, 3}};
        Course testCourse = new Course("Test Course", 3);
        RunCollection runCollection = makeRunCollection(testCourse, obstacleData);
        Run testRun = new Run(testCourse);
        testRun.addObstacleTime(3);
        testRun.addObstacleTime(3);
        double chance = runCollection.chanceOfPersonalBest(testRun);
        // The current run has 2 obstacles in it, {3, 3}
        // chance_of_personal_best will run 10,000 trials with to fill in the
        // remaining obstacles, randomly selecting one of 2 or 3 (the times for
        // the third obstacle)
        // The chance of the run being 8 seconds or less is 1/2
        assert .48 <= chance && chance <= .52 : "chance should be between .48 and .52, was " + chance;

        // Test 2
        obstacleData = new int[][] {{3, 3, 2, 3},
                {3, 3, 3, 2},
                {5, 5, 2}};
        testCourse = new Course("Test Course", 4);
        runCollection = makeRunCollection(testCourse, obstacleData);
        testRun = new Run(testCourse);
        testRun.addObstacleTime(3);
        testRun.addObstacleTime(3);
        chance = runCollection.chanceOfPersonalBest(testRun);
        // The current run has 2 obstacles in it, {3, 3}
        // chance_of_personal_best will run 10,000 trials with to fill in the
        // remaining two obstacles, randomly selecting one of:
        // {2, 3, 2} for obstacle 3 (includes the incomplete run)
        // {3, 2}    for obstacle 4
        // The chance of the run being 11 seconds or less is 5/6 ~= .83333
        assert .81333 <= chance && chance <= .85333 : "chance should be between .81333 and .85333, was " + chance;

        // Test 3
        obstacleData = new int[][] {{32, 37},
                {31, 29, 34, 25, 25, 39},
                {25, 34, 38, 24, 26, 39, 33},
                {39, 21, 39, 34, 39, 29, 31, 22, 28, 20},
                {23, 22, 35, 33, 36, 21, 29, 37, 24, 34},
                {28, 34, 28, 22, 40, 28, 31, 33, 25, 20},
                {20, 38, 40, 28, 34, 22},
                {36, 39, 20, 32, 38, 24, 22},
                {40, 20, 21, 37, 32, 30, 40, 25, 37, 30},
                {21, 35, 30, 37, 32, 40, 26, 29, 29}};
        testCourse = new Course("Test Course", 10);
        runCollection = makeRunCollection(testCourse, obstacleData);
        testRun = new Run(testCourse);
        testRun.addObstacleTime(19);
        testRun.addObstacleTime(19);
        testRun.addObstacleTime(19);
        chance = runCollection.chanceOfPersonalBest(testRun);
        assert .92813 <= chance && chance <= .96813 : "chance should be between .92813 and .96813, was " + chance;
    }
}
