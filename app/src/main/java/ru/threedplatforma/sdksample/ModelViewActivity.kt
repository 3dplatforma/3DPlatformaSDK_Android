package ru.threedplatforma.sdksample

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import ru.threedplatforma.common.util.exception.PlatformaException
import ru.threedplatforma.view.PlatformaModel
import ru.threedplatforma.view.PlatformaModelView
import ru.threedplatforma.view.PlatformaModelViewParams
import ru.threedplatforma.view.PlatformaViewer

class ModelViewActivity : AppCompatActivity(R.layout.activity_model_view) {

    private lateinit var btnSearch: AppCompatButton
    private lateinit var etModelIdOrUrl: AppCompatEditText
    private lateinit var pbLoading: ProgressBar
    private lateinit var inputsContainer: LinearLayout
    private lateinit var modelView: PlatformaModelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnSearch = findViewById(R.id.btnSearch)
        etModelIdOrUrl = findViewById(R.id.etModelIdOrLink)
        pbLoading = findViewById(R.id.pbLoading)
        inputsContainer = findViewById(R.id.llInputsContainer)
        modelView = findViewById(R.id.modelView)
        btnSearch.setOnClickListener { v: View? -> onSearchClicked() }

    }

    private fun onSearchClicked() {
        val searchText = etModelIdOrUrl.text.toString()
        if (searchText.isEmpty()) {
            return
        }
        showLoading()
        val isUrl = searchText.contains("https://")

        /**
         * Для информации о модели по id или ссылке, используется @see[PlatformaViewer]
         * В случае успеха, возвращает доменное представление @see[PlatformaModel],
         * с помощью которого можно загрузить @see[PlatformaModelView.loadModel], либо получить
         * ссылку на превью для отрисовки в вашем приложении @see[PlatformaModel.getPreviewUrl]
         *
         * Подробнее об ошибках, которые могут прийти написано в официальной документации
         * */
        if (!isUrl) {
            PlatformaViewer.getModel(
                searchText,
                ::displayModel,
                onFailure = ::handleError)
        } else {
            PlatformaViewer.getModelByLink(
                searchText,
                ::displayModel,
                onFailure = ::handleError)
        }
    }

    private fun displayModel(model: PlatformaModel) {
        /**
         * Загрузим модель, полученную ранее. Так же можно передать дополнительные параметры
         * отображения в @see[PlatformaModelViewParams]. Более подробно о них, а так же об ошибках,
         * которые могут возникнуть - в официальной документации
         * */
        modelView.loadModel(model,
            modelViewParams = PlatformaModelViewParams(
                autoRun = true, hideHints = true
            ), onModelReady = {
                inputsContainer.isVisible = false
            }, onLoadingFailed = ::handleError)
    }

    private fun handleError(exception: PlatformaException) {
        hideLoading()
        showToast(exception.toString())
        exception.printStackTrace()
    }

    private fun hideLoading() {
        pbLoading.isVisible = false
        btnSearch.isVisible = true
    }

    private fun showLoading() {
        pbLoading.isVisible = true
        btnSearch.isVisible = false
    }
}