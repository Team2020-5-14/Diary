package com.ynu.diary.entries;


import android.support.v4.app.Fragment;

import com.ynu.diary.entries.entries.EntriesEntity;

import java.util.List;


public class BaseDiaryFragment extends Fragment {


    public long getTopicId() {
        return ((DiaryActivity) getActivity()).getTopicId();
    }


    public List<EntriesEntity> getEntriesList() {
        return ((DiaryActivity) getActivity()).getEntriesList();
    }

    public void updateEntriesList() {
        ((DiaryActivity) getActivity()).loadEntries();
    }
}

