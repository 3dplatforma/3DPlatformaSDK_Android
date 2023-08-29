package ru.threedplatforma.javasample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import ru.threedplatforma.capturing.CapturingType;
import ru.threedplatforma.capturing.ModelAccessType;
import ru.threedplatforma.capturing.PlatformaCapturing;
import ru.threedplatforma.capturing.PlatformaCapturingSession;
import ru.threedplatforma.common.TokenStatus;
import ru.threedplatforma.sdk.PlatformaSDK;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private boolean hasStoragePermission() {
        return isPermissionGranted(targetStoragePermission());
    }

    private boolean hasCameraPermission() {
        return isPermissionGranted(Manifest.permission.CAMERA);
    }

    private String targetStoragePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Перед использованием необходимо проверять @see[TokenStatus], в противном случае - функционал
     * работать не будет. В случае, если статус TokenStatus = @see[TokenStatus.SUCCESS],
     * повторной проверки не требуется
     */
    private boolean isTokenValid() {

        switch (PlatformaSDK.getTokenStatus()) {
            case CHECKING: {
                String msg = "Идет проверка токена, повторите позже";
                Log.e(TAG, msg);
                showToast(msg);
                return false;
            }
            case ERROR: {
                String msg = "Указан неправильный токен, работа невозможна";
                Log.e(TAG, msg);
                showToast(msg);
                return false;
            }
            default:
                return true;
        }
    }

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatTextView tvTokenStatus = findViewById(R.id.tvTokenStatus);
        AppCompatTextView tvFrameworkVersion = findViewById(R.id.tvFrameworkVersion);

        SwitchCompat switchCacheSession = findViewById(R.id.switchCacheSession);
        SwitchCompat switchModelAccess = findViewById(R.id.switchModelAccess);

        AppCompatButton btnPanorama = findViewById(R.id.btnPanorama);
        AppCompatButton btnIndoor = findViewById(R.id.btnIndoor);
        AppCompatButton btnCars = findViewById(R.id.btnCars);
        AppCompatButton btnView = findViewById(R.id.btnView);

        //Необходимо для съемок
        requestCameraAndStoragePermissions();

        tvTokenStatus.setText("Статус токена: Идет проверка");

        PlatformaSDK.setOnTokenStatusChangedListener(tokenStatus -> {
            String status = "Статус токена: ";
            if (tokenStatus == TokenStatus.SUCCESS) {
                status += "Успех";
            } else  {
                status += "Ошибка";
            }

            tvTokenStatus.setText(status);
        });

        tvFrameworkVersion.setText("Версия сдк: " + PlatformaSDK.getVersion());

        switchCacheSession.setChecked(PlatformaCapturing.getCacheSession());
        switchCacheSession.setOnCheckedChangeListener((buttonView, isChecked) -> PlatformaCapturing.setCacheSession(isChecked));

        switchModelAccess.setChecked(PlatformaCapturing.getModelAccess() == ModelAccessType.UNLISTED);
        switchModelAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            /*Так же вы можете в любое время поменять доступ, который будут иметь модель,
             * загруженная в рамках нынешней сессии. Внимание (!) на доступ к моделям,
             * которые были загружены ранее, смена этого флага не отразится
             *
             * @see[ModelAccessType.PUBLIC] - модель будет видна всем на платформе
             * @see[ModelAccessType.UNLISTED] - модель будет видна только по прямой ссылке
             *
             * Изначальное значение - @see[ModelAccessType.PUBLIC]
             */

            PlatformaCapturing.setModelAccess(isChecked ? ModelAccessType.UNLISTED : ModelAccessType.PUBLIC);

        });

        btnIndoor.setOnClickListener(v -> startCapturingActivity(CapturingType.INDOOR));
        btnCars.setOnClickListener(v -> startCapturingActivity(CapturingType.CARS_AND_VEHICLES));
        btnPanorama.setOnClickListener(v -> startCapturingActivity(CapturingType.PANORAMA));

        btnView.setOnClickListener(v -> {
            if (isTokenValid()) {
                startActivity(new Intent(this, ModelViewActivity.class));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Если пользователь закрыл приложение при включенном кэше, предложим ему восстановить сессию
        PlatformaCapturingSession session = PlatformaCapturing.getSession();
        if (session != null) {
            LinearLayout llPreviousSession = findViewById(R.id.llPreviousSession);
            AppCompatImageView ivPreviousSession = findViewById(R.id.ivPreviousSession);
            AppCompatButton btnResume = findViewById(R.id.btnResume);
            AppCompatButton btnDelete = findViewById(R.id.btnDelete);

            llPreviousSession.setVisibility(View.VISIBLE);
            ivPreviousSession.setImageBitmap(session.getPreview());

            btnResume.setOnClickListener(v -> {
                //Без разницы, так как откроется последняя сессия
                startCapturingActivity(null);
            });

            btnDelete.setOnClickListener(v -> {
                PlatformaCapturing.clearSessionCaches();
                llPreviousSession.setVisibility(View.GONE);
            });
        }

    }

    private void startCapturingActivity(@Nullable CapturingType type) {
        if (!isTokenValid())
            return;

        /*
         * В случае, если на устройстве < 3гб свободной памяти, стоит попросить пользователя
         * освободить место. В противном случае - необходимо предупрелить, что съемка может
         * работать нестабильно
         * */
        if (!PlatformaCapturing.getHasEnoughMemory()) {
            String msg = "На устройстве недостаточно памяти, возможно нестабильная работа";
            Log.e(TAG, msg);
            showToast(msg);
        }

        /*
         * Увы, не на всех устройствах есть датчик гироскопа, необходимый для съемок.
         * В этом случае стоит скрывать для пользователя возможность взаимодействия с
         * функционалом съемок. Этот флаг доступен сразу после вызова @see[PlatformaSDK.init]
         * Советуем строить пользовательский интерфейс, отталкиваясь от него
         * */
        if (!PlatformaCapturing.getHasRequiredSensors()) {
            String msg = "На устройстве нет гироскопа, съемка невозможна";
            Log.e(TAG, msg);
            showToast(msg);
            return;
        }

        Intent capturingIntent = new Intent(this, CapturingActivity.class);
        if (type != null) {
            capturingIntent.putExtra(CapturingActivity.TAG, (Parcelable) type);
        }

        startActivity(capturingIntent);
    }

    private void requestCameraAndStoragePermissions() {
        String[] permissions = new String[]{Manifest.permission.CAMERA, targetStoragePermission()};

        if (!hasStoragePermission() || !hasCameraPermission())
            requestPermissions(permissions, 123);
    }

}
