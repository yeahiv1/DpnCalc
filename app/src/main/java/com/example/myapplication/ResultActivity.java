package com.example.myapplication;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class ResultActivity extends AppCompatActivity {
    private TextView riskLevelText;
    private TextView riskFactorsText;
    private TextView symptomsText;
    private TextView recommendationsText;
    private Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_results);

        riskLevelText = findViewById(R.id.risk_level_text);
        riskFactorsText = findViewById(R.id.risk_factors_text);
        symptomsText = findViewById(R.id.symptoms_text);
        recommendationsText = findViewById(R.id.recommendations_text);
        returnButton = findViewById(R.id.return_button);

        Intent intent = getIntent();
        double dpnRiskScore = intent.getDoubleExtra("dpnRiskScore", 0.0);
        String dpnRiskLevel = intent.getStringExtra("dpnRiskLevel");

        HashMap<String, String> patientData =
                (HashMap<String, String>) intent.getSerializableExtra("patientData");
        ArrayList<String> selectedSymptoms = intent.getStringArrayListExtra("selectedSymptoms");

        displayRiskLevel(dpnRiskScore, dpnRiskLevel);

        if (patientData != null) {
            String[] riskFactors = determineContributingFactors(patientData);
            if (riskFactors.length > 0) {
                StringBuilder factorsText = new StringBuilder("Faktor yang berkontribusi:\n");
                for (String factor : riskFactors) {
                    factorsText.append("• ").append(factor).append("\n");
                }
                riskFactorsText.setText(factorsText.toString());
            } else {
                riskFactorsText.setText("Faktor Penyebab: Tidak ada yang teridentifikasi");
            }
        } else {
            String[] riskFactors = intent.getStringArrayExtra("riskFactors");
            if (riskFactors != null && riskFactors.length > 0) {
                StringBuilder factorsText = new StringBuilder("Faktor yang berkontribusi:\n");
                for (String factor : riskFactors) {
                    factorsText.append("• ").append(factor).append("\n");
                }
                riskFactorsText.setText(factorsText.toString());
            } else {
                riskFactorsText.setText("Faktor Penyebab: Tidak ada yang teridentifikasi");
            }
        }
        if (dpnRiskLevel != null) {
            displayRecommendations(dpnRiskLevel);
        }
        if (selectedSymptoms != null && !selectedSymptoms.isEmpty()) {
            StringBuilder symptomsBuilder = new StringBuilder();
            for (String symptom : selectedSymptoms) {
                symptomsBuilder.append("• ").append(symptom).append("\n");
            }
            symptomsText.setText(symptomsBuilder.toString());
        } else {
            symptomsText.setText("Tidak ada gejala yang dilaporkan.");
        }

        returnButton.setOnClickListener(v -> finish());
    }

    private void displayRiskLevel(double riskScore, String riskLevel) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16);

        int backgroundColor;
        int textColor;

        if (riskScore < 0.3) {
            backgroundColor = ContextCompat.getColor(this, R.color.risk_low_bg);
            textColor = ContextCompat.getColor(this, R.color.risk_low_text);
        } else if (riskScore < 0.7) {
            backgroundColor = ContextCompat.getColor(this, R.color.risk_medium_bg);
            textColor = ContextCompat.getColor(this, R.color.risk_medium_text);
        } else {
            backgroundColor = ContextCompat.getColor(this, R.color.risk_high_bg);
            textColor = ContextCompat.getColor(this, R.color.risk_high_text);
        }

        shape.setColor(backgroundColor);
        riskLevelText.setBackground(shape);

        riskLevelText.setTextColor(textColor);
        riskLevelText.setText(String.format("Tingkat Risiko DPN: %s\n (Score: %.2f)", riskLevel, riskScore));
        riskLevelText.setTypeface(null, Typeface.BOLD);
    }

    private void displayRecommendations(String riskLevel) {
        Map<String, List<String>> recommendationMap = new HashMap<>();

        recommendationMap.put("low risk", Collections.unmodifiableList(Arrays.asList(
                "• Tetap pertahankan gaya hidup sehat.",
                "• Lanjutkan perawatan diabetes secara rutin",
                "• Lakukan pemeriksaan kaki tahunan",
                "• Pantau perubahan pada sensasi kaki",
                "• Jaga kebersihan kaki dan gunakan pelembap",
                "• Gunakan sepatu yang pas dan nyaman"
        )));

        recommendationMap.put("medium risk", Collections.unmodifiableList(Arrays.asList(
                "• Awasi gejala seperti kesemutan, kebas, atau rasa terbakar pada kaki/tangan.",
                "• Jadwalkan kunjungan kontrol dalam 3 bulan",
                "• Pertimbangkan penggunaan alas kaki khusus",
                "• Lakukan pemeriksaan kaki harian",
                "• Tinjau kembali pengelolaan gula darah",
                "• Hindari berjalan tanpa alas kaki",
                "• Pertimbangkan suplemen vitamin B"
        )));

        recommendationMap.put("high risk", Collections.unmodifiableList(Arrays.asList(
                "• Segera temui dokter untuk pemeriksaan fisik",
                "• Kurangi konsumsi gula dan perhatikan pola makan.",
                "• Tingkatkan pemantauan gula darah",
                "• Lakukan pemeriksaan kaki klinis setiap bulan",
                "• Pertimbangkan konsultasi neurologi",
                "• Lakukan penilaian manajemen nyeri",
                "• Disarankan evaluasi pembuluh darah"
        )));

        List<String> recs = recommendationMap.get(riskLevel.toLowerCase(Locale.ROOT));
        if (recs != null) {
            StringBuilder joined = new StringBuilder();
            for (int i = 0; i < recs.size(); i++) {
                joined.append(recs.get(i));
                if (i < recs.size() - 1) {
                    joined.append("\n");
                }
            }
            recommendationsText.setText(joined.toString());
        } else {
            recommendationsText.setText("Tidak ada rekomendasi untuk kategori risiko ini.");
        }
    }

    private String[] determineContributingFactors(HashMap<String, String> patientData) {
        ArrayList<String> factors = new ArrayList<>();

        float bmi = parseSafeFloat(patientData.get("bmi"));
        if (bmi >= 30) {
            factors.add("Obesitas (BMI: " + bmi + ")");
        } else if (bmi >= 25) {
            factors.add("Overweight (BMI: " + bmi + ")");
        }

        String a1cResult = patientData.get("A1Cresult");
        if (">8".equals(a1cResult) || ">7".equals(a1cResult)) {
            factors.add("Kontrol glikemik yang buruk (A1C: " + a1cResult + ")");
        }

        boolean onInsulin = "Steady".equals(patientData.get("insulin"));
        boolean onMetformin = "Steady".equals(patientData.get("metformin"));
        boolean onDiabetesMeds = "Yes".equals(patientData.get("diabetesMed"));

        if (onInsulin) {
            factors.add("ketergantungan Insulin");
        }

        if (onInsulin && onMetformin) {
            factors.add("Menggunakan beberapa obat diabetes (insulin + metformin)");
        } else if (onDiabetesMeds) {
            factors.add("Menggunakan obat diabetes");
        }

        int emergencyVisits = parseSafeInt(patientData.get("number_emergency"));
        if (emergencyVisits > 0) {
            factors.add("Kunjungan darurat (" + emergencyVisits + ")");
        }

        int inpatientVisits = parseSafeInt(patientData.get("number_inpatient"));
        if (inpatientVisits > 0) {
            factors.add("Rawat Inap (" + inpatientVisits + ")");
        }

        int outpatientVisits = parseSafeInt(patientData.get("number_outpatient"));
        if (outpatientVisits > 2) {
            factors.add("Beberapa kunjungan rawat jalan (" + outpatientVisits + ")");
        }

        int age = parseSafeInt(patientData.get("age"));
        if (age > 65) {
            factors.add("Usia lanjut (" + age + " tahun)");
        } else if (age > 50) {
            factors.add("Usia diatas 50 (" + age + " tahun)");
        }

        String[] diags = { patientData.get("diag_1"), patientData.get("diag_2"), patientData.get("diag_3") };

        if (anyMatches(diags, this::containsNeuropathyCode)) {
            factors.add("Ada diagnosis Neuropati");
        }
        if (anyMatches(diags, this::containsDiabetesCode)) {
            factors.add("Ada diagnosis Diabetes");
        }
        if (anyMatches(diags, this::containsVascularDiseaseCode)) {
            factors.add("Penyakit pembuluh darah perifer");
        }
        if (anyMatches(diags, this::containsRenalDiseaseCode)) {
            factors.add("Penyakit ginjal");
        }

        return factors.toArray(new String[0]);
    }

    private boolean containsNeuropathyCode(String diagCode) {
        if (diagCode == null || diagCode.isEmpty()) {
            return false;
        }
        String[] neuropathyCodes = {
                "357.0",
                "356",
                "357.2"
        };
        for (String code : neuropathyCodes) {
            if (diagCode.startsWith(code)) {
                return true;
            }
        }
        return false;
    }
    private boolean containsDiabetesCode(String diagCode) {
        if (diagCode == null || diagCode.isEmpty()) {
            return false;
        }
        String[] diabetesCodes = {
                "250",
                "250.01",
                "250.02",
                "250.6",
                "249.0",
                "249.6",

        };
        for (String code : diabetesCodes) {
            if (diagCode.startsWith(code)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsVascularDiseaseCode(String diagCode) {
        if (diagCode == null || diagCode.isEmpty()) {
            return false;
        }

        String[] pvdCodes = {
                "440",
                "443",
                "443.81",
                "785.4"
        };
        for (String code : pvdCodes) {
            if (diagCode.startsWith(code)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsRenalDiseaseCode(String diagCode) {
        if (diagCode == null || diagCode.isEmpty()) {
            return false;
        }

        String[] renalCodes = {
                "585",
                "584"
        };
        for (String code : renalCodes) {
            if (diagCode.startsWith(code)) {
                return true;
            }
        }
        return false;
    }
    private int parseSafeInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private float parseSafeFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean anyMatches(String[] values, Predicate<String> checker) {
        for (String val : values) {
            if (val != null && checker.test(val)) {
                return true;
            }
        }
        return false;
    }
}