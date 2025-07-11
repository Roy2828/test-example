package com.example.myapplication.pop;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.concurrent.atomic.AtomicInteger;

import razerdp.util.log.PopupLog;

/**
 * Created by 大灯泡 on 2019/4/9
 * <p>
 * Description：
 */
public class ViewUtil {

    public static void clearImageViewMemory(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        imageView.setImageBitmap(null);
    }

    public static View removeFromParent(View v) {
        if (v == null) return null;
        ViewParent p = v.getParent();
        if (p instanceof ViewGroup) {
            ((ViewGroup) p).removeViewInLayout(v);
        }
        return v;
    }

    public static void setViewsClickListener(@Nullable View.OnClickListener listener, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(listener);
            }
        }
    }

    public static void setViewsClickListener(@NonNull View rootView, @Nullable View.OnClickListener listener, @IdRes int... ids) {
        if (rootView == null) return;
        for (int id : ids) {
            View v = rootView.findViewById(id);
            if (v != null) {
                v.setOnClickListener(listener);
            }
        }
    }



    public static void setViewsEnable(boolean enable, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setEnabled(enable);
            }
        }
    }

    public static void setTextViewColor(@ColorInt int color, TextView... views) {
        for (TextView view : views) {
            if (view != null) {
                view.setTextColor(color);
            }
        }
    }



    public static void setViewsEnableAndClickable(boolean enable, boolean clickable, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setEnabled(enable);
                view.setClickable(clickable);
            }
        }
    }

    public static void expandViewTouchDelegate(View view, int expanded) {
        expandViewTouchDelegate(view, expanded, expanded, expanded, expanded);
    }

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围（ABSListView无效）
     * 延迟300毫秒执行，使该方法可以在onCreate里面使用
     */
    public static void expandViewTouchDelegate(final View view, final int top, final int bottom, final int left, final int right) {
        if (view == null) {
            return;
        }

        view.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent == null) return;
                Rect bounds = new Rect();
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                PopupLog.i("扩大触摸面积 ", bounds);

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                parent.setTouchDelegate(touchDelegate);

            }
        });
    }

    /**
     * 还原View的触摸和点击响应范围,最小不小于View自身范围
     */
    public static void restoreViewTouchDelegate(final View view) {
        if (view == null) {
            return;
        }

        if (view.getParent() != null) {

            ((View) view.getParent()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Rect bounds = new Rect();
                    bounds.setEmpty();
                    TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                    if (View.class.isInstance(view.getParent())) {
                        ((View) view.getParent()).setTouchDelegate(touchDelegate);
                    }
                }
            }, 300);
        }
    }


    public static void setViewsFocusListener(View.OnFocusChangeListener listener, View... views) {
        for (View view : views) {
            if (view != null) {
                view.setOnFocusChangeListener(listener);
            }
        }
    }

    public static void setEditTextWatcher(TextWatcher watcher, EditText... views) {
        for (EditText view : views) {
            if (view != null) {
                view.addTextChangedListener(watcher);
            }
        }
    }

    public static void setTextViewDrawable(TextView textView, @DrawableRes int start, @DrawableRes int top, @DrawableRes int end, @DrawableRes int bottom) {
        if (textView == null) return;
        final Context context = textView.getContext();
        textView.setCompoundDrawablesWithIntrinsicBounds(
                start != 0 ? context.getResources().getDrawable(start) : null,
                top != 0 ? context.getResources().getDrawable(top) : null,
                end != 0 ? context.getResources().getDrawable(end) : null,
                bottom != 0 ? context.getResources().getDrawable(bottom) : null);
    }







    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static final int generateViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        } else {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }

            }
        }
    }

    public static void setBackground(View v, Drawable background) {
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(background);
        } else {
            v.setBackgroundDrawable(background);
        }
    }



    public static boolean pointInView(View view, float x, float y) {
        if (view == null || !view.isShown()) return false;
        float[] points = {x, y};
        points[0] -= view.getLeft();
        points[1] -= view.getTop();
        Matrix matrix = view.getMatrix();
        if (!matrix.isIdentity()) {
            matrix.invert(matrix);
            matrix.mapPoints(points);
        }
        return points[0] >= 0 && points[1] >= 0 && points[0] < view.getWidth() && points[1] < view.getHeight();
    }


    public static void setViewPivotRatio(final View v, final float pvX, final float pvY) {
        if (v == null) return;
        v.post(new Runnable() {
            @Override
            public void run() {
                v.setPivotX(v.getWidth() * pvX);
                v.setPivotY(v.getHeight() * pvY);
            }
        });
    }

    public static void setViewPivotValue(final View v, final float pvX, final float pvY) {
        if (v == null) return;
        v.setPivotX(pvX);
        v.setPivotY(pvY);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void safeRequestLayout(final View v) {
        if (v == null || !v.isShown()) return;

        boolean preventRequestLayout = false;
        if (v.isInLayout()) {
            preventRequestLayout = v.isLayoutRequested();
        }
        if (!preventRequestLayout) {
            ViewParent parent = v.getParent();
            while (parent != null) {
                if (parent.isLayoutRequested()) {
                    preventRequestLayout = true;
                    break;
                }
                parent = parent.getParent();
            }
        }

        if (!preventRequestLayout) {
            v.requestLayout();
        } else {
            v.post(new Runnable() {
                @Override
                public void run() {
                    v.requestLayout();
                }
            });
        }
    }

    public static View inflate(Context context, @LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        return LayoutInflater.from(context).inflate(resource, root, attachToRoot);
    }



}
