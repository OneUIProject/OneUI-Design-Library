package de.dlyt.yanndroid.oneuiexample.tabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.util.SeslRoundedCorner;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.ViewPager;
import de.dlyt.yanndroid.oneui.widget.BottomNavigationView;
import de.dlyt.yanndroid.oneui.view.IndexScrollView;
import de.dlyt.yanndroid.oneui.widget.TabLayout;
import de.dlyt.yanndroid.oneuiexample.R;

public class IconsTab extends Fragment {
    //229
    Integer[] imageIDs = {R.drawable.ic_samsung_accessibility,
            R.drawable.ic_samsung_account,
            R.drawable.ic_samsung_advanced_feature,
            R.drawable.ic_samsung_anc,
            R.drawable.ic_samsung_apk,
            R.drawable.ic_samsung_apps,
            R.drawable.ic_samsung_apps_2,
            R.drawable.ic_samsung_archive,
            R.drawable.ic_samsung_arrow_down,
            R.drawable.ic_samsung_arrow_left,
            R.drawable.ic_samsung_arrow_right,
            R.drawable.ic_samsung_arrow_up,
            R.drawable.ic_samsung_attach,
            R.drawable.ic_samsung_audio,
            R.drawable.ic_samsung_audio_2,
            R.drawable.ic_samsung_back,
            R.drawable.ic_samsung_bag,
            R.drawable.ic_samsung_biometric_face,
            R.drawable.ic_samsung_biometric_fingerprint,
            R.drawable.ic_samsung_biometric_fingerprint_2,
            R.drawable.ic_samsung_bluetooth_off,
            R.drawable.ic_samsung_bluetooth_on,
            R.drawable.ic_samsung_bluetooth_sync,
            R.drawable.ic_samsung_book,
            R.drawable.ic_samsung_bookmark,
            R.drawable.ic_samsung_briefcase,
            R.drawable.ic_samsung_brush,
            R.drawable.ic_samsung_calendar_day,
            R.drawable.ic_samsung_calendar_month,
            R.drawable.ic_samsung_calendar_task,
            R.drawable.ic_samsung_calendar_week,
            R.drawable.ic_samsung_calendar_year,
            R.drawable.ic_samsung_camera,
            R.drawable.ic_samsung_car,
            R.drawable.ic_samsung_close,
            R.drawable.ic_samsung_cloud_storage,
            R.drawable.ic_samsung_community,
            R.drawable.ic_samsung_composer,
            R.drawable.ic_samsung_contact,
            R.drawable.ic_samsung_convert,
            R.drawable.ic_samsung_copy,
            R.drawable.ic_samsung_creature,
            R.drawable.ic_samsung_crown,
            R.drawable.ic_samsung_delete,
            R.drawable.ic_samsung_device,
            R.drawable.ic_samsung_devicecare,
            R.drawable.ic_samsung_digitalwellbeing,
            R.drawable.ic_samsung_document,
            R.drawable.ic_samsung_download,
            R.drawable.ic_samsung_drawer,
            R.drawable.ic_samsung_edit,
            R.drawable.ic_samsung_equalizer,
            R.drawable.ic_samsung_error,
            R.drawable.ic_samsung_favorite_off,
            R.drawable.ic_samsung_favorite_on,
            R.drawable.ic_samsung_file_type_amr,
            R.drawable.ic_samsung_file_type_apk,
            R.drawable.ic_samsung_file_type_audio,
            R.drawable.ic_samsung_file_type_contact,
            R.drawable.ic_samsung_file_type_eml,
            R.drawable.ic_samsung_file_type_etc,
            R.drawable.ic_samsung_file_type_excel,
            R.drawable.ic_samsung_file_type_folder,
            R.drawable.ic_samsung_file_type_ftp,
            R.drawable.ic_samsung_file_type_gltf,
            R.drawable.ic_samsung_file_type_html,
            R.drawable.ic_samsung_file_type_hwp,
            R.drawable.ic_samsung_file_type_image,
            R.drawable.ic_samsung_file_type_memo,
            R.drawable.ic_samsung_file_type_pdf,
            R.drawable.ic_samsung_file_type_planner,
            R.drawable.ic_samsung_file_type_ppt,
            R.drawable.ic_samsung_file_type_raw,
            R.drawable.ic_samsung_file_type_scrapbook,
            R.drawable.ic_samsung_file_type_sftp,
            R.drawable.ic_samsung_file_type_smb,
            R.drawable.ic_samsung_file_type_snb,
            R.drawable.ic_samsung_file_type_snt,
            R.drawable.ic_samsung_file_type_spd,
            R.drawable.ic_samsung_file_type_story_album,
            R.drawable.ic_samsung_file_type_task,
            R.drawable.ic_samsung_file_type_txt,
            R.drawable.ic_samsung_file_type_video,
            R.drawable.ic_samsung_file_type_vnt,
            R.drawable.ic_samsung_file_type_word,
            R.drawable.ic_samsung_file_type_zip,
            R.drawable.ic_samsung_folder,
            R.drawable.ic_samsung_game,
            R.drawable.ic_samsung_game_2,
            R.drawable.ic_samsung_game_detail,
            R.drawable.ic_samsung_game_launcher,
            R.drawable.ic_samsung_game_launcher_2,
            R.drawable.ic_samsung_game_profile,
            R.drawable.ic_samsung_gift,
            R.drawable.ic_samsung_google_drive,
            R.drawable.ic_samsung_group,
            R.drawable.ic_samsung_help,
            R.drawable.ic_samsung_help_2,
            R.drawable.ic_samsung_home,
            R.drawable.ic_samsung_home_shortcut,
            R.drawable.ic_samsung_hourglass,
            R.drawable.ic_samsung_iconview,
            R.drawable.ic_samsung_image,
            R.drawable.ic_samsung_image_2,
            R.drawable.ic_samsung_image_3,
            R.drawable.ic_samsung_import,
            R.drawable.ic_samsung_info,
            R.drawable.ic_samsung_info_2,
            R.drawable.ic_samsung_keyboard,
            R.drawable.ic_samsung_labs,
            R.drawable.ic_samsung_light_bulb,
            R.drawable.ic_samsung_light_bulb_settings,
            R.drawable.ic_samsung_like_off,
            R.drawable.ic_samsung_like_on,
            R.drawable.ic_samsung_link,
            R.drawable.ic_samsung_list,
            R.drawable.ic_samsung_list_add,
            R.drawable.ic_samsung_list_audio,
            R.drawable.ic_samsung_list_extended,
            R.drawable.ic_samsung_list_filter,
            R.drawable.ic_samsung_list_grid,
            R.drawable.ic_samsung_list_search,
            R.drawable.ic_samsung_list_sort,
            R.drawable.ic_samsung_location_2,
            R.drawable.ic_samsung_location_off,
            R.drawable.ic_samsung_location_on,
            R.drawable.ic_samsung_lock,
            R.drawable.ic_samsung_mail,
            R.drawable.ic_samsung_manage,
            R.drawable.ic_samsung_manual,
            R.drawable.ic_samsung_maximize,
            R.drawable.ic_samsung_memo,
            R.drawable.ic_samsung_message,
            R.drawable.ic_samsung_minimize,
            R.drawable.ic_samsung_minus,
            R.drawable.ic_samsung_more,
            R.drawable.ic_samsung_move,
            R.drawable.ic_samsung_move_to_beginning,
            R.drawable.ic_samsung_network_storage,
            R.drawable.ic_samsung_network_storage_manage,
            R.drawable.ic_samsung_no_network,
            R.drawable.ic_samsung_notice,
            R.drawable.ic_samsung_onedrive,
            R.drawable.ic_samsung_open,
            R.drawable.ic_samsung_page,
            R.drawable.ic_samsung_password_hide,
            R.drawable.ic_samsung_password_view,
            R.drawable.ic_samsung_pause,
            R.drawable.ic_samsung_pdf,
            R.drawable.ic_samsung_pen,
            R.drawable.ic_samsung_pen_calligraphy,
            R.drawable.ic_samsung_pen_calligraphy_brush,
            R.drawable.ic_samsung_pen_eraser,
            R.drawable.ic_samsung_pen_fountain,
            R.drawable.ic_samsung_pen_marker,
            R.drawable.ic_samsung_pen_marker_round,
            R.drawable.ic_samsung_pen_pencil,
            R.drawable.ic_samsung_phone,
            R.drawable.ic_samsung_pin,
            R.drawable.ic_samsung_pin_2,
            R.drawable.ic_samsung_play,
            R.drawable.ic_samsung_plug_in,
            R.drawable.ic_samsung_plus,
            R.drawable.ic_samsung_plus_2,
            R.drawable.ic_samsung_power,
            R.drawable.ic_samsung_privacy,
            R.drawable.ic_samsung_recent,
            R.drawable.ic_samsung_recentview,
            R.drawable.ic_samsung_rectify,
            R.drawable.ic_samsung_redo,
            R.drawable.ic_samsung_refresh,
            R.drawable.ic_samsung_reminder_2,
            R.drawable.ic_samsung_reminder_off,
            R.drawable.ic_samsung_reminder_on,
            R.drawable.ic_samsung_remove,
            R.drawable.ic_samsung_remove_2,
            R.drawable.ic_samsung_remove_3,
            R.drawable.ic_samsung_rename,
            R.drawable.ic_samsung_reorder,
            R.drawable.ic_samsung_repeat,
            R.drawable.ic_samsung_restore,
            R.drawable.ic_samsung_save,
            R.drawable.ic_samsung_scan,
            R.drawable.ic_samsung_sd_card,
            R.drawable.ic_samsung_search,
            R.drawable.ic_samsung_secure_folder,
            R.drawable.ic_samsung_security,
            R.drawable.ic_samsung_security_2,
            R.drawable.ic_samsung_selected,
            R.drawable.ic_samsung_selected_2,
            R.drawable.ic_samsung_send,
            R.drawable.ic_samsung_settings,
            R.drawable.ic_samsung_settings_2,
            R.drawable.ic_samsung_settings_3,
            R.drawable.ic_samsung_share,
            R.drawable.ic_samsung_share_2,
            R.drawable.ic_samsung_shuffle,
            R.drawable.ic_samsung_sim,
            R.drawable.ic_samsung_smart_view,
            R.drawable.ic_samsung_software_update,
            R.drawable.ic_samsung_speed,
            R.drawable.ic_samsung_squircle,
            R.drawable.ic_samsung_sticker,
            R.drawable.ic_samsung_stop,
            R.drawable.ic_samsung_store,
            R.drawable.ic_samsung_subscription,
            R.drawable.ic_samsung_support,
            R.drawable.ic_samsung_sync,
            R.drawable.ic_samsung_tag,
            R.drawable.ic_samsung_text,
            R.drawable.ic_samsung_text_2,
            R.drawable.ic_samsung_ticket,
            R.drawable.ic_samsung_time,
            R.drawable.ic_samsung_undo,
            R.drawable.ic_samsung_unlock,
            R.drawable.ic_samsung_unpair,
            R.drawable.ic_samsung_usb,
            R.drawable.ic_samsung_video,
            R.drawable.ic_samsung_video_conference,
            R.drawable.ic_samsung_voice,
            R.drawable.ic_samsung_voice_2,
            R.drawable.ic_samsung_volume,
            R.drawable.ic_samsung_volume_down,
            R.drawable.ic_samsung_volume_mute,
            R.drawable.ic_samsung_volume_up,
            R.drawable.ic_samsung_warning,
            R.drawable.ic_samsung_web_search,
            R.drawable.ic_samsung_website,
            R.drawable.ic_samsung_wifi,
            R.drawable.ic_samsung_work,
            -1};
    RecyclerView listView;
    private View mRootView;
    private Context mContext;

    private ImageAdapter imageAdapter;
    private OnBackPressedCallback onBackPressedCallback;
    private HashMap<Integer, Boolean> selected = new HashMap<>();
    private boolean mSelecting = false;
    private boolean checkAllListening = true;

    public IconsTab() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
        TypedValue divider = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.listDivider, divider, true);

        for (int i = 0; i < imageIDs.length; i++) selected.put(i, false);

        listView = mRootView.findViewById(R.id.images);
        listView.setLayoutManager(new LinearLayoutManager(mContext));
        imageAdapter = new ImageAdapter();
        listView.setAdapter(imageAdapter);

        ItemDecoration decoration = new ItemDecoration();
        listView.addItemDecoration(decoration);
        decoration.setDivider(mContext.getDrawable(divider.resourceId));

        listView.setItemAnimator(null);
        listView.seslSetFillBottomEnabled(true);
        listView.seslSetGoToTopEnabled(true);
        listView.seslSetLastRoundedCorner(false);

        IndexScrollView indexScrollView = mRootView.findViewById(R.id.indexScrollView);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < imageIDs.length - 1; i++)
            list.add(getResources().getResourceEntryName(imageIDs[i]).substring(11));
        indexScrollView.syncWithRecyclerView(listView, list, true);

        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                setSelecting(false);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }


    public void setSelecting(boolean enabled) {
        ToolbarLayout toolbarLayout = ((DrawerLayout) getActivity().findViewById(R.id.drawer_view)).getToolbarLayout();
        TabLayout tabLayout = getActivity().findViewById(R.id.tabLayout);
        BottomNavigationView bnv = getActivity().findViewById(R.id.main_samsung_tabs);
        ViewPager viewPager = getActivity().findViewById(R.id.viewPager);

        if (enabled) {
            mSelecting = true;
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount() - 1);
            toolbarLayout.setSelectModeBottomMenu(R.menu.select_mode_menu, item -> {
                item.setBadge(item.getBadge() + 1);
                Toast.makeText(mContext, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            });
            toolbarLayout.showSelectMode();
            toolbarLayout.setSelectModeAllCheckedChangeListener((buttonView, isChecked) -> {
                if (checkAllListening) {
                    for (int i = 0; i < imageAdapter.getItemCount() - 1; i++) {
                        selected.put(i, isChecked);
                        imageAdapter.notifyItemChanged(i);
                    }
                }
                int count = 0;
                for (Boolean b : selected.values()) if (b) count++;
                toolbarLayout.setSelectModeCount(count);
            });
            tabLayout.setEnabled(false);
            bnv.setEnabled(false);
            viewPager.setPagingEnabled(false);
            onBackPressedCallback.setEnabled(true);
        } else {
            mSelecting = false;
            for (int i = 0; i < imageAdapter.getItemCount() - 1; i++) selected.put(i, false);
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount() - 1);

            toolbarLayout.setSelectModeCount(0);
            toolbarLayout.dismissSelectMode();
            tabLayout.setEnabled(true);
            bnv.setEnabled(true);
            viewPager.setPagingEnabled(true);
            onBackPressedCallback.setEnabled(false);
        }
    }

    public void toggleItemSelected(int position) {
        selected.put(position, !selected.get(position));
        imageAdapter.notifyItemChanged(position);

        checkAllListening = false;
        int count = 0;
        for (Boolean b : selected.values()) if (b) count++;
        ToolbarLayout toolbarLayout = ((DrawerLayout) getActivity().findViewById(R.id.drawer_view)).getToolbarLayout();
        toolbarLayout.setSelectModeAllChecked(count == imageAdapter.getItemCount() - 1);
        toolbarLayout.setSelectModeCount(count);
        checkAllListening = true;
    }


    //Adapter for the Icon RecyclerView
    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        @Override
        public int getItemCount() {
            return imageIDs.length;
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public int getItemViewType(final int position) {
            if (imageIDs[position] == -1) return 1;
            return 0;
        }

        @Override
        public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int resId = 0;

            switch (viewType) {
                case 0:
                    resId = R.layout.icon_tab_listview_item;
                    break;
                case 1:
                    resId = R.layout.icon_tab_listview_bottom_spacing;
                    break;
            }

            View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
            return new ImageAdapter.ViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(ImageAdapter.ViewHolder holder, final int position) {
            if (holder.isItem) {
                holder.checkBox.setVisibility(mSelecting ? View.VISIBLE : View.GONE);
                holder.checkBox.setChecked(selected.get(position));

                holder.imageView.setImageResource(imageIDs[position]);
                holder.textView.setText(getResources().getResourceEntryName(imageIDs[position]));

                holder.parentView.setOnClickListener(view -> {
                    if (mSelecting) toggleItemSelected(position);
                });
                holder.parentView.setOnLongClickListener(v -> {
                    if (!mSelecting) setSelecting(true);
                    toggleItemSelected(position);

                    listView.seslStartLongPressMultiSelection();
                    listView.seslSetLongPressMultiSelectionListener(new RecyclerView.SeslLongPressMultiSelectionListener() {
                        @Override
                        public void onItemSelected(RecyclerView var1, View var2, int var3, long var4) {
                            if (getItemViewType(var3) == 0) toggleItemSelected(var3);
                        }

                        @Override
                        public void onLongPressMultiSelectionEnded(int var1, int var2) {

                        }

                        @Override
                        public void onLongPressMultiSelectionStarted(int var1, int var2) {

                        }
                    });
                    return true;
                });
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            boolean isItem;

            RelativeLayout parentView;
            ImageView imageView;
            TextView textView;
            CheckBox checkBox;

            ViewHolder(View itemView, int viewType) {
                super(itemView);

                isItem = viewType == 0;

                if (isItem) {
                    parentView = (RelativeLayout) itemView;
                    imageView = parentView.findViewById(R.id.icon_tab_item_image);
                    textView = parentView.findViewById(R.id.icon_tab_item_text);
                    checkBox = parentView.findViewById(R.id.checkbox);
                }
            }
        }
    }

    public class ItemDecoration extends RecyclerView.ItemDecoration {
        private SeslRoundedCorner mSeslRoundedCornerTop;
        private SeslRoundedCorner mSeslRoundedCornerBottom;
        private Drawable mDivider;
        private int mDividerHeight;

        public ItemDecoration() {
            mSeslRoundedCornerTop = new SeslRoundedCorner(getContext(), true);
            mSeslRoundedCornerTop.setRoundedCorners(3);
            mSeslRoundedCornerBottom = new SeslRoundedCorner(getContext(), true);
            mSeslRoundedCornerBottom.setRoundedCorners(12);
        }

        @Override
        public void seslOnDispatchDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            super.seslOnDispatchDraw(canvas, recyclerView, state);

            int childCount = recyclerView.getChildCount();
            int width = recyclerView.getWidth();

            // draw divider for each item
            for (int i = 0; i < childCount; i++) {
                View childAt = recyclerView.getChildAt(i);
                ImageAdapter.ViewHolder viewHolder = (ImageAdapter.ViewHolder) recyclerView.getChildViewHolder(childAt);
                int y = ((int) childAt.getY()) + childAt.getHeight();

                boolean shallDrawDivider;

                if (recyclerView.getChildAt(i + 1) != null)
                    shallDrawDivider = ((ImageAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i + 1))).isItem;
                else
                    shallDrawDivider = false;

                if (mDivider != null && viewHolder.isItem && shallDrawDivider) {
                    mDivider.setBounds(130, y, width, mDividerHeight + y);
                    mDivider.draw(canvas);
                }

                if (!viewHolder.isItem) {
                    if (recyclerView.getChildAt(i + 1) != null)
                        mSeslRoundedCornerTop.drawRoundedCorner(recyclerView.getChildAt(i + 1), canvas);
                    if (recyclerView.getChildAt(i - 1) != null)
                        mSeslRoundedCornerBottom.drawRoundedCorner(recyclerView.getChildAt(i - 1), canvas);
                }
            }

            mSeslRoundedCornerTop.drawRoundedCorner(canvas);
        }

        public void setDivider(Drawable d) {
            mDivider = d;
            mDividerHeight = d.getIntrinsicHeight();
            listView.invalidateItemDecorations();
        }
    }
}
