package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by ChristianL on 11.04.16.
 */
public class QuoteWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = QuoteWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] DETAIL_COLUMNS = {
            QuoteDatabase.QUOTES + "." + QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CREATED,
            QuoteColumns.ISUP,
            QuoteColumns.ISCURRENT,
    };

    private static final int COL_QUOTE_ID = 0;
    private static final int COL_QUOTE_SYMBOL = 1;
    private static final int COL_QUOTE_PCHANGE = 2;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuoteRemoteViewsFactory(getApplicationContext(), intent);
    }

    class QuoteRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Cursor mCursor = null;
        private Context mContext;
        private int mAppWidgetId;

        public QuoteRemoteViewsFactory(Context applicationContext, Intent intent) {
            mContext = applicationContext;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            // Since we reload the cursor in onDataSetChanged() which gets called immediately after
            // onCreate(), we do nothing here.
        }

        @Override
        public void onDataSetChanged() {
            Log.d(LOG_TAG, "onDataSetChanged()");
            final long identityToken = Binder.clearCallingIdentity();
            // Refresh the cursor
            if (mCursor != null) {
                mCursor.close();
            }
            Uri quoteUri = QuoteProvider.Quotes.CONTENT_URI;
            mCursor = getContentResolver().query(
                    quoteUri,
                    DETAIL_COLUMNS,
                    QuoteColumns.ISCURRENT + "=?",
                    new String[]{"1"},
                    null
            );
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            Log.d(LOG_TAG, "onDestroy()");
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            Log.d(LOG_TAG, "getViewAt(position: " + position + ")");
            if (position == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_collection_item);

            final String symbol = mCursor.getString(COL_QUOTE_SYMBOL);
            views.setTextViewText(R.id.stock_symbol, symbol);
            views.setTextViewText(R.id.change, mCursor.getString(COL_QUOTE_PCHANGE));

            Uri quoteUri = QuoteProvider.Quotes.withSymbol(symbol);
            Intent intent = new Intent();
            intent.setData(quoteUri);
            views.setOnClickFillInIntent(R.id.widget_list_item, intent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_collection_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(COL_QUOTE_ID);
            }
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
