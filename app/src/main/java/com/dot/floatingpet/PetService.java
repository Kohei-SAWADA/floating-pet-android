package com.dot.floatingpet;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.*;
import android.hardware.display.DisplayManager;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.Toast;
import com.dot.floatingpet.core.*;

public final class PetService extends Service implements android.content.SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String STOP="com.dot.floatingpet.STOP", TOGGLE="com.dot.floatingpet.TOGGLE";
    static final String CHANNEL="pet";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WindowManager wm; private WindowManager.LayoutParams params; private PetView pet; private Bitmap atlas;
    private Context windowContext;
    private final ComponentCallbacks windowCallbacks=new ComponentCallbacks(){public void onConfigurationChanged(Configuration c){if(pet!=null){cancelInteraction();computeBounds(true);safeUpdate();}}public void onLowMemory(){}};
    private HomeMonitor homeMonitor; private boolean homeVisible,usageAccess;
    private PetStore store; private boolean hidden, screenOn=true, attached;
    private final PetMotion motion=new PetMotion();
    private int screenWidth,screenHeight; private float downX,downY;
    private int startX,startY; private boolean dragging,touchActive;
    private final Runnable animate=new Runnable(){public void run(){
        if(pet==null || hidden || !screenOn || !homeVisible){cancelInteraction();return;}
        if(!Settings.canDrawOverlays(PetService.this)){stopSelf();return;}
        long now=SystemClock.uptimeMillis();
        boolean animations=pet.systemAnimationsAllowed();
        int[] pose=animations?motion.pose(now):new int[]{0,0};pet.pose(pose[0],pose[1]);
        if(animations&&motion.isActive(now))handler.postDelayed(this,60);
    }};
    private void redrawInteraction(){handler.removeCallbacks(animate);handler.post(animate);}
    private void cancelInteraction(){
        touchActive=false;dragging=false;motion.cancel();handler.removeCallbacks(animate);
        if(pet!=null){pet.pose(0,0);pet.setIdleAllowed(true);}
    }
    private final BroadcastReceiver screenReceiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){
        screenOn=!Intent.ACTION_SCREEN_OFF.equals(i.getAction());
        if(!screenOn)homeVisible=false;updateVisibility();if(homeMonitor!=null){if(!screenOn)homeMonitor.invalidateAndRefresh();else homeMonitor.refresh();}
    }};
    @Override protected void attachBaseContext(Context base){super.attachBaseContext(AppLanguage.wrap(base));}
    @Override public void onCreate(){
        super.onCreate(); store=new PetStore(this); windowContext=this;
        if(Build.VERSION.SDK_INT>=30){android.view.Display display=((DisplayManager)getSystemService(DISPLAY_SERVICE)).getDisplay(android.view.Display.DEFAULT_DISPLAY);
            if(display!=null)windowContext=createDisplayContext(display).createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,null);}
        wm=(WindowManager)windowContext.getSystemService(WINDOW_SERVICE);
        if(windowContext!=this)windowContext.registerComponentCallbacks(windowCallbacks);
        updateNotificationChannel();
        IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_SCREEN_OFF);f.addAction(Intent.ACTION_SCREEN_ON);f.addAction(Intent.ACTION_USER_PRESENT);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(screenReceiver,f,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(screenReceiver,f);
        screenOn=((PowerManager)getSystemService(POWER_SERVICE)).isInteractive();
        store.prefs().registerOnSharedPreferenceChangeListener(this);
        homeMonitor=new HomeMonitor(this,(home,observedAccess)->{boolean access=home?HomeMonitor.hasUsageAccess(this):observedAccess;boolean permitted=home&&screenOn&&HomeMonitor.isUnlocked(this)&&access;if(homeVisible!=permitted||usageAccess!=access){homeVisible=permitted;usageAccess=access;updateVisibility();}});homeMonitor.start();
    }
    @Override public int onStartCommand(Intent intent,int flags,int id){
        String action=intent==null?null:intent.getAction();
        if(STOP.equals(action)){stopSelf();return START_NOT_STICKY;}
        if(!HomeMonitor.hasUsageAccess(this)){Toast.makeText(this,R.string.service_usage_required,Toast.LENGTH_LONG).show();stopSelf();return START_NOT_STICKY;}
        if(!Settings.canDrawOverlays(this)){Toast.makeText(this,R.string.service_overlay_required,Toast.LENGTH_LONG).show();stopSelf();return START_NOT_STICKY;}
        try {
            if(Build.VERSION.SDK_INT>=34) startForeground(1,notification(),ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE); else startForeground(1,notification());
            if(pet==null) createPet();
            if(TOGGLE.equals(action)) hidden=!hidden;
            else hidden=false;
            updateVisibility();
        } catch(RuntimeException | java.io.IOException e){
            String message=e instanceof java.io.IOException
                ?getString(R.string.pet_show_image_failed,getString(PetStore.errorMessageResource(e)))
                :getString(R.string.pet_show_failed);
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();stopSelf();
        }
        return START_NOT_STICKY;
    }
    // Saved/raw touch coordinates are physical left-origin pixels in every locale.
    @android.annotation.SuppressLint("RtlHardcoded")
    private void createPet() throws java.io.IOException {
        atlas=store.load();pet=new PetView(AppLanguage.wrap(windowContext),atlas);
        pet.setIdleMotionEnabled(store.prefs().getBoolean("idle_motion",true));
        params=new WindowManager.LayoutParams(120,130,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.LEFT;
        computeBounds(true);
        pet.setOnClickListener(v->openChatGPT());
        pet.setOnTouchListener((v,event)->{
            switch(event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    downX=event.getRawX();downY=event.getRawY();startX=params.x;startY=params.y;
                    dragging=false;touchActive=true;pet.setIdleAllowed(false);motion.down(SystemClock.uptimeMillis());redrawInteraction();return true;
                case MotionEvent.ACTION_MOVE:
                    if(!touchActive)return true;
                    if(OverlayGeometry.isDrag(downX,downY,event.getRawX(),event.getRawY(),ViewConfiguration.get(windowContext).getScaledTouchSlop()))dragging=true;
                    if(dragging)movePet(event,true);return true;
                case MotionEvent.ACTION_UP:
                    if(!touchActive)return true;
                    if(OverlayGeometry.isDrag(downX,downY,event.getRawX(),event.getRawY(),ViewConfiguration.get(windowContext).getScaledTouchSlop()))dragging=true;
                    boolean tap=!dragging;
                    if(dragging){movePet(event,false);savePosition();}
                    cancelInteraction();
                    if(tap)v.performClick();return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_CANCEL:
                    if(touchActive&&dragging)savePosition();cancelInteraction();return true;
                default:return true;
            }
        });
        pet.setVisibility(View.GONE);wm.addView(pet,params);attached=true;
    }
    private void movePet(MotionEvent event,boolean animateMovement){
        int[] p=OverlayGeometry.clampPosition(startX+Math.round(event.getRawX()-downX),startY+Math.round(event.getRawY()-downY),params.width,params.height,screenWidth,screenHeight);
        int dx=p[0]-params.x,dy=p[1]-params.y;
        params.x=p[0];params.y=p[1];safeUpdate();
        // Only actual on-screen movement animates; pushing against an edge stays calm.
        if(animateMovement&&(dx!=0||dy!=0)){motion.moved(dx,dy,SystemClock.uptimeMillis());redrawInteraction();}
    }
    private void computeBounds(boolean restore){
        if(Build.VERSION.SDK_INT>=30){
            WindowMetrics m=wm.getCurrentWindowMetrics();
            Insets i=m.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
            screenWidth=Math.max(1,m.getBounds().width()-i.left-i.right);screenHeight=Math.max(1,m.getBounds().height()-i.top-i.bottom);
        }else{android.util.DisplayMetrics m=new android.util.DisplayMetrics();wm.getDefaultDisplay().getMetrics(m);screenWidth=m.widthPixels;screenHeight=m.heightPixels;}
        int[] size=OverlayGeometry.sizeForWidthDp(Math.max(48,Math.min(240,store.prefs().getInt("size",112))),windowContext.getResources().getDisplayMetrics().density);
        int[] fit=OverlayGeometry.clampSize(size[0],size[1],screenWidth,screenHeight);params.width=fit[0];params.height=fit[1];
        if(restore){params.x=Math.round(store.prefs().getFloat("x",0.8f)*Math.max(0,screenWidth-params.width));params.y=Math.round(store.prefs().getFloat("y",0.6f)*Math.max(0,screenHeight-params.height));}
        int[] p=OverlayGeometry.clampPosition(params.x,params.y,params.width,params.height,screenWidth,screenHeight);params.x=p[0];params.y=p[1];
    }
    private void safeUpdate(){try{if(attached)wm.updateViewLayout(pet,params);}catch(RuntimeException e){stopSelf();}}
    private void savePosition(){store.prefs().edit().putFloat("x",(float)params.x/Math.max(1,screenWidth-params.width)).putFloat("y",(float)params.y/Math.max(1,screenHeight-params.height)).apply();}
    private void updateVisibility(){
        if(pet==null)return;cancelInteraction();pet.setVisibility(hidden||!screenOn||!homeVisible?View.GONE:View.VISIBLE);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,notification());
    }
    private void updateNotificationChannel(){
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,getString(R.string.notification_channel),NotificationManager.IMPORTANCE_LOW));
    }
    private Notification notification(){
        PendingIntent settings=PendingIntent.getActivity(this,1,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent toggle=PendingIntent.getService(this,2,new Intent(this,PetService.class).setAction(TOGGLE),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop=PendingIntent.getService(this,3,new Intent(this,PetService.class).setAction(STOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(com.dot.floatingpet.R.drawable.ic_pet).setContentTitle(getString(!HomeMonitor.hasUsageAccess(this)?R.string.notification_usage_required:(hidden?R.string.notification_hidden:(homeVisible?R.string.notification_visible:R.string.notification_waiting))))
            .setContentText(getString(R.string.notification_help))
            .setContentIntent(settings).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(new Notification.Action.Builder(null,getString(hidden?R.string.notification_show:R.string.notification_hide),toggle).build())
            .addAction(new Notification.Action.Builder(null,getString(R.string.notification_stop),stop).build()).build();
    }
    private void openChatGPT(){
        Intent launch=getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
        if(launch==null){Toast.makeText(this,R.string.chatgpt_unavailable,Toast.LENGTH_LONG).show();return;}
        try{launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(launch);homeVisible=false;updateVisibility();if(homeMonitor!=null)homeMonitor.invalidateAndRefresh();}catch(ActivityNotFoundException|SecurityException e){Toast.makeText(this,R.string.chatgpt_launch_failed,Toast.LENGTH_LONG).show();}
    }
    @Override public void onConfigurationChanged(Configuration c){
        super.onConfigurationChanged(c);updateNotificationChannel();
        if(pet!=null){cancelInteraction();computeBounds(true);safeUpdate();((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1,notification());}
    }
    @Override public void onSharedPreferenceChanged(android.content.SharedPreferences p,String key){
        if(pet==null)return;
        if("idle_motion".equals(key))pet.setIdleMotionEnabled(p.getBoolean("idle_motion",true));
        if("size".equals(key)){computeBounds(true);safeUpdate();}
        if("fast_visibility".equals(key)&&homeMonitor!=null)homeMonitor.refresh();
        
    }
    @Override public void onDestroy(){
        if(homeMonitor!=null)homeMonitor.close();handler.removeCallbacksAndMessages(null);if(windowContext!=this)windowContext.unregisterComponentCallbacks(windowCallbacks);store.prefs().unregisterOnSharedPreferenceChangeListener(this);
        try{unregisterReceiver(screenReceiver);}catch(IllegalArgumentException ignored){}
        if(pet!=null)pet.setIdleMotionEnabled(false);
        if(attached){try{wm.removeView(pet);}catch(RuntimeException ignored){}attached=false;}
        if(atlas!=null){atlas.recycle();atlas=null;}pet=null;stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
