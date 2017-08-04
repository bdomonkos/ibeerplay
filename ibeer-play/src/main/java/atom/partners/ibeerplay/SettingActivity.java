package atom.partners.ibeerplay;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    TextView id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // id = (EditText) findViewById(R.id.device_id);
        int deviceId = getIntent().getIntExtra("id", -1);
        if (deviceId > 0) id.setText(Integer.toString(deviceId));
       // url = (EditText) findViewById(R.id.url);
   //     url.setText(getIntent().getStringExtra("url"));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
             //   AppHandler.H.setConfig(id.getText().toString());
                finish();
            }
        });
    }

}
