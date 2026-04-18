public class Request {

    public int id;
    public int position;//id of place where request sits

    //time is counted since beginning of simulation
    public int arrivalTime;//time of request creation
    public int finishTime;
    public int deadline;//moment of time until which request has to be done

    public Request(){
        this.finishTime = -1;//request not finished
        this.deadline = -1;//basic request has no deadline
    }

    public void update(int currentTime, int headPosition){

        if(headPosition == position){
            finishTime = currentTime;
        }
        if(currentTime > deadline){
            finishTime = -1;
        }

    }
}
