package info.androidhive.gpluslogin;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by User on 01-Dec-15.
 */
public class MyActivity2 extends AppCompatActivity {
    
	Button log;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main5_slider);
        Toolbar mytoolbar =(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mytoolbar);
        log = (Button) findViewById(R.id.log_button);
        
        log.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MyActivity2.this, LoginActivity.class);
				startActivity(i);
				finish();
			}
		});

       
        
        
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
	        Toast.makeText(MyActivity2.this, "Imtiaz Abedin\n\nSumaita Sanila\n\nShakil Ahmed\n\nFarhana Shila\n\nRayan Sami",Toast.LENGTH_LONG).show();
	    }
//	    else if(item.getItemId() == R.id.Youtube){
//	        Toast.makeText(MainActivity.this, "Option pressed= youtube",Toast.LENGTH_LONG).show();
//	    }
//	    else if(item.getItemId() == R.id.Twitter){
//	        Toast.makeText(MainActivity.this, "Option pressed= twitter",Toast.LENGTH_LONG).show();
//	    }
	    return true;
	}
	
}