package DataWareHouse;

import java.io.Serializable;
import java.util.ArrayList;

public class Posting implements Serializable {
    protected String ID;

    public Posting(String ID){
        this.ID=ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Posting){
            return ID.equals(((Posting) obj).getID());
        }
        return false;
    }

    public String getID() {
        return ID;
    }
}

/**
 * Term ID-> ArrayList<IIPosting>
 *
 * */
class IIPosting extends Posting{

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
}

class FIPosting extends Posting{

    public FIPosting(String ID) {
        super(ID);
    }
}

