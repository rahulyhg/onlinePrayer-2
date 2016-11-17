package info.androidhive.gpluslogin;
 
import android.app.*;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.Toast;
import info.androidhive.gpluslogin.MainActivity.CounterClass;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;


public class AlarmService extends IntentService 
{
    public AlarmManager alarmManager;
    Intent alarmIntent;
 //   PendingIntent pendingIntent;
   private static final int NOTIFICATION_ID = 1;
   private static final String TAG = "BANANEALARM";
   private NotificationManager notificationManager;
   private PendingIntent pendingIntent,pendingIntent2;
   long time_next,time1;
   String emni="Baalu";
   String need_next;
 
   public AlarmService() {
	      super("AlarmService");
	  }
   
   
   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
       return super.onStartCommand(intent,flags,startId);
   }
   
   @Override
   protected void onHandleIntent(Intent intent) {

           // don't notify if they've played in last 24 hr
	   Log.i(TAG,"Alarm Service has started.");
       Context context = this.getApplicationContext();
       
	
       notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
       Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent mIntent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle(); 
        bundle.putString("test", "test");
        mIntent.putExtras(bundle);
		pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Resources res = this.getResources();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		builder.setContentIntent(pendingIntent)
		            .setSmallIcon(R.drawable.ic_launcher)
		            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
		            .setTicker(res.getString(R.string.notification_title))
		            .setAutoCancel(true)
		            .setContentTitle(res.getString(R.string.notification_title))
		            .setContentText(res.getString(R.string.notification_subject))
                    .setSound(soundUri);
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		notificationManager.notify(NOTIFICATION_ID, builder.build());
		  Log.i(TAG,"POPO");
		
		  String tag_string_reg = "time_request";
//			dialog.setMessage("logging in....");
//				showDialog();
			StringRequest strReq = new StringRequest(Method.POST, AppConfig.TIME_URL,
				new Response.Listener<String>(){
		
					@Override
					public void onResponse(String response) {
						
						//long time;
						Log.d(TAG, "Time Response: " + response.toString());
						//hideDialog();
						try{
							JSONObject jsb = new JSONObject(response);
							boolean error = jsb.getBoolean("error");
							if(!error){
								
								
									need_next = jsb.getString("need_alarm");
									 time_next = Long.parseLong(need_next);
									 time_next=time_next*1000;
								 Log.d(TAG, "alarmserViCE ::"+" next "+time_next);
								  
								 time_next=time_next-(1*60*1000);
								 Log.d(TAG, "alarmserViCE ::"+" next alarm time "+time_next);
								 
							       alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

							       alarmIntent = new Intent(AlarmService.this, AlarmReceiver.class);
							       pendingIntent2 = PendingIntent.getBroadcast( AlarmService.this, 0, alarmIntent, 0);

							       Calendar alarmStartTime = Calendar.getInstance();
							       
							       Log.d(TAG, "alarmserViCE ::"+" just time "+time_next);
							       alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+time_next , 0, pendingIntent2);
								 
							}else{
								String errorMsg = jsb.getString("error_msg");
								
							}
						}
						catch(JSONException je){
							je.printStackTrace();						
						}
						
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, error.getMessage());
					
					}				
				}){
				@Override
				protected Map<String, String> getParams(){
					Map<String, String> params = new HashMap<String, String>();
					params.put("tag", "gettime_b");
					params.put("emni", emni);
					
					return params;
				}			
			};
			
			AppController.getInstance().addToRequestQueue(strReq, tag_string_reg);


       Log.i(TAG,"Alarms set every 30 minutes.");

       Log.i(TAG,"Notifications sent.");
		




    }


}