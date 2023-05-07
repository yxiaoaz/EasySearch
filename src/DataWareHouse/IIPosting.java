package DataWareHouse;

import DataWareHouse.Posting;

import java.util.ArrayList;

/**
 * Term ID-> ArrayList<IIPosting>
 * Record the positions of the key(term) in a specific doc
 * */
public class IIPosting extends Posting {

    // the position of terms in this document
    private ArrayList<Integer> positions;

    /**ID: the id of the DOCUMENT
     * */
    public IIPosting(String ID) {
        super(ID);
        positions = new ArrayList<>();
    }

    public void addPosition(int position){
        positions.add(position);
    }
    public ArrayList<Integer> getPositions(){return positions;}
}