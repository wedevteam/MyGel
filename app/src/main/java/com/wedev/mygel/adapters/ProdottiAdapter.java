package com.wedev.mygel.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.wedev.mygel.models.ModelProdotti;

import java.util.List;

public class ProdottiAdapter extends RecyclerView.Adapter<ProdottiAdapter.ProdottiViewHolder>{
    Context context;

    private final List<ModelProdotti> elencoProdotti ;

    public ProdottiAdapter(Context context, List<ModelProdotti> elencoProdotti) {
        this.context = context;
        this.elencoProdotti = elencoProdotti;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void change(){
        this.notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ProdottiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProdottiViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.contentprodotti,parent,false
                )
        );
    }
    @Override
    public void onBindViewHolder(@NonNull ProdottiViewHolder holder, int position) {
        holder.setData(elencoProdotti.get(position),position);
    }

    @Override
    public int getItemCount() {
        return elencoProdotti.size();
    }



    @SuppressWarnings({"FieldMayBeFinal", "deprecation"})
    class ProdottiViewHolder extends RecyclerView.ViewHolder {

        private TextView nome;
        RelativeLayout relativeLayout;

        ProdottiViewHolder(@NonNull View itemView) {
            super(itemView);
        // relativeLayout = itemView.findViewById(R.id.p1);
            nome = itemView.findViewById(R.id.nome);
        }
        @SuppressLint("UseCompatLoadingForColorStateLists")
        void setData(ModelProdotti boardingItem, int position){
            nome.setText(boardingItem.getNome());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("idProdotto",boardingItem.getIdServer());
                    context.startActivity(intent);
                }
            });
        }
    }
}
