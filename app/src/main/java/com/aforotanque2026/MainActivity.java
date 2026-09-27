package com.aforotanque2026;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int CREATE_PDF = 1001;
    private EditText diameterInput, lengthInput, levelInput;
    private TextView resultText;
    private LinearLayout tableContainer;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(16, 24, 16, 24);
        main.setBackgroundColor(0xFFDDF3FF);
        scroll.addView(main);

        TextView title = new TextView(this);
        title.setText("Calculadora de aforo de tanque");
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(0xFF17324D);
        main.addView(title);

        TextView info = new TextView(this);
        info.setText("Tanque cilíndrico horizontal • cálculo matemático • sin Internet");
        info.setTextSize(15);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 10, 0, 18);
        main.addView(info);

        diameterInput = field("Diámetro", "0 cm");
        lengthInput = field("Largo", "0 cm");
        levelInput = field("Altura del líquido", "0 cm");
        main.addView(row("Diámetro", diameterInput));
        main.addView(row("Largo", lengthInput));
        main.addView(row("Altura", levelInput));

        Button calculate = new Button(this);
        calculate.setText("CALCULAR");
        main.addView(calculate);

        resultText = new TextView(this);
        resultText.setTextSize(18);
        resultText.setGravity(Gravity.CENTER);
        resultText.setPadding(0, 15, 0, 15);
        main.addView(resultText);

        Button table = new Button(this);
        table.setText("GENERAR TABLA DE AFORO");
        main.addView(table);

        Button pdf = new Button(this);
        pdf.setText("📥 GENERAR PDF");
        main.addView(pdf);

        tableContainer = new LinearLayout(this);
        tableContainer.setOrientation(LinearLayout.VERTICAL);
        main.addView(tableContainer);

        calculate.setOnClickListener(v -> calculateLevel());
        table.setOnClickListener(v -> generateTable());
        pdf.setOnClickListener(v -> createPdfChooser());

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { calculateLevel(); }
            public void afterTextChanged(Editable s) {}
        };
        diameterInput.addTextChangedListener(watcher);
        lengthInput.addTextChangedListener(watcher);
        levelInput.addTextChangedListener(watcher);

        setContentView(scroll);
        calculateLevel();
    }

    private LinearLayout row(String label, EditText input) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(18);
        t.setTextColor(0xFF17324D);
        r.addView(t, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        r.addView(input, new LinearLayout.LayoutParams(170, LinearLayout.LayoutParams.WRAP_CONTENT));
        return r;
    }

    private EditText field(String hint, String value) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setSelectAllOnFocus(true);
        e.setTextSize(18);
        e.setGravity(Gravity.CENTER);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setSingleLine(true);
        e.setPadding(8, 8, 8, 8);
        return e;
    }

    private double val(EditText e) {
        String s = e.getText().toString().trim().replace(',', '.');
        if (s.isEmpty() || s.equals(".")) return 0.0;
        return Double.parseDouble(s);
    }

    // Área exacta del segmento circular en cm², por el largo en cm, convertido a litros.
    private double litersAt(double d, double l, double h) {
        if (d <= 0 || l <= 0 || h <= 0) return 0.0;
        if (h >= d) return Math.PI * Math.pow(d / 2.0, 2) * l / 1000.0;
        double r = d / 2.0;
        double y = h - r;
        double inside = Math.max(0.0, r * r - y * y);
        double area = r * r * Math.acos(-y / r) + y * Math.sqrt(inside);
        return area * l / 1000.0;
    }

    private void calculateLevel() {
        try {
            double d = val(diameterInput), l = val(lengthInput), h = val(levelInput);
            if (d < 0 || l < 0 || h < 0 || (d > 0 && h > d)) throw new Exception();
            double liters = litersAt(d, l, h);
            double total = litersAt(d, l, d);
            double percent = total > 0 ? liters * 100.0 / total : 0.0;
            resultText.setText(String.format(Locale.US,
                    "Volumen: %.2f litros\nCapacidad total: %.2f litros\nLlenado: %.2f %%",
                    liters, total, percent));
        } catch (Exception e) {
            resultText.setText("Revisa las medidas. La altura debe estar entre 0 y el diámetro.");
        }
    }

    private void generateTable() {
        tableContainer.removeAllViews();
        try {
            double d = val(diameterInput), l = val(lengthInput);
            if (d <= 0 || l <= 0 || d > 10000 || l > 10000) throw new Exception();

            TextView headerInfo = new TextView(this);
            headerInfo.setText(String.format(Locale.US,
                    "Diámetro: %.2f cm   Largo: %.2f cm", d, l));
            headerInfo.setTypeface(null, Typeface.BOLD);
            headerInfo.setTextSize(16);
            headerInfo.setGravity(Gravity.CENTER);
            headerInfo.setPadding(8, 12, 8, 12);
            tableContainer.addView(headerInfo);

            LinearLayout header = tableRow();
            header.addView(cell("Altura", true), tableCellParams(100));
            header.addView(cell("Litros", true), tableCellParams(105));
            header.addView(cell("Diferencia", true), tableCellParams(115));
            tableContainer.addView(header);

            int max = (int) Math.floor(d);
            double previous = 0.0;
            for (int h = 0; h <= max; h++) {
                double liters = litersAt(d, l, h);
                double difference = (h == 0) ? 0.0 : liters - previous;

                LinearLayout row = tableRow();
                row.addView(cell(String.format(Locale.US, "%d cm", h), false), tableCellParams(100));
                row.addView(cell(String.format(Locale.US, "%.2f L", liters), false), tableCellParams(105));
                row.addView(cell(String.format(Locale.US, "%.2f L", difference), false), tableCellParams(115));
                tableContainer.addView(row);

                previous = liters;
            }
        } catch (Exception e) {
            TextView error = new TextView(this);
            error.setText("Introduce diámetro y largo válidos.");
            tableContainer.addView(error);
        }
    }

    private LinearLayout tableRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private LinearLayout.LayoutParams tableCellParams(int dpWidth) {
        float density = getResources().getDisplayMetrics().density;
        return new LinearLayout.LayoutParams((int)(dpWidth * density + 0.5f), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private TextView cell(String text, boolean bold) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(15);
        v.setGravity(Gravity.CENTER);
        v.setPadding(4, 7, 4, 7);
        if (bold) v.setTypeface(null, Typeface.BOLD);
        return v;
    }

    private void createPdfChooser() {
        try {
            double d = val(diameterInput), l = val(lengthInput);
            if (d <= 0 || l <= 0 || d > 10000 || l > 10000) {
                Toast.makeText(this, "Introduce diámetro y largo válidos.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_TITLE, "AforoTanque.pdf");
            startActivityForResult(intent, CREATE_PDF);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo preparar el PDF.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CREATE_PDF || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        try {
            double d = val(diameterInput), l = val(lengthInput);
            PdfDocument doc = buildPdf(d, l);
            OutputStream out = getContentResolver().openOutputStream(uri);
            if (out == null) throw new IOException("No se pudo abrir el archivo");
            doc.writeTo(out);
            out.close();
            doc.close();
            Toast.makeText(this, "PDF generado correctamente.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al generar el PDF.", Toast.LENGTH_LONG).show();
        }
    }

    private PdfDocument buildPdf(double d, double l) {
        PdfDocument doc = new PdfDocument();
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int pageNumber = 1;
        PdfDocument.Page page = newPage(doc, pageNumber);
        Canvas canvas = page.getCanvas();
        float y = 45;
        p.setTextSize(18); p.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("AFORO DE TANQUE", 40, y, p);
        y += 25;
        p.setTextSize(12); p.setTypeface(Typeface.DEFAULT);
        canvas.drawText(String.format(Locale.US, "Diámetro: %.2f cm   Largo: %.2f cm", d, l), 40, y, p);
        y += 25;
        p.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("Altura (cm)", 45, y, p);
        canvas.drawText("Litros", 245, y, p);
        canvas.drawText("Diferencia", 390, y, p);
        p.setTypeface(Typeface.DEFAULT);
        y += 20;
        int max = (int)Math.floor(d);
        double previous = 0.0;
        for (int h = 0; h <= max; h++) {
            double liters = litersAt(d, l, h);
            double difference = (h == 0) ? 0.0 : liters - previous;
            if (y > 810) {
                doc.finishPage(page);
                pageNumber++;
                page = newPage(doc, pageNumber);
                canvas = page.getCanvas();
                y = 45;
                p.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText("AFORO DE TANQUE (continuación)", 40, y, p);
                y += 25;
                canvas.drawText("Altura (cm)", 45, y, p);
                canvas.drawText("Litros", 245, y, p);
                canvas.drawText("Diferencia", 390, y, p);
                p.setTypeface(Typeface.DEFAULT);
                y += 20;
            }
            canvas.drawText(String.format(Locale.US, "%d", h), 70, y, p);
            canvas.drawText(String.format(Locale.US, "%.2f", liters), 245, y, p);
            canvas.drawText(String.format(Locale.US, "%.2f", difference), 390, y, p);
            y += 16;
            previous = liters;
        }
        doc.finishPage(page);
        return doc;
    }

    private PdfDocument.Page newPage(PdfDocument doc, int number) {
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, number).create();
        return doc.startPage(info);
    }
}
