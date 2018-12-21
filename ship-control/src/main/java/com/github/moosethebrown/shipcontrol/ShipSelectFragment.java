package com.github.moosethebrown.shipcontrol;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.moosethebrown.shipcontrol.data.ShipViewModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A fragment representing a list of ship ids.
 */
public class ShipSelectFragment extends Fragment {

    private ArrayList<String> shipIds;
    private OnListFragmentInteractionListener listener;
    private ShipRecyclerViewAdapter adapter = null;

    public ShipSelectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ship_select_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            adapter = new ShipRecyclerViewAdapter(shipIds, listener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // get list of available ships and subscribe to its updates
        ShipViewModel viewModel = ViewModelProviders.of(getActivity()).get(ShipViewModel.class);
        adapter.setShipIds(viewModel.getAvailableShips());
        viewModel.getLastAnnounced().observe(this, (newShip) -> {
            adapter.setShipIds(viewModel.getAvailableShips());
        });
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(String shipId);
    }
}
