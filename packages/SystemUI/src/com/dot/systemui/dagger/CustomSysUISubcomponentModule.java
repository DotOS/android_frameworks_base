package com.dot.systemui.dagger;

import dagger.Module;

/**
 * Dagger module for including the WMComponent.
 */
@Module(subcomponents = {CustomSysUIComponent.class})
public abstract class CustomSysUISubcomponentModule {
}
