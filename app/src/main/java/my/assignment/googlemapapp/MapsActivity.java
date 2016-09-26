package my.assignment.googlemapapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng mLatlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

        // Add a marker in Sydney and move the camera
      /*  LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mLatlng=latLng;
                String position= latLng.toString().substring(latLng.toString().indexOf("(")+1,latLng.toString().indexOf(")")-1);
                Log.i("onLongClickMap:",position);
                String Url ="http://maps.googleapis.com/maps/api/geocode/json?latlng="+position+"&sensor=false";
                new FetchLocation().execute(Url);

            }
        });
    }

    public class FetchLocation extends AsyncTask<String,Void,Void >{
        ProgressDialog progressDialog=new ProgressDialog(MapsActivity.this);
        String content;
        String error;
        String data="";


        @Override
        protected void onPreExecute() {
        }
        @Override
        protected Void doInBackground(String... urls) {
            BufferedReader br=null;
            URL url;

            try {
                url=new URL(urls[0]);
                Log.i("FetchAddress:",url+"");

                URLConnection connection=url.openConnection();
                connection.setDoOutput(true);

                OutputStreamWriter outputStreamWriter=new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(data);
                outputStreamWriter.flush();

                br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb=new StringBuilder();
                String line=null;

                while ((line=br.readLine())!=null){
                    sb.append(line+"\n");

                }
                content=sb.toString();


            } catch (MalformedURLException e) {
                error=e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                error=e.getMessage();
                e.printStackTrace();
            }finally {
                try {
                    br.close();
                } catch (IOException e) {
                    error=e.getMessage();
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void output) {
            super.onPostExecute(output);
            progressDialog.dismiss();
            if(error!=null){
                Toast.makeText(MapsActivity.this,error,Toast.LENGTH_LONG).show();

            }else {

                try {
                    JSONObject response=new JSONObject(content);

                    JSONArray jsonArray=response.getJSONArray("results");
                    String address=jsonArray.getJSONObject(0).getString("formatted_address");
                    mMap.addMarker(new MarkerOptions().position(mLatlng)).setTitle(address);
                    CameraUpdate center=
                            CameraUpdateFactory.newLatLng(mLatlng);
                    CameraUpdate zoom=CameraUpdateFactory.zoomTo(7);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);

                   // mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatlng));


                } catch (JSONException e) {
                    Log.e("Map Info:","one or more field not found in JSON data");
                    e.printStackTrace();
                }
            }
        }


    }
}