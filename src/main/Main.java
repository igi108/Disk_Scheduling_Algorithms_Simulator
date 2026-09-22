package main;

import java.util.*;
import static tools.Algorithms.*;
import static main.Settings.*;
import static tools.Printer.printSettings;

public class Main {

    static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        //test normal system usage
        diskSize = 2000;
        totalRequests = 3000;
        totalArrivalTime = 20000;
        clutterPercentage = 0.2;
        numberOfClutters = 6;
        maxClutterDistance = 80;
        deadlinesPercentage = 0.08;
        minDeadline = 20;
        maxDeadline = 500;
        testAllAlgorithms();
        System.out.println("\nPress ENTER to continue...");

        //test random chaos
        if(scanner.hasNextLine()){
            diskSize = 2000;
            totalRequests = 6000;
            totalArrivalTime = 50000;
            clutterPercentage = 0.02;
            numberOfClutters = 1;
            maxClutterDistance = 200;
            deadlinesPercentage = 0.02;
            minDeadline = 10;
            maxDeadline = 50;
            testAllAlgorithms();
            System.out.println("\nPress ENTER to continue...");
            scanner.nextLine();
        }

        //test many system files
        if(scanner.hasNextLine()){
            diskSize = 2000;
            totalRequests = 3000;
            totalArrivalTime = 15000;
            clutterPercentage = 0.60;
            numberOfClutters = 4;
            maxClutterDistance = 40;
            deadlinesPercentage = 0.05;
            minDeadline = 50;
            maxDeadline = 300;
            testAllAlgorithms();
            System.out.println("\nPress ENTER to continue...");
            scanner.nextLine();
        }

    }

    /**
     * <p>Performs all algorithms for chosen inputs.</p>
     */
    private static void testAllAlgorithms(){
        printSettings();
        FCFS();
        SSTF();
        SCAN();
        C_SCAN();
        EDF();
        FD_SCAN();
    }
}
