package com.reja.chatapp.UI.Home.AddFriend;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.reja.chatapp.Adapter.ShowFriendAndSearchAdapter;
import com.reja.chatapp.Interface.Listener.OnAcceptClickListener;
import com.reja.chatapp.Interface.Listener.OnRejectClickListener;
import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.R;
import com.reja.chatapp.ViewModel.AddFriendViewModel;
import com.reja.chatapp.databinding.FragmentReceiveRequestBinding;
import com.reja.chatapp.databinding.FragmentSentRequestBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReceiveRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiveRequestFragment extends Fragment {
    private AddFriendViewModel viewModel;
    private ShowFriendAndSearchAdapter adapter;
    private FragmentReceiveRequestBinding binding;

    public ReceiveRequestFragment() {
        // Required empty public constructor
    }

    private ReceiveRequestFragment(AddFriendViewModel viewModel) {
        this.viewModel = viewModel;
    }


    public static ReceiveRequestFragment newInstance(AddFriendViewModel viewModel) {
        return new ReceiveRequestFragment(viewModel);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReceiveRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ShowFriendAndSearchAdapter(getContext(), new ArrayList<>()); // Initialize with empty list
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);
        showLoading();
        // Observe ViewModel data and update the adapter as needed
        viewModel.getReceiveRequestList().observe(getViewLifecycleOwner(), sentRequests -> {
            closeLoading();
            if(!sentRequests.isEmpty()){
                closeNoResultFound();
                adapter.updateData(sentRequests);
            }else{
                showNoResultFound();
                adapter.updateData(sentRequests);
            }
            adapter.notifyDataSetChanged();

        });

        viewModel.getUserActionLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    adapter.notifyDataSetChanged();
                }
            }
        });

        adapter.setOnAcceptClickListener(new OnAcceptClickListener() {
            @Override
            public void onAcceptClick(FriendRequest request) {
                viewModel.acceptFriendRequest(request);
            }
        });

        adapter.setOnRejectClickListener(new OnRejectClickListener() {
            @Override
            public void onRejectClick(FriendRequest friendRequest) {
                viewModel.rejectFriendRequest(friendRequest);
            }
        });
    }

    private void showNoResultFound(){
        View view = binding.noResultLayout;
        RelativeLayout mainLayout = view.findViewById(R.id.mainLayout);
        ImageView icon = view.findViewById(R.id.search_icon);
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        TextView startConvoBtn = view.findViewById(R.id.startConvoBtn);

        mainLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        icon.setImageResource(R.drawable.add_group_icon);
        title.setText("No Friend Requests Received Yet!");
        description.setText("You haven't received any friend requests yet. Don't worry, it's easy to get noticed! Start by sending requests to others !");
        startConvoBtn.setVisibility(View.GONE);

        binding.noResultLayout.setVisibility(View.VISIBLE);
    }
    private void closeNoResultFound(){
        binding.noResultLayout.setVisibility(View.GONE);
    }

    private void showLoading(){
        binding.loadingLayout.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.INVISIBLE);
        closeNoResultFound();
    }
    private void closeLoading(){
        binding.loadingLayout.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }
}