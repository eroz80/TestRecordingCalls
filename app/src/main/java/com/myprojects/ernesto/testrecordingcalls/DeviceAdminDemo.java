package com.myprojects.ernesto.testrecordingcalls;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ernesto on 02/01/18.
 */

public class DeviceAdminDemo extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        context.stopService(new Intent(context, TService.class));
        Intent myIntent = new Intent(context, TService.class);
        context.startService(myIntent);
    }

    public void onEnabled(Context context, Intent intent) {
    };

    public void onDisabled(Context context, Intent intent) {
    };
}
