package com.reja.chatapp.Adapter;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.reja.chatapp.R;
import com.reja.chatapp.databinding.ItemImageBinding;
import com.reja.chatapp.databinding.ItemPdfBinding;
import com.reja.chatapp.databinding.ItemVideoBinding;

import java.util.List;
import java.util.Objects;

public class MediaPagerAdapter extends PagerAdapter {
    private Context context;
    private Activity activity;
    private List<Uri> mediaUris;
    private LayoutInflater layoutInflater;
    private String itemType;
    public MediaPagerAdapter(Context context,Activity activity, List<Uri> mediaUris,String itemType) {
        this.context = context;
        this.activity = activity;
        this.mediaUris = mediaUris;
        this.layoutInflater = LayoutInflater.from(context);
        this.itemType = itemType;
    }

    @Override
    public int getCount() {
        return mediaUris.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(getItemLayout(), container, false);
        container.addView(itemView);

        switch (itemType) {
            case "image":
                bindImage(itemView, mediaUris.get(position));
                break;
            case "video":
                bindVideo(itemView, mediaUris.get(position));
                break;
            case "pdf":
                bindPdf(itemView, mediaUris.get(position));
                break;
            default:
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
                break;
        }

        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private int getItemLayout() {

        switch (itemType) {
            case "image":
                return R.layout.item_image;
            case "video":
                return R.layout.item_video;
            case "pdf":
                return R.layout.item_pdf;
            default:
                return 0;
        }
    }

    private void bindImage(View itemView, Uri uri) {
        ItemImageBinding binding = ItemImageBinding.bind(itemView);
        //RelativeLayout root = binding.getRoot();
        Log.d("Camera Image", uri.toString());
       binding.imageView.setImage(ImageSource.uri(uri));
    }

    private void bindVideo(View itemView, Uri uri) {
       ItemVideoBinding binding = ItemVideoBinding.bind(itemView);

       MediaController mediaController = new MediaController(activity);
       mediaController.setAnchorView(binding.videoView);
       binding.videoView.setMediaController(mediaController);

       binding.videoView.setVideoURI(uri);
       binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
           @Override
           public void onPrepared(MediaPlayer mediaPlayer) {
               binding.videoView.start();
           }
       });

    }

    private void bindPdf(View itemView, Uri uri) {
        ItemPdfBinding binding = ItemPdfBinding.bind(itemView);
        binding.pdfView.fromUri(uri)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(activity))
                .spacing(5)
                .load();

    }
}

