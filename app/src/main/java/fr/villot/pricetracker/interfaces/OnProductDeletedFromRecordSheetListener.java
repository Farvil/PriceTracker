package fr.villot.pricetracker.interfaces;

public interface OnProductDeletedFromRecordSheetListener {
    void onProductDeletedFromRecordSheet(String barcode, int recordSheetId);
}
