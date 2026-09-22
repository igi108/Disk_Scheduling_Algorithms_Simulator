package tools;

import static main.Settings.*;

public class Printer {

    /**
     * <p>Prints configuration of inputs before starting simulation.</p>
     */
    public static void printSettings() {
        System.out.println("=====================================================");
        System.out.println("            DISK SIMULATION CONFIGURATION");
        System.out.println("=====================================================");
        System.out.printf("   - %-35s %d\n", "Disk size (blocks):", diskSize);
        System.out.printf("   - %-35s %d\n", "Total number of requests:", totalRequests);
        System.out.printf("   - %-35s %d\n", "Max request creation time:", totalArrivalTime);
        System.out.println("-----------------------------------------------------");
        System.out.printf("   - %-35s %.1f%%\n", "Clustered requests:", clutterPercentage * 100);
        System.out.printf("   - %-35s %d\n", "Number of clusters:", numberOfClutters);
        System.out.printf("   - %-35s %d\n", "Max cluster radius:", maxClutterDistance);
        System.out.println("-----------------------------------------------------");
        System.out.printf("   - %-35s %.1f%%\n", "Real-Time requests:", deadlinesPercentage * 100);
        System.out.printf("   - %-35s %d / %d\n", "Deadline time range (Min/Max):", minDeadline, maxDeadline);
        System.out.println("=====================================================\n");
    }

    /**
     * <p>Prints summary of single algorithm result.</p>
     */
    public static void printAlgorithmReport(String algorithmName, int simulationTime, int totalMoves) {
        long totalTimeWaiting = 0;
        int minTimeWaiting = Integer.MAX_VALUE;
        int maxTimeWaiting = 0;

        int realTimeRequestsNumber = 0;
        int missedDeadlines = 0;
        int finishedRequests = 0;

        for (Request request : Algorithms.array) {
            boolean isRealTime = (request.deadline != -1);
            if (isRealTime) realTimeRequestsNumber++;

            //if finishTime is -1, request was timed out
            if (request.finishTime == -1) {
                missedDeadlines++;
                continue;
            }

            int requestWaitTime = request.finishTime - request.arrivalTime;
            totalTimeWaiting += requestWaitTime;
            finishedRequests ++;

            if (requestWaitTime < minTimeWaiting) minTimeWaiting = requestWaitTime;
            if (requestWaitTime > maxTimeWaiting) maxTimeWaiting = requestWaitTime;
        }

        double averageWaitTime;
        double successRate;
        if(finishedRequests != 0){
            averageWaitTime = (double) totalTimeWaiting / (double) finishedRequests;
        }else {
            averageWaitTime = 0;
        }
        if(realTimeRequestsNumber != 0){
            successRate = 100.0 * ((double) (realTimeRequestsNumber - missedDeadlines) / (double) realTimeRequestsNumber);
        }else {
            successRate = 100.0;
        }

        System.out.println(">>> ALGORITHM REPORT: " + algorithmName);
        System.out.printf("   - %-35s %d\n", "Total simulation time:", simulationTime);
        System.out.printf("   - %-35s %d\n", "Total head movements:", totalMoves);
        System.out.println("   [WAITING STATISTICS]");
        System.out.printf("   - %-35s %.2f\n", "Average waiting time:", averageWaitTime);
        System.out.printf("   - %-35s %d\n", "Minimum waiting time:", minTimeWaiting);
        System.out.printf("   - %-35s %d\n", "Maximum waiting time:", maxTimeWaiting);
        System.out.println("   [REAL-TIME PERFORMANCE]");
        System.out.printf("   - %-35s %.2f%%\n", "Deadline success rate:", successRate);
        System.out.printf("   - %-35s %d out of %d\n", "Missed deadlines:", missedDeadlines, realTimeRequestsNumber);

        printTimeDistribution(averageWaitTime);
        System.out.println("\n");
    }

    /**
     * <p>Prints distribution of requests times in algorithm compared to average for this algorithm.</p>
     */
    private static void printTimeDistribution(double averageWaitingTime) {
        int[] count = new int[5];//amount of requests per ratio

        int finishedRequests = 0;
        //prepare distribution
        for (Request request : Algorithms.array) {
            if (request.finishTime == -1) continue;//do not count unfinished deadlines

            finishedRequests ++;
            int waitTime = request.finishTime - request.arrivalTime;
            double ratio = (double) waitTime / averageWaitingTime;

            if (ratio < 0.5) count[0]++;
            else if (ratio < 1.0) count[1]++;
            else if (ratio < 1.5) count[2]++;
            else if (ratio < 2.0) count[3]++;
            else count[4]++;
        }

        System.out.println("   [WAITING TIME DISTRIBUTION OF FINISHED REQUESTS]");
        String[] descriptions = {
                "Very Fast (<50% avg)",
                "Fast (50-100% avg)",
                "Average (100-150% avg)",
                "Slow (150-200% avg)",
                "Starved (>200% avg)"
        };
        if(finishedRequests == 0){
            System.out.println("0 REQUESTS FINISHED!!!");
        }

        for (int i = 0; i < 5; i++) {
            double percentage = (count[i] * 100.0) / finishedRequests;
            System.out.printf("     %-25s [%5d] %5.1f%% : ", descriptions[i], count[i], percentage);

            for (int j = 0; j < (int) percentage; j++) {
                System.out.print("■");
            }
            System.out.println();
        }
    }
}
