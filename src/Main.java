import java.util.Random;

//percentage 0.0 - 1.0
public class Main {

    static final int seed = 123456789;

    //total amount of disk positions
    static final int diskSize = 1000;
    //total amount of requests created during one simulation
    static final int totalRequests = 5000;
    //requests arrival time (min time of simulation)
    static final int totalArrivalTime = 10000;


    //percentage of cluttered requests (many reads/writes in one place)
    static final double clutterPercentage = 0.1;
    //number of clutter groups (number of big file reads/writes in one place)
    static final int numberOfClutters = 5;
    //distance between middle of clutter to its furthest request;
    static final int maxClutterDistance = 50;


    //percentage of requests with deadlines (applies only to basic requests, not clutters)
    static final double deadlinesPercentage = 0.05;
    //max, min available time for request deadline
    static final int maxDeadline = 500;
    static final int minDeadline = 100;

    static void main(String[] args) {

    }
    //array of requests. There are all the requests, even ones not already "created"
    static Request[] array;

    private static int randomInt(int min, int max, Random random){
        return random.nextInt(max - min) + min;
    }
}
