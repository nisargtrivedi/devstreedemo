package com.devstree.project.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.devstree.project.R;
import com.devstree.project.adapter.PlaceAdapter;
import com.devstree.project.databinding.ActivityMainBinding;
import com.devstree.project.databinding.BottomsheetAscDscBinding;
import com.devstree.project.databse.DBHelper;
import com.devstree.project.listener.ItemClickListener;
import com.devstree.project.model.Place;

import com.devstree.project.utils.Contants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ItemClickListener {

    PlaceAdapter placeListadapter;

    DBHelper localDatabase;
    ArrayList<Place> placeList;
    ActivityMainBinding mainBinding;
    LatLng currentLatLng;
    String apiKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        apiKey = getString(R.string.places_api_key);
        initViews();
    }

    @UiThread
    private void initViews() {

        localDatabase = new DBHelper(this);
        placeList = localDatabase.getAllPlace();

        mainBinding.llAddPositionCenter.setVisibility(
                placeList != null && placeList.size() > 0 ? View.GONE : View.VISIBLE
        );
        mainBinding.llAddPositionBottom.setVisibility(
                placeList != null && placeList.size() > 0 ? View.VISIBLE : View.GONE
        );
        mainBinding.imgFilter.setVisibility(
                placeList != null && placeList.size() > 1 ? View.VISIBLE : View.GONE
        );

        if (placeList != null && placeList.size() > 0) {
            Place startPlace = localDatabase.getPlace(1);
            currentLatLng = new LatLng(startPlace.getLatitude(), startPlace.getLongitude());
        } else {
            placeList = new ArrayList<Place>();
        }

        placeListadapter = new PlaceAdapter(MainActivity.this, placeList, this);
        setNavigationVisibility();
        mainBinding.rlvPlace.setHasFixedSize(true);
        mainBinding.rlvPlace.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.rlvPlace.setAdapter(placeListadapter);
        mainBinding.imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomsheetDialog();
            }
        });
        mainBinding.crdNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMapActivity(true);
            }
        });

        mainBinding.btnAddPlaceCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // You must grant user permission for access device location first
                // please don't ignore this step >> Ignoring location permission may cause application to crash !
                if (hasPermissionInManifest(MainActivity.this, 1, Manifest.permission.ACCESS_FINE_LOCATION))
                    startMapActivity(false);
            }
        });
        mainBinding.btnAddPlaceBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity(false);
            }
        });

    }

    private void setNavigationVisibility() {
        if (placeList.size() > 1) {
            mainBinding.crdNavigate.setVisibility(View.VISIBLE);
        } else {
            mainBinding.crdNavigate.setVisibility(View.GONE);
        }
    }

    private void showBottomsheetDialog() {

        BottomsheetAscDscBinding bottomsheetAscDscBinding = BottomsheetAscDscBinding.inflate(LayoutInflater.from(this));
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.AppBottomSheetTheme);
        bottomSheetDialog.setContentView(bottomsheetAscDscBinding.getRoot());
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        bottomsheetAscDscBinding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.rbAsc:

                        placeList = localDatabase.getAllPlaceByAscDesc("ASC");
                        placeListadapter.updateAll(placeList);
                        break;
                    case R.id.rbDesc:
                        placeList = localDatabase.getAllPlaceByAscDesc("DESC");
                        placeListadapter.updateAll(placeList);
                        break;

                }
            }
        });
        bottomsheetAscDscBinding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });


        bottomSheetDialog.show();

    }


    private void startMapActivity(boolean routeOnly) {
        Intent intent = new Intent(this, MapActivity.class);
        Bundle bundle = new Bundle();

        bundle.putBoolean(Contants.ROUTE_ONLY, routeOnly);
        if(routeOnly){
            bundle.putString(Contants.LIST_DATA,  new Gson().toJson(placeList));

        }
        bundle.putString(Contants.API_KEY, apiKey);
        bundle.putString(Contants.COUNTRY, "ind");
        bundle.putString(Contants.LANGUAGE, "en");

        intent.putExtras(bundle);
        startActivityForResult(intent, Contants.SELECT_LOCATION_REQUEST_CODE);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateUi(Intent data) {
        Log.d("*****Distance*****", "*****getBooleanExtra*****" + data.getBooleanExtra("isEdit", false));
        String placeName = data.getStringExtra("PlaceName");

        Double latitude = data.getDoubleExtra("Latitude", 0.0);
        Double longitude = data.getDoubleExtra("Longitude", 0.0);
        Double distnace = 0.0;
        String cityName = data.getStringExtra("city");
        if (localDatabase.getAllPlace() != null && localDatabase.getAllPlace().size() > 0) {
            Place startPlace = localDatabase.getPlace(1);

            currentLatLng = new LatLng(startPlace.getLatitude(), startPlace.getLongitude());
            LatLng destLatLng = new LatLng(latitude, longitude);

            Double findDistance = SphericalUtil.computeDistanceBetween(currentLatLng, destLatLng) / 1000;
            DecimalFormat df = new DecimalFormat("#.##");
            distnace = Double.parseDouble(df.format(findDistance));
        }
        if (data.getBooleanExtra("isEdit", false)) {
            int position = data.getIntExtra("position", -1);
            int id = data.getIntExtra("ID", -1);
            localDatabase.updatePlace(id, placeName, latitude, longitude, distnace, cityName);
            Place updatePlace = new Place();
            updatePlace.setId(id);
            updatePlace.setCityName(cityName);
            updatePlace.setPlaceName(placeName);
            updatePlace.setLatitude(latitude);
            updatePlace.setLongitude(longitude);
            updatePlace.setDistnace(distnace);
            placeList.set(position, updatePlace);

            placeListadapter.notifyDataSetChanged();
        } else {

            localDatabase.insertDistance(placeName, latitude, longitude, distnace, cityName);
            Place addPlace = new Place();
            addPlace.setId(placeList.size()+1);
            addPlace.setCityName(cityName);
            addPlace.setPlaceName(placeName);
            addPlace.setLatitude(latitude);
            addPlace.setLongitude(longitude);
            addPlace.setDistnace(distnace);
            placeList.add(addPlace);
            placeListadapter.notifyItemInserted(placeList.size() - 1);

            if (placeList.size() > 0) {
                mainBinding.llAddPositionCenter.setVisibility(View.GONE);
                mainBinding.llAddPositionBottom.setVisibility(View.VISIBLE);
            }
            mainBinding.imgFilter.setVisibility(
                    placeList != null && placeList.size() > 1 ? View.VISIBLE : View.GONE
            );
            setNavigationVisibility();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Contants.SELECT_LOCATION_REQUEST_CODE) {
            if (data != null) updateUi(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startMapActivity(false);
        }
    }

    //check for location permission
    public static boolean hasPermissionInManifest(Activity activity, int requestCode, String permissionName) {
        if (ContextCompat.checkSelfPermission(activity,
                permissionName)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{permissionName},
                    requestCode);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void itemClicked(final Place place, int position, String actionType) {
        switch (actionType) {
            case "Edit":
                Intent intent = new Intent(this, MapActivity.class);
                Bundle bundle = new Bundle();

                String apiKey = getString(R.string.places_api_key);
                bundle.putString(Contants.API_KEY, apiKey);
                bundle.putString(Contants.COUNTRY, "ind");
                bundle.putString(Contants.LANGUAGE, "en");
                bundle.putDouble("Latitude", place.getLatitude());
                bundle.putDouble("Longitude", place.getLongitude());
                bundle.putString("Address", place.getPlaceName());
                bundle.putInt("ID", place.getId());
                bundle.putBoolean("isEdit", true);
                bundle.putInt("position", position);

                intent.putExtras(bundle);
                startActivityForResult(intent, Contants.SELECT_LOCATION_REQUEST_CODE);
                break;
            case "Delete":
                if (place.getId()!=1) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Confirm Delete")
                            .setMessage("Do you really want to delete?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    localDatabase.deleteDistance(place.getId());
                                    placeList = localDatabase.getAllPlace();
                                    if (placeList != null && placeList.size() > 0) {
                                        Place startPlace = localDatabase.getPlace(1);
                                        currentLatLng = new LatLng(startPlace.getLatitude(), startPlace.getLongitude());
                                        mainBinding.llAddPositionCenter.setVisibility(View.GONE);
                                        mainBinding.llAddPositionBottom.setVisibility(View.VISIBLE);
                                        placeListadapter.updateAll(placeList);
                                        if (placeList != null && placeList.size() > 1) {
                                            mainBinding.crdNavigate.setVisibility(View.VISIBLE);
                                        } else {
                                            mainBinding.crdNavigate.setVisibility(View.GONE);
                                        }

                                        mainBinding.imgFilter.setVisibility(
                                                placeList != null && placeList.size() > 1 ? View.VISIBLE : View.GONE
                                        );
                                    } else {
                                        mainBinding.llAddPositionCenter.setVisibility(View.VISIBLE);
                                        mainBinding.llAddPositionBottom.setVisibility(View.GONE);
                                        mainBinding.rlvPlace.setVisibility(View.GONE);
                                        mainBinding.crdNavigate.setVisibility(View.GONE);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();


                    break;
                }else {
                    Toast.makeText(MainActivity.this,"You can not delete start location.",Toast.LENGTH_LONG).show();
                }
        }

    }

}