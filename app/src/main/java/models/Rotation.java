package models;
/**
 * Created by wc on 6/7/2015.
 */
public class Rotation {
    private double last;
    private double _new;
    private double change;
    private long time;
    private String photo;

    public double getLast() {
        return last;
    }

    public void setLast(double last) {
        this.last = last;
    }

    public double get_new() {
        return _new;
    }

    public void set_new(double _new) {
        this._new = _new;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
