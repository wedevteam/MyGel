package com.wedev.mygel.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wedev.mygel.AssegnazioneNomeFragment;
import com.wedev.mygel.MainActivity;
import com.wedev.mygel.R;

import java.util.ArrayList;
import java.util.List;

public class SSIDSAdapter extends RecyclerView.Adapter<SSIDSAdapter.SSIDSViewHolder>{
    Context context;
    AssegnazioneNomeFragment fragment;
    private final List<String> elencoSSID ;

    public SSIDSAdapter(Context context, List<String> elencoSSID, AssegnazioneNomeFragment fragment) {
        this.context = context;
        this.elencoSSID = elencoSSID;
        this.fragment=fragment;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void change(){
        this.notifyDataSetChanged();
    }
    @NonNull
    @Override
    public SSIDSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SSIDSViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.contentssid,parent,false
                )
        );
    }
    @Override
    public void onBindViewHolder(@NonNull SSIDSViewHolder holder, int position) {
        holder.setData(elencoSSID.get(position),position);
    }

    @Override
    public int getItemCount() {
        return elencoSSID.size();
    }



    @SuppressWarnings({"FieldMayBeFinal", "deprecation"})
    class SSIDSViewHolder extends RecyclerView.ViewHolder {

        private TextView nome;
        RelativeLayout relativeLayout;

        SSIDSViewHolder(@NonNull View itemView) {
            super(itemView);
        // relativeLayout = itemView.findViewById(R.id.p1);
            nome = itemView.findViewById(R.id.nome);
        }
        @SuppressLint("UseCompatLoadingForColorStateLists")
        void setData(String boardingItem, int position){
            nome.setText(boardingItem);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.nomerete.setText(nome.getText().toString());
                }
            });
        }
    }
}
