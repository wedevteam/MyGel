package com.wedev.mygel;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TMain;
import com.wedev.mygel.functions.RestFunctions;
import com.wedev.mygel.functions.RestParams;
import com.wedev.mygel.functions.SerialListener;
import com.wedev.mygel.functions.SerialService;
import com.wedev.mygel.functions.SerialSocket;
import com.wedev.mygel.functions.TextUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConnessioneFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean initialStart2 = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;


    int CMDHELLO=1;
    int CMDPW=2;
    int CMDQUIT=3;
    int cmdState=0;

    Context ctx;
    ArrayList<String>  listaSSID = new ArrayList<>();
    RelativeLayout listaReti;
    RetiAdapter retiAdapter;

    RecyclerView elencoReti;
    String nomescelto="";

    Button avviaRicerca,esci,impostazioni,inviapw;
    View view;

    TextView titolo,titoloreti;
    EditText pw;

    String jsonString="";

    // Rest
    RestFunctions rf;

    //DB
    TMain mainData;
    DB db;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getContext();

        rf = new RestFunctions();
        SharedPreferences prefs = getContext().getSharedPreferences("gel", 0);
        deviceAddress= prefs.getString("indirizzoDevice" , "GELDEVICE");
        nomescelto= prefs.getString("nomeDevice" , "GELDEVICE");
      //  deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(ctx, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();
        View view = inflater.inflate(R.layout.fragment_connessione, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.black)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        sendText = view.findViewById(R.id.send_text);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));
        listaReti = view.findViewById(R.id.layoutreti);
        inviapw = view.findViewById(R.id.inviapw);
        inviapw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    // /cmdSSID=xxx:PASS=xxx&
                    if (pw.getText().toString().isEmpty()){
                        Toast.makeText(ctx, "Inserire password", Toast.LENGTH_SHORT).show();
                    }else{
                        String cmd="/cmdSSID="+titoloreti.getText().toString()+":PASS="+pw.getText().toString()+"&";
                        String x = "1";
                        cmdState = CMDPW;
                        jsonString="";
                        send(cmd);
                        /*isSetPassword=true;
                        sendReceive= new SceltaProdottoFragment.SendReceive(socket);
                        sendReceive.start();
                        sendReceive.write(cmd.getBytes(StandardCharsets.UTF_8));*/
                    }

            }
        });
        titoloreti = view.findViewById(R.id.titoloreti);
        pw = view.findViewById(R.id.pw);
        elencoReti = view.findViewById(R.id.elencoSSID);
        listaReti = view.findViewById(R.id.layoutreti);
        titolo = view.findViewById(R.id.titolo);
        this.view = view;
        return view;
    }

    // ------------ DB -------------
    public void setDB() {
        db = DB.getInstance(getContext());
    }
    public TMain getMainData() {
        return (db.tMainDao().getNumber()==0) ? null : db.tMainDao().getAll().get(0) ;
    }
    // -----------------------------

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
          //  receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        if(hexEnabled) {
            receiveText.append(TextUtil.toHexString(data) + '\n');
        } else {
            String msg = new String(data);
            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    Editable edt = receiveText.getEditableText();
                    if (edt != null && edt.length() > 1)
                        edt.replace(edt.length() - 2, edt.length(), "");
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }
            receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
            jsonString += msg;
  //          receiveText.append("X");
            parseJson(jsonString);
        }
    }
    private void parseJson(String dataX) {
        if (cmdState == CMDPW){
            boolean pwAccettata = verifyCMD(dataX);
            if (!pwAccettata){
                Toast.makeText(ctx, "Password non accettata dal device", Toast.LENGTH_SHORT).show();
            }else{
                // Termina
                cmdState = CMDQUIT;
                send("/cmdQUIT&");
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
        }else
            if (cmdState == CMDQUIT){
                // VAI A CHIUSURA
                setServer();
            }else
                try {
                    JSONObject dati = new JSONObject(dataX);
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
                    setRecycler2();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.w("JSON ===============>>>>>>",dataX);
                }
    }
    private boolean verifyCMD(String dataX){
        return dataX.toLowerCase().startsWith("success");
    }
    private void setRecycler2() {
        retiAdapter = new RetiAdapter(view.getContext(), listaSSID);
        elencoReti.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false));
        elencoReti.setItemAnimator(new DefaultItemAnimator());
        elencoReti.setAdapter(retiAdapter);
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      //  receiveText.append(spn);
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
                Navigation.findNavController(view).navigate(R.id.attivazioneFragment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // TODO
        }
        // setStart(s);
        //5
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
        p.setValue(nomescelto);
        params.add(p);

        p = new RestParams();
        p.setName("Nome");
        p.setValue(nomescelto);
        params.add(p);


        p = new RestParams();
        p.setName("Source");
        p.setValue("android");
        params.add(p);



        return params;
    }
    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
        if (initialStart2){
            initialStart2=false;
            cmdState = CMDHELLO;
            send("/hello");
        }

    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
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
        public RetiAdapter.RetiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ConnessioneFragment.RetiAdapter.RetiViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.contentssid,parent,false
                    )
            );
        }
        @Override
        public void onBindViewHolder(@NonNull ConnessioneFragment.RetiAdapter.RetiViewHolder holder, int position) {
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
}
