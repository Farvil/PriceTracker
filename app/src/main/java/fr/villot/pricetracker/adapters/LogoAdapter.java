package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.LogoItem;

public class LogoAdapter extends ArrayAdapter<LogoItem> {
    public LogoAdapter(Context context, List<LogoItem> logoItems) {
        super(context, 0, logoItems);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        LogoItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_logo, parent, false);
        }

        ImageView logoImageView = convertView.findViewById(R.id.logoImageView);

        int imageResourceId = getContext().getResources().getIdentifier(item.getImageName(), "drawable", getContext().getPackageName());
        logoImageView.setImageResource(imageResourceId);

        return convertView;
    }
}
