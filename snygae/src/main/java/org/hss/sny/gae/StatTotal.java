package org.hss.sny.gae;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by sk on 12/21/14.
 */
@Entity
public class StatTotal {

    @Id
    Long id;

    private int maxAge;
    private int males;
    private int females;
    private int mcount;
    private int fcount;

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMales() {
        return males;
    }

    public void setMales(int males) {
        this.males = males;
    }

    public int getFemales() {
        return females;
    }

    public void setFemales(int females) {
        this.females = females;
    }

    public int getMcount() {
        return mcount;
    }

    public void setMcount(int mcount) {
        this.mcount = mcount;
    }

    public int getFcount() {
        return fcount;
    }

    public void setFcount(int fcount) {
        this.fcount = fcount;
    }
}
