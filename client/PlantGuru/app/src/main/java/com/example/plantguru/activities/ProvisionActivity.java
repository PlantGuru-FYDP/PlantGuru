package com.example.plantguru.activities;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ContentLoadingProgressBar;

import com.espressif.provisioning.ESPDevice;
import com.example.plantguru.constants.AppConstants;
import com.example.plantguru.R;
import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.ResponseListener;
import com.example.plantguru.models.Plant;
import com.example.plantguru.utils.SharedPreferencesHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProvisionActivity extends AppCompatActivity {

    private static final String TAG = ProvisionActivity.class.getSimpleName();

    private TextView tvTitle, tvBack, tvCancel;
    private ImageView tick1, tick2, tick3, tick4;
    private ContentLoadingProgressBar progress1, progress2, progress3, progress4;
    private TextView tvErrAtStep1, tvErrAtStep2, tvErrAtStep3, tvErrAtStep4, tvProvError;

    private CardView btnOk;
    private TextView txtOkBtn;

    private String ssidValue, passphraseValue = "";
    private ESPProvisionManager provisionManager;
    private BluetoothDevice device;
    private boolean isProvisioningCompleted = false;
    private String userToken; // Add this line to store the user token

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision);

        Intent intent = getIntent();
        ssidValue = intent.getStringExtra(AppConstants.KEY_WIFI_SSID);
        passphraseValue = intent.getStringExtra(AppConstants.KEY_WIFI_PASSWORD);
        userToken = "android";// intent.getStringExtra(AppConstants.KEY_USER_TOKEN); // Add this line to retrieve the user token from the intent
        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());

        initViews();
        EventBus.getDefault().register(this);

        Log.d(TAG, "Selected AP -" + ssidValue);
        showLoading();
        sendDataToCustomEndPoint(); // Send data to custom endpoint

    }

    @Override
    public void onBackPressed() {
        provisionManager.getEspDevice().disconnectDevice();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceConnectionEvent event) {

        Log.d(TAG, "On Device Connection Event RECEIVED : " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                if (!isFinishing() && !isProvisioningCompleted) {
                    showAlertForDeviceDisconnected();
                }
                break;
        }
    }

    private View.OnClickListener okBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            provisionManager.getEspDevice().disconnectDevice();
            finish();
        }
    };

    private void initViews() {

        tvTitle = findViewById(R.id.main_toolbar_title);
        tvBack = findViewById(R.id.btn_back);
        tvCancel = findViewById(R.id.btn_cancel);

        tick1 = findViewById(R.id.iv_tick_1);
        tick2 = findViewById(R.id.iv_tick_2);
        tick3 = findViewById(R.id.iv_tick_3);
        tick4 = findViewById(R.id.iv_tick_4);

        progress1 = findViewById(R.id.prov_progress_1);
        progress2 = findViewById(R.id.prov_progress_2);
        progress3 = findViewById(R.id.prov_progress_3);
        progress4 = findViewById(R.id.prov_progress_4);

        tvErrAtStep1 = findViewById(R.id.tv_prov_error_1);
        tvErrAtStep2 = findViewById(R.id.tv_prov_error_2);
        tvErrAtStep3 = findViewById(R.id.tv_prov_error_3);
        tvErrAtStep4 = findViewById(R.id.tv_prov_error_4);
        tvProvError = findViewById(R.id.tv_prov_error);

        tvTitle.setText(R.string.title_activity_provisioning);
        tvBack.setVisibility(View.GONE);
        tvCancel.setVisibility(View.GONE);

        btnOk = findViewById(R.id.btn_ok);
        txtOkBtn = findViewById(R.id.text_btn);
        btnOk.findViewById(R.id.iv_arrow).setVisibility(View.GONE);

        txtOkBtn.setText(R.string.btn_ok);
        btnOk.setOnClickListener(okBtnClickListener);
    }

    private void sendDataToCustomEndPoint() {
        tick1.setVisibility(View.GONE);
        progress1.setVisibility(View.VISIBLE);

        provisionManager.getEspDevice().sendDataToCustomEndPoint(AppConstants.USER_TOKEN_ENDPOINT, userToken.getBytes(), new ResponseListener() {
            @Override
            public void onSuccess(byte[] returnData) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_checkbox_on);
                        tick1.setVisibility(View.VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tick2.setVisibility(View.GONE);
                        progress2.setVisibility(View.VISIBLE);
                        doProvisioning();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick1.setImageResource(R.drawable.ic_error);
                        tick1.setVisibility(View.VISIBLE);
                        progress1.setVisibility(View.GONE);
                        tvErrAtStep1.setVisibility(View.VISIBLE);
                        tvErrAtStep1.setText(R.string.error_prov_step_4);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }
        });
    }
    private void doProvisioning() {
        provisionManager.getEspDevice().provision(ssidValue, passphraseValue, new ProvisionListener() {

            @Override
            public void createSessionFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_error);
                        tick2.setVisibility(View.VISIBLE);
                        progress2.setVisibility(View.GONE);
                        tvErrAtStep2.setVisibility(View.VISIBLE);
                        tvErrAtStep2.setText(R.string.error_session_creation);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void wifiConfigSent() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_checkbox_on);
                        tick2.setVisibility(View.VISIBLE);
                        progress2.setVisibility(View.GONE);
                        tick3.setVisibility(View.GONE);
                        progress3.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void wifiConfigFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick2.setImageResource(R.drawable.ic_error);
                        tick2.setVisibility(View.VISIBLE);
                        progress2.setVisibility(View.GONE);
                        tvErrAtStep2.setVisibility(View.VISIBLE);
                        tvErrAtStep2.setText(R.string.error_prov_step_1);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void wifiConfigApplied() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick3.setImageResource(R.drawable.ic_checkbox_on);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        tick4.setVisibility(View.GONE);
                        progress4.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick3.setImageResource(R.drawable.ic_error);
                        tick3.setVisibility(View.VISIBLE);
                        progress3.setVisibility(View.GONE);
                        tvErrAtStep3.setVisibility(View.VISIBLE);
                        tvErrAtStep3.setText(R.string.error_prov_step_2);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void provisioningFailedFromDevice(final ESPConstants.ProvisionFailureReason failureReason) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (failureReason) {
                            case AUTH_FAILED:
                                tvErrAtStep4.setText(R.string.error_authentication_failed);
                                break;
                            case NETWORK_NOT_FOUND:
                                tvErrAtStep4.setText(R.string.error_network_not_found);
                                break;
                            case DEVICE_DISCONNECTED:
                            case UNKNOWN:
                                tvErrAtStep4.setText(R.string.error_prov_step_3);
                                break;
                        }
                        tick4.setImageResource(R.drawable.ic_error);
                        tick4.setVisibility(View.VISIBLE);
                        progress4.setVisibility(View.GONE);
                        tvErrAtStep4.setVisibility(View.VISIBLE);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void deviceProvisioningSuccess() {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        savePlant();
                        isProvisioningCompleted = true;
                        tick4.setImageResource(R.drawable.ic_checkbox_on);
                        tick4.setVisibility(View.VISIBLE);
                        progress4.setVisibility(View.GONE);
                        hideLoading();
                    }
                });
            }

            @Override
            public void onProvisioningFailed(Exception e) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tick4.setImageResource(R.drawable.ic_error);
                        tick4.setVisibility(View.VISIBLE);
                        progress4.setVisibility(View.GONE);
                        tvErrAtStep4.setVisibility(View.VISIBLE);
                        tvErrAtStep4.setText(R.string.error_prov_step_3);
                        tvProvError.setVisibility(View.VISIBLE);
                        hideLoading();
                    }
                });
            }
        });
    }

    private void savePlant() {
        Log.d("plantguru", "saving plant!!");
        ESPDevice e = provisionManager.getEspDevice();
        BluetoothDevice b = e.getBluetoothDevice();
        SharedPreferencesHelper.savePlant(this, new Plant(
                e.getDeviceName()
                ,
                "fernie"));
    }

    private void showLoading() {
        btnOk.setEnabled(false);
        btnOk.setAlpha(0.5f);
    }

    public void hideLoading() {
        btnOk.setEnabled(true);
        btnOk.setAlpha(1f);
    }

    private void showAlertForDeviceDisconnected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.dialog_msg_ble_device_disconnection);
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }
}
