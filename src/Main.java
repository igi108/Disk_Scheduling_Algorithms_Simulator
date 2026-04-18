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

    }

    static void main(String[] args) {

        generateRequests();
        
    }
    //array of requests. There are all the requests, even ones not already "created"
    static Request[] array;

    private static int randomInt(int min, int max, Random random){
        return random.nextInt(max - min) + min;
    }
}
