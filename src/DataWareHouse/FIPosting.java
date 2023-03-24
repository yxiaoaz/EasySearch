package DataWareHouse;

import DataWareHouse.Posting;

/**
 * Record the occurence of A term in A doc
 * */
public class FIPosting extends Posting {
    private int occurence;
    public FIPosting(String ID) {
        super(ID);
        occurence=0;
    }

    public void addOccurence(){
        occurence++;
    }
    public int getOccurence(){
        return occurence;
    }

}