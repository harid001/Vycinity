package tinovation.org.vycinity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

// rahul test 1 12:40PM
public class MainActivity extends ActionBarActivity implements StreamFragment.OnLocationChangedListener {

    private ViewPager mViewPager;
    private TabAdapter mTapAdapter;
    public static String myLocation = "T-Pumps";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up view pager and fragment adapter
        mViewPager = (ViewPager) this.findViewById(R.id.swiper);
        mTapAdapter = new TabAdapter(this.getSupportFragmentManager());
        mViewPager.setAdapter(mTapAdapter);

//        //set up action bar tabs
        final ActionBar actionBar = this.getSupportActionBar();
//        SpannableString s = new SpannableString("My title");
//        s.setSpan(new TypefaceSpan(),0,s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//        actionBar.setTitle(s);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // show the given tab
                mViewPager.setCurrentItem(tab.getPosition());

            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        actionBar.addTab(actionBar.newTab().setText("Nearby").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Stream").setTabListener(tabListener));

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {



            }


        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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

    @Override
    public void onLocationChanged(String newLocation) {
//        Fragment pf = getSupportFragmentManager().findFragmentById(R.id.test);
//        TextView v = (TextView) pf.getView().findViewById(R.id.current_place_text);
//        v.setText(newLocation);
    }

    public class TabAdapter extends FragmentPagerAdapter{

        public static final int TAB_COUNT = 2;

        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                PlaceFragment pf = new PlaceFragment();
                Bundle bundle = new Bundle();
                bundle.putString("location",MainActivity.myLocation);
                pf.setArguments(bundle);
                return pf;
            }
            else{
                return new StreamFragment();
            }
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }
    }

}
