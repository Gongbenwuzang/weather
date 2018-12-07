package com.example.tnt.weather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseLocationCity extends SQLiteOpenHelper {
    static String name = "cityManagerDataBase.db";        //数据库名
    static int version = 1;                     //没什么用
    private ContentValues cv;                   // 插入对象
    private SQLiteDatabase database;                        // 数据库操作对象类
    private String cityLocationName;                            // 表里唯一的字符串 定位到的城市名字

    public DataBaseLocationCity(Context context) {
        super(context, name, null, version);
        database = this.getReadableDatabase();

    }

    //创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table cityNameLocationTable(cityName varchar(20) primary key)";
        db.execSQL(sql);
    }

    //更新数据库
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // 向数据库添加新的城市，检查以前有没有添加过，添加成功返回 1，添加失败返回 -1
    public void addToDataBaseLocationCity(String cityName) {
        database.beginTransaction();
        try {
            cv = new ContentValues();
            cv.put("cityName", cityName);
            database.insert("cityNameLocationTable", null, cv);

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

            database.delete("cityNameLocationTable", "cityName = ?", new String[]{
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
    public String findAllCity() {

        // 开启事务
        database.beginTransaction();
        try {
            cityLocationName = null;  // 添加默认返回字符

            String sql = "select * from cityNameLocationTable";
            Cursor cursor = database.rawQuery(
                    sql, null
            );

            while (cursor.moveToNext())  //这一行有数据
            {
                System.out.println("cursor.moveToNext:" + cursor.getString(cursor.getColumnIndex("cityName")));
                cityLocationName = cursor.getString(cursor.getColumnIndex("cityName"));
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            return cityLocationName;
        }

    }

    //查询里面是否有数据
    public int findAllCityLength() {

        int catCount = 0;
        String sql = "select COUNT(*)  from cityNameLocationTable";

        Cursor cursor = database.rawQuery(
                sql, null
        );
        catCount = cursor.getCount();

        return catCount;

    }
}
