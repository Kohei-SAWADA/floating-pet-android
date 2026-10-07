package com.dot.floatingpet;

import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.*;
import com.dot.floatingpet.core.HomeVisibility;
import com.dot.floatingpet.core.UsageQueryWindow;
import com.dot.floatingpet.core.PollPolicy;

/** On-device, session-only visibility evidence. Never logs or persists usage events. */
final class HomeMonitor {
    interface Listener { void onHomeState(boolean home, boolean usageAccess); }
    private final Context context;
    private final Listener listener;
    private final android.content.SharedPreferences preferences;
    private final PollPolicy pollPolicy=new PollPolicy();
    private long nextAllowedPollAt,lastStartedUptime=-1;
    private boolean published, publishedHome, publishedAccess;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final HandlerThread thread=new HandlerThread("PetHomeVisibility");
    private Handler worker;
    private volatile boolean closed;
    private volatile int generation;
    private int appliedGeneration;
    private HomeVisibility visibility=new HomeVisibility(null);
    private long lastQuery, sessionStart;
    private boolean wasEligible;
    private final Runnable staleQuery;

    HomeMonitor(Context context,Listener listener){this.context=context.getApplicationContext();this.listener=listener;this.preferences=new PetStore(context).prefs();this.staleQuery=()->{if(!closed){published=false;this.listener.onHomeState(false,hasUsageAccess(this.context));}};}
    static boolean hasUsageAccess(Context context){
        try{
        AppOpsManager ops=(AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        if(ops==null)return false;
        int result=Build.VERSION.SDK_INT>=29
                ?ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),context.getPackageName())
                :ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),context.getPackageName());
        return result==AppOpsManager.MODE_ALLOWED;
        }catch(RuntimeException e){return false;}
    }
    static boolean isUnlocked(Context context){
        try{
        KeyguardManager keyguard=(KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        UserManager users=(UserManager)context.getSystemService(Context.USER_SERVICE);
        return keyguard!=null&&!keyguard.isKeyguardLocked()&&users!=null&&users.isUserUnlocked();
        }catch(RuntimeException e){return false;}
    }
    void start(){thread.start();worker=new Handler(thread.getLooper());worker.post(poll);}
    void refresh(){if(worker!=null&&!closed)worker.post(this::schedulePoll);}
    void invalidateAndRefresh(){if(worker!=null&&!closed){int expected=++generation;worker.post(()->{visibility=new HomeVisibility(null);wasEligible=false;appliedGeneration=expected;publish(false,hasUsageAccess(context),expected);schedulePoll();});}}
    void close(){closed=true;main.removeCallbacksAndMessages(null);if(worker!=null)worker.removeCallbacksAndMessages(null);thread.quitSafely();}
    private void publish(boolean home,boolean access,int expected){main.post(()->{
        if(!closed&&generation==expected&&(!published||publishedHome!=home||publishedAccess!=access)){
            published=true;publishedHome=home;publishedAccess=access;listener.onHomeState(home,access);
        }
    });}
    /** Worker-only scheduling: refresh requests cannot bypass the rate cap or overlap queries. */
    private void schedulePoll(){
        if(closed)return;
        worker.removeCallbacks(poll);
        if(lastStartedUptime>=0)nextAllowedPollAt=Math.max(nextAllowedPollAt,lastStartedUptime+selectedInterval());
        worker.postDelayed(poll,Math.max(0,nextAllowedPollAt-SystemClock.uptimeMillis()));
    }
    private long selectedInterval(){return preferences.getBoolean("fast_visibility",true)?PollPolicy.FAST_INTERVAL_MS:PollPolicy.ECONOMY_INTERVAL_MS;}
    private final Runnable poll=new Runnable(){public void run(){
        if(closed)return;
        long startedUptime=SystemClock.uptimeMillis();
        if(lastStartedUptime>=0)nextAllowedPollAt=Math.max(nextAllowedPollAt,lastStartedUptime+selectedInterval());
        if(startedUptime<nextAllowedPollAt){schedulePoll();return;}
        lastStartedUptime=startedUptime;
        int expected=appliedGeneration;
        boolean home=false,access=false,eligible=false,querySucceeded=false;
        long queryStarted=SystemClock.elapsedRealtime();
        main.postDelayed(staleQuery,1500);
        long now=System.currentTimeMillis();
        try{
            PowerManager power=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            access=hasUsageAccess(context);
            eligible=power!=null&&power.isInteractive()&&isUnlocked(context)&&access;
            if(!eligible){visibility.reset();wasEligible=false;lastQuery=now;sessionStart=now;}
            else{
                // Never scan pre-session history. An explicit return to Home supplies fresh evidence.
                if(!wasEligible||now<lastQuery){visibility=new HomeVisibility(null);sessionStart=now;lastQuery=now;}
                wasEligible=true;
                if(UsageQueryWindow.incompleteCoverage(lastQuery,now))visibility.reset();
                String homePackage=resolveHomePackage();visibility.setHomePackage(homePackage);
                UsageStatsManager stats=(UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
                UsageEvents events=stats==null?null:stats.queryEvents(UsageQueryWindow.begin(sessionStart,lastQuery,now),now);
                if(homePackage==null){visibility.reset();wasEligible=false;sessionStart=now;lastQuery=now;}
                else if(events==null){home=false; /* Do not publish cached evidence until a complete retry succeeds. */}
                else{
                    UsageEvents.Event event=new UsageEvents.Event();
                    int count=0;
                    while(events.hasNextEvent()){
                        if(++count>4096)throw new IllegalStateException("Usage query exceeds bounded event budget");
                        events.getNextEvent(event);
                        int type=event.getEventType();
                        String activity=event.getClassName();
                        if(type==UsageEvents.Event.MOVE_TO_FOREGROUND)visibility.resumed(event.getPackageName(),activity,event.getTimeStamp());
                        else if(type==UsageEvents.Event.MOVE_TO_BACKGROUND)visibility.paused(event.getPackageName(),activity,event.getTimeStamp());
                        else if(Build.VERSION.SDK_INT>=29&&type==UsageEvents.Event.ACTIVITY_STOPPED)visibility.stopped(event.getPackageName(),activity,event.getTimeStamp());
                    }
                    home=visibility.shouldShow(false,true,true,true);
                    lastQuery=now;querySucceeded=true;
                }
            }
        }catch(RuntimeException e){home=false; /* Hide now; replay from last success before confirming retained state. */}
        main.removeCallbacks(staleQuery);
        if(SystemClock.elapsedRealtime()-queryStarted>1000){home=false; /* Keep parsed evidence; next timely poll must confirm it. */}
        publish(home,access,expected);
        long duration=SystemClock.elapsedRealtime()-queryStarted;
        boolean fast=preferences.getBoolean("fast_visibility",true);
        long wait=pollPolicy.nextDelayMillis(fast,duration,querySucceeded,eligible);
        nextAllowedPollAt=Math.max(startedUptime+(fast?PollPolicy.FAST_INTERVAL_MS:PollPolicy.ECONOMY_INTERVAL_MS),SystemClock.uptimeMillis()+wait);
        // Screen-off/locked states do no usage queries. Screen broadcasts also request an immediate check.
        PowerManager currentPower=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        if(!closed&&currentPower!=null&&currentPower.isInteractive())schedulePoll();
    }};
    private String resolveHomePackage(){
        Intent home=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info=context.getPackageManager().resolveActivity(home,PackageManager.MATCH_DEFAULT_ONLY);
        if(info==null||info.activityInfo==null||"android".equals(info.activityInfo.packageName))return null;
        return info.activityInfo.packageName;
    }
}
