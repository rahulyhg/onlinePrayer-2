package info.androidhive.gpluslogin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gcm.GCMRegistrar;


public class FBActivity extends Activity {
	// Create, automatically open (if applicable), save, and restore the 
	// Active Session in a way that is similar to Android UI lifecycles. 
	private UiLifecycleHelper uiHelper;
	private View otherView;
	public Button btn;
	private AsyncFacebookRunner mAsyncRunner;
	private static final String TAG = "MainActivity";
	static final String SENDER_ID = "602475542996";
	String regId;
	private SessionManager sessionf;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fbmain);
		// Set View that should be visible after log-in invisible initially
//		btn=(Button) findViewById(R.id.authButton);
		
		//otherView = (View) findViewById(R.id.other_views);
		//otherView.setVisibility(View.GONE);
		sessionf = new SessionManager(getApplicationContext());
		// To maintain FB Login session
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
	}
	
	// Called when session changes
	private Session.StatusCallback callback = new Session.StatusCallback() {
				@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	
	// When session is changed, this method is called from callback method
	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		
		LoginButton authButton=(LoginButton) findViewById(R.id.authButton);
		List<String> permissions=new ArrayList<>();
		permissions.add("public_profile");
		permissions.add("email");
		permissions.add("user_birthday");
		authButton.setReadPermissions(permissions);
		
//		final TextView name = (TextView) findViewById(R.id.name);
//		final TextView id = (TextView) findViewById(R.id.gender);
//		final TextView location = (TextView) findViewById(R.id.location);
		// When Session is successfully opened (User logged-in)
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			// make request to the /me API to get Graph user
		Request request=	Request.newMeRequest(session, new GraphUserCallback() {

				// callback after Graph API response with user
				// object
				@Override
				public void onCompleted(GraphUser user, Response response) {
			
					if (user != null) {
						// Set view visibility to true
						//otherView.setVisibility(View.VISIBLE);
						// Set User name 
						//name.setText("Hello " + user.getName());
						
						gcm();
						registerUser(user.getName(),user.getProperty("email").toString(),regId);
						sessionf.setLogin(true);
						Log.i(TAG, "Session True Set");
						Intent i = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(i);
						finish();
						// Set Gender
						
						//id.setText("id " + user.getProperty("id").toString());
						
					}
				}
			});
		Bundle parameters =new  Bundle();
		parameters.putString("fields","id,name,email");
		//parameters.putString("fields","email");
		request.setParameters(parameters);
		
		Request.executeBatchAsync(request);
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
			otherView.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "OnActivityResult...");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
	
	
	private void registerUser(final String name, final String email,final String reg) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		

		StringRequest strReq = new StringRequest(Method.POST, AppConfig.REGISTER_URL, 
				new Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Register Response: " + response.toString());
			

				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean("error");
					if (!error) {
						String uid = jObj.getString("uid");

						JSONObject user = jObj.getJSONObject("user");
						String name = user.getString("name");
						String email = user.getString("email");
						String created_at = user.getString("created_at");
						

					} else {
						//session.setLogin(true);
						String errorMsg = jObj.getString("error_msg");
						showMessage(errorMsg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Registration Error: " + error.getMessage());
				showMessage(error.getMessage());
				
			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "gmailregister");
				params.put("name", name);
				params.put("email", email);
				params.put("regId", reg);
				//params.put("password", password);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}
	
	public void gcm(){
		
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);

		//lblMessage = (TextView) findViewById(R.id.lblMessage);
		
//		registerReceiver(mHandleMessageReceiver, new IntentFilter(
//				DISPLAY_MESSAGE_ACTION));
		
		// Get GCM registration id
		regId = GCMRegistrar.getRegistrationId(this);

		// Check if regid already presents
		if (regId.equals("")) {
			// Registration is not present, register now with GCM			
			GCMRegistrar.register(this, SENDER_ID);
			regId = GCMRegistrar.getRegistrationId(this);
		} else {
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.				
				regId = GCMRegistrar.getRegistrationId(this);
				//Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
			} 
		}
		Log.d(TAG, "GCM Response: " + regId);
		
	}
	
	private void showMessage(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}
	
}

