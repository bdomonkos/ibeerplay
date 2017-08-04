package atom.partners.ibeerplay;

import android.Manifest;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;

public class SplashActivity extends AppCompatActivity implements GetPermissions.Permissions {

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViewById(R.id.lin_lay).startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha));
        findViewById(R.id.splash).startAnimation(AnimationUtils.loadAnimation(this, R.anim.translate));

        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(4000);
                } catch (Exception e) {
                }
                if (GetPermissions.checkPermissions(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)) {
                    startIbeer();
                }

            }
        };
        splashTread.start();

    }

    private void startIbeer() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void permissionsResult(boolean granted) {
        if (granted) startIbeer();
        else finish();
    }


}