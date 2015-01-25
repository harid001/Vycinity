package tinovation.org.vycinity;

/**
 * Created by Hari on 1/24/15.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private CustomListAdapter mLocationAdapter;
    private HashMap<String,String> locationMap;

    public PlaceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLocationRequest();
        buildGoogleApiClient();
        setHasOptionsMenu(true);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place, container, false);

        mLocationAdapter = new CustomListAdapter(this.getActivity(),R.layout.data_item);


        ListView v = (ListView) rootView.findViewById(R.id.data_list);
        v.setAdapter(mLocationAdapter);

        TextView mini_location = (TextView) rootView.findViewById(R.id.current_location_text);
        TextView large_location = (TextView) rootView.findViewById(R.id.current_place_text);

        large_location.setText(MainActivity.myLocation);




        Typeface bold =Typeface.createFromAsset(getActivity().getAssets(),
                "RobotoCondensed-Bold.ttf");

        Typeface regular = Typeface.createFromAsset(getActivity().getAssets(),
                "RobotoCondensed-Regular.ttf");

        mini_location.setTypeface(regular);
        large_location.setTypeface(bold);


        return rootView;
    }

    @Override
    public void onConnected(Bundle bundle) {
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


    public class CustomListAdapter extends ArrayAdapter{


        public CustomListAdapter(Context context, int resource) {
            super(context,resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View vi = convertView;
            Typeface regular = Typeface.createFromAsset(getActivity().getAssets(),
                    "RobotoCondensed-Regular.ttf");
            Typeface italic = Typeface.createFromAsset(getActivity().getAssets(),
                    "RobotoCondensed-Italic.ttf");
            if (vi == null) {
                vi = inflater.inflate(R.layout.deal_item,null);
                Deal deal = (Deal)getItem(position);
                TextView title = (TextView) vi.findViewById(R.id.deal_title);
                title.setTypeface(regular);
                title.setText(deal.getTitle());
                TextView descrip = (TextView) vi.findViewById(R.id.deal_description);
                descrip.setTypeface(italic);
                descrip.setText(deal.getDescription());

            }
            //holder = (ViewHolder)vi.getTag();
            Deal deal = (Deal)getItem(position);
            TextView title = (TextView) vi.findViewById(R.id.deal_title);
            title.setTypeface(regular);
            title.setText(deal.getTitle());
            TextView descrip = (TextView) vi.findViewById(R.id.deal_description);
            descrip.setTypeface(italic);
            descrip.setText(deal.getDescription());
//            holder.title = (TextView) vi.findViewById(R.id.deal_title);
//            holder.description = (TextView) vi.findViewById(R.id.deal_description);

            return vi;
        }
    }

    public class GetLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            try {

                ArrayList<String> adapterStrings = new ArrayList<String>();

                JSONObject json = new JSONObject(s);
                locationMap = new HashMap<String,String>();
                json = json.getJSONObject("response");
                JSONArray venues = json.getJSONArray("venues");
                JSONObject venObj = venues.getJSONObject(0);
                String name = venObj.getString("name");
                if(!adapterStrings.contains(name)) {
                    adapterStrings.add(name);
                }
                String loc = venObj.getJSONObject("location").getString("lat") + "," + venObj.getJSONObject("location").getString("lng");


                TextView tv = (TextView) getView().findViewById(R.id.current_place_text);
                tv.setText(adapterStrings.get(0));
                locationMap.put(adapterStrings.get(0),loc);
                new GetInformation().execute(loc);
                //mLocationAdapter.notifyDataSetChanged();
                //Log.v("test", (String) mLocationAdapter.getItem(0));

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


    public class GetInformation extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            ArrayList<Deal> adapterStrings = null;
            try {
                adapterStrings = new ArrayList<Deal>();

                JSONObject json = new JSONObject(s);

                JSONArray deals = json.getJSONArray("deals");
                for(int i = 0; i < deals.length(); i++){
                    JSONObject venObj = deals.getJSONObject(i);
                    venObj = venObj.getJSONObject("deal");
                    String descript = venObj.getString("title");
                    venObj = venObj.getJSONObject("merchant");
                    String title = venObj.getString("name");
                    adapterStrings.add(new Deal(title,descript));
                }


                mLocationAdapter.clear();
                for(Deal test : adapterStrings){
                    mLocationAdapter.add(test);
                }
                //mInformationAdapter.notifyDataSetChanged();


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
                        "http://api.sqoot.com/v2/deals/?";
                final String API_KEY = "api_key";
                final String PER_PAGE = "per_page";
                final String CATEGORY_ID = "query";
                final String RADIUS = "radius";
                final String LOCATION = "location";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, "vfzhni")
                        .appendQueryParameter(PER_PAGE, "10")
                        .appendQueryParameter(CATEGORY_ID, "food")
                        .appendQueryParameter(RADIUS, "2")
                        .appendQueryParameter(LOCATION, params[0])
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
