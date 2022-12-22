package com.devstree.project.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.devstree.project.R;
import com.devstree.project.listener.ItemClickListener;
import com.devstree.project.model.Place;

import java.util.ArrayList;

/**
 * Created by Jitendra on 23,November,2022
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.MyViewHolder> {
    private ArrayList<Place> placeArrayList;
    private Context context;
    public ItemClickListener itemClickListener;
    public class MyViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView txtCityName;
        AppCompatTextView txtPrimary;
        AppCompatTextView txtAddress;
        AppCompatTextView txtDistance;
        AppCompatImageView imgEdit;
        AppCompatImageView imgDelete;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.txtCityName = (AppCompatTextView) itemView.findViewById(R.id.txtCityName);
            this.txtPrimary = (AppCompatTextView) itemView.findViewById(R.id.txtPrimary);
            this.txtAddress = (AppCompatTextView) itemView.findViewById(R.id.txtAddress);
            this.txtDistance = (AppCompatTextView) itemView.findViewById(R.id.txtDistance);
            this.imgDelete = (AppCompatImageView) itemView.findViewById(R.id.imgDelete);
            this.imgEdit = (AppCompatImageView) itemView.findViewById(R.id.imgEdit);
        }
    }


    public void updateAll(ArrayList<Place> listData) {
        placeArrayList.clear();
        placeArrayList.addAll(listData);
        notifyDataSetChanged();
    }
    public PlaceAdapter(Context context, ArrayList<Place> placeArrayList,ItemClickListener itemClickListener) {
        this.context = context;
        this.placeArrayList = placeArrayList;
        this.itemClickListener=itemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_place_item, parent, false);


        PlaceAdapter.MyViewHolder myViewHolder = new PlaceAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.txtCityName.setText(placeArrayList.get(position).getCityName());
        holder.txtAddress.setText(placeArrayList.get(position).getPlaceName());
        try {
            holder.txtPrimary.setVisibility(placeArrayList.get(position).getDistnace() == 0.0 || placeArrayList.get(position).getId() == 1 ? View.VISIBLE : View.GONE);
            if (placeArrayList.get(position).getDistnace()==0.0||placeArrayList.get(position).getId()==1){
                holder.txtDistance.setVisibility(View.GONE);
            }else {
                holder.txtDistance.setVisibility(View.VISIBLE);
                holder.txtDistance.setText("Distance: "+placeArrayList.get(position).getDistnace()+" Km");
            }
        }catch (Exception e){
            Log.e("***Exception****","****txtPrimary****"+e.getLocalizedMessage());
            holder.txtDistance.setVisibility(View.GONE);
        }

        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.itemClicked(placeArrayList.get(position),position,"Edit");
            }
        });
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.itemClicked(placeArrayList.get(position),position,"Delete");
            }
        });
    }


    @Override
    public int getItemCount() {
        return placeArrayList.size();
    }

}
