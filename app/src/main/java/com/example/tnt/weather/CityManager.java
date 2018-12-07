package com.example.tnt.weather;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnt.weather.INTERFACE.HttpCallbackListener;
import com.example.tnt.weather.Utils.ErrorCode;
import com.example.tnt.weather.Utils.GPSUtil;
import com.example.tnt.weather.Utils.HttpUtil;
import com.example.tnt.weather.Utils.StatusBar;
import com.example.tnt.weather.animation.AnimationUtil;
import com.example.tnt.weather.bean.CityBean;
import com.example.tnt.weather.database.DataBaseCity;
import com.example.tnt.weather.database.DataBaseLocationCity;
import com.example.tnt.weather.Utils.ErrorCode;
import com.hdl.myhttputils.MyHttpUtils;
import com.hdl.myhttputils.bean.CommCallback;

public class CityManager extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private TextView addCityTEXT, finishCurrentAtyTEXT;
    private ListView allCityInfo;                           // 显示所有城市的listView
    private TextView locationCityTEXT;                      // 显示 开启定位后显示当前城市 名称 的textView
    private ArrayAdapter<String> allCityInfo_arr_adapter;   // 显示所有城市的listView 的适配器
    private DataBaseLocationCity dataBaseLocationCity;      // 定位 表 对象
    private Switch locateSwitch;                            // 开启定位按钮
    private String locationCurrent;                         // 当前位置
    private GPSUtil gpsUtil;                                // 定位对象
    private DataBaseCity dataBaseCity;                      //引入自定义的操作数据库类
    private Intent i;                                        // 返回实例信息
    private SharedPreferences sharedPreferencesStatus;  //保存开关状态
    private SharedPreferences.Editor editor;                  // 输入状态编辑者
    private String SWITCH_STATUS_KEY = "SWITCH_STATUS";       // 开关的键
    private String LOCATION_INFO_KEY = "LOCATION_INFO_KEY";   // 是否已经定位过的键
    private String switchStatusString;                       // 开关状态
    private TextView locationCityTemperatureTEXT;           // 显示 开启定位后显示当前城市 气温 的textView
    private Dialog mShareDialog;                            // 常点击删除城市
    private String deleteCityName ;                         //要删除的城市名
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);


        // 初始化组件
        findViewFunction();

        // 初始化 对象
        initialObject();

        // 初始化方法
        initialFunction();

        // 监听事件
        ListenFunction();

        //顶部的系统栏跟随APP背景色
        initStatusBar();

//        cityManager 布局头的点击事件
        cityManagerClickFunction();


        //    所有城市显示
        allCityInfoShowFunction();


//        定位开关开启功能
        locateSwitchFunction();
//        Toast.makeText(this,"cityNameTable数据个数："+dataBaseCity.findAllCityLength(),Toast.LENGTH_SHORT).show();
    }

    //item 长时间点击 事件
    public void longClickShowDialog( ) {
        showDialog();// 单击按钮后 调用显示视图的 showDialog 方法，
    }

    /**
     * 显示弹出框
     */
    private void showDialog( ) {
        if (mShareDialog == null) {
            initShareDialog();
        }
        mShareDialog.show();
    }

    /**
     * 初始化删除弹出框
     */
    private void initShareDialog() {
        mShareDialog = new Dialog(this, R.style.dialog_bottom_full);
        mShareDialog.setCanceledOnTouchOutside(true); //手指触碰到外界取消
        mShareDialog.setCancelable(true);             //可取消 为true
        Window window = mShareDialog.getWindow();      // 得到dialog的窗体
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);

        View view = View.inflate(this, R.layout.lay_share, null); //获取布局视图
        view.findViewById(R.id.deleteTheCity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShareDialog != null && mShareDialog.isShowing()) {
                  // 点击删除按钮删除 当前城市
                    dataBaseCity.deleteCityName(deleteCityName);
                    // 获取所有的数据库信息,重新加载 显示很多城市名称的 listView
                    allCityInfoShowFunction();
                    //     Toast.makeText(this,"删除城市"+ allCityInfo.getItemAtPosition(position),Toast.LENGTH_SHORT).show();




                    mShareDialog.dismiss();
                }
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }
    // 初始化 对象
    private void initialObject() {
        // 定义数据库对象
        dataBaseCity = new DataBaseCity(CityManager.this);

        //引入自定义的操作数据库类
        dataBaseLocationCity = new DataBaseLocationCity(this);

        //保存开关状态
        sharedPreferencesStatus = getPreferences(Activity.MODE_PRIVATE);

        // 输入状态编辑者
        editor = sharedPreferencesStatus.edit();  //获取输入能力

        // gps 初始化

//        Toast.makeText(this, "开关状态为：" + switchStatusString, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "locateSwitch.isChecked()：" + locateSwitch.isChecked(), Toast.LENGTH_SHORT).show();

    }

    // 初始化方法
    private void initialFunction() {
        // 获取获取开关状态
        getSwitchStartAtyStatus();

        // 如果是保存的状态是 开 ，就打开开关
        judgeSwitch();
    }

    // 如果是保存的状态是 开 ，就打开开关
    private void judgeSwitch() {

        if (switchStatusString.equals("开")) {
            locateSwitch.setChecked(true);
            //
            // 如果是 开 就 将数据添加到定位布局里
//            Toast.makeText(this, "开", Toast.LENGTH_SHORT).show();

            // 判断是否定位过
            String value = sharedPreferencesStatus.getString(LOCATION_INFO_KEY, "未定位");
            if (value.equals("定位过了")) {
                //开启GPS定位
                gpsGetLocation();
//                Toast.makeText(this, "开启过定位权限", Toast.LENGTH_SHORT).show();
            }// 否则什么也不做
            else {
//                Toast.makeText(this, "没有开启过定位权限", Toast.LENGTH_SHORT).show();
            }


        } else {
            locateSwitch.setChecked(false);
        }
    }

    // 获取开关状态
    private String getSwitchStartAtyStatus() {
        switchStatusString = sharedPreferencesStatus.getString(SWITCH_STATUS_KEY, "关");
        return switchStatusString;
    }


    // 获取组件
    private void findViewFunction() {
        // 获取所有的城市信息
        allCityInfo = findViewById(R.id.allCityInfo);

        // 显示 开启定位后显示当前城市 名称 的textView
        locationCityTEXT = findViewById(R.id.locationCityTEXT);
        // 显示 开启定位后显示当前城市 气温 的textView
        locationCityTemperatureTEXT = findViewById(R.id.locationCityTemperatureTEXT);

        // 获取 开关 组件
        locateSwitch = findViewById(R.id.locateSwitch);

    }

    // 监听事件
    private void ListenFunction() {
        allCityInfo.setOnItemClickListener(this);
        allCityInfo.setOnItemLongClickListener(this);
    }

    //        定位开关 开启，关闭 功能
    private void locateSwitchFunction() {


        locateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {   //开启定位功能
                    gpsGetLocation();
//                    Toast.makeText(CityManager.this, "开启", Toast.LENGTH_SHORT).show();
                } else {  // 关闭定位功能  ，移除与定位相关的 信息组件
                    // 清除定位信息
                    clearLocateCityInfo();
                }
            }
        });

    }

    // 定位获取
    private void gpsGetLocation() {
//        System.out.println("最终的信息：" + locationCurrent);
        // 获取 gps 对象
        gpsUtil = new GPSUtil(CityManager.this);
        gpsUtil.locationFunction();
        locationCurrent = gpsUtil.getLocationCurrent();
        if (locationCurrent == null || locationCurrent == ErrorCode.LOCATION_ERROR_CODE)  //位置信息获取失败
            Toast.makeText(CityManager.this, "定位出错", Toast.LENGTH_SHORT).show();
        else {
            httpGetLocateCityInfo(locationCurrent);  // 将定位到的城市信息，添加到定位布局里，然后获取天气信息
        }
    }

    // 清除定位信息
    private void clearLocateCityInfo() {
        // 清除 cityNameTable 里的城市信息
        dataBaseCity.deleteCityName(locationCityTEXT.getText().toString());
        // 清除显示城市信息的布局参数

        // 设置开关状态为 关
        editor.putString(SWITCH_STATUS_KEY, "关").commit();

        // 清除信息
        locationCityTemperatureTEXT.setText("");
        locationCityTEXT.setText(R.string.locatePosition);

        // 加载 所有城市 布局列表
        allCityInfoShowFunction();
    }

    // 执行http请求返回天气信息
    private void httpGetLocateCityInfo(String locationCurrent) {
        String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + locationCurrent;
        HttpUtil.httpGetLocateCityInfo(url, new HttpCallbackListener() {
            @Override
            public void onFinished(CityBean cityBeanResponse) {
                addLocateCityToUI(cityBeanResponse);         //将定位城市信息，添加到布局里
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(CityManager.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 将定位到的城市信息，添加到定位布局 listView ,然后删除其他 cityNameTable 的相同城市 里，然后获取天气信息
    private void addLocateCityToUI(CityBean cityBean) {


        // 将城市信息添加到 cityNameLocationTable 里
        addLocationCityToCityNameLocationTable(cityBean.getData().getCity());

//        // 删除 cityNameTable 里相同的城市
//        deleteCityOfCityNameTable(cityBean.getData().getCity());

        // 将信息添加到 cityNameTable 里
        dataBaseCity.addToDataBaseCity(cityBean.getData().getCity());

        System.out.println("定位的城市信息：" + cityBean.getData().getCity());

        // 将城市信息添加到定位布局里
        addLocationCityToLocationUI(cityBean.getData().getCity(), cityBean.getData().getWendu());


        //保存开关状态
        saveSwitchStatusOnSwitchChecked();
        // 保存是否定位过状态
        saveLocationInfoStatus();

        // 获取所有的数据库信息,重新加载 显示很多城市名称的 listView
        allCityInfoShowFunction();
    }

    // 保存是否定位过状态
    private void saveLocationInfoStatus() {
        editor.putString(LOCATION_INFO_KEY, "定位过了").commit();
    }

    //保存开关状态
    private void saveSwitchStatusOnSwitchChecked() {
        editor.putString(SWITCH_STATUS_KEY, "开").commit();
    }



    // 将城市信息添加到 cityNameLocationTable 里
    private void addLocationCityToCityNameLocationTable(String city) {
        dataBaseLocationCity.addToDataBaseLocationCity(city);
    }

    // 将定位到的 城市信息添加到定位布局里
    private void addLocationCityToLocationUI(String city, String wendu) {
        locationCityTEXT.setText(city);
        locationCityTemperatureTEXT.setText(wendu + "℃");

//        Toast.makeText(this, "设置", Toast.LENGTH_SHORT).show();
    }

    //        所有城市显示
    private void allCityInfoShowFunction() {
        // 数组适配器
        String[] allCityInfo_data = dataBaseCity.findAllCity().toArray(new String[0]);

//      添加自定义的展示 allCityInfo 的布局文件
        allCityInfo_arr_adapter = new ArrayAdapter<>(this, R.layout.city_manger_listview_style, allCityInfo_data);
        allCityInfo.setAdapter(allCityInfo_arr_adapter);

    }

    //        cityManager 布局头的点击事件
    private void cityManagerClickFunction() {

        addCityTEXT = findViewById(R.id.addCityTEXT);
        finishCurrentAtyTEXT = findViewById(R.id.finishCurrentAtyTEXT);
        // 注册 点击事件
        addCityTEXT.setOnClickListener(this);
        finishCurrentAtyTEXT.setOnClickListener(this);
    }

    //顶部的系统栏跟随APP背景色
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finishCurrentAtyTEXT:   // 结束当前城市管理界面
                // 动画 点击透明一半效果
                AnimationUtil.clickAnimation(getApplicationContext(), v);
                // 返回信息
                returnMainActivityCityName();
                finish();
                break;
            case R.id.addCityTEXT:           // 进入添加城市界面
                // 动画 点击透明一半效果
                //启动另一个添加城市界面
                AnimationUtil.clickAnimation(getApplicationContext(), v);

                // 获取从城市添加页面来的城市，然后添加到数据库里
                startActivityForResult(new Intent(this, CityChooseActivity.class), StatusBar.REQUEST_CODE_CHOOSE_LOCATION);
                break;


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == StatusBar.REQUEST_CODE_CHOOSE_LOCATION && requestCode == StatusBar.REQUEST_CODE_CHOOSE_LOCATION)  // 是热门城市里选择的
        {
            String cityName = data.getStringExtra("clickCityName");
//            Toast.makeText(this, "cityName:" + cityName, Toast.LENGTH_SHORT).show();

            // 将城市信息添加到数据库里
            dataBaseCity.addToDataBaseCity(cityName);

        }
    }

    // 布局开启加载城市信息，如果没有城市就强制跳转到添加城市界面

    @Override
    protected void onStart() {
        super.onStart();
        // 获取所有的数据库信息,重新加载 显示很多城市名称的 listView
        allCityInfoShowFunction();
    }

    // 布局重启加载城市信息
    @Override
    protected void onRestart() {
        super.onRestart();
        // 获取所有的数据库信息,重新加载 显示很多城市名称的 listView
        allCityInfoShowFunction();
//        Toast.makeText(this, "布局重启", Toast.LENGTH_SHORT).show();
    }


    //重写onKeyDown方法,对按键(不一定是返回按键)监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) //当返回按键被按下
        {
            returnMainActivityCityName();     // 没有选择listView的城市，返回这条信息
        }
        return false;
    }

    // 定位开启后，用户按了 系统返回键 或者 提供的返回键 就返回定位的位置

    // 定位没有开启，用户没有点击 listView ，返回 空

    private void returnMainActivityCityName() {

        if (getSwitchStartAtyStatus().equals("开")) // 检查用户是否 开启了定位
        {
//            Toast.makeText(this,"cityNameTable数据个数："+dataBaseCity.findAllCityLength(),Toast.LENGTH_SHORT).show();
            returnLocatePosition();    // 返回定位的位置
//            Toast.makeText(this, "开：" + getSwitchStartAtyStatus(), Toast.LENGTH_SHORT).show();
        } else {
            returnNullIntent();    // 返回空
//            Toast.makeText(this, "关：" + getSwitchStartAtyStatus(), Toast.LENGTH_SHORT).show();
        }

    }

    // 用户按了返回键图标，或者 按了系统返回键  返回空
    private void returnNullIntent() {

        if(dataBaseCity.findAllCityLength() == 0) // 说明里面没有数据，返回默认城市 北京
        {
            i = new Intent();
            i.putExtra("chooseCityName", "灵武");
            setResult(StatusBar.REQUEST_CODE_CITY_MANAGER, i);
            finish();
        }else{
            i = new Intent();
            i.putExtra("chooseCityName", "空");
            setResult(StatusBar.REQUEST_CODE_CITY_MANAGER, i);
            finish();
        }

    }

    // 返回定位的位置
    private void returnLocatePosition() {
        i = new Intent();
        i.putExtra("chooseCityName", locationCityTEXT.getText().toString());
        setResult(StatusBar.REQUEST_CODE_CITY_MANAGER, i);
        finish();
    }

    // 选择 了某一个 Item
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String chooseCityName = (String) allCityInfo.getItemAtPosition(position);
            i = new Intent();
            i.putExtra("chooseCityName", chooseCityName);
            setResult(StatusBar.REQUEST_CODE_CITY_MANAGER, i);
            finish();
    }

    // 长时间点击 item  从数据库移除 此城市
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//        Toast.makeText(this, "长时间点击了" + allCityInfo.getItemAtPosition(position), Toast.LENGTH_SHORT).show();

        deleteCityName = (String) allCityInfo.getItemAtPosition(position);

        // 长时间点击显示出 Dialog
        longClickShowDialog();



        return true;
    }
}
