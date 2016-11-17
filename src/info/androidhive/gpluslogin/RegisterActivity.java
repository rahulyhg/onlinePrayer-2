package info.androidhive.gpluslogin;


import java.util.HashMap;
import java.util.Map;








import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;









import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	static final String SENDER_ID = "602475542996"; 
	public static String TAG = RegisterActivity.class.getSimpleName();

	private Button goToLoginBtn, registerBtn;
	private EditText nameFT, emailFT, passwordFT;
	private SessionManager session;
	private SQLiteHandler db;
	private ProgressDialog pDialog;
	ConnectionDetector cd;
	
	String regId;
	
	AlertDialogManager alert = new AlertDialogManager();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main2_signup);
		
		goToLoginBtn = (Button) findViewById(R.id.gotologin_btn);
		registerBtn = (Button) findViewById(R.id.register_btn);
		nameFT = (EditText) findViewById(R.id.fullnameRF);
		emailFT = (EditText) findViewById(R.id.emailRF);
		passwordFT = (EditText) findViewById(R.id.passwordRF);
		
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(RegisterActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
		gcm();
		

		session = new SessionManager(getApplicationContext());
		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		db = new SQLiteHandler(getApplicationContext());

		if (session.isLoggedIn()) {
			gotoLogin();
		}

		goToLoginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(i);
				finish();
			}

		});
		registerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = nameFT.getText().toString();
				String email = emailFT.getText().toString();
				String password = passwordFT.getText().toString();

				if (name.isEmpty() && email.isEmpty() && password.isEmpty()) {
					showMessage("Please fill out ALL fields");
				} else {
					gcm();
					Log.d(TAG, "GCM Response: " + regId);
					registerUser(name, email, password,regId);
				}
			}
		});
		
		
		
		
		

	}

	private void registerUser(final String name, final String email, final String password, final String reg) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Registering ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST, AppConfig.REGISTER_URL, 
				new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Register Response: " + response.toString());
				hideDialog();

				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean("error");
					if (!error) {
						String uid = jObj.getString("uid");

						JSONObject user = jObj.getJSONObject("user");
						String name = user.getString("name");
						String email = user.getString("email");
						String created_at = user.getString("created_at");
						//db.addUser(name, email, uid, created_at);
						
						gotoLogin();

					} else {
						gotoLogin();
						String errorMsg = jObj.getString("error_msg");
						showMessage(errorMsg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Registration Error: " + error.getMessage());
				showMessage(error.getMessage());
				hideDialog();
			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				
				
				
				
				
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "register");
				params.put("name", name);
				params.put("email", email);
				params.put("password", password);
				params.put("regId", reg);

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

	public void gotoLogin() {
		Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	public void showDialog() {
		if (!pDialog.isShowing()) {
			pDialog.show();
		}
	}

	public void hideDialog() {
		if (pDialog.isShowing()) {
			pDialog.dismiss();
		}
	}
}
