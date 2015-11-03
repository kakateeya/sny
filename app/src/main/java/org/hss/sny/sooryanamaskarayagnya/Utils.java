package org.hss.sny.sooryanamaskarayagnya;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by sk on 12/13/14.
 */
public class Utils {
    public static GoogleAccountCredential getBaseCredential(Context c) {
        return GoogleAccountCredential.usingAudience(c, "server:client_id:" + Config.WEB_CLIENT_ID);
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String msg) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setIndeterminate(true);
        pd.setTitle(title);
        pd.setMessage(msg);
        pd.show();
        return pd;
    }
}
