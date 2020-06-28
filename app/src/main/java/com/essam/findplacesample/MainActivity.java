package com.essam.findplacesample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.essam.simpleplacepicker.MapActivity;
import com.essam.simpleplacepicker.utils.SimplePlacePicker;

/**
 * This is the main activity of findPlace project sample, this project aims to help you understand
 * how to use SimplePlacePicker module and what is the main parameter you could pass to get the
 * best behavior and customization .
* */

public class MainActivity extends AppCompatActivity {
    private TextView mLocationText, mLatitudeTv, mLongitudeTv;
    private EditText mSupportedAreaEt;
    private String [] countryListIso = {"eg","sau","om","mar","usa","ind"};
    private String [] addressLanguageList = {"en","ar"};

    private Spinner mCountryListSpinner,mLanguageSpinner;
    private LinearLayout mLlResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews(){
        // textViews for displaying the result selected address
        mLocationText = findViewById(R.id.tv_location_text);
        mLatitudeTv = findViewById(R.id.tv_latitude);
        mLongitudeTv = findViewById(R.id.tv_longitude);

        mSupportedAreaEt = findViewById(R.id.et_supportedArea);
        mLlResult = findViewById(R.id.ll_result_data);

        Button selectLocation = findViewById(R.id.select_location_btn);
        selectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // You must grant user permission for access device location first
                // please don't ignore this step >> Ignoring location permission may cause application to crash !
                if (hasPermissionInManifest(MainActivity.this,1, Manifest.permission.ACCESS_FINE_LOCATION))
                    selectLocationOnMap();
            }
        });

        // country list spinner
        String[] countryList = getResources().getStringArray(R.array.country_list);
        mCountryListSpinner = findViewById(R.id.country_listSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, countryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountryListSpinner.setAdapter(adapter);

        //language list spinner
        String[] languageList = getResources().getStringArray(R.array.language_list);
        mLanguageSpinner = findViewById(R.id.lang_list_pinner);
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,languageList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLanguageSpinner.setAdapter(langAdapter);
    }

    /**
     *@param apiKey Required parameter, put your google maps and places api key
     *               { you must get a places key it's required for search autocomplete }
     * @param country Optional parameter to restrict autocomplete search to a specific country
     *                put country ISO like "eg" for Egypt and so on ..
     *                if there is no value attached to RESTRICT_PREDICTIONS_COUNTRY key, search will
     *                be worldWide
     * @param language Optional parameter to specify the language of the resulting address
     *                 could be "en" for English or "ar" for Arabic .
     *                 If this not specified, resulting address will be English by default
     *
     * @param supportedAreas Optional Array of supported areas that user can pick a location from.
     *                       Please be careful when you put areas as this will be literal comparison.
     *                       Ex : if you put the value of RESTRICT_LOCATION_SUPPORTED_AREAS key
     *                       to something like {"Cairo"} , user will only be able to select the address
     *                       that only contains cairo on it, like "zamalec,cairo,Egypt"
     *                       Also consider adding arabic translation as some google addresses contains
     *                       booth english and arabic together..
     *          I KNOW THIS IS A TRICKY ONE BUT I JUST USED IT TO RESTRICT USER SELECTION TO A SPECIFIC
     *                 COUNTRY AND SAVE MANY NETWORK CALLS AS USERS ALWAYS LIKE TO PLAY WITH MAP ^_^
     *                       it was something like {"Egypt","مصر"}
     *                       if this array was empty, the whole world is a supported area , user can
     *                       pick location anywhere!.
     * @return result bundle to be passed through an Intent to MapActivity
     */
    private Bundle createBundle(String apiKey, String country, String language, String[]supportedAreas){
        Bundle bundle = new Bundle();
        bundle.putString(SimplePlacePicker.API_KEY,apiKey);
        bundle.putString(SimplePlacePicker.COUNTRY,country);
        bundle.putString(SimplePlacePicker.LANGUAGE,language);
        bundle.putStringArray(SimplePlacePicker.SUPPORTED_AREAS,supportedAreas);
        return bundle;
    }

    private void selectLocationOnMap() {
        String apiKey = getString(R.string.places_api_key);
        String mCountry = countryListIso[mCountryListSpinner.getSelectedItemPosition()];
        String mLanguage = addressLanguageList[mLanguageSpinner.getSelectedItemPosition()];
        String [] mSupportedAreas = mSupportedAreaEt.getText().toString().split(",");

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtras(createBundle(apiKey,mCountry, mLanguage, mSupportedAreas));
        startActivityForResult(intent, SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE);
    }

    private void updateUi(Intent data){
        mLocationText.setText(data.getStringExtra(SimplePlacePicker.SELECTED_ADDRESS));
        mLatitudeTv.setText(String.valueOf(data.getDoubleExtra(SimplePlacePicker.LOCATION_LAT_EXTRA,-1)));
        mLongitudeTv.setText(String.valueOf(data.getDoubleExtra(SimplePlacePicker.LOCATION_LNG_EXTRA,-1)));
        mLlResult.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK){
            if (data != null) updateUi(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectLocationOnMap();
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

}