package com.example.tnt.weather.INTERFACE;

import com.example.tnt.weather.bean.CityBean;

public interface HttpCallbackListener {
    void onFinished(CityBean cityBeanResponse);
    void onError(String errorMessage);
}
