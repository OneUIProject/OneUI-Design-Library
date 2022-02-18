package de.dlyt.yanndroid.oneui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import de.dlyt.yanndroid.oneui.R;

public class Toast extends android.widget.Toast {
    private Context mContext;

    public Toast(Context context) {
        super(context);
        mContext = context;
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast result = new Toast(context);

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.sesl_transient_notification, null);
        TextView tv = v.findViewById(R.id.message);
        tv.setText(text);

        result.setView(v);
        result.setDuration(duration);

        return result;
    }

    public static Toast makeText(Context context, @StringRes int resId, int duration) throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }
}
