package com.idiotnation.raspored.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.adapters.SettingsCoursesAdapter;
import com.idiotnation.raspored.custom.MaterialDialog;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.services.CourseService;
import com.idiotnation.raspored.views.SettingsView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class CourseSelectionDialog extends MaterialDialog {

    @BindView(R.id.settings_view_course_selection_list)
    RecyclerView list;

    @BindView(R.id.settings_view_course_selection_progress)
    ContentLoadingProgressBar progressBar;

    private SettingsCoursesAdapter coursesAdapter;
    private List<CourseDto> courses;
    private OnSelectListener listener;
    private Integer selectedCourse;
    private Integer filteredOutCourse;

    public CourseSelectionDialog(Activity activity, Integer selectedCourse, Integer filteredOutCourse) {
        super(activity);
        this.selectedCourse = selectedCourse;
        this.filteredOutCourse = filteredOutCourse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view_course_selection_dialog);
        setTitle(getContext().getResources().getString(R.string.settings_view_course_selection));
        ButterKnife.bind(this);
        progressBar.show();
        coursesAdapter = new SettingsCoursesAdapter(getContext(), new ArrayList<CourseDto>(), selectedCourse);
        coursesAdapter.setItemOnSelectListener(new SettingsCoursesAdapter.ItemOnSelectListener() {
            @Override
            public void onSelect(CourseDto item) {
                if (listener != null) {
                    listener.onSelect(item);
                    dismiss();
                }
            }
        });
        list.setAdapter(coursesAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        new CourseService()
                .syncLatest(((SettingsView) getActivity()).presenter.getCoursesFilter(), filteredOutCourse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<CourseDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<CourseDto> courses) {
                        if (courses != null) {
                            coursesAdapter.setList(courses);
                            coursesAdapter.notifyDataSetChanged();
                            progressBar.hide();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof IOException) {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.request_error_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.request_error_internal), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public interface OnSelectListener {
        void onSelect(CourseDto course);
    }

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }
}
