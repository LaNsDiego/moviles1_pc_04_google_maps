package com.desarrollo.labmapa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap mMap;
    private LatLng casa;
    private String strLatitude , strLongitude;
    private float zoom;
    FusedLocationProviderClient locationMe;
    private static final String MAP_STYLE_NORMAL ="NORMAL";
    private static final String MAP_STYLE_HIBRIDO ="HÍBRIDO";
    private static final String MAP_STYLE_SATELITAL ="SATELITAL";
    private static final String MAP_STYLE_TERRENO ="TERRENO";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mapFragment).commit();
        mapFragment.getMapAsync(this);
         locationMe = LocationServices.getFusedLocationProviderClient(this);

        casa = new LatLng(-18.007741, -70.244397);
        zoom = 15;

        // spiner
        List<String> lstStyle = new ArrayList<>();
        lstStyle.add(MAP_STYLE_NORMAL);
        lstStyle.add(MAP_STYLE_HIBRIDO);
        lstStyle.add(MAP_STYLE_SATELITAL);
        lstStyle.add(MAP_STYLE_TERRENO);

        ArrayAdapter<String> adapterSpinnerMapStyle = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,lstStyle);
        final Spinner spinerMapStyle = findViewById(R.id.spinner_map_theme);
        spinerMapStyle.setAdapter(adapterSpinnerMapStyle);
        spinerMapStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setChangeMapStyle(spinerMapStyle.getSelectedItem().toString());
                Log.d("MAPP>" , spinerMapStyle.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Calcular la distancia de mi ubicación hasta otro punto
        Button btnCalculateDistance = findViewById(R.id.btn_calculate_distance);
        btnCalculateDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resultMessage = "";
                float distance=0;
                Location crntLocation=new Location("crntlocation");
                crntLocation.setLatitude(casa.latitude);
                crntLocation.setLongitude(casa.longitude);

                Location locationJapan = new Location("japan");
                locationJapan.setLatitude(35.680513);
                locationJapan.setLongitude(139.769051);
                distance = crntLocation.distanceTo(locationJapan) / 1000; // in km
                resultMessage += String.valueOf("Distancia a Japon \n "+distance +"Km \n\n");

                Location locationAlemania = new Location("Alemania");
                locationJapan.setLatitude(52.516934);
                locationJapan.setLongitude(13.403190);
                distance = crntLocation.distanceTo(locationAlemania) / 1000; // in km
                resultMessage += String.valueOf("Distancia a Alemania \n "+distance +"Km \n\n");

                Location locationItalia = new Location("Italia");
                locationJapan.setLatitude(41.902609);
                locationJapan.setLongitude(12.494847);
                distance = crntLocation.distanceTo(locationItalia) / 1000; // in km
                resultMessage += String.valueOf("Distancia a Italia \n "+distance +"Km \n\n");

                Location locationFrancia = new Location("Francia");
                locationJapan.setLatitude(48.843489);
                locationJapan.setLongitude(2.355331);
                distance = crntLocation.distanceTo(locationFrancia) / 1000; // in km
                resultMessage += String.valueOf("Distancia a Francia \n "+distance +"Km");

                showDistancaInDialog(resultMessage);
            }
        });
        getLastLocation();
    }

    private void showDistancaInDialog(String resultMessage){
        new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Distancias")
                .setMessage(resultMessage)
                .show();
    }

    private void setInfoWindowClickToPanorama(GoogleMap map) {
        map.setOnInfoWindowClickListener(
            new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if (marker.getTag() == "poi") {
                        StreetViewPanoramaOptions options =
                                new StreetViewPanoramaOptions().position(
                                        marker.getPosition());
                        SupportStreetViewPanoramaFragment streetViewFragment
                                = SupportStreetViewPanoramaFragment
                                .newInstance(options);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,
                                        streetViewFragment)
                                .addToBackStack(null).commit();
                    }
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(casa,zoom));
        GroundOverlayOptions casaOverlay = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.grocery))
                .position(casa, 100);

        mMap.addGroundOverlay(casaOverlay);

        setMapLongClick(mMap);
        setPoiClick(mMap);
        setInfoWindowClickToPanorama(mMap);
        enableMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void setMapLongClick(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude);
                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.app_name))
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker
                                (BitmapDescriptorFactory.HUE_BLUE)));
            }
        });
    }

    private void setPoiClick(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                Marker poiMarker = mMap.addMarker(new MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name));
                poiMarker.showInfoWindow();
                poiMarker.setTag("poi");
            }
        });
    }

    private void setChangeMapStyle(String typeMapStyle){
        switch (typeMapStyle){
            case MAP_STYLE_HIBRIDO:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return;
            case MAP_STYLE_SATELITAL:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return;
            case MAP_STYLE_TERRENO:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return;
            case MAP_STYLE_NORMAL:
            default:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return;
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

        }
    };

    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        locationMe = LocationServices.getFusedLocationProviderClient(this);
        locationMe.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

//    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                locationMe.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                   @Override
                   public void onComplete(@NonNull Task<Location> task) {
                       Location location = task.getResult();
                       if (location == null) {
                           requestNewLocationData();
                       } else {

                           Log.d(TAG,"--------------------------------");
                           Log.d(TAG,String.valueOf(location.getLatitude()));
                           Log.d(TAG,String.valueOf(location.getLongitude()));
                           casa = new LatLng(location.getLatitude(),location.getLongitude());
                       }
                   }
                });
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }
}
