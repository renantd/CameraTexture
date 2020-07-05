package br.sofex.com.cameratexture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import br.sofex.com.cameratexture.CameraPreview.CamPreview;

public class MainActivity extends AppCompatActivity {

    Button btn_Call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_Call = findViewById(R.id.btn_Call);
        btn_Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CamPreview.class);
                startActivity(intent);
            }
        });

    }
}
