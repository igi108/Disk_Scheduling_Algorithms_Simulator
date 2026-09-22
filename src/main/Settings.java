package main;

//percentage 0.0 - 1.0
//one disk-head move is one time unit
public class Settings {
    /**
     * <p>Seed is used to get same created requests for every algorithm. Thanks to that, algorithms
     * can be compared on same input.</p>
     */
    public static final int seed = 123456789;

    /**
     * <p>Total amount of disk space. This is also number of positions that disk's head can reach.</p>
     */
    public static int diskSize = 2000;
    /**
     * <p>Total amount of requests created during simulation of one algorithm.</p>
     */
    public static int totalRequests = 3000;
    /**
     * <p>tools.Request arrives between time=0 and this number.</p>
     */
    public static int totalArrivalTime = 20000;


    /**
     * <p>Percentage of cluttered requests (many reads/writes in small space).</p>
     * <b>Large percentage causes head to stay in small space in some algorithms.</b>
     */
    public static double clutterPercentage = 0.2;
    /**
     * <p>Number of clutter groups (number of big file reads/writes in small space). Assuming the disk
     * is not fragmented, parts of file will be close to each other.</p>
     */
    public static int numberOfClutters = 6;
    /**
     * <p>Radius of clutter in disk space.
     * Distance between middle of clutter to its furthest request.</p>
     */
    public static int maxClutterDistance = 80;


    /**
     * <p>Percentage of requests with deadlines (applies only to basic requests, not clutters meaning clutter has
     * no deadline requests).</p>
     */
    public static double deadlinesPercentage = 0.08;
    /**
     * <p>Max available time for request's deadline.</p>
     */
    public static int maxDeadline = 500;
    /**
     * <p>Min available time for request's deadline.</p>
     */
    public static int minDeadline = 20;
}
