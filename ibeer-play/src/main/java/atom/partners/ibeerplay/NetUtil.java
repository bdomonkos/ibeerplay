package atom.partners.ibeerplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil extends BroadcastReceiver {
    public final static int TypeNo = -1;
    private static NetworkInfo inf;

    NetUtil(Context context) {
        inf = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    void update(Context context) {
        inf = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (inf == null || !inf.isConnectedOrConnecting()) {
            AppHandler.H.showToast("No network");
        } else if (inf.isConnected()) {
            AppHandler.H.getPlaylist();
        }
    }

    void registerReceiver(ContextWrapper context) {
        context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        update(context);
    }

    public static String getNetName() {
        if (inf == null) return "No network";
        return inf.getTypeName();
    }


    public static int getNetType() {
        if (inf != null && inf.isConnected()) {
            return inf.getType();
        }
        return TypeNo;
    }

}			
