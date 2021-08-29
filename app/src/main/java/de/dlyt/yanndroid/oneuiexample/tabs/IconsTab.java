package de.dlyt.yanndroid.oneuiexample.tabs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import de.dlyt.yanndroid.oneui.recyclerview.GridLayoutManager;
import de.dlyt.yanndroid.oneui.recyclerview.SeslRecyclerView;
import de.dlyt.yanndroid.oneuiexample.R;

public class IconsTab extends Fragment {

    Integer[] imageIDs = {R.drawable.ic_samsung_arrow_down, R.drawable.ic_samsung_arrow_left, R.drawable.ic_samsung_arrow_right, R.drawable.ic_samsung_arrow_up, R.drawable.ic_samsung_attach, R.drawable.ic_samsung_audio, R.drawable.ic_samsung_back, R.drawable.ic_samsung_book, R.drawable.ic_samsung_bookmark, R.drawable.ic_samsung_brush, R.drawable.ic_samsung_camera, R.drawable.ic_samsung_close, R.drawable.ic_samsung_convert, R.drawable.ic_samsung_copy, R.drawable.ic_samsung_delete, R.drawable.ic_samsung_document, R.drawable.ic_samsung_download, R.drawable.ic_samsung_drawer, R.drawable.ic_samsung_edit, R.drawable.ic_samsung_equalizer, R.drawable.ic_samsung_favorite, R.drawable.ic_samsung_group, R.drawable.ic_samsung_help, R.drawable.ic_samsung_image, R.drawable.ic_samsung_image_2, R.drawable.ic_samsung_import, R.drawable.ic_samsung_info, R.drawable.ic_samsung_keyboard, R.drawable.ic_samsung_lock, R.drawable.ic_samsung_mail, R.drawable.ic_samsung_maximize, R.drawable.ic_samsung_minimize, R.drawable.ic_samsung_minus, R.drawable.ic_samsung_more, R.drawable.ic_samsung_move, R.drawable.ic_samsung_mute, R.drawable.ic_samsung_page, R.drawable.ic_samsung_pause, R.drawable.ic_samsung_pdf, R.drawable.ic_samsung_pen, R.drawable.ic_samsung_pen_calligraphy, R.drawable.ic_samsung_pen_calligraphy_brush, R.drawable.ic_samsung_pen_eraser, R.drawable.ic_samsung_pen_fountain, R.drawable.ic_samsung_pen_marker, R.drawable.ic_samsung_pen_marker_round, R.drawable.ic_samsung_pen_pencil, R.drawable.ic_samsung_play, R.drawable.ic_samsung_plus, R.drawable.ic_samsung_rectify, R.drawable.ic_samsung_redo, R.drawable.ic_samsung_remind, R.drawable.ic_samsung_rename, R.drawable.ic_samsung_reorder, R.drawable.ic_samsung_restore, R.drawable.ic_samsung_save, R.drawable.ic_samsung_scan, R.drawable.ic_samsung_search, R.drawable.ic_samsung_selected, R.drawable.ic_samsung_send, R.drawable.ic_samsung_settings, R.drawable.ic_samsung_share, R.drawable.ic_samsung_shuffle, R.drawable.ic_samsung_smart_view, R.drawable.ic_samsung_stop, R.drawable.ic_samsung_tag, R.drawable.ic_samsung_text, R.drawable.ic_samsung_text_2, R.drawable.ic_samsung_time, R.drawable.ic_samsung_undo, R.drawable.ic_samsung_unlock, R.drawable.ic_samsung_voice, R.drawable.ic_samsung_volume, R.drawable.ic_samsung_warning, R.drawable.ic_samsung_web_search};

    private View mRootView;
    private AppCompatActivity mActivity;

    public IconsTab() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_icons_tab, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Icons
        SeslRecyclerView images = mRootView.findViewById(R.id.images);
        images.setLayoutManager(new GridLayoutManager(mActivity, 15));
        images.setAdapter(new ImageAdapter(mActivity));
        images.seslSetFillBottomEnabled(false);
        images.seslSetLastRoundedCorner(false);
    }


    //Adapter for the Icon RecyclerView
    public class ImageAdapter extends SeslRecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private Context mContext;

        ImageAdapter(Context context) {
            mContext = context;
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView;
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imageView.setImageResource(imageIDs[position]);
        }

        @Override
        public int getItemCount() {
            return imageIDs.length;
        }

        public class ViewHolder extends SeslRecyclerView.ViewHolder {
            ImageView imageView;

            ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }

}