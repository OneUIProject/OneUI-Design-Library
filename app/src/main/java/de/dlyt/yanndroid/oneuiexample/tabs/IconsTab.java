package de.dlyt.yanndroid.oneuiexample.tabs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner;
import de.dlyt.yanndroid.oneui.utils.ThemeDynamicDrawable;
import de.dlyt.yanndroid.oneui.view.IndexScrollView;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.ViewPager2;
import de.dlyt.yanndroid.oneui.widget.TabLayout;
import de.dlyt.yanndroid.oneuiexample.R;

public class IconsTab extends Fragment {
    Integer[] imageIDs = {R.drawable.ic_oui3_accessibility,
            R.drawable.ic_oui3_account,
            R.drawable.ic_oui3_advanced_feature,
            R.drawable.ic_oui3_apps,
            R.drawable.ic_oui3_apps_2,
            R.drawable.ic_oui3_archive,
            R.drawable.ic_oui3_audio,
            R.drawable.ic_oui3_audio_2,
            R.drawable.ic_oui3_biometric_fingerprint,
            R.drawable.ic_oui3_briefcase,
            R.drawable.ic_oui3_brush,
            R.drawable.ic_oui3_calendar_day,
            R.drawable.ic_oui3_calendar_month,
            R.drawable.ic_oui3_calendar_task,
            R.drawable.ic_oui3_calendar_week,
            R.drawable.ic_oui3_calendar_year,
            R.drawable.ic_oui3_camera,
            R.drawable.ic_oui3_cloud_storage,
            R.drawable.ic_oui3_community,
            R.drawable.ic_oui3_composer,
            R.drawable.ic_oui3_contact,
            R.drawable.ic_oui3_creature,
            R.drawable.ic_oui3_crown,
            R.drawable.ic_oui3_delete,
            R.drawable.ic_oui3_device,
            R.drawable.ic_oui3_document,
            R.drawable.ic_oui3_edit,
            R.drawable.ic_oui3_error,
            R.drawable.ic_oui3_folder,
            R.drawable.ic_oui3_gift,
            R.drawable.ic_oui3_group,
            R.drawable.ic_oui3_help,
            R.drawable.ic_oui3_home,
            R.drawable.ic_oui3_home_shortcut,
            R.drawable.ic_oui3_iconview,
            R.drawable.ic_oui3_image,
            R.drawable.ic_oui3_image_2,
            R.drawable.ic_oui3_info,
            R.drawable.ic_oui3_light_bulb,
            R.drawable.ic_oui3_light_bulb_settings,
            R.drawable.ic_oui3_location_2,
            R.drawable.ic_oui3_location_off,
            R.drawable.ic_oui3_location_on,
            R.drawable.ic_oui3_lock,
            R.drawable.ic_oui3_mail,
            R.drawable.ic_oui3_manual,
            R.drawable.ic_oui3_memo,
            R.drawable.ic_oui3_message,
            R.drawable.ic_oui3_network_storage,
            R.drawable.ic_oui3_network_storage_manage,
            R.drawable.ic_oui3_notice,
            R.drawable.ic_oui3_pdf,
            R.drawable.ic_oui3_pin,
            R.drawable.ic_oui3_plug_in,
            R.drawable.ic_oui3_plus,
            R.drawable.ic_oui3_privacy,
            R.drawable.ic_oui3_recent,
            R.drawable.ic_oui3_reminder_2,
            R.drawable.ic_oui3_reminder_off,
            R.drawable.ic_oui3_reminder_on,
            R.drawable.ic_oui3_remove,
            R.drawable.ic_oui3_rename,
            R.drawable.ic_oui3_save,
            R.drawable.ic_oui3_scan,
            R.drawable.ic_oui3_sd_card,
            R.drawable.ic_oui3_secure_folder,
            R.drawable.ic_oui3_security,
            R.drawable.ic_oui3_selected_2,
            R.drawable.ic_oui3_send,
            R.drawable.ic_oui3_settings,
            R.drawable.ic_oui3_settings_2,
            R.drawable.ic_oui3_share,
            R.drawable.ic_oui3_software_update,
            R.drawable.ic_oui3_unlock,
            R.drawable.ic_oui3_usb,
            R.drawable.ic_oui3_video,
            R.drawable.ic_oui3_video_conference,
            R.drawable.ic_oui3_voice,
            R.drawable.ic_oui3_warning,
            R.drawable.ic_oui3_work,
            R.drawable.ic_oui4_audio,
            R.drawable.ic_oui4_biometric_fingerprint,
            R.drawable.ic_oui4_brush,
            R.drawable.ic_oui4_camera,
            R.drawable.ic_oui4_contact,
            R.drawable.ic_oui4_delete,
            R.drawable.ic_oui4_edit,
            R.drawable.ic_oui4_error,
            R.drawable.ic_oui4_folder,
            R.drawable.ic_oui4_group,
            R.drawable.ic_oui4_help,
            R.drawable.ic_oui4_image,
            R.drawable.ic_oui4_info,
            R.drawable.ic_oui4_lock,
            R.drawable.ic_oui4_mail,
            R.drawable.ic_oui4_pdf,
            R.drawable.ic_oui4_pin,
            R.drawable.ic_oui4_remove,
            R.drawable.ic_oui4_rename,
            R.drawable.ic_oui4_scan,
            R.drawable.ic_oui4_security,
            R.drawable.ic_oui4_settings,
            R.drawable.ic_oui4_settings_2,
            R.drawable.ic_oui4_share,
            R.drawable.ic_oui4_sticker,
            R.drawable.ic_oui4_store,
            R.drawable.ic_oui4_sync_off,
            R.drawable.ic_oui4_sync_on,
            R.drawable.ic_oui4_unlock,
            R.drawable.ic_oui4_voice,
            R.drawable.ic_oui4_volume,
            R.drawable.ic_oui4_volume_down,
            R.drawable.ic_oui4_volume_mute,
            R.drawable.ic_oui4_volume_up,
            R.drawable.ic_oui4_warning,
            R.drawable.ic_oui_align,
            R.drawable.ic_oui_align_center,
            R.drawable.ic_oui_align_left,
            R.drawable.ic_oui_align_right,
            R.drawable.ic_oui_anc,
            R.drawable.ic_oui_apk,
            R.drawable.ic_oui_arrow_down,
            R.drawable.ic_oui_arrow_left,
            R.drawable.ic_oui_arrow_right,
            R.drawable.ic_oui_arrow_up,
            R.drawable.ic_oui_attach,
            R.drawable.ic_oui_back,
            R.drawable.ic_oui_back_2,
            R.drawable.ic_oui_biometric_face,
            R.drawable.ic_oui_bluetooth_off,
            R.drawable.ic_oui_bluetooth_on,
            R.drawable.ic_oui_bluetooth_sync,
            R.drawable.ic_oui_book,
            R.drawable.ic_oui_bookmark,
            R.drawable.ic_oui_car,
            R.drawable.ic_oui_close,
            R.drawable.ic_oui_convert,
            R.drawable.ic_oui_copy,
            R.drawable.ic_oui_devicecare,
            R.drawable.ic_oui_digitalwellbeing,
            R.drawable.ic_oui_download,
            R.drawable.ic_oui_drawer,
            R.drawable.ic_oui_enter,
            R.drawable.ic_oui_equalizer,
            R.drawable.ic_oui_favorite_off,
            R.drawable.ic_oui_favorite_on,
            R.drawable.ic_oui_file_type_amr,
            R.drawable.ic_oui_file_type_apk,
            R.drawable.ic_oui_file_type_audio,
            R.drawable.ic_oui_file_type_contact,
            R.drawable.ic_oui_file_type_eml,
            R.drawable.ic_oui_file_type_etc,
            R.drawable.ic_oui_file_type_excel,
            R.drawable.ic_oui_file_type_folder,
            R.drawable.ic_oui_file_type_ftp,
            R.drawable.ic_oui_file_type_gltf,
            R.drawable.ic_oui_file_type_html,
            R.drawable.ic_oui_file_type_hwp,
            R.drawable.ic_oui_file_type_image,
            R.drawable.ic_oui_file_type_memo,
            R.drawable.ic_oui_file_type_pdf,
            R.drawable.ic_oui_file_type_planner,
            R.drawable.ic_oui_file_type_ppt,
            R.drawable.ic_oui_file_type_raw,
            R.drawable.ic_oui_file_type_scrapbook,
            R.drawable.ic_oui_file_type_sftp,
            R.drawable.ic_oui_file_type_smb,
            R.drawable.ic_oui_file_type_snb,
            R.drawable.ic_oui_file_type_snt,
            R.drawable.ic_oui_file_type_spd,
            R.drawable.ic_oui_file_type_story_album,
            R.drawable.ic_oui_file_type_task,
            R.drawable.ic_oui_file_type_txt,
            R.drawable.ic_oui_file_type_video,
            R.drawable.ic_oui_file_type_vnt,
            R.drawable.ic_oui_file_type_word,
            R.drawable.ic_oui_file_type_zip,
            R.drawable.ic_oui_game,
            R.drawable.ic_oui_game_2,
            R.drawable.ic_oui_game_detail,
            R.drawable.ic_oui_game_launcher,
            R.drawable.ic_oui_game_launcher_2,
            R.drawable.ic_oui_game_profile,
            R.drawable.ic_oui_google_drive,
            R.drawable.ic_oui_horizontal,
            R.drawable.ic_oui_hourglass,
            R.drawable.ic_oui_import,
            R.drawable.ic_oui_keyboard,
            R.drawable.ic_oui_labs,
            R.drawable.ic_oui_like_off,
            R.drawable.ic_oui_like_on,
            R.drawable.ic_oui_link,
            R.drawable.ic_oui_list,
            R.drawable.ic_oui_list_add,
            R.drawable.ic_oui_list_audio,
            R.drawable.ic_oui_list_dot,
            R.drawable.ic_oui_list_extended,
            R.drawable.ic_oui_list_filter,
            R.drawable.ic_oui_list_grid,
            R.drawable.ic_oui_list_indent,
            R.drawable.ic_oui_list_numbering,
            R.drawable.ic_oui_list_outdent,
            R.drawable.ic_oui_list_search,
            R.drawable.ic_oui_list_sort,
            R.drawable.ic_oui_list_text,
            R.drawable.ic_oui_manage,
            R.drawable.ic_oui_maximize,
            R.drawable.ic_oui_minimize,
            R.drawable.ic_oui_minus,
            R.drawable.ic_oui_more,
            R.drawable.ic_oui_move,
            R.drawable.ic_oui_move_to_beginning,
            R.drawable.ic_oui_nearby_devices,
            R.drawable.ic_oui_no_network,
            R.drawable.ic_oui_one_drive,
            R.drawable.ic_oui_open,
            R.drawable.ic_oui_page,
            R.drawable.ic_oui_page_lock,
            R.drawable.ic_oui_page_settings,
            R.drawable.ic_oui_page_unlock,
            R.drawable.ic_oui_password_hide,
            R.drawable.ic_oui_password_view,
            R.drawable.ic_oui_pause,
            R.drawable.ic_oui_pen,
            R.drawable.ic_oui_pen_calligraphy,
            R.drawable.ic_oui_pen_calligraphy_brush,
            R.drawable.ic_oui_pen_eraser,
            R.drawable.ic_oui_pen_fountain,
            R.drawable.ic_oui_pen_marker,
            R.drawable.ic_oui_pen_marker_round,
            R.drawable.ic_oui_pen_pencil,
            R.drawable.ic_oui_phone,
            R.drawable.ic_oui_play,
            R.drawable.ic_oui_plus,
            R.drawable.ic_oui_power,
            R.drawable.ic_oui_recentview,
            R.drawable.ic_oui_rectify,
            R.drawable.ic_oui_redo,
            R.drawable.ic_oui_refresh,
            R.drawable.ic_oui_remove,
            R.drawable.ic_oui_reorder,
            R.drawable.ic_oui_repeat,
            R.drawable.ic_oui_restore,
            R.drawable.ic_oui_search,
            R.drawable.ic_oui_selected,
            R.drawable.ic_oui_shape,
            R.drawable.ic_oui_shuffle,
            R.drawable.ic_oui_sim,
            R.drawable.ic_oui_smart_view,
            R.drawable.ic_oui_space,
            R.drawable.ic_oui_speed,
            R.drawable.ic_oui_spotify,
            R.drawable.ic_oui_squircle,
            R.drawable.ic_oui_stop,
            R.drawable.ic_oui_subscription,
            R.drawable.ic_oui_support,
            R.drawable.ic_oui_sync,
            R.drawable.ic_oui_tag,
            R.drawable.ic_oui_text,
            R.drawable.ic_oui_text_2,
            R.drawable.ic_oui_text_bold,
            R.drawable.ic_oui_text_italic,
            R.drawable.ic_oui_text_strikethrough,
            R.drawable.ic_oui_text_underline,
            R.drawable.ic_oui_ticket,
            R.drawable.ic_oui_time,
            R.drawable.ic_oui_undo,
            R.drawable.ic_oui_unpair,
            R.drawable.ic_oui_vertical,
            R.drawable.ic_oui_voice,
            R.drawable.ic_oui_web_search,
            R.drawable.ic_oui_website,
            R.drawable.ic_oui_wifi,
            R.drawable.ic_ouid_audio,
            R.drawable.ic_ouid_warning,
            R.drawable.ic_ouid_settings_2,
            R.drawable.ic_ouid_delete,
            R.drawable.ic_ouid_rename,
            R.drawable.ic_ouid_camera,
            R.drawable.ic_ouid_remove,
            R.drawable.ic_ouid_contact,
            R.drawable.ic_ouid_voice,
            R.drawable.ic_ouid_mail,
            R.drawable.ic_ouid_error,
            R.drawable.ic_ouid_pin,
            R.drawable.ic_ouid_settings,
            R.drawable.ic_ouid_image,
            R.drawable.ic_ouid_unlock,
            R.drawable.ic_ouid_info,
            R.drawable.ic_ouid_lock,
            R.drawable.ic_ouid_brush,
            R.drawable.ic_ouid_group,
            R.drawable.ic_ouid_edit,
            R.drawable.ic_ouid_help,
            R.drawable.ic_ouid_pdf,
            R.drawable.ic_ouid_share,
            R.drawable.ic_ouid_security,
            R.drawable.ic_ouid_folder,
            R.drawable.ic_ouid_scan,
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

        //prepare list
        Arrays.sort(imageIDs, (i1, i2) -> {
            if (i1 == -1) return 1;
            if (i2 == -1) return -1;
            return getResShortName(i1).compareToIgnoreCase(getResShortName(i2));
        });
        for (int i = 0; i < imageIDs.length; i++) selected.put(i, false);

        //init list
        listView = mRootView.findViewById(R.id.images);
        listView.setLayoutManager(new LinearLayoutManager(mContext));
        imageAdapter = new ImageAdapter();
        listView.setAdapter(imageAdapter);

        listView.setItemAnimator(null);
        listView.seslSetFillBottomEnabled(true);
        listView.seslSetGoToTopEnabled(true);
        listView.seslSetLastRoundedCorner(false);

        //divider
        TypedValue divider = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.listDivider, divider, true);
        ItemDecoration decoration = new ItemDecoration();
        listView.addItemDecoration(decoration);
        decoration.setDivider(mContext.getDrawable(divider.resourceId));

        //index scroll
        IndexScrollView indexScrollView = mRootView.findViewById(R.id.indexScrollView);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < imageIDs.length - 1; i++) list.add(getResShortName(imageIDs[i]));
        indexScrollView.syncWithRecyclerView(listView, list, true);
        indexScrollView.setIndexBarGravity(isRTL() ? 0 : 1);

        //select mode dismiss on back
        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                setSelecting(false);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    private String getResShortName(int id) {
        return getResources().getResourceEntryName(id)
                .replace("ic_ouid_", "")
                .replace("ic_oui3_", "")
                .replace("ic_oui4_", "")
                .replace("ic_oui_", "");
    }

    private boolean isRTL() {
        return getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public void setSelecting(boolean enabled) {
        DrawerLayout drawerLayout = ((DrawerLayout) getActivity().findViewById(R.id.drawer_view));
        TabLayout subTabs = getActivity().findViewById(R.id.sub_tabs);
        TabLayout mainTabs = getActivity().findViewById(R.id.main_samsung_tabs);
        ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager2);

        if (enabled) {
            mSelecting = true;
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount() - 1);
            drawerLayout.setSelectModeBottomMenu(R.menu.select_mode_menu, item -> {
                item.setBadge(item.getBadge() + 1);
                Toast.makeText(mContext, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            });
            drawerLayout.showSelectMode();
            drawerLayout.setSelectModeAllCheckedChangeListener((buttonView, isChecked) -> {
                if (checkAllListening) {
                    for (int i = 0; i < imageAdapter.getItemCount() - 1; i++) {
                        selected.put(i, isChecked);
                        imageAdapter.notifyItemChanged(i);
                    }
                }
                int count = 0;
                for (Boolean b : selected.values()) if (b) count++;
                drawerLayout.setSelectModeCount(count);
            });
            subTabs.setEnabled(false);
            mainTabs.setEnabled(false);
            viewPager2.setUserInputEnabled(false);
            onBackPressedCallback.setEnabled(true);
        } else {
            mSelecting = false;
            for (int i = 0; i < imageAdapter.getItemCount() - 1; i++) selected.put(i, false);
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount() - 1);

            drawerLayout.setSelectModeCount(0);
            drawerLayout.dismissSelectMode();
            subTabs.setEnabled(true);
            mainTabs.setEnabled(true);
            viewPager2.setUserInputEnabled(true);
            onBackPressedCallback.setEnabled(false);
        }
    }

    public void toggleItemSelected(int position) {
        selected.put(position, !selected.get(position));
        imageAdapter.notifyItemChanged(position);

        checkAllListening = false;
        int count = 0;
        for (Boolean b : selected.values()) if (b) count++;
        DrawerLayout drawerLayout = ((DrawerLayout) getActivity().findViewById(R.id.drawer_view));
        drawerLayout.setSelectModeAllChecked(count == imageAdapter.getItemCount() - 1);
        drawerLayout.setSelectModeCount(count);
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

                holder.dynamicTag.setVisibility(mContext.getDrawable(imageIDs[position]) instanceof ThemeDynamicDrawable ? View.VISIBLE : View.GONE);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            boolean isItem;

            RelativeLayout parentView;
            ImageView imageView;
            TextView textView;
            CheckBox checkBox;
            TextView dynamicTag;

            ViewHolder(View itemView, int viewType) {
                super(itemView);

                isItem = viewType == 0;

                if (isItem) {
                    parentView = (RelativeLayout) itemView;
                    imageView = parentView.findViewById(R.id.icon_tab_item_image);
                    textView = parentView.findViewById(R.id.icon_tab_item_text);
                    checkBox = parentView.findViewById(R.id.checkbox);
                    dynamicTag = parentView.findViewById(R.id.icon_tab_item_dynamic);
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
                    int moveRTL = isRTL() ? 130 : 0;
                    mDivider.setBounds(130 - moveRTL, y, width - moveRTL, mDividerHeight + y);
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
