package com.example.tnt.weather.animation;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.example.tnt.weather.R;

public class AnimationUtil {
    // 提取 动画效


    // 提取点击 动画效果
    public static void clickAnimation(Context context, View v){
        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.activity_main_header_action_down_setting));
        v.startAnimation(AnimationUtils.loadAnimation(context,R.anim.activity_main_header_action_up_setting));
    }
    //    提取触碰动画效果
    public static void clickAnimation(MotionEvent event,Context context, View v){
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.activity_main_header_action_down_setting));
            v.startAnimation(AnimationUtils.loadAnimation(context,R.anim.activity_main_header_action_up_setting));
        }

    }


}
