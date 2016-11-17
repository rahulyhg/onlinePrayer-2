package info.androidhive.gpluslogin;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;




import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	public static String TAG = LoginActivity.class.getSimpleName();
	
	private EditText emailFT, passwordFT;
	private Button loginButton, notRegButton;
	private ProgressDialog dialog;
	private SessionManager session;
	private SQLiteHandler db;
	ConnectionDetector cd;
	AlertDialogManager alert = new AlertDialogManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		loginButton = (Button) findViewById(R.id.signin_btn);
		notRegButton = (Button) findViewById(R.id.notreg_btn);
		emailFT = (EditText) findViewById(R.id.emailSF);
		passwordFT = (EditText) findViewById(R.id.passwordSF);
		
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		
		db = new SQLiteHandler(getApplicationContext());
		session = new SessionManager(getApplicationContext());
		
		if(session.isLoggedIn()){
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(i);
			finish();
		}
		
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(LoginActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
		
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String email = emailFT.getText().toString();
				String password = passwordFT.getText().toString();

				if (!email.isEmpty() && !password.isEmpty()) {
					checkLogin(email, password);
				}else{
					showMessage("Please fill out fields");
				}

			}
		});
		notRegButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Gmailactivity.class);
				startActivity(i);
				finish();
			}
		});

	}

	protected void checkLogin(final String email, final String password) {
		String tag_string_reg = "login_request";
		
		dialog.setMessage("logging in....");
		showDialog();
		StringRequest strReq = new StringRequest(Method.POST, AppConfig.LOGIN_URL,
			new Response.Listener<String>(){
	
				@Override
				public void onResponse(String response) {
					Log.d(TAG, "Login Response: " + response.toString());
					hideDialog();
					try{
						JSONObject jsb = new JSONObject(response);
						boolean error = jsb.getBoolean("error");
						if(!error){
							
							String uid = jsb.getString("uid");

							JSONObject user = jsb.getJSONObject("user");
							String name = user.getString("name");
							String email = user.getString("email");
							String created_at = user.getString("created_at");
							db.addUser(name, email, uid, created_at);
							
							session.setLogin(true);
							Intent i = new Intent(LoginActivity.this, MainActivity.class);
							startActivity(i);
							finish();
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
				params.put("tag", "login");
				params.put("email", email);
				params.put("password",password);
				return params;
			}			
		};
		
		AppController.getInstance().addToRequestQueue(strReq, tag_string_reg);

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
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
