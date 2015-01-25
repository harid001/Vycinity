package tinovation.org.vycinity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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


public class InformationActivity extends ActionBarActivity {

    CustomListAdapter mInformationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_information, container, false);
            ListView li = (ListView) rootView.findViewById(R.id.information_list);
            mInformationAdapter = new CustomListAdapter(getActivity(),R.layout.deal_item);
            li.setAdapter(mInformationAdapter);
            Intent i = getActivity().getIntent();
            Log.v("location",i.getExtras().getString("location"));
            new GetInformation().execute(i.getExtras().getString("location"));
            return rootView;
        }
    }

    static class ViewHolder{
        TextView title;
        TextView description;
    }

    public class CustomListAdapter extends ArrayAdapter {

        public CustomListAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            LayoutInflater inflater = getLayoutInflater();
            View vi = convertView;
            if (vi == null) {
                vi = inflater.inflate(R.layout.deal_item,null);
                Deal deal = (Deal)getItem(position);
                //holder = new ViewHolder();
                TextView title = (TextView) vi.findViewById(R.id.deal_title);
                title.setText(deal.getTitle());
                TextView descrip = (TextView) vi.findViewById(R.id.deal_description);
                descrip.setText(deal.getDescription());
//                holder.title = title;
//                holder.description = descrip;
//                vi.setTag(holder);

            }
            //holder = (ViewHolder)vi.getTag();
            Deal deal = (Deal)getItem(position);
            TextView title = (TextView) vi.findViewById(R.id.deal_title);
            title.setText(deal.getTitle());
            TextView descrip = (TextView) vi.findViewById(R.id.deal_description);
            descrip.setText(deal.getDescription());
//            holder.title = (TextView) vi.findViewById(R.id.deal_title);
//            holder.description = (TextView) vi.findViewById(R.id.deal_description);

            return vi;
        }


    }

    public class GetInformation extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            try {
                ArrayList<Deal> adapterStrings = new ArrayList<Deal>();

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


                mInformationAdapter.clear();
                for(Deal test : adapterStrings){
                    mInformationAdapter.add(test);
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
                        .appendQueryParameter(PER_PAGE, "20")
                        .appendQueryParameter(CATEGORY_ID, "food")
                        .appendQueryParameter(RADIUS, "5")
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

            Log.v("test",result);
            return result;
        }
    }
}
