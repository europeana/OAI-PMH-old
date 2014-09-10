package com.ontotext;

import com.ontotext.walk.Navigator;
import com.ontotext.walk.StandardNavigator;

/**
 * Created by Simo on 23.4.2014 г..
 */
public class OaiClientControl implements OaiClientControlMBean {
    private Navigator navigator;

    public OaiClientControl(Navigator navigator) {
        this.navigator = navigator;
    }

    public void stop() {
        navigator.stop();
    }
}
