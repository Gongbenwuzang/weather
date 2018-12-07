package com.example.tnt.weather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBaseCity extends SQLiteOpenHelper {
    static String name = "cityManagerDataBase.db";        //数据库名
    static int version = 1;                     //没什么用
    private ContentValues cv;                   // 插入对象
    private List<String> allCityInfo_data;       // 查询到的所有城市名称
    //    private String[] allCityInfo_data;          // 查询到的所有城市名称
    private SQLiteDatabase database;                        // 数据库操作对象类

    public DataBaseCity(Context context) {
        super(context, name, null, version);
        database = this.getReadableDatabase();

    }

    //创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table cityNameTable(cityName varchar(20) primary key)";
        db.execSQL(sql);
    }

    //更新数据库
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // 向数据库添加新的城市，检查以前有没有添加过，添加成功返回 1，添加失败返回 -1
    public void addToDataBaseCity(String cityName) {


        database.beginTransaction();
        try {

            cv = new ContentValues();
            cv.put("cityName", cityName);
            database.insert("cityNameTable", null, cv);

            database.setTransactionSuccessful();
        } finally {
            //关闭事务
            //如果此时已经设置事务执行成功，则sql语句生效，否则不生效
            database.endTransaction();
        }


    }

    // 删除数据库里的城市
    public void deleteCityName(String cityName) {
        // 开启事务
        database.beginTransaction();
        try {

            database.delete("cityNameTable", "cityName = ?", new String[]{
                    cityName
            });

            database.setTransactionSuccessful();
        } finally {

            //关闭事务
            //如果此时已经设置事务执行成功，则sql语句生效，否则不生效
            database.endTransaction();
        }

    }

    // 查找数据库里存储的所有城市，返回一个字符串数据
    public List<String> findAllCity() {


        allCityInfo_data = new ArrayList<>();

        database.beginTransaction();

        try {
            String sql = "select * from cityNameTable";
            Cursor cursor = database.rawQuery(
                    sql, null
            );

            while (cursor.moveToNext())  //这一行有数据
            {

                allCityInfo_data.add(cursor.getString(cursor.getColumnIndex("cityName")));
                System.out.println("cursor.moveToNext:" + cursor.getString(cursor.getColumnIndex("cityName")));

            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            return allCityInfo_data;
        }
    }


    //查询里面是否有数据
    public long findAllCityLength() {
        Cursor cursor = database.rawQuery("select count(*)from cityNameTable",null);
        cursor.moveToFirst();
        Long count = cursor.getLong(0);
        cursor.close();
        return count;
    }
}
