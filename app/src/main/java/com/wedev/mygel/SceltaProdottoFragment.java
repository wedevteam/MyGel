package com.wedev.mygel;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.wedev.mygel.adapters.SSIDSAdapter;
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TMain;
import com.wedev.mygel.functions.RestFunctions;
import com.wedev.mygel.functions.RestParams;
import com.wedev.mygel.functions.SerialListener;
import com.wedev.mygel.functions.SerialService;
import com.wedev.mygel.functions.TextUtil;
import com.wedev.mygel.models.ModelDevices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SceltaProdottoFragment extends Fragment  {

    Button avviaRicerca,esci,impostazioni,inviapw;
    View view;
    RecyclerView elencoDevices,elencoReti;
    BTAdapter btAdapter;
    RetiAdapter retiAdapter;
    Context ctx;
    CardView cardmessaggio;
    ProgressDialog progressBar;
    TextView titolo,titoloreti;
    RelativeLayout listaReti;
    EditText pw;

    // BT
    ArrayList<BluetoothDevice> listaDiscoveredDevices = new ArrayList<>();
    ArrayList<ModelDevices>  listaRifDevices = new ArrayList<>();
    ArrayList<String>  listaSSID = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_NOTCCONNECTED = 4;
    static final int STATE_MESSAGERECEIVEDHELLO = 5;
    static final int STATE_MESSAGERECEIVEDSEND =6;
    private static final String APPNAME = "MYGEL";
    private static final UUID MYUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    boolean isAuthorized = false;
    boolean isRequestFromAvvia = false;
    PermissionToken tokenPermesso=null;
    BluetoothDevice deviceDaAccoppiare=null;
    SendReceive sendReceive ;
    BluetoothSocket socket;
    String stringaJson = "";
    boolean isSetPassword = false;
    String nomeScelto="";
    String nomedaattivare="";

    boolean isSocketConnected = false;
    int CMD_HELLO = 1;
    int CMD_PW = 2;
    int CMD_QUIT = 3;

    // Rest
    RestFunctions rf;

    //DB
    TMain mainData;
    DB db;

    public SceltaProdottoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rf = new RestFunctions();
        ctx = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scelta_prodotto, container, false);
    }
    // ------------ DB -------------
    public void setDB() {
        db = DB.getInstance(getContext());
    }
    public TMain getMainData() {
        return (db.tMainDao().getNumber()==0) ? null : db.tMainDao().getAll().get(0) ;
    }
    // -----------------------------

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        setUI();
        setPermessi();
        setRecycler();
        avviaRicerca.performClick();
    }
    private void setRecycler() {
        btAdapter = new BTAdapter(view.getContext(), listaRifDevices);
        elencoDevices.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false));
        elencoDevices.setItemAnimator(new DefaultItemAnimator());
        elencoDevices.setAdapter(btAdapter);
    }
    private void setRecycler2() {
        retiAdapter = new RetiAdapter(view.getContext(), listaSSID);
        elencoReti.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false));
        elencoReti.setItemAnimator(new DefaultItemAnimator());
        elencoReti.setAdapter(retiAdapter);
    }
    private void setPermessi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            verifyPerms2();
        }else
            verifyPerms();
    }
    private void setUI() {
        inviapw = view.findViewById(R.id.inviapw);
        inviapw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (socket!=null){
                    // /cmdSSID=xxx:PASS=xxx&
                    if (pw.getText().toString().isEmpty()){
                        Toast.makeText(ctx, "Inserire password", Toast.LENGTH_SHORT).show();
                    }else{
                        String cmd="/cmdcmdSSID="+titoloreti+":PASS="+pw.getText().toString()+"&";
                        isSetPassword=true;
                        sendReceive= new SendReceive(socket);
                        sendReceive.start();
                        sendReceive.write(cmd.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        });
        titoloreti = view.findViewById(R.id.titoloreti);
        pw = view.findViewById(R.id.pw);
        elencoReti = view.findViewById(R.id.elencoSSID);
        listaReti = view.findViewById(R.id.layoutreti);
        titolo = view.findViewById(R.id.titolo);
        elencoDevices = view.findViewById(R.id.elencoBT);
        avviaRicerca = view.findViewById(R.id.avviaricerca);
        avviaRicerca.setVisibility(View.VISIBLE);
        avviaRicerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (avviaRicerca.getText().toString().startsWith("avvia")){
                    if (!isAuthorized) {
                        isRequestFromAvvia=true;
                        setPermessi();
                    }

                    else{
                        CountDownTimer countDownTimer = new CountDownTimer(12000,1000) {
                            @Override
                            public void onTick(long l) {
                                avviaRicerca.setText("RICERCA IN CORSO...");
                            }
                            @Override
                            public void onFinish() {
                                bluetoothAdapter.cancelDiscovery();
                                avviaRicerca.setVisibility(View.GONE);
                                avviaRicerca.setText("avvia ricerca");
                            }
                        }.start();

                        startDiscover();
                    }
                }else{
                    stopDiscover();
                }

            }
        });
        if (!isAuthorized) avviaRicerca.setVisibility(View.GONE);
        cardmessaggio = view.findViewById(R.id.cardmessaggio);
        cardmessaggio.setVisibility(View.GONE);
        esci = view.findViewById(R.id.esci);
        esci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardmessaggio.setVisibility(View.GONE);
                Navigation.findNavController(view).navigate(R.id.homeFragment);
            }
        });
        impostazioni = view.findViewById(R.id.impostazioni);
        impostazioni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tokenPermesso == null){
                    cardmessaggio.setVisibility(View.GONE);
                    avviaRicerca.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", ctx.getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 101);
                }else{
                    tokenPermesso.continuePermissionRequest();
                }

            }
        });
    }
    private void stopDiscover() {
    }
    private void startDiscover() {
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ctx.registerReceiver(broadcastReceiver,intentFilter);
        IntentFilter intentFilter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        ctx. registerReceiver(broadcastReceiver2,intentFilter2);
        bluetoothAdapter.startDiscovery();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        setDefault();
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(act)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                if (name!= null && name.startsWith("GEL_")){
                    if (listaRifDevices.size()==0){
                        aggiornaLista(device);
                    }else{
                        boolean trovato = false;
                        for (int i = 0; i < listaRifDevices.size(); i++) {
                            if (listaRifDevices.get(i).getNome().equals(device.getName())){
                                trovato = true;
                            }
                        }
                        if (!trovato){
                            aggiornaLista(device);
                        }
                    }
                }
            }
        }
    };


    BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(act)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (deviceDaAccoppiare!=null)
                    if (device.getName().equals(deviceDaAccoppiare.getName())){

                        /*if (device.getBondState() == BluetoothDevice.BOND_BONDING){
                            Toast.makeText(ctx, "INSERISCI CODICE => 1980", Toast.LENGTH_SHORT).show();
                        }*/
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                            if (progressBar!=null){
                                progressBar.dismiss();
                            }
                            Toast.makeText(ctx, "ASSOCIAZIONE EFFETTUATA", Toast.LENGTH_SHORT).show();
                            aggiornaLista2(device);
                        }
                        if (device.getBondState() == BluetoothDevice.BOND_NONE){
                            if (progressBar!=null){
                                progressBar.dismiss();
                            }
                            Toast.makeText(ctx, "NON ASSOCIATO", Toast.LENGTH_SHORT).show();
                        }
                    }
            }
        }
    };
    private void aggiornaLista(BluetoothDevice device){
        listaDiscoveredDevices.add(device);
        ModelDevices modelDevice = new ModelDevices();
        modelDevice.setMcaddress(device.getAddress());
        modelDevice.setNome(device.getName());
        modelDevice.setStato(device.getBondState());
        listaRifDevices.add(modelDevice);
        btAdapter.notifyItemInserted(listaRifDevices.size()-1);
        btAdapter.notifyDataSetChanged();
    }    
    private void aggiornaLista2(BluetoothDevice device){
        for (int i = 0; i < listaRifDevices.size(); i++) {
            if (listaRifDevices.get(i).getNome().equals(device.getName())){
                listaRifDevices.get(i).setStato(device.getBondState());
            }
        }


        btAdapter.notifyItemInserted(listaRifDevices.size()-1);
        btAdapter.notifyDataSetChanged();
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
                    isAuthorized=true;
                    avviaRicerca.setVisibility(View.VISIBLE);
                    if (isRequestFromAvvia){
                        isRequestFromAvvia=false;
                        startDiscover();
                        CountDownTimer countDownTimer = new CountDownTimer(12000,1000) {
                            @Override
                            public void onTick(long l) {
                                avviaRicerca.setText("RICERCA IN CORSO...");
                            }
                            @Override
                            public void onFinish() {
                                avviaRicerca.setText("avvia ricerca");
                                bluetoothAdapter.cancelDiscovery();
                            }
                        }.start();
                    }
                }else{
                   // showSettingsDialog();
                    isAuthorized=false;
                    cardmessaggio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
               /* tokenPermesso = permissionToken;
                cardmessaggio.setVisibility(View.VISIBLE);*/
                permissionToken.continuePermissionRequest();
            }
        })
                .check();

    }

    private void showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx,R.style.BTSCAN);

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

    private void verifyPerms() {
        Dexter.withContext(ctx).withPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isAuthorized=true;
                avviaRicerca.setVisibility(View.VISIBLE);
                if (isRequestFromAvvia){

                    isRequestFromAvvia=false;
                    startDiscover();
                    CountDownTimer countDownTimer = new CountDownTimer(12000,1000) {
                        @Override
                        public void onTick(long l) {
                            avviaRicerca.setText("RICERCA IN CORSO...");
                        }
                        @Override
                        public void onFinish() {
                            bluetoothAdapter.cancelDiscovery();
                            avviaRicerca.setText("avvia ricerca");
                        }
                    }.start();
                }
            }
            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                cardmessaggio.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();

            }
        }).check();
    }
    @Override
    public void onStop() {
        avviaRicerca.setVisibility(View.VISIBLE);
        super.onStop();
    }
    public void accoppia(String nome){
        BluetoothDevice device = null;
        for (int i = 0; i < listaDiscoveredDevices.size(); i++) {
                if (listaDiscoveredDevices.get(i).getName().equals(nome)){
                    device=listaDiscoveredDevices.get(i);
                }
        }
        if (device != null){
            deviceDaAccoppiare = device;
            progressBar = new ProgressDialog(ctx);
            progressBar.setTitle("attendere...");
            progressBar.show();
            device.createBond();
        }
    }

    public class BTAdapter extends RecyclerView.Adapter<BTAdapter.BTViewHolder>{
        Context context;
        AssegnazioneNomeFragment fragment;
        private final ArrayList<ModelDevices> elencoDevices ;

        public BTAdapter(Context context, ArrayList<ModelDevices> elencoDevices) {
            this.context = context;
            this.elencoDevices = elencoDevices;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void change(){
            this.notifyDataSetChanged();
        }
        @NonNull
        @Override
        public BTViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BTViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.contentprodottidisponibili,parent,false
                    )
            );
        }
        @Override
        public void onBindViewHolder(@NonNull BTAdapter.BTViewHolder holder, int position) {
            holder.setData(elencoDevices.get(position),position);
        }

        @Override
        public int getItemCount() {
            return elencoDevices.size();
        }



        @SuppressWarnings({"FieldMayBeFinal", "deprecation"})
        class BTViewHolder extends RecyclerView.ViewHolder {

            private TextView nome;
            private Button azione;

            BTViewHolder(@NonNull View itemView) {
                super(itemView);
                // relativeLayout = itemView.findViewById(R.id.p1);
                nome = itemView.findViewById(R.id.nome);
                azione = itemView.findViewById(R.id.azione);
            }
            @SuppressLint("UseCompatLoadingForColorStateLists")
            void setData(ModelDevices boardingItem, int position){

                if (boardingItem.getNome().startsWith("GEL_ABAB")){
                    nome.setText(String.format("%s \n(Acqua Click)", boardingItem.getNome()));
                }else{
                    nome.setText(boardingItem.getNome());
                }
                if (boardingItem.getStato()==  BluetoothDevice.BOND_BONDED){
                    azione.setText("ATTIVA");
                }
                if (boardingItem.getStato()==  BluetoothDevice.BOND_BONDING || boardingItem.getStato()==10){
                    azione.setText("ACCOPPIA");
                }

                azione.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if ( azione.getText().toString().equals("ACCOPPIA"))
                            accoppia(boardingItem.getNome());
                        if ( azione.getText().toString().equals("ATTIVA")){
                            try {
                                bluetoothAdapter.cancelDiscovery();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            nomedaattivare = boardingItem.getNome();
                            attiva(boardingItem.getNome());


                        }

                    }
                });
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ( azione.getText().toString().equals("ACCOPPIA"))
                            accoppia(boardingItem.getNome());
                        if ( azione.getText().toString().equals("ATTIVA")){
                            try {
                                bluetoothAdapter.cancelDiscovery();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            nomedaattivare = boardingItem.getNome();
                            attiva(boardingItem.getNome());

                        }
                    }
                });
            }
        }
    }

    public class RetiAdapter extends RecyclerView.Adapter<RetiAdapter.RetiViewHolder>{
        Context context;
        AssegnazioneNomeFragment fragment;
        private final ArrayList<String> elencoReti ;

        public RetiAdapter(Context context, ArrayList<String> elencoReti) {
            this.context = context;
            this.elencoReti = elencoReti;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void change(){
            this.notifyDataSetChanged();
        }
        @NonNull
        @Override
        public RetiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RetiViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.contentssid,parent,false
                    )
            );
        }
        @Override
        public void onBindViewHolder(@NonNull RetiAdapter.RetiViewHolder holder, int position) {
            holder.setData(elencoReti.get(position),position);
        }

        @Override
        public int getItemCount() {
            return elencoReti.size();
        }



        @SuppressWarnings({"FieldMayBeFinal", "deprecation"})
        class RetiViewHolder extends RecyclerView.ViewHolder {

            private TextView nome;
            private Button azione;

            RetiViewHolder(@NonNull View itemView) {
                super(itemView);
                // relativeLayout = itemView.findViewById(R.id.p1);
                nome = itemView.findViewById(R.id.nome);
                azione = itemView.findViewById(R.id.azione);
            }
            @SuppressLint("UseCompatLoadingForColorStateLists")
            void setData(String boardingItem, int position){
                nome.setText(boardingItem);
                azione.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pw.setVisibility(View.VISIBLE);
                        setElencoRetiInvisible();
                        titoloreti.setText(boardingItem);
                        inviapw.setVisibility(View.VISIBLE);
                        avviaRicerca.setVisibility(View.GONE);
                    }
                });
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // fragment.nomerete.setText(nome.getText().toString());
                    }
                });
            }
        }
    }
    public void setElencoRetiInvisible(){
        elencoReti.setVisibility(View.GONE);
    }
    public void attiva(String nome){

        // Stop il discovery
        try {
            bluetoothAdapter.cancelDiscovery();
        }catch (Exception e){

        }

        BluetoothDevice device = null;
        for (int i = 0; i < listaDiscoveredDevices.size(); i++) {
            if (listaDiscoveredDevices.get(i).getName().equals(nome)){
                device=listaDiscoveredDevices.get(i);
            }
        }

        if (device != null){
            SharedPreferences prefs = getContext().getSharedPreferences("gel", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("indirizzoDevice", device.getAddress());
            editor.putString("nomeDevice", device.getName());
            editor.apply();
            Navigation.findNavController(view).navigate(R.id.connessioneFragment);

            /*nomeScelto=nome;

            progressBar = new ProgressDialog(ctx);
            progressBar.setTitle("CONNESSIONE");
            progressBar.setMessage("Attendere....");
            progressBar.show();

            stringaJson="";


            if (connetti(device)){
                CountDownTimer countDownTimer = new CountDownTimer(2000,1000) {
                    @Override
                    public void onTick(long l) {
                        try{
                            progressBar.setTitle("CONNESSIONE");
                            progressBar.setMessage("Attendere2....");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFinish() {
                        sendReceive= new SendReceive(socket);
                        sendReceive.start();
                        sendReceive.write("/hello".getBytes(StandardCharsets.UTF_8));
                    }
                }.start();
              //  scrivi("/hello".getBytes(),CMD_HELLO);

            }*/
           /* ClientClass clientClass = new ClientClass(device);
            clientClass.start();*/
        }else{
            Toast.makeText(ctx, "Errore in fase di attivazione", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean connetti(BluetoothDevice device) {
        try {
            socket = device.createRfcommSocketToServiceRecord(MYUUID);
            socket.connect();
            isSocketConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
            if (progressBar!=null){
                progressBar.dismiss();
            }
            Toast.makeText(ctx, "Errore di connessione al device: ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void leggi(){}
    private void scrivi(byte[] bytes,int tipoComando){
        final OutputStream outputStream;
        OutputStream tempOUT = null;
        try {
            tempOUT=socket.getOutputStream();
        } catch (IOException e) {
            if (progressBar!=null){
                progressBar.dismiss();
            }
            e.printStackTrace();
        }
        outputStream = tempOUT;
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("WRITE ===============>>>>>>","boh");
            if (progressBar!=null){
                progressBar.dismiss();
            }
        }
    }

    private class ClientClass extends Thread{
        private BluetoothDevice device;

        public ClientClass(BluetoothDevice device1){
            device=device1;

            try {
                socket = device.createRfcommSocketToServiceRecord(MYUUID);

            } catch (IOException e) {
                e.printStackTrace();
                if (progressBar!=null){
                    progressBar.dismiss();
                }

            }
        }
        public void run(){
            try {

                socket.connect();

                sendReceive= new SendReceive(socket);
                sendReceive.start();
                sendReceive.write("/hello".getBytes(StandardCharsets.UTF_8));

            } catch (IOException e) {
                if (progressBar!=null){
                    progressBar.dismiss();
                }

            }
        }
    }

    private class SendReceive extends Thread{

        private final InputStream inputstream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket1){

            InputStream tempIN = null;
            OutputStream tempOUT = null;
            try {
                tempIN=socket.getInputStream();
                tempOUT=socket.getOutputStream();
            } catch (IOException e) {
                if (progressBar!=null){
                    progressBar.dismiss();
                }
                e.printStackTrace();
            }
            inputstream = tempIN;
            outputStream = tempOUT;

        }
        public void run(){
            try {

                int totalbytes = 0;
                byte[] buffer = new byte[512];
                int bytes;
                while((bytes = inputstream.read(buffer)) >0){
                    //bytes=inputstream.read(buffer);
                    totalbytes+=bytes;
                    String str = new String(buffer,0,bytes); // for UTF-8 encoding

                    stringaJson = String.format("%s%s", stringaJson, str);
                    if (str.isEmpty() || bytes<512) break;

                   /* byte[] readBytes = (byte[]) message.obj;
                    String tempMsg = new String(readBytes,0,message.arg1);
                    testo.setText(tempMsg);
                    handler.obtainMessage(STATE_MESSAGERECEIVED,bytes,-1,buffer).sendToTarget();*/
                }
                if (totalbytes>0){
                    handler.obtainMessage(STATE_MESSAGERECEIVEDHELLO,bytes,-1,buffer).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (progressBar!=null){
                    progressBar.dismiss();
                }
                if (!socket.isConnected()){
                    try {
                        attiva(nomedaattivare);
                    }catch(Exception e2){
                        e2.printStackTrace();
                    }
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
                if (isSetPassword){
                    CountDownTimer countDownTimer = new CountDownTimer(2000,1000) {
                        @Override
                        public void onTick(long l) {

                        }
                        @Override
                        public void onFinish() {
                            setServer();
                        }
                    }.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.w("WRITE ===============>>>>>>","boh");
                if (progressBar!=null){
                    progressBar.dismiss();
                }

            }
        }
    }
    private void setServer() {

        // =============================
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.joindevice),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        if (s == null) {
                            String t="v";
                            // TODO
                        } else {
                            String v = "1";
                            fillData(s);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "Connessione internet inesistente", Toast.LENGTH_SHORT).show();
                        String t="v";
                        // TODO
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
            @Override
            public byte[] getBody() {
                return rf.setJsonBody(params).toString() == null ? null : rf.setJsonBody(params).toString().getBytes(StandardCharsets.UTF_8);
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        int socketTimeout = 150000;//50 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // Richiesta
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }
    private void fillData(JSONObject s) {
        // TODO
        try {
            boolean success = s.getBoolean("success");
            if (!success){
                showError(s.getString("error_code"),s.getString("message"));
            }else{
                setDefault();
                Navigation.findNavController(view).navigate(R.id.attivazioneFragment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // TODO
        }
        // setStart(s);
        //5
    }
    private void setDefault() {
        avviaRicerca.setVisibility(View.VISIBLE);
        if (socket != null){
            if (socket.isConnected()){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        try {
            ctx.unregisterReceiver(broadcastReceiver);
            ctx.unregisterReceiver(broadcastReceiver2);
        }catch(Exception e){
            e.printStackTrace();
        }
        listaReti.setVisibility(View.GONE);
        titoloreti.setVisibility(View.GONE);
        pw.setVisibility(View.GONE);
        inviapw.setVisibility(View.GONE);
        try {
            if (bluetoothAdapter!= null){
                bluetoothAdapter.cancelDiscovery();
            }
        }catch (Exception e ){
            e.printStackTrace();
        }

    }
    private void showError(String error_code, String message) {
        hideSoftKeyBoard();
        showErrBase(" "+error_code,message);
    }

    public void showErrBase(String titolo, String messaggio){
        MaterialAlertDialogBuilder dialogo=  new MaterialAlertDialogBuilder(getContext(),R.style.AlertDialogTheme)
                .setTitle(titolo)
                .setMessage(messaggio)
                .setNeutralButton("Ok",null);
        dialogo.show();
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
    private ArrayList<RestParams> setParams() {
        ArrayList<RestParams> params = new ArrayList<>();

        RestParams p = new RestParams();
        p.setName("app_token");
        p.setValue(getString(R.string.tokenapp));
        params.add(p);

        p = new RestParams();
        p.setName("Token");
        p.setValue(mainData.getToken());
        params.add(p);

        p = new RestParams();
        p.setName("ProductId");
        p.setValue("1"); // acquaclick
        params.add(p);

        p = new RestParams();
        p.setName("SerialNumber");
        p.setValue(nomeScelto);
        params.add(p);

        p = new RestParams();
        p.setName("Nome");
        p.setValue(nomeScelto);
        params.add(p);


        p = new RestParams();
        p.setName("Source");
        p.setValue("android");
        params.add(p);



        return params;
    }
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){

                case STATE_MESSAGERECEIVEDHELLO:
                    if (progressBar!=null){
                        progressBar.dismiss();
                    }
                    Toast.makeText(ctx, stringaJson, Toast.LENGTH_SHORT).show();
                    Log.w("ERRORE JSON ===============> ",stringaJson);
                /*    byte[] readBytes = (byte[]) message.obj;
                    String tempMsg = new String(readBytes,0,message.arg1);
                    stringaJson = stringaJson+tempMsg;*/
                    try {
                        JSONObject dati = new JSONObject(stringaJson);
                        JSONObject response = dati.getJSONObject("response");
                        String nomeDevice = response.getString("message");
                        JSONObject data = response.getJSONObject("data");
                        JSONArray menu = data.getJSONArray("menu");
                        listaSSID = new ArrayList<>();
                        for (int i = 0; i < menu.length(); i++) {
                            JSONObject dato = menu.getJSONObject(i);
                            listaSSID.add(dato.getString("SSID"));
                        }
                        listaReti.setVisibility(View.VISIBLE);
                        listaReti.bringToFront();
                        avviaRicerca.setVisibility(View.GONE);
                        setRecycler2();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.w("JSON ===============>>>>>>",stringaJson);
                    }
                    break;

            }
            return true;
        }
    });
}