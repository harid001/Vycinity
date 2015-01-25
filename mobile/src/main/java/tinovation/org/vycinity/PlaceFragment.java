package tinovation.org.vycinity;

/**
 * Created by Hari on 1/24/15.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceFragment extends Fragment {

    public PlaceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_place, container, false);

        CustomListAdapter test = new CustomListAdapter(this.getActivity(),R.layout.data_item);


        ListView v = (ListView) rootView.findViewById(R.id.data_list);
        v.setAdapter(test);

        TextView mini_location = (TextView) rootView.findViewById(R.id.current_location_text);
        TextView large_location = (TextView) rootView.findViewById(R.id.current_place_text);



        Typeface bold =Typeface.createFromAsset(getActivity().getAssets(),
                "RobotoCondensed-Bold.ttf");

        Typeface regular = Typeface.createFromAsset(getActivity().getAssets(),
                "RobotoCondensed-Regular.ttf");

        mini_location.setTypeface(regular);
        large_location.setTypeface(bold);


        return rootView;
    }

    public class CustomListAdapter extends ArrayAdapter{


        public CustomListAdapter(Context context, int resource) {
            super(context,resource);
        }

        public int getCount(){
            return 5;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View vi = convertView;
            if (vi == null) {
                vi = inflater.inflate(R.layout.data_item, null);

                TextView data_description = (TextView)vi.findViewById(R.id.data_description);
                TextView data_content = (TextView)vi.findViewById(R.id.data_content);

                Typeface italic = Typeface.createFromAsset(getActivity().getAssets(),
                        "RobotoCondensed-Italic.ttf");

                Typeface regular = Typeface.createFromAsset(getActivity().getAssets(),
                        "RobotoCondensed-Regular.ttf");

                data_description.setTypeface(regular);
                data_content.setTypeface(italic);

            }
            return vi;
        }
    }

}
