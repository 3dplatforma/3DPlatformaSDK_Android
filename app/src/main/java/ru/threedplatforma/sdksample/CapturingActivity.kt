package ru.threedplatforma.sdksample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.threedplatforma.capturing.CapturingResultListener
import ru.threedplatforma.capturing.CapturingType
import ru.threedplatforma.capturing.PlatformaCapturingView

class CapturingActivity : AppCompatActivity(R.layout.activity_capturing) {

    companion object {
        const val TAG = "CapturingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val type: CapturingType? = intent.getParcelableExtra(TAG)

        with(findViewById<PlatformaCapturingView>(R.id.capturingView)) {

            //Или укзать в xml app:platformaCapturingType
            type?.let { setCapturingType(it) }

            //Для контроля жизненного цикла экрана
            setLifecycleOwner(this@CapturingActivity)

            //Для отрисовки диалоговых окон
            setFragmentManager(supportFragmentManager)

            //Для того, чтобы обрабатывать нажатия кнопки "назад"
            setBackPressedDispatcher(onBackPressedDispatcher)

            /**
             * В @see[CapturingResultListener.onComplete] приходит id модели, загруженной на сервер
             * Если сработал @see[CapturingResultListener.onCancel] - пользователь отменил все свои
             * действия, и решил завершить процесс съемок
             *
             * Вы должны завершать работу экрана в любом случае, чтобы избежать возможных
             * утечек памяти
             * */
            setCapturingResultCallback(onComplete = { modelId: String ->
                context.copyToClipboard(modelId)
                showToast("Скопировано\n$modelId")
                finish()
            }, onCancel = ::finish)
        }
    }

}