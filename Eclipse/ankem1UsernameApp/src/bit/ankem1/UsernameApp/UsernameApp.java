package bit.ankem1.UsernameApp;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;

public class UsernameApp extends Activity {

	// Event handler for onCreate method
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_username_app);
		
		// Get the EditText
		EditText edtUsername = (EditText)findViewById(R.id.edtUsername);
		
		// Create an instance of the class that implements the required interface/s
		KeyEventsHandler handler = new KeyEventsHandler();
		
		// Bind the event to the control
		edtUsername.setOnKeyListener(handler);
	}
	
	// An inner class that implements the required interfaces for KeyEvent handling
	public class KeyEventsHandler implements OnKeyListener
	{
		// Event handling code for a Key event
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) 
		{
			if(event.getAction() == KeyEvent.ACTION_DOWN)
			{
				// If the user has pressed Enter
				if (keyCode == KeyEvent.KEYCODE_ENTER)
				{
					// Get a reference to the EditText
					EditText editText = (EditText)findViewById(v.getId());
					
					// Get the text from the EditText
					String username = editText.getText().toString();
					
					// Decide which Toast message to show
					if(editText.getText().toString().length() != 8)
					{
						Toast.makeText(UsernameApp.this, "Usernames must be 8 characters, " + username, Toast.LENGTH_LONG).show();
					}
					else
					{
						Toast.makeText(UsernameApp.this, "Thank you " + username, Toast.LENGTH_LONG).show();
					}
	
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}	
		}		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.username_app, menu);
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
