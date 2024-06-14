package com.reja.chatapp.UI.Home.AddFriend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.reja.chatapp.Adapter.ShowFriendAndSearchAdapter;
import com.reja.chatapp.Interface.Listener.OnAcceptClickListener;
import com.reja.chatapp.Interface.Listener.OnCancelRequestClickListener;
import com.reja.chatapp.Interface.Listener.OnRejectClickListener;
import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.Model.SearchResult;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Home.HomeScreenActivity;
import com.reja.chatapp.ViewModel.AddFriendViewModel;
import com.reja.chatapp.databinding.FragmentSentRequestBinding;

import java.util.ArrayList;
import java.util.List;

public class SentRequestFragment extends Fragment  {

    private AddFriendViewModel viewModel;
    private ShowFriendAndSearchAdapter adapter;
    private FragmentSentRequestBinding binding;
    public SentRequestFragment(AddFriendViewModel viewModel) {
        // Required empty public constructor
        this.viewModel = viewModel;
    }

    public static SentRequestFragment newInstance(AddFriendViewModel viewModel) {
        return new SentRequestFragment(viewModel);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSentRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView and set the adapter
        adapter = new ShowFriendAndSearchAdapter(getContext(), new ArrayList<>()); // Initialize with empty list
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);
        showLoading();
        // Observe ViewModel data and update the adapter as needed
        viewModel.getSentRequestList().observe(getViewLifecycleOwner(), sentRequests -> {
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

        adapter.setOnCancelRequestClickListener(new OnCancelRequestClickListener() {
            @Override
            public void onCancelFriendRequest(FriendRequest friendRequest) {
                viewModel.cancelFriendRequest(friendRequest);
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        title.setText("No Friend Requests Sent Yet!");
        description.setText("It seems you haven't sent any friend requests yet. Start connecting with others by exploring profiles and sending requests to those who share your interests. Your next great conversation is just a click away!");
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
