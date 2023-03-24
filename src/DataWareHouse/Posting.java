package DataWareHouse;

import java.io.Serializable;

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








