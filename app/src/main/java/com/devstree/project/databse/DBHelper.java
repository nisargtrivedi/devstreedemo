package com.devstree.project.databse;

/**
 * Created by Jitendra on 23,November,2022
 */

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.devstree.project.model.Place;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PlaceLocation.db";
    public static final String PLACE_TABLE_NAME = "place";
    public static final String PLACE_COLUMN_ID = "id";
    public static final String PLACE_COLUMN_PLACENAME = "placeName";
    public static final String PLACE_COLUMN_LATITUDE = "latitude";
    public static final String PLACE_COLUMN_LONGITUDE = "longitude";
    public static final String PLACE_COLUMN_DISTANCE = "distance";
    public static final String PLACE_COLUMN_CITY = "city";
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table "+PLACE_TABLE_NAME+
                        "(id INTEGER  primary key, placeName TEXT,latitude DOUBLE,longitude DOUBLE, distance DOUBLE,city TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+PLACE_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertDistance (String placeName, Double latitude, Double longitude, Double distance,String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLACE_COLUMN_LATITUDE, latitude);
        contentValues.put(PLACE_COLUMN_LONGITUDE, longitude);
        contentValues.put(PLACE_COLUMN_DISTANCE, distance);
        contentValues.put(PLACE_COLUMN_PLACENAME, placeName);
        contentValues.put(PLACE_COLUMN_CITY, city);
        db.insert(PLACE_TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }


    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from place where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, PLACE_TABLE_NAME);
        return numRows;
    }

    public boolean updatePlace (Integer id, String placeName, Double latitude, Double longitude, Double distance, String city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLACE_COLUMN_PLACENAME, placeName);
        contentValues.put(PLACE_COLUMN_LATITUDE, latitude);
        contentValues.put(PLACE_COLUMN_LONGITUDE, longitude);
        contentValues.put(PLACE_COLUMN_DISTANCE, distance);
        contentValues.put(PLACE_COLUMN_CITY, city);
        db.update(PLACE_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteDistance (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PLACE_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }
    public Place getPlace(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(PLACE_TABLE_NAME, new String[] { PLACE_COLUMN_ID,
                        PLACE_COLUMN_PLACENAME, PLACE_COLUMN_LATITUDE,PLACE_COLUMN_LONGITUDE,PLACE_COLUMN_DISTANCE }, PLACE_COLUMN_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Place place=new Place();

        place.setPlaceName(cursor.getString(1));
        place.setLatitude(cursor.getDouble(2));
        place.setLongitude(cursor.getDouble(3));
        place.setDistnace(cursor.getDouble(4));

        return place;
    }
    public ArrayList<Place> getAllPlaceByAscDesc(String asc){
        SQLiteDatabase db=getReadableDatabase();
        ArrayList<Place> placeArrayList=new ArrayList<Place>();
        String query="select * from "+ PLACE_TABLE_NAME+" ORDER BY distance "+asc;
        Cursor c=db.rawQuery(query,null);

        if(c.moveToFirst()){
            do{
                Place place = new Place();
                place.setId(c.getInt(0));
                place.setPlaceName(c.getString(1));
                place.setLatitude(c.getDouble(2));
                place.setLongitude(c.getDouble(3));
                place.setDistnace(c.getDouble(4));
                place.setCityName(c.getString(5));
                Log.d("Distnace--->",""+c.getDouble(4));//but here value getting c.getDouble(4)=0.0
                placeArrayList.add(place);
            }while(c.moveToNext());
        }
        db.close();
        c.close();

        return placeArrayList;
    }
    public ArrayList<Place> getAllPlace(){
        SQLiteDatabase db=getReadableDatabase();
        ArrayList<Place> placeArrayList=new ArrayList<Place>();
        String query="select * from "+ PLACE_TABLE_NAME;
        Cursor c=db.rawQuery(query,null);

        if(c.moveToFirst()){
            do{
                Place place = new Place();
                place.setId(c.getInt(0));
                place.setPlaceName(c.getString(1));
                place.setLatitude(c.getDouble(2));
                place.setLongitude(c.getDouble(3));
                place.setDistnace(c.getDouble(4));
                place.setCityName(c.getString(5));
                placeArrayList.add(place);


            }while(c.moveToNext());

            Log.d("SIZE--->",""+c.getCount()+"--");//but here value getting c.getDouble(4)=0.0

        }
        db.close();
        c.close();

        return placeArrayList;
    }

}

