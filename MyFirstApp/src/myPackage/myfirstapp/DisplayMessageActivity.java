package myPackage.myfirstapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayMessageActivity extends Activity {

  private Socket socket;
  private static int SERVERPORT = 5000;
  private static String SERVER_IP = "192.168.0.17";
  
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_display_message);
    
    Intent intent = getIntent();
    SERVER_IP = intent.getStringExtra(MainActivity.IPaddr);
    SERVERPORT = intent.getIntExtra(MainActivity.PortNo, 5000);
    // Show the Up button in the action bar.
    setupActionBar();
    String logText;
    logText = displayOutput(intent);
    new NetworkOperation().execute(logText);
  }

  public String displayOutput(Intent intent)
  {
      File file = new File(getFilesDir()+File.separator+"accel_log.txt");
      if (!file.exists())
      {
        Toast.makeText(getApplicationContext(),"File doesn't exist!",Toast.LENGTH_LONG).show();
      }
      
      int lineCount = 0;
      StringBuilder text = new StringBuilder();
      try {
          BufferedReader br = new BufferedReader(new FileReader(file));
          String line;
          while ((line = br.readLine()) != null) {
              lineCount++;
              text.append(line);
              text.append('\n');
          }
          br.close();
      }
      catch (IOException e) {
          Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
          e.printStackTrace();
      } 
      
      String displayText = "Lines in log file = "+Integer.toString(lineCount);
      //file.delete();
      Log.d("ERROR", displayText);
      TextView textView = new TextView(this);
      textView.setTextSize(20);
      textView.setText(displayText);
      setContentView(textView);
      return text.toString();
      
  }
  
  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void setupActionBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.display_message, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      // This ID represents the Home or Up button. In the case of this
      // activity, the Up button is shown. Use NavUtils to allow users
      // to navigate up one level in the application structure. For
      // more details, see the Navigation pattern on Android Design:
      //
      // http://developer.android.com/design/patterns/navigation.html#up-vs-back
      //
      NavUtils.navigateUpFromSameTask(this);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
  
  private class NetworkOperation extends AsyncTask<String, Void , Integer> {

    @Override
    protected Integer doInBackground(String... params) {
      
      File file = new File(getFilesDir()+File.separator+"accel_log.txt");
      if (!file.exists())
      {
        Toast.makeText(getApplicationContext(),"File doesn't exist!",Toast.LENGTH_LONG).show();
      }
      StringBuilder text = new StringBuilder();
      try {
          InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
          socket = new Socket(serverAddr, SERVERPORT);
          PrintWriter out = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream())),
            true);
          
          BufferedReader br = new BufferedReader(new FileReader(file));
          String line;
          while ((line = br.readLine()) != null) {
              text.append(line);
              text.append('\n');
              out.println(line);
          }
          br.close();
          out.close();
          file.delete();
      }
      catch (IOException e) {
          //Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
          Log.d("Error : ", "Error reading file");
          e.printStackTrace();
      } 
      return 1;
    }
    
    
  }

  

}
