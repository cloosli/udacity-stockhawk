package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    private static final int CURSOR_LOADER_ID = 0;
    private Cursor mCursor;

    private static final String[] DETAIL_COLUMNS = {
            QuoteDatabase.QUOTES + "." + QuoteColumns._ID,
    };
    static final int COL_QUOTE_ID = 0;

    @Bind(R.id.textview)
    TextView mTextView;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        Bundle arguments = getArguments();
//        if (arguments != null) {
//            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
//        }
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, rootView);
//        Intent intent = getActivity().getIntent();
//        Bundle args = new Bundle();
//        args.putString(QuoteColumns.SYMBOL, intent.getStringExtra(QuoteColumns.SYMBOL));
//        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                QuoteProvider.Quotes.withSymbol(""),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
//        findRange(mCursor);
//        fillLineSet();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
