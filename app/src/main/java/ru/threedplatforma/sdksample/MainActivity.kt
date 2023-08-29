package ru.threedplatforma.sdksample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ru.threedplatforma.capturing.CapturingType
import ru.threedplatforma.capturing.ModelAccessType
import ru.threedplatforma.capturing.PlatformaCapturing
import ru.threedplatforma.common.TokenStatus
import ru.threedplatforma.sdk.PlatformaSDK

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val Context.hasStoragePermission: Boolean
        get() = isPermissionGranted(
            targetStoragePermission
        )
    private val Context.hasCameraPermission get() = isPermissionGranted(Manifest.permission.CAMERA)

    private val targetStoragePermission
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else Manifest.permission.WRITE_EXTERNAL_STORAGE

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Необходимо для съемок
        requestCameraAndStoragePermissions()

        findViewById<AppCompatTextView>(R.id.tvTokenStatus).text ="Статус токена: Идет проверка"
        PlatformaSDK.onTokenStatusChangedCallback = { status: TokenStatus ->
            val prefix = "Статус токена: "
            findViewById<AppCompatTextView>(R.id.tvTokenStatus).text = when (status) {
                TokenStatus.SUCCESS -> prefix + "Успех"
                else -> prefix + "Ошибка"
            }
        }

        findViewById<AppCompatTextView>(R.id.tvFrameworkVersion).text =
            "Версия сдк: ${PlatformaSDK.getVersion()}"

        with(findViewById<SwitchCompat>(R.id.switchCacheSession)) {
            isChecked = PlatformaCapturing.getCacheSession()
            setOnCheckedChangeListener { _, isEnabled ->
                PlatformaCapturing.setCacheSession(isEnabled)
            }
        }

        with(findViewById<SwitchCompat>(R.id.switchModelAccess)) {
            isChecked = PlatformaCapturing.modelAccess == ModelAccessType.UNLISTED
            setOnCheckedChangeListener { _, isEnabled ->
                /**
                 * Так же вы можете в любое время поменять доступ, который будут иметь модель,
                 * загруженная в рамках нынешней сессии. Внимание (!) на доступ к моделям,
                 * которые были загружены ранее, смена этого флага не отразится
                 *
                 * @see[ModelAccessType.PUBLIC] - модель будет видна всем на платформе
                 * @see[ModelAccessType.UNLISTED] - модель будет видна только по прямой ссылке
                 *
                 * Изначальное значение - @see[ModelAccessType.PUBLIC]
                 * */

                PlatformaCapturing.modelAccess =
                    isEnabled then ModelAccessType.UNLISTED ?: ModelAccessType.PUBLIC
            }
        }

        findViewById<AppCompatButton>(R.id.btnPanorama).setOnClickListener {
            startCapturingActivity(CapturingType.PANORAMA)
        }

        findViewById<AppCompatButton>(R.id.btnIndoor).setOnClickListener {
            startCapturingActivity(CapturingType.INDOOR)
        }

        findViewById<AppCompatButton>(R.id.btnCars).setOnClickListener {
            startCapturingActivity(CapturingType.CARS_AND_VEHICLES)
        }

        findViewById<AppCompatButton>(R.id.btnView).setOnClickListener {
            checkTokenAndExecute {
                startActivity(Intent(this, ModelViewActivity::class.java))
            }
        }
    }


    override fun onStart() {
        super.onStart()
        //Если пользователь закрыл приложение при включенном кэше, предложим ему восстановить сессию
        PlatformaCapturing.session?.let { previousSession ->
            val llSessionPreview = findViewById<LinearLayout>(R.id.llPreviousSession)
            llSessionPreview.isVisible = true

            findViewById<AppCompatImageView>(R.id.ivPreviousSession).setImageBitmap(previousSession.preview)

            findViewById<AppCompatButton>(R.id.btnResume).setOnClickListener {
                //Без разницы, так как откроется последняя сессия
                startCapturingActivity(null)
            }

            findViewById<AppCompatButton>(R.id.btnDelete).setOnClickListener {
                PlatformaCapturing.clearSessionCaches()
                llSessionPreview.isVisible = false
            }
        }
    }

    private fun startCapturingActivity(type: CapturingType?) {
        checkTokenAndExecute {

            /**
             * В случае, если на устройстве < 3гб свободной памяти, стоит попросить пользователя
             * освободить место. В противном случае - необходимо предупрелить, что съемка может
             * работать нестабильно
             * */
            if (!PlatformaCapturing.hasEnoughMemory) {
                val msg = "На устройстве недостаточно памяти, возможно нестабильная работа"
                Log.e(TAG, msg)
                showToast(msg)
            }

            /**
             * Увы, не на всех устройствах есть датчик гироскопа, необходимый для съемок.
             * В этом случае стоит скрывать для пользователя возможность взаимодействия с
             * функционалом съемок. Этот флаг доступен сразу после вызова @see[PlatformaSDK.init]
             * Советуем строить пользовательский интерфейс, отталкиваясь от него
             * */
            if (!PlatformaCapturing.hasRequiredSensors) {
                val msg = "На устройстве нет гироскопа, съемка невозможна"
                Log.e(TAG, msg)
                showToast(msg)
                return@checkTokenAndExecute
            }

            startActivity(
                Intent(this, CapturingActivity::class.java).apply {
                    type?.let { putExtra(CapturingActivity.TAG, it as Parcelable) }
                })
        }
    }

    /**
     * Перед использованием необходимо проверять @see[TokenStatus], в противном случае - функционал
     * работать не будет. В случае, если статус TokenStatus = @see[TokenStatus.SUCCESS],
     * повторной проверки не требуется
     * */
    private fun checkTokenAndExecute(action: () -> Unit) {
        when (PlatformaSDK.tokenStatus) {
            TokenStatus.CHECKING -> {
                val msg = "Идет проверка токена, повторите позже"
                Log.e(TAG, msg)
                showToast(msg)
            }

            TokenStatus.ERROR -> {
                val msg = "Указан неправильный токен, работа невозможна"
                Log.e(TAG, msg)
                showToast(msg)
            }

            TokenStatus.SUCCESS -> action.invoke()
        }
    }

    private fun requestCameraAndStoragePermissions() {
        val permissions = arrayOf(
            targetStoragePermission,
            Manifest.permission.CAMERA
        )
        if (!hasStoragePermission || !hasCameraPermission)
            requestPermissions(permissions, 123)
    }

    private fun Context.isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}