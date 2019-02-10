package kz.qazlatynhelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText srcText =  findViewById(R.id.srcText);
        final EditText descText = findViewById(R.id.descText);
        Button btn_convert = findViewById(R.id.btn_convert);

        btn_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cyrlText = String.valueOf(srcText.getText());
                String latynText = ConvertHelper.Cyrl2Latyn(cyrlText);
                descText.setText(latynText);
            }
        });
    }
}
