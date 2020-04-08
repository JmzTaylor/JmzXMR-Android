package com.jmzsoft.jmzxmr;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    ProgressDialog pd;
    ArrayList<Rig> rigs;
    RecyclerView recyclerView;
    Context ctx;
    Boolean running = false;
    double totalHash = 0;
    Handler handler;
    Timer timer;
    TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rigs= new ArrayList<>();
        ctx = this;
        recyclerView = findViewById(R.id.my_recycler_view);

        handler = new Handler();
        timer = new Timer();

        if (getSharedPref().contains("url")) {
            runTask();
        } else {
            Toast.makeText(this, getString(R.string.no_server), Toast.LENGTH_LONG).show();
        }
    }

    public void runTask() {
        if (task != null) {
            task.cancel();
        }
        if (getPollingFreq() != 0) {
            task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> new JsonTask().execute(getSharedPref().getString("url", "") + "/api"));
                }
            };

            timer.schedule(task, 0, getPollingFreq());
        } else {
            new JsonTask().execute(getSharedPref().getString("url", "") + "/api");
        }
    }

    public int getPollingFreq() {
        int polling = getSharedPref().getInt("polling", 0);
        int poll = 0;
        switch (polling) {
            case 1:
                poll = 5;
                break;
            case 2:
                poll = 10;
                break;
            case 3:
                poll = 30;
                break;
            case 4:
                poll = 60;
                break;
            default:
                break;
        }
        return poll*1000;
    }

    @SuppressLint("StaticFieldLeak")
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            if (!running) {
                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Please wait");
                pd.setCancelable(false);
                pd.show();
            }
        }

        protected String doInBackground(String... params) {
            if (getSharedPref().getBoolean("ignore_ssl", false)) {
                InternetClass.disableSslChecks(ctx);
            }

            return InternetClass.queryApi(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject reader;
            rigs.clear();
            try {
                reader = new JSONObject(result);
                JSONArray contacts = reader.getJSONArray("rigs");
                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    totalHash = totalHash + c.getDouble("currentHash");
                    rigs.add(new Rig(
                            c.getString("rigName"),
                            c.getString("workerId"),
                            c.getString("diffCurrent"),
                            c.getDouble("currentHash"),
                            c.getString("minerVersion"),
                            c.getString("uptime"),
                            1
                            )
                    );
                }

                JSONArray poolData = reader.getJSONArray("pool");
                for (int i = 0; i < poolData.length(); i++) {
                    JSONObject c = poolData.getJSONObject(i);

                    Rig rig = new Rig("", "","",0,"","",2);
                    rig.setPool(
                            new Pool(
                            c.getString("totalHashes"),
                            c.getString("validShares"),
                            c.getDouble("amtPaid"),
                            c.getDouble("amtDue")
                            )
                    );

                    rigs.add(rig);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            rigs.add(0, new Rig("","","", totalHash,"","",0));

            MyAdapter mAdapter = new MyAdapter(rigs);
            final GridLayoutManager layoutManager = new GridLayoutManager(ctx, 2);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch (mAdapter.getItemViewType(position)) {
                        case 2:
                            return 2;
                        case 0:
                        case 1:
                        default:
                            return 1;
                    }
                }
            });
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);

            totalHash = 0;
            if (!running) {
                running = true;
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_server:
                showSettingsDialog();
                break;
            case R.id.timer:
                showTimerDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("How often do you want the data to refresh?");
        String[] timer = {"Off", "5 secs", "10 secs", "30 secs", "1 min"};
        int checkedItem = getSharedPref().getInt("polling", 0);
        builder.setSingleChoiceItems(timer, checkedItem, (dialog, which) ->
                getSharedPref().edit().putInt("polling", which).apply());
        builder.setPositiveButton("OK", (dialog, which) -> runTask());
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));

    }

    public void showSettingsDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        builder.setView(view);

        EditText inputTemp = view.findViewById(R.id.editText);
        CheckBox checkBox = view.findViewById(R.id.checkbox);
        
        if (getSharedPref().contains("url")) {
            inputTemp.setText(getSharedPref().getString("url", ""));
            checkBox.setChecked(getSharedPref().getBoolean("ignore_ssl", false));
        }

        builder.setPositiveButton("Save", null);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (isValidUrl(inputTemp.getText().toString())) {
                getSharedPref().edit().putString("url", inputTemp.getText().toString()).apply();
                getSharedPref().edit().putBoolean("ignore_ssl", checkBox.isChecked()).apply();
                dialog.dismiss();
                runTask();
            } else {
                TextInputLayout url = view.findViewById(R.id.url);
                url.setError("Enter valid URL");
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private boolean isValidUrl(String url) {

        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return false;
        }

        if (!URLUtil.isValidUrl(url)) {
            return false;
        }

        return !(!url.substring(0, 7).contains("http://") & !url.substring(0, 8).contains("https://"));
    }

    public SharedPreferences getSharedPref() {
        return getApplicationContext().getSharedPreferences(ctx.getPackageName(), 0);
    }
}
