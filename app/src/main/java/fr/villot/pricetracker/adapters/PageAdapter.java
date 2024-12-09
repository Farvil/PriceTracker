package fr.villot.pricetracker.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import fr.villot.pricetracker.fragments.RecordSheetsFragment;
import fr.villot.pricetracker.fragments.ProductsFragment;
import fr.villot.pricetracker.fragments.StoresFragment;


public class PageAdapter extends FragmentPagerAdapter {

    //Default Constructor
    public PageAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public int getCount() {
        return(3);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return ProductsFragment.getInstance();
            case 1:
                return StoresFragment.getInstance();
            case 2:
                return RecordSheetsFragment.getInstance();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Produits";
            case 1:
                return "Magasins";
            case 2:
                return "Relevés de prix";
            default:
                return null;
        }
    }

    public int getItemPositionFromTitle(CharSequence title) {
        if (title.equals("Produits"))
            return 0;
        else if (title.equals("Magasins"))
            return 1;
        else if (title.equals("Relevés de prix"))
            return 2;
        else
            return -1;
    }

}