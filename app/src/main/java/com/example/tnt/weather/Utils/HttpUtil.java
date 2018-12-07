package com.example.tnt.weather.Utils;


import com.example.tnt.weather.INTERFACE.HttpCallbackListener;
import com.example.tnt.weather.bean.CityBean;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    public static void httpGetLocateCityInfo(String url, final HttpCallbackListener callbackListener){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callbackListener.onError("请求错误");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Gson gson = new Gson();
                CityBean cityBean = gson.fromJson(data,CityBean.class);
                callbackListener.onFinished(cityBean);
            }
        });
    }
}
