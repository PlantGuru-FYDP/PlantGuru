package com.example.plantguru.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantguru.R;
import com.example.plantguru.models.Plant;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<Plant> plantList;

    public PlantAdapter(List<Plant> plantList) {
        this.plantList = plantList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        holder.plantName.setText(plant.getPlantName());
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        TextView plantName;
        TextView plantStatus;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plant_name);
        }
    }
}