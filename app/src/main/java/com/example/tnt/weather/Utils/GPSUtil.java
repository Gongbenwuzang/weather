package com.example.tnt.weather.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

public class GPSUtil {
    private LocationManager locationManager;                // 定位管理器
    private Location location;                              // 位置对象
    private AppCompatActivity context;
    private String locationCurrent;                         // 当前位置 城市
    private String locationCurrentCountry;                         // 当前位置国家
    private String locationCurrentQu;                         // 当前位置区
    private String locationCurrentStreet;                         // 当前位置街道
    private double latitudeCurrent;                                    //纬度
    private double longitudeCurrent;                                    //经度


    public GPSUtil(AppCompatActivity context){
        this.context = context;
    }
//    当前位置所在的城市
    public String getLocationCurrent() {
        return locationCurrent;
    }

    //开启定位功能，获取当前城市信息
    public void locationFunction() {

        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) context.getSystemService(serviceName);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
//         如果没有定位权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//                ActivityCompat#requestPermissions
//             here to request the missing permissions, and then overriding
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                      int[] grantResults)
//             to handle the case where the user grants the permission. See the documentation
//             for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        updateWithNewLocation(location);
        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);


    }

    // 定位监听器
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };


    //    更新位置信息
    private void updateWithNewLocation(Location location) {

       try {
                Thread.sleep(0);//因为真机获取gps数据需要一定的时间，为了保证获取到，采取系统休眠的延迟方法
        } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
        }


        if(location != null){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latitudeCurrent = lat;
            longitudeCurrent = lng;
            Geocoder geocoder=new Geocoder(context);       // Android 自带位置信息 提供者
            List places = null;

            try {
                places = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (places != null && places.size() > 0) {
//                当前城市
                locationCurrent = ((Address)places.get(0)).getLocality();
                //一下的信息将会具体到某条街
                //其中getAddressLine(0)表示国家，getAddressLine(1)表示精确到某个区，getAddressLine(2)表示精确到具体的街
                locationCurrentCountry=((Address) places.get(0)).getAddressLine(0);
                locationCurrentQu = ((Address) places.get(0)).getAddressLine(1);
                locationCurrentStreet = ((Address) places.get(0)).getAddressLine(2);

            }
        }else{
//            提示获取失败  城市信息获取出错
        }
        if(locationCurrent == null){
            locationCurrent = ErrorCode.LOCATION_ERROR_CODE;
        }
    }

    // 当前位置国家
    public String getLocationCurrentCountry() {
        return locationCurrentCountry;
    }

    // 当前位置所在区
    public String getLocationCurrentQu() {
        return locationCurrentQu;
    }

    // 当前位置街道
    public String getLocationCurrentStreet() {
        return locationCurrentStreet;
    }
    //纬度
    public double getLatitudeCurrent() {
        return latitudeCurrent;
    }
    //经度
    public double getLongitudeCurrent() {
        return longitudeCurrent;
    }

}
