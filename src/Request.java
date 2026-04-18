public class Request {

    public int id;
    public int position;//id of place where request sits
    public boolean finished;

    //time is counted since beginning of simulation
    public int arrivalTime;//time of request creation
    public int finishTime;//time of request finish. null means request can still be finished
    public int deadline;//moment of time until which request has to be done

    public Request(){
        this.deadline = -1;//basic request has no deadline
        this.finished = false;
    }

    public void update(int currentTime, int headPosition){

        if(headPosition == position){
            finishTime = currentTime;
            finished = true;
            return;
        }
        if(deadline != -1 && currentTime > deadline){
            finishTime = -1;
            finished = true;
        }

    }
}
