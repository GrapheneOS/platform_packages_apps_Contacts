/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.contacts;

import android.app.Activity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Shared static contact utility methods. */
public class MoreContactUtils {

    /**
     * Enable new edge to edge feature.
     *
     * @param activity the Activity need to setup the edge to edge feature.
     */
    public static void setupEdgeToEdge(@NonNull Activity activity, EdgeToEdgeInsetHandler handler) {
        final int statusBarHeight = getStatusBarHeight(activity);
        final int actionBarHeight = getActionBarHeight(activity);
        final View statusBarBackground = handler == null ? new View(activity) : null;

        if (statusBarBackground != null) {
            statusBarBackground.setBackgroundResource(R.color.primary_color_dark);
            statusBarBackground.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(
                    statusBarBackground,
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            );
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                activity.findViewById(android.R.id.content),
                (v, windowInsets) -> {
                    final Insets insets =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                                            | WindowInsetsCompat.Type.ime()
                                            | WindowInsetsCompat.Type.displayCutout());

                    if (statusBarBackground != null) {
                        final Insets statusInsets = windowInsets.getInsets(
                                        WindowInsetsCompat.Type.statusBars()
                                                | WindowInsetsCompat.Type.displayCutout()
                        );
                        final FrameLayout.LayoutParams layoutParams =
                                (FrameLayout.LayoutParams) statusBarBackground.getLayoutParams();

                        final int actionBarTop = insets.top - actionBarHeight;
                        final int height = Math.max(
                                statusBarHeight > 0 ? statusBarHeight : statusInsets.top,
                                actionBarHeight > 0 ? actionBarTop : 0
                        );
                        if (layoutParams.height != height) {
                            layoutParams.height = height;
                            statusBarBackground.setLayoutParams(layoutParams);
                        }
                    }

                    // Apply the insets paddings to the view.
                    v.setPadding(
                            insets.left,
                            handler == null ? insets.top : v.getPaddingTop(),
                            insets.right,
                            insets.bottom);

                    if (handler != null) {
                        handler.applyTopInset(insets.top);
                    }

                    // Return CONSUMED if you don't want the window insets to keep being
                    // passed down to descendant views.
                    return WindowInsetsCompat.CONSUMED;
                });
    }

    private static int getStatusBarHeight(@NonNull Activity activity) {
        final int statusBarHeightId = activity
                .getResources()
                .getIdentifier("status_bar_height", "dimen", "android");

        if (statusBarHeightId == 0) {
            return 0;
        }

        return activity.getResources().getDimensionPixelSize(statusBarHeightId);
    }

    private static int getActionBarHeight(@NonNull Activity activity) {
        final TypedValue outValue = new TypedValue();

        if (!activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, outValue, true)) {
            return 0;
        }

        if (outValue.resourceId != 0) {
            return activity.getResources().getDimensionPixelSize(outValue.resourceId);
        }

        return TypedValue.complexToDimensionPixelSize(
                outValue.data,
                activity.getResources().getDisplayMetrics()
        );
    }

    /** Handles setting the insets on a {@link View}. */
    public static class EdgeToEdgeInsetHandler {

        private final View mView;

        private int mOriginalHeight = -1;
        private int mOriginalPaddingTop = -1;

        public EdgeToEdgeInsetHandler(View view) {
            mView = view;
        }

        public void applyTopInset(int top) {
            ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
            if (mOriginalHeight == -1) {
                mOriginalHeight = layoutParams.height;
            }
            if (mOriginalPaddingTop == -1) {
                mOriginalPaddingTop = mView.getPaddingTop();
            }
            layoutParams.height = mOriginalHeight + top;
            mView.setLayoutParams(layoutParams);
            mView.setPadding(
                    mView.getPaddingLeft(),
                    mOriginalPaddingTop + top,
                    mView.getPaddingRight(),
                    mView.getPaddingBottom());
        }
    }
}
