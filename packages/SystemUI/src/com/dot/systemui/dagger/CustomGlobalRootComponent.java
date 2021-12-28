package com.dot.systemui.dagger;

import android.content.Context;

import com.android.systemui.dagger.GlobalModule;
import com.android.systemui.dagger.GlobalRootComponent;
import com.android.systemui.dagger.WMModule;

import com.android.systemui.util.concurrency.ThreadFactory;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {
        GlobalModule.class,
        CustomSysUISubcomponentModule.class,
        WMModule.class})
public interface CustomGlobalRootComponent extends GlobalRootComponent {

    @Component.Builder
    interface Builder extends GlobalRootComponent.Builder {
        CustomGlobalRootComponent build();
    }

    @Override
    CustomSysUIComponent.Builder getSysUIComponent();
}
