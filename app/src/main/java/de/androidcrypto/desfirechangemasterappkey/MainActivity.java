package de.androidcrypto.desfirechangemasterappkey;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.skjolber.desfire.ev1.model.command.DefaultIsoDepWrapper;
import com.github.skjolber.desfire.ev1.model.command.IsoDepWrapper;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Arrays;

import nfcjlib.core.DESFireAdapter;
import nfcjlib.core.DESFireEV1;
import nfcjlib.core.KeyType;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText output, errorCode;
    private com.google.android.material.textfield.TextInputLayout errorCodeLayout;
    private RadioButton changeMasterAppKeyToAes, changeMasterAppKeyToDes;
    private ScrollView scrollView;

    // constants
    private String lineSeparator = "----------";
    private final byte[] MASTER_APPLICATION_IDENTIFIER = new byte[3]; // '00 00 00'
    private final byte[] MASTER_APPLICATION_KEY_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000");
    private final byte[] MASTER_APPLICATION_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
    private final int COLOR_GREEN = Color.rgb(0, 255, 0);
    private final int COLOR_RED = Color.rgb(255, 0, 0);

    // variables for NFC handling

    private NfcAdapter mNfcAdapter;
    private IsoDep isoDep;
    private byte[] tagIdByte;
    private DESFireEV1 desfire;
    private DESFireAdapter desFireAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        output = findViewById(R.id.etOutput);
        errorCode = findViewById(R.id.etErrorCode);
        errorCodeLayout = findViewById(R.id.etErrorCodeLayout);

        changeMasterAppKeyToAes = findViewById(R.id.rbChangeKeyTypeToAes);
        changeMasterAppKeyToDes = findViewById(R.id.rbChangeKeyTypeToDes);

        scrollView = findViewById(R.id.svScrollView);

        // hide soft keyboard from showing up on startup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private void runChangeMasterKeys() {
        System.out.println("runChangeMasterKeys");
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success;
                try {
                    // select master application
                    success = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
                    writeToUiAppend(output, "selectMasterApplicationSuccess: " + success);
                    if (!success) {
                        writeToUiAppend(output, "selectMasterApplication NOT Success, aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "selectMasterApplication NOT Success, aborted", COLOR_RED);
                        scrollView.smoothScrollTo(0, 0);
                        return;
                    }
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    scrollView.smoothScrollTo(0, 0);
                    return;
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    scrollView.smoothScrollTo(0, 0);
                    return;
                }
                if (changeMasterAppKeyToAes.isChecked()) {
                    // authenticate with default DES key
                    success = authenticateApplication(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, "Default DES Key", KeyType.DES);
                    writeToUiAppend(output, "Authenticate with Default DES Key: " + success);
                    if (!success) {
                        writeToUiAppend(output, "Authentication NOT Success, aborted");
                        writeToUiAppend(output, "Maybe the Master Application Key was changed or is DES Key ?");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Authentication NOT Success, aborted", COLOR_RED);
                        scrollView.smoothScrollTo(0, 0);
                        return;
                    }
                    // change key to AES
                    try {
                        success = desfire.changeKey(MASTER_APPLICATION_KEY_NUMBER, KeyType.AES, MASTER_APPLICATION_KEY_AES_DEFAULT, MASTER_APPLICATION_KEY_DES_DEFAULT);
                    } catch (IOException e) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                        e.printStackTrace();
                        scrollView.smoothScrollTo(0, 0);
                        return;
                    }
                    if (success) {
                        writeToUiAppend(output, "Change of the Master Application Key to AES was Success");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Change of the Master Application Key was Success", COLOR_GREEN);
                        scrollView.smoothScrollTo(0, 0);
                    } else {
                        writeToUiAppend(output, "Change of the Master Application Key to AES was NOT Success, aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Change of the Master Application Key was NOT Success, aborted", COLOR_RED);
                        scrollView.smoothScrollTo(0, 0);
                        return;
                    }
                } else {
                    // authenticate with default AES key
                    success = authenticateApplication(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT, "Default AES Key", KeyType.AES);
                    writeToUiAppend(output, "Authenticate with Default DES Key: " + success);
                    if (!success) {
                        writeToUiAppend(output, "Authentication NOT Success, aborted");
                        writeToUiAppend(output, "Maybe the Master Application Key was changed or is AES Key ?");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Authentication NOT Success, aborted", COLOR_RED);
                        scrollView.smoothScrollTo(0, 0);
                        return;
                    }
                    // change key to DES
                    try {
                        success = desfire.changeKey(MASTER_APPLICATION_KEY_NUMBER, KeyType.DES, MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_AES_DEFAULT);
                    } catch (IOException e) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                        e.printStackTrace();
                        scrollView.smoothScrollTo(0, 0);
                        return;
                    }
                    if (success) {
                        writeToUiAppend(output, "Change of the Master Application Key to DES was Success");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Change of the Master Application Key was Success", COLOR_GREEN);
                        scrollView.smoothScrollTo(0, 0);
                    } else {
                        writeToUiAppend(output, "Change of the Master Application Key to DES was NOT Success, aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Change of the Master Application Key was NOT Success, aborted", COLOR_RED);
                        scrollView.smoothScrollTo(0, 0);
                        return;
                    }
                }
            }
        });
        worker.start();
    }

    /**
     * section for authentication
     */

    private boolean authenticateApplication(byte keyNumber, byte[] key, String keyName, KeyType keyType) {
        writeToUiAppend(output, keyType.toString() + " authentication with key " + String.format("0x%02X", keyNumber) + "(= " + keyName + "access key)");
        try {
            boolean authApp = desfire.authenticate(key, keyNumber, keyType);
            if (!authApp) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                return false;
            } else {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication SUCCESS", COLOR_GREEN);
                return true;
            }
        } catch (IOException e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
            writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
            //writeToUiAppend(output, "IOException: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
            writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            return false;
        }
    }

    /**
     * section for NFC handling
     */

    // This method is run in another thread when a card is discovered
    // !!!! This method cannot cannot direct interact with the UI Thread
    // Use `runOnUiThread` method to change the UI from this method
    @Override
    public void onTagDiscovered(Tag tag) {

        writeToUiAppend(output, "NFC tag discovered");
        isoDep = null;
        try {
            isoDep = IsoDep.get(tag);
            if (isoDep != null) {

                // Make a Sound
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
                } else {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(200);
                }

                runOnUiThread(() -> {
                    output.setText("");
                    errorCode.setText("");
                });
                isoDep.connect();
                // get tag ID
                tagIdByte = tag.getId();
                writeToUiAppend(output, "tag id: " + Utils.bytesToHex(tagIdByte));
                writeToUiAppend(output, "NFC tag connected");
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "the app is ready to work with", COLOR_GREEN);
                IsoDepWrapper isoDepWrapper = new DefaultIsoDepWrapper(isoDep);
                desFireAdapter = new DESFireAdapter(isoDepWrapper, true);
                desfire = new DESFireEV1();
                desfire.setAdapter(desFireAdapter);

                /**
                 * Depending on the change key this happens:
                 *
                 * If "change key type to AES" is checked:
                 * - Select Master Application
                 * - Authenticate with default DES key
                 * - on success change the key to default AES key
                 *
                 * If "change key type to DES" is checked:
                 * - Select Master Application
                 * - Authenticate with default AES key
                 * - on success change the key to default DES key
                 */

                runChangeMasterKeys();
            }
        } catch (IOException e) {
            writeToUiAppend(output, "ERROR: IOException " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {

            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(this);
    }

    /**
     * section for UI handling
     */

    private void writeToUiAppend(TextView textView, String message) {
        runOnUiThread(() -> {
            String oldString = textView.getText().toString();
            if (TextUtils.isEmpty(oldString)) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + oldString;
                textView.setText(newString);
                System.out.println(message);
            }
        });
    }

    private void writeToUiAppendBorderColor(TextView textView, TextInputLayout textInputLayout, String message, int color) {
        runOnUiThread(() -> {

            // set the color to green
            //Color from rgb
            // int color = Color.rgb(255,0,0); // red
            //int color = Color.rgb(0,255,0); // green
            //Color from hex string
            //int color2 = Color.parseColor("#FF11AA"); light blue
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_focused}, // focused
                    new int[]{android.R.attr.state_hovered}, // hovered
                    new int[]{android.R.attr.state_enabled}, // enabled
                    new int[]{}  //
            };
            int[] colors = new int[]{
                    color,
                    color,
                    color,
                    //color2
                    color
            };
            ColorStateList myColorList = new ColorStateList(states, colors);
            textInputLayout.setBoxStrokeColorStateList(myColorList);

            String oldString = textView.getText().toString();
            if (TextUtils.isEmpty(oldString)) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + oldString;
                textView.setText(newString);
                System.out.println(message);
            }
        });
    }

    /**
     * section for options menu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mLicenseInformation = menu.findItem(R.id.action_licenseInformation);
        mLicenseInformation.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                displayLicensesAlertDialog();
                return false;
            }
        });
        MenuItem mAbout = menu.findItem(R.id.action_about);
        mAbout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(i);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    // run: displayLicensesAlertDialog();
    // display licenses dialog see: https://bignerdranch.com/blog/open-source-licenses-and-android/
    private void displayLicensesAlertDialog() {
        WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");
        android.app.AlertDialog mAlertDialog = new android.app.AlertDialog.Builder(MainActivity.this).create();
        mAlertDialog = new android.app.AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Libraries used and their licenses")
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}