package info.androidhive.gpluslogin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;












import com.google.android.gms.common.api.GoogleApiClient;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	
	public static String TAG = MainActivity.class.getSimpleName();
	private TextView fullnameTV, emailTV,textViewTime,textViewdate;
	private Button logoutBtn,Timer;
	Calendar calendar;
	private SessionManager session;
	String emni="Baal";
	private SQLiteHandler db;
	long time,time1,timenoti,time_next;
	
	public AlarmManager alarmManager;
	Intent alarmIntent;
	PendingIntent pendingIntent;
	
	private ProgressDialog dialog;
	String need,need_next;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main4_after_login);
		Toolbar mytoolbar =(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mytoolbar);
		checknext(emni);
		
		
		
		//fullnameTV = (TextView) findViewById(R.id.nameWF);
		//emailTV = (TextView) findViewById(R.id.emailWF);
		textViewTime = (TextView) findViewById(R.id.textViewTime);
		textViewdate = (TextView) findViewById(R.id.textViewdate);
		logoutBtn = (Button) findViewById(R.id.logout_btn);
		//pbtn = (Button) findViewById(R.id.push_btn);
		//Timer=     (Button) findViewById(R.id.button1);
		
		session = new SessionManager(getApplicationContext());
		//db = new SQLiteHandler(getApplicationContext());
		
		if(!session.isLoggedIn()){
			logoutUser();
		}
		
		//HashMap<String, String> user = db.getUserDetails();
		//String name = user.get("name");
		//String email = user.get("email");
		//fullnameTV.setText("Name: " + name);
		//emailTV.setText("email: " + email );
		
		calendar = Calendar.getInstance();
		//date format is:  "Date-Month-Year Hour:Minutes am/pm"
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy"); //Date and time
		String currentDate = sdf.format(calendar.getTime());

		//Day of Name in full form like,"Saturday", or if you need the first three characters you have to put "EEE" in the date format and your result will be "Sat".
		SimpleDateFormat sdf_ = new SimpleDateFormat("EEEE"); 
		Date date = new Date();
		String dayName = sdf_.format(date);
		textViewdate.setText("" + dayName + " " + currentDate + "");
		
		
		//addme();
		
//		//time=time*1000;
//		CounterClass timer = new CounterClass(time, 1000);
//		 Log.d(TAG, "Timeb ::" + time);
//	timer.start();
		

		
		
		logoutBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				logoutUser();		

					
			}
		});
	
		}

	
	protected void checknext(final String emni) {
		String tag_string_reg = "time_request";
//		dialog.setMessage("logging in....");
//			showDialog();
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
							
							need = jsb.getString("need");
							 time = Long.parseLong(need);
							 time=time*1000;
							 Log.d(TAG, "Timejust Next ::" + time);
							 if((time-(1*60*1000))<0){
								 
								 check_next_to_next(emni);
								 
							 }
						
								//need_next = jsb.getString("need_alarm");
								 //time_next = Long.parseLong(need_next);
								 //time_next=time_next*1000;
							// Log.d(TAG, "Timea ::" + time);
							  
							 setAlarm();
							 CounterClass timer = new CounterClass(time, 1000);
							// Log.d(TAG, "Timeb ::" + time);
						     timer.start();
						     
							
								
//							JSONObject user = jsb.getJSONObject("user");
//							String name = user.getString("name");
//							String email = user.getString("email");
//							String created_at = user.getString("created_at");
							//db.addUser(name, email, uid, created_at);
							
							//session.setLogin(true);
//							Intent i = new Intent(LoginActivity.this, MainActivity.class);
//							startActivity(i);
//							finish();
						}else{
							String errorMsg = jsb.getString("error_msg");
							showMessage(errorMsg);
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
					showMessage(error.getMessage());
					hideDialog();
				}				
			}){
			@Override
			protected Map<String, String> getParams(){
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "gettime_a");
				params.put("emni", emni);
				//params.put("password",password);
				return params;
			}			
		};
		
		AppController.getInstance().addToRequestQueue(strReq, tag_string_reg);

	}
	
	
	protected void check_next_to_next(final String emni) {
		String tag_string_reg = "time_request";
//		dialog.setMessage("logging in....");
//			showDialog();
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
								 time = Long.parseLong(need_next);
								 time=time*1000;
							 Log.d(TAG, "Timea :: Next to Next" + time);
							  
							
						     
							
								
//							JSONObject user = jsb.getJSONObject("user");
//							String name = user.getString("name");
//							String email = user.getString("email");
//							String created_at = user.getString("created_at");
							//db.addUser(name, email, uid, created_at);
							
							//session.setLogin(true);
//							Intent i = new Intent(LoginActivity.this, MainActivity.class);
//							startActivity(i);
//							finish();
						}else{
							String errorMsg = jsb.getString("error_msg");
							showMessage(errorMsg);
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
					showMessage(error.getMessage());
					hideDialog();
				}				
			}){
			@Override
			protected Map<String, String> getParams(){
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "gettime_b");
				params.put("emni", emni);
				//params.put("password",password);
				return params;
			}			
		};
		
		AppController.getInstance().addToRequestQueue(strReq, tag_string_reg);

	}
	
	private void logoutUser(){
		
	
		
		session.setLogin(false);
		//db.deleteUsers();
	
		
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
		
	
		finish();
	}
	private void showMessage(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), 
				msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}
	private void showDialog(){
		if(!dialog.isShowing()){
			dialog.show();
		}
	}
	
	private void hideDialog(){
		if(dialog.isShowing()){
			dialog.dismiss();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		super.onOptionsItemSelected(item);
	    if(item.getItemId() == R.id.action_about){
	        Toast.makeText(MainActivity.this, "Imtiaz Abedin\n\nSumaita Sanila\n\nShakil Ahmed\n\nFarhana Shila\n\nRayan Sami",Toast.LENGTH_LONG).show();
	    }
	    return true;
	}
	
	
	
	public class CounterClass extends CountDownTimer {

		public CounterClass(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}

		@SuppressLint("NewApi")
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			
			long millis = millisUntilFinished;
			String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
					TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
					TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
//			if(hms.equals("00:30:00")){
//				
//				 int icon = R.drawable.ic_launcher;
//				    long when = System.currentTimeMillis();
//				    NotificationManager nm=(NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
//				    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
//				    PendingIntent  pending=PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//				    Notification notification;
//				        if (Build.VERSION.SDK_INT < 11) {
//				            notification = new Notification(icon, "Title", when);
//				            notification.setLatestEventInfo(
//				            		getApplicationContext(),
//				                    "ONLINEPRAYER",
//				                    "30 minutes till Next Prayer",
//				                    pending);
//				        } else {
//				            notification = new Notification.Builder(getApplicationContext())
//				                    .setContentTitle("ONLINEPRAYER")
//				                    .setContentText(
//				                            "30 minutes till Next Prayer").setSmallIcon(R.drawable.ic_launcher)
//				                    .setContentIntent(pending).setWhen(when).setAutoCancel(true)
//				                    .build();
//				        }
//				    notification.flags |= Notification.FLAG_AUTO_CANCEL;
//				    notification.defaults |= Notification.DEFAULT_SOUND;
//				    nm.notify(0, notification);
//				
//			}
			textViewTime.setText(hms);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			textViewTime.setText("");
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
								
								need = jsb.getString("need");
								 time = Long.parseLong(need);
								 time=time*1000;
									need_next = jsb.getString("need_alarm");
									// time_next = Long.parseLong(need_next);
									 //time_next=time_next*1000;
								// Log.d(TAG, "Timea ::" + time+" next "+time_next);
								  
								 
								 CounterClass timer = new CounterClass(time, 1000);
								// Log.d(TAG, "Timeb ::" + time);
							     timer.start();
							     
								
									
//								JSONObject user = jsb.getJSONObject("user");
//								String name = user.getString("name");
//								String email = user.getString("email");
//								String created_at = user.getString("created_at");
								//db.addUser(name, email, uid, created_at);
								
								//session.setLogin(true);
//								Intent i = new Intent(LoginActivity.this, MainActivity.class);
//								startActivity(i);
//								finish();
							}else{
								String errorMsg = jsb.getString("error_msg");
								showMessage(errorMsg);
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
						showMessage(error.getMessage());
						hideDialog();
					}				
				}){
				@Override
				protected Map<String, String> getParams(){
					Map<String, String> params = new HashMap<String, String>();
					params.put("tag", "gettime_a");
					params.put("emni", emni);
					//params.put("password",password);
					return params;
				}			
			};
			
			AppController.getInstance().addToRequestQueue(strReq, tag_string_reg);

			
		}
		
		
		
	}
	
	
	public void setAlarm(){
		
		timenoti=time-(1*60*1000);
		
		Log.d(TAG, "First alarm time ::" + timenoti);
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
		pendingIntent = PendingIntent.getBroadcast(  MainActivity.this, 0, alarmIntent, 0);
		
		Calendar alarmStartTime = Calendar.getInstance();
		alarmStartTime.add(Calendar.MINUTE, 1);
	 	alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+timenoti , 0, pendingIntent);
	 	Log.i(TAG,"Alarms set every 30 minutes.");

	}
}
