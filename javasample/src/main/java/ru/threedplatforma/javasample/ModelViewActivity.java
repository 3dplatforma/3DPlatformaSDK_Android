package ru.threedplatforma.javasample;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import java.util.Objects;

import ru.threedplatforma.common.util.exception.PlatformaException;
import ru.threedplatforma.view.PlatformaModel;
import ru.threedplatforma.view.PlatformaModelView;
import ru.threedplatforma.view.PlatformaModelViewParams;
import ru.threedplatforma.view.PlatformaViewer;

public class ModelViewActivity extends BaseActivity {

    private AppCompatButton btnSearch;
    private AppCompatEditText etModelIdOrUrl;
    private ProgressBar pbLoading;
    private LinearLayout inputsContainer;
    private PlatformaModelView modelView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_model_view);

        btnSearch = findViewById(R.id.btnSearch);
        etModelIdOrUrl = findViewById(R.id.etModelIdOrLink);
        pbLoading = findViewById(R.id.pbLoading);
        inputsContainer = findViewById(R.id.llInputsContainer);
        modelView = findViewById(R.id.modelView);
        btnSearch.setOnClickListener(v -> onSearchClicked());
    }

    private void onSearchClicked() {
        String searchText = Objects.requireNonNull(etModelIdOrUrl.getText()).toString();
        if (searchText.isEmpty()) {
            return;
        }
        showLoading();
        boolean isUrl = searchText.contains("https://");

        /*
         * Для информации о модели по id или ссылке, используется @see[PlatformaViewer]
         * В случае успеха, возвращает доменное представление @see[PlatformaModel],
         * с помощью которого можно загрузить @see[PlatformaModelView.loadModel], либо получить
         * ссылку на превью для отрисовки в вашем приложении @see[PlatformaModel.getPreviewUrl]
         *
         * Подробнее об ошибках, которые могут прийти написано в официальной документации
         * */

        PlatformaViewer.ModelCallback callback = new PlatformaViewer.ModelCallback() {
            @Override
            public void onSuccess(@NonNull PlatformaModel platformaModel) {
                displayModel(platformaModel);
            }

            @Override
            public void onFailure(@NonNull PlatformaException e) {
                handleError(e);
            }
        };

        if (!isUrl)
            PlatformaViewer.getModel(searchText, callback);
        else
            PlatformaViewer.getModelByLink(searchText, callback);

    }

    private void displayModel(PlatformaModel model) {
        /*
         * Загрузим модель, полученную ранее. Так же можно передать дополнительные параметры
         * отображения в @see[PlatformaModelViewParams]. Более подробно о них, а так же об ошибках,
         * которые могут возникнуть - в официальной документации
         * */
        PlatformaModelViewParams params = new PlatformaModelViewParams.Builder()
                .autoRun(true)
                .hideHints(true)
                .build();

        modelView.loadModel(model, params, new PlatformaModelView.OnModelLoadListener() {
            @Override
            public void onLoadingFailed(@NonNull PlatformaException e) {
                handleError(e);
            }

            @Override
            public void onModelReady() {
                inputsContainer.setVisibility(View.GONE);
            }
        });

    }

    private void handleError(PlatformaException exception) {
        hideLoading();
        showToast(exception.toString());
        exception.printStackTrace();
    }

    private void hideLoading() {
        pbLoading.setVisibility(View.GONE);
        btnSearch.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        pbLoading.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.GONE);
    }

}