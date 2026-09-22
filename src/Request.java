public class Request {

    public int id;
    /**
     * <p>Id of place where request sits.</p>
     */
    public int position;
    /**
     * <p>True when request is done or request is not done on deadline.</p>
     */
    public boolean finished;

    //time is counted since beginning of simulation
    /**
     * <p>Time when request occurs.</p>
     */
    public int arrivalTime;
    /**
     * <p>Time when request finished. -1 means deadline was reached and request not done.</p>
     */
    public int finishTime;
    /**
     * <p>/Moment of time until which request has to be done.</p>
     */
    public int deadline;

    public Request(){
        this.deadline = -1;//basic request has no deadline
        this.finished = false;
    }

    /**
     * <p>Function called for every request to check if request is done because of current head position,
     * or request reaches deadline.</p>
     */
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
