package com.reja.chatapp.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.reja.chatapp.Model.MutualFriend;
import com.reja.chatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MutualFriendAdapter extends RecyclerView.Adapter<MutualFriendAdapter.LayoutHolder> {
    private Context context;
    private List<MutualFriend> mutualFriendList;



    public MutualFriendAdapter(Context context, List<MutualFriend> mutualFriendList) {
        this.context = context;
        this.mutualFriendList = mutualFriendList;
    }



    @NonNull
    @Override
    public MutualFriendAdapter.LayoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mutual_friend_item,parent,false);
        return new LayoutHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MutualFriendAdapter.LayoutHolder holder, int position) {
        MutualFriend mutualFriend = mutualFriendList.get(position);
         LayoutHolder viewHolder = ((LayoutHolder) holder);
         viewHolder.userName.setText(mutualFriend.getName());
         Glide.with(context).load(mutualFriend.getProfilePicture()).addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).apply(RequestOptions.placeholderOf(R.drawable.account).centerCrop())
                .into(viewHolder.profileImage);
    }

    @Override
    public int getItemCount() {
        return mutualFriendList.size();
    }

    static class LayoutHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userName;
        public LayoutHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
        }
    }
}
