package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CubicEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    static final int COL_QUOTE_ID = 0;
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";

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

    private static final int COL_QUOTE_SYMBOL = 1;

    @Bind(R.id.detail_symbol_tv)
    TextView mSymbol;
    @Bind(R.id.detail_bidprice_tv)
    TextView mBidprice;
    @Bind(R.id.detail_change_tv)
    TextView mChange;
    @Bind(R.id.detail_percent_change_tv)
    TextView mPercent_change;
    @Bind(R.id.linechart)
    LineChartView mChart;

    private Uri mUri;
    private boolean mTransitionAnimation;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailActivityFragment.DETAIL_TRANSITION_ANIMATION, false);
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
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }
//        ViewParent vp = getView().getParent();
//        if ( vp instanceof CardView ) {
//            ((View)vp).setVisibility(View.INVISIBLE);
//        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String symbol = data.getString(COL_QUOTE_SYMBOL);
            mSymbol.setText(symbol);
            mBidprice.setText(data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
            mChange.setText(data.getString(data.getColumnIndex(QuoteColumns.CHANGE)));
            mPercent_change.setText(data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));

            downloadDetails(symbol);

//            Log.d(LOG_TAG, "data from db : " + mChange.getText() + "; "+ mPercent_change.getText());
//            String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
//            SimpleDateFormat sdf = new SimpleDateFormat(format);
//            sdf.setTimeZone(TimeZone.getTimeZone("GTM"));
//            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
//            sdf2.setTimeZone(TimeZone.getDefault());
//            LineSet dataset = new LineSet();
//
//            float minValue = Float.MAX_VALUE;
//            float maxValue = 0;
//            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
//                // The Cursor is now set to the right position
//                String createdString = data.getString(COL_QUOTE_CREATED);
//
//                Date date = null;
//                try {
//                    date = sdf.parse(createdString);
//                    float value = data.getFloat(COL_QUOTE_BIDPRICE);
//                    minValue = Math.min(value, minValue);
//                    maxValue = Math.max(value, maxValue);
//                    dataset.addPoint(sdf2.format(date), value);
////                    Log.i(LOG_TAG, "date=" + createdString + " sdf2=" + sdf2.format(date) + " value=" + value);
//                } catch (ParseException | NumberFormatException e) {
//                    Log.e(LOG_TAG, e.getMessage(), e);
//                }
//            }
//
//            fillLineSet(dataset, Math.round(minValue), Math.round(maxValue));
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if (null != toolbarView) {
                activity.setSupportActionBar(toolbarView);
//                toolbarView.setTitle(mSymbol.getText());
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if (null != toolbarView) {
                Menu menu = toolbarView.getMenu();
                if (null != menu) menu.clear();
                toolbarView.inflateMenu(R.menu.menu_detail);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//        menuItem.setIntent(createShareForecastIntent());
    }

    private void downloadDetails(String symbol) {
        //TODO: laod data from the database
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
//        https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22YHOO%22%20and%20startDate%20%3D%20%222009-09-11%22%20and%20endDate%20%3D%20%222010-03-10%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=
//        http://chartapi.finance.yahoo.com/instrument/1.0/YHOO/chartdata;type=quote;range=5d/json
                .url("https://chartapi.finance.yahoo.com/instrument/1.0/" + symbol + "/chartdata;type=quote;range=1m/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                downloadFailed();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 200) {
                    downloadFailed();
                } else {
                    String result = response.body().string();
                    downloadComplete(result);
                }
            }
        });
    }

    private void downloadComplete(String result) {
        try {
            if (result.startsWith("finance_charts_json_callback( ")) {
                result = result.substring(29, result.length() - 2);
            }
            JSONObject object = new JSONObject(result);
            final String companyName = object.getJSONObject("meta").getString("Company-Name");

            JSONArray labels = object.getJSONArray("labels");
            ArrayList<String> labelsList = new ArrayList<>();
            // skip first entry
            for (int i = 1; i < labels.length(); i++) {
                labelsList.add(labels.getString(i));
            }

            final LineSet dataset = new LineSet();

            SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMdd");
            JSONArray series = object.getJSONArray("series");
//            int min = Integer.MAX_VALUE;
//            int max = Integer.MIN_VALUE;
            for (int i = 0; i < series.length(); i++) {
                JSONObject seriesItem = series.getJSONObject(i);
                String date = "";
                if (labelsList.contains(seriesItem.getString("Date"))) {
                    date = android.text.format.DateFormat.getMediumDateFormat(getActivity()).format(srcFormat.parse(seriesItem.getString("Date")));
                }
                float value = Float.parseFloat(seriesItem.getString("close"));
//                min = (int) Math.min(min, value); // 4.3 > 4; 4.7 > 4
//                max = (int) Math.max(max, value);
                dataset.addPoint(date, value);
            }
            JSONObject closeRanges = object.getJSONObject("ranges").getJSONObject("close");
            final int min = (int) Float.parseFloat(closeRanges.getString("min"));
            final int max = (int) Float.parseFloat(closeRanges.getString("max"));
            fillLineSet(dataset, min, max + 1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(companyName);
                }
            });

        } catch (Exception e) {
            downloadFailed();
            e.printStackTrace();
        }

    }

//    private void setupChart(LineChart chart, LineData data, int color) {
//////            mChart.setDrawGridBackground(false);
////            // no description text
////            mChart.setDescription("");
////            mChart.setNoDataTextDescription("You need to provide data for the chart.");
////            // enable touch gestures
////            mChart.setTouchEnabled(true);
//
//    // create a dataset and give it a type
//    LineDataSet set1 = new LineDataSet(values, "DataSet 1");
//
//    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
//    dataSets.add(set1); // add the datasets
//
//    // create a data object with the datasets
//    final LineData data = new LineData(labels, dataSets);
////
////            // set data
////            mChart.setData(data);
//
//    getActivity().runOnUiThread(new Runnable() {
//        @Override
//        public void run() {
//            int blue500 = getResources().getColor(R.color.material_blue_500);
//            setupChart(mChart, data, blue500);
//        }
//    });
//        ((LineDataSet) data.getDataSetByIndex(0)).setCircleColor(color);
//
//        // no description text
//        chart.setDescription("");
//        chart.setNoDataTextDescription("You need to provide data for the chart.");
//
//        // mChart.setDrawHorizontalGrid(false);
//        //
//        // enable / disable grid background
//        chart.setDrawGridBackground(false);
////        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);
//
//        // enable touch gestures
//        chart.setTouchEnabled(true);
//
//        // enable scaling and dragging
//        chart.setDragEnabled(true);
//        chart.setScaleEnabled(true);
//
//        // if disabled, scaling can be done on x- and y-axis separately
//        chart.setPinchZoom(false);
//
//        chart.setDrawGridBackground(false);
//
//        // set custom chart offsets (automatic offset calculation is hereby disabled)
//        chart.setViewPortOffsets(10, 0, 10, 0);
//
//        // add data
//        chart.setData(data);
//
//        // get the legend (only possible after setting data)
//        Legend l = chart.getLegend();
//        l.setEnabled(false);
//
//        chart.getAxisLeft().setEnabled(true);
//        chart.getAxisLeft().setSpaceTop(40);
//        chart.getAxisLeft().setSpaceBottom(40);
//        chart.getAxisRight().setEnabled(true);
//
//        chart.getXAxis().setEnabled(true);
//
//        // animate calls invalidate()...
//        chart.animateX(2500);
//    }

    private void downloadFailed() {
        Snackbar.make(getView(), getString(R.string.download_error), Snackbar.LENGTH_SHORT).show();
    }

    private void fillLineSet(LineSet dataset, int minValue, int maxValue) {
        int blue500 = getResources().getColor(R.color.material_blue_500);
        int bluegrey500 = getResources().getColor(R.color.material_blue_grey_500);
        // Line chart customization
        dataset.setThickness(Tools.fromDpToPx(2.0f));
        dataset.setColor(blue500);
        dataset.setDotsRadius(8f);
        dataset.setDotsColor(blue500);

        mChart.addData(dataset);

        mChart.setLabelsColor(bluegrey500);
        mChart.setClickablePointRadius(5f);

        // Generic chart customization
        mChart.setAxisColor(bluegrey500);
        mChart.setAxisLabelsSpacing(10f);

        // Paint object used to draw Grid
        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.material_blue_grey_50));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(0.5f));
        mChart.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
//        if (minValue>10) {
//            minValue = ((minValue - 9) / 10) * 10;
//        }
//        maxValue = ((maxValue + 9) / 10) * 10;
//        lineChartView.setAxisBorderValues(minValue, maxValue, maxValue / 10);
//        mChart.setLabelsFormat(new DecimalFormat("#"));
        int diff = maxValue - minValue;
//        if (minValue>10 && diff > 10) {
//            minValue = ((minValue - 9) / 10) * 10;
//            maxValue = ((maxValue + 9) / 10) * 10;
//            mChart.setAxisBorderValues(minValue, maxValue, maxValue / 10);
//        }
        mChart.setAxisBorderValues(minValue, maxValue);

//        chart.setYLabels(LabelPosition.NONE/OUTSIDE/INSIDE)
//        chart.setXLabels(LabelPosition.NONE/OUTSIDE/INSIDE)
//        mChart.setAxisLabelsSpacing(10f);

        // Animation customization
        Animation anim = new Animation(500);
        anim.setEasing(new CubicEase());
//        anim.setOverlap(0.5f, new int[]{0, 1, 2, });
        mChart.show(anim);
    }
}
