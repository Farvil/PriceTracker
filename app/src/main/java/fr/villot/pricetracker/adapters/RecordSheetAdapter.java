package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.RecordSheet;

public class RecordSheetAdapter extends ArrayAdapter<RecordSheet> {

    private List<RecordSheet> recordSheets;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(RecordSheet recordSheet);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RecordSheetAdapter(Context context, List<RecordSheet> recordSheets) {
        super(context, 0, recordSheets);
        this.recordSheets = recordSheets;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_record_sheet, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);

        RecordSheet recordSheet = recordSheets.get(position);

        nameTextView.setText(recordSheet.getName());
        // dateTextView.setText(recordSheet.getDate().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
        String formattedDate = dateFormat.format(recordSheet.getDate());
        dateTextView.setText(formattedDate);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(recordSheet);
                }
            }
        });

        return convertView;
    }


}
