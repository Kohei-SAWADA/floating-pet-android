package com.dot.floatingpet;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
    private static final int IMPORT=10, NOTIFY=11;
    private PetStore store; private TextView status,sizeLabel; private Button importButton,sampleButton;
    private static final ExecutorService worker=Executors.newSingleThreadExecutor();
    private static final java.util.concurrent.atomic.AtomicBoolean importing=new java.util.concurrent.atomic.AtomicBoolean();
    // Keep resource IDs, not translated text, so a locale change also updates the last result.
    private static volatile ImportOutcome importOutcome;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final Runnable importRefresh=new Runnable(){public void run(){refresh();if(importing.get())ui.postDelayed(this,300);}};
    @Override protected void attachBaseContext(Context base){super.attachBaseContext(AppLanguage.wrap(base));}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);AppLanguage.initialize(this);store=new PetStore(this);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(Color.rgb(255,248,243));
        LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);int pad=dp(24);content.setPadding(pad,pad,pad,pad);scroll.addView(content);
        // Respect system bars, including Android 15's enforced edge-to-edge window behavior.
        scroll.setOnApplyWindowInsetsListener((v,insets)->{int left=insets.getSystemWindowInsetLeft(),top=insets.getSystemWindowInsetTop(),right=insets.getSystemWindowInsetRight(),bottom=insets.getSystemWindowInsetBottom();content.setPadding(pad+left,pad+top,pad+right,pad+bottom);return insets;});
        label(content,R.string.app_name,30,Color.rgb(81,49,66));
        label(content,R.string.tagline,18,Color.rgb(120,88,103));
        label(content,R.string.introduction,15,Color.DKGRAY);
        label(content,R.string.language_heading,20,Color.rgb(81,49,66));
        String selectedLanguage=AppLanguage.selected(this);
        int currentLanguage=selectedLanguage.equals("en")?R.string.language_english:(selectedLanguage.equals("ja")?R.string.language_japanese:R.string.language_device);
        label(content,getString(R.string.language_current,getString(currentLanguage)),14,Color.DKGRAY);
        button(content,R.string.language_english,()->changeLanguage("en"));
        button(content,R.string.language_japanese,()->changeLanguage("ja"));
        button(content,R.string.language_device,()->changeLanguage(""));
        label(content,R.string.language_help,13,Color.rgb(100,86,94));
        status=label(content,"",14,Color.rgb(112,74,91));
        label(content,R.string.section_image,20,Color.rgb(81,49,66));
        label(content,R.string.image_requirements,14,Color.DKGRAY);
        sampleButton=button(content,R.string.use_sample_pet,()->startImport(store::importSampleImage));
        label(content,R.string.sample_pet_help,13,Color.rgb(100,86,94));
        importButton=button(content,R.string.select_image,()->{
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/png");
            try{startActivityForResult(i,IMPORT);}catch(ActivityNotFoundException e){toast(R.string.file_picker_unavailable);}
        });
        label(content,R.string.section_overlay,20,Color.rgb(81,49,66));
        label(content,R.string.overlay_help,14,Color.DKGRAY);
        button(content,R.string.open_overlay_settings,()->{
            try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}catch(ActivityNotFoundException e){toast(R.string.overlay_settings_unavailable);}
        });
        button(content,R.string.allow_notifications,()->{
            if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFY);
            else checkNotificationControls();
        });
        label(content,R.string.section_home,20,Color.rgb(81,49,66));
        label(content,R.string.usage_access_help,14,Color.DKGRAY);
        label(content,R.string.home_detection_limits,13,Color.rgb(100,86,94));
        button(content,R.string.open_usage_settings,()->{
            try{startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,Uri.parse("package:"+getPackageName())));}
            catch(ActivityNotFoundException e){try{startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));}catch(ActivityNotFoundException missing){toast(R.string.usage_settings_unavailable);}}
        });
        Switch fastMode=new Switch(this);
        fastMode.setText(R.string.response_priority);
        fastMode.setChecked(store.prefs().getBoolean("fast_visibility",true));content.addView(fastMode);
        fastMode.setOnCheckedChangeListener((button,checked)->store.prefs().edit().putBoolean("fast_visibility",checked).apply());
        label(content,R.string.response_priority_help,13,Color.rgb(100,86,94));
        label(content,R.string.section_preferences,20,Color.rgb(81,49,66));
        sizeLabel=label(content,"",15,Color.DKGRAY);
        SeekBar size=new SeekBar(this);size.setMax(192);size.setProgress(Math.max(0,Math.min(192,store.prefs().getInt("size",112)-48)));content.addView(size);updateSizeLabel();
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean user){if(user){store.prefs().edit().putInt("size",p+48).apply();updateSizeLabel();}}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        label(content,R.string.calm_heading,18,Color.rgb(81,49,66));
        label(content,R.string.calm_help,14,Color.DKGRAY);
        Switch idleMode=new Switch(this);
        idleMode.setText(R.string.idle_motion);
        idleMode.setChecked(store.prefs().getBoolean("idle_motion",true));content.addView(idleMode);
        idleMode.setOnCheckedChangeListener((button,checked)->store.prefs().edit().putBoolean("idle_motion",checked).apply());
        label(content,R.string.idle_motion_help,13,Color.rgb(100,86,94));
        button(content,R.string.show_pet,()->{
            if(!store.hasImage()){toast(R.string.image_required);return;}
            if(!HomeMonitor.hasUsageAccess(this)){toast(R.string.usage_access_required);return;}
            if(!Settings.canDrawOverlays(this)){toast(R.string.overlay_required);return;}
            try{startForegroundService(new Intent(this,PetService.class));toast(R.string.pet_start_requested);}catch(RuntimeException e){toast(R.string.pet_start_failed);}
        });
        button(content,R.string.stop_pet,()->{stopService(new Intent(this,PetService.class));toast(R.string.pet_stopped);});
        label(content,R.string.service_help,13,Color.rgb(100,86,94));
        setContentView(scroll);refresh();
    }
    private void changeLanguage(String language){
        if(AppLanguage.selected(this).equals(language))return;
        stopService(new Intent(this,PetService.class));
        try{
            AppLanguage.set(this,language);
            // Android 13+ updates app configuration itself; older versions use wrapped contexts.
            if(Build.VERSION.SDK_INT<33)recreate();
        }catch(RuntimeException unavailable){toast(R.string.language_change_failed);}
    }
    private void checkNotificationControls(){
        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel=manager==null?null:manager.getNotificationChannel(PetService.CHANNEL);
        boolean channelBlocked=channel!=null&&channel.getImportance()==NotificationManager.IMPORTANCE_NONE;
        if(manager!=null&&manager.areNotificationsEnabled()&&!channelBlocked){toast(R.string.notifications_ready);return;}
        toast(R.string.notifications_blocked);
        Intent settings=new Intent(channelBlocked?Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS:Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName());
        if(channelBlocked)settings.putExtra(Settings.EXTRA_CHANNEL_ID,PetService.CHANNEL);
        try{startActivity(settings);}catch(ActivityNotFoundException e){toast(R.string.notification_settings_unavailable);}
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
        super.onRequestPermissionsResult(request,permissions,results);
        if(request!=NOTIFY)return;
        if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)checkNotificationControls();
        else toast(R.string.notifications_denied);
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView label(LinearLayout parent,int textId,int size,int color){return label(parent,getString(textId),size,color);}
    private TextView label(LinearLayout parent,String text,int size,int color){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,dp(12),0,dp(8));parent.addView(v);return v;}
    private Button button(LinearLayout parent,int textId,Runnable action){Button b=new Button(this);b.setText(textId);b.setAllCaps(false);b.setOnClickListener(v->action.run());parent.addView(b,new LinearLayout.LayoutParams(-1,-2));return b;}
    private void updateSizeLabel(){sizeLabel.setText(getString(R.string.size_label,store.prefs().getInt("size",112)));}
    private void toast(int textId){toast(getString(textId));}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void refresh(){
        boolean busy=importing.get();
        if(importButton!=null)importButton.setEnabled(!busy);
        if(sampleButton!=null)sampleButton.setEnabled(!busy);
        if(status==null)return;
        if(busy){status.setText(R.string.image_checking);return;}
        String summary=getString(R.string.status_summary,
            getString(store.hasImage()?R.string.image_loaded:R.string.image_not_selected),
            getString(Settings.canDrawOverlays(this)?R.string.permission_granted:R.string.permission_not_set),
            getString(HomeMonitor.hasUsageAccess(this)?R.string.permission_granted:R.string.status_usage_required));
        ImportOutcome outcome=importOutcome;
        status.setText(outcome==null?summary:getString(R.string.status_with_outcome,summary,outcome.text(this)));
    }
    @Override protected void onResume(){super.onResume();ui.removeCallbacks(importRefresh);ui.post(importRefresh);}
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);if(request!=IMPORT||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();startImport(()->store.importImage(uri));
    }
    private interface ImageImporter { void run() throws java.io.IOException; }
    private void startImport(ImageImporter importer){
        if(!importing.compareAndSet(false,true)){toast(R.string.image_import_busy);return;}
        refresh();ui.removeCallbacks(importRefresh);ui.post(importRefresh);
        worker.execute(()->{
            ImportOutcome outcome;
            try{importer.run();stopService(new Intent(this,PetService.class));outcome=new ImportOutcome(R.string.image_imported,0);}
            catch(Exception e){outcome=new ImportOutcome(R.string.image_import_failed,PetStore.errorMessageResource(e));}
            importOutcome=outcome;importing.set(false);final ImportOutcome finished=outcome;
            runOnUiThread(()->{if(!isDestroyed()&&!isFinishing()){refresh();toast(finished.text(this));}});
        });
    }
    private static final class ImportOutcome {
        final int messageId,detailId;
        ImportOutcome(int messageId,int detailId){this.messageId=messageId;this.detailId=detailId;}
        String text(Context context){return detailId==0?context.getString(messageId):context.getString(messageId,context.getString(detailId));}
    }
    @Override protected void onPause(){ui.removeCallbacks(importRefresh);super.onPause();}
    @Override protected void onDestroy(){ui.removeCallbacksAndMessages(null);super.onDestroy();}
}
