package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CubicEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    static final int COL_QUOTE_ID = 0;

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int CURSOR_LOADER_ID = 0;
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

    private static final int COL_QUOTE__ID = 0;
    private static final int COL_QUOTE_SYMBOL = 1;
    private static final int COL_QUOTE_PERCENT_CHANGE = 2;
    private static final int COL_QUOTE_CHANGE = 3;
    private static final int COL_QUOTE_BIDPRICE = 4;
    private static final int COL_QUOTE_CREATED = 5;
    private static final int COL_QUOTE_ISUP = 6;
    private static final int COL_QUOTE_ISCURRENT = 7;

    @Bind(R.id.detail_symbol_tv)
    TextView mSymbol;
    @Bind(R.id.detail_bidprice_tv)
    TextView mBidprice;
    @Bind(R.id.detail_change_tv)
    TextView mChange;
    @Bind(R.id.detail_percent_change_tv)
    TextView mPercent_change;
    @Bind(R.id.linechart)
    LineChartView lineChartView;

    private Uri mUri;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String symbol = data.getString(COL_QUOTE_SYMBOL);
            mSymbol.setText(symbol);
            mBidprice.setText(data.getString(COL_QUOTE_BIDPRICE));
            mChange.setText(data.getString(COL_QUOTE_CHANGE));
            mPercent_change.setText(data.getString(COL_QUOTE_PERCENT_CHANGE));

            String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("GTM"));
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            sdf2.setTimeZone(TimeZone.getDefault());
            LineSet dataset = new LineSet();

            float minValue = Float.MAX_VALUE;
            float maxValue = 0;
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                // The Cursor is now set to the right position
                String createdString = data.getString(data.getColumnIndex(QuoteColumns.CREATED));

                Date date = null;
                try {
                    date = sdf.parse(createdString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date != null) {
                    float value = Float.parseFloat(data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                    dataset.addPoint(sdf2.format(date), value);
                    Log.i(LOG_TAG, "date=" + createdString + " sdf2=" + sdf2.format(date) + " value=" + value);
                } else {
                    Log.i(LOG_TAG, "date=" + createdString + " value=" + data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
                }
            }

            fillLineSet(dataset, Math.round(minValue), Math.round(maxValue));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void fillLineSet(LineSet dataset, int minValue, int maxValue) {
//        String[] labels = new String[]{"a", "b", "c"};
//        float[] values = new float[]{20, 2, 540};
//        float min = Collections.max(values);
        int blue500 = getResources().getColor(R.color.material_blue_500);
        int bluegrey500 = getResources().getColor(R.color.material_blue_grey_500);
        // Line chart customization
        dataset.setThickness(Tools.fromDpToPx(2.0f));
        dataset.setColor(blue500);
        dataset.setDotsRadius(5f);
        dataset.setDotsColor(blue500);

        lineChartView.addData(dataset);
        lineChartView.setClickablePointRadius(5f);

        // Generic chart customization
        lineChartView.setAxisColor(bluegrey500);
        lineChartView.setLabelsColor(bluegrey500);
        // Paint object used to draw Grid
        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.material_blue_grey_50));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(0.5f));

        if (minValue>10) {
            minValue = ((minValue - 9) / 10) * 10;
        }
        maxValue = ((maxValue + 9) / 10) * 10;
//        lineChartView.setAxisBorderValues(minValue, maxValue, maxValue / 10);
        lineChartView.setAxisBorderValues(minValue, maxValue);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
        lineChartView.setLabelsFormat(new DecimalFormat("#"));
        lineChartView.setStep(100);
//        lineChartView.setAxisLabelsSpacing(3f);

        // Animation customization
        Animation anim = new Animation(500);
        anim.setEasing(new CubicEase());
//        anim.setOverlap(0.5f, new int[]{0, 1, 2, });
        lineChartView.show(anim);
//        lineChartView.setToo
        lineChartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Tooltip tooltip = new Tooltip(getActivity());
//                lineChartView.showTooltip(tooltip, true);
            }
        });
        lineChartView.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {

            }
        });
    }
}
