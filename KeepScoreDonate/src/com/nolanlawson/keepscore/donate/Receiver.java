package com.nolanlawson.keepscore.donate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        Helper.setDonateIcon(arg0, !Helper.isFreeVersionInstalled(arg0));
    }
}
