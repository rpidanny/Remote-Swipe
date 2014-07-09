package com.example.swiper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Control extends Activity implements OnGestureListener,SensorEventListener{

	TextView textView;
	float previousx=0,previousy=0;
	
	String ipaddress="192.168.1.5";
	int port=5010;
	private Socket clientSocket=null;
	private DataOutputStream outToServer=null;
	boolean connectflag=false;
	private static final int RESULT_SETTINGS = 1;
	
	private GestureDetector gDetector;
	  private static final int SWIPE_THRESHOLD = 100;
      private static final int SWIPE_VELOCITY_THRESHOLD = 100;
	
      Button conb;
      TextView txtstatus;
      ImageView img;
      RelativeLayout myrelativelayout;
      
      boolean vibratorflag=true;
      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		gDetector = new GestureDetector(this);
		textView=(TextView)findViewById(R.id.textview1);
		txtstatus=(TextView)findViewById(R.id.errorMsg);
		
		img=(ImageView)findViewById(R.id.imageView1);
		myrelativelayout=(RelativeLayout) findViewById(R.id.myLayout);
		conb= (Button)findViewById(R.id.button1);
        conb.setOnClickListener(new OnClickListener() {
    
			@Override
			public void onClick(View arg0) {
				MyTask task = new MyTask();
		        task.execute();
			}
        });

		
	}
	
	public void connect()
	 {
		 final TextView view = (TextView) findViewById(R.id.errorMsg);
		 try {
			clientSocket= new Socket(ipaddress,port);
			outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
			
			connectflag=true;
			
			Control.this.runOnUiThread(new Runnable() {
		        public void run() {
		        	view.setText("");
		        	textView.setText("Connected");
		        	img.setImageResource(R.drawable.connected);
		        	myrelativelayout.setBackgroundColor(Color.parseColor("#E8E8E8"));
		        	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		        	// Start without a delay
		        	// Each element then alternates between vibrate, sleep, vibrate, sleep...
		        	long[] pattern = {0, 10, 100, 30, 20, 10, 50, 20, 10};

		        	// The '-1' here means to vibrate once
		        	// '0' would make the pattern vibrate indefinitely
		        	v.vibrate(pattern, -1);
		        }
		    });	
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Control.this.runOnUiThread(new Runnable() {
		        public void run() {
		        	view.setText("Don't know about host: hostname");
		        }
		    });	
			 connectflag=false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Control.this.runOnUiThread(new Runnable() {
		        public void run() {
		        	view.setText("Couldn't get I/O for the connection to: hostname");
		        }
		    });	
			  connectflag=false;
		}
	 }
	       
	 
	 public void sendData(String msg)
	 {
		TextView view = (TextView) findViewById(R.id.errorMsg);
		 if (clientSocket != null && outToServer != null) {
	          
	            try {
					outToServer.writeBytes(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					 view.setText("IOException:  " + e);
				}
	          
	            }
	           
	        }
		 
	 
	
	 public void closeSocket()
	 {
		 TextView view = (TextView) findViewById(R.id.errorMsg);
		 
		 if (clientSocket != null && outToServer != null) {
	            try {
	                outToServer.close();
	                clientSocket.close();
	                img.setImageResource(R.drawable.disconnected);
	                myrelativelayout.setBackgroundColor(Color.parseColor("#585858"));
	            } catch (UnknownHostException e) {
	               view.setText("Trying to connect to unknown host: " + e);
	            } catch (IOException e) {
	               view.setText("IOException:  " + e);
	            }
	        }
	 }
	 
	 

	 @Override
		public boolean onCreateOptionsMenu(Menu menu) {
		   getMenuInflater().inflate(R.menu.control, menu);
		     menu.add(1, 1, 0, "Disconnect");
		     menu.add(1, 2, 1, "Exit");
			return true;
		}
		
		@Override
	    public boolean onOptionsItemSelected(MenuItem item)
	    {
	    
	     switch(item.getItemId())
	     {
	     case 1:
	    	 closeSocket();
	    	 connectflag=false;
	    	 conb.setVisibility(View.VISIBLE);
	    	 textView.setText("Disconnected");
	    	 //img.setImageResource(R.drawable.disconnected);
	    	 
	      return true;
	     case 2:
	    	 Intent intent = new Intent(getApplicationContext(), MainActivity.class);
	 	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 	    startActivity(intent);
	      return true;
	     case R.id.action_settings:
				Intent i = new Intent(this, UserSettingActivity.class);
				startActivityForResult(i, RESULT_SETTINGS);
				break;

	     }
	     return super.onOptionsItemSelected(item);

	    }
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);

			switch (requestCode) {
			case RESULT_SETTINGS:
				showUserSettings();
				break;

			}

		}

		private void showUserSettings() {
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			ipaddress=sharedPrefs.getString("prefIpaddress", "NULL");
			port=Integer.parseInt(sharedPrefs.getString("prefPort", "8050"));
			vibratorflag=sharedPrefs.getBoolean("prefVibrate", true);

		}
		
		
		
		public void onDestroy() {
		    super.onDestroy(); 
		    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);
		    //System.exit(0);
		}
		
		
	   private class MyTask extends AsyncTask<String, String, String>{

			@Override
			protected String doInBackground(String... params) {
			
				connect();
				
				return null;
				
			}
			
			protected void onPostExecute(String result) {
				  if(connectflag){
				sendData("swiper connected...");
				
				//StringBuilder builder = new StringBuilder();

				//builder.append(ipaddress+":"+ Integer.toString(port));

				//txtstatus.setText(builder.toString());
				
				conb.setVisibility(View.GONE);
				
				//statusimg.setImageResource(R.drawable.connected);
				//mylinearlayout.setVisibility(View.VISIBLE);
				//myrelativelayout.setBackgroundColor(Color.parseColor("#E8E8E8"));
				  }
				  else{
				  //txtstatus.setText("Connection Failed!");
				  }
				  }
				
			}
	   

	   
	   

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		
		 boolean result = false;
         try {
             float diffY = e2.getY() - e1.getY();
             float diffX = e2.getX() - e1.getX();
             if (Math.abs(diffX) > Math.abs(diffY)) {
                 if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                     if (diffX > 0) {
                         onSwipeRight();
                     } else {
                         onSwipeLeft();
                     }
                 }
             } else {
                 if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                     if (diffY > 0) {
                         onSwipeBottom();
                     } else {
                         onSwipeTop();
                     }
                 }
             }
         } catch (Exception exception) {
             exception.printStackTrace();
         }
         return result;
	}
	
	public void onSwipeTop() {
		 if(connectflag){
        	 sendData("F");
        	 
        	 if(vibratorflag)
        	 {
        		 // Toast.makeText(Control.this, "top", Toast.LENGTH_SHORT).show();
        	        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        	        // Vibrate for 400 milliseconds
        	        v.vibrate(100);
        	 }
        }
		 
      
    }
    public void onSwipeRight() {
    	
        //Toast.makeText(Control.this, "right", Toast.LENGTH_SHORT).show();
        if(connectflag){
        	 sendData("R");
        	 if(vibratorflag)
        	 {
        		 Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        	        // Vibrate for 400 milliseconds
        	        v.vibrate(100);
        	 }
        }
       
       
    }
    public void onSwipeLeft() {
       // Toast.makeText(Control.this, "left", Toast.LENGTH_SHORT).show();
        if(connectflag){
        sendData("L");
        if(vibratorflag)
   	 {
        	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 400 milliseconds
            v.vibrate(100);
   	 }
        }
        
    }
    public void onSwipeBottom() {
    	 if(connectflag){
        	 sendData("B");
        	 if(vibratorflag)
        	 {
        		// Toast.makeText(Control.this, "bottom", Toast.LENGTH_SHORT).show();
        	        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        	        // Vibrate for 400 milliseconds
        	        v.vibrate(100);
        	 }
        }
       
    }

    
	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
	return gDetector.onTouchEvent(me);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}

