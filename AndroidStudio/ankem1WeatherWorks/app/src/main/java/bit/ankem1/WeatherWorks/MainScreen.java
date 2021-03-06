package bit.ankem1.WeatherWorks;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import bit.ankem1.WeatherWorks.MetserviceApi.MetserviceResponse;
import bit.ankem1.WeatherWorks.MetserviceApi.MetserviceRestClient;
import bit.ankem1.WeatherWorks.OpenWeatherMapApi.OpenWeatherMapRestClient;
import bit.ankem1.WeatherWorks.WeatherUndergroundApi.WeatherUndergroundResponse;
import bit.ankem1.WeatherWorks.WeatherUndergroundApi.WeatherUndergroundRestClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import bit.ankem1.WeatherWorks.OpenWeatherMapApi.OpenWeatherMapResponse;

public class MainScreen extends ActionBarActivity
{
    // Shared Preferences Filename
    private final String PREFS_NAME = "metservice_location";
    // Constants for polling location
    private final long INTERVAL = 0;
    private final float DISTANCE = 0;
    // The custom Application class
    protected WeatherWorksApplication app;
    // Api keys
    private final String OPENWEATHERMAP_KEY = "9c77b952a7356dbff8d7341b3ad07025";
    private final String WEATHERUNDERGROUND_KEY = "5c4cb075a7f03f66";
    // Weather Underground API options
    private final String WEATHERUNDERGROUND_FORECASTOPTION = "forecast10day";
    // Screen controls
    TextView txtMetserDescription;
    TextView txtWUDescription;
    TextView txtOWMDescription;
    ListView lvProviders;
    // Location
    double latitude;
    double longitude;
    // City location
    String city;
    // The LocationManager
    LocationManager locationManager;
    // Available weather Providers
    ArrayList<Provider> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // Set basic ui settings
        setTitle("Weather Works");

        // Get the application instance
        app = (WeatherWorksApplication)getApplication();

        // Get references to controls
        txtMetserDescription = (TextView)findViewById(R.id.txtMainMetserviceDescription);
        txtOWMDescription = (TextView)findViewById(R.id.txtMainOpenWeatherMapDescription);
        txtWUDescription = (TextView)findViewById(R.id.txtMainWeatherUndergroundDescription);
        lvProviders = (ListView)findViewById(R.id.lvProviders);

        // Bind click handlers
        lvProviders.setOnItemClickListener(new ListViewClickHandler());

        // Turn on location polling
        requestLocationUpdates();

        // Get latitude and longitude values
        Location loc = getLastKnownLocation();

        // This loop can potentially run for a long time.
        while (loc == null)
            loc = getLastKnownLocation();


        longitude = loc.getLongitude();
        latitude = loc.getLatitude();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        city = settings.getString("location", "dunedin");

        // Pull all weather info, store in WeatherStore.
        pullOpenWeatherMap();
        pullMetservice();
        pullWeatherUnderground();

        // Populate the ListView with available providers
        populateProviderListView();

    }

    // Inner class for handling ListView click events
    public class ListViewClickHandler implements AdapterView.OnItemClickListener
    {

        // Event handling code for selecting an item from the ListView
        @Override
        public void onItemClick(AdapterView<?> list, View itemView, int position, long id)
        {
            // Create the Activity we want to launch
            ForecastDaysScreen forecastScreen = new ForecastDaysScreen();

            // Create an intent
            Intent goToIntent = new Intent(MainScreen.this, forecastScreen.getClass());

            // Put a integer value in the intent to indicate which provider we chose.
            goToIntent.putExtra("provider", position);

            startActivity(goToIntent);

        }

    }

    // Pull weather data from weatherunderground, store in WeatherStore
    public void pullWeatherUnderground()
    {
        // Create a string that holds all parameters that we need
        String urlString = WEATHERUNDERGROUND_KEY + "/" + WEATHERUNDERGROUND_FORECASTOPTION +
                "/q/" + latitude + "," + longitude + ".json";


        // Get the Interface from the RestClient, call necessary functions
        WeatherUndergroundRestClient.get().getWeather(urlString, new Callback<WeatherUndergroundResponse>() {
            @Override
            public void success(WeatherUndergroundResponse wuResponse, Response response)
            {
                txtWUDescription.setText(wuResponse.getForecast().getTxt_forecast().getForecastday()[0].getFcttext_metric());
                // Save a reference to the entire object hierarchy for use later.
                WeatherStore.getInstance().setWuResponse(wuResponse);
            }

            @Override
            public void failure(RetrofitError error)
            {
                // Report the error
                txtWUDescription.setText("Error loading data.");
            }
        });
    }

    // Pull weather data from metservice, store in WeatherStore
    public void pullMetservice()
    {
        // Get the Interface from the RestClient, call necessary functions
        MetserviceRestClient.get().getWeather(city, new Callback<MetserviceResponse>() {
            @Override
            public void success(MetserviceResponse metserviceResponse, Response response)
            {
                txtMetserDescription.setText(metserviceResponse.getDays()[0].getForecast());
                // Save a reference to the returned object in the singleton
                WeatherStore.getInstance().setMetserviceResponse(metserviceResponse);
            }

            @Override
            public void failure(RetrofitError error)
            {
                // Report the error
                txtMetserDescription.setText("Error loading data.");
            }
        });
    }

    // Pull weather data from OpenWeatherMap, store in WeatherStore
    public void pullOpenWeatherMap()
    {
        // Create and populate a dictionary of keys and values for the url string
        HashMap<String, String> urlParamMap = new HashMap<>();
        urlParamMap.put("lat", String.valueOf(latitude));
        urlParamMap.put("lon", String.valueOf(longitude));
        urlParamMap.put("cnt", "10");
        urlParamMap.put("mode", "json");
        urlParamMap.put("APPID", OPENWEATHERMAP_KEY);

        // Get the Interface from the RestClient, call necessary functions
        OpenWeatherMapRestClient.get().getWeather(urlParamMap, new Callback<OpenWeatherMapResponse>()
        {
            @Override
            public void success(OpenWeatherMapResponse owmResponse, Response response)
            {
                txtOWMDescription.setText(owmResponse.getList()[0].getWeather()[0].getDescription());
                // Save a reference to the entire object hierarchy for use later.
                WeatherStore.getInstance().setOwmResponse(owmResponse);
            }

            @Override
            public void failure(RetrofitError error)
            {
                // Report the error
                txtOWMDescription.setText("Error loading data.");
            }
        });
    }

    // Create an ArrayList, use it to create the custom adapter,
    // bind the adapter to our ListView (lvProviders)
    public void populateProviderListView()
    {
        // Create and populate a List of Providers

        providers = new ArrayList<>();

        Resources resources = getResources();

        String owmImageName = resources.getResourceEntryName(R.drawable.owm);
        String metserviceImageName = resources.getResourceEntryName(R.drawable.metservice);
        String wuImageName = resources.getResourceEntryName(R.drawable.wu);

        int owmResourceId = resources.getIdentifier(owmImageName, "drawable", getPackageName());
        int metserviceResourceId = resources.getIdentifier(metserviceImageName, "drawable", getPackageName());
        int wuResourceId = resources.getIdentifier(wuImageName, "drawable", getPackageName());

        // Populate List
        providers.add(new Provider("Metservice", metserviceResourceId));
        providers.add(new Provider("Open Weather Map", owmResourceId));
        providers.add(new Provider("Weather Underground", wuResourceId));

        // Make custom adapter
        ProviderArrayAdapter providerArrayAdapter = new ProviderArrayAdapter(MainScreen.this,
                R.layout.provider_list_item, providers);

        // Set the ListView's adapter
        lvProviders.setAdapter(providerArrayAdapter);
    }

    // Get the last known location. Return a location object.
    public Location getLastKnownLocation()
    {
        // Create a Criteria instance to indicate the application criteria for selecting a location provider
        Criteria defaultCriteria = new Criteria();

        // Get the provider we will use
        String providerName = locationManager.getBestProvider(defaultCriteria, false);

        // Read the last known location
        Location currentLocation = locationManager.getLastKnownLocation(providerName);

        return currentLocation;
    }

    // Set up automatic polling for current location
    public void requestLocationUpdates()
    {
        // Get the LocationManager and set up automatic polling for current location
        // A location manager gives us access to system location services
        locationManager = (LocationManager)getSystemService(MainScreen.LOCATION_SERVICE);

        // Create a Criteria instance to indicate the application criteria for selecting a location provider
        Criteria defaultCriteria = new Criteria();

        // Get the provider we will use
        String providerName = locationManager.getBestProvider(defaultCriteria, false);

        // Request updates
        locationManager.requestLocationUpdates(providerName, INTERVAL, DISTANCE, new CustomLocationListener());
    }

    // Inner class: CustomLocationListener - responds to location based events
    private class CustomLocationListener implements LocationListener
    {
        // Called when our location changes
        @Override
        public void onLocationChanged(Location location)
        {
            // Update location
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onProviderDisabled(String provider) { }
    }

    // This class is an extension of ArrayAdapter and its
    // purpose is to return a layout with a text field and an image field.
    public class ProviderArrayAdapter extends ArrayAdapter<Provider>
    {
        public ProviderArrayAdapter(Context context, int resource, ArrayList<Provider> objects)
        {
            super(context, resource, objects);
        }

        // Override the getView() method
        @Override
        public View getView(int position, View convertView, ViewGroup container)
        {
            // Get a LayoutInflater
            LayoutInflater inflater = LayoutInflater.from(MainScreen.this);

            // Inflate top_artist_list_item and store the returned View in a variable
            View customView = inflater.inflate(R.layout.provider_list_item, container, false);

            // Get references to the controls in provider_list_item.
            TextView txtProviderName = (TextView)customView.findViewById(R.id.txtProviderName);
            ImageView ivProviderImage = (ImageView)customView.findViewById(R.id.ivProviderImage);

            // Get the current Artist instance. Use the Adapter base class's getItem command
            Provider providerItem = getItem(position);

            // Use the data fields of the current Provider instance to initialize the View controls correctly
            String providerName = providerItem.getName();
            int providerImageResource = providerItem.getImageResource();

            // Set the text property of the TextView
            txtProviderName.setText(providerName);

            // Set the image resource for the ImageView
            ivProviderImage.setImageResource(providerImageResource);

            // Return our custom view
            return customView;
        }
    }

    @Override
    protected void onActivityResult(int requestId, int resultCode, Intent returnIntent)
    {
        //---------------------------
        // Retrieve the requested data

        // Determine appropriate action based on the request ID
        if(requestId == 0)
        {
            // Did the activity return 'Okay'?
            if(resultCode == Activity.RESULT_OK)
            {
                // Get the data from the returnIntent
                String returnedLocation = returnIntent.getStringExtra("location" );

                // Modify our preferences file
                // We need an Editor object to make preference changes.
                // All objects are from android.context.Context
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("location", returnedLocation);

                city = returnedLocation;

                // Commit the edits
                editor.commit();

                // Pull all weather info, store in WeatherStore.
                pullOpenWeatherMap();
                pullMetservice();
                pullWeatherUnderground();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            // Get the desired location from the settings activity
            // Call activity for result
            Intent intentForResult = new Intent(MainScreen.this, Settings.class);

            // The second parameter is an id to track the request.
            startActivityForResult(intentForResult, 0);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
