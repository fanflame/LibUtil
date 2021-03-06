package com.fanyiran.utils.looperexecute;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by fanqiang on 2018/12/7.
 */
public class MainLooperExecuteUtil {
    private static MainLooperExecuteUtil mainLooperExecuteUtil;
    public static MainLooperExecuteUtil getInstance(){
        if(mainLooperExecuteUtil == null){
            mainLooperExecuteUtil = new MainLooperExecuteUtil();
        }
        return mainLooperExecuteUtil;
    }

    private MainLooperExecuteUtil(){
    }

    static public Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj instanceof CustomRunnable){
                CustomRunnable runnable = (CustomRunnable) msg.obj;
                runnable.getRunnable().run();
                if(!runnable.isCanceled() && runnable.isRepeated()){
                    MainLooperExecuteUtil.getInstance().postDelay(runnable);
                }
            }
        }
    };

    public void postDelay(CustomRunnable runnable){
        if(runnable == null){
            return;
        }
        Message msg = Message.obtain();
        msg.obj = runnable;
        msg.what = runnable.getId();
        if(runnable.getDelayTime() != CustomRunnable.NO_DELAY){
            handler.sendMessageDelayed(msg,runnable.getDelayTime());
        }else{
            handler.sendMessage(msg);
        }
    }

    public void cancelRunnable(CustomRunnable runnable){
        if(runnable == null){
            return;
        }
        runnable.setCanceled(true);
        handler.removeMessages(runnable.getId());
    }
}
