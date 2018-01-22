package org.usslab.decam.Util;

import com.orhanobut.logger.Logger;

/**
 * Created by pip on 2017/1/27.
 */

public  class Logg {
    //reuse TAG, <Prefix-Tag>

    public static final int VERBOSE=1;
    public static final int DEBUG=2;
    public static final int INFO=3;
    public static final int WARN=4;
    public static final int ERROR=5;
    public static final int NOTHING=9;

    public static int level=VERBOSE;

    public static void v(String tag,String msg){
        if(level<=VERBOSE){
            Logger.t(tag).v(msg);
        }

    }
    public static void d(String tag,String msg){
        if(level<=DEBUG){
            Logger.t(tag).d(msg);
        }

    }
    public static void i(String tag,String msg){
        if(level<=INFO){
            Logger.t(tag).i(msg);
        }

    }
    public static void w(String tag,String msg){
        if(level<=WARN){
            Logger.t(tag).w(msg);
        }

    }


    public static void e(String tag,String msg){
        if(level<=ERROR){
            Logger.t(tag).e(msg);
        }

    }
    public static void e(String tag,String msg,Exception e){
        if (level<=ERROR){
            Logger.t(tag).e(e,msg);//a little different with log.e(tag,msg,e);
        }
    }
}
