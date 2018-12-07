package com.example.tnt.weather;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnt.weather.INTERFACE.HttpCallbackListener;
import com.example.tnt.weather.Utils.HttpUtil;
import com.example.tnt.weather.Utils.StatusBar;
import com.example.tnt.weather.Utils.InfoRegularExpressionGet;
import com.example.tnt.weather.animation.AnimationUtil;
import com.example.tnt.weather.bean.CityBean;
import com.example.tnt.weather.database.DataBaseCity;
import com.example.tnt.weather.Utils.StatusBar;
import com.hdl.myhttputils.MyHttpUtils;
import com.hdl.myhttputils.bean.CommCallback;

import java.util.Calendar;

/**
 * 1. 判断当前数据库里是否有城市信息，如果没有就跳转到 城市管理，城市选择布局，
 * 如果用户始终没有选择，那么就人为退出程序
 */

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    private View activity_main_header;                      // 获取 activity_main_header 视图
    private View activity_main_scroll_weather_info;         // 获取 activity_main_scroll_weather_info 视图
    private TextView showCurrentCityNameTEXT;                // activity_main_header 视图的 settingsIV
    private TextView currentCityTemperatureTEXT;             //  视图的 currentCityTemperatureTEXT
    private TextView currentCityLowHighTemperature;          //  视图的 currentCityLowHighTemperature
    private TextView weather_proposal_TEXT;                  //  视图的 weather_proposal_TEXT 提供建议的组件
    private TextView fengli;                                 //  视图的 fengli 提供风力的组件
    private TextView updateTime;                            //  视图的 updateTime 提供显示更新时间的组件
    private TextView todayDate;                              //  视图的 todayDate 显示今天日期的组件
    private ImageView settingsIV;
    private ImageView cityManagerIV;
    private SwipeRefreshLayout swipeRefreshLayout;            // 刷新布局
    private SharedPreferences sharedPreferences;              // 保存页面上一次显示的城市名称
    private SharedPreferences.Editor editor;                  // 输入状态编辑者
    private String LOCATION_KEY = "LOCATION_KEY";             // 是否已经定位过的键
    private ImageView currentCityWeatherType;                 // 显示今天的天气图片
    private LinearLayout main_container;                      // main_container 设置背景
    private Calendar cal = Calendar.getInstance();             // 当前日期
    private int currentHours = cal.get(Calendar.HOUR_OF_DAY); //当前小时数
    private DataBaseCity dataBaseCity;                      //引入自定义的操作数据库类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始布局参数
        initialCity();


        //获取 activity_main_header 布局
        activity_main_headerFunction();

        //activity_main_header 的点击事件
        activity_main_headerClickFunction();


        //获取 activity_main_scroll_weather_info 布局
        activity_main_scroll_weather_infoFunction();

        // 获取 weather_main_info 视图
        weather_main_infoFunction();

        // 加载城市信息
        httpGetLocateCityInfo();

//        系统状态栏背景色为 APP 背景
        initStatusBar();

        setBackgroundWhenTimeMoreThan7OClock();
    }

    // 时间比较 如果超过晚上 7点 就将布局背景设置为 星空背景
    private void setBackgroundWhenTimeMoreThan7OClock() {
        if (currentHours >= 19 || currentHours <= 6)  //当前时间大于 19 小于 6  在夜间
        {
            main_container.setBackgroundResource(R.drawable.xingkong);
//            Toast.makeText(this, "时间符合", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(this, ""+currentHours, Toast.LENGTH_SHORT).show();

    }


    // 布局重启检查是否有城市在数据库检查是否有城市在数据库
    @Override
    protected void onRestart() {
        super.onRestart();
        initialCity();
    }

    // 检查是否有城市在数据库,没有就一直让用户选择
    private void initialCity() {

        //保存开关状态
        sharedPreferences = getPreferences(Activity.MODE_PRIVATE);
        // 输入状态编辑者
        editor = sharedPreferences.edit();  //获取输入能力

        // 显示当前城市注主界面组件 currentCityTemperatureTEXT currentCityLowHighTemperature
        currentCityTemperatureTEXT = findViewById(R.id.currentCityTemperatureTEXT);
        currentCityLowHighTemperature = findViewById(R.id.currentCityLowHighTemperature);
        weather_proposal_TEXT = findViewById(R.id.weather_proposal_TEXT);
        currentCityWeatherType = findViewById(R.id.currentCityWeatherType);
        todayDate = findViewById(R.id.todayDate);
        main_container = findViewById(R.id.main_container);
        updateTime = findViewById(R.id.updateTime);
        fengli = findViewById(R.id.fengli);
        // 定义数据库对象
        dataBaseCity = new DataBaseCity(MainActivity.this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        initialCity();
    }


    //activity_main_header 的点击事件
    private void activity_main_headerClickFunction() {
        settingsIV = findViewById(R.id.settingsIV);
        cityManagerIV = findViewById(R.id.cityManagerIV);
        showCurrentCityNameTEXT = findViewById(R.id.showCurrentCityNameTEXT);

        // 注册 settingsIV 和 cityManagerIV 和 showCurrentCityNameTEXT 单击事件
        settingsIV.setOnClickListener(this);
        cityManagerIV.setOnClickListener(this);
        showCurrentCityNameTEXT.setOnClickListener(this);


    }

    // 获取 weather_future_info 视图,一个线性布局里面添加线性布局（4个组件，日期，图片，最低温，最高温）
    private void weatherInfoFunction(CityBean cityBean) {

        // 获取container 布局
        LinearLayout container_linearLayout = findViewById(R.id.container_linearLayout);
        // 移除内部的所有组件
        container_linearLayout.removeAllViewsInLayout();
        currentCityTemperatureTEXT.setText("");
        currentCityLowHighTemperature.setText("");
        weather_proposal_TEXT.setText("");
        todayDate.setText("");
        updateTime.setText("最后一次更新时间");
//        LinearLayout layout = new LinearLayout(this);
//        设置 container内的 线性布局 的布局参数
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                100

        );
        param.setMarginStart(10);
//        param.setMarginEnd(10);
        param.setMargins(10, 30, 0, 0);

        // 设置内部组件参数
        LinearLayout.LayoutParams paramInnerWidget = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        paramInnerWidget.gravity = Gravity.CENTER_HORIZONTAL;
        paramInnerWidget.gravity = Gravity.CENTER_VERTICAL;

        // 初始化对象参数
        LinearLayout[] linearLayouts = new LinearLayout[5];
        TextView[] textViewsDate = new TextView[5];
        TextView[] textViewsFutureCityLowTmp = new TextView[5];
        TextView[] textViewsFutureCityHighTmp = new TextView[5];
        ImageView[] imageViewsFutureWeather = new ImageView[5];

        for (int i = 0; i < 4; i++) {
//            System.out.println("多发点");
            // 设置新添加的布局参数
            linearLayouts[i] = new LinearLayout(this);
            linearLayouts[i].setOrientation(LinearLayout.HORIZONTAL);
            linearLayouts[i].setLayoutParams(param);

            //实例 textView  显示 日期
            textViewsDate[i] = new TextView(this);
            textViewsDate[i].setLayoutParams(paramInnerWidget);
            textViewsDate[i].setTextSize(15f);
            textViewsDate[i].setText(cityBean.getData().getForecast().get(i + 1).getDate());
            textViewsDate[i].setTextColor(Color.WHITE);
            linearLayouts[i].addView(textViewsDate[i]);


            //实例imageVIew
            imageViewsFutureWeather[i] = new ImageView(this);
            // 动态判断 天气类型
            whatSetTodayIV(imageViewsFutureWeather[i], cityBean.getData().getForecast().get(i + 1).getType());
//            imageViewsFutureWeather[i].setImageResource(R.drawable.sun_enough);

            imageViewsFutureWeather[i].setLayoutParams(paramInnerWidget);
            linearLayouts[i].addView(imageViewsFutureWeather[i]);
//
            // 添加城市最低温
            textViewsFutureCityLowTmp[i] = new TextView(this);
            textViewsFutureCityLowTmp[i].setLayoutParams(paramInnerWidget);
            textViewsFutureCityLowTmp[i].setTextSize(15f);
            textViewsFutureCityLowTmp[i].setText(cityBean.getData().getForecast().get(i + 1).getLow());
            textViewsFutureCityLowTmp[i].setTextColor(Color.WHITE);
            linearLayouts[i].addView(textViewsFutureCityLowTmp[i]);
//            // textView
            // 添加城市最高温
            textViewsFutureCityHighTmp[i] = new TextView(this);
            textViewsFutureCityHighTmp[i].setLayoutParams(paramInnerWidget);
            textViewsFutureCityHighTmp[i].setTextSize(15f);
            textViewsFutureCityHighTmp[i].setText(cityBean.getData().getForecast().get(i + 1).getHigh());
            textViewsFutureCityHighTmp[i].setTextColor(Color.WHITE);
            linearLayouts[i].addView(textViewsFutureCityHighTmp[i]);

            //
            // 将新建的布局添加到 container 布局里
            container_linearLayout.addView(linearLayouts[i]);
            Log.i("MainActivity", cityBean.getData().getForecast().get(i).getType());
        }
        // 将 显示的城市名 信息添加 到 布局里
        showCurrentCityNameTEXT.setText(cityBean.getData().getCity());
        // 将城市的当前温度显示了
        // 显示当前城市注主界面组件 currentCityTemperatureTEXT currentCityLowHighTemperature
        currentCityTemperatureTEXT.setText(cityBean.getData().getWendu() + "℃");
        // 使用正则 提取 显示的温度
        currentCityLowHighTemperature.append(InfoRegularExpressionGet.getTemperatureNumber(cityBean.getData().getForecast().get(1).getLow()) + "~" + InfoRegularExpressionGet.getTemperatureNumber(cityBean.getData().getForecast().get(1).getHigh()));
        // 将天气建议填入
        weather_proposal_TEXT.setText("    " + cityBean.getData().getGanmao());
        // 将今天的天气图片填入
        whatSetTodayIV(currentCityWeatherType, cityBean.getData().getForecast().get(0).getType());
        // 将今天的日期填入
        todayDate.setText(cityBean.getData().getForecast().get(0).getDate());
        // 将选择的城市加入 sharedPreference
        editor.putString(LOCATION_KEY, cityBean.getData().getCity());
        // 更改布局背景
        changeMainContainerBackground(cityBean.getData().getForecast().get(0).getType());
        // 设置更新时间
        updateTime.setText("最后一次更新" + currentHours + ":00");
        // 设置风力 风向
        fengli.setText("风力:" + InfoRegularExpressionGet.getFengliNumber(cityBean.getData().getForecast().get(0).getFengli()) + "   风向:" + cityBean.getData().getForecast().get(0).getFengxiang());

    }

    // 更改布局背景
    // 如果时间超过了 7PM 就设置 背景色为黑夜
    private void changeMainContainerBackground(String type) {

        if (type.equals("晴")) {
            main_container.setBackgroundResource(R.drawable.qin);
        } else if (type.equals("阴")) {
            main_container.setBackgroundResource(R.drawable.yin);
        } else if (type.equals("多云")) {
            main_container.setBackgroundResource(R.drawable.duoyun);
        } else if (type.equals("阵雨")) {
            main_container.setBackgroundResource(R.drawable.xiaoyu_zhongyu);
        } else if (type.equals("雷阵雨")) {
            main_container.setBackgroundResource(R.drawable.leizhengyu);
        } else if (type.equals("小雨")) {
            main_container.setBackgroundResource(R.drawable.xiaoyu_zhongyu);
        } else if (type.equals("中雨")) {
            main_container.setBackgroundResource(R.drawable.xiaoyu_zhongyu);
        } else if (type.equals("大雨") || type.equals("暴雨")) {
            main_container.setBackgroundResource(R.drawable.dayu);
        } else if (type.equals("小雪") || type.equals("中雪")) {
            main_container.setBackgroundResource(R.drawable.xiaoxue_zhongxue);
        } else if (type.equals("大雪") || type.equals("暴雪")) {
            main_container.setBackgroundResource(R.drawable.daxue_baoxue);
        } else if (type.equals("雾霾")) {
            main_container.setBackgroundResource(R.drawable.wumai);
        } else if (type.equals("冰雹")) {
            main_container.setBackgroundResource(R.drawable.bingbao);
        } else if (type.equals("雨夹雪")) {
            main_container.setBackgroundResource(R.drawable.yujiaxue);
        } else {
            main_container.setBackgroundResource(R.drawable.nothing);
        }
        setBackgroundWhenTimeMoreThan7OClock();
    }

    // 将今天的天气图片填入 判断是 什么类型 的天气
    private void whatSetTodayIV(ImageView imageView, String type) {
        if (type.equals("晴")) {
            imageView.setImageResource(R.drawable.qin);
        } else if (type.equals("阴")) {
            imageView.setImageResource(R.drawable.yin);
        } else if (type.equals("多云")) {
            imageView.setImageResource(R.drawable.duoyun);
        } else if (type.equals("阵雨")) {
            imageView.setImageResource(R.drawable.zhenyu);
        } else if (type.equals("雷阵雨")) {
            imageView.setImageResource(R.drawable.leizhenyu);
        } else if (type.equals("小雨")) {
            imageView.setImageResource(R.drawable.xiaoyu);
        } else if (type.equals("中雨")) {
            imageView.setImageResource(R.drawable.zhongyu);
        } else if (type.equals("大雨") || type.equals("暴雨")) {
            imageView.setImageResource(R.drawable.daxue_baoxue);
        } else if (type.equals("小雪") || type.equals("中雪")) {
            imageView.setImageResource(R.drawable.xiaoxue_zhongxue);
        } else if (type.equals("大雪") || type.equals("暴雪")) {
            imageView.setImageResource(R.drawable.daxue_baoxue);
        } else if (type.equals("雾霾")) {
            imageView.setImageResource(R.drawable.wumai);
        } else if (type.equals("冰雹")) {
            imageView.setImageResource(R.drawable.bingbao);
        } else if (type.equals("雨夹雪")) {
            imageView.setImageResource(R.drawable.yujiaxue);
        } else {
            imageView.setImageResource(R.drawable.nothing);
        }

    }


    // 获取 weather_main_info 视图
    private void weather_main_infoFunction() {
        findViewById(R.id.currentCityTemperatureTEXT).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "点了..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //获取 activity_main_scroll_weather_info 布局
    private void activity_main_scroll_weather_infoFunction() {
        activity_main_scroll_weather_info = findViewById(R.id.activity_main_scroll_weather_info);
        swipeRefreshLayout = activity_main_scroll_weather_info.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 开始刷新，设置当前为刷新状态
                swipeRefreshLayout.setRefreshing(true);
                httpGetLocateCityInfo(showCurrentCityNameTEXT.getText().toString());   // 刷新信息
                // 这里是主线程
                // 一些比较耗时的操作，比如联网获取数据，需要放到子线程去执行
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {  // 刷新时间超过

                    }
                }, 5000);
            }
        });
    }

    //获取 activity_main_header 布局
    private void activity_main_headerFunction() {
        activity_main_header = findViewById(R.id.activity_main_header);  //获取 activity_main 顶部布局
        showCurrentCityNameTEXT = activity_main_header.findViewById(R.id.showCurrentCityNameTEXT);
        //activity_main_header-settings 和 cityManagerIV 和 showCurrentCityNameTEXT 的触碰事件
        settingsIV = activity_main_header.findViewById(R.id.settingsIV);
        cityManagerIV = activity_main_header.findViewById(R.id.cityManagerIV);
        settingsIV.setOnTouchListener(this);
        cityManagerIV.setOnTouchListener(this);
        showCurrentCityNameTEXT.setOnTouchListener(this);
    }


    //顶部的系统栏跟随APP背景色
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


    //    activity_main_header 的三个组件的 触碰 事件
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.settingsIV://按下事件
                //  触碰动画效果 透明一半
                AnimationUtil.clickAnimation(event, getApplicationContext(), v);
                break;
            case R.id.cityManagerIV://按下事件
                //  触碰动画效果 透明一半
                AnimationUtil.clickAnimation(event, getApplicationContext(), v);
                break;
            case R.id.showCurrentCityNameTEXT://按下事件
                //  触碰动画效果 透明一半
                AnimationUtil.clickAnimation(event, getApplicationContext(), v);
                break;
        }
        return false;
    }

    // settingsIV 和 cityManagerIV 和 showCurrentCityNameTEXT 单击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settingsIV:
                break;
            case R.id.cityManagerIV: //跳到 city_manager 布局
//                startActivity(new Intent(this,CityManager.class));
                startActivityForResult(new Intent(this, CityManager.class), StatusBar.REQUEST_CODE_CITY_MANAGER);
                break;

            case R.id.showCurrentCityNameTEXT: //跳到 city_manager 布局
//                startActivity(new Intent(this,CityManager.class));
                startActivityForResult(new Intent(this, CityManager.class), StatusBar.REQUEST_CODE_CITY_MANAGER);
//                Toast.makeText(this, "点了", Toast.LENGTH_SHORT).show();
                break;


        }
    }

    // 接受从cityManager.java 返回的城市信息，
    // 如果是 选择的城市，就执行 天气网络请求
    // 如果是 空 ，就什么也不动


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (StatusBar.REQUEST_CODE_CITY_MANAGER == requestCode && requestCode == StatusBar.REQUEST_CODE_CITY_MANAGER && !data.getStringExtra("chooseCityName").equals("空")) {
//            Toast.makeText(this, "选择城市是" + data.getStringExtra("chooseCityName"), Toast.LENGTH_SHORT).show();

            editor.putString(LOCATION_KEY, data.getStringExtra("chooseCityName")).commit();

//            Log.i("MainActivity", sharedPreferences.getString(LOCATION_KEY,"灵武"));
//            执行 天气网络请求
            httpGetLocateCityInfo(data.getStringExtra("chooseCityName"));
        }
    }

    // 请求天气数据 ， javaBean,将返回到的信息，添加到布局里
    // 执行http请求返回天气信息
    private void httpGetLocateCityInfo(String locationCurrent) {
        String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + locationCurrent;
        HttpUtil.httpGetLocateCityInfo(url, new HttpCallbackListener() {
            @Override
            public void onFinished(final CityBean cityBeanResponse) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cityBeanResponse.getStatus() == 1000) // 说明请求到了数据
                        {
                            Toast.makeText(MainActivity.this, "更新城市数据成功", Toast.LENGTH_SHORT).show();
                            weatherInfoFunction(cityBeanResponse);  //将信息添加到 布局里
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            Toast.makeText(MainActivity.this, "连接出错", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            // 第一种 常用 连续点击 退出程序
                        exit();
                        return false;
        }

        return super.onKeyDown(keyCode, event);
    }
    private long mExitTime ;
    private void exit() {
        if((System.currentTimeMillis() - mExitTime) >2000 ){
            Toast.makeText(this, "再次点击退出", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }else{
            finish();
        }
    }




    // 默认的加载城市信息
    // 执行http请求返回天气信息
    private void httpGetLocateCityInfo() {

        // 判断是否用过
        String value = sharedPreferences.getString(LOCATION_KEY, "灵武");
//        Log.i("MainActivity","httpGetLocateCityInfo():"+value);

        String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + value;
        HttpUtil.httpGetLocateCityInfo(url, new HttpCallbackListener() {
            @Override
            public void onFinished(final CityBean cityBeanResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weatherInfoFunction(cityBeanResponse);  //将信息添加到 布局里
                    }
                });

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                // 网络错误
                // 数据库没有城市信息
                // 默认背景设置
                dataBaseNoInfo_InternetError_DefaultMainContainerBackground();
            }
        });
    }

    //  网络错误 是处理：数据库没有城市信息 默认背景设置
    private void dataBaseNoInfo_InternetError_DefaultMainContainerBackground() {
        if (dataBaseCity.findAllCityLength() == 0) // 没有城市信息
        {
            main_container.setBackgroundResource(R.drawable.nothing);
        }
    }

}
