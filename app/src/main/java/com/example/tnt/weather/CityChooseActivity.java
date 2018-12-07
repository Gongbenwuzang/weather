package com.example.tnt.weather;


import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tnt.weather.INTERFACE.HttpCallbackListener;
import com.example.tnt.weather.Utils.ErrorCode;
import com.example.tnt.weather.Utils.HttpUtil;
import com.example.tnt.weather.Utils.StatusBar;
import com.example.tnt.weather.bean.CityBean;

public class CityChooseActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private ListView hotCityLV_1, hotCityLV_2, hotCityLV_3, hotCityLV_4;
    private ArrayAdapter<String> hotCityLV_arr_adapter_1, hotCityLV_arr_adapter_2, hotCityLV_arr_adapter_3, hotCityLV_arr_adapter_4;
    private Intent i;                              // 传递确定的城市给城市管理类
    private String clickCityName;                           // 点击的城市名称
    private EditText edtCityName;                           // et 添加城市信息
    private TextView btnOkCityName;                           //  确定添加

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_city_choose);

        // 获取需要的组件
        findViewFunction();

        //顶部的系统栏跟随APP背景色
        initStatusBar();

        // 给 listView 添加热门城城市信息选择
        addHotCityChoose();

        // 设置监听事件
        addHotCityListenFunction();

    }

    // 获取需要的组件
    private void findViewFunction() {
        // 输入添加城市的框
        edtCityName = findViewById(R.id.edtCityName);

        // 按钮 确定添加
        btnOkCityName = findViewById(R.id.btnOkCityName);

        //       找到四个 显示热门城市 listView
        hotCityLV_1 = findViewById(R.id.hotCityLV_1);
        hotCityLV_2 = findViewById(R.id.hotCityLV_2);
        hotCityLV_3 = findViewById(R.id.hotCityLV_3);
        hotCityLV_4 = findViewById(R.id.hotCityLV_4);
    }

    // 设置监听事件
    private void addHotCityListenFunction() {
        //        设置 热门城市 监听器
        hotCityLV_1.setOnItemClickListener(this);
        hotCityLV_2.setOnItemClickListener(this);
        hotCityLV_3.setOnItemClickListener(this);
        hotCityLV_4.setOnItemClickListener(this);

        // 设置 确定添加 btn 的监听事件
        btnOkCityName.setOnClickListener(this);

    }

    // 给 listView 添加热门城市信息选择
    private void addHotCityChoose() {


        // 数组适配器
        String[] hotCityLV_1_data = {"北京", "上海", "广州"};
        hotCityLV_arr_adapter_1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hotCityLV_1_data);
        hotCityLV_1.setAdapter(hotCityLV_arr_adapter_1);

        String[] hotCityLV_2_data = {"天津", "杭州", "东莞"};
        hotCityLV_arr_adapter_2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hotCityLV_2_data);
        hotCityLV_2.setAdapter(hotCityLV_arr_adapter_2);

        String[] hotCityLV_3_data = {"宁波", "西安", "成都"};
        hotCityLV_arr_adapter_3 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hotCityLV_3_data);
        hotCityLV_3.setAdapter(hotCityLV_arr_adapter_3);

        String[] hotCityLV_4_data = {"重庆", "武汉", "厦门"};
        hotCityLV_arr_adapter_4 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hotCityLV_4_data);
        hotCityLV_4.setAdapter(hotCityLV_arr_adapter_4);

    }


    //顶部的系统栏跟随APP背景色
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOkCityName:
                // 检查 要添加的城市是否输入正确，不正确就提示框
                httpGetLocateCityInfo(edtCityName.getText().toString());

                break;
        }
    }

    // 检查 要添加的城市是否输入正确，不正确就提示框
    private void inspectAddCityWhetherValid(CityBean cityBean) {

        if (cityBean.getDesc() == ErrorCode.weatherDesc || cityBean.getStatus() == ErrorCode.weatherStatus)  // 输入城市正确
        {
            i = new Intent();
            clickCityName = cityBean.getData().getCity();    // 获取到的城市
            i.putExtra("clickCityName", clickCityName);
            setResult(StatusBar.REQUEST_CODE_CHOOSE_LOCATION, i);
            finish();
        } else { // 将获取到的城市信息返回

            Toast.makeText(this, "输入城市错误", Toast.LENGTH_SHORT).show();
        }

    }

    // 网络请求
    private void httpGetLocateCityInfo(String locationCurrent) {
        String url = "http://wthrcdn.etouch.cn/weather_mini?city=" + locationCurrent;
        // 回调获取信息
        HttpUtil.httpGetLocateCityInfo(url, new HttpCallbackListener() {
            @Override
            public void onFinished(final CityBean cityBeanResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inspectAddCityWhetherValid(cityBeanResponse);  // 检查是否是正确的城市
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CityChooseActivity.this, "城市信息获取失败，稍后再试", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }


    //    当用户选择了热门城市中的一个
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        System.out.println("ID"+view.findViewById(R.id.text1))
//        Toast.makeText(this, "postion :"+position+"  id:"+id+" parent:"+parent, Toast.LENGTH_SHORT).show();
        i = new Intent();
        switch (parent.getId()) {
            case R.id.hotCityLV_1:

                clickCityName = (String) hotCityLV_1.getItemAtPosition(position);
                i.putExtra("clickCityName", clickCityName);
                setResult(StatusBar.REQUEST_CODE_CHOOSE_LOCATION, i);
                finish();
                break;
            case R.id.hotCityLV_2:
                clickCityName = (String) hotCityLV_2.getItemAtPosition(position);
                i.putExtra("clickCityName", clickCityName);
                setResult(StatusBar.REQUEST_CODE_CHOOSE_LOCATION, i);
                finish();
//                Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.hotCityLV_3:
                clickCityName = (String) hotCityLV_3.getItemAtPosition(position);
                i.putExtra("clickCityName", clickCityName);
                setResult(StatusBar.REQUEST_CODE_CHOOSE_LOCATION, i);
                finish();
//                Toast.makeText(this, "3", Toast.LENGTH_SHORT).show();
                break;
            case R.id.hotCityLV_4:
                clickCityName = (String) hotCityLV_4.getItemAtPosition(position);
                i.putExtra("clickCityName", clickCityName);
                setResult(StatusBar.REQUEST_CODE_CHOOSE_LOCATION, i);
                finish();
//                Toast.makeText(this, "4", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
