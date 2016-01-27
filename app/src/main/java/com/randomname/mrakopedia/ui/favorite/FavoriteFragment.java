package com.randomname.mrakopedia.ui.favorite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.realm.DBWorker;
import com.randomname.mrakopedia.ui.RxBaseFragment;
import com.randomname.mrakopedia.ui.pagesummary.PageSummaryActivity;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import carbon.widget.TextView;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by vgrigoryev on 27.01.2016.
 */
public class FavoriteFragment extends RxBaseFragment {

    private static final String TAG = "Favorite Fragment";

    private static final int PAGE_SUMMARY_ACTIVITY_CODE = 11;

    @Bind(R.id.error_text_view)
    TextView errorTextView;

    @Bind(R.id.favorite_recycler_view)
    RecyclerView recyclerView;
    FavoriteAdapter adapter;

    ArrayList<PageSummaryRealm> favoritePages;

    private int selectedPosition = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoritePages = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorite_fragment, null);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new FavoriteAdapter(favoritePages, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                selectedPosition = position;

                Intent intent = new Intent(getActivity(), PageSummaryActivity.class);
                intent.putExtra(PageSummaryActivity.PAGE_NAME_EXTRA, favoritePages.get(position).getPageTitle());

                startActivityForResult(intent, PAGE_SUMMARY_ACTIVITY_CODE);
            }
        });
        recyclerView.setAdapter(adapter);

        getFavoriteFromRealm();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAGE_SUMMARY_ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            if (!favoritePages.get(selectedPosition).isFavorite()) {
                favoritePages.remove(selectedPosition);
                checkForEmpty();
            }
            adapter.notifyDataSetChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkForEmpty() {
        if (favoritePages.isEmpty()) {
            errorTextView.setText(R.string.no_favorite_error);
            errorTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    private void getFavoriteFromRealm() {
        Subscription subscription =
                DBWorker
                        .getFavoritePages()
                        .subscribe(new Subscriber<PageSummaryRealm>() {
                            @Override
                            public void onCompleted() {
                                checkForEmpty();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(PageSummaryRealm pageSummaryRealm) {
                                favoritePages.add(pageSummaryRealm);
                                adapter.notifyItemInserted(favoritePages.indexOf(pageSummaryRealm));
                            }
                        });

        bindToLifecycle(subscription);
    }

    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
        private ArrayList<PageSummaryRealm> favoritePages;
        private View.OnClickListener onClickListener;

        public FavoriteAdapter(ArrayList<PageSummaryRealm> favoritePages, View.OnClickListener onClickListener) {
            this.favoritePages = favoritePages;
            this.onClickListener = onClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_member_view_holder, parent, false);
            view.setOnClickListener(onClickListener);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.titleTextView.setText(favoritePages.get(position).getPageTitle());

            if (favoritePages.get(position).isRead()) {
                holder.titleTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                holder.titleTextView.setTextColor(Color.parseColor("#D9000000"));
            }
        }

        @Override
        public int getItemCount() {
            return favoritePages == null ? 0 : favoritePages.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            public TextView titleTextView;

            public ViewHolder(View itemView) {
                super(itemView);

                titleTextView = (TextView)itemView.findViewById(R.id.title_text_view);
            }
        }
    }
}
