package de.androidcrypto.mifaredesfireev3examplesnfcjlib;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.skjolber.desfire.ev1.model.DesfireApplicationId;
import com.github.skjolber.desfire.ev1.model.DesfireApplicationKeySettings;
import com.github.skjolber.desfire.ev1.model.VersionInfo;
import com.github.skjolber.desfire.ev1.model.command.DefaultIsoDepWrapper;
import com.github.skjolber.desfire.ev1.model.command.IsoDepWrapper;
import com.github.skjolber.desfire.ev1.model.file.DesfireFile;
import com.github.skjolber.desfire.ev1.model.file.DesfireFileCommunicationSettings;
import com.github.skjolber.desfire.ev1.model.file.RecordDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.StandardDesfireFile;
import com.github.skjolber.desfire.ev1.model.file.ValueDesfireFile;
import com.github.skjolber.desfire.ev1.model.key.DesfireKeyType;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nfcjlib.core.DESFireAdapter;
import nfcjlib.core.DESFireEV1;
import nfcjlib.core.KeyType;
import nfcjlib.core.util.CRC16;
import nfcjlib.core.util.TripleDES;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private com.google.android.material.textfield.TextInputEditText output, errorCode;
    private com.google.android.material.textfield.TextInputLayout errorCodeLayout;

    /**
     * section for temporary actions
     */

    private Button setupCompleteApplication, standardWriteRead, standardWriteReadDefaultKeys;
    private Button getFileSettingsDesfire, changeFileSettingsDesfire;

    /**
     * section for general workflow
     */

    private LinearLayout llGeneralWorkflow;
    private Button tagVersion, keySettings, freeMemory, formatPicc, selectMasterApplication;

    /**
     * section for application handling
     */
    private LinearLayout llApplicationHandling;
    private Button applicationList, applicationCreate, applicationCreateAes, applicationSelect, applicationDelete;
    private com.google.android.material.textfield.TextInputEditText numberOfKeys, applicationId, applicationSelected;
    private RadioButton rbApplicationKeyTypeDes, rbApplicationKeyTypeAes;
    private byte[] selectedApplicationId = null;

    /**
     * section for files handling
     */

    private LinearLayout llFiles;

    private Button fileList, fileSelect, fileDelete;
    private com.google.android.material.textfield.TextInputEditText fileSelected;
    private String selectedFileId = "";
    private int selectedFileIdInt = -1;
    private int selectedFileSize;

    /**
     * section for standard & backup file handling
     */

    private LinearLayout llStandardFile;
    private Button fileStandardCreate, fileStandardWrite, fileStandardRead;
    private com.google.android.material.textfield.TextInputEditText fileSize, fileData;
    private RadioButton rbStandardFile, rbBackupFile;
    private com.shawnlin.numberpicker.NumberPicker npStandardFileId;
    RadioButton rbFileStandardPlainCommunication, rbFileStandardMacedCommunication, rbFileStandardEncryptedCommunication;
    private final int MAXIMUM_STANDARD_DATA_CHUNK = 40; // if any data are longer we create chunks when writing

    //private FileSettings selectedFileSettings;


    /**
     * section for value file handling
     */

    private LinearLayout llValueFile;
    private Button fileValueCreate, fileValueCredit, fileValueDebit, fileValueRead;
    RadioButton rbFileValuePlainCommunication, rbFileValueMacedCommunication, rbFileValueEncryptedCommunication;
    private com.shawnlin.numberpicker.NumberPicker npValueFileId;
    private com.google.android.material.textfield.TextInputEditText lowerLimitValue, upperLimitValue, initialValueValue, creditDebitValue;

    /**
     * section for record file handling
     */

    private LinearLayout llRecordFile;
    private Button fileRecordCreate, fileRecordWrite, fileRecordWriteTimestamp, fileRecordRead;
    private RadioButton rbLinearRecordFile, rbCyclicRecordFile;
    RadioButton rbFileRecordPlainCommunication, rbFileRecordMacedCommunication, rbFileRecordEncryptedCommunication;
    private com.shawnlin.numberpicker.NumberPicker npRecordFileId;
    private com.google.android.material.textfield.TextInputEditText fileRecordSize, fileRecordData, fileRecordNumberOfRecords;

    /**
     * work with encrypted standard files - EXPERIMENTAL
     */

    /*
    private LinearLayout llStandardFileEnc;
    private Button fileStandardCreateEnc, fileStandardWriteEnc, manualEncryption;

     */

    /**
     * work with transaction mac files - only available on EV2+
     */
    /*

    private LinearLayout llTMacFile;
    private Button fileTMacCreate, fileTMacWrite, fileTMacRead;


     */


    /**
     * section for authentication
     */

    private Button authDM0D, authD0D, authD1D, authD2D, authD3D, authD4D; // auth with default DES keys
    private Button authDM0A, authD0A, authD1A, authD2A, authD3A, authD4A; // auth with default AES keys
    private Button authDM0DC, authD0DC, authD1DC, authD2DC, authD3DC, authD4DC; // auth with changed DES keys
    private Button authDM0AC, authD0AC, authD1AC, authD2AC, authD3AC, authD4AC; // auth with changed AES keys

    /*
    // the old menu:
    private Button authKeyDM0, authKeyD0, authKeyD1, authKeyD2, authKeyD3, authKeyD4; // M0 is the Master Application Key

    // changed keys
    private Button authKeyDM0C, authKeyD0C, authKeyD1C, authKeyD2C, authKeyD3C, authKeyD4C; // M0 is the Master Application Key
*/

    /**
     * section for key handling
     */

    //private Button changeKeyDM0, changeKeyD0, changeKeyD1, changeKeyD2, changeKeyD3, changeKeyD4;

    // virtual card key handling
    //private Button authKeyAM0; // M0 is the Master Application Key AES
    //private Button changeKeyVc20, authKeyVc20, changeKeyVc21;

    // changed keys
    //private Button changeKeyDM0C, changeKeyD0C, changeKeyD1C, changeKeyD2C, changeKeyD3C, changeKeyD4C;

    // constants
    private final byte[] MASTER_APPLICATION_IDENTIFIER = new byte[3]; // '00 00 00'
    private final byte[] MASTER_APPLICATION_KEY_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000");
    private final byte[] MASTER_APPLICATION_KEY_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] MASTER_APPLICATION_KEY_DES = Utils.hexStringToByteArray("DD00000000000000");
    private final byte[] MASTER_APPLICATION_KEY_AES = Utils.hexStringToByteArray("AA000000000000000000000000000000");
    private final byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
    private final byte[] APPLICATION_ID_DES = Utils.hexStringToByteArray("A1A2A3");
    private final byte[] APPLICATION_KEY_MASTER_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_MASTER_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000"); // default AES key with 16 nulls
    private final byte[] APPLICATION_KEY_MASTER_DES = Utils.hexStringToByteArray("D000000000000000");
    private final byte[] APPLICATION_KEY_MASTER_AES = Utils.hexStringToByteArray("A0000000000000000000000000000000");
    private final byte APPLICATION_KEY_MASTER_NUMBER = (byte) 0x00;
    private final byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks
    private final byte KEY_NUMBER_RW = (byte) 0x01;
    private final byte[] APPLICATION_KEY_RW_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_RW_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_RW_DES = Utils.hexStringToByteArray("D100000000000000");
    private final byte[] APPLICATION_KEY_RW_AES = Utils.hexStringToByteArray("A1000000000000000000000000000000");
    private final byte APPLICATION_KEY_RW_NUMBER = (byte) 0x01;
    private final byte[] APPLICATION_KEY_CAR_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_CAR_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_CAR_DES = Utils.hexStringToByteArray("D200000000000000");
    private final byte[] APPLICATION_KEY_CAR_AES = Utils.hexStringToByteArray("A2000000000000000000000000000000");
    private final byte APPLICATION_KEY_CAR_NUMBER = (byte) 0x02;

    private final byte[] APPLICATION_KEY_R_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_R_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_R_DES = Utils.hexStringToByteArray("D300000000000000");
    private final byte[] APPLICATION_KEY_R_AES = Utils.hexStringToByteArray("A3000000000000000000000000000000");
    private final byte APPLICATION_KEY_R_NUMBER = (byte) 0x03;

    private final byte[] APPLICATION_KEY_W_DES_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
    private final byte[] APPLICATION_KEY_W_AES_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] APPLICATION_KEY_W_DES = Utils.hexStringToByteArray("D400000000000000");
    private final byte[] APPLICATION_KEY_W_AES = Utils.hexStringToByteArray("A4000000000000000000000000000000");
    private final byte APPLICATION_KEY_W_NUMBER = (byte) 0x04;

    private final byte[] VIRTUAL_CARD_KEY_CONFIG_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] VIRTUAL_CARD_KEY_CONFIG = Utils.hexStringToByteArray("20200000000000000000000000000000");
    private final byte VIRTUAL_CARD_KEY_CONFIG_NUMBER = (byte) 0x20;
    private final byte[] VIRTUAL_CARD_KEY_PROXIMITY_DEFAULT = Utils.hexStringToByteArray("00000000000000000000000000000000");
    private final byte[] VIRTUAL_CARD_KEY_PROXIMITY = Utils.hexStringToByteArray("20200000000000000000000000000000");
    private final byte VIRTUAL_CARD_KEY_PROXIMITY_NUMBER = (byte) 0x21;

    private final byte STANDARD_FILE_NUMBER = (byte) 0x01;


    int COLOR_GREEN = Color.rgb(0, 255, 0);
    int COLOR_RED = Color.rgb(255, 0, 0);

    // variables for NFC handling

    private NfcAdapter mNfcAdapter;
    private IsoDep isoDep;
    private byte[] tagIdByte;
    DESFireEV1 desfire;
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

        // temporary workflow
        setupCompleteApplication = findViewById(R.id.btnSetupCompleteApplication);
        standardWriteRead = findViewById(R.id.btnStandardFileWriteRead);
        //standardWriteReadDefaultKeys = findViewById(R.id.btnStandardFileWriteReadDefaultKeys);



        // general workflow
        tagVersion = findViewById(R.id.btnGetTagVersion);
        keySettings = findViewById(R.id.btnGetKeySettings);
        freeMemory = findViewById(R.id.btnGetFreeMemory);
        formatPicc = findViewById(R.id.btnFormatPicc);
        selectMasterApplication = findViewById(R.id.btnSelectMasterApplication);

        // application handling
        llApplicationHandling = findViewById(R.id.llApplications);
        applicationList = findViewById(R.id.btnListApplications);
        applicationCreate = findViewById(R.id.btnCreateApplication);
        applicationCreateAes = findViewById(R.id.btnCreateApplicationAes);
        applicationSelect = findViewById(R.id.btnSelectApplication);
        applicationDelete = findViewById(R.id.btnDeleteApplication);
        applicationSelected = findViewById(R.id.etSelectedApplicationId);
        numberOfKeys = findViewById(R.id.etNumberOfKeys);
        applicationId = findViewById(R.id.etApplicationId);
        rbApplicationKeyTypeDes = findViewById(R.id.rbApplicationKeyTypeDes);
        rbApplicationKeyTypeAes = findViewById(R.id.rbApplicationKeyTypeAes);


        // files handling
        fileList = findViewById(R.id.btnListFiles);
        fileSelect = findViewById(R.id.btnSelectFile);
        fileDelete = findViewById(R.id.btnDeleteFile);
        getFileSettingsDesfire = findViewById(R.id.btnGetFileSettings);
        changeFileSettingsDesfire = findViewById(R.id.btnChangeFileSettings);

        // standard & backup file handling
        llStandardFile = findViewById(R.id.llStandardFile);
        fileStandardCreate = findViewById(R.id.btnCreateStandardFile);
        fileStandardWrite = findViewById(R.id.btnWriteStandardFile);
        fileStandardRead = findViewById(R.id.btnReadStandardFile);
        npStandardFileId = findViewById(R.id.npStandardFileId);
        rbStandardFile = findViewById(R.id.rbStandardFile);
        rbBackupFile = findViewById(R.id.rbBackupFile);
        rbFileStandardPlainCommunication = findViewById(R.id.rbFileStandardPlainCommunication);
        rbFileStandardMacedCommunication = findViewById(R.id.rbFileStandardMacedCommunication);
        rbFileStandardEncryptedCommunication = findViewById(R.id.rbFileStandardEncryptedCommunication);
        fileSize = findViewById(R.id.etFileStandardSize);
        fileData = findViewById(R.id.etFileStandardData);
        fileSelected = findViewById(R.id.etSelectedFileId);

        // value file handling
        llValueFile = findViewById(R.id.llValueFile);
        fileValueCreate = findViewById(R.id.btnCreateValueFile);
        fileValueRead = findViewById(R.id.btnReadValueFile);
        fileValueCredit = findViewById(R.id.btnCreditValueFile);
        fileValueDebit = findViewById(R.id.btnDebitValueFile);
        npValueFileId = findViewById(R.id.npValueFileId);
        rbFileValuePlainCommunication = findViewById(R.id.rbFileValuePlainCommunication);
        rbFileValueMacedCommunication = findViewById(R.id.rbFileValueMacedCommunication);
        rbFileValueEncryptedCommunication = findViewById(R.id.rbFileValueEncryptedCommunication);
        lowerLimitValue = findViewById(R.id.etValueLowerLimit);
        upperLimitValue = findViewById(R.id.etValueUpperLimit);
        initialValueValue = findViewById(R.id.etValueInitialValue);
        creditDebitValue = findViewById(R.id.etValueCreditDebitValue);

        // record file handling
        llRecordFile = findViewById(R.id.llRecordFile);
        fileRecordCreate = findViewById(R.id.btnCreateRecordFile);
        fileRecordRead = findViewById(R.id.btnReadRecordFile);
        fileRecordWrite = findViewById(R.id.btnWriteRecordFile);
        fileRecordWriteTimestamp = findViewById(R.id.btnWriteRecordFileTimestamp);
        npRecordFileId = findViewById(R.id.npRecordFileId);
        rbFileRecordPlainCommunication = findViewById(R.id.rbFileRecordPlainCommunication);
        rbFileRecordMacedCommunication = findViewById(R.id.rbFileRecordMacedCommunication);
        rbFileRecordEncryptedCommunication = findViewById(R.id.rbFileRecordEncryptedCommunication);
        fileRecordSize = findViewById(R.id.etRecordFileSize);
        fileRecordNumberOfRecords = findViewById(R.id.etRecordFileNumberRecords);
        fileRecordData = findViewById(R.id.etRecordFileData);
        rbLinearRecordFile = findViewById(R.id.rbLinearRecordFile);
        rbCyclicRecordFile = findViewById(R.id.rbCyclicRecordFile);
/*
        // transaction mac file handling
        llTMacFile = findViewById(R.id.llTransactionMacFile);
        fileTMacCreate = findViewById(R.id.btnCreateTransactionMacFile);
        fileTMacWrite = findViewById(R.id.btnWriteTransactionMacFile);
        fileTMacRead = findViewById(R.id.btnReadTransactionMacFile);

        // encrypted standard file handling
        llStandardFileEnc = findViewById(R.id.llStandardFileEnc);
        fileStandardCreateEnc = findViewById(R.id.btnCreateStandardFileEnc);
        fileStandardWriteEnc = findViewById(R.id.btnWriteStandardFileEnc);
        manualEncryption = findViewById(R.id.btnManualEnc);
*/
        // authentication handling DES default keys
        authDM0D = findViewById(R.id.btnAuthDM0D);
        authD0D = findViewById(R.id.btnAuthD0D);
        authD1D = findViewById(R.id.btnAuthD1D);
        authD2D = findViewById(R.id.btnAuthD2D);
        authD3D = findViewById(R.id.btnAuthD3D);
        authD4D = findViewById(R.id.btnAuthD4D);

        // authentication handling AES default keys
        authDM0A = findViewById(R.id.btnAuthDM0A);
        authD0A = findViewById(R.id.btnAuthD0A);
        authD1A = findViewById(R.id.btnAuthD1A);
        authD2A = findViewById(R.id.btnAuthD2A);
        authD3A = findViewById(R.id.btnAuthD3A);
        authD4A = findViewById(R.id.btnAuthD4A);

        // authentication handling DES changed keys
        authDM0DC = findViewById(R.id.btnAuthDM0DC);
        authD0DC = findViewById(R.id.btnAuthD0DC);
        authD1DC = findViewById(R.id.btnAuthD1DC);
        authD2DC = findViewById(R.id.btnAuthD2DC);
        authD3DC = findViewById(R.id.btnAuthD3DC);
        authD4DC = findViewById(R.id.btnAuthD4DC);

        // authentication handling AES changed keys
        authDM0AC = findViewById(R.id.btnAuthDM0AC);
        authD0AC = findViewById(R.id.btnAuthD0AC);
        authD1AC = findViewById(R.id.btnAuthD1AC);
        authD2AC = findViewById(R.id.btnAuthD2AC);
        authD3AC = findViewById(R.id.btnAuthD3AC);
        authD4AC = findViewById(R.id.btnAuthD4AC);

        // authentication handling old menu
/*
        authKeyDM0 = findViewById(R.id.btnAuthDM0);
        authKeyD0 = findViewById(R.id.btnAuthD0);
        authKeyD1 = findViewById(R.id.btnAuthD1);
        authKeyD2 = findViewById(R.id.btnAuthD2);
        authKeyD3 = findViewById(R.id.btnAuthD3);
        authKeyD4 = findViewById(R.id.btnAuthD4);
        // now with changed keys
        authKeyDM0C = findViewById(R.id.btnAuthDM0C);
        authKeyD0C = findViewById(R.id.btnAuthD0C);
        authKeyD1C = findViewById(R.id.btnAuthD1C);
        authKeyD2C = findViewById(R.id.btnAuthD2C);
        authKeyD3C = findViewById(R.id.btnAuthD3C);
        authKeyD4C = findViewById(R.id.btnAuthD4C);

        // key handling
        changeKeyDM0 = findViewById(R.id.btnChangeKeyDM0);
        changeKeyD0 = findViewById(R.id.btnChangeKeyD0);
        changeKeyD1 = findViewById(R.id.btnChangeKeyD1);
        changeKeyD2 = findViewById(R.id.btnChangeKeyD2);
        changeKeyD3 = findViewById(R.id.btnChangeKeyD3);
        changeKeyD4 = findViewById(R.id.btnChangeKeyD4);

        // virtual card key handling
        changeKeyVc20 = findViewById(R.id.btnChangeKeyA20);
        authKeyAM0 = findViewById(R.id.btnAuthAM0);
        authKeyVc20 = findViewById(R.id.btnAuthA20);
        changeKeyVc21 = findViewById(R.id.btnChangeKeyA21);

        // now with changed keys
        changeKeyDM0C = findViewById(R.id.btnChangeKeyDM0C);
        changeKeyD0C = findViewById(R.id.btnChangeKeyD0C);
        changeKeyD1C = findViewById(R.id.btnChangeKeyD1C);
        changeKeyD2C = findViewById(R.id.btnChangeKeyD2C);
        changeKeyD3C = findViewById(R.id.btnChangeKeyD3C);
        changeKeyD4C = findViewById(R.id.btnChangeKeyD4C);
*/
        // todo clear TextView (e.g. selectedApplication/file) on some actions

        //allLayoutsInvisible(); // default

        // hide soft keyboard from showing up on startup
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        /**
         * section for temporary workflow
         */

        setupCompleteApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // setup a complete application: app with 5 DES keys, standard file 32 byte, write and read
                writeToUiAppend(output, "setup a complete application with a standard file");
                try {
                    byte[] AID_MASTER = Utils.hexStringToByteArray("000000");

                    // select master application
                    boolean dfSelectM = desfire.selectApplication(AID_MASTER);
                    writeToUiAppend(output, "dfSelectMResult: " + dfSelectM);

                    // authenticate with MasterApplicationKey
                    byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
                    boolean dfAuthM = desfire.authenticate(MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthMReadResult: " + dfAuthM);

                    byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks, see M075031_desfire.pdf pages 33 ff
                    byte NUMBER_OF_KEYS = (byte) 0x05; // key numbers 0..4

                    byte[] aid = APPLICATION_ID_DES.clone();
                    Utils.reverseByteArrayInPlace(aid);

                    //boolean dfCreateApplication = desfire.createApplication(AID_DES, APPLICATION_MASTER_KEY_SETTINGS, KeyType.DES, NUMBER_OF_KEYS);
                    boolean dfCreateApplication = desfire.createApplication(aid, APPLICATION_MASTER_KEY_SETTINGS, KeyType.DES, NUMBER_OF_KEYS);
                    writeToUiAppend(output, "dfCreateApplicationResult: " + dfCreateApplication);

                    //boolean dfSelectApplication = desfire.selectApplication(AID_DES);
                    boolean dfSelectApplication = desfire.selectApplication(aid);
                    writeToUiAppend(output, "dfSelectApplicationResult: " + dfSelectApplication);

                    // as of the key settings we do not need an authentication to create a file ?

                    byte APPLICATION_COMMUNICATION_SETTINGS = (byte) 0x00; // plain access (no MAC nor Encryption)
                    byte APPLICATION_ACCESS_RIGHTS_RW_CAR = (byte) 0x12; // Read&Write Access & ChangeAccessRights
                    byte APPLICATION_ACCESS_RIGHTS_R_W = (byte) 0x34;    // Read Access & Write Access // read with key 3, write with key 4
                    byte[] STANDARD_FILE_SIZE = new byte[]{(byte) 0x20, (byte) 0x00, (byte) 0x00}; // 32 bytes, LSB

                    byte[] payloadStandardFile = new byte[7];
                    payloadStandardFile[0] = STANDARD_FILE_NUMBER; // fileNumber
                    payloadStandardFile[1] = APPLICATION_COMMUNICATION_SETTINGS;
                    payloadStandardFile[2] = APPLICATION_ACCESS_RIGHTS_RW_CAR;
                    payloadStandardFile[3] = APPLICATION_ACCESS_RIGHTS_R_W;
                    System.arraycopy(STANDARD_FILE_SIZE, 0, payloadStandardFile, 4, 3);
                    writeToUiAppend(output, printData("payloadStandardFile", payloadStandardFile));
                    boolean dfCreateStandardFile = desfire.createStdDataFile(payloadStandardFile);
                    writeToUiAppend(output, "dfCreateStandardFileResult: " + dfCreateStandardFile);
                    writeToUiAppend(output, "dfCreateStandardFileResultCode: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                    writeToUiAppend(output, "finished");
                    writeToUiAppend(output, "");

                } catch (IOException e) {
                    writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
                } catch (Exception e) {
                    writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
                }

            }
        });

        standardWriteRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a standard file and read from a standard file
                writeToUiAppend(output, "write to a standard file and read from a standard file");
                try {

                    // select master application
                    boolean dfSelectM = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
                    writeToUiAppend(output, "dfSelectMResult: " + dfSelectM);
/*
                    // authenticate with MasterApplicationKey
                    //byte[] MASTER_APPLICATION_KEY = new byte[8];
                    //byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
                    boolean dfAuthM = desfire.authenticate(MASTER_APPLICATION_KEY, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthMReadResult: " + dfAuthM);

                    //byte[] AID_DES = Utils.hexStringToByteArray("B3B2B1");
                    //byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks, see M075031_desfire.pdf pages 33 ff
                    //byte NUMBER_OF_KEYS = (byte) 0x05; // key numbers 0..4
*/
                    //boolean dfCreateApplication = desfire.createApplication(AID_DES, APPLICATION_MASTER_KEY_SETTINGS, KeyType.DES, NUMBER_OF_KEYS);
                    //writeToUiAppend(output, "dfCreateApplicationResult: " + dfCreateApplication);
                    byte[] aid = APPLICATION_ID_DES.clone();
                    Utils.reverseByteArrayInPlace(aid);
                    boolean dfSelectApplication = desfire.selectApplication(aid);
                    writeToUiAppend(output, "dfSelectApplicationResult: " + dfSelectApplication);

                    // we do need an authentication to write to a file
                    //byte[] APPLICATION_KEY_W_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
                    //byte APPLICATION_KEY_W_NUMBER = (byte) 0x04;
                    // authenticate with ApplicationWriteKey
                    boolean dfAuthApp = desfire.authenticate(APPLICATION_KEY_W_DES_DEFAULT, APPLICATION_KEY_W_NUMBER, KeyType.DES);
                    //boolean dfAuthApp = desfire.authenticate(APPLICATION_KEY_W, APPLICATION_KEY_W_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthApp);

                    // get a random payload with 32 bytes
                    UUID uuid = UUID.randomUUID(); // this is 36 characters long
                    byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long

                    byte[] offset = new byte[]{(byte) 0x00, (byte) 0xf00, (byte) 0x00}; // write at the beginning
                    byte lengthOfData = (byte) (dataToWrite.length & 0xFF);
                    byte[] payloadWriteData = new byte[7 + dataToWrite.length]; // 7 + length of data
                    payloadWriteData[0] = STANDARD_FILE_NUMBER; // fileNumber
                    //payloadWriteData[0] = (byte) 0x00; // fileNumber // todo change
                    System.arraycopy(offset, 0, payloadWriteData, 1, 3);
                    payloadWriteData[4] = lengthOfData; // lsb
                    //payloadStandardFile[5] = 0; // is 0x00 // lsb
                    //payloadStandardFile[6] = 0; // is 0x00 // lsb
                    System.arraycopy(dataToWrite, 0, payloadWriteData, 7, dataToWrite.length);
                    writeToUiAppend(output, printData("payloadWriteData", payloadWriteData));
                    boolean dfWriteStandard = desfire.writeData(payloadWriteData);

                    writeToUiAppend(output, "dfWriteStandardResult: " + dfWriteStandard);
                    writeToUiAppend(output, "dfWriteStandardResultCode: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());

                    writeToUiAppend(output, "");
                    writeToUiAppend(output, "now we are reading the content of the file");

                    // select master application
                    boolean dfSelectMR = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
                    writeToUiAppend(output, "dfSelectMResult: " + dfSelectMR);

                    // authenticate with MasterApplicationKey
                    //byte[] MASTER_APPLICATION_KEY = new byte[8];
                    //byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
                    boolean dfAuthMR = desfire.authenticate(MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthMReadResult: " + dfAuthMR);

                    //byte[] AID_DES = Utils.hexStringToByteArray("B3B2B1");
                    //byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks, see M075031_desfire.pdf pages 33 ff
                    //byte NUMBER_OF_KEYS = (byte) 0x05; // key numbers 0..4

                    //boolean dfCreateApplication = desfire.createApplication(AID_DES, APPLICATION_MASTER_KEY_SETTINGS, KeyType.DES, NUMBER_OF_KEYS);
                    //writeToUiAppend(output, "dfCreateApplicationResult: " + dfCreateApplication);

                    dfSelectApplication = desfire.selectApplication(aid);
                    writeToUiAppend(output, "dfSelectApplicationResult: " + dfSelectApplication);

                    // we do need an authentication to read from a file
                    byte[] APPLICATION_KEY_R_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
                    //byte APPLICATION_KEY_R_NUMBER = (byte) 0x03;
                    // authenticate with ApplicationReadKey
                    boolean dfAuthAppRead = desfire.authenticate(APPLICATION_KEY_R_DEFAULT, APPLICATION_KEY_R_NUMBER, KeyType.DES);
                    //boolean dfAuthAppRead = desfire.authenticate(APPLICATION_KEY_R, APPLICATION_KEY_R_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthAppRead);


                    //
                    // todo get the maximal length from getFileSettings
                    DesfireFile fileSettings = desfire.getFileSettings(STANDARD_FILE_NUMBER);
                    //DesfireFile fileSettings = desfire.getFileSettings((byte) 0x00);
                    // todo check that it is a standard file !
                    StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                    int fileSize = standardDesfireFile.getFileSize();
                    writeToUiAppend(output, "fileSize: " + fileSize);

                    byte[] readStandard = desfire.readData(STANDARD_FILE_NUMBER, 0, fileSize);
                    //byte[] readStandard = desfire.readData((byte) 0x00, 0, fileSize);
                    writeToUiAppend(output, printData("readStandard", readStandard));
                    if (readStandard != null) {
                        writeToUiAppend(output, new String(readStandard, StandardCharsets.UTF_8));
                    }

                    writeToUiAppend(output, "finished");
                    writeToUiAppend(output, "");

                } catch (IOException e) {
                    writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

/*
        standardWriteReadDefaultKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a standard file and read from a standard file
                writeToUiAppend(output, "write to a standard file and read from a standard file using default keys");
                try {

                    // select master application
                    boolean dfSelectM = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
                    writeToUiAppend(output, "dfSelectMResult: " + dfSelectM);

                    // authenticate with MasterApplicationKey
                    //byte[] MASTER_APPLICATION_KEY = new byte[8];
                    //byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
                    boolean dfAuthM = desfire.authenticate(MASTER_APPLICATION_KEY, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthMReadResult: " + dfAuthM);

                    //byte[] AID_DES = Utils.hexStringToByteArray("B3B2B1");
                    //byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks, see M075031_desfire.pdf pages 33 ff
                    //byte NUMBER_OF_KEYS = (byte) 0x05; // key numbers 0..4

                    //boolean dfCreateApplication = desfire.createApplication(AID_DES, APPLICATION_MASTER_KEY_SETTINGS, KeyType.DES, NUMBER_OF_KEYS);
                    //writeToUiAppend(output, "dfCreateApplicationResult: " + dfCreateApplication);

                    boolean dfSelectApplication = desfire.selectApplication(AID_DES);
                    writeToUiAppend(output, "dfSelectApplicationResult: " + dfSelectApplication);

                    // we do need an authentication to write to a file
                    //byte[] APPLICATION_KEY_W_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
                    //byte APPLICATION_KEY_W_NUMBER = (byte) 0x04;
                    // authenticate with ApplicationWriteKey
                    boolean dfAuthApp = desfire.authenticate(APPLICATION_KEY_W_DEFAULT, APPLICATION_KEY_W_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthApp);

                    // get a random payload with 32 bytes
                    UUID uuid = UUID.randomUUID(); // this is 36 characters long
                    byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long

                    byte[] offset = new byte[]{(byte) 0x00, (byte) 0xf00, (byte) 0x00}; // write at the beginning
                    byte lengthOfData = (byte) (dataToWrite.length & 0xFF);
                    byte[] payloadWriteData = new byte[7 + dataToWrite.length]; // 7 + length of data
                    payloadWriteData[0] = STANDARD_FILE_NUMBER; // fileNumber
                    System.arraycopy(offset, 0, payloadWriteData, 1, 3);
                    payloadWriteData[4] = lengthOfData; // lsb
                    //payloadStandardFile[5] = 0; // is 0x00 // lsb
                    //payloadStandardFile[6] = 0; // is 0x00 // lsb
                    System.arraycopy(dataToWrite, 0, payloadWriteData, 7, dataToWrite.length);
                    writeToUiAppend(output, printData("payloadWriteData", payloadWriteData));
                    boolean dfWriteStandard = desfire.writeData(payloadWriteData);

                    writeToUiAppend(output, "dfWriteStandardResult: " + dfWriteStandard);
                    writeToUiAppend(output, "dfWriteStandardResultCode: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());

                    writeToUiAppend(output, "");
                    writeToUiAppend(output, "now we are reading the content of the file");

                    // select master application
                    dfSelectM = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
                    writeToUiAppend(output, "dfSelectMResult: " + dfSelectM);

                    // authenticate with MasterApplicationKey
                    //byte[] MASTER_APPLICATION_KEY = new byte[8];
                    //byte MASTER_APPLICATION_KEY_NUMBER = (byte) 0x00;
                    dfAuthM = desfire.authenticate(MASTER_APPLICATION_KEY, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthMReadResult: " + dfAuthM);

                    //byte[] AID_DES = Utils.hexStringToByteArray("B3B2B1");
                    //byte APPLICATION_MASTER_KEY_SETTINGS = (byte) 0x0f; // amks, see M075031_desfire.pdf pages 33 ff
                    //byte NUMBER_OF_KEYS = (byte) 0x05; // key numbers 0..4

                    //boolean dfCreateApplication = desfire.createApplication(AID_DES, APPLICATION_MASTER_KEY_SETTINGS, KeyType.DES, NUMBER_OF_KEYS);
                    //writeToUiAppend(output, "dfCreateApplicationResult: " + dfCreateApplication);

                    dfSelectApplication = desfire.selectApplication(AID_DES);
                    writeToUiAppend(output, "dfSelectApplicationResult: " + dfSelectApplication);

                    // we do need an authentication to read from a file
                    byte[] APPLICATION_KEY_R_DEFAULT = Utils.hexStringToByteArray("0000000000000000"); // default DES key with 8 nulls
                    byte APPLICATION_KEY_R_NUMBER = (byte) 0x03;
                    // authenticate with ApplicationWReadKey
                    boolean dfAuthAppRead = desfire.authenticate(APPLICATION_KEY_R_DEFAULT, APPLICATION_KEY_R_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthAppRead);


                    // todo get the maximal length from getFileSettings
                    DesfireFile fileSettings = desfire.getFileSettings(STANDARD_FILE_NUMBER);
                    // todo check that it is a standard file !
                    StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                    int fileSize = standardDesfireFile.getFileSize();
                    writeToUiAppend(output, "fileSize: " + fileSize);

                    byte[] readStandard = desfire.readData(STANDARD_FILE_NUMBER, 0, fileSize);
                    writeToUiAppend(output, printData("readStandard", readStandard));
                    if (readStandard != null) {
                        writeToUiAppend(output, new String(readStandard, StandardCharsets.UTF_8));
                    }

                    writeToUiAppend(output, "finished");
                    writeToUiAppend(output, "");

                } catch (IOException e) {
                    writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
*/

        /**
         * section for general workflow
         */

        tagVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the tag version data
                clearOutputFields();
                VersionInfo versionInfo;
                try {
                    versionInfo = desfire.getVersion();
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    //writeToUiAppend(output, "Exception: " + e.getMessage());
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
                if (versionInfo == null) {
                    writeToUiAppend(output, "getVersionInfo is NULL");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getVersionInfo is NULL", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, "getVersionInfo: " + versionInfo.dump());
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "success in getting tagVersion", COLOR_GREEN);
                writeToUiAppend(errorCode, "getVersion: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                vibrateShort();
            }
        });

        keySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the key settings for the selected application
                clearOutputFields();
                writeToUiAppend(output, "get key settings for selected application: " + Utils.printData("AID", selectedApplicationId));
                writeToUiAppend(output, "if AID == NULL it is for the Master Application 00 00 00");
                DesfireApplicationKeySettings keySettings;
                try {
                    keySettings = desfire.getKeySettings();
                    vibrateShort();
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                if (keySettings == null) {
                    writeToUiAppend(output, "could not get the key settings (missing authentication ?)");
                    return;
                }
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {

                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                boolean success;

                                /*
                                try {

                                    success = desfire.formatPICC();
                                    writeToUiAppend(output, "formatPiccSuccess: " + success);
                                    if (!success) {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "formatPicc NOT Success, aborted", COLOR_RED);
                                        writeToUiAppend(errorCode, "formatPicc NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                                        writeToUiAppend(errorCode, "Did you forget to authenticate with the Master Key first ?");
                                        return;
                                    } else {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "formatPicc success", COLOR_GREEN);
                                        selectedFileId = "";
                                        fileSelected.setText("");
                                        selectedApplicationId = null;
                                        applicationSelected.setText("");
                                    }
                                } catch (IOException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                                    e.printStackTrace();
                                    return;
                                }

                                 */
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, "format of the PICC aborted");
                                break;
                        }
                    }
                };
                //final String selectedFolderString = "You are going to format the PICC " + "\n\n" + "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.application_key_settings, null);
                builder.setView(dialogView);

                CheckBox cbBit0MasterKeyIsChangeable = dialogView.findViewById(R.id.cbAksBit0MasterKeyIsChangeable);
                CheckBox cbBit1MasterKeyAuthenticationNeededDirListing = dialogView.findViewById(R.id.cbAksBit1MasterKeyAuthenticationNeededDirListing);
                CheckBox cbBit2MasterKeyAuthenticationNeededCreateDelete = dialogView.findViewById(R.id.cbAksBit2MasterKeyAuthenticationNeededCreateDelete);
                CheckBox cbBit3MasterKeySettingsChangeAllowed = dialogView.findViewById(R.id.cbAksBit3MasterKeySettingsChangeAllowed);
                EditText maximumNumberOfKeys = dialogView.findViewById(R.id.etAksMaximumNumberOfKeys);
                EditText keySettingsCarKey = dialogView.findViewById(R.id.etAksKeySettingsCarKey);
                // set data from key settings
                cbBit0MasterKeyIsChangeable.setChecked(keySettings.isCanChangeMasterKey());
                cbBit1MasterKeyAuthenticationNeededDirListing.setChecked(!keySettings.isFreeDirectoryAccess());
                cbBit2MasterKeyAuthenticationNeededCreateDelete.setChecked(!keySettings.isFreeCreateAndDelete());
                cbBit3MasterKeySettingsChangeAllowed.setChecked(keySettings.isConfigurationChangable());
                DesfireKeyType keyType = keySettings.getType();
                maximumNumberOfKeys.setText(keySettings.getMaxKeys() + " of type " + keyType.toString());
                // get the following data only if it is not the Master Application
                String accessTypeDescription = "";
                if (!Arrays.equals(selectedApplicationId, MASTER_APPLICATION_IDENTIFIER)) {
                    int carKeySettings = keySettings.getChangeKeyAccessRights();
                    // see DESFire D40 datasheet M075031_desfire.pdf, page 35
                    if (carKeySettings == 0) {
                        accessTypeDescription = "Application master key authentication is necessary to change any key (default)";
                    } else if ((carKeySettings > 0) && (carKeySettings < 14)) {
                        accessTypeDescription = "Authentication with the specified key is necessary to change any key: " + carKeySettings;
                    } else if (carKeySettings == 14) {
                        accessTypeDescription = "Authentication with the key to be changed (same KeyNo) is necessary to change a key";
                    } else if (carKeySettings == 15) {
                        accessTypeDescription = "All Keys (except application master key, see Bit0) within this application are frozen";
                    } else {
                        accessTypeDescription = "undefined car key settings";
                    }
                } else {
                    accessTypeDescription = "data not available in Master Application";
                }
                keySettingsCarKey.setText(accessTypeDescription);
                //keySettingsCarKey.setText(keySettings.getChangeKeyAccessRights());

                builder
                        //.setMessage(selectedFolderString)
                        .setPositiveButton(android.R.string.yes, dialogClickListener)
                        //.setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("Application key settings")
                        //.setView(R.layout.application_key_settings)
                        .show();

            }
        });

        freeMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the free memory on the PICC
                clearOutputFields();
                writeToUiAppend(output, "get the free memory on the PICC");
                byte[] freeMemoryOnPicc;
                try {
                    freeMemoryOnPicc = desfire.freeMemory();
                    Utils.reverseByteArrayInPlace(freeMemoryOnPicc); // LSB
                    writeToUiAppend(output, "The free memory is " + Utils.intFrom3ByteArray(freeMemoryOnPicc) + " bytes");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "success in getting the free memory", COLOR_GREEN);
                    vibrateShort();
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        });

        formatPicc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // format the PICC
                clearOutputFields();
                writeToUiAppend(output, "format the PICC");

                // open a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                boolean success;
                                try {
                                    success = desfire.formatPICC();
                                    writeToUiAppend(output, "formatPiccSuccess: " + success);
                                    if (!success) {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "formatPicc NOT Success, aborted", COLOR_RED);
                                        writeToUiAppend(errorCode, "formatPicc NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                                        writeToUiAppend(errorCode, "Did you forget to authenticate with the Master Key first ?");
                                        return;
                                    } else {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "formatPicc success", COLOR_GREEN);
                                        selectedFileId = "";
                                        fileSelected.setText("");
                                        selectedApplicationId = null;
                                        applicationSelected.setText("");
                                        vibrateShort();
                                    }
                                } catch (IOException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                                    e.printStackTrace();
                                    return;
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, "format of the PICC aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to format the PICC " + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("FORMAT the PICC")
                        .show();
        /*
        If you want to use the "yes" "no" literals of the user's language you can use this
        .setPositiveButton(android.R.string.yes, dialogClickListener)
        .setNegativeButton(android.R.string.no, dialogClickListener)
         */
            }
        });

        selectMasterApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "select the Master application";
                writeToUiAppend(output, logString);
                String[] applicationList;
                try {
                    // select PICC (is selected by default but...)
                    boolean success = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
                    writeToUiAppend(output, logString +  ": " + success);
                    if (!success) {
                        writeToUiAppend(output, logString + " NOT Success, aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                        return;
                    } else {
                        applicationSelected.setText("000000");
                        selectedApplicationId = new byte[3]; // 00 00 00
                        selectedFileId = "";
                        fileSelected.setText("");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + ": " + success, COLOR_GREEN);
                        vibrateShort();
                    }

                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        /**
         * section for applications
         */
        applicationList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get application ids
                clearOutputFields();
                byte[] responseData = new byte[2];
                List<byte[]> applicationIdList = getApplicationIdsList(output, responseData);
                String errorCodeString = Ev3.getErrorCode(responseData);
                if (errorCodeString.equals("00 success")) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getApplicationIdsList: " + errorCodeString, COLOR_GREEN);
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getApplicationIdsList: " + errorCodeString, COLOR_RED);
                }
                if (applicationIdList != null) {
                    for (int i = 0; i < applicationIdList.size(); i++) {
                        writeToUiAppend(output, "entry " + i + " app id : " + Utils.bytesToHex(applicationIdList.get(i)));
                    }
                    vibrateShort();
                } else {
                    //writeToUiAppend(errorCode, "getApplicationIdsList: returned NULL");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "getApplicationIdsList returned NULL", COLOR_RED);
                }
            }
        });

        applicationCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new application
                // get the input and sanity checks
                clearOutputFields();
                String logString = "create a new application";
                writeToUiAppend(output, logString);
                byte numberOfKeysByte = Byte.parseByte(numberOfKeys.getText().toString());
                byte[] applicationIdentifier = Utils.hexStringToByteArray(applicationId.getText().toString());
                if (applicationIdentifier == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong application ID", COLOR_RED);
                    return;
                }
                Utils.reverseByteArrayInPlace(applicationIdentifier); // change to LSB
                if (applicationIdentifier.length != 3) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you did not enter a 6 hex string application ID", COLOR_RED);
                    return;
                }
                KeyType keyType = KeyType.DES; // default
                if (rbApplicationKeyTypeAes.isChecked()) keyType = KeyType.AES;
                try {
                    boolean success = desfire.createApplication(applicationIdentifier, APPLICATION_MASTER_KEY_SETTINGS, keyType, numberOfKeysByte);
                    writeToUiAppend(output, logString + " Success: " + success);
                    if (!success) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, logString + " NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    } else {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                        vibrateShort();
                    }
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        applicationCreateAes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new application
                // get the input and sanity checks
                clearOutputFields();
                String logString = "create a new application (AES keys)";
                writeToUiAppend(output, logString);
                byte numberOfKeysByte = Byte.parseByte(numberOfKeys.getText().toString());
                byte[] applicationIdentifier = Utils.hexStringToByteArray(applicationId.getText().toString());
                if (applicationIdentifier == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong application ID", COLOR_RED);
                    return;
                }
                Utils.reverseByteArrayInPlace(applicationIdentifier); // change to LSB
                if (applicationIdentifier.length != 3) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you did not enter a 6 hex string application ID", COLOR_RED);
                    return;
                }
                try {
                    boolean success = desfire.createApplication(applicationIdentifier, APPLICATION_MASTER_KEY_SETTINGS, KeyType.AES, numberOfKeysByte);
                    writeToUiAppend(output, logString + ": " + success);
                    if (!success) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, logString + " NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    } else {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " success", COLOR_GREEN);
                        vibrateShort();
                    }
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        applicationSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get all applications and show them in a listview for selection
                clearOutputFields();
                String logString = "select an application";
                writeToUiAppend(output, logString);
                String[] applicationList;
                try {
                    // select PICC (is selected by default but...)
                    boolean success = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
                    writeToUiAppend(output, "selectMasterApplicationSuccess: " + success);
                    if (!success) {
                        writeToUiAppend(output, "selectMasterApplication NOT Success, aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "selectMasterApplication NOT Success, aborted", COLOR_RED);
                        return;
                    }
                    List<DesfireApplicationId> desfireApplicationIdList = desfire.getApplicationsIds();
                    applicationList = new String[desfireApplicationIdList.size()];
                    for (int i = 0; i < desfireApplicationIdList.size(); i++) {
                        applicationList[i] = desfireApplicationIdList.get(i).getIdString();
                    }
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose an application");

                // add a list
                //String[] animals = {"horse", "cow", "camel", "sheep", "goat"};
                //builder.setItems(animals, new DialogInterface.OnClickListener() {
                builder.setItems(applicationList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeToUiAppend(output, "you selected nr " + which + " = " + applicationList[which]);
                        boolean dfSelectApplication = false;
                        DesfireApplicationKeySettings desfireApplicationKeySettings;
                        try {
                            byte[] aid = Utils.hexStringToByteArray(applicationList[which]);
                            Utils.reverseByteArrayInPlace(aid);
                            dfSelectApplication = desfire.selectApplication(aid);
                            desfireApplicationKeySettings = desfire.getKeySettings();
                        } catch (IOException e) {
                            //throw new RuntimeException(e);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                            e.printStackTrace();
                            return;
                        }
                        // get the number and type of keys
                        int maxKeys = desfireApplicationKeySettings.getMaxKeys();
                        DesfireKeyType desfireKeyType = desfireApplicationKeySettings.getType();
                        String desfireKeyTypeString = desfireKeyType.toString();
                        // correct the output 'TDES' is in this context to 'DES'
                        if (desfireKeyTypeString.equals(DesfireKeyType.TDES.toString())) desfireKeyTypeString = "DES";
                        writeToUiAppend(output, logString + " Result: " + dfSelectApplication);
                        if (dfSelectApplication) {
                            selectedApplicationId = Utils.hexStringToByteArray(applicationList[which]);
                            applicationSelected.setText(applicationList[which] + " (max " + maxKeys + " keys of " + desfireKeyTypeString + " type)");
                            selectedFileId = "";
                            fileSelected.setText("");
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " Result: " + dfSelectApplication, COLOR_GREEN);
                            vibrateShort();
                        } else {
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_RED);
                        }
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        applicationDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                String logString = "delete a selected application";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                // open a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    byte[] aid = selectedApplicationId.clone();
                                    Utils.reverseByteArrayInPlace(aid);
                                    boolean success = desfire.deleteApplication(aid);
                                    writeToUiAppend(output, logString + " Success: " + success + " for applicationID: " + Utils.bytesToHexNpe(selectedApplicationId));
                                    if (!success) {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NOT Success, aborted", COLOR_RED);
                                        writeToUiAppend(errorCode, "Did you forget to authenticate with the Application Master Key first ?");
                                        writeToUiAppend(errorCode, logString + " NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                                        return;
                                    } else {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " success", COLOR_GREEN);
                                        applicationSelected.setText("");
                                        selectedApplicationId = null;
                                        selectedFileId = "";
                                        fileSelected.setText("");
                                        vibrateShort();
                                    }
                                } catch (IOException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                    return;
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, logString + " aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to delete the application " +
                        Utils.bytesToHexNpe(selectedApplicationId) + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("DELETE an application")
                        .show();
        /*
        If you want to use the "yes" "no" literals of the user's language you can use this
        .setPositiveButton(android.R.string.yes, dialogClickListener)
        .setNegativeButton(android.R.string.no, dialogClickListener)
         */
            }
        });


        /**
         * section for files
         */

        fileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // select a file in a selected application
                clearOutputFields();

                byte[] fileIds;
                try {
                    fileIds = desfire.getFileIds();
                    if (fileIds == null) {
                        writeToUiAppend(output, "The getFileIds returned NULL");
                        return;
                    }
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    return;
                    //throw new RuntimeException(e);
                }
                if (fileIds.length == 0) {
                    writeToUiAppend(output, "The getFileIds returned no files");
                    return;
                }
                List<Byte> fileIdList = new ArrayList<>();
                List<String> fileIdInformationList = new ArrayList<>();
                for (int i = 0; i < fileIds.length; i++) {
                    fileIdList.add(fileIds[i]);
                    fileIdInformationList.add(getFileInformationType((int) fileIds[i]));
                }
                //byte[] responseData = new byte[2];
                //List<Byte> fileIdList = fileIds.t getFileIdsList(output, responseData);
                //writeToUiAppend(errorCode, "getFileIdsList: " + Ev3.getErrorCode(responseData));

                for (int i = 0; i < fileIdList.size(); i++) {
                    writeToUiAppend(output, "entry " + i + " file id : " + fileIdList.get(i) + (" (") + Utils.byteToHex(fileIdList.get(i)) + ")"
                            + " " + fileIdInformationList.get(i));
                }

                String[] fileList = new String[fileIdList.size()];
                for (int i = 0; i < fileIdList.size(); i++) {
                    //fileList[i] = Utils.byteToHex(fileIdList.get(i));
                    fileList[i] = String.valueOf(fileIdList.get(i))
                            + " (" + fileIdInformationList.get(i) + ")";
                }

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose a file");

                builder.setItems(fileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeToUiAppend(output, "you  selected nr " + which + " = " + fileList[which]);
                        selectedFileId = String.valueOf(fileIdList.get(which));
                        selectedFileIdInt = Integer.parseInt(selectedFileId);
                        // now we run the command to select the application
                        byte[] responseData = new byte[2];
                        //boolean result = selectDes(output, selectedApplicationId, responseData);
                        //writeToUiAppend(output, "result of selectApplicationDes: " + result);
                        //writeToUiAppend(errorCode, "selectApplicationDes: " + Ev3.getErrorCode(responseData));

                        // here we are reading the fileSettings
                        DesfireFile desfireFile;
                        try {
                            desfireFile = desfire.forceFileSettingsUpdate(selectedFileIdInt);
                        } catch (Exception e) {
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                            return;
                        }
                        if (desfireFile == null) {
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "cant update the file communication settings, aborted", COLOR_RED);
                            return;
                        }
                        int readAccessKey = desfireFile.getReadAccessKey();
                        int writeAccessKey = desfireFile.getWriteAccessKey();
                        String csDescription = desfireFile.getCommunicationSettings().getDescription();
                        System.out.println("read: " + readAccessKey + " write " + writeAccessKey + " comm: " + csDescription);

                        /*
                        String outputString = fileList[which] + " ";
                        byte fileIdByte = Byte.parseByte(selectedFileId);
                        byte[] fileSettingsBytes = getFileSettings(output, fileIdByte, responseData);
                        if ((fileSettingsBytes != null) & (fileSettingsBytes.length >= 7)) {
                            selectedFileSettings = new FileSettings(fileIdByte, fileSettingsBytes);
                            outputString += "(" + selectedFileSettings.getFileTypeName();
                            selectedFileSize = selectedFileSettings.getFileSizeInt();
                            outputString += " size: " + selectedFileSize + ")";
                            writeToUiAppend(output, outputString);
                        }
                        */
                        fileSelected.setText(fileList[which]);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "file selected", COLOR_GREEN);
                        vibrateShort();
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        fileDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutputFields();
                writeToUiAppend(output, "delete a selected file");
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                // open a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                try {
                                    byte fileNo = Byte.parseByte(selectedFileId);
                                    boolean success = desfire.deleteFile(fileNo);
                                    writeToUiAppend(output, "deleteFileSuccess: " + success + " for fileID: " + selectedFileId);
                                    if (!success) {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "deleteFile NOT Success, aborted", COLOR_RED);
                                        writeToUiAppend(errorCode, "Did you forget to authenticate with the Application Master Key first ?");
                                        writeToUiAppend(errorCode, "deleteFile NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                                        return;
                                    } else {
                                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "deleteFile success", COLOR_GREEN);
                                        fileSelected.setText("");
                                        selectedFileId = null;
                                        vibrateShort();
                                    }
                                } catch (IOException e) {
                                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                                    e.printStackTrace();
                                    return;
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                writeToUiAppend(output, "delete a selected file aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to delete the file " +
                        selectedFileId + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("DELETE a file")
                        .show();
        /*
        If you want to use the "yes" "no" literals of the user's language you can use this
        .setPositiveButton(android.R.string.yes, dialogClickListener)
        .setNegativeButton(android.R.string.no, dialogClickListener)
         */
            }
        });

        /**
         * section  for standard files
         */

        /*
        authenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the default DES key
                clearOutputFields();
                try {
                    boolean success = desfire.authenticate(DES_DEFAULT_KEY, KEY_NUMBER_RW, KeyType.DES);
                    writeToUiAppend(output, "authenticateDesSuccess: " + success);
                    if (!success) {
                        writeToUiAppend(output, "authenticateDes NOT Success, aborted");
                        writeToUiAppend(output, "authenticateDes NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    }

                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppend(output, "Exception: " + e.getMessage());
                    writeToUiAppend(output, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });
        */

        /**
         * section for standard & backup files
         */

        fileStandardCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new standard file
                writeToUiAppend(output, "create a standard or backup file");
                // get the input and sanity checks
                clearOutputFields();
                byte fileIdByte = (byte) (npStandardFileId.getValue() & 0xFF);

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileSize.getText().toString());
                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                /*
                if (fileSizeInt != 32) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file size, 32 bytes allowed only", COLOR_RED);
                    return;
                }
                 */
                // new for communication setting choice
                PayloadBuilder.CommunicationSetting communicationSetting;
                if (rbFileStandardPlainCommunication.isChecked()) {
                    communicationSetting = PayloadBuilder.CommunicationSetting.Plain;
                } else if (rbFileStandardMacedCommunication.isChecked()) {
                    communicationSetting = PayloadBuilder.CommunicationSetting.MACed;
                } else {
                    communicationSetting = PayloadBuilder.CommunicationSetting.Encrypted;
                }
                boolean isStandardFile = rbStandardFile.isChecked(); // as there are 2 options only we just just check rbStandardFile
                try {
                    PayloadBuilder pb = new PayloadBuilder();
                    byte[] payloadStandardFile = pb.createStandardFile(fileIdByte, communicationSetting,
                            1, 2, 3, 4, fileSizeInt);
                    boolean success;
                    String createString;
                    if (isStandardFile) {
                        createString = "createStandardDataFile";
                        success = desfire.createStdDataFile(payloadStandardFile);

                    } else {
                        createString = "createBackupDataFile";
                        success = desfire.createBackupDataFile(payloadStandardFile);
                    }
                    writeToUiAppend(output, createString + "Success: " + success + " with FileID: " + Utils.byteToHex(fileIdByte) + " and size: " + fileSizeInt);
                    if (!success) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, createString + " NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, createString + " NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, createString + " Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });
/*
        // this version is doeing hardcoded chunking in chunks of 40 bytes of data to write before I corrected the Error in DESFireAdapter.java
        fileStandardWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected standard file in a selected application
                clearOutputFields();
                writeToUiAppend(output, "write to a standard file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    //writeToUiAppend(errorCode, "please enter some data to write");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);
                byte fileIdByte = Byte.parseByte(selectedFileId);

                // check that it is a standard file !
                DesfireFile fileSettings = null;
                try {
                    fileSettings = desfire.getFileSettings(fileIdInt);
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }
                //DesfireFile fileSettings = desfire.getFileSettings((byte) 0x00);
                // check that it is a standard file !
                String fileTypeName = fileSettings.getFileTypeName();
                writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                if (!fileTypeName.equals("Standard")) {
                    writeToUiAppend(output, "The selected file is not of type Standard but of type " + fileTypeName + ", aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }

                // get a random payload with 32 bytes
                UUID uuid = UUID.randomUUID(); // this is 36 characters long
                //byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long
                byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);

                // create an empty array and copy the dataToWrite to clear the complete standard file
                StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                int fileSize = standardDesfireFile.getFileSize();
                byte[] fullDataToWrite = new byte[fileSize];
                System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);

                // todo remove testdata
                fullDataToWrite = Utils.generateTestData(fileSize);

                // this is new to accept standard file data > 32/42 bytes size
                // this is due to maximum APDU size limit of 55 bytes for a DESFire D40 card
                // I'm splitting the complete data and send them in chunks

                List<byte[]> chunkedFullData = divideArray(fullDataToWrite, MAXIMUM_STANDARD_DATA_CHUNK);
                int chunkedFullDataSize = chunkedFullData.size();
                int dataSizeLoop = 0;
                System.out.println("chunkedFullDataSize: " + chunkedFullDataSize + " full length: " + fullDataToWrite.length);
                for (int i = 0; i < chunkedFullDataSize; i++) {
                    System.out.println("chunk " + i + " length: " + chunkedFullData.get(i).length);
                    writeToUiAppend(output, "writeStandard chunk number " + (i + 1));

                    PayloadBuilder pb = new PayloadBuilder();
                    byte[] payload = pb.writeToStandardFile(fileIdInt, chunkedFullData.get(i), dataSizeLoop);

                    writeToUiAppend(output, printData("payloadWriteData", payload));
                    boolean writeStandardSuccess = false;
                    try {
                        writeStandardSuccess = desfire.writeData(payload);
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                        writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                        e.printStackTrace();
                        return;
                    }
                    writeToUiAppend(output, "writeStandardResult: " + writeStandardSuccess);
                    if (writeStandardSuccess) {
                        writeToUiAppend(output, "number of bytes written: " + chunkedFullData.get(i).length + " to fileID " + fileIdInt);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeStandard success", COLOR_GREEN);
                    } else {
                        writeToUiAppend(output, "writeStandard NO success for fileID" + fileIdInt);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeStandard failed with code " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_RED);
                    }
                    dataSizeLoop += chunkedFullData.get(i).length;
                }
            }
        });

 */


        fileStandardWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected standard file in a selected application
                clearOutputFields();
                writeToUiAppend(output, "write to a standard or backup file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);

                // check that it is a standard or backup file !
                DesfireFile fileSettings = null;
                try {
                    fileSettings = desfire.getFileSettings(fileIdInt);
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }
                // check that it is a standard or backup file !
                String fileTypeName = fileSettings.getFileTypeName();
                writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                if ((!fileTypeName.equals("Standard")) && (!fileTypeName.equals("Backup"))) {
                    writeToUiAppend(output, "The selected file is not of type Standard or Backup but of type " + fileTypeName + ", aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }
                boolean isBackupFile = false; // backup files require a commit after writing
                if (fileTypeName.equals("Backup")) isBackupFile = true;
                // get a random payload with 32 bytes
                UUID uuid = UUID.randomUUID(); // this is 36 characters long
                //byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long

                // create an empty array and copy the dataToWrite to clear the complete standard file
                StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                int fileSize = standardDesfireFile.getFileSize();
                byte[] fullDataToWrite = new byte[fileSize];
                // limit the string
                if (dataToWriteString.length() > fileSize) dataToWriteString = dataToWriteString.substring(0, fileSize);
                byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);

                PayloadBuilder pb = new PayloadBuilder();
                byte[] payload = pb.writeToStandardFile(fileIdInt, fullDataToWrite);

                writeToUiAppend(output, printData("payloadWriteData", payload));
                String writeFileString;
                if (isBackupFile) {
                    writeFileString = "writeToBackupFile";
                } else {
                    writeFileString = "writeToStandardFile";
                }
                boolean writeStandardSuccess = false;
                try {
                    writeStandardSuccess = desfire.writeData(payload);
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                    e.printStackTrace();
                    return;
                }
                writeToUiAppend(output, writeFileString + "Result: " + writeStandardSuccess);
                writeToUiAppend(output,  writeFileString + "ResultCode: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                if (writeStandardSuccess) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, writeFileString + " Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout,  writeFileString + " NOT Success: " + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                }
                if (isBackupFile) {
                    // a following commit is necessary
                    writeToUiAppend(output, writeFileString + ": a commit to the card is necessary");
                    if (writeStandardSuccess) {
                        try {
                        boolean successCommit = desfire.commitTransaction();
                        writeToUiAppend(output, "commitSuccess: " + successCommit);
                        if (!successCommit) {
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit NOT Success, aborted", COLOR_RED);
                            writeToUiAppend(errorCode, "commit NOT Success: " + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                            writeToUiAppend(errorCode, "Did you forget to authenticate with a Write Access Key first ?");
                            return;
                        }
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit Success: " + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                            vibrateShort();
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                            writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        writeToUiAppend(output, "as the writing to the backup file was not successful I'm not trying to send a commit");
                    }
                }
            }
        });

        fileStandardRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // read from a selected standard file in a selected application
                clearOutputFields();
                // this uses the pre selected file
                writeToUiAppend(output, "read from a standard or backup file");
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = selectedFileIdInt;
                byte[] readStandard;
                boolean isBackupFile = false; // backup files require a commit after writing
                String readFileString;
                try {
                    // get the maximal length from getFileSettings
                    DesfireFile fileSettings = desfire.getFileSettings(fileIdInt);
                    // check that it is a standard file !
                    String fileTypeName = fileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    if ((!fileTypeName.equals("Standard")) && (!fileTypeName.equals("Backup"))) {
                        writeToUiAppend(output, "The selected file is not of type Standard or Backup but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }
                    if (fileTypeName.equals("Backup")) isBackupFile = true;

                    if (isBackupFile) {
                        readFileString = "readFromBackupFile";
                    } else {
                        readFileString = "readFromStandardFile";
                    }

                    StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                    int fileSize = standardDesfireFile.getFileSize();
                    writeToUiAppend(output, "fileSize: " + fileSize);
                    readStandard = desfire.readData((byte) (fileIdInt & 0xff), 0, fileSize);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, readFileString + "Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a read access key ?");
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a read access key ?");
                    e.printStackTrace();
                    return;
                }
                if (readStandard == null) {
                    writeToUiAppend(output, "error on reading from file number " + fileIdInt);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "did you forget to authenticate with a read access key ?", COLOR_RED);
                    return;
                }
                writeToUiAppend(output, printData(readFileString, readStandard));
                writeToUiAppend(output, new String(readStandard, StandardCharsets.UTF_8));
                writeToUiAppend(output, "finished");
                writeToUiAppend(output, "");
            }
        });

        /**
         * section for value files
         */

        fileValueCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a value file
                // get the input and sanity checks
                clearOutputFields();
                byte fileIdByte = (byte) (npValueFileId.getValue() & 0xFF);

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int lowerLimitInt = Integer.parseInt(lowerLimitValue.getText().toString());
                int upperLimitInt = Integer.parseInt(upperLimitValue.getText().toString());
                int initialValueInt = Integer.parseInt(initialValueValue.getText().toString());

                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npStandardFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }

                PayloadBuilder pb = new PayloadBuilder();
                // new for communication setting choice
                PayloadBuilder.CommunicationSetting communicationSetting;
                if (rbFileValuePlainCommunication.isChecked()) {
                    communicationSetting = PayloadBuilder.CommunicationSetting.Plain;
                } else if (rbFileValueMacedCommunication.isChecked()) {
                    communicationSetting = PayloadBuilder.CommunicationSetting.MACed;
                } else {
                    communicationSetting = PayloadBuilder.CommunicationSetting.Encrypted;
                }
                if ((lowerLimitInt < pb.getMINIMUM_VALUE_LOWER_LIMIT()) || (lowerLimitInt > pb.getMAXIMUM_VALUE_LOWER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong lower limit, maximum 1000 allowed only", COLOR_RED);
                    return;
                }
                if ((upperLimitInt < pb.getMINIMUM_VALUE_UPPER_LIMIT()) || (upperLimitInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong upper limit, maximum 1000 allowed only", COLOR_RED);
                    return;
                }
                if (upperLimitInt <= lowerLimitInt) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong upper limit, should be higher than lower limit", COLOR_RED);
                    return;
                }
                if ((initialValueInt < pb.getMINIMUM_VALUE_LOWER_LIMIT()) || (initialValueInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong initial value, should be between lower and higher limit", COLOR_RED);
                    return;
                }

                try {
                    byte[] payloadValueFile = pb.createValueFile(fileIdByte, communicationSetting,
                            1, 2, 3, 4, lowerLimitInt, upperLimitInt, initialValueInt, false);

                    boolean success = desfire.createValueFile(payloadValueFile);
                    writeToUiAppend(output, "createValueFileSuccess: " + success + " with FileID: " + Utils.byteToHex(fileIdByte)
                            + " lower limit: " + lowerLimitInt + " upper limit: " + upperLimitInt + " initial limit: " + initialValueInt);
                    if (!success) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createValueFile NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "createValueFile NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createValueFile Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        fileValueRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // read the values of a value file
                clearOutputFields();
                writeToUiAppend(output, "read the value of a value file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);
                byte fileIdByte = Byte.parseByte(selectedFileId);

                try {
                    // check that it is a value file !
                    DesfireFile fileSettings = desfire.getFileSettings(fileIdInt);
                    String fileTypeName = fileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    if (!fileTypeName.equals("Value")) {
                        writeToUiAppend(output, "The selected file is not of type Value but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }
                    ValueDesfireFile valueDesfireFile = (ValueDesfireFile) fileSettings;
                    try {
                        int valueFromFileSettings = valueDesfireFile.getValue();
                        writeToUiAppend(output, "the actual value of fileID " + fileIdInt + " is: " + valueFromFileSettings + " (retrieved from fileSettings)");
                    } catch (NullPointerException e) {
                        // do nothing
                    }
                    int value = 0;
                    try {
                        value = desfire.getValue(fileIdByte);
                    } catch (NullPointerException e) {
                        writeToUiAppend(output, "cannot read the value of the file");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readValue NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "readValue NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read Access Key first ?");
                        return;
                    }
                    int transactionCode = desfire.getCode();
                    if (transactionCode == 0) {
                        writeToUiAppend(output, "the actual value of fileID " + fileIdInt + " is: " + value);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readValue success", COLOR_GREEN);
                        vibrateShort();
                    } else {
                        writeToUiAppend(output, "cannot read the value of the file");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "readValue NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "readValue NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read Access Key first ?");
                        return;
                    }
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        fileValueCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // credit the selected value file
                clearOutputFields();
                writeToUiAppend(output, "credit the value of a value file");
                // The Credit command requires a preceding authentication with the key specified for “Read&Write” access
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);
                byte fileIdByte = Byte.parseByte(selectedFileId);

                try {
                    // check that it is a value file !
                    DesfireFile fileSettings = desfire.getFileSettings(fileIdInt);
                    String fileTypeName = fileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    if (!fileTypeName.equals("Value")) {
                        writeToUiAppend(output, "The selected file is not of type Value but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }
                    ValueDesfireFile valueDesfireFile = (ValueDesfireFile) fileSettings;
                    try {
                        int valueFromFileSettings = valueDesfireFile.getValue();
                        writeToUiAppend(output, "the actual value of fileID " + fileIdInt + " is: " + valueFromFileSettings + " (retrieved from fileSettings)");
                    } catch (NullPointerException e) {
                        // do nothing
                    }

                    PayloadBuilder pb = new PayloadBuilder();

                    int changeValueInt = Integer.parseInt(creditDebitValue.getText().toString());
                    if ((changeValueInt < 1) || (changeValueInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong change value, should be between lower and higher limit", COLOR_RED);
                        return;
                    }

                    boolean successWrite = desfire.credit(fileIdByte, changeValueInt);
                    writeToUiAppend(output, "creditValueFileSuccess: " + successWrite + " with FileID: " + Utils.byteToHex(fileIdByte)
                            + " credit value: " + changeValueInt);
                    if (!successWrite) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "creditValueFile NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "creditValueFile NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read&Write Access Key first ?");
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "creditValueFile Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();

                    boolean successCommit = desfire.commitTransaction();
                    writeToUiAppend(output, "commitSuccess: " + successCommit);
                    if (!successCommit) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "commit NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read&Write Access Key first ?");
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        fileValueDebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // debit the selected value file
                clearOutputFields();
                writeToUiAppend(output, "debit the value of a value file");
                // The Debit command requires a preceding authentication with one of the keys specified for Read, Write or Read&Write access
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = Integer.parseInt(selectedFileId);
                byte fileIdByte = Byte.parseByte(selectedFileId);

                try {
                    // check that it is a value file !
                    DesfireFile fileSettings = desfire.getFileSettings(fileIdInt);
                    String fileTypeName = fileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    if (!fileTypeName.equals("Value")) {
                        writeToUiAppend(output, "The selected file is not of type Value but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }
                    ValueDesfireFile valueDesfireFile = (ValueDesfireFile) fileSettings;
                    try {
                        int valueFromFileSettings = valueDesfireFile.getValue();
                        writeToUiAppend(output, "the actual value of fileID " + fileIdInt + " is: " + valueFromFileSettings + " (retrieved from fileSettings)");
                    } catch (NullPointerException e) {
                        // do nothing
                    }

                    PayloadBuilder pb = new PayloadBuilder();

                    int changeValueInt = Integer.parseInt(creditDebitValue.getText().toString());
                    if ((changeValueInt < 1) || (changeValueInt > pb.getMAXIMUM_VALUE_UPPER_LIMIT())) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong change value, should be between lower and higher limit", COLOR_RED);
                        return;
                    }

                    boolean successWrite = desfire.debit(fileIdByte, changeValueInt);
                    writeToUiAppend(output, "debitValueFileSuccess: " + successWrite + " with FileID: " + Utils.byteToHex(fileIdByte)
                            + " credit value: " + changeValueInt);
                    if (!successWrite) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "debitValueFile NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "debitValueFile NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read&Write Access Key first ?");
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "debitValueFile Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();

                    boolean successCommit = desfire.commitTransaction();
                    writeToUiAppend(output, "commitSuccess: " + successCommit);
                    if (!successCommit) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "commit NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read&Write Access Key first ?");
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        /**
         * section for record files
         * Note: as the 2 record types 'linear' and 'cyclic' are very similar they are handled in one method by choosing the file type with the radio button
         */

        fileRecordCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new record file
                // get the input and sanity checks
                clearOutputFields();
                byte fileIdByte = (byte) (npRecordFileId.getValue() & 0xFF);

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileRecordSize.getText().toString());
                if (fileSizeInt == 0) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a 0 size (minimum 1)", COLOR_RED);
                    return;
                }
                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                int fileNumberOfRecordsInt = Integer.parseInt(fileRecordNumberOfRecords.getText().toString());
                if (fileNumberOfRecordsInt < 2) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a 0 record number (minimum 2)", COLOR_RED);
                    return;
                }

                // get the type of file - linear or cyclic
                boolean isLinearRecordFile = rbLinearRecordFile.isChecked();
                boolean isCyclicRecordFile = rbCyclicRecordFile.isChecked();

                /*
                if (fileSizeInt != 32) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file size, 32 bytes allowed only", COLOR_RED);
                    return;
                }
                 */
                String fileTypeString = "";
                if (isLinearRecordFile) {
                    fileTypeString = "LinearRecord";
                } else {
                    fileTypeString = "CyclicRecord";
                }
                try {
                    PayloadBuilder pb = new PayloadBuilder();
                    // new for communication setting choice
                    PayloadBuilder.CommunicationSetting communicationSetting;
                    if (rbFileRecordPlainCommunication.isChecked()) {
                        communicationSetting = PayloadBuilder.CommunicationSetting.Plain;
                    } else if (rbFileRecordMacedCommunication.isChecked()) {
                        communicationSetting = PayloadBuilder.CommunicationSetting.MACed;
                    } else {
                        communicationSetting = PayloadBuilder.CommunicationSetting.Encrypted;
                    }
                    byte[] payloadRecordFile;
                    boolean success;
                    if (isLinearRecordFile) {
                        payloadRecordFile = pb.createLinearRecordsFile(fileIdByte, communicationSetting,
                                1, 2, 3, 4, fileSizeInt, fileNumberOfRecordsInt);
                        writeToUiAppend(output, printData("payloadCreateRecordFile", payloadRecordFile));
                        success = desfire.createLinearRecordFile(payloadRecordFile);
                    } else {
                        payloadRecordFile = pb.createCyclicRecordsFile(fileIdByte, communicationSetting,
                                1, 2, 3, 4, fileSizeInt, fileNumberOfRecordsInt);
                        writeToUiAppend(output, printData("payloadCreateRecordFile", payloadRecordFile));
                        success = desfire.createCyclicRecordFile(payloadRecordFile);
                    }
                    writeToUiAppend(output, "create" + fileTypeString + "FileSuccess: " + success
                            + " with FileID: " + Utils.byteToHex(fileIdByte) + ", size: " + fileSizeInt + " and number of records: " + fileNumberOfRecordsInt);
                    if (!success) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "create" + fileTypeString + "File NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "create" + fileTypeString + "File NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "create" + fileTypeString + "File Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

        fileRecordRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // read from a selected record file in a selected application
                clearOutputFields();
                // this uses the pre selected file
                writeToUiAppend(output, "read from a record file");
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                int fileIdInt = selectedFileIdInt;
                try {
                    // get the maximal length from getFileSettings
                    DesfireFile fileSettings = desfire.getFileSettings(fileIdInt);
                    // check that it is a standard file !
                    String fileTypeName = fileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    boolean isLinearRecordFile = false;
                    if (fileTypeName.equals("Linear Record")) {
                        isLinearRecordFile = true;
                        writeToUiAppend(output, "The selected file is of type Linear Record File");
                    } else if (fileTypeName.equals("Cyclic Record")) {
                        isLinearRecordFile = false;
                        writeToUiAppend(output, "The selected file is of type Cyclic Record File");
                    } else {
                        writeToUiAppend(output, "The selected file is not of type Linear or Cyclic Record but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }
                    String fileTypeString = "";
                    if (isLinearRecordFile) {
                        fileTypeString = "LinearRecord";
                    } else {
                        fileTypeString = "CyclicRecord";
                    }
                    RecordDesfireFile recordDesfireFile = (RecordDesfireFile) fileSettings;
                    //StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                    int recordSize = recordDesfireFile.getRecordSize();
                    int currentRecords = recordDesfireFile.getCurrentRecords();
                    int maxRecords = recordDesfireFile.getMaxRecords();
                    writeToUiAppend(output, "recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);
                    byte[] readRecords; // will hold the complete data of all records
                    readRecords = desfire.readRecords((byte) (fileIdInt & 0xff), 0, 0);
                    List<byte[]> readRecordList = divideArray(readRecords, recordSize);
                    //readStandard = desfire.readData(STANDARD_FILE_NUMBER, 0, fileSize);
                    int listSize = readRecordList.size();
                    writeToUiAppend(output, "--------");
                    for (int i = 0; i < listSize; i++) {
                        byte[] record = readRecordList.get(i);
                        writeToUiAppend(output, "record " + i + printData(" data", record));
                        if (record != null) {
                            writeToUiAppend(output, new String(record, StandardCharsets.UTF_8));
                        }
                        writeToUiAppend(output, "--------");
                    }
                    writeToUiAppend(output, "finished");
                    writeToUiAppend(output, "");
                    vibrateShort();
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a read access key ?");
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a read access key ?");
                    e.printStackTrace();
                    return;
                }

            }
        });

        fileRecordWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected record file in a selected application
                clearOutputFields();
                writeToUiAppend(output, "write to a record file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileRecordData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    //writeToUiAppend(errorCode, "please enter some data to write");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                int fileIdInt = selectedFileIdInt;
                try {
                    // check that it is a record file !
                    // get the maximal length from getFileSettings
                    DesfireFile fileSettings = desfire.getFileSettings(fileIdInt);
                    // check that it is a standard file !
                    String fileTypeName = fileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    boolean isLinearRecordFile = false;
                    if (fileTypeName.equals("Linear Record")) {
                        isLinearRecordFile = true;
                        writeToUiAppend(output, "The selected file is of type Linear Record File");
                    } else if (fileTypeName.equals("Cyclic Record")) {
                        isLinearRecordFile = false;
                        writeToUiAppend(output, "The selected file is of type Cyclic Record File");
                    } else {
                        writeToUiAppend(output, "The selected file is not of type Linear or Cyclic Record but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }
                    String fileTypeString = "";
                    if (isLinearRecordFile) {
                        fileTypeString = "LinearRecord";
                    } else {
                        fileTypeString = "CyclicRecord";
                    }
                    RecordDesfireFile recordDesfireFile = (RecordDesfireFile) fileSettings;
                    //StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                    int recordSize = recordDesfireFile.getRecordSize();
                    int currentRecords = recordDesfireFile.getCurrentRecords();
                    int maxRecords = recordDesfireFile.getMaxRecords();
                    writeToUiAppend(output, "recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);

                    // get a random payload with 32 bytes
                    UUID uuid = UUID.randomUUID(); // this is 36 characters long
                    byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long
                    //byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);

                    // create an empty array and copy the dataToWrite to clear the complete standard file
                    byte[] fullDataToWrite = new byte[recordSize];
                    fullDataToWrite = Utils.generateTestData(recordSize);
                    //System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);

                    // this the regular way but will probably fail when record size extends 40 bytes
                    writeToUiAppend(output, printData("fullDataToWrite", fullDataToWrite));
                    boolean writeRecordSuccess = false;
                    PayloadBuilder pbRecord = new PayloadBuilder();
                    if (isLinearRecordFile) {
                        byte[] payload = pbRecord.writeToLinearRecordsFile(fileIdInt, fullDataToWrite);
                        writeToUiAppend(output, printData("payloadWriteData", payload));
                        writeRecordSuccess = desfire.writeRecord(payload);
                    } else {
                        byte[] payload = pbRecord.writeToCyclicRecordsFile(fileIdInt, fullDataToWrite);
                        writeToUiAppend(output, printData("payloadWriteData", payload));
                        writeRecordSuccess = desfire.writeRecord(payload);
                    }
                    writeToUiAppend(output, "writeRecordResult: " + writeRecordSuccess);
                    if (writeRecordSuccess) {
                        writeToUiAppend(output, "record written " + " to fileID " + fileIdInt);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeRecord success", COLOR_GREEN);
                        vibrateShort();
                    } else {
                        writeToUiAppend(output, "writeRecord NO success for fileID" + fileIdInt);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeRecord failed with code " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_RED);
                    }

                    boolean successCommit = desfire.commitTransaction();
                    writeToUiAppend(output, "commitSuccess: " + successCommit);
                    if (!successCommit) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "commit NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read&Write Access Key first ?");
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();

                    // todo remove testdata
                    //fullDataToWrite = Utils.generateTestData(recordSize);

                    // this is new to accept standard file data > 32/42 bytes size
                    // this is due to maximum APDU size limit of 55 bytes for a DESFire D40 card
                    // I'm splitting the complete data and send them in chunks
/*
                    List<byte[]> chunkedFullData = divideArray(fullDataToWrite, MAXIMUM_STANDARD_DATA_CHUNK);
                    int chunkedFullDataSize = chunkedFullData.size();
                    int dataSizeLoop = 0;
                    System.out.println("chunkedFullDataSize: " + chunkedFullDataSize + " full length: " + fullDataToWrite.length);
                    for (int i = 0; i < chunkedFullDataSize; i++) {
                        System.out.println("chunk " + i + " length: " + chunkedFullData.get(i).length);
                        writeToUiAppend(output, "writeStandard chunk number " + (i + 1));

                        PayloadBuilder pb = new PayloadBuilder();
                        byte[] payload = pb.writeToStandardFile(fileIdInt, chunkedFullData.get(i), dataSizeLoop);

                        writeToUiAppend(output, printData("payloadWriteData", payload));
                        boolean writeStandardSuccess = false;
                        try {
                            writeStandardSuccess = desfire.writeData(payload);
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                            writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                            e.printStackTrace();
                            return;
                        }
                        writeToUiAppend(output, "writeStandardResult: " + writeStandardSuccess);
                        if (writeStandardSuccess) {
                            writeToUiAppend(output, "number of bytes written: " + chunkedFullData.get(i).length + " to fileID " + fileIdInt);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeStandard success", COLOR_GREEN);
                        } else {
                            writeToUiAppend(output, "writeStandard NO success for fileID" + fileIdInt);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeStandard failed with code " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_RED);
                        }
                        dataSizeLoop += chunkedFullData.get(i).length;
                    }

 */
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                    e.printStackTrace();
                    return;
                }
            }
        });

        fileRecordWriteTimestamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected record file in a selected application, the record gets an actual timestamp at the beginning
                clearOutputFields();
                writeToUiAppend(output, "write to a record file with a timestamp");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileRecordData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    //writeToUiAppend(errorCode, "please enter some data to write");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                int fileIdInt = selectedFileIdInt;
                try {
                    // check that it is a record file !
                    // get the maximal length from getFileSettings
                    DesfireFile fileSettings = desfire.getFileSettings(fileIdInt);
                    // check that it is a standard file !
                    String fileTypeName = fileSettings.getFileTypeName();
                    writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                    boolean isLinearRecordFile = false;
                    if (fileTypeName.equals("Linear Record")) {
                        isLinearRecordFile = true;
                        writeToUiAppend(output, "The selected file is of type Linear Record File");
                    } else if (fileTypeName.equals("Cyclic Record")) {
                        isLinearRecordFile = false;
                        writeToUiAppend(output, "The selected file is of type Cyclic Record File");
                    } else {
                        writeToUiAppend(output, "The selected file is not of type Linear or Cyclic Record but of type " + fileTypeName + ", aborted");
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        return;
                    }
                    String fileTypeString = "";
                    if (isLinearRecordFile) {
                        fileTypeString = "LinearRecord";
                    } else {
                        fileTypeString = "CyclicRecord";
                    }
                    RecordDesfireFile recordDesfireFile = (RecordDesfireFile) fileSettings;
                    //StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                    int recordSize = recordDesfireFile.getRecordSize();
                    int currentRecords = recordDesfireFile.getCurrentRecords();
                    int maxRecords = recordDesfireFile.getMaxRecords();
                    writeToUiAppend(output, "recordSize: " + recordSize + " currentRecords: " + currentRecords + " maxRecords: " + maxRecords);

                    // get a random payload with 32 bytes
                    UUID uuid = UUID.randomUUID(); // this is 36 characters long
                    //byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long
                    //byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);

                    // limit the string
                    dataToWriteString = Utils.getTimestamp() + " " + dataToWriteString;
                    if (dataToWriteString.length() > recordSize) dataToWriteString = dataToWriteString.substring(0, recordSize);
                    byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);
                    byte[] fullDataToWrite = new byte[recordSize];
                    System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);

                    /* testdata
                    // create an empty array and copy the dataToWrite to clear the complete standard file
                    byte[] fullDataToWrite = new byte[recordSize];
                    fullDataToWrite = Utils.generateTestData(recordSize);
                    System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);
                     */

                    writeToUiAppend(output, printData("fullDataToWrite", fullDataToWrite));
                    boolean writeRecordSuccess = false;
                    PayloadBuilder pbRecord = new PayloadBuilder();
                    if (isLinearRecordFile) {
                        byte[] payload = pbRecord.writeToLinearRecordsFile(fileIdInt, fullDataToWrite);
                        writeToUiAppend(output, printData("payloadWriteData", payload));
                        writeRecordSuccess = desfire.writeRecord(payload);
                    } else {
                        byte[] payload = pbRecord.writeToCyclicRecordsFile(fileIdInt, fullDataToWrite);
                        writeToUiAppend(output, printData("payloadWriteData", payload));
                        writeRecordSuccess = desfire.writeRecord(payload);
                    }
                    writeToUiAppend(output, "writeRecordResult: " + writeRecordSuccess);
                    if (writeRecordSuccess) {
                        writeToUiAppend(output, "record written " + " to fileID " + fileIdInt);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeRecord success", COLOR_GREEN);
                        vibrateShort();
                    } else {
                        writeToUiAppend(output, "writeRecord NO success for fileID" + fileIdInt);
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeRecord failed with code " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_RED);
                        return;
                    }

                    boolean successCommit = desfire.commitTransaction();
                    writeToUiAppend(output, "commitSuccess: " + successCommit);
                    if (!successCommit) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "commit NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        writeToUiAppend(errorCode, "Did you forget to authenticate with a Read&Write Access Key first ?");
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "commit Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                    vibrateShort();

                    // todo remove testdata
                    //fullDataToWrite = Utils.generateTestData(recordSize);

                    // this is new to accept standard file data > 32/42 bytes size
                    // this is due to maximum APDU size limit of 55 bytes for a DESFire D40 card
                    // I'm splitting the complete data and send them in chunks
/*
                    List<byte[]> chunkedFullData = divideArray(fullDataToWrite, MAXIMUM_STANDARD_DATA_CHUNK);
                    int chunkedFullDataSize = chunkedFullData.size();
                    int dataSizeLoop = 0;
                    System.out.println("chunkedFullDataSize: " + chunkedFullDataSize + " full length: " + fullDataToWrite.length);
                    for (int i = 0; i < chunkedFullDataSize; i++) {
                        System.out.println("chunk " + i + " length: " + chunkedFullData.get(i).length);
                        writeToUiAppend(output, "writeStandard chunk number " + (i + 1));

                        PayloadBuilder pb = new PayloadBuilder();
                        byte[] payload = pb.writeToStandardFile(fileIdInt, chunkedFullData.get(i), dataSizeLoop);

                        writeToUiAppend(output, printData("payloadWriteData", payload));
                        boolean writeStandardSuccess = false;
                        try {
                            writeStandardSuccess = desfire.writeData(payload);
                        } catch (Exception e) {
                            //throw new RuntimeException(e);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                            writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                            e.printStackTrace();
                            return;
                        }
                        writeToUiAppend(output, "writeStandardResult: " + writeStandardSuccess);
                        if (writeStandardSuccess) {
                            writeToUiAppend(output, "number of bytes written: " + chunkedFullData.get(i).length + " to fileID " + fileIdInt);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeStandard success", COLOR_GREEN);
                        } else {
                            writeToUiAppend(output, "writeStandard NO success for fileID" + fileIdInt);
                            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "writeStandard failed with code " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_RED);
                        }
                        dataSizeLoop += chunkedFullData.get(i).length;
                    }

 */
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                    e.printStackTrace();
                    return;
                }
            }
        });


        /**
         * section for transaction MAC files
         * Note: this  is available on DESFire EV2+ cards and using AES keys in application
         */
/*
        fileTMacCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new transaction mac file
                // get the input and sanity checks
                clearOutputFields();
                byte fileIdByte = (byte) (npRecordFileId.getValue() & 0xFF);

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileRecordSize.getText().toString());
                if (fileSizeInt == 0) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a 0 size (minimum 1)", COLOR_RED);
                    return;
                }
                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                int fileNumberOfRecordsInt = Integer.parseInt(fileRecordNumberOfRecords.getText().toString());
                if (fileNumberOfRecordsInt < 2) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a 0 record number (minimum 2)", COLOR_RED);
                    return;
                }

                // get the type of file - linear or cyclic
                boolean isLinearRecordFile = rbLinearRecordFile.isChecked();
                boolean isCyclicRecordFile = rbCyclicRecordFile.isChecked();

                String fileTypeString = "";
                if (isLinearRecordFile) {
                    fileTypeString = "LinearRecord";
                } else {
                    fileTypeString = "CyclicRecord";
                }
                try {
                    PayloadBuilder pb = new PayloadBuilder();
                    // new for communication setting choice
                    PayloadBuilder.CommunicationSetting communicationSetting;
                    if (rbFileRecordPlainCommunication.isChecked()) {
                        communicationSetting = PayloadBuilder.CommunicationSetting.Plain;
                    } else if (rbFileRecordMacedCommunication.isChecked()) {
                        communicationSetting = PayloadBuilder.CommunicationSetting.MACed;
                    } else {
                        communicationSetting = PayloadBuilder.CommunicationSetting.Encrypted;
                    }
                    byte[] payloadRecordFile;
                    boolean success;
                    if (isLinearRecordFile) {
                        payloadRecordFile = pb.createLinearRecordsFile(fileIdByte, communicationSetting,
                                1, 2, 3, 4, fileSizeInt, fileNumberOfRecordsInt);
                        writeToUiAppend(output, printData("payloadCreateRecordFile", payloadRecordFile));
                        success = desfire.createLinearRecordFile(payloadRecordFile);
                    } else {
                        payloadRecordFile = pb.createCyclicRecordsFile(fileIdByte, communicationSetting,
                                1, 2, 3, 4, fileSizeInt, fileNumberOfRecordsInt);
                        writeToUiAppend(output, printData("payloadCreateRecordFile", payloadRecordFile));
                        success = desfire.createCyclicRecordFile(payloadRecordFile);
                    }
                    writeToUiAppend(output, "create" + fileTypeString + "FileSuccess: " + success
                            + " with FileID: " + Utils.byteToHex(fileIdByte) + ", size: " + fileSizeInt + " and number of records: " + fileNumberOfRecordsInt);
                    if (!success) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "create" + fileTypeString + "File NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "create" + fileTypeString + "File NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "create" + fileTypeString + "File Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });
        */

        /**
         * section for encrypted standard files
         */
/*
        fileStandardCreateEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // create a new standard file
                // get the input and sanity checks
                clearOutputFields();
                byte fileIdByte = (byte) (npStandardFileId.getValue() & 0xFF);

                // the number of files on an EV1 tag is limited to 32 (00..31), but we are using the limit for the old D40 tag with a maximum of 15 files (00..14)
                // this limit is hardcoded in the XML file for the fileId numberPicker

                //byte fileIdByte = Byte.parseByte(fileId.getText().toString());
                int fileSizeInt = Integer.parseInt(fileSize.getText().toString());
                if (fileIdByte > (byte) 0x0f) {
                    // this should not happen as the limit is hardcoded in npFileId
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you entered a wrong file ID", COLOR_RED);
                    return;
                }
                try {
                    PayloadBuilder pb = new PayloadBuilder();
                    byte[] payloadStandardFile = pb.createStandardFile(fileIdByte, PayloadBuilder.CommunicationSetting.Plain,
                            1, 2, 3, 4, fileSizeInt);
                    boolean success = desfire.createStdDataFile(payloadStandardFile);
                    writeToUiAppend(output, "createStdDataFileSuccess: " + success + " with FileID: " + Utils.byteToHex(fileIdByte) + " and size: " + fileSizeInt);
                    if (!success) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createStdDataFile NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "createStdDataFile NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    }
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "createStdDataFile Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc(), COLOR_GREEN);
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    e.printStackTrace();
                    return;
                }
            }
        });

 */
/*
        fileStandardWriteEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // write to a selected standard file in a selected application
                clearOutputFields();
                writeToUiAppend(output, "write to an encrypted standard file");
                // this uses the pre selected file
                if (TextUtils.isEmpty(selectedFileId)) {
                    //writeToUiAppend(errorCode, "you need to select a file first");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select a file first", COLOR_RED);
                    return;
                }
                String dataToWriteString = fileData.getText().toString();
                if (TextUtils.isEmpty(dataToWriteString)) {
                    //writeToUiAppend(errorCode, "please enter some data to write");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "please enter some data to write", COLOR_RED);
                    return;
                }
                int fileIdInt = selectedFileIdInt;
                //int fileIdInt = Integer.parseInt(selectedFileId);
                //byte fileIdByte = Byte.parseByte(selectedFileId);

                // check that it is a standard file !
                DesfireFile fileSettings = null;
                try {
                    fileSettings = desfire.getFileSettings(fileIdInt);
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }
                //DesfireFile fileSettings = desfire.getFileSettings((byte) 0x00);
                // check that it is a standard file !
                String fileTypeName = fileSettings.getFileTypeName();
                writeToUiAppend(output, "file number " + fileIdInt + " is of type " + fileTypeName);
                if (!fileTypeName.equals("Standard")) {
                    writeToUiAppend(output, "The selected file is not of type Standard but of type " + fileTypeName + ", aborted");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                    return;
                }

                // get a random payload with 32 bytes
                UUID uuid = UUID.randomUUID(); // this is 36 characters long
                //byte[] dataToWrite = Arrays.copyOf(uuid.toString().getBytes(StandardCharsets.UTF_8), 32); // this 32 bytes long
                byte[] dataToWrite = dataToWriteString.getBytes(StandardCharsets.UTF_8);

                // create an empty array and copy the dataToWrite to clear the complete standard file
                StandardDesfireFile standardDesfireFile = (StandardDesfireFile) fileSettings;
                int fileSize = standardDesfireFile.getFileSize();
                byte[] fullDataToWrite = new byte[fileSize];
                System.arraycopy(dataToWrite, 0, fullDataToWrite, 0, dataToWrite.length);

                PayloadBuilder pb = new PayloadBuilder();
                byte[] payload = pb.writeToStandardFile(fileIdInt, fullDataToWrite);

                writeToUiAppend(output, printData("payloadWriteData", payload));
                boolean writeStandardSuccess = false;
                try {
                    writeStandardSuccess = desfire.writeData(payload);
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "did you forget to authenticate with a write access key ?");
                    e.printStackTrace();
                    return;
                }
                writeToUiAppend(output, "writeStandardSuccessResult: " + writeStandardSuccess);
                writeToUiAppend(output, "writeStandardSuccessResultCode: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
            }
        });

 */
/*
        manualEncryption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // runs a manual encryption
                byte[] preprocessEnciphered = Utils.hexStringToByteArray("903d0000270e000000200000736f6d652064617461000000000000000000000000000000000000000000000000");
                int preprocessEncipheredOffset = 7; // + 5
                byte[] skey = Utils.hexStringToByteArray("b1220ed204344aad");
                byte[] iv = Utils.hexStringToByteArray("0000000000000000");
                byte[] ciphertextExp = Utils.hexStringToByteArray("cfc254a80ee964f79da93f53018db7e0905540c6d5c08d8fc612c7631c90f12d2e1f04f40f8ec935");
                byte[] ret = Utils.hexStringToByteArray("903d00002f0e000000200000cfc254a80ee964f79da93f53018db7e0905540c6d5c08d8fc612c7631c90f12d2e1f04f40f8ec93500");
                byte[] x = Utils.hexStringToByteArray("");

                byte[] preprocessEncipheredShort = Arrays.copyOfRange(preprocessEnciphered, 12, preprocessEnciphered.length);
                String preprocessEncipheredShortString = Utils.printData("preprocessEncipheredShort", preprocessEncipheredShort);
                writeToUiAppend(output, printData("preprocessEncipheredShort", preprocessEncipheredShort));

                writeToUiAppend(output, printData("preprocessEnciphered", preprocessEnciphered));
                writeToUiAppend(output, printData("ciphertext expected ", ciphertextExp));
                byte[] ciphertext = preprocessEncipheredDes(preprocessEnciphered, preprocessEncipheredOffset, skey);
                writeToUiAppend(output, printData("ciphertext done     ", ciphertext));
            }
        });

 */

        /**
         * section for authentication with default keys
         */

        authDM0D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the master application key = 00...
                clearOutputFields();
                String logString = "authenticate with DEFAULT DES key number 0x00 = master application key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, "master", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });


        authD0D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the application master key = 00...
                clearOutputFields();
                String logString = "authenticate with DEFAULT DES key number 0x00 = application master key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, "app master", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD1D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with DEFAULT DES key number 0x01 = read & write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES_DEFAULT, "read & write", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the change access rights key = 02...
                clearOutputFields();
                String logString = "authenticate with DEFAULT DES key number 0x02 = change access rights key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES_DEFAULT, "change access rights", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read access key = 03...
                clearOutputFields();
                String logString = "authenticate with DEFAULT DES key number 0x03 = read access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES_DEFAULT, "read", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with DEFAULT DES key number 0x04 = write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES_DEFAULT, "write", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });
        
        // AES keys
        
        authDM0A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the master application key = 00...
                clearOutputFields();
                String logString = "authenticate with DEFAULT AES key number 0x00 = master application key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT, "master", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });


        authD0A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the application master key = 00...
                clearOutputFields();
                String logString = "authenticate with DEFAULT AES key number 0x00 = application master key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES_DEFAULT, "app master", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });
        
        authD1A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with DEFAULT AES key number 0x01 = read & write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES_DEFAULT, "read & write", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the change access rights key = 02...
                clearOutputFields();
                String logString = "authenticate with DEFAULT AES key number 0x02 = change access rights key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES_DEFAULT, "change access rights", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read access key = 03...
                clearOutputFields();
                String logString = "authenticate with DEFAULT AES key number 0x03 = read access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES_DEFAULT, "read", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with DEFAULT AES key number 0x04 = write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES_DEFAULT, "write", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        /**
         * section for authentication with changed keys
         */

        authDM0DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the master application key = 00...
                clearOutputFields();
                String logString = "authenticate with CHANGED DES key number 0x00 = master application key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES, "master", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });


        authD0DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the application master key = 00...
                clearOutputFields();
                String logString = "authenticate with CHANGED DES key number 0x00 = application master key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, "app master", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD1DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with CHANGED DES key number 0x01 = read & write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES, "read & write", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the change access rights key = 02...
                clearOutputFields();
                String logString = "authenticate with CHANGED DES key number 0x02 = change access rights key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES, "change access rights", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read access key = 03...
                clearOutputFields();
                String logString = "authenticate with CHANGED DES key number 0x03 = read access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES, "read", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4DC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with CHANGED DES key number 0x04 = write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES, "write", KeyType.DES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        // AES keys

        authDM0AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the master application key = 00...
                clearOutputFields();
                String logString = "authenticate with CHANGED AES key number 0x00 = master application key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES, "master", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });


        authD0AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the application master key = 00...
                clearOutputFields();
                String logString = "authenticate with CHANGED AES key number 0x00 = application master key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_AES, "app master", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD1AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with CHANGED AES key number 0x01 = read & write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_AES, "read & write", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD2AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the change access rights key = 02...
                clearOutputFields();
                String logString = "authenticate with CHANGED AES key number 0x02 = change access rights key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_AES, "change access rights", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD3AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read access key = 03...
                clearOutputFields();
                String logString = "authenticate with CHANGED AES key number 0x03 = read access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_AES, "read", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });

        authD4AC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                String logString = "authenticate with CHANGED AES key number 0x04 = write access key";
                writeToUiAppend(output, logString);
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplication(APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES, "write", KeyType.AES);
                if (success) {
                    writeToUiAppend(output, logString + " SUCCESS");
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                    vibrateShort();
                } else {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " NO SUCCESS", COLOR_RED);
                }
            }
        });
        

        // following is the old authentication menu

        /**
         * section for authentication with default keys
         */
/*
        authKeyDM0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the master application key = 00...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with key number 0x00 = master application key");
                // run this auth when selectedApplicationId == MASTER APPLICATION ID
                // this method should run with selected Master Application (master AID) only
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                if (!Arrays.equals(selectedApplicationId, new byte[3])) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select the master application first", COLOR_RED);
                    return;
                }
                boolean success = authenticateApplicationDes(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, "master");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });
        authKeyD0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the application master key = 00...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with key number 0x00 = application master key");
                try {
                    boolean dfAuthApp = desfire.authenticate(APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthApp);
                    if (!dfAuthApp) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "authenticateApplication NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    } else {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication SUCCESS", COLOR_GREEN);
                    }
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        });

        authKeyD1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with key number 0x01 = read&write access key");
                try {
                    boolean dfAuthApp = desfire.authenticate(APPLICATION_KEY_RW_DES_DEFAULT, KEY_NUMBER_RW, KeyType.DES);
                    writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthApp);
                    if (!dfAuthApp) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "authenticateApplication NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    } else {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication SUCCESS", COLOR_GREEN);
                    }
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }

            }
        });

        authKeyD2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the change access access key = 02...
                clearOutputFields();
                boolean success = authenticateWithKeyDes(APPLICATION_KEY_CAR_DES_DEFAULT, APPLICATION_KEY_CAR_NUMBER);
            }
        });

        authKeyD3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read access key = 03...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with key number 0x03 = read access key");
                try {
                    boolean dfAuthApp = desfire.authenticate(APPLICATION_KEY_R_DES_DEFAULT, APPLICATION_KEY_R_NUMBER, KeyType.DES);
                    writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthApp);
                    if (!dfAuthApp) {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                        writeToUiAppend(errorCode, "authenticateApplication NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
                        return;
                    } else {
                        writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication SUCCESS", COLOR_GREEN);
                    }
                } catch (IOException e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    writeToUiAppend(errorCode, "Stack: " + Arrays.toString(e.getStackTrace()));
                    //writeToUiAppend(output, "IOException: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }

            }
        });

        authKeyD4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the write access key = 04...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with DEFAULT key number 0x04 = write access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplicationDes(APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES_DEFAULT, "write");
                //boolean success = authenticateApplicationAes(APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_AES_DEFAULT, "write");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });
*/
        /**
         * section for authentication with changed keys
         */
/*
        authKeyDM0C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the master access key = 04...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with CHANGED key number 0x00 = master application key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                // run this auth when selectedApplicationId == MASTER APPLICATION ID
                // this method should run with selected Master Application (master AID) only
                if (!Arrays.equals(selectedApplicationId, new byte[3])) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select the master application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                //boolean success = authenticateApplicationDes(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY, "master");
                boolean success = authenticateApplicationAes(MASTER_APPLICATION_KEY_NUMBER, VIRTUAL_CARD_KEY_CONFIG, "master"); // todo CHANGE because misconfiguration
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });

        authKeyD0C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the master access key = 04...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with CHANGED key number 0x00 = master access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplicationDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, "master");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });

        authKeyD1C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read&write access key = 01...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with CHANGED key number 0x01 = read & write access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplicationDes(APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES, "read & write");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });

        authKeyD2C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the change access access key = 02...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with CHANGED key number 0x02 = change access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplicationDes(APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES, "change");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });

        authKeyD3C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the read access key = 03...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with CHANGED key number 0x03 = read access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplicationDes(APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES, "read");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });

        authKeyD4C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the write access key = 04...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with CHANGED key number 0x04 = write access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = authenticateApplicationDes(APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES, "write");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });
*/

        // virtual card configuration key
/*
        authKeyAM0.setOnClickListener(new View.OnClickListener() {
            // this is the auth for the Master Application Key using AES key
            @Override
            public void onClick(View view) {
                // authenticate with the master application key = 00...
                clearOutputFields();
                writeToUiAppend(output, "AES authenticate with key number 0x00 = master application key");
                // run this auth when selectedApplicationId == MASTER APPLICATION ID
                // this method should run with selected Master Application (master AID) only
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                if (!Arrays.equals(selectedApplicationId, new byte[3])) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select the master application first", COLOR_RED);
                    return;
                }
                boolean success = authenticateApplicationAes(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_AES_DEFAULT, "master");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });
        authKeyVc20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // authenticate with the virtual card configuration key = 20...
                clearOutputFields();
                writeToUiAppend(output, "authenticate with key number 0x20 = virtual card configuration key");
                // run this auth when selectedApplicationId == MASTER APPLICATION ID
                // this method should run with selected Master Application (master AID) only
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                if (!Arrays.equals(selectedApplicationId, new byte[3])) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select the master application first", COLOR_RED);
                    return;
                }
                boolean success = authenticateApplicationAes(VIRTUAL_CARD_KEY_CONFIG_NUMBER, VIRTUAL_CARD_KEY_CONFIG, "VC20 CONFIG");
                writeToUiAppend(output, "authenticateApplication run successfully: " + success);
            }
        });
*/
        /**
         * section for key handling (default keys)
         */
/*
        changeKeyDM0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x00 = master application key
                writeToUiAppend(output, "change the key number 0x00 = master application key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                // this method should run with selected Master Application (master AID) only
                if (!Arrays.equals(selectedAid, new byte[3])) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select the master application first", COLOR_RED);
                    return;
                }
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES, MASTER_APPLICATION_KEY_DES_DEFAULT, "master");
                writeToUiAppend(output, "changeMasterApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });


 */
/*
        changeKeyD0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x00 = master application key
                writeToUiAppend(output, "change the key number 0x00 = application master key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_MASTER_DES_DEFAULT, "master");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();

            }
        });
*/
        /*
        changeKeyD1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x01 = read&write access key
                writeToUiAppend(output, "change the key number 0x01 = read & write access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES, APPLICATION_KEY_RW_DES_DEFAULT, "read & write");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });

        changeKeyD2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x02 = change access key
                writeToUiAppend(output, "change the key number 0x02 = change access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId;
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES, APPLICATION_KEY_CAR_DES_DEFAULT, "change");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });

        changeKeyD3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x03 = read access key
                writeToUiAppend(output, "change the key number 0x03 = read access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES, APPLICATION_KEY_R_DES_DEFAULT, "read");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();

            }
        });
*/
        /*
        changeKeyD4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x04 = write access key
                writeToUiAppend(output, "change the key number 0x04 = write access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES, APPLICATION_KEY_W_DES_DEFAULT, "write");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();

            }
        });
*/
        /*
        changeKeyVc20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x20 = virtual card configuration key
                writeToUiAppend(output, "change the key number 0x20 = virtual card configuration key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                // this method should run with selected Master Application (master AID) only
                if (!Arrays.equals(selectedAid, new byte[3])) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select the master application first", COLOR_RED);
                    return;
                }
                Utils.reverseByteArrayInPlace(selectedAid);
                //boolean success = changeApplicationKeyAes(VIRTUAL_CARD_KEY_CONFIG_NUMBER, APPLICATION_KEY_MASTER, VIRTUAL_CARD_KEY_CONFIG_NUMBER, VIRTUAL_CARD_KEY_CONFIG, VIRTUAL_CARD_KEY_CONFIG_DEFAULT, "vc20");
                boolean success = desfire.changeKeyVc20();

                writeToUiAppend(output, "changeVcConfigurationKey run successfully: " + success);
                vibrateShort();
            }
        });
*/

        /**
         * section for changed key handling
         */
/*
        changeKeyDM0C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x00 = master application key
                writeToUiAppend(output, "change the key number 0x00 = master application key (changed key)");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                if (!Arrays.equals(selectedAid, new byte[3])) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select the master application first", COLOR_RED);
                    return;
                }
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DES_DEFAULT, MASTER_APPLICATION_KEY_DES, "master");
                // todo CHANGE because misconfiguration
                //boolean success = changeApplicationKeyAes(MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY, MASTER_APPLICATION_KEY_NUMBER, MASTER_APPLICATION_KEY_DEFAULT, MASTER_APPLICATION_KEY_AES_DEFAULT, "master");
                writeToUiAppend(output, "changeMasterApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });

        changeKeyD0C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x00 = master application key
                writeToUiAppend(output, "change the key number 0x00 = application master key (changed key)");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES_DEFAULT, APPLICATION_KEY_MASTER_DES, "master");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });

        changeKeyD1C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x01 = read&write access key
                writeToUiAppend(output, "change the key number 0x01 = read & write access key");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_RW_NUMBER, APPLICATION_KEY_RW_DES_DEFAULT, APPLICATION_KEY_RW_DES, "read & write");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });

        changeKeyD2C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x02 = change access key
                writeToUiAppend(output, "change the key number 0x02 = change access key (changed key)");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_CAR_NUMBER, APPLICATION_KEY_CAR_DES_DEFAULT, APPLICATION_KEY_CAR_DES, "change");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });

        changeKeyD3C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x03 = read access key
                writeToUiAppend(output, "change the key number 0x03 = read access key (changed keys)");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_R_NUMBER, APPLICATION_KEY_R_DES_DEFAULT, APPLICATION_KEY_R_DES, "read");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });

        changeKeyD4C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change key number 0x04 = write access key
                writeToUiAppend(output, "change the key number 0x04 = write access key (changed keys)");
                if (selectedApplicationId == null) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "you need to select an application first", COLOR_RED);
                    return;
                }
                byte[] selectedAid = selectedApplicationId.clone();
                Utils.reverseByteArrayInPlace(selectedAid);
                boolean success = changeApplicationKeyDes(APPLICATION_KEY_MASTER_NUMBER, APPLICATION_KEY_MASTER_DES, APPLICATION_KEY_W_NUMBER, APPLICATION_KEY_W_DES_DEFAULT, APPLICATION_KEY_W_DES, "write");
                writeToUiAppend(output, "changeApplicationKey run successfully: " + success);
                vibrateShort();
            }
        });
*/
        /**
         * section for service methods
         */

        getFileSettingsDesfire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // reads the stored file settings from DESFireEV1 class
                clearOutputFields();
                writeToUiAppend(output, "get the fileSettings from DESFireEV1 card");
                DesfireFile desfireFile = null;
                try {
                    desfireFile = desfire.getFileSettings(selectedFileIdInt);
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }
                if (desfireFile == null) {
                    writeToUiAppend(output, "the fileSettings from DESFireEV1 class are NULL");
                    return;
                }
                String fileTypeName = desfireFile.getFileTypeName();
                int fileStandardSize = 0;
                writeToUiAppend(output, "The file " + selectedFileIdInt + " is of type " + fileTypeName);
                DesfireFileCommunicationSettings comSetting = desfireFile.getCommunicationSettings();
                writeToUiAppend(output, "the communicationSettings are " + comSetting.getDescription());
                if (!fileTypeName.equals("Standard")) {
                    writeToUiAppend(output, "The file is not of type Standard but of type " + fileTypeName + ", no fileSize");
                    //writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                } else {
                    StandardDesfireFile standardDesfireFile = (StandardDesfireFile) desfireFile;
                    fileStandardSize = standardDesfireFile.getFileSize();
                    writeToUiAppend(output, "file " + selectedFileIdInt + " size: " + fileStandardSize);
                }

                Map<Integer, String> permMap = desfireFile.getCompactPermissionMap();
                writeToUiAppend(output, "----- permission map ------");
                for (Map.Entry<Integer, String> entry : permMap.entrySet()) {
                    writeToUiAppend(output, entry.getKey() + ":" + entry.getValue().toString());
                }
                writeToUiAppend(output, "-----------");
            }
        });

        changeFileSettingsDesfire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // reads the stored file settings from DESFireEV1 class
                clearOutputFields();
                String logString = "change the fileSettings on a DESFireEV1 card";
                writeToUiAppend(output, logString);
                DesfireFile desfireFile = null;
                try {
                    desfireFile = desfire.getFileSettings(selectedFileIdInt);
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }
                if (desfireFile == null) {
                    writeToUiAppend(output, "the fileSettings from DESFireEV1 class are NULL");
                    return;
                }
                String fileTypeName = desfireFile.getFileTypeName();
                int fileStandardSize = 0;
                writeToUiAppend(output, "The file " + selectedFileIdInt + " is of type " + fileTypeName);
                DesfireFileCommunicationSettings comSetting = desfireFile.getCommunicationSettings();
                writeToUiAppend(output, "the communicationSettings are " + comSetting.getDescription());
                if (!fileTypeName.equals("Standard")) {
                    writeToUiAppend(output, "The file is not of type Standard but of type " + fileTypeName + ", no fileSize");
                    //writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                } else {
                    StandardDesfireFile standardDesfireFile = (StandardDesfireFile) desfireFile;
                    fileStandardSize = standardDesfireFile.getFileSize();
                    writeToUiAppend(output, "file " + selectedFileIdInt + " size: " + fileStandardSize);
                }

                Map<Integer, String> permMap = desfireFile.getCompactPermissionMap();
                writeToUiAppend(output, "----- permission map ------");
                for (Map.Entry<Integer, String> entry : permMap.entrySet()) {
                    writeToUiAppend(output, entry.getKey() + ":" + entry.getValue().toString());
                }
                writeToUiAppend(output, "-----------");
                writeToUiAppend(output, desfireFile.toString());
                writeToUiAppend(output, "now we change the keys");
                byte fileNumberByte = Byte.parseByte(selectedFileId);
                byte FILE_COMMUNICATION_SETTINGS = (byte) 0x00; // plain communication
                byte ACCESS_RIGHTS_RW_CAR_FREE = (byte) 0xEE; // Read&Write Access (free) & ChangeAccessRights (free)
                byte ACCESS_RIGHTS_R_W_FREE = (byte) 0xEE; // Read Access (free) & Write Access (free)
                try {
                    boolean success = desfire.changeFileSettings(fileNumberByte, FILE_COMMUNICATION_SETTINGS, ACCESS_RIGHTS_RW_CAR_FREE, ACCESS_RIGHTS_R_W_FREE);
                    writeToUiAppend(output, logString + "success: " + success);
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, logString + " SUCCESS", COLOR_GREEN);
                } catch (Exception e) {
                    writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
                    e.printStackTrace();
                    return;
                }
            }
        });

        /*
        getFileSettingsDesfire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // reads the stored file settings from DESFireEV1 class
                clearOutputFields();
                writeToUiAppend(output, "get the fileSettings from DESFireEV1 class");
                DesfireFile[] desfireFiles = desfire.getFileSettings();
                if (desfireFiles == null) {
                    writeToUiAppend(output, "the fileSettings from DESFireEV1 class are NULL");
                    return;
                }
                int desfireFileSize = desfireFiles.length;
                int fileSize = 0;
                writeToUiAppend(output, "the fileSettings are stored for " + desfireFileSize + " files:");
                for (int i = 0; i < desfireFileSize; i++) {
                    DesfireFile desfireFile = desfireFiles[i];

                    if (desfireFile == null) {
                        //writeToUiAppend(output, "The file " + i + " is NULL");
                        // do nothing to keep the output short
                    } else {
                        String fileTypeName = desfireFile.getFileTypeName();
                        writeToUiAppend(output, "The file " + i + " is of type " + fileTypeName);
                        if (!fileTypeName.equals("Standard")) {
                            writeToUiAppend(output, "The file is not of type Standard but of type " + fileTypeName + ", no fileSize");
                            //writeToUiAppendBorderColor(errorCode, errorCodeLayout, "wrong file type", COLOR_RED);
                        } else {
                            StandardDesfireFile standardDesfireFile = (StandardDesfireFile) desfireFile;
                            fileSize = standardDesfireFile.getFileSize();
                        }
                        writeToUiAppend(output, "file " + i + " size: " + fileSize);
                        Map<Integer, String> permMap = desfireFile.getCompactPermissionMap();
                        writeToUiAppend(output, "----- permission map ------");
                        for (Map.Entry<Integer, String> entry : permMap.entrySet()) {
                            writeToUiAppend(output, entry.getKey() + ":" + entry.getValue().toString());
                        }
                        writeToUiAppend(output, "-----------");
                    }
                }

            }
        });
        */
        // DesfireFile[] getFileSettings()

    }

    private String getFileInformationType(int fileNumber) {
        String fileType;
        String commType;
        DesfireFile desfireFile = null;
        try {
            desfireFile = desfire.getFileSettings(fileNumber);
        } catch (Exception e) {
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
            e.printStackTrace();
            return "";
        }
        if (desfireFile == null) {
            writeToUiAppend(output, "the fileSettings from DESFireEV1 class are NULL");
            return "";
        }
        fileType = desfireFile.getFileTypeName();
        int comSett = desfireFile.getCommunicationSettings().getValue();
        if (comSett == 0) {
            commType = "Plain";
        } else if (comSett == 1) {
            commType = "MACed";
        } else {
            commType = "Encrypted";
        }
        return fileType + " | " + commType;
    }

    /**
     * experimental DES encryption
     */


    // code taken from NFCjLib DESFireEV1.java but reduced to DES mode only
    // warning: do not use for TDES or AES keys

    // calculate CRC and append, encrypt, and update global IV
    private byte[] preprocessEncipheredDes(byte[] apdu, int offset, byte[] skey) {
        byte[] ciphertext = encryptApduDes(apdu, offset, skey);

        byte[] ret = new byte[5 + offset + ciphertext.length + 1];
        System.arraycopy(apdu, 0, ret, 0, 5 + offset);
        System.arraycopy(ciphertext, 0, ret, 5 + offset, ciphertext.length);
        ret[4] = (byte) (offset + ciphertext.length);

        return ret;
    }

    /* Only data is encrypted. Headers are left out (e.g. keyNo for credit). */
    private static byte[] encryptApduDes(byte[] apdu, int offset, byte[] sessionKey) {
        int blockSize = 8;
        int payloadLen = apdu.length - 6;
        byte[] crc = null;
        crc = calculateApduCRC16C(apdu, offset);

        int padding = 0;  // padding=0 if block length is adequate
        if ((payloadLen - offset + crc.length) % blockSize != 0)
            padding = blockSize - (payloadLen - offset + crc.length) % blockSize;
        int ciphertextLen = payloadLen - offset + crc.length + padding;
        byte[] plaintext = new byte[ciphertextLen];
        System.arraycopy(apdu, 5 + offset, plaintext, 0, payloadLen - offset);
        System.arraycopy(crc, 0, plaintext, payloadLen - offset, crc.length);
        return sendDes(sessionKey, plaintext);
    }

    private static byte[] sendDes(byte[] key, byte[] data) {
        return decryptDes(key, data);
    }

    // CRC16 calculated only over data
    private static byte[] calculateApduCRC16C(byte[] apdu, int offset) {
        if (apdu.length == 5) {
            return CRC16.get(new byte[0]);
        } else {
            return CRC16.get(apdu, 5 + offset, apdu.length - 5 - offset - 1);
        }
    }

    // DES/3DES decryption: CBC send mode and CBC receive mode
    // here fixed to SEND_MODE = decrypt
    private static byte[] decryptDes(byte[] key, byte[] data) {

        /* this method
        plaintext before encryption length: 24 data: d400000000000000d4000000000000007f917f9100000000
        ciphertext after encryption length: 24 data: 3b93de449348de6a16c92664a51d152d5d07194befeaa71d
         */
        /* method from DESFireEV1.java
        plaintext before encryption: d400000000000000d4000000000000007f917f9100000000
        ciphertext after encryption: 2c1ba72be0074ee529f8b450bfe42a465196116967b8272f
         */

        byte[] modifiedKey = new byte[24];
        System.arraycopy(key, 0, modifiedKey, 16, 8);
        System.arraycopy(key, 0, modifiedKey, 8, 8);
        System.arraycopy(key, 0, modifiedKey, 0, key.length);

        /* MF3ICD40, which only supports DES/3DES, has two cryptographic
         * modes of operation (CBC): send mode and receive mode. In send mode,
         * data is first XORed with the IV and then decrypted. In receive
         * mode, data is first decrypted and then XORed with the IV. The PCD
         * always decrypts. The initial IV, reset in all operations, is all zeros
         * and the subsequent IVs are the last decrypted/plain block according with mode.
         *
         * MDF EV1 supports 3K3DES/AES and remains compatible with MF3ICD40.
         */
        byte[] ciphertext = new byte[data.length];
        byte[] cipheredBlock = new byte[8];

        // XOR w/ previous ciphered block --> decrypt
        for (int i = 0; i < data.length; i += 8) {
            for (int j = 0; j < 8; j++) {
                data[i + j] ^= cipheredBlock[j];
            }
            cipheredBlock = TripleDES.decrypt(modifiedKey, data, i, 8);
            System.arraycopy(cipheredBlock, 0, ciphertext, i, 8);
        }
        return ciphertext;
    }

    /**
     * section for authentication
     */

    private boolean authenticateApplication(byte keyNumber, byte[] key, String keyName, KeyType keyType) {
        writeToUiAppend(output,  keyType.toString() + " authentication with key " + String.format("0x%02X", keyNumber) + "(= " + keyName + "access key)");
        try {
            boolean authApp = desfire.authenticate(key, keyNumber, keyType);
            writeToUiAppend(output, "authApplicationResult: " + authApp);
            if (!authApp) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                writeToUiAppend(errorCode, "authenticateApplication NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
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
            //writeToUiAppend(output, "IOException: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticateApplicationDes(byte keyNumber, byte[] key, String keyName) {
        writeToUiAppend(output, "authenticate the selected application with the key number " + String.format("0x%02X", keyNumber) + "(= " + keyName + "access key)");
        try {
            boolean authApp = desfire.authenticate(key, keyNumber, KeyType.DES);
            writeToUiAppend(output, "authApplicationResult: " + authApp);
            if (!authApp) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                writeToUiAppend(errorCode, "authenticateApplication NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
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
            //writeToUiAppend(output, "IOException: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticateApplicationAes(byte keyNumber, byte[] key, String keyName) {
        writeToUiAppend(output, "AES authenticate the selected application with the key number " + String.format("0x%02X", keyNumber) + "(= " + keyName + "access key)");
        try {
            boolean authApp = desfire.authenticate(key, keyNumber, KeyType.AES);
            writeToUiAppend(output, "authApplicationResult: " + authApp);
            if (!authApp) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                writeToUiAppend(errorCode, "authenticateApplication NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
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
            //writeToUiAppend(output, "IOException: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * section for change key handling
     */

    /*
    private boolean changeApplicationKeyDes(byte[] applicationId, byte applicationMasterKeyNumber,
                                            byte[] applicationMasterKey, byte changeKeyNumber, byte[] changeKeyNew, byte[] changeKeyOld, String changeKeyName) {
*/
    private boolean changeApplicationKeyDes(byte applicationMasterKeyNumber,
                                            byte[] applicationMasterKey, byte changeKeyNumber, byte[] changeKeyNew, byte[] changeKeyOld, String changeKeyName) {

        // change key name e.g. master, read&write, car, read, write
        boolean result = false;
        try {
            /*
            // select master application
            boolean dfSelectM = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
            writeToUiAppend(output, "selectMasterApplicationResult: " + dfSelectM);

            // authenticate with MasterApplicationKey
            boolean dfAuthM = desfire.authenticate(MASTER_APPLICATION_KEY, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
            writeToUiAppend(output, "authMasterApplicationResult: " + dfAuthM);
            */
            writeToUiAppend(output, "changing the key number " + String.format("0x%02X", changeKeyNumber) + "(= " + changeKeyName + "access key)");
            // step 1 select the target application
            /*
            boolean selectApplication = desfire.selectApplication(applicationId);
            writeToUiAppend(output, "selectApplicationResult: " + selectApplication);
            if (!selectApplication) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "error on select application, aborted", COLOR_RED);
                return false;
            }
             */
            // step 2 authenticate with the application master key
            // we do need an authentication to change a key with the application master key = 0x00
            boolean authApp = desfire.authenticate(applicationMasterKey, applicationMasterKeyNumber, KeyType.DES);
            writeToUiAppend(output, "authApplicationResult: " + authApp);
            if (!authApp) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "error on authenticate application, aborted", COLOR_RED);
                return false;
            }
            // step 3 change the key
            // this is the real key used without any keyVersion bits. The new key is automatically stripped off the version bytes but not the old key
            boolean changeKey = desfire.changeKey(changeKeyNumber, KeyType.DES, changeKeyNew, changeKeyOld);
            writeToUiAppend(output, "changeKeyResult: " + changeKey);
            writeToUiAppend(output, "changeKeyResultCode: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
            writeToUiAppend(output, "finished");
            writeToUiAppend(output, "");
            if (changeKey) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "changeKey SUCCESS", COLOR_GREEN);
                return true;
            } else {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "changeKey NOT SUCCESS", COLOR_RED);
                writeToUiAppend(errorCode, "did you forget to authenticate with a master access key ?");
                return false;
            }
        } catch (IOException e) {
            writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
            writeToUiAppend(errorCode, "did you forget to authenticate with a master access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
            writeToUiAppend(errorCode, "did you forget to authenticate with a master access key ?");
            e.printStackTrace();
        }
        return false;
    }

    private boolean changeApplicationKeyAes(byte applicationMasterKeyNumber,
                                            byte[] applicationMasterKey, byte changeKeyNumber, byte[] changeKeyNew, byte[] changeKeyOld, String changeKeyName) {
        writeToUiAppend(output, "changeApplicationKeyAes: " + "for key number " + changeKeyNumber);
        // change key name e.g. master, read&write, car, read, write
        boolean result = false;
        try {
            /*
            // select master application
            boolean dfSelectM = desfire.selectApplication(MASTER_APPLICATION_IDENTIFIER);
            writeToUiAppend(output, "selectMasterApplicationResult: " + dfSelectM);

            // authenticate with MasterApplicationKey
            boolean dfAuthM = desfire.authenticate(MASTER_APPLICATION_KEY, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
            writeToUiAppend(output, "authMasterApplicationResult: " + dfAuthM);
            */
            writeToUiAppend(output, "changing the key number " + String.format("0x%02X", changeKeyNumber) + " (= " + changeKeyName + "access key)");
            // step 1 select the target application
            /*
            boolean selectApplication = desfire.selectApplication(applicationId);
            writeToUiAppend(output, "selectApplicationResult: " + selectApplication);
            if (!selectApplication) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "error on select application, aborted", COLOR_RED);
                return false;
            }
             */
            // step 2 authenticate with the application master key
            // we do need an authentication to change a key with the application master key = 0x00
            // todo change back because misconfiguration
            /*
            boolean authApp = desfire.authenticate(MASTER_APPLICATION_KEY_DEFAULT, MASTER_APPLICATION_KEY_NUMBER, KeyType.DES);
            writeToUiAppend(output, "authApplicationResult: " + authApp);
            if (!authApp) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "error on authenticate application, aborted", COLOR_RED);
                return false;
            } else {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticate application SUCCESS", COLOR_GREEN);
            }

             */
            // step 3 change the key
            // this is the real key used without any keyVersion bits. The new key is automatically stripped off the version bytes but not the old key
            boolean changeKey = desfire.changeKeyNoCheck(changeKeyNumber, KeyType.AES, changeKeyNew, changeKeyOld);
            writeToUiAppend(output, "changeKeyResult: " + changeKey);
            writeToUiAppend(output, "changeKeyResultCode: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
            writeToUiAppend(output, "finished");
            writeToUiAppend(output, "");
            if (changeKey) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "changeKey SUCCESS", COLOR_GREEN);
                return true;
            } else {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "changeKey NOT SUCCESS", COLOR_RED);
                writeToUiAppend(errorCode, "did you forget to authenticate with a master access key ?");
                return false;
            }
        } catch (IOException e) {
            writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "IOException: " + e.getMessage(), COLOR_RED);
            writeToUiAppend(errorCode, "did you forget to authenticate with a master access key ?");
            e.printStackTrace();
        } catch (Exception e) {
            writeToUiAppend(output, "Error with DESFireEV1 + " + e.getMessage());
            writeToUiAppendBorderColor(errorCode, errorCodeLayout, "Exception: " + e.getMessage(), COLOR_RED);
            writeToUiAppend(errorCode, "did you forget to authenticate with a master access key ?");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * section for general workflow
     */

    public String dumpVersionInfo(VersionInfoTest vi) {
        StringBuilder sb = new StringBuilder();
        sb.append("hardwareVendorId: ").append(vi.getHardwareVendorId()).append("\n");
        sb.append("hardwareType: ").append(vi.getHardwareType()).append("\n");
        sb.append("hardwareSubtype: ").append(vi.getHardwareSubtype()).append("\n");
        sb.append("hardwareVersionMajor: ").append(vi.getHardwareVersionMajor()).append("\n");
        sb.append("hardwareVersionMinor: ").append(vi.getHardwareVersionMinor()).append("\n");
        sb.append("hardwareStorageSize: ").append(vi.getHardwareStorageSize()).append("\n");

        sb.append("hardwareProtocol: ").append(vi.getHardwareProtocol()).append("\n");
        sb.append("softwareVendorId: ").append(vi.getSoftwareVendorId()).append("\n");
        sb.append("softwareType: ").append(vi.getSoftwareType()).append("\n");
        sb.append("softwareSubtype: ").append(vi.getSoftwareSubtype()).append("\n");

        sb.append("softwareVersionMajor: ").append(vi.getSoftwareVersionMajor()).append("\n");
        sb.append("softwareVersionMinor: ").append(vi.getSoftwareVersionMinor()).append("\n");
        sb.append("softwareStorageSize: ").append(vi.getSoftwareStorageSize()).append("\n");

        sb.append("softwareProtocol: ").append(vi.getSoftwareProtocol()).append("\n");
        sb.append("Uid: ").append(Utils.bytesToHex(vi.getUid())).append("\n");
        sb.append("batchNumber: ").append(Utils.bytesToHex(vi.getBatchNumber())).append("\n");
        sb.append("productionWeek: ").append(vi.getProductionWeek()).append("\n");
        sb.append("productionYear: ").append(vi.getProductionYear()).append("\n");
        sb.append("*** dump ended ***").append("\n");
        return sb.toString();
    }

    /**
     * section for authentication with DES
     */

    private boolean authenticateWithKeyDes(byte[] keyData, byte keyNumber) {
        writeToUiAppend(output, "authenticate with key number " + keyNumber + " " + Utils.printData("keyData", keyData));
        try {
            boolean dfAuthApp = desfire.authenticate(keyData, keyNumber, KeyType.DES);
            writeToUiAppend(output, "dfAuthApplicationResult: " + dfAuthApp);
            if (!dfAuthApp) {
                writeToUiAppendBorderColor(errorCode, errorCodeLayout, "authenticateApplication NOT Success, aborted", COLOR_RED);
                writeToUiAppend(errorCode, "authenticateApplication NOT Success: " + desfire.getCode() + ":" + String.format("0x%02X", desfire.getCode()) + ":" + desfire.getCodeDesc());
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
            //writeToUiAppend(output, "IOException: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // if verbose = true all steps are printed out
    private boolean authenticateApplicationDes(TextView logTextView, byte keyId, byte[] key, boolean verbose, byte[] response) {
        try {
            writeToUiAppend(logTextView, "authenticateApplicationDes for keyId " + keyId + " and key " + Utils.bytesToHex(key));
            // do DES auth
            //String getChallengeCommand = "901a0000010000";
            //String getChallengeCommand = "9084000000"; // IsoGetChallenge
            byte[] getChallengeResponse = isoDep.transceive(wrapMessage((byte) 0x1a, new byte[]{(byte) (keyId & 0xFF)}));
            if (verbose)
                writeToUiAppend(logTextView, printData("getChallengeResponse", getChallengeResponse));
            // cf5e0ee09862d90391af
            // 91 af at the end shows there is more data

            byte[] challenge = Arrays.copyOf(getChallengeResponse, getChallengeResponse.length - 2);
            if (verbose) writeToUiAppend(logTextView, printData("challengeResponse", challenge));

            // Of course the rndA shall be a random number,
            // but we will use a constant number to make the example easier.
            //byte[] rndA = Utils.hexStringToByteArray("0001020304050607");
            byte[] rndA = Ev3.getRndADes();
            if (verbose) writeToUiAppend(logTextView, printData("rndA", rndA));

            // This is the default key for a blank DESFire card.
            // defaultKey = 8 byte array = [0x00, ..., 0x00]
            //byte[] defaultDESKey = Utils.hexStringToByteArray("0000000000000000");
            byte[] defaultDESKey = key.clone();
            byte[] IV = new byte[8];

            // Decrypt the challenge with default keybyte[] rndB = decrypt(challenge, defaultDESKey, IV);
            byte[] rndB = Ev3.decrypt(challenge, defaultDESKey, IV);
            if (verbose) writeToUiAppend(logTextView, printData("rndB", rndB));
            // Rotate left the rndB byte[] leftRotatedRndB = rotateLeft(rndB);
            byte[] leftRotatedRndB = Ev3.rotateLeft(rndB);
            if (verbose)
                writeToUiAppend(logTextView, printData("leftRotatedRndB", leftRotatedRndB));
            // Concatenate the RndA and rotated RndB byte[] rndA_rndB = concatenate(rndA, leftRotatedRndB);
            byte[] rndA_rndB = Ev3.concatenate(rndA, leftRotatedRndB);
            if (verbose) writeToUiAppend(logTextView, printData("rndA_rndB", rndA_rndB));

            // Encrypt the bytes of the last step to get the challenge answer byte[] challengeAnswer = encrypt(rndA_rndB, defaultDESKey, IV);
            IV = challenge;
            byte[] challengeAnswer = Ev3.encrypt(rndA_rndB, defaultDESKey, IV);
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswer", challengeAnswer));

            IV = Arrays.copyOfRange(challengeAnswer, 8, 16);
                /*
                    Build and send APDU with the answer. Basically wrap the challenge answer in the APDU.
                    The total size of apdu (for this scenario) is 22 bytes:
                    > 0x90 0xAF 0x00 0x00 0x10 [16 bytes challenge answer] 0x00
                */
            byte[] challengeAnswerAPDU = new byte[22];
            challengeAnswerAPDU[0] = (byte) 0x90; // CLS
            challengeAnswerAPDU[1] = (byte) 0xAF; // INS
            challengeAnswerAPDU[2] = (byte) 0x00; // p1
            challengeAnswerAPDU[3] = (byte) 0x00; // p2
            challengeAnswerAPDU[4] = (byte) 0x10; // data length: 16 bytes
            challengeAnswerAPDU[challengeAnswerAPDU.length - 1] = (byte) 0x00;
            System.arraycopy(challengeAnswer, 0, challengeAnswerAPDU, 5, challengeAnswer.length);
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswerAPDU", challengeAnswerAPDU));

            /*
             * Sending the APDU containing the challenge answer.
             * It is expected to be return 10 bytes [rndA from the Card] + 9100
             */
            byte[] challengeAnswerResponse = isoDep.transceive(challengeAnswerAPDU);
            // response = channel.transmit(new CommandAPDU(challengeAnswerAPDU));
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswerResponse", challengeAnswerResponse));
            byte[] challengeAnswerResp = Arrays.copyOf(challengeAnswerResponse, getChallengeResponse.length - 2);
            if (verbose)
                writeToUiAppend(logTextView, printData("challengeAnswerResp", challengeAnswerResp));

            /*
             * At this point, the challenge was processed by the card. The card decrypted the
             * rndA rotated it and sent it back.
             * Now we need to check if the RndA sent by the Card is valid.
             */// encrypted rndA from Card, returned in the last step byte[] encryptedRndAFromCard = response.getData();

            // Decrypt the rnd received from the Card.byte[] rotatedRndAFromCard = decrypt(encryptedRndAFromCard, defaultDESKey, IV);
            //byte[] rotatedRndAFromCard = decrypt(encryptedRndAFromCard, defaultDESKey, IV);
            byte[] rotatedRndAFromCard = Ev3.decrypt(challengeAnswerResp, defaultDESKey, IV);
            if (verbose)
                writeToUiAppend(logTextView, printData("rotatedRndAFromCard", rotatedRndAFromCard));

            // As the card rotated left the rndA,// we shall un-rotate the bytes in order to get compare it to our original rndA.byte[] rndAFromCard = rotateRight(rotatedRndAFromCard);
            byte[] rndAFromCard = Ev3.rotateRight(rotatedRndAFromCard);
            if (verbose) writeToUiAppend(logTextView, printData("rndAFromCard", rndAFromCard));
            writeToUiAppend(logTextView, "********** AUTH RESULT **********");
            //System.arraycopy(createApplicationResponse, 0, response, 0, createApplicationResponse.length);
            if (Arrays.equals(rndA, rndAFromCard)) {
                writeToUiAppend(logTextView, "Authenticated");
                response = new byte[]{(byte) 0x91, (byte) 0x00};
                return true;
            } else {
                writeToUiAppend(logTextView, "Authentication failed");
                response = new byte[]{(byte) 0x91, (byte) 0xFF};
                return false;
                //System.err.println(" ### Authentication failed. ### ");
                //log("rndA:" + toHexString(rndA) + ", rndA from Card: " + toHexString(rndAFromCard));
            }
            //writeToUiAppend(logTextView, "********** AUTH RESULT END **********");
            //return false;
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "authenticateApplicationDes transceive failed: " + e.getMessage());
            writeToUiAppend(logTextView, "authenticateApplicationDes transceive failed: " + Arrays.toString(e.getStackTrace()));
        }
        //System.arraycopy(createApplicationResponse, 0, response, 0, createApplicationResponse.length);
        return false;
    }

    /**
     * section for application handling
     */

    private List<byte[]> getApplicationIdsList(TextView logTextView, byte[] response) {
        // get application ids
        List<byte[]> applicationIdList = new ArrayList<>();
        byte getApplicationIdsCommand = (byte) 0x6a;
        byte[] getApplicationIdsResponse = new byte[0];
        try {
            getApplicationIdsResponse = isoDep.transceive(wrapMessage(getApplicationIdsCommand, null));
        } catch (Exception e) {
            //throw new RuntimeException(e);
            writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
            return null;
        }
        writeToUiAppend(logTextView, printData("getApplicationIdsResponse", getApplicationIdsResponse));
        // getApplicationIdsResponse length: 2 data: 9100 = no applications on card
        // getApplicationIdsResponse length: 5 data: a1a2a3 9100
        // there might be more application on the card that fit into one frame:
        // getApplicationIdsResponse length: 5 data: a1a2a3 91AF
        // AF at the end is indicating more data

        // check that result if 0x9100 (success) or 0x91AF (success but more data)
        if ((!checkResponse(getApplicationIdsResponse)) && (!checkResponseMoreData(getApplicationIdsResponse))) {
            // something got wrong (e.g. missing authentication ?)
            writeToUiAppend(logTextView, "there was an unexpected response");
            return null;
        }
        // if the read result is success 9100 we return the data received so far
        if (checkResponse(getApplicationIdsResponse)) {
            System.arraycopy(returnStatusBytes(getApplicationIdsResponse), 0, response, 0, 2);
            byte[] applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
            applicationIdList = divideArray(applicationListBytes, 3);
            return applicationIdList;
        }
        if (checkResponseMoreData(getApplicationIdsResponse)) {
            writeToUiAppend(logTextView, "getApplicationIdsList: we are asked to grab more data from the card");
            byte[] applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
            applicationIdList = divideArray(applicationListBytes, 3);
            byte getMoreDataCommand = (byte) 0xaf;
            boolean readMoreData = true;
            try {
                while (readMoreData) {
                    try {
                        getApplicationIdsResponse = isoDep.transceive(wrapMessage(getMoreDataCommand, null));
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        writeToUiAppend(logTextView, "transceive failed: " + e.getMessage());
                        return null;
                    }
                    writeToUiAppend(logTextView, printData("getApplicationIdsResponse", getApplicationIdsResponse));
                    if (checkResponse(getApplicationIdsResponse)) {
                        // now we have received all data
                        List<byte[]> applicationIdListTemp = new ArrayList<>();
                        System.arraycopy(returnStatusBytes(getApplicationIdsResponse), 0, response, 0, 2);
                        applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
                        applicationIdListTemp = divideArray(applicationListBytes, 3);
                        readMoreData = false; // end the loop
                        applicationIdList.addAll(applicationIdListTemp);
                        return applicationIdList;
                    }
                    if (checkResponseMoreData(getApplicationIdsResponse)) {
                        // some more data will follow, store temp data
                        List<byte[]> applicationIdListTemp = new ArrayList<>();
                        applicationListBytes = Arrays.copyOf(getApplicationIdsResponse, getApplicationIdsResponse.length - 2);
                        applicationIdListTemp = divideArray(applicationListBytes, 3);
                        applicationIdList.addAll(applicationIdListTemp);
                        readMoreData = true;
                    }
                } // while (readMoreData) {
            } catch (Exception e) {
                writeToUiAppend(logTextView, "Exception failure: " + e.getMessage());
            } // try
        }
        return null;
    }

    /**
     * section for command and response handling
     */

    private byte[] wrapMessage(byte command, byte[] parameters) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write((byte) 0x90);
        stream.write(command);
        stream.write((byte) 0x00);
        stream.write((byte) 0x00);
        if (parameters != null) {
            stream.write((byte) parameters.length);
            stream.write(parameters);
        }
        stream.write((byte) 0x00);
        return stream.toByteArray();
    }

    private byte[] returnStatusBytes(byte[] data) {
        return Arrays.copyOfRange(data, (data.length - 2), data.length);
    }

    /**
     * checks if the response has an 0x'9100' at the end means success
     * and the method returns the data without 0x'9100' at the end
     * if any other trailing bytes show up the method returns false
     *
     * @param data
     * @return
     */
    private boolean checkResponse(@NonNull byte[] data) {
        // simple sanity check
        if (data.length < 2) {
            return false;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status == 0x9100) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * checks if the response has an 0x'91AF' at the end means success
     * but there are more data frames available
     * if any other trailing bytes show up the method returns false
     *
     * @param data
     * @return
     */
    private boolean checkResponseMoreData(@NonNull byte[] data) {
        // simple sanity check
        if (data.length < 2) {
            return false;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status == 0x91AF) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * checks if the response has an 0x'91de' at the end means the data
     * element is already existing
     * if any other trailing bytes show up the method returns false
     *
     * @param data
     * @return true is code is 91DE
     */
    private boolean checkDuplicateError(@NonNull byte[] data) {
        // simple sanity check
        if (data.length < 2) {
            return false;
        } // not ok
        int status = ((0xff & data[data.length - 2]) << 8) | (0xff & data[data.length - 1]);
        if (status != 0x91DE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * splits a byte array in chunks
     *
     * @param source
     * @param chunksize
     * @return a List<byte[]> with sets of chunksize
     */
    private static List<byte[]> divideArray(byte[] source, int chunksize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }
        return result;
    }

    /**
     * section for helper methods
     */

    private void vibrateShort() {
        // Make a Sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50, 10));
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
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
                /*
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(),
                            "NFC tag is IsoDep compatible",
                            Toast.LENGTH_SHORT).show();
                });
                 */

                // Make a Sound
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, 10));
                } else {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(200);
                }

                runOnUiThread(() -> {
                    output.setText("");
                    //output.setBackgroundColor(getResources().getColor(R.color.white));
                });
                isoDep.connect();
                // get tag ID
                tagIdByte = tag.getId();
                writeToUiAppend(output, "tag id: " + Utils.bytesToHex(tagIdByte));
                writeToUiAppend(output, "NFC tag connected");
                IsoDepWrapper isoDepWrapper = new DefaultIsoDepWrapper(isoDep);
                desFireAdapter = new DESFireAdapter(isoDepWrapper, true);
                desfire = new DESFireEV1();
                desfire.setAdapter(desFireAdapter);

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
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
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
     * section for layout handling
     */
    private void allLayoutsInvisible() {
        // todo change this
        //llApplicationHandling.setVisibility(View.GONE);
        //llStandardFile.setVisibility(View.GONE);
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

    public String printData(String dataName, byte[] data) {
        int dataLength;
        String dataString = "";
        if (data == null) {
            dataLength = 0;
            dataString = "IS NULL";
        } else {
            dataLength = data.length;
            dataString = Utils.bytesToHex(data);
        }
        StringBuilder sb = new StringBuilder();
        sb
                .append(dataName)
                .append(" length: ")
                .append(dataLength)
                .append(" data: ")
                .append(dataString);
        return sb.toString();
    }

    private void clearOutputFields() {
        output.setText("");
        errorCode.setText("");
        // reset the border color to primary for errorCode
        int color = R.color.colorPrimary;
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
                color
        };
        ColorStateList myColorList = new ColorStateList(states, colors);
        errorCodeLayout.setBoxStrokeColorStateList(myColorList);
    }

    /**
     * section for options menu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mApplications = menu.findItem(R.id.action_applications);
        mApplications.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                allLayoutsInvisible();
                llApplicationHandling.setVisibility(View.VISIBLE);
                return false;
            }
        });

        MenuItem mStandardFile = menu.findItem(R.id.action_standard_file);
        mStandardFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                allLayoutsInvisible();
                llStandardFile.setVisibility(View.VISIBLE);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}