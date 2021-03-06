package com.anysoftkeyboard.ui.tutorials;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anysoftkeyboard.ui.settings.MainFragment;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public abstract class ChangeLogFragment extends Fragment {

    private final List<VersionChangeLogs.VersionChangeLog> mChangeLog;
    private final StringBuilder mBulletsBuilder = new StringBuilder();

    protected ChangeLogFragment() {
        mChangeLog = VersionChangeLogs.createChangeLog();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getMainLayout(), container, false);
    }

    protected abstract int getMainLayout();

    protected void fillViewForLogItem(int index, ChangeLogViewHolder holder) {
        final VersionChangeLogs.VersionChangeLog change = mChangeLog.get(index);

        setTitleText(holder.titleView, change.versionName);

        mBulletsBuilder.setLength(0);
        for (String changeEntry : change.changes) {
            if (mBulletsBuilder.length() != 0) mBulletsBuilder.append('\n');
            mBulletsBuilder.append(getString(R.string.change_log_bullet_point, changeEntry));
        }

        holder.bulletPointsView.setText(mBulletsBuilder.toString());

        holder.webLinkChangeLogView.setText(
                getString(R.string.change_log_url, change.changesWebUrl.toString()));
    }

    protected abstract void setTitleText(TextView titleView, String versionName);

    public static class FullChangeLogFragment extends ChangeLogFragment {
        @Override
        public void onStart() {
            super.onStart();
            MainSettingsActivity.setActivityTitle(this, getString(R.string.changelog));
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            RecyclerView recyclerView = view.findViewById(R.id.change_logs_container);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new ChangeLogsAdapter(VersionChangeLogs.createChangeLog()));
            recyclerView.setHasFixedSize(false);
        }

        private class ChangeLogsAdapter extends RecyclerView.Adapter<ChangeLogViewHolder> {
            private final List<VersionChangeLogs.VersionChangeLog> mChangeLog;

            ChangeLogsAdapter(List<VersionChangeLogs.VersionChangeLog> changeLog) {
                mChangeLog = changeLog;
                setHasStableIds(true);
            }

            @Override
            public ChangeLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ChangeLogViewHolder(
                        getLayoutInflater().inflate(R.layout.changelogentry_item, parent, false));
            }

            @Override
            public void onBindViewHolder(ChangeLogViewHolder holder, int position) {
                fillViewForLogItem(position, holder);
            }

            @Override
            public long getItemId(int position) {
                return mChangeLog.get(position).hashCode();
            }

            @Override
            public int getItemCount() {
                return mChangeLog.size();
            }
        }

        @Override
        protected int getMainLayout() {
            return R.layout.changelog;
        }

        @Override
        protected void setTitleText(TextView titleView, String versionName) {
            titleView.setText(
                    getString(R.string.change_log_entry_header_template_without_name, versionName));
        }
    }

    public static class LatestChangeLogFragment extends ChangeLogFragment {

        @Override
        protected int getMainLayout() {
            return R.layout.card_with_more_container;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ViewGroup container = view.findViewById(R.id.card_with_read_more);
            MainFragment.setupLink(
                    container,
                    R.id.read_more_link,
                    new ClickableSpan() {
                        @Override
                        public void onClick(View v) {
                            FragmentChauffeurActivity activity =
                                    (FragmentChauffeurActivity) getActivity();
                            if (activity == null) return;
                            activity.addFragmentToUi(
                                    new FullChangeLogFragment(),
                                    TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                        }
                    },
                    true);

            final ChangeLogViewHolder changeLogViewHolder =
                    new ChangeLogViewHolder(
                            getLayoutInflater()
                                    .inflate(R.layout.changelogentry_item, container, false));
            fillViewForLogItem(0, changeLogViewHolder);
            changeLogViewHolder.webLinkChangeLogView.setVisibility(View.GONE);

            container.addView(changeLogViewHolder.itemView, 0);
        }

        @Override
        protected void setTitleText(TextView titleView, String versionName) {
            titleView.setText(
                    getString(R.string.change_log_card_version_title_template, versionName));
        }
    }

    @VisibleForTesting
    static class ChangeLogViewHolder extends RecyclerView.ViewHolder {

        public final TextView titleView;
        public final TextView bulletPointsView;
        public final TextView webLinkChangeLogView;

        ChangeLogViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.changelog_version_title);
            bulletPointsView = itemView.findViewById(R.id.chang_log_item);
            webLinkChangeLogView = itemView.findViewById(R.id.change_log__web_link_item);

            titleView.setPaintFlags(titleView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }
}
