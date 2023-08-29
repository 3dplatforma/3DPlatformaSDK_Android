package ru.threedplatforma.javasample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import ru.threedplatforma.capturing.CapturingResultListener;
import ru.threedplatforma.capturing.CapturingType;
import ru.threedplatforma.capturing.PlatformaCapturingView;

public class CapturingActivity extends BaseActivity {

    public static final String TAG = "CapturingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capturing);

        CapturingType type = getIntent().getParcelableExtra(TAG);

        PlatformaCapturingView capturingView = findViewById(R.id.capturingView);

        //Или укзать в xml app:platformaCapturingType
        if (type != null)
            capturingView.setCapturingType(type);

        //Для контроля жизненного цикла экрана
        capturingView.setLifecycleOwner(this);

        //Для отрисовки диалоговых окон
        capturingView.setFragmentManager(getSupportFragmentManager());

        //Для того, чтобы обрабатывать нажатия кнопки "назад"
        capturingView.setBackPressedDispatcher(getOnBackPressedDispatcher());

        /*
         * В @see[CapturingResultListener.onComplete] приходит id модели, загруженной на сервер
         * Если сработал @see[CapturingResultListener.onCancel] - пользователь отменил все свои
         * действия, и решил завершить процесс съемок
         *
         * Вы должны завершать работу экрана в любом случае, чтобы избежать возможных
         * утечек памяти
         * */
        capturingView.setCapturingResultListener(new CapturingResultListener() {
            @Override
            public void onComplete(String modelId) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("modelId", modelId);
                clipboard.setPrimaryClip(clip);
                showToast("Скопировано\n" + modelId);
                finish();
            }

            @Override
            public void onCancel() {
                finish();
            }
        });

    }

}
