package br.sofex.com.cameratexture.CameraPreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import br.sofex.com.cameratexture.R;

public class CamPreview extends AppCompatActivity {

    TextureView CameraTexture;
    TextView CF_Preview_Data;
    CameraDevice cameraDevice;
    private String cameraId;
    private Size imageDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    Integer CountResumedTimesCamera = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_preview);

        CF_Preview_Data = findViewById(R.id.txt_cam_Data);
        CameraTexture = findViewById(R.id.camTexture);
        CameraTexture.setSurfaceTextureListener(mSurfaceTexture);

        FloatingActionButton fab_fotografar = findViewById(R.id.fab_fotografar);
        fab_fotografar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //Fotografar();
            }
        });

    }

    //Prepara o Textureview
    TextureView.SurfaceTextureListener mSurfaceTexture = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // Pega a data de Hoje , no Sistema Android
            SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(System.currentTimeMillis());
            String DataHoje = formatter.format(date);

            CF_Preview_Data.setText(DataHoje);
            openCamera();
            CountResumedTimesCamera++;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    // TODO Cria o preview da camera
    // Abre a câmera
    private void openCamera(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e("App", "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // Add permission for camera and let user grant the permission
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraFotografar.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }*/
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                manager.openCamera(cameraId, cameraStateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e("app", "openCamera X");
    }
    // Fecha a câmera
    private void closeCamera(){
        if(cameraDevice != null){
            cameraDevice.close(); //fecha a camera
            cameraDevice = null; // variavel fica nula
        }
    }

    // Verifica o estado da câmera ,se está aberta, fechada ou se deu erro
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //Toast.makeText(CameraFotografar.this, " Camera Aberta ", Toast.LENGTH_SHORT).show();
            cameraDevice = camera;
            createCameraPreview();
            //createCameraPreviewBack();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            //Toast.makeText(CameraFotografar.this, " Camera Fechada ", Toast.LENGTH_SHORT).show();
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
            Toast.makeText(CamPreview.this, "Erro :"+error, Toast.LENGTH_SHORT).show();
        }
    };

    // Camera de frente 0 // Camera de trás 1
    private int CameraIdDefault(int width, int height){
        int codCamera = 0;
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraID : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraCharacteristics.LENS_FACING_FRONT){
                    codCamera = 0;
                }
                else if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraCharacteristics.LENS_FACING_BACK){
                    codCamera = 1;
                }
            }
        }catch (CameraAccessException cae){
            //mensagem.ErrorMsg("Error: "+cae);
        }
        return codCamera;
    }
    //TODO : ---------------------------------


    // TODO Preview da Imagem
    // Cria o preview da foto
    protected void createCameraPreview() {
        try {
            // Construa uma nova SurfaceTexture para transmitir imagens para uma determinada textura OpenGL.
            SurfaceTexture surfaceTexture = CameraTexture.getSurfaceTexture();

            //setDefaultBufferSize : Define o tamanho padrão dos buffers de imagem.
            // surfaceTexture.setDefaultBufferSize(CameraFotografarTexture.getWidth(),CameraFotografarTexture.getHeight());
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            Toast.makeText(CamPreview.this, "width :"+width+" - height :"+height, Toast.LENGTH_SHORT).show();

            int heightDisply = displayMetrics.heightPixels + getNavigationBarHeight();
            int WidthDisply  = displayMetrics.widthPixels + getNavigationBarHeight();
            surfaceTexture.setDefaultBufferSize(heightDisply,WidthDisply);

            /*
             * Geralmente, um Surface é criado por ou a partir de um consumidor de buffers de imagem
             * (como SurfaceTexture, MediaRecorder ou Allocation) e é entregue a algum tipo de produtor
             * (como OpenGL, MediaPlayer ou CameraDevice) para atrair.
             */
            Surface surface = new Surface(surfaceTexture);

            // Para obter uma instância do construtor, use o método CameraDevice #createCaptureRequest,
            // que inicializa os campos de solicitação em um dos modelos definidos em CameraDevice.
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            /*
             * Adicione uma superfície à lista de destinos para esta solicitação
             * A superfície adicionada deve ser uma das superfícies incluídas na chamada mais recente para CameraDevice #createCaptureSession,
             * quando a solicitação é feita ao dispositivo da câmera.
             */
            captureRequestBuilder.addTarget(surface);

            /*
             *  CameraCaptureSession.StateCallback()
             *  O retorno de chamada para configurar sessões de captura de uma câmera.
             *  Isso é necessário para verificar se a sessão da câmera está configurada e pronta para mostrar uma visualização.
             *
             *  A callback object for receiving updates about the state of a camera capture session.
             * Este método é chamado quando a sessão começa a processar ativamente solicitações de captura.
             *
             * Se as solicitações de captura forem enviadas antes da chamada de onConfigured (CameraCaptureSession),
             * a sessão começará a processar essas solicitações imediatamente após o retorno de chamada e esse método
             * será chamado imediatamente após onConfigured (CameraCaptureSession).
             */
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    /*
                     * Esse método é chamado quando a fila de captura de entrada do dispositivo da câmera
                     * fica vazia e está pronta para aceitar a próxima solicitação.
                     */
                    cameraCaptureSessions = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    /*
                     * Este método é chamado quando a sessão é fechada.
                     */
                    //mensagem.ErrorMsg("Não é possivel carregar o preview da camera");
                }
            },null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //Atualiza o preview
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e("App", "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            // setRepeatingRequest:  Solicite a captura de imagens repetidamente interminável nesta sessão de captura.
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {e.printStackTrace();
        } catch (NullPointerException null_e){
            //mensagem.ErrorMsgRedirect("Fatal Error \n(CreateCameraPreview(Preview Class)) \n\n"+null_e,CameraFotografar.this, MainActivity.class);
        }
    }

    private int getNavigationBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else return 0;
        }
        return 0;
    }



    // TODO BackroundThread()
    protected void startBackroundThread(){
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackroundThread(){
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // TODO Ciclo de vida
    @Override
    protected void onResume(){
        super.onResume();
        Toast.makeText(this, "Camera Resumida", Toast.LENGTH_SHORT).show();
        if(CameraTexture.isAvailable()){openCamera();}
        else{CameraTexture.setSurfaceTextureListener(mSurfaceTexture); }
    }
    @Override
    protected void onPause(){
        stopBackroundThread();
        closeCamera();
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        closeCamera();
        super.onDestroy();
    }

}
