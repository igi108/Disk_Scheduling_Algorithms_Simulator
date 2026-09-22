import java.util.*;

//percentage 0.0 - 1.0

//one disk-head move is one time unit

public class Main {

    static final int seed = 123456789;

    /**
     * <p>Total amount of disk space. This is also number of positions that disk's head can reach.</p>
     */
    static int diskSize = 2000;
    /**
     * <p>Total amount of requests created during simulation of one algorithm.</p>
     */
    static int totalRequests = 3000;
    /**
     * <p>Request arrives between time=0 and this number.</p>
     */
    static int totalArrivalTime = 20000;


    /**
     * <p>Percentage of cluttered requests (many reads/writes in small space).</p>
     * <b>Large percentage causes head to stay in small space in some algorithms.</b>
     */
    static double clutterPercentage = 0.2;
    /**
     * <p>Number of clutter groups (number of big file reads/writes in small space). Assuming the disk
     * is not fragmented, parts of file will be close to each other.</p>
     */
    static int numberOfClutters = 6;
    /**
     * <p>Radius of clutter in disk space.
     * Distance between middle of clutter to its furthest request.</p>
     */
    static int maxClutterDistance = 80;


    /**
     * <p>Percentage of requests with deadlines (applies only to basic requests, not clutters meaning clutter has
     * no deadline requests).</p>
     */
    static double deadlinesPercentage = 0.08;
    /**
     * <p>Max available time for request's deadline.</p>
     */
    static int maxDeadline = 500;
    /**
     * <p>Min available time for request's deadline.</p>
     */
    static int minDeadline = 20;

    static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        //test normal system usage
        testAllAlgorithms();

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
        }

    }

    /**
     * <p>Performs all algorithms for chosen inputs.</p>
     */
    private static void testAllAlgorithms(){
        printMainReport();
        FCFS();
        SSTF();
        SCAN();
        C_SCAN();
        EDF();
        FD_SCAN();
    }

    /**
     * <p>Generates requests in order based on input seed. Every call of this function generates
     * exact same requests.</p>
     */
    private static void generateRequests(){
        Random random = new Random(seed);
        int requestsGenerated = 0;
        array = new Request[totalRequests];

        int notClutteredRequests = (int)Math.round(totalRequests * (1.0 - clutterPercentage));

        //generate basic requests
        for (int i = 0; i < notClutteredRequests; i++){
            Request request= new Request();
            request.arrivalTime = randomInt(0, totalArrivalTime, random);
            request.position = randomInt(0, diskSize, random);

            array[requestsGenerated] = request;
            requestsGenerated ++;
        }

        int requestsWithDeadlines = (int)Math.round(deadlinesPercentage * totalRequests);
        int i = 0;
        //set deadlines only for basic requests
        while (i < requestsWithDeadlines){
            int randRequest = randomInt(0, requestsGenerated, random);
            Request request = array[randRequest];
            if(request.deadline == -1){
                request.deadline = request.arrivalTime + randomInt(minDeadline, maxDeadline, random);
                i++;
            }
        }

        int clutteredRequests = (int)Math.round(totalRequests * clutterPercentage);
        int requestsPerClutter = Math.max(1, clutteredRequests / numberOfClutters);
        //generate clutters - equal distribution in time
        for (int clutter = 0; clutter < numberOfClutters; clutter++){

            int clutterArrivalTime = randomInt(0, totalArrivalTime, random);
            int clutterPosition = randomInt(0, diskSize, random);//middle of clutter on disk

            //generate requests in clutter
            for (int j = 0; j < requestsPerClutter; j++){
                Request request = new Request();
                request.arrivalTime = clutterArrivalTime;
                array[requestsGenerated] = request;
                requestsGenerated ++;

                int position = clutterPosition + randomInt(- maxClutterDistance, maxClutterDistance, random);
                if(position > diskSize) position = diskSize;
                if(position < 0) position = 0;
                request.position = position;
            }
        }
        Arrays.sort(array, Comparator.comparingInt(a -> a.arrivalTime));
    }

    /**
     * <p>FIRST COME FIRST SERVE. Requests are done in order of their arrival</p>
     */
    private static void FCFS(){
        generateRequests();
        int totalMoves = 0;
        int time = 0;
        int headPosition = diskSize / 2;

        int lastAddedId = 0;
        List<Request> list = new ArrayList<>(totalRequests);

        while (true){
            //add created requests
            while (lastAddedId < totalRequests && array[lastAddedId].arrivalTime <= time){
                list.add(array[lastAddedId]);
                lastAddedId ++;
            }

            //move to first request arrived (FIFO)
            if(!list.isEmpty()){
                Request request = list.getFirst();
                int previousPosition = headPosition;
                //when no deadline (move until request finished)
                if(request.deadline == -1){
                    headPosition = request.position;
                    time += Math.abs(previousPosition - headPosition);

                }else {//when there is deadline, move as long as request exists, or until it is finished
                    int timeUntil = request.deadline - time;//time until request deletes itself
                    int deltaPosition = request.position - previousPosition;

                    if(timeUntil < Math.abs(deltaPosition)){
                        //if cannot be reached before deadline, move as long as request exist and then delete it
                        time += timeUntil;
                        if (request.position > headPosition) {
                            headPosition += timeUntil;
                        } else {
                            headPosition -= timeUntil;
                        }
                        request.finishTime = -1;
                        list.remove(request);
                    }else {
                        time += Math.abs(deltaPosition);
                        headPosition += deltaPosition;
                    }
                }
                totalMoves += Math.abs(previousPosition - headPosition);

            }else {
                //when there is no request, time passes. jump to next request
                if (lastAddedId < totalRequests) {
                    time = array[lastAddedId].arrivalTime;
                }
            }

            //update all requests to delete realtime out of deadline requests, and finished request
            for (Request request: list){
                request.update(time, headPosition);
            }
            list.removeIf(request -> request.finished);

            //check if simulation is done
            if(lastAddedId == totalRequests && list.isEmpty()){
                break;
            }
        }
        printAlgorithmReport("FCFS", time, totalMoves);
    }

    /**
     * <p>SHORTEST SEEK TIME FIRST. Request closest to the head is done first. In case of another request arriving,
     * that is closer to the head, new one is chosen as the closest.</p>
     */
    private static void SSTF(){

        generateRequests();

        int time = 0;
        int totalMoves = 0;
        int headPosition = diskSize / 2;

        //here will be all waiting requests
        List<Request> list = new ArrayList<>(totalRequests);
        //here will be all still waiting deadline requests
        List<Request> deadlineList = new ArrayList<>((int)Math.round(deadlinesPercentage * totalRequests));
        int lastAddedId = 0;

        boolean newClosestHasToBeFound = true;
        Request closestRequest = null;
        int closestDistance;

        while (true){

            //add created requests
            while (lastAddedId < totalRequests && array[lastAddedId].arrivalTime <= time){
                Request request = array[lastAddedId];
                list.add(request);
                if(request.deadline != -1){
                    deadlineList.add(request);
                }
                lastAddedId ++;
                newClosestHasToBeFound = true;
            }

            //move to current closest request
            if(!list.isEmpty()){
                //search for the closest if new was added
                if(newClosestHasToBeFound){
                    closestRequest = list.getFirst();
                    closestDistance = Math.abs(closestRequest.position - headPosition);
                    int distance;
                    for (Request request: list){
                        distance = Math.abs(request.position - headPosition);
                        if(distance < closestDistance){
                            closestRequest = request;
                            closestDistance = Math.abs(closestRequest.position - headPosition);
                        }
                    }
                }
                //move to the closest
                if(closestRequest.position > headPosition) headPosition ++;
                if(closestRequest.position < headPosition) headPosition --;

                time ++;
                totalMoves ++;
                closestRequest.update(time, headPosition);
                if(closestRequest.finished){
                    list.remove(closestRequest);
                    deadlineList.remove(closestRequest);
                    closestRequest = null;
                }

            }else {//if at the moment no requests exist, jump to next
                if (lastAddedId < totalRequests) {
                    time = array[lastAddedId].arrivalTime;
                } else {
                    break;
                }
            }
            //update all deadline requests to delete out of deadline requests
            for (Request request: deadlineList){
                request.update(time, headPosition);
            }
            deadlineList.removeIf(request -> request.finished);//delete out od deadline requests
            list.removeIf(request -> request.finished);//delete out od deadline requests
            if (closestRequest != null && list.contains(closestRequest)) {
                newClosestHasToBeFound = false;
            }else {
                newClosestHasToBeFound = true;
            }

            //check if simulation is done
            if(lastAddedId == totalRequests && list.isEmpty()){
                break;
            }

        }
        printAlgorithmReport("SSTF", time, totalMoves);

    }

    /**
     * <p>SCAN. Head is moving in one direction doing all requests on it's path
     * until it reaches end of space, then head moves opposite direction.</p>
     */
    private static void SCAN(){

        generateRequests();

        int time = 0;
        int headPosition = diskSize / 2;
        int velocity = 1;
        int totalMoves = 0;

        //here will be all waiting requests
        List<Request> list = new ArrayList<>(totalRequests);
        int lastAddedId = 0;

        while (true){
            //add created requests
            while (lastAddedId < totalRequests && array[lastAddedId].arrivalTime <= time){
                Request request = array[lastAddedId];
                list.add(request);
                lastAddedId ++;
            }

            //move head
            if(headPosition == 0) velocity = 1;
            if(headPosition == diskSize - 1) velocity = -1;
            headPosition += velocity;
            time ++;
            totalMoves ++;

            //update all requests on current head position.
            for (Request request: list){
                request.update(time, headPosition);
            }

            //delete out of deadline requests and finished requests
            list.removeIf(request -> request.finished);//delete finished

            //check if simulation is done
            if(lastAddedId == totalRequests && list.isEmpty()){
                break;
            }
        }

        printAlgorithmReport("SCAN", time, totalMoves);
    }

    /**
     * <p>CIRCULAR SCAN. Head is moving in one direction doing all requests on it's path
     * until it reaches end of space, then head moves to beginning of space instantly without doing any
     * requests on it's way back.</p>
     */
    private static void C_SCAN(){

        generateRequests();

        int time = 0;
        int headPosition = diskSize / 2;
        int totalMoves = 0;

        //here will be all waiting requests
        List<Request> list = new ArrayList<>(totalRequests);
        int lastAddedId = 0;

        while (true){
            //add created requests
            while (lastAddedId < totalRequests && array[lastAddedId].arrivalTime <= time){
                Request request = array[lastAddedId];
                list.add(request);
                lastAddedId ++;
            }

            //move head, if it reaches end of disk space, move it instantly to beginning
            if(headPosition == diskSize) headPosition = -1;//move to -1 so next position would be id=0
            headPosition ++;
            time ++;
            totalMoves ++;

            //update all requests on current head position.
            for (Request request: list){
                request.update(time, headPosition);
            }

            //delete out of deadline requests and finished requests
            list.removeIf(request -> request.finished);//delete finished

            //check if simulation is done
            if(lastAddedId == totalRequests && list.isEmpty()){
                break;
            }
        }

        printAlgorithmReport("C-SCAN", time, totalMoves);
    }

    /**
     * <p>EARLIEST DEADLINE FIRST. When there are no deadline requests, algorithm works as C-SCAN.
     * When there are deadline requests, head tries to do the earliest deadline one (also does every other request on
     * head's path), without checking if head can even reach it on time.</p>
     * <b>If deadlines are too small, this algorithm will still try to do the earliest one, which might result in none
     * of deadline request being done.</b>
     */
    private static void EDF(){

        generateRequests();

        int time = 0;
        int headPosition = diskSize / 2;
        int totalMoves = 0;

        //here will be all no deadline waiting requests
        List<Request> list = new ArrayList<>(totalRequests);
        List<Request> deadlineList = new ArrayList<>((int)Math.round(deadlinesPercentage * totalRequests));
        int lastAddedId = 0;

        Request earliestDeadlineRequest;
        int earliestDeadline;

        while (true){
            //add created requests
            while (lastAddedId < totalRequests && array[lastAddedId].arrivalTime <= time){
                Request request = array[lastAddedId];
                if(request.deadline != -1){
                    deadlineList.add(request);
                }else {
                    list.add(request);
                }
                lastAddedId ++;
            }

            //choose algorithm
            if(deadlineList.isEmpty()){
                //move head, if it reaches end of disk space, move it instantly to beginning
                if(headPosition == diskSize - 1) headPosition = -1;//-1 so next would be id=0
                headPosition ++;
            }
            else {
                //if there are any deadline requests: find earliest, but do not check if it can be done on time

                //find earliest deadline request
                earliestDeadlineRequest = deadlineList.getFirst();
                earliestDeadline = earliestDeadlineRequest.deadline;
                for (Request request : deadlineList){
                    if(request.deadline < earliestDeadline){
                        earliestDeadlineRequest = request;
                        earliestDeadline = earliestDeadlineRequest.deadline;
                    }
                }

                //move to the earliest
                if(earliestDeadlineRequest.position > headPosition) headPosition ++;
                if(earliestDeadlineRequest.position < headPosition) headPosition --;
            }
            totalMoves ++;
            time ++;

            //update requests based on used algorithm
            if(deadlineList.isEmpty()){
                //update basic requests on current head position.
                for (Request request: list){
                    request.update(time, headPosition);
                }
                //delete finished requests
                list.removeIf(request -> request.finished);
            }
            else {
                //update deadline requests
                for (Request request: deadlineList){
                    request.update(time, headPosition);
                }
                //delete finished request and out of deadline requests
                deadlineList.removeIf(request -> request.finished);
            }

            //check if simulation is done
            if(lastAddedId == totalRequests && list.isEmpty() && deadlineList.isEmpty()){
                break;
            }
        }

        printAlgorithmReport("EDF", time, totalMoves);
    }

    /**
     * <p>FEASIBLE DEADLINE SCAN. When there are no deadline requests, algorithm works as C-SCAN.
     * When there are deadline requests, head moves to the earliest deadline one that can still be done
     * (and also does every other request on head's path)</p>
     * <b>Algorithm will filter out deadline requests and will not try to reach ones with too close deadline.</b>
     */
    private static void FD_SCAN(){

        generateRequests();

        int time = 0;
        int headPosition = diskSize / 2;
        int totalMoves = 0;

        //here will be all no deadline waiting requests
        List<Request> list = new ArrayList<>(totalRequests);
        //here will be all deadline waiting requests that can still be finished (other ones are discarded)
        List<Request> deadlineList = new ArrayList<>((int)Math.round(deadlinesPercentage * totalRequests));
        int lastAddedId = 0;

        Request earliestDeadlineRequest;
        int earliestDeadline;

        while (true){
            //add created requests
            while (lastAddedId < totalRequests && array[lastAddedId].arrivalTime <= time){
                Request request = array[lastAddedId];
                if(request.deadline != -1){
                    deadlineList.add(request);
                }else {
                    list.add(request);
                }
                lastAddedId ++;
            }

            //choose algorithm and move by one unit
            if(deadlineList.isEmpty()){
                //move head, if it reaches end of disk space, move it instantly to beginning
                if(headPosition == diskSize - 1) headPosition = -1;//-1 so next would be id=0
                headPosition ++;
            }
            else {
                //remove all impossible to finish in time requests
                final int currentTime = time;//for lambda
                final int currentHeadPosition = headPosition;//for lambda
                for (Request request: deadlineList){
                    request.update(time, headPosition);
                    request.finishTime = -1;
                }
                deadlineList.removeIf(request ->
                        currentTime + Math.abs(request.position - currentHeadPosition) > request.deadline
                );

                //search for earliest deadline if there are any left
                if(!deadlineList.isEmpty()){
                    //search
                    earliestDeadlineRequest = deadlineList.getFirst();
                    earliestDeadline = earliestDeadlineRequest.deadline;
                    for (Request request : deadlineList){
                        if(request.deadline < earliestDeadline){
                            earliestDeadlineRequest = request;
                            earliestDeadline = earliestDeadlineRequest.deadline;
                        }
                    }

                    //move to the closest
                    if(earliestDeadlineRequest.position > headPosition) headPosition ++;
                    if(earliestDeadlineRequest.position < headPosition) headPosition --;
                }
                else {
                    //move head, if it reaches end of disk space, move it instantly to beginning
                    if(headPosition == diskSize - 1) headPosition = -1;//-1 so next would be id=0
                    headPosition ++;
                }
            }
            time ++;
            totalMoves ++;


            //update all requests on its way
            for (Request request: list){
                request.update(time, headPosition);
            }
            for (Request request: deadlineList){
                request.update(time, headPosition);
            }

            //delete finished requests
            list.removeIf(request -> request.finished);
            deadlineList.removeIf(request -> request.finished);

            //check if simulation is done
            if(lastAddedId == totalRequests && list.isEmpty() && deadlineList.isEmpty()){
                break;
            }
        }

        printAlgorithmReport("FD-SCAN", time, totalMoves);
    }

    /**
     * <p>Array of past and future requests. There are all the requests, even ones not already waiting for
     * the disk.</p>
     */
    static Request[] array;

    /**
     * <p>Generates random int value (inclusive).</p>
     */
    private static int randomInt(int min, int max, Random random){
        return random.nextInt(max - min) + min;
    }

    /**
     * <p>Prints configuration of inputs before starting simulation.</p>
     */
    private static void printMainReport() {
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
    private static void printAlgorithmReport(String algorithmName, int simulationTime, int totalMoves) {
        long totalTimeWaiting = 0;
        int minTimeWaiting = Integer.MAX_VALUE;
        int maxTimeWaiting = 0;

        int realTimeRequestsNumber = 0;
        int missedDeadlines = 0;
        int finishedRequests = 0;

        for (Request request : array) {
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
        for (Request request : array) {
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
