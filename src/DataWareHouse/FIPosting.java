package DataWareHouse;

import DataWareHouse.Posting;

/**
 * Record the occurence of A term in A doc
 * */
public class FIPosting extends Posting implements Comparable<FIPosting> {
    private int numOfOccurence;
    public FIPosting(String ID) {
        super(ID);
        numOfOccurence=0;
    }

    public void addOccurence(){
        numOfOccurence++;
    }
    public int getOccurence(){
        return numOfOccurence;
    }

    public int compareTo(FIPosting b){
        return b.getOccurence()-this.numOfOccurence;
    }
}