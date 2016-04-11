package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by ChristianL on 11.04.16.
 */
public class QuoteWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = QuoteWidgetProvider.class.getSimpleName();
    private boolean mIsLargeLayout = true;
    public static String REFRESH_ACTION = "com.sam_chordas.android.stockhawk.widget.REFRESH";
    public static String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        Log.d(LOG_TAG, "onReceive(action: " + action + ")");
//        if (action.equals(REFRESH_ACTION)) {
//            // BroadcastReceivers have a limited amount of time to do work, so for this sample, we
//            // are triggering an update of the data on another thread.  In practice, this update
//            // can be triggered from a background service, or perhaps as a result of user actions
//            // inside the main application.
//            final Context context = ctx;
//            sWorkerQueue.removeMessages(0);
//            sWorkerQueue.post(new Runnable() {
//                @Override
//                public void run() {
//                    final ContentResolver r = context.getContentResolver();
//                    final Cursor c = r.query(WeatherDataProvider.CONTENT_URI, null, null, null,
//                            null);
//                    final int count = c.getCount();
//
//                    // We disable the data changed observer temporarily since each of the updates
//                    // will trigger an onChange() in our data observer.
//                    r.unregisterContentObserver(sDataObserver);
//                    for (int i = 0; i < count; ++i) {
//                        final Uri uri = ContentUris.withAppendedId(WeatherDataProvider.CONTENT_URI, i);
//                        final ContentValues values = new ContentValues();
//                        values.put(WeatherDataProvider.Columns.TEMPERATURE,
//                                new Random().nextInt(sMaxDegrees));
//                        r.update(uri, values, null, null);
//                    }
//                    r.registerContentObserver(WeatherDataProvider.CONTENT_URI, true, sDataObserver);
//
//                    final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
//                    final ComponentName cn = new ComponentName(context, WeatherWidgetProvider.class);
//                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.weather_list);
//                }
//            });
//
//            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
//                    AppWidgetManager.INVALID_APPWIDGET_ID);
//        } else if (action.equals(CLICK_ACTION)) {
//            // Show a toast
//            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
//                    AppWidgetManager.INVALID_APPWIDGET_ID);
//            final String day = intent.getStringExtra(EXTRA_DAY_ID);
//            final String formatStr = ctx.getResources().getString(R.string.toast_format_string);
//            Toast.makeText(ctx, String.format(formatStr, day), Toast.LENGTH_SHORT).show();
//        }

        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(ctx, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }

        super.onReceive(ctx, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate()");
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_collection);
            // Create an Intent to refresh the widget
            Intent intent = new Intent(context, QuoteWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent);

            // Create an Intent to launch MainActivity
//            Intent intent2 = new Intent(context, MyStocksActivity.class);
//            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
//            views.setOnClickPendingIntent(R.id.widget, pendingIntent2);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                views.setRemoteAdapter(appWidgetId, R.id.widget_list, new Intent(context, QuoteWidgetRemoteViewsService.class));
            } else {
                views.setRemoteAdapter(R.id.widget_list, new Intent(context, QuoteWidgetRemoteViewsService.class));
            }
            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            views.setEmptyView(R.id.widget_list, R.id.empty_view);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d(LOG_TAG, "onAppWidgetOptionsChanged()");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
//            options.getSize(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
//            if (size > min-width) {
//                R.layout.widget_large
//            }
//        }
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        RemoteViews layout;
        if (minHeight < 100) {
            mIsLargeLayout = false;
        } else {
            mIsLargeLayout = true;
        }
//        layout = buildLayout(context, appWidgetId, mIsLargeLayout);
//        appWidgetManager.updateAppWidget(appWidgetId, layout);
    }
}
