package com.idiotnation.raspored;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;

public class SettingsDialog extends Dialog {

    Activity activity;
    TextView easterEgg;
    CheckBox autoUpdate, nightMode;
    Spinner godineSpinner;
    SharedPreferences prefs;
    onEggsterListener mOnEggsterListener;
    List list;
    int egg = 0;
    static boolean doRefresh = false;

    public SettingsDialog(Activity activity, List list) {
        super(activity);
        this.activity = activity;
        this.list = list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_dialog);
        init();
        properties();
    }

    public void init() {
        godineSpinner = (Spinner) findViewById(R.id.spinner);
        autoUpdate = (CheckBox) findViewById(R.id.auto_update);
        nightMode = (CheckBox) findViewById(R.id.night_mode);
        easterEgg = (TextView) findViewById(R.id.easter_egg);
        prefs = activity.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
    }

    public void properties() {
        autoUpdate.setChecked(prefs.getBoolean("AutoUpdate", false));
        nightMode.setChecked(prefs.getBoolean("NightMode", false));
        autoUpdate.setVisibility(View.VISIBLE);
        nightMode.setVisibility(View.VISIBLE);
        autoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("AutoUpdate", isChecked).apply();
                activity.startService(new Intent(activity.getApplicationContext(), AutoUpdateService.class));
            }
        });
        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("NightMode", isChecked).apply();
                if (isChecked) {
                    prefs.edit().putInt("CurrentTheme", R.style.AppTheme_Dark).apply();
                } else {
                    prefs.edit().putInt("CurrentTheme", R.style.AppTheme_Light).apply();
                }
                activity.finish();
                activity.startActivity(activity.getIntent());
            }
        });
        final ArrayAdapter<String> dataAdapter = new SpinnerArrayAdapter(activity.getApplicationContext(), R.layout.spinner_selected_item, activity.getResources().getStringArray(R.array.godine_array));
        godineSpinner.setAdapter(dataAdapter);
        godineSpinner.setSelection(prefs.getInt("SpinnerDefault", 0) + ((prefs.getInt("SpinnerDefault", 0)/6)+1));
        godineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newPosition = position - ((position/6)+1);
                if(prefs.getInt("SpinnerDefault", 0)!=newPosition){
                    doRefresh = true;
                }
                if (list.size() > newPosition) {
                    if (list.get(newPosition).equals("NN")) {
                        Toast.makeText(activity, "Raspored nije dostupan", Toast.LENGTH_SHORT).show();
                        godineSpinner.setSelection(prefs.getInt("SpinnerDefault", 0));
                        doRefresh = false;
                    } else {
                        prefs.edit().putInt("SpinnerDefault", newPosition).apply();
                    }
                }else{
                    doRefresh = false;
                    Toast.makeText(activity, "Podatci za godine studija nedostaju, osvježite", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        easterEgg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                egg++;
                if(egg==8){
                    mOnEggsterListener.onEgg();
                    dismiss();
                }
            }
        });
    }

    public class SpinnerArrayAdapter extends ArrayAdapter<String> {

        String[] items;
        List headers;

        public SpinnerArrayAdapter(Context context, int resource, String[] items) {
            super(context, resource, items);
            this.items = items;
            headers = new ArrayList(Arrays.asList(0, 6, 12));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_selected_item, null);
            }
            int[] attrs = {R.attr.textColorPrimary};
            TypedArray ta = activity.getApplicationContext().obtainStyledAttributes(prefs.getInt("CurrentTheme", R.style.AppTheme_Light), attrs);
            TextView tv = (TextView) convertView.findViewById(R.id.ssi_item);
            tv.setText(getHeader(position) + items[position]);
            tv.setTextColor(ta.getColor(0, Color.BLACK));
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return !headers.contains(position);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, null);
            }
            int[] attrs = {headers.contains(position)?R.attr.textColorSecondary:R.attr.textColorPrimary};
            TypedArray ta = activity.getApplicationContext().obtainStyledAttributes(prefs.getInt("CurrentTheme", R.style.AppTheme_Light), attrs);
            TextView tv = (TextView) convertView.findViewById(R.id.si_item);
            tv.setText(items[position]);
            tv.setTextColor(ta.getColor(0, Color.BLACK));
            return convertView;
        }

        public String getHeader(int position){
           if(position<6){
               return "S. ";
           }else if(position<12){
               return "R. ";
           }else if(position<18){
               return "E. ";
           }
            return "";
        }
    }

    public void setOnEggListener(onEggsterListener eggsterListener){
        mOnEggsterListener = eggsterListener;
    }

    interface onEggsterListener{
        void onEgg();
    }
}