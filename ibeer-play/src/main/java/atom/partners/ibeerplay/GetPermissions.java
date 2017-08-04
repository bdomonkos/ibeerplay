package atom.partners.ibeerplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class GetPermissions extends Activity {
    private static Permissions pe;
    private static List<String> ls = new ArrayList();

    public interface Permissions {
        void permissionsResult(boolean granted);
        Context getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, ls.toArray(new String[ls.size()]), 1);
    }

    public static boolean getPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            ls.clear();
            for (String s : permissions) {
                if (ContextCompat.checkSelfPermission(pe.getApplicationContext(), s) != PackageManager.PERMISSION_GRANTED)
                    ls.add(s);
            }
            return ls.isEmpty();
        }
        return true;
    }

    public static void getPermissions(Permissions p) {
        pe = p;
        Intent i = new Intent(p.getApplicationContext(), GetPermissions.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        p.getApplicationContext().startActivity(i);
    }

    public static boolean checkPermissions(Permissions p, String... permissions) {
        pe = p;
        if (getPermissions(permissions)) return true;
        getPermissions(p);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        finish();
        int i = grantResults.length;
        while (i-- > 0) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (pe != null) pe.permissionsResult(false);
                return;
            }
        }
        if (pe != null) pe.permissionsResult(true);
    }

}

/*
  String p;
            if (loc < 10) p = Manifest.permission.READ_EXTERNAL_STORAGE;
            else if (loc == FileSource.LocStorage) p = null;
            else p = Manifest.permission.INTERNET;
            if (p != null && !GetPermissions.checkPermissions(this, p)) return;

              @Override
    public void permissionsResult(boolean granted) {
        //if (granted) loadChecked();
    }
*/