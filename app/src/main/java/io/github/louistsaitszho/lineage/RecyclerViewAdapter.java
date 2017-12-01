package io.github.louistsaitszho.lineage;

import android.os.Environment;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import io.github.louistsaitszho.lineage.activities.MainActivity;
import io.github.louistsaitszho.lineage.model.Video;
import io.github.louistsaitszho.lineage.model.VideoDownloader;

/**
 * todo rename to sth like video adapter (we will have other adapter)
 * todo check this out: https://stackoverflow.com/a/26196831/2384934
 * Created by lsteamer on 15/09/2017.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    //List that holds the information for all the Contents
    private List<Video> listContents;
    private String thumbnail;
    private OnItemClickListener<Video> cardClickListener;
    private OnItemClickListener<Video> downloadClickListener;
    private File moduleFolder;


    public RecyclerViewAdapter(String moduleId, String thumbnail, List<Video> videos, OnItemClickListener<Video> cardClickListener, OnItemClickListener<Video> downloadClickListener) {
        this.listContents = videos;
        this.thumbnail = thumbnail;
        this.cardClickListener = cardClickListener;
        this.downloadClickListener = downloadClickListener;
        this.moduleFolder = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SystemConfig.downloadFolderName), moduleId);
    }

    //Method creates (inflates) the View Holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Depending on the setting 'thumbnail' we use a different layout
        @LayoutRes int layoutRes;
        switch (thumbnail) {
            case MainActivity.LARGE:
                layoutRes = R.layout.view_holder_video_large_thumbnail;
                break;
            case MainActivity.MEDIUM:
                layoutRes = R.layout.view_holder_video_medium_thumbnail;
                break;
            default:
                layoutRes = R.layout.view_holder_video_no_thumbnail;
                break;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    //Method gives the ViewHolder it's contents
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Video listUnit = listContents.get(position);
        holder.textUnitDescription.setText(listUnit.getTitle());

        //Since we don't have Thumbnail, these variables are not used
        if (!thumbnail.equals(MainActivity.NO_THUMBNAIL)) {
            //holder.urlUnitImage.setText(listUnit.getUrlUnitImage());
            //holder.urlUnitVideo.setText(listUnit.getUrlUnitVideo());
            Glide.with(holder.unitImage.getContext())
                    .load(listUnit.getThumbnailUrl())
                    .into(holder.unitImage);
            //TODO check out ThumbnailUtils.createVideoThumbnail
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardClickListener.onSelect(listUnit);
            }
        });

        if (new VideoDownloader(listUnit).isVideoAvailableLocally()) {
            holder.downloadButton.setVisibility(View.GONE);
        } else {
            holder.downloadButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return listContents.size();
    }

    //Reference to the Views for each data item
    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textUnitDescription;
        private ImageView unitImage;
        private TextView urlUnitVideo;
        private CardView cardView;
        private ImageButton downloadButton;

        //Constructor
        ViewHolder(View itemView) {
            super(itemView);
            switch (thumbnail) {
                case MainActivity.LARGE:
                    //Here we Fill the Thumbnail LARGE
                    textUnitDescription = itemView.findViewById(R.id.textViewUnitDescriptionLarge);
                    unitImage = itemView.findViewById(R.id.image_view_large);
                    break;
                case MainActivity.MEDIUM:
                    //Here we Fill the Thumbnail MEDIUM
                    textUnitDescription = itemView.findViewById(R.id.textViewUnitDescriptionMedium);
                    unitImage = itemView.findViewById(R.id.image_view_medium);
                    break;
                default:
                    //Here we're filling with NO_THUMBNAIL
                    textUnitDescription = itemView.findViewById(R.id.textViewUnitDescriptionNoThumbnail);
                    cardView = itemView.findViewById(R.id.card_view);
                    downloadButton = itemView.findViewById(R.id.image_button_download);
                    break;
            }
        }
    }


}
