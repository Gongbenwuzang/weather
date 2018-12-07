package com.example.tnt.weather.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoRegularExpressionGet {
    //使用正则找到温度
    public static String getTemperatureNumber(String temperature){
        //使用正则找到风力级别
        String pattern = "\\d+℃";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(temperature);
        if(m.find()){
//            System.out.println("找到了"+m.group(0));
            return m.group(0);
        }else{
//            System.out.println("未找到");
        }
        return  m.group(0); //返回匹配到的温度

    }
    //使用正则找到风力级别
    public static String getFengliNumber(String windLevelString){
        //使用正则找到风力级别
        String pattern = "\\d级";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(windLevelString);
        if(m.find()){
            return m.group(0);
        }else{

        }
        return  m.group(0); //返回匹配到的风力

    }
}
