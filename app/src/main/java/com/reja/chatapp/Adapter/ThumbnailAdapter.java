package com.reja.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.pdf.PdfDocument;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.reja.chatapp.Interface.Listener.OnItemClickListener;
import com.reja.chatapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

//import com.shockwave.pdfium.PdfiumCore;
public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private static final int THUMBNAIL_SIZE = 100;
    private Context context;
    private List<Uri> mediaUris;
    private String type;
    private OnItemClickListener listener;
    private int selectedPosition;
    public ThumbnailAdapter(Context context, List<Uri> mediaUris, String type) {
        this.context = context;
        this.mediaUris = mediaUris;
        this.type = type;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Uri uri = mediaUris.get(position);
        if(mediaUris.size()<=1){
            holder.thumbnail.setVisibility(View.GONE);

        }else{
            holder.thumbnail.setVisibility(View.VISIBLE);

            Bitmap thumbnail = null;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
            if (type.equals("image")){
                Glide.with(context).load(uri).into(holder.imageView);
            }else if (type.equals("video")){
                Glide.with(context).asBitmap().load(uri).apply(RequestOptions.placeholderOf(R.drawable.video).centerCrop()).into(holder.imageView);

            }else if (type.equals("pdf")){
                Glide.with(context).asBitmap().load(uri).apply(RequestOptions.placeholderOf(R.drawable.pdf).centerCrop()).into(holder.imageView);
            }

            if(selectedPosition==position){
                holder.mask.setVisibility(View.VISIBLE);
            }else{
                holder.mask.setVisibility(View.GONE);
            }

        }

    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mediaUris.size();
    }

    private Bitmap getThumbnailFromUri(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getImageBitmap(Uri uri, int targetWidth, int targetHeight) {
        try {
            if (uri != null) {
                String imagePath = uri.getPath();
                if (imagePath != null) {
                    // Decode the image bitmap
                    Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
                    if (originalBitmap != null) {
                        // Resize the bitmap
                        return ThumbnailUtils.extractThumbnail(originalBitmap, targetWidth, targetHeight);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if there was an error or if the Uri is invalid
    }


    private Bitmap getVideoBitmap(Uri uri, int targetWidth, int targetHeight) {
        try {
            if (uri != null) {
                String[] filePathColumn = {MediaStore.Video.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String videoPath = cursor.getString(columnIndex);
                    cursor.close();

                    if (videoPath != null && !videoPath.isEmpty()) {
                        Bitmap originalBitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                        if (originalBitmap != null) {
                            float scale = Math.min((float) targetWidth / originalBitmap.getWidth(), (float) targetHeight / originalBitmap.getHeight());
                            Matrix matrix = new Matrix();
                            matrix.postScale(scale, scale);
                            return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


//    private Bitmap getPdfBitMap(Uri uri) {
//        try {
//            PdfiumCore pdfiumCore = new PdfiumCore(context);
//            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
//            com.shockwave.pdfium.PdfDocument pdfDocument = pdfiumCore.newDocument(fileDescriptor);
//            pdfiumCore.openPage(pdfDocument, 0);
//
//            int width = pdfiumCore.getPageWidthPoint(pdfDocument, 0);
//            int height = pdfiumCore.getPageHeightPoint(pdfDocument, 0);
//
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, 0, 0,0, width, height);
//
//            pdfiumCore.closeDocument(pdfDocument);
//            return bitmap;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }



    public static class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View mask;
        CardView thumbnail;

        public ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.thumbnail_image);
            mask = itemView.findViewById(R.id.mask);
            thumbnail = itemView.findViewById(R.id.thumbnail);
        }
    }
}

