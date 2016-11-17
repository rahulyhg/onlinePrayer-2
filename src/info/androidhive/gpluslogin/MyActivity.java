package info.androidhive.gpluslogin;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MyActivity extends Activity {


    /**
     * Called when the activity is first created.
     */



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main6_splash);

        // METHOD 1

        /****** Create Thread that will sleep for 5 seconds *************/
        Thread background = new Thread() {
            public void run() {

                try {
                    // Thread will sleep for 5 seconds
                    sleep(2000);
                    
                    // After 5 seconds redirect to another intent

                    Intent i = new Intent(MyActivity.this, MyActivity2.class);
                    startActivity(i);
                    //Remove activity
                    finish();

                } catch (Exception e) {

                }
            }
        };


        // start thread
        background.start();


    }


}
