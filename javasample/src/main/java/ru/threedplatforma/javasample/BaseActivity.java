package ru.threedplatforma.javasample;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

class BaseActivity extends AppCompatActivity {

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
