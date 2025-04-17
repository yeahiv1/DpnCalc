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
import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {
    private TextView riskLevelText;
    private TextView riskFactorsText;
    private TextView recommendationsText;
    private Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_results);

        riskLevelText = findViewById(R.id.risk_level_text);
        riskFactorsText = findViewById(R.id.risk_factors_text);
        recommendationsText = findViewById(R.id.recommendations_text);
        returnButton = findViewById(R.id.return_button);

        Intent intent = getIntent();
        double dpnRiskScore = intent.getDoubleExtra("dpnRiskScore", 0.0);
        String dpnRiskLevel = intent.getStringExtra("dpnRiskLevel");

        HashMap<String, String> patientData =
                (HashMap<String, String>) intent.getSerializableExtra("patientData");

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
        StringBuilder recommendations = new StringBuilder();

        if (riskLevel.equalsIgnoreCase("Low Risk")) {
            recommendations.append("• Lanjutkan perawatan diabetes secara rutin\n");
            recommendations.append("• Lakukan pemeriksaan kaki tahunan\n");
            recommendations.append("• Pantau perubahan pada sensasi kaki\n");
            recommendations.append("• Jaga kebersihan kaki dan gunakan pelembap\n");
            recommendations.append("• Gunakan sepatu yang pas dan nyaman");
        } else if (riskLevel.equalsIgnoreCase("Medium Risk")) {
            recommendations.append("• Jadwalkan kunjungan kontrol dalam 3 bulan\n");
            recommendations.append("• Pertimbangkan penggunaan alas kaki khusus\n");
            recommendations.append("• Lakukan pemeriksaan kaki harian\n");
            recommendations.append("• Tinjau kembali pengelolaan gula darah\n");
            recommendations.append("• Hindari berjalan tanpa alas kaki\n");
            recommendations.append("• Pertimbangkan suplemen vitamin B");
        } else if (riskLevel.equalsIgnoreCase("High Risk")) {
            recommendations.append("• Segera rujuk ke dokter spesialis kaki (podiatris)\n");
            recommendations.append("• Wajib menggunakan alas kaki pelindung\n");
            recommendations.append("• Tingkatkan pemantauan gula darah\n");
            recommendations.append("• Lakukan pemeriksaan kaki klinis setiap bulan\n");
            recommendations.append("• Pertimbangkan konsultasi neurologi\n");
            recommendations.append("• Lakukan penilaian manajemen nyeri\n");
            recommendations.append("• Disarankan evaluasi pembuluh darah");
        }
        recommendationsText.setText(recommendations.toString());
    }
    private String[] determineContributingFactors(HashMap<String, String> patientData) {
        ArrayList<String> factors = new ArrayList<>();

        try {
            float bmi = Float.parseFloat(patientData.get("bmi"));
            if (bmi >= 30) {
                factors.add("Obesitas (BMI: " + bmi + ")");
            } else if (bmi >= 25) {
                factors.add("Overweight (BMI: " + bmi + ")");
            }
        } catch (NumberFormatException | NullPointerException e) {
            // Skip
        }

        String a1cResult = patientData.get("A1Cresult");
        if (a1cResult != null) {
            if (a1cResult.equals(">8") || a1cResult.equals(">7")) {
                factors.add("Kontrol glikemik yang buruk (A1C: " + a1cResult + ")");
            }
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

        try {
            int emergencyVisits = Integer.parseInt(patientData.get("number_emergency"));
            if (emergencyVisits > 0) {
                factors.add("Kunjungan darurat (" + emergencyVisits + ")");
            }
        } catch (NumberFormatException | NullPointerException e) {
            // Skip if value is invalid
        }

        try {
            int inpatientVisits = Integer.parseInt(patientData.get("number_inpatient"));
            if (inpatientVisits > 0) {
                factors.add("Rawat Inap (" + inpatientVisits + ")");
            }
        } catch (NumberFormatException | NullPointerException e) {
            // Skip if value is invalid
        }

        try {
            int outpatientVisits = Integer.parseInt(patientData.get("number_outpatient"));
            if (outpatientVisits > 2) {
                factors.add("Beberapa kunjungan rawat jalan (" + outpatientVisits + ")");
            }
        } catch (NumberFormatException | NullPointerException e) {
            // Skip if value is invalid
        }

        try {
            int age = Integer.parseInt(patientData.get("age"));
            if (age > 65) {
                factors.add("Usia lanjut (" + age + " tahun)");
            } else if (age > 50) {
                factors.add("Usia diatas 50 (" + age + " tahun)");
            }
        } catch (NumberFormatException | NullPointerException e) {
            // Skip if age value is invalid
        }

        String diag1 = patientData.get("diag_1");
        String diag2 = patientData.get("diag_2");
        String diag3 = patientData.get("diag_3");

        if (containsNeuropathyCode(diag1) || containsNeuropathyCode(diag2) || containsNeuropathyCode(diag3)) {
            factors.add("Ada diagnosis Neuropati");
        }
        if (containsDiabetesCode(diag1) || containsDiabetesCode(diag2) || containsDiabetesCode(diag3)) {
            factors.add("Ada diagnosis Diabetes");
        }

        if (containsVascularDiseaseCode(diag1) || containsVascularDiseaseCode(diag2) || containsVascularDiseaseCode(diag3)) {
            factors.add("Penyakit pembuluh darah perifer");
        }

        if (containsRenalDiseaseCode(diag1) || containsRenalDiseaseCode(diag2) || containsRenalDiseaseCode(diag3)) {
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
}