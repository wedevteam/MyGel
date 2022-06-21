package com.wedev.mygel.functions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.wedev.mygel.R;

public class Connessione implements ServiceConnection, SerialListener{
    private enum Connected { False, Pending, True }

    private Context ctx;
    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    public String dataReceived="";


    public Connessione(Context ctx, String deviceAddress) {
        this.deviceAddress = deviceAddress;
        this.ctx = ctx;
    }

    public void destroy(){
        if (connected != Connected.False)
            disconnect();
        ctx.stopService(new Intent(ctx, SerialService.class));
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }
    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(ctx, "Non connesso", Toast.LENGTH_SHORT).show();
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
            spn.setSpan(new ForegroundColorSpan(ctx.getResources().getColor(R.color.white)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
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
            receiveText.append("X");
        }
    }
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

    }
    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
    @Override
    public void onSerialConnect() {

    }
    @Override
    public void onSerialConnectError(Exception e) {

    }
    @Override
    public void onSerialRead(byte[] data) {

    }
    @Override
    public void onSerialIoError(Exception e) {

    }
}
