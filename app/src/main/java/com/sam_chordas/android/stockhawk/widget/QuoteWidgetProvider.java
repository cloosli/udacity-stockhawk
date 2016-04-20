package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by ChristianL on 11.04.16.
 */
public class QuoteWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = QuoteWidgetProvider.class.getSimpleName();
    public static String REFRESH_ACTION = "com.sam_chordas.android.stockhawk.widget.REFRESH";
    public static String ACTION_CLICK = "ACTION_CLICK";
    private boolean mIsLargeLayout = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate() mIsLargeLayout=" + mIsLargeLayout);
        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_collection);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }
            boolean useDetailActivity = context.getResources().getBoolean(R.bool.use_detail_activity);
            Intent clickIntentTemplate = useDetailActivity
                    ? new Intent(context, DetailActivity.class)
                    : new Intent(context, MyStocksActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_list, R.id.empty_view);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(LOG_TAG, "onReceive(action: " + action + ")");

//        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)) {
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
//            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(ctx, getClass()));
////            appWidgetManager.updateAppWidget(appWidgetIds, buildLayout(ctx, appWidgetId));
////            appWidgetManager.updateAppWidget(appWidgetIds[0], buildLayout(ctx, appWidgetIds[0]));
//            for (int appWidgetId : appWidgetIds) {
//                appWidgetManager.updateAppWidget(appWidgetId, buildLayout(ctx, appWidgetId));
//            }
//        } else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
//            int[] appWidgetIds = null;
//            if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)) {
//                appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//            } else {
//                appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(ctx, getClass()));
//            }
//            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
//        }

        super.onReceive(context, intent);
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
    }

//    @Override
//    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
//        Log.v(LOG_TAG, "onAppWidgetOptionsChanged()");
////        context.startService(new Intent(context, TodayWidgetIntentService.class));
//
//        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
////        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
////        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
////        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
//
//        RemoteViews layout;
//        if (minWidth <= 225) {
//            mIsLargeLayout = false;
//        } else {
//            mIsLargeLayout = true;
//        }
//        Log.d(LOG_TAG, "onAppWidgetOptionsChanged minWidth=" + minWidth + ", mIsLargeLayout=" + mIsLargeLayout);
//        layout = buildLayout(context, appWidgetId);
//        appWidgetManager.updateAppWidget(appWidgetId, layout);
//        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
//    }

    private RemoteViews buildLayout(Context context, int appWidgetId) {
        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_collection);

        // Create an Intent to refresh the widget
        Intent intent = new Intent(context, QuoteWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent);

        // Create an Intent to launch MainActivity
//            Intent intent2 = new Intent(context, MyStocksActivity.class);
//            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
//            views.setOnClickPendingIntent(R.id.widget, pendingIntent2);

//        mIsLargeLayout = intent.getBooleanExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, false);
        Intent widgetIntent = new Intent(context, QuoteWidgetRemoteViewsService.class);
        widgetIntent.putExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, mIsLargeLayout);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            views.setRemoteAdapter(appWidgetId, R.id.widget_list, widgetIntent);
        } else {
            views.setRemoteAdapter(R.id.widget_list, widgetIntent);
        }
        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews
        // object above.
        views.setEmptyView(R.id.widget_list, R.id.empty_view);
        return views;
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, QuoteWidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, QuoteWidgetRemoteViewsService.class));
    }
}
