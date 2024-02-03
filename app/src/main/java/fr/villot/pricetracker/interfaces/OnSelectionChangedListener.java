package fr.villot.pricetracker.interfaces;

import androidx.fragment.app.Fragment;

public interface OnSelectionChangedListener {
    void onSelectionChanged(Fragment fragment, int numSelectedItems);
}

