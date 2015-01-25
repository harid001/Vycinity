/**
 * Created by rahul_000 on 1/24/15.
 */


package tinovation.org.vycinity;


import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rahul_000 on 1/24/2015.
 */
public class StreamFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ArrayAdapter<String> mLocationAdapter;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    HashMap<String,String> mapOfLocations;


    public StreamFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLocationRequest();
        buildGoogleApiClient();
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filter_menu_item,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_stream, container, false);
        ListView v = (ListView) rootView.findViewById(R.id.stream_list);
        List<String> test = Arrays.asList(new String[]{"test","test","test","test","test","test","test","test","test"});
        mLocationAdapter = new ArrayAdapter<String>(getActivity(),R.layout.stream_item,R.id.textView3);
        mLocationAdapter.addAll(test);
        v.setAdapter(mLocationAdapter);
        return rootView;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnected(Bundle bundle) {
       // mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                //mGoogleApiClient);
        //Log.v("location", String.valueOf(mCurrentLocation.getLatitude()));
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        new GetLocationTask().execute(lat, lon);
    }


    public class GetLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            try {
                ArrayList<String> adapterStrings = new ArrayList<String>();
                mapOfLocations = new HashMap<String,String>();

                JSONObject json = new JSONObject(s);
                json = json.getJSONObject("response");
                JSONArray venues = json.getJSONArray("venues");
                for(int i = 0; i < venues.length(); i++){
                    JSONObject venObj = venues.getJSONObject(i);
                    String name = venObj.getString("name");
                    adapterStrings.add(name);
                    mapOfLocations.put(name,venObj.getJSONObject("location").getString("lat") + "," + venObj.getJSONObject("location").getString("lng"));
                }

                mLocationAdapter.clear();
                for(String test : adapterStrings){
                    mLocationAdapter.add(test);
                }
                mLocationAdapter.notifyDataSetChanged();


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String result = null;


            try {

                final String FORECAST_BASE_URL =
                        "https://api.foursquare.com/v2/venues/search?";
                final String LAT_LON = "ll";
                final String RADIUS = "radius";
                final String CATEGORY_ID = "categoryId";
                final String CLIENT_ID = "client_id";
                final String CLIENT_SECRET = "client_secret";
                final String VERSION = "v";
                final String STYLE = "m";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LAT_LON, params[0] + "," + params[1])
                        .appendQueryParameter(RADIUS, "1000")
                        .appendQueryParameter(CATEGORY_ID, "4d4b7105d754a06374d81259")
                        .appendQueryParameter(CLIENT_ID, "RWUD2MQ2OIX2P5E5IJBMBQKHDGMOGFE5CMSFCLHDKATCOCR2")
                        .appendQueryParameter(CLIENT_SECRET, "JJOZSJ2G4SKI3KO0D0GXQL4DOAD2QZ2YYPCFRX0RTFEIPQGI")
                        .appendQueryParameter(VERSION,"20140806")
                        .appendQueryParameter(STYLE,"foursquare")
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(StreamFragment.class.getSimpleName(), "Built URI " + builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                result = buffer.toString();

                Log.v(StreamFragment.class.getSimpleName(), result);
            } catch (IOException e) {
                //Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        //Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return result;
        }
    }

}