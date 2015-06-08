package sg.edu.sit.nyp.contentschecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import models.Rotation;

/**
 * Created by wc on 6/7/2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ccapp";

    public static final String ROTATION_TABLE_NAME = "rotations";
    public static final String ROTATION_COLUMN_LAST = "last";
    public static final String ROTATION_COLUMN_NEW = "new";
    public static final String ROTATION_COLUMN_ID = "id";
    public static final String ROTATION_COLUMN_CHANGE = "change";
    public static final String ROTATION_COLUMN_TIME = "time";
    public static final String ROTATION_COLUMN_PHOTO = "photo";

    public DBHelper(Context context){
       super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ROTATION_TABLE_NAME+" ("+
                   ROTATION_COLUMN_ID+ " integer primary key, "+
                   ROTATION_COLUMN_LAST+" text ,"+
                   ROTATION_COLUMN_NEW+" text ,"+
                   ROTATION_COLUMN_CHANGE+" text ,"+
                   ROTATION_COLUMN_TIME+" text ,"+
                   ROTATION_COLUMN_PHOTO+" text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ROTATION_TABLE_NAME);
        onCreate(db);
    }
    public boolean insertRotation(Rotation r){
        double last = r.getLast();
        double _new = r.get_new();
        double change = r.getChange();
        long time = r.getTime();
        String photo = r.getPhoto();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ROTATION_COLUMN_LAST, last);
        cv.put(ROTATION_COLUMN_NEW, _new);
        cv.put(ROTATION_COLUMN_CHANGE, change);
        cv.put(ROTATION_COLUMN_TIME, time);
        cv.put(ROTATION_COLUMN_PHOTO, photo);
        db.insert(ROTATION_TABLE_NAME, null, cv);
        return true;
    }
    public ArrayList<Rotation> getAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = new String[]{ ROTATION_COLUMN_LAST, ROTATION_COLUMN_NEW, ROTATION_COLUMN_CHANGE, ROTATION_COLUMN_TIME, ROTATION_COLUMN_PHOTO};

        ArrayList<Rotation> list = new ArrayList<Rotation>();
        Cursor c = db.query(ROTATION_TABLE_NAME, columns, null, null, null , null, null);
        if(c != null){
            if(c.moveToFirst()){
                do{
                    String last = c.getString(c.getColumnIndex(ROTATION_COLUMN_LAST));
                    String _new = c.getString(c.getColumnIndex(ROTATION_COLUMN_NEW));
                    String change = c.getString(c.getColumnIndex(ROTATION_COLUMN_CHANGE));
                    String time = c.getString(c.getColumnIndex(ROTATION_COLUMN_TIME));
                    String photo = c.getColumnName(c.getColumnIndex(ROTATION_COLUMN_PHOTO));

                    Rotation r = new Rotation();
                    r.setLast(Double.parseDouble(last));
                    r.set_new(Double.parseDouble(_new));
                    r.setChange(Double.parseDouble(change));
                    r.setTime(Long.parseLong(time));
                    r.setPhoto(photo);

                    list.add(r);
                }while(c.moveToNext());
            }
        }
        return list;
    }
}
