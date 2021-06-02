package com.example.skateterrain;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    //SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

    private MapView mMapView;
    RelativeLayout mapContainer, listContainer;
    ImageButton iconMapScreenSize;
    TextView textViewNoSpots;
    String address;
    private GoogleMap mMap;
    long TimeBeforeLocationUpdate = 20000;
    float minDistMoved = 100f;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    LocationManager locationManager;
    LocationListener locationListener;

    ListView listView;

    ArrayList<Marker> markers = new ArrayList<Marker>();

    ArrayList<Item> items = new ArrayList<Item>();

    SharedPreferences sharedPreferences;

    CustomListAdapter adapter;
    //ArrayAdapter arrayAdapter;
    boolean mapFullscreen = false;
    static boolean newSpotMode = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.action_bar_bg_rounded);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.colorPrimary)));

        mMapView = (MapView) findViewById(R.id.mapView);
        mapContainer = (RelativeLayout) findViewById(R.id.RelativeLayoutMapView);
        listContainer = (RelativeLayout) findViewById(R.id.RelativeLayoutListView);
        initGoogleMap(savedInstanceState);
        iconMapScreenSize = (ImageButton) findViewById(R.id.btn_full_screen_map);
        textViewNoSpots = (TextView) findViewById(R.id.textViewNoSpots);
        listView = findViewById(R.id.listView);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");




/*
        if (!locations.isEmpty()){
            if (locations.get(0) != new LatLng(0,0))
            locations.add(new LatLng(0,0));
        } else {
            locations.add(new LatLng(0,0));
        }


*/

        sharedPreferences = getSharedPreferences("sharedPref",MODE_PRIVATE);

        //sharedPreferences.edit().clear().apply();

        getLocalData();

        adapter = new CustomListAdapter(this, items);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Log.i("locations", String.valueOf(items.get(position).getItemLocation()));
                Location markerLocation = new Location(LocationManager.GPS_PROVIDER);
                markerLocation.setLatitude(items.get(position).getItemLocation().latitude);
                markerLocation.setLongitude(items.get(position).getItemLocation().longitude);
                centerMapOnLocation(markerLocation);

            }
        });



        spotsAddedText();
    }

    public void getLocalData() {
        try {
            Gson gson = new Gson();

                String json = sharedPreferences.getString("items", "");
                Type type = new TypeToken<ArrayList<Item>>() {
                }.getType();
                if (gson.fromJson(json, type) != null) {

                    items = gson.fromJson(json, type);
                    Log.i("data", String.valueOf(items.get(0).getItemDescription()));
                    Log.i("data", String.valueOf(items.get(0).getItemLocation()));
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    showUserLocationAsBlueDot();
                }
            }
        }
    }

    public void clickFunction(View view) {
        if (mapFullscreen) {
            contractMapAnimation();
        } else {
            expandMapAnimation();
        }
    }

    public void expandMapAnimation() {
        mapFullscreen = true;
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                iconMapScreenSize.setImageResource(R.drawable.ic_fullscreen_exit_black_24dp);
                final ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mapContainer);
                final ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                        "weight",
                        50,
                        100);
                mapAnimation.setDuration(800);

                ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(listContainer);
                ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                        "weight",
                        50,
                        0);
                recyclerAnimation.setDuration(800);

                recyclerAnimation.start();
                mapAnimation.start();

                mapAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.i("weight", String.valueOf(mapAnimationWrapper.getWeight()));
                        mapAnimationWrapper.setWeight(100f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        });
    }

    public void contractMapAnimation() {
        mapFullscreen = false;
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                iconMapScreenSize.setImageResource(R.drawable.ic_fullscreen_black_24dp);
                final ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mapContainer);
                ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                        "weight",
                        100,
                        50);
                mapAnimation.setDuration(800);

                ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(listContainer);
                ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                        "weight",
                        0,
                        50);
                recyclerAnimation.setDuration(800);

                recyclerAnimation.start();
                mapAnimation.start();


                mapAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.i("weight", String.valueOf(mapAnimationWrapper.getWeight()));
                        mapAnimationWrapper.setWeight(50f);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        });

    }


    public void centerMapOnLocation(Location location) {
        if (location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
        }
    }


    public void showUserLocationAsBlueDot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    public void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (newSpotMode == true){
            expandMapAnimation();
        }

        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        final Boolean setCamera = false;
        mMap = map;
        map.getUiSettings().setMapToolbarEnabled(false);
        showUserLocationAsBlueDot();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            centerMapOnLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (!items.isEmpty()) {
            for (int i = 0; i < items.size();i++) {
                newMarker(items.get(i).getItemLocation(),items.get(i).getItemType());
            }
        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (newSpotMode) {
                    newMarker(latLng,SpotActivity.getSpotType());
                    updateItems(latLng);
                    applyToPreferences();

                    newSpotMode = false;

                    if (mapFullscreen) {
                        contractMapAnimation();
                    }
                }

            }
        });


    }

    public void newMarker(LatLng latLng, String spotType) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        address = "";

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            Log.i("address info", String.valueOf(listAddresses.get(0)));
            if (listAddresses != null && listAddresses.size() > 0) {
                if (listAddresses.get(0).getThoroughfare() != null && !listAddresses.get(0).getThoroughfare().contains("Unnamed") && !listAddresses.get(0).getThoroughfare().equals("null")) {
                    address = listAddresses.get(0).getThoroughfare();

                } else if (listAddresses.get(0).getLocality() != null && !listAddresses.get(0).getLocality().contains("Unnamed") && !listAddresses.get(0).getLocality().equals("null")) {
                    address = listAddresses.get(0).getLocality();

                } else if (listAddresses.get(0).getSubAdminArea() != null && !listAddresses.get(0).getSubAdminArea().contains("Unnamed") && !listAddresses.get(0).getSubAdminArea().equals("null")) {
                    address = listAddresses.get(0).getSubAdminArea();
                } else {
                    address = listAddresses.get(0).getCountryName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker_" + spotType)))));
        //locations.add(latLng);


    }


    public void updateItems(LatLng latLng){
        items.add(new Item(SpotActivity.getSpotName(),address,SpotActivity.getSpotType(),SpotActivity.getFilePath(),latLng));
        adapter.notifyDataSetChanged();

    }

    public void applyToPreferences() {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(items);
        prefsEditor.putString("items", json);
        prefsEditor.apply();

        spotsAddedText();
    }

    public Bitmap resizeMapIcons(String iconName){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, (imageBitmap.getWidth()/3)+10, (imageBitmap.getHeight()/3)+10, false);
        return resizedBitmap;
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void spotsAddedText(){
        if (items.isEmpty()) {

            textViewNoSpots.setVisibility(View.VISIBLE);
        } else {
            textViewNoSpots.setVisibility(View.INVISIBLE);
        }
    }


    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_button_plus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            // do something here
            Intent intent = new Intent(MapActivity.this, SpotActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }





    public class CustomListAdapter extends BaseAdapter {

        private Context context; //context
        private ArrayList<Item> items; //data source of the list adapter

        //public constructor
        public CustomListAdapter(Context context, ArrayList<Item> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size(); //returns total of items in the list
        }

        @Override
        public Object getItem(int position) {
            return items.get(position); //returns list item at the specified position
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // inflate the layout for each list row
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.layout_list_view_row_items, parent, false);
            }

            // get current item to be displayed
            final Item currentItem = (Item) getItem(position);

            // get the TextView for item name and item description
            TextView textViewItemName = (TextView)
                    convertView.findViewById(R.id.text_view_item_name);
            TextView textViewItemDescription = (TextView)
                    convertView.findViewById(R.id.text_view_item_description);

            ImageView imageViewSpot = (ImageView) convertView.findViewById(R.id.spotImage);

            ImageView imageViewDelete = (ImageView) convertView.findViewById(R.id.deleteButton);

            imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                    alertDialog.setTitle("Alert");
                    alertDialog.setMessage("Are you sure you want to delete this spot?");


                    alertDialog.setButton( Dialog.BUTTON_POSITIVE, "DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // remove from map!
                            markers.get(position).remove();
                            // remove from arrayList!
                            markers.remove(position);
                            items.remove(position);
                            adapter.notifyDataSetChanged();
                            spotsAddedText();
                            Toast.makeText(MapActivity.this, "spot: " + currentItem.getItemName()+ " deleted", Toast.LENGTH_SHORT).show();
                            applyToPreferences();

                        }
                    });

                    alertDialog.setButton( Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()    {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.create();

                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#CCCCCC"));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                        }
                    });

                    alertDialog.show();

                }
            });

            //sets the text for item name and item description from the current item object
            textViewItemName.setText(currentItem.getItemName());
            textViewItemDescription.setText(currentItem.getItemDescription());
/*
            Bitmap bitmap = getBitmap(currentItem.getItemFilePath());
            */

            if (currentItem.getItemFilePath() != null && !currentItem.getItemFilePath().equals("")) {
                Log.i("filepath", currentItem.getItemFilePath());
                imageViewSpot.setImageURI(Uri.parse(currentItem.getItemFilePath()));
            } else {
                imageViewSpot.setImageResource(getResources().getIdentifier(currentItem.getItemType(),"drawable",getPackageName()));
            }

            // returns the view for the current row
            return convertView;
        }
    }




    public Bitmap getImageBitmapSpot(String filepath) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File imageFile = new File(sd+filepath);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            return BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Bitmap getBitmap(String path) {
        try {
            Bitmap bitmap = null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
