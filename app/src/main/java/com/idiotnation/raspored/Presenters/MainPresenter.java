package com.idiotnation.raspored.Presenters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.Modules.DegreeLoader;
import com.idiotnation.raspored.Modules.FilterOption;
import com.idiotnation.raspored.Modules.FiltersLoader;
import com.idiotnation.raspored.Modules.HTMLConverter;
import com.idiotnation.raspored.Modules.NotificationLoader;
import com.idiotnation.raspored.Modules.NotificationReciever;
import com.idiotnation.raspored.Modules.TableColumn;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;
import static com.idiotnation.raspored.Utils.ERROR_INTERNAL;
import static com.idiotnation.raspored.Utils.ERROR_INTERNET;
import static com.idiotnation.raspored.Utils.ERROR_UNAVAILABLE;
import static com.idiotnation.raspored.Utils.INFO_FINISHED;
import static com.idiotnation.raspored.Utils.INFO_MESSAGE;

public class MainPresenter implements MainContract.Presenter {

    @Inject
    public MainPresenter() {};


    MainContract.View view;
    Context context;
    List<List<TableColumn>> columns;

    @Override
    public void start(MainContract.View view, Context context) {
        this.view = view;
        this.context = context;
        view.initialize();
    }

    @Override
    public void download(String url) {
        try {
            if (url != "NN") {
                HTMLConverter htmlConverter = new HTMLConverter(context, url);
                htmlConverter.setFinishListener(new HTMLConverter.HTMLConverterListener() {
                    @Override
                    public void onFinish(List<List<TableColumn>> columns) {
                        if (columns != null) {
                            view.showMessage(View.VISIBLE, INFO_FINISHED);
                            refreshFilters();
                        } else {
                            view.stopAnimation();
                            view.showMessage(View.VISIBLE, ERROR_INTERNAL);
                        }
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    htmlConverter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    htmlConverter.execute();
                }
            } else {
                view.showMessage(View.VISIBLE, ERROR_UNAVAILABLE);
                view.stopAnimation();
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.showMessage(View.VISIBLE, ERROR_INTERNAL);
            view.stopAnimation();
        }
    }

    @Override
    public List<List<TableColumn>> getRaspored() {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(context.getFilesDir(), "raspored.json")));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
        } finally {
            return (List<List<TableColumn>>) new Gson().fromJson(text.toString(), new TypeToken<List<List<TableColumn>>>() {
            }.getType());
        }
    }

    @Override
    public void refresh(final int idNumber) {
        if (idNumber != -1) {
            view.startAnimation();
            DegreeLoader degreeLoader = new DegreeLoader(context);
            degreeLoader.setOnFinishListener(new DegreeLoader.onFinihListener() {
                @Override
                public void onFinish(List list) {
                    if (list != null) {
                        context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE).edit().putInt("SpinnerDefault", idNumber).apply();
                        download(list.get(idNumber).toString());
                    } else {
                        view.stopAnimation();
                        view.showMessage(View.VISIBLE, ERROR_INTERNET);
                    }
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                degreeLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                degreeLoader.execute();
            }
        } else {
            view.showMessage(View.VISIBLE, INFO_MESSAGE);
            view.stopAnimation();
        }
    }

    @Override
    public void refreshNotifications() {
        NotificationLoader notificationLoader = new NotificationLoader(context, getRaspored());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notificationLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            notificationLoader.execute();
        }
    }

    @Override
    public void refreshFilters() {
        FiltersLoader filtersLoader = new FiltersLoader(context, getRaspored());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            filtersLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            filtersLoader.execute();
        }
        filtersLoader.setOnFinishListener(new FiltersLoader.onFinishListner() {
            @Override
            public void onFinish() {
                refreshNotifications();
                view.setRaspored(getRaspored());
            }
        });
    }

    @Override
    public void populateHours(RelativeLayout parentView, Context context) {
        for (int i = 0; i < 13; i++) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setText(String.format("%02d", 7 + i) + ":00");
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(Utils.getColor(R.color.hoursTextColorPrimary, context));
            float scale = context.getResources().getDisplayMetrics().density;
            GradientDrawable textViewBg = (GradientDrawable) context.getResources().getDrawable(R.drawable.separator).getConstantState().newDrawable();
            textViewBg.setStroke((int) (1 * scale + 0.5f), Utils.getColor(R.color.hoursBackgroundStrokeColor, context));
            textViewBg.setColor(Utils.getColor(R.color.hoursBackgroundColor, context));
            textView.setBackgroundDrawable(textViewBg);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, i == 12 ? ViewGroup.LayoutParams.MATCH_PARENT : parentView.getHeight() / 13);
            params.topMargin = (parentView.getHeight() / 13) * i;
            textView.setLayoutParams(params);
            parentView.addView(textView);
        }
    }
}
