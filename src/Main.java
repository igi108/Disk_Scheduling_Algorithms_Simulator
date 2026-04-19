import java.util.*;

//percentage 0.0 - 1.0

//one disk-head move is one time unit

public class Main {

    static final int seed = 123456789;

    //total amount of disk positions
    static final int diskSize = 1000;
    //total amount of requests created during one simulation
    static final int totalRequests = 5000;
    //requests arrival time (min time of simulation)
    static final int totalArrivalTime = 100000;


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

    private static void generateRequests(){
        Random random = new Random(seed);
        int requestsGenerated = 0;
        array = new Request[totalRequests];

        int notClutteredRequests = (int)Math.round(totalRequests * (1.0 - clutterPercentage));

        //generate basic requests
        for (int i = 0; i < notClutteredRequests; i++){
            Request request= new Request();
            request.arrivalTime = randomInt(0, totalArrivalTime, random);
            request.id = requestsGenerated;
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
        int requestsPerClutter = clutteredRequests / numberOfClutters;
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

    static void main(String[] args) {

        FCFS();
        SSTF();
        SCAN();
        G_SCAN();
        EDF();
        FD_SCAN();

    }

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

                    int min = Math.min(timeUntil, Math.abs(deltaPosition));
                    time += min;
                    if (request.position > headPosition) {
                        headPosition += min;
                    } else {
                        headPosition -= min;
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
        //todo get results
    }

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
            if (list.contains(closestRequest)) {
                newClosestHasToBeFound = false;
            }else {
                newClosestHasToBeFound = true;
            }

            //check if simulation is done
            if(lastAddedId == totalRequests && list.isEmpty()){
                break;
            }

        }
        //todo get results

    }

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

        //todo get results
    }

    private static void G_SCAN(){

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

        //todo get results
    }

    //when there are deadline requests: only earliest deadline is done (tries to be done), when there are none, C_SCAN
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

        //todo get results
    }

    //when there are deadline requests: earliest deadline is done (that can still be done)
    // and all other requests on its way.
    // when there are none real-time, C_SCAN
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

        //todo get results
    }

    //array of requests. There are all the requests, even ones not already "created"
    static Request[] array;

    private static int randomInt(int min, int max, Random random){
        return random.nextInt(max - min) + min;
    }
}
