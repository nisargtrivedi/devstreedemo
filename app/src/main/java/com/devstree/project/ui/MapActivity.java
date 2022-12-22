package com.devstree.project.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;


import com.devstree.project.adapter.SuggestionsAdapter;
import com.devstree.project.utils.MaterialSearchBar;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.devstree.project.R;
import com.devstree.project.model.RoutePlace;
import com.devstree.project.utils.FetchAddressIntentService;
import com.devstree.project.utils.Contants;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLAstKnownLocation;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 17;

    LatLng latLng;
    Place place;
    String placeName;

    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private LinearLayout llUpdate;
    private AppCompatButton btnSave;
    private AppCompatImageView imgBack;
    private TextView txtTitle;

    //variables
    private String addressOutput;
    private int addressResultCode;

    private LatLng currentMarkerPosition;

    //receiving
    private String mApiKey = "";
    private boolean routeOnly = false;
    private String mCountry = "";
    private String mLanguage = "en";
    private String mJsonString = "";
    double lat;
    double lng;
    boolean isEdit = false;
    String address = "";
    int placeID;
    int position;
    //polyline object
    private List<Polyline> polylines = null;

    private static final String TAG = MapActivity.class.getSimpleName();
    ArrayList<RoutePlace> latLngArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ImageView imageView = findViewById(R.id.TopBar).findViewById(R.id.imgBack);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapActivity.super.onBackPressed();
            }
        });
        initViews();
        receiveIntent();
        initMapsAndPlaces();

        //check if view route mode
        materialSearchBar.setVisibility(
                routeOnly ? View.GONE : View.VISIBLE
        );

        txtTitle.setText(
                routeOnly ? "View Route" : "Search Places"
        );


    }


    private void initViews() {
        materialSearchBar = findViewById(R.id.searchBar);
        llUpdate = findViewById(R.id.llUpdate);
        btnSave = findViewById(R.id.btnSave);
        txtTitle = findViewById(R.id.txtTitle);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "place found " + place.getName() + place.getAddress());
                latLng = place.getLatLng();
                Double latitude = latLng.latitude;
                Double longitude = latLng.longitude;
                Location currentLocation = mMap.getMyLocation();
                if (currentLocation != null) {
                    double currentLatitude = currentLocation.getLatitude();
                    double currentLongitude = currentLocation.getLongitude();

                    Intent intent = new Intent();
                    intent.putExtra("Latitude", latitude);
                    intent.putExtra("Longitude", longitude);
                    intent.putExtra("PlaceName", place.getAddress());
                    intent.putExtra("city", place.getName());

                    intent.putExtra("isEdit", isEdit);
                    if (isEdit) {
                        intent.putExtra("ID", placeID);
                        intent.putExtra("position", position);
                    }
                    setResult(Contants.SELECT_LOCATION_REQUEST_CODE, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to fetch the current location", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void receiveIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra(Contants.API_KEY)) {
            mApiKey = intent.getStringExtra(Contants.API_KEY);
        }

        if (intent.hasExtra(Contants.COUNTRY)) {
            mCountry = intent.getStringExtra(Contants.COUNTRY);
        }

        if (intent.hasExtra(Contants.ROUTE_ONLY)) {
            routeOnly = intent.getBooleanExtra(Contants.ROUTE_ONLY, false);
        }

        if (intent.hasExtra(Contants.LIST_DATA)) {
            mJsonString = intent.getStringExtra(Contants.LIST_DATA);
        }

        if (intent.hasExtra(Contants.LANGUAGE)) {
            mLanguage = intent.getStringExtra(Contants.LANGUAGE);
        }
        if (intent.hasExtra("Latitude")) {
            lat = intent.getDoubleExtra("Latitude", 0.0);
        }
        if (intent.hasExtra("Longitude")) {
            lng = intent.getDoubleExtra("Longitude", 0.0);
        }
        if (intent.hasExtra("isEdit")) {
            isEdit = intent.getBooleanExtra("isEdit", false);
        }
        if (intent.hasExtra("Address")) {
            address = intent.getStringExtra("Address");
        }
        if (intent.hasExtra("ID")) {
            placeID = intent.getIntExtra("ID", -1);
        }
        if (intent.hasExtra("position")) {
            position = intent.getIntExtra("position", -1);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initMapsAndPlaces() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(this, mApiKey);
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                  //  materialSearchBar.disableSearch();
                    materialSearchBar.clearSuggestions();
                    llUpdate.setVisibility(View.GONE);
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry(mCountry)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!materialSearchBar.isSuggestionsVisible()) {
                                            materialSearchBar.showSuggestionsList();
                                        }
                                    }
                                }, 1000);
                            }
                        } else {
                            Log.i(TAG, "prediction fetching task unSuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }

                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        place = fetchPlaceResponse.getPlace();
                        Log.i(TAG, "place found " + place.getName() + place.getAddress());
                        latLng = place.getLatLng();
                        if (latLng != null) {

                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(place.getAddress()));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                        }
                        llUpdate.setVisibility(View.VISIBLE);
                        if (isEdit) {
                            btnSave.setText("Update");
                        } else {
                            btnSave.setText("Save");
                        }
                        /*rippleBg.startRippleAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rippleBg.stopRippleAnimation();
                            }
                        }, 2000);*/
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof ApiException) {
                                    ApiException apiException = (ApiException) e;
                                    apiException.printStackTrace();
                                    int statusCode = apiException.getStatusCode();
                                    Log.i(TAG, "place not found" + e.getMessage());
                                    Log.i(TAG, "status code : " + statusCode);
                                }
                            }
                        });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")

    /*
      is triggered when the map is loaded and ready to display
      @param GoogleMap
     *
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
            if(!getResources().getConfiguration().isNightModeActive()){
               mMap.setMapStyle(null);
            }else {
                MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map);
                mMap.setMapStyle(style);
            }

        mMap.setMyLocationEnabled(true);
        if (isEdit) {
            LatLng sydney = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions()
                    .position(sydney)
                    .title(address));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM));
        }
        if (!TextUtils.isEmpty(mJsonString)) {
            mMap.getUiSettings().setScrollGesturesEnabled(true);
        } else {
            mMap.getUiSettings().setScrollGesturesEnabled(false);
        }
        //enable location button
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);

        //move location button to the required position and adjust params such margin
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 60, 500);
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        //if task is successful means the gps is enabled so go and get device location amd move the camera to that location
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (!isEdit && TextUtils.isEmpty(mJsonString))
                    getDeviceLocation();
            }
        });

        //if task failed means gps is disabled so ask user to enable gps
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (materialSearchBar.isSuggestionsVisible()) {
                    materialSearchBar.clearSuggestions();
                }
             /*   if (materialSearchBar.isSearchEnabled()) {
                    materialSearchBar.disableSearch();
                    llUpdate.setVisibility(View.GONE);
                }*/
                return false;
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
//                mSmallPinIv.setVisibility(View.GONE);
//                mProgressBar.setVisibility(View.VISIBLE);
                Log.i(TAG, "changing address");
//                ToDo : you can use retrofit for this network call instead of using services
                //hint: services is just for doing background tasks when the app is closed no need to use services to update ui
                //best way to do network calls and then update user ui is Retrofit .. consider it
                if (!isEdit)
                    startIntentService();
            }
        });

        /**************/
        if (!TextUtils.isEmpty(mJsonString)) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Findroutes();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    /**
     * is triggered whenever we want to fetch device location
     * in order to get device's location we use FusedLocationProviderClient object that gives us the last location
     * if the task of getting last location is successful and not equal to null ,
     * apply this location to mLastLocation instance and move the camera to this location
     * if the task is not successful create new LocationRequest and LocationCallback instances and update lastKnownLocation with location result
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLAstKnownLocation = task.getResult();
                            if (mLAstKnownLocation != null) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(mLAstKnownLocation.getLatitude(), mLAstKnownLocation.getLongitude()))
                                        .title(""));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLAstKnownLocation.getLatitude(), mLAstKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(1000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLAstKnownLocation = locationResult.getLastLocation();
                                        LatLng lastLatLng = new LatLng(mLAstKnownLocation.getLatitude(), mLAstKnownLocation.getLongitude());
                                        mMap.addMarker(new MarkerOptions()
                                                .position(lastLatLng)
                                                .title(""));
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, DEFAULT_ZOOM));
                                        //remove location updates in order not to continues check location unnecessarily
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, null);
                            }
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get last location ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected void startIntentService() {
        currentMarkerPosition = mMap.getCameraPosition().target;
        AddressResultReceiver resultReceiver = new AddressResultReceiver(new Handler());

        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Contants.RECEIVER, resultReceiver);
        intent.putExtra(Contants.LOCATION_LAT_EXTRA, currentMarkerPosition.latitude);
        intent.putExtra(Contants.LOCATION_LNG_EXTRA, currentMarkerPosition.longitude);
        intent.putExtra(Contants.LANGUAGE, mLanguage);

        startService(intent);
    }

    private void updateUi() {
        mMap.clear();
    }


    // function to find Routes.
    public void Findroutes() {
        try {
            JSONArray jsonArray = new JSONArray(mJsonString);
            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            Double a = 0.0;
            Double b = 0.0;
            Double c = 0.0;
            latLngArrayList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                RoutePlace routePlace = new RoutePlace();
                routePlace.latitude = jsonArray.getJSONObject(i).getDouble("latitude");
                routePlace.longitude = jsonArray.getJSONObject(i).getDouble("longitude");
                routePlace.title = jsonArray.getJSONObject(i).getString("cityName");
                latLngArrayList.add(routePlace);
            }

            Log.d("SIZE OF ARRAY LIST--->",""+latLngArrayList.size()+"--");//but here value getting c.getDouble(4)=0.0

            for (int j = 0; j < latLngArrayList.size(); j++) {
                if (latLngArrayList.size() > 1) {
                    if (latLngArrayList.size() > 2 && j > 1) {
                        drawRoute(new LatLng(latLngArrayList.get(j - 1).latitude, latLngArrayList.get(j - 1).longitude), new LatLng(latLngArrayList.get(j).latitude, latLngArrayList.get(j).longitude));
                    } else {
                        if (latLngArrayList.size() == 2) {
                            drawRoute(new LatLng(latLngArrayList.get(0).latitude, latLngArrayList.get(0).longitude), new LatLng(latLngArrayList.get(1).latitude, latLngArrayList.get(1).longitude));
                        } else {
                            drawRoute(new LatLng(latLngArrayList.get(j).latitude, latLngArrayList.get(j).longitude), new LatLng(latLngArrayList.get(j + 1).latitude, latLngArrayList.get(j + 1).longitude));
                        }
                    }
                } else {
                    if (latLngArrayList.size() == 1) {
                        drawRoute(new LatLng(latLngArrayList.get(0).latitude, latLngArrayList.get(0).longitude), new LatLng(latLngArrayList.get(0).latitude, latLngArrayList.get(0).longitude));
                    }
                }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLngArrayList.get(0).latitude, latLngArrayList.get(0).longitude), DEFAULT_ZOOM));
        } catch (Exception e) {
            Log.e("****Exception****", "***path**" + e.getLocalizedMessage());
        }
    }

    private void drawRoute(LatLng origin,LatLng destination){
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(origin,destination)
                .key(getString(R.string.google_key))  //also define your api key here.
                .build();
        routing.execute();
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
//        Findroutes(start,end);
    }

    //Start Draw the route
    @Override
    public void onRoutingStart() {

        //if (!TextUtils.isEmpty(mJsonString))
       //     Toast.makeText(MapActivity.this, "Finding Route...", Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {


        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if (polylines != null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng = null;
        LatLng polylineEndLatLng = null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i < route.size(); i++) {

            Log.d("ROUTE NAME--",latLngArrayList.get(i).title);
            Log.d("ROUTE SIZE--",route.size()+"");

            if (i == shortestRouteIndex) {
                polyOptions.color(getResources().getColor(R.color.colorAccent));
                polyOptions.width(20);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);

                /*polylineStartLatLng = polyline.getPoints().get(i);
                int k = polyline.getPoints().size();
                polylineEndLatLng = polyline.getPoints().get(k - 1);*/
                polylines.add(polyline);



               /* MarkerOptions endMarker = new MarkerOptions();
                endMarker.position(polylineEndLatLng);
                endMarker.title(latLngArrayList.get(i).title);
                mMap.addMarker(endMarker);*/

            }
        }
        setMarker();
    }
    private void setMarker(){
        for(int j=0;j<latLngArrayList.size();j++){
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(new LatLng(latLngArrayList.get(j).latitude,latLngArrayList.get(j).longitude));
            startMarker.title(latLngArrayList.get(j).title);
            mMap.addMarker(startMarker);
            //Log.d("TITLE --",latLngArrayList.get(j).title+"");

        }

    }

    @Override
    public void onRoutingCancelled() {
        Findroutes();
    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            addressResultCode = resultCode;
            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData.getString(Contants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            updateUi();
        }
    }
}

