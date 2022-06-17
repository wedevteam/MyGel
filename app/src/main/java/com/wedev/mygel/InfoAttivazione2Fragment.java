package com.wedev.mygel;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class InfoAttivazione2Fragment extends Fragment {

    private boolean isPermissed = false;
    Button impostazioni;
    Button associa;
    TextView risultati;
    Context ctx;
    View view;
    private final String prename = "GEL_";
    private enum ScanState {NONE, LESCAN, DISCOVERY, DISCOVERY_FINISHED}
    private ScanState scanState = ScanState.NONE;
    private static final long LESCAN_PERIOD = 10000; // similar to bluetoothAdapter.startDiscovery
    private Handler leScanStopHandler = new Handler();
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BroadcastReceiver discoveryBroadcastReceiver;
    private IntentFilter discoveryIntentFilter;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    private enum Connected {False, Pending, True}

    private InfoAttivazione2Fragment.Connected connected = InfoAttivazione2Fragment.Connected.False;
    private ArrayList<BluetoothDevice> listItems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;
    IntentFilter filter;
    int REQUEST_ENABLE_BT = 100;
    public InfoAttivazione2Fragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getContext();
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ctx.registerReceiver(receiver, filter);
    }
    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC && getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateScan(device));
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
            if (intent.getAction().equals((BluetoothAdapter.ACTION_DISCOVERY_FINISHED))) {
                scanState = ScanState.DISCOVERY_FINISHED; // don't cancel again
                stopScan();
            }
        }
    };

    private void updateScan(BluetoothDevice device) {
        if (scanState == ScanState.NONE)
            return;
        if (listItems.indexOf(device) < 0) {
            if (device.getName() != null) {
                if (!device.getName().isEmpty() && device.getName().substring(0, 4).equals(prename) || device.getName().substring(0, 4).equals("Test")) {
                    listItems.add(device);
                    Collections.sort(listItems, InfoAttivazione2Fragment::compareTo);
                    risultati.setText(risultati.getText().toString()+"\n"+device.getName()+" "+ device.getAddress()+"\n");
                   // listAdapter.notifyDataSetChanged();
                }
            }
        }
    }
    static int compareTo(BluetoothDevice a, BluetoothDevice b) {
        boolean aValid = a.getName() != null && !a.getName().isEmpty();
        boolean bValid = b.getName() != null && !b.getName().isEmpty();
        if (aValid && bValid) {
            int ret = a.getName().compareTo(b.getName());
            if (ret != 0) return ret;
            return a.getAddress().compareTo(b.getAddress());
        }
        if (aValid) return -1;
        if (bValid) return +1;
        return a.getAddress().compareTo(b.getAddress());
    }
    private void stopScan() {
        if (scanState == ScanState.NONE)
            return;

        switch (scanState) {

            case DISCOVERY:
                bluetoothAdapter.cancelDiscovery();
                break;
            default:
                // already canceled
        }
        scanState = ScanState.NONE;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        ctx.unregisterReceiver(receiver);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_attivazione2, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        setUI();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            verifyPerms2();
        }else
        verifyPerms();
    }
    private void verifyPerms() {
        Dexter.withContext(ctx).withPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermissed=true;
                setUpBT();
            }
            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                showSettingsDialog();
            }
            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();

            }
        }).check();
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void verifyPerms2() {
        Dexter.withContext(ctx).withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN

        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()){
                    isPermissed=true;
                    setUpBT();
                }else{
                    showSettingsDialog();
                }
            }
            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).onSameThread()
                .check();



    }
    private void setUpBT() {
        bluetoothManager = ctx.getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            showNoBTDialog();
        }
    }
    private void showNoBTDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme);

        // below line is the title
        // for our alert dialog.
        builder.setTitle("Bluetooth non supportato");
        builder.setCancelable(false);
        // below line is our message for our dialog
        builder.setMessage("Questo device non supporta il bluetooth. L'app non potrà connettersi all'apparato GEL.");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called on click on positive
                // button and on clicking shit button we
                // are redirecting our user from our app to the
                // settings page of our app.
                dialog.cancel();
                Navigation.findNavController(view).navigate(R.id.homeFragment);
            }
        });

        // below line is used
        // to display our dialog
        builder.show();
    }
    private void showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.AlertDialogTheme);

        // below line is the title
        // for our alert dialog.
        builder.setTitle("Permesso necessario");
        builder.setCancelable(false);
        // below line is our message for our dialog
        builder.setMessage("L'app ha bisogno di questo permesso per connettersi all'apparato GEL.");
        builder.setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called on click on positive
                // button and on clicking shit button we
                // are redirecting our user from our app to the
                // settings page of our app.
                dialog.cancel();
                // below is the intent from which we
                // are redirecting our user.
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", ctx.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Non connettere", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called when
                // user click on negative button.
                dialog.cancel();
                Navigation.findNavController(view).navigate(R.id.homeFragment);
            }
        });
        // below line is used
        // to display our dialog
        builder.show();
    }
    private void setUI() {
        risultati =view.findViewById(R.id.risposta);
        impostazioni = view.findViewById(R.id.impostazioni);
        impostazioni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openWirelessSettings = new Intent("android.settings.WIRELESS_SETTINGS"); startActivity(openWirelessSettings);
            }
        });
        associa = view.findViewById(R.id.associa);
        associa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPermissed) showSettingsDialog();
                else{
                    enableBT();
                }
            }
        });
    }
    private void enableBT() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            findBTPairedDevices();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode<0){
            Toast.makeText(ctx, "Bluetooth abilitato", Toast.LENGTH_SHORT).show();
            findBTPairedDevices();
        }else{
            showNoBTDialog();
        }
    }
    private void findBTPairedDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                risultati.setText(risultati.getText().toString()+"\n"+deviceName+" "+ deviceHardwareAddress+"\n");
            }
        }
        bluetoothAdapter.startDiscovery();
    }
}