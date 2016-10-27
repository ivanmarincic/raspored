package com.idiotnation.raspored;

import android.app.Application;

import com.idiotnation.raspored.Presenters.MainPresenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;

public class RasporedApplication extends Application {

    @Singleton
    @Component(modules = AndroidModule.class)
    public interface ApplicationComponent {
        void inject(RasporedApplication application);
        void inject(MainActivity activity);
    }

    @Inject
    MainPresenter presenter;

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerRasporedApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
        component().inject(this);
    }

    public ApplicationComponent component() {
        return component;
    }
}