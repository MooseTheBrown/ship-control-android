package com.github.moosethebrown.shipcontrol;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.moosethebrown.shipcontrol.ShipSelectFragment.OnListFragmentInteractionListener;

import java.util.List;

public class ShipRecyclerViewAdapter extends RecyclerView.Adapter<ShipRecyclerViewAdapter.ViewHolder> {

    private List<String> shipIds;
    private final OnListFragmentInteractionListener listener;

    public ShipRecyclerViewAdapter(List<String> shipIds, OnListFragmentInteractionListener listener) {
        this.shipIds = shipIds;
        this.listener = listener;
    }

    public void setShipIds(List<String> ids) {
        if (ids != null) {
            this.shipIds = ids;
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_ship_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.shipId = shipIds.get(position);
        holder.contentView.setText(shipIds.get(position));

        holder.view.setOnClickListener(v -> {
            if (null != listener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                listener.onListFragmentInteraction(holder.shipId);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (shipIds != null) {
            return shipIds.size();
        }
        else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView contentView;
        public String shipId;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            contentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
