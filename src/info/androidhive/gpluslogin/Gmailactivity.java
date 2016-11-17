package info.androidhive.gpluslogin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.SessionState;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class Gmailactivity extends Activity implements OnClickListener,
		ConnectionCallbacks, OnConnectionFailedListener {
public static String TAG = Gmailactivity.class.getSimpleName();

	static final String SENDER_ID = "602475542996"; 
	private static final int RC_SIGN_IN = 0;
	String regId;
	// Logcat tag
	//private static final String TAG = "MainActivity";

	// Profile pic image size in pixels
	private static final int PROFILE_PIC_SIZE = 400;

	// Google client to interact with Google API
	public GoogleApiClient mGoogleApiClient;

	/**
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;

	private boolean mSignInClicked;

	private ConnectionResult mConnectionResult;

	private SignInButton btnSignIn;
	private Button btnSignOut, btnRevokeAccess, btnreg,btnfb;
	private ImageView imgProfilePic;
	private TextView txtName, txtEmail;
	private LinearLayout llProfileLayout;
	private SessionManager session1;
	ConnectionDetector cd;
	AlertDialogManager alert = new AlertDialogManager();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main3_reg);
		gcm();
		btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
		btnreg = (Button) findViewById(R.id.reg_sign_in);
		btnfb = (Button) findViewById(R.id.login_facebook_login_bt);
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(Gmailactivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
		
		session1 = new SessionManager(getApplicationContext());

		
//		if(session.isLoggedIn()){
//			Intent i = new Intent(getApplicationContext(), MainActivity.class);
//			startActivity(i);
//			finish();
//		}
		btnreg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Gmailactivity.this, RegisterActivity.class);
				startActivity(i);
				finish();
				
				
			}
		});
		
		btnfb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				Intent i = new Intent(Gmailactivity.this, FBActivity.class);
//				startActivity(i);
//				finish();
				openFacebookSession();
			}
		});
		
		// Button click listeners
		btnSignIn.setOnClickListener(this);
//		btnSignOut.setOnClickListener(this);
//		btnRevokeAccess.setOnClickListener(this);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	/**
	 * Method to resolve any signin errors
	 * */
	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}

		if (!mIntentInProgress) {
			// Store the ConnectionResult for later usage
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}

	}

//	@Override
//	protected void onActivityResult(int requestCode, int responseCode,
//			Intent intent) {
//		if (requestCode == RC_SIGN_IN) {
//			if (responseCode != RESULT_OK) {
//				mSignInClicked = false;
//			}
//
//			mIntentInProgress = false;
//
//			if (!mGoogleApiClient.isConnecting()) {
//				mGoogleApiClient.connect();
//			}
//		}
//	}

	@Override
	public void onConnected(Bundle arg0) {
		mSignInClicked = false;
		Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
		
		// Get user's information
		getProfileInformation();
		
		// Update the UI after signin
		updateUI(true);

	}
	
	
	
	//*pp//
	private void openFacebookSession(){
	    Session.openActiveSession( this, true, Arrays.asList("email", "user_birthday", "user_hometown", "user_location"), new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
//		    if (exception != null) {
//			Log.d("Facebook", exception.getMessage());
//			 
//		    }
			onSessionStateChange(session, state, exception);
	            Log.d("Facebook", "Session State: " + session.getState());
	           
	            
	            // you can make request to the /me API or do other stuff like post, etc. here
		}

	
	    });
	}
	
	
	private static Session openActiveSession(Activity activity, boolean allowLoginUI, List permissions, StatusCallback callback) { 
	    OpenRequest openRequest = new OpenRequest(activity).setPermissions(permissions).setCallback(callback);
	    Session session = new Session.Builder(activity).build();
	    if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
	        Session.setActiveSession(session);
	        session.openForRead(openRequest);
	        return session;
	    }
	    return null;
	}
	
	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		
//		LoginButton authButton=(LoginButton) findViewById(R.id.authButton);
//		List<String> permissions=new ArrayList<>();
//		permissions.add("public_profile");
//		permissions.add("email");
//		permissions.add("user_birthday");
//		authButton.setReadPermissions(permissions);
		
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
				public void onCompleted(GraphUser user,
						com.facebook.Response response) {
					
					if (user != null) {
						// Set view visibility to true
						//otherView.setVisibility(View.VISIBLE);
						// Set User name 
						//name.setText("Hello " + user.getName());
						
						gcm();
						registerUser(user.getName(),user.getProperty("email").toString(),regId);
						session1.setLogin(true);
						Log.i(TAG, user.getName());
						Intent i = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(i);
						finish();
						// Set Gender
						
						//id.setText("id " + user.getProperty("id").toString());
						
					}
					// TODO Auto-generated method stub
					
				}
			});
		Bundle parameters =new  Bundle();
		parameters.putString("fields","id,name,email");
		//parameters.putString("fields","email");
		request.setParameters(parameters);
		
		Request.executeBatchAsync(request);
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
			
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
		if (requestCode == RC_SIGN_IN) {
			if (resultCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		}
		
		else {
	    Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
	}
	

	
	

	/**
	 * Updating the UI, showing/hiding buttons and profile layout
	 * */
	private void updateUI(boolean isSignedIn) {
		
		
 if (isSignedIn && !session1.isLoggedIn()) {
			
	 signOutFromGplus();
			//btnSignIn.setVisibility(View.GONE);
			//session.setLogin(true);
		
			
		}
 else	if (isSignedIn) {
			
			//btnSignIn.setVisibility(View.GONE);
			//session.setLogin(true);
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(i);
			finish();
			
		} 
		
		
		
		else {
			btnSignIn.setVisibility(View.VISIBLE);
			btnreg.setVisibility(View.VISIBLE);
			
		}
	}

	/**
	 * Fetching user's information name, email, profile pic
	 * */
	private void getProfileInformation() {
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(mGoogleApiClient);
				String personName = currentPerson.getDisplayName();
				String personPhotoUrl = currentPerson.getImage().getUrl();
				String personGooglePlusProfile = currentPerson.getUrl();
				String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

				Log.e(TAG, "Name: " + personName + ", plusProfile: "
						+ personGooglePlusProfile + ", email: " + email
						+ ", Image: " + personPhotoUrl);

//				txtName.setText(personName);
//				txtEmail.setText(email);
				
				
				gcm();
				registerUser(  personName,  email,regId);
				
				// by default the profile url gives 50x50 px image only
				// we can replace the value with whatever dimension we want by
				// replacing sz=X
//				personPhotoUrl = personPhotoUrl.substring(0,
//						personPhotoUrl.length() - 2)
//						+ PROFILE_PIC_SIZE;

				//new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

			} else {
				Toast.makeText(getApplicationContext(),
						"Person information is null", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
		updateUI(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Button on click listener
	 * */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_sign_in:
			// Signin button clicked
			signInWithGplus();
			break;
		
		}
	}

	/**
	 * Sign-in into google
	 * */
	private void signInWithGplus() {
		if (!mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			session1.setLogin(true);
			
			resolveSignInError();
		}
	}

	/**
	 * Sign-out from google
	 * */
	private void signOutFromGplus() {
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
			updateUI(false);
		}
	}

	/**
	 * Revoking access from google
	 * */
	private void revokeGplusAccess() {
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
					.setResultCallback(new ResultCallback<Status>() {
						@Override
						public void onResult(Status arg0) {
							Log.e(TAG, "User access revoked!");
							mGoogleApiClient.connect();
							updateUI(false);
						}

					});
		}
	}
	
	
	
	private void registerUser(final String name, final String email,final String reg) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		

		StringRequest strReq = new StringRequest(Method.POST, AppConfig.REGISTER_URL, 
				new Response.Listener<String>() {

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
		}, new Response.ErrorListener() {

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
	
	

	/**
	 * Background Async task to load user profile picture from url
	 * */
//	private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
//		ImageView bmImage;
//
//		public LoadProfileImage(ImageView bmImage) {
//			this.bmImage = bmImage;
//		}
//
//		protected Bitmap doInBackground(String... urls) {
//			String urldisplay = urls[0];
//			Bitmap mIcon11 = null;
//			try {
//				InputStream in = new java.net.URL(urldisplay).openStream();
//				mIcon11 = BitmapFactory.decodeStream(in);
//			} catch (Exception e) {
//				Log.e("Error", e.getMessage());
//				e.printStackTrace();
//			}
//			return mIcon11;
//		}
//
//		protected void onPostExecute(Bitmap result) {
//			bmImage.setImageBitmap(result);
//		}
//	}
	
	
	
	
	private void showMessage(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}
	


}
