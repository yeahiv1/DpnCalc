package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import ai.onnxruntime.*;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class DpnCalcActivity extends AppCompatActivity {
    private OrtEnvironment ortEnvironment;
    private OrtSession ortSession;
    private static final String MODEL_NAME = "model_1 (3).onnx";

    private AutoCompleteTextView raceDropdown, genderDropdown, maxGluSerumDropdown,
            a1cResultDropdown, readmittedDropdown,  diag1Input, diag2Input, diag3Input;
    private TextInputEditText ageInput;

    private TextInputEditText numLabProceduresInput;
    private TextInputEditText numProceduresInput;
    private TextInputEditText numMedicationsInput;
    private TextInputEditText numberOutpatientInput;
    private TextInputEditText numberEmergencyInput;
    private TextInputEditText numberInpatientInput;
    private TextInputEditText numberDiagnosesInput;
    private TextInputEditText heightInput;
    private TextInputEditText weightInput;
    private CheckBox metforminCheckbox, insulinCheckbox, diabetesMedCheckbox, changeMedCheckbox;
    private CheckBox kesemutanCheckbox, matirasaCheckbox, senspanasCheckbox, nyeriCheckbox, lemahototCheckbox, jarumCheckbox;
    private TextView resultText;
    private Button calculateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dpn_calc);
        initializeViews();
        setupDropdowns();
        initializeOnnxRuntime();


        calculateButton.setOnClickListener(v -> calculateAndDisplayRisk());
        setupBmiCalculation();
        setupResetFunctionality();
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initializeViews() {
        raceDropdown = findViewById(R.id.raceSpinner);
        genderDropdown = findViewById(R.id.genderSpinner);
        maxGluSerumDropdown = findViewById(R.id.maxGluSerumSpinner);
        a1cResultDropdown = findViewById(R.id.a1cResultSpinner);
        readmittedDropdown = findViewById(R.id.readmittedSpinner);

        ageInput = findViewById(R.id.ageInput);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        findViewById(R.id.bmiInput);
        numLabProceduresInput = findViewById(R.id.numLabProceduresInput);
        numProceduresInput = findViewById(R.id.numProceduresInput);
        numMedicationsInput = findViewById(R.id.numMedicationsInput);
        numberOutpatientInput = findViewById(R.id.numberOutpatientInput);
        numberEmergencyInput = findViewById(R.id.numberEmergencyInput);
        numberInpatientInput = findViewById(R.id.numberInpatientInput);
        diag1Input = findViewById(R.id.diag1Input);
        diag2Input = findViewById(R.id.diag2Input);
        diag3Input = findViewById(R.id.diag3Input);
        numberDiagnosesInput = findViewById(R.id.numberDiagnosesInput);

        metforminCheckbox = findViewById(R.id.metforminCheckbox);
        insulinCheckbox = findViewById(R.id.insulinCheckbox);
        diabetesMedCheckbox = findViewById(R.id.diabetesMedCheckbox);
        changeMedCheckbox = findViewById(R.id.changeMedCheckbox);

        kesemutanCheckbox = findViewById(R.id.kesemutanCheckbox);
        matirasaCheckbox = findViewById(R.id.matirasaCheckbox);
        senspanasCheckbox = findViewById(R.id.senspanasCheckbox);
        nyeriCheckbox = findViewById(R.id.nyeriCheckbox);
        lemahototCheckbox = findViewById(R.id.lemahototCheckbox);
        jarumCheckbox = findViewById(R.id.jarumCheckbox);

        resultText = findViewById(R.id.resultText);
        calculateButton = findViewById(R.id.calculateButton);
    }
    private void setupBmiCalculation() {
        Button calculateBmiButton = findViewById(R.id.calculateBmiButton);
        final TextInputEditText heightInput = findViewById(R.id.heightInput);
        final TextInputEditText weightInput = findViewById(R.id.weightInput);
        final TextInputEditText bmiInput = findViewById(R.id.bmiInput);

        calculateBmiButton.setOnClickListener(v -> {
            String heightText = heightInput.getText().toString().trim();
            String weightText = weightInput.getText().toString().trim();

            if (heightText.isEmpty() || weightText.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Harap masukkan tinggi dan berat badan", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double heightInMeters = Double.parseDouble(heightText) / 100;
                double weightInKg = Double.parseDouble(weightText);

                if (heightInMeters <= 0 || weightInKg <= 0) {
                    Toast.makeText(getApplicationContext(), "Tinggi dan berat harus bernilai positif", Toast.LENGTH_SHORT).show();
                    return;
                }

                double bmi = weightInKg / (heightInMeters * heightInMeters);

                String formattedBmi = String.format(Locale.US, "%.1f", bmi);

                bmiInput.setText(formattedBmi);

                String bmiCategory;
                if (bmi < 18.5) {
                    bmiCategory = "Underweight";
                } else if (bmi < 25) {
                    bmiCategory = "Normal weight";
                } else if (bmi < 30) {
                    bmiCategory = "Overweight";
                } else if (bmi < 35) {
                    bmiCategory = "Obesity class I";
                } else if (bmi < 40) {
                    bmiCategory = "Obesity class II";
                } else {
                    bmiCategory = "Obesity class III";
                }

                Toast.makeText(getApplicationContext(), "BMI Category: " + bmiCategory, Toast.LENGTH_LONG).show();

            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Harap masukkan angka yang valid untuk tinggi dan berat badan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDropdowns() {
        String[] raceItems = RACE_MAP.keySet().toArray(new String[0]);
        String[] genderItems = GENDER_MAP.keySet().toArray(new String[0]);
        String[] maxGluSerumItems = MAX_GLUCOSE_MAP.keySet().toArray(new String[0]);
        String[] a1cResultItems = A1C_RESULT_MAP.keySet().toArray(new String[0]);
        String[] readmittedItems = READMITTED_MAP.keySet().toArray(new String[0]);
        String[] diagnosisItems = {
                "250 - Diabetes melitus",
                "250.01 - Diabetes melitus tipe 1",
                "250.02 - Diabetes melitus tipe 2",
                "250.6 - Diabetes dengan neuropati",
                "249.0 - Diabetes melitus sekunder",
                "249.6 - Diabetes sekunder dengan neuropati",
                "356 - Neuropati idiopatik",
                "357.0 - Neuropati inflamatori",
                "357.2 - Polineuropati diabetik",
                "440 - Aterosklerosis",
                "443 - Penyakit pembuluh darah perifer",
                "443.81 - Angiopati perifer",
                "428 - Gagal jantung",
                "401 - Hipertensi",
                "414 - Penyakit jantung iskemik",
                "278 - Obesitas",
                "278.01 - Obesitas morbid",
                "585 - Penyakit ginjal kronis",
                "584 - Gagal ginjal akut",
                "707 - Ulkus kronis",
                "707.1 - Ulkus pada tungkai bawah",
                "782.0 - Gangguan sensasi kulit",
                "785.4 - Gangren",
                "E938 - Efek samping obat pada sistem saraf pusat",
                "E940 - Efek samping obat - antibiotik",
                "E941 - Efek samping obat - anti-infeksi lainnya",
                "V45 - Status pasca tindakan medis",
                "V49.75 - Status amputasi tungkai bawah",
                "V58.67 - Penggunaan insulin jangka panjang"
        };

        setupDropdown(raceDropdown, raceItems);
        setupDropdown(genderDropdown, genderItems);
        setupDropdown(maxGluSerumDropdown, maxGluSerumItems);
        setupDropdown(a1cResultDropdown, a1cResultItems);
        setupDropdown(readmittedDropdown, readmittedItems);
        setupDropdown(diag1Input, diagnosisItems);
        setupDropdown(diag2Input, diagnosisItems);
        setupDropdown(diag3Input, diagnosisItems);
    }
    private void setupDropdown(AutoCompleteTextView dropdown, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, items);
        dropdown.setAdapter(adapter);
        dropdown.setThreshold(1);
    }

    private void initializeOnnxRuntime() {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment();
            ortSession = createORTSession();
        } catch (Exception e) {
            Toast.makeText(this, "Kesalahan saat menginisialisasi model: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
    private OrtSession createORTSession() throws IOException {
        try {
            String modelPath = copyModelToCache();
            return ortEnvironment.createSession(modelPath, new OrtSession.SessionOptions());
        } catch (OrtException e) {
            throw new IOException("Gagal membuat sesi ONNX Runtime: " + e.getMessage(), e);
        }
    }
    private String copyModelToCache() throws IOException {
        File modelFile = new File(getCacheDir(), MODEL_NAME);
        if (!modelFile.exists()) {
            InputStream modelInput = getAssets().open(MODEL_NAME);
            FileOutputStream modelOutput = new FileOutputStream(modelFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = modelInput.read(buffer)) != -1) {
                modelOutput.write(buffer, 0, bytesRead);
            }
            modelInput.close();
            modelOutput.close();
        }
        return modelFile.getAbsolutePath();
    }
    public class PreprocessingConstants {
        public static final float NUM_LAB_PROCEDURES_MEAN = 43.09564f;
        public static final float NUM_LAB_PROCEDURES_STD = 19.67427f;
        public static final float NUM_PROCEDURES_MEAN = 43.09564f;
        public static final float NUM_PROCEDURES_STD = 19.67427f;

        public static final float NUM_MEDICATIONS_MEAN = 16.02184f;
        public static final float NUM_MEDICATIONS_STD = 8.12753f;

        public static final float NUM_DIAGNOSES_MEAN = 7.42261f;
        public static final float NUM_DIAGNOSES_STD = 1.93359f;
        public static final float NUM_OUTPATIENT_MEAN = 0.63557f;
        public static final float NUM_OUTPATIENT_STD = 1.26286f;

        public static final float NUM_EMERGENCY_MEAN = 0.19784f;
        public static final float NUM_EMERGENCY_STD = 0.93047f;

        public static final float NUM_INPATIENT_MEAN = 0.63557f;
        public static final float NUM_INPATIENT_STD = 1.26286f;

        public static final float AGE_MEAN = 6.09670f;
        public static final float AGE_STD = 1.59408f;
    }
    public static final Map<String, Float> MEDICATION_STATUS;
    public static final Map<String, Float> A1C_RESULT;
    public static final Map<String, Float> GLU_RESULT;

    static {
        MEDICATION_STATUS = new HashMap<>();
        MEDICATION_STATUS.put("No", 0.0f);
        MEDICATION_STATUS.put("Steady", 1.0f);
        MEDICATION_STATUS.put("Up", 2.0f);
        MEDICATION_STATUS.put("Down", 3.0f);

        A1C_RESULT = new HashMap<>();
        A1C_RESULT.put("None", 0.0f);
        A1C_RESULT.put("Norm", 1.0f);
        A1C_RESULT.put(">7", 2.0f);
        A1C_RESULT.put(">8", 3.0f);

        GLU_RESULT = new HashMap<>();
        GLU_RESULT.put("None", 0.0f);
        GLU_RESULT.put("Norm", 1.0f);
        GLU_RESULT.put(">200", 2.0f);
        GLU_RESULT.put(">300", 3.0f);
    }
    public static final Map<String, String> RACE_MAP;
    public static final Map<String, String> GENDER_MAP;
    public static final Map<String, String> MAX_GLUCOSE_MAP;
    public static final Map<String, String> A1C_RESULT_MAP;
    public static final Map<String, String> READMITTED_MAP;

    static {
        RACE_MAP = new LinkedHashMap<>();
        RACE_MAP.put("Kulit Putih", "Caucasian");
        RACE_MAP.put("Kulit Hitam", "African American");
        RACE_MAP.put("Asia", "Asian");
        RACE_MAP.put("Hispanik", "Hispanic");
        RACE_MAP.put("Lainnya", "Other");

        GENDER_MAP = new LinkedHashMap<>();
        GENDER_MAP.put("Laki-laki", "Male");
        GENDER_MAP.put("Perempuan", "Female");
        GENDER_MAP.put("Lainnya", "Other");

        MAX_GLUCOSE_MAP = new LinkedHashMap<>();
        MAX_GLUCOSE_MAP.put(">200", ">200");
        MAX_GLUCOSE_MAP.put(">300", ">300");
        MAX_GLUCOSE_MAP.put("Normal", "Normal");
        MAX_GLUCOSE_MAP.put("Tidak Ada", "None");

        A1C_RESULT_MAP = new LinkedHashMap<>();
        A1C_RESULT_MAP.put(">7", ">7");
        A1C_RESULT_MAP.put(">8", ">8");
        A1C_RESULT_MAP.put("Normal", "Normal");
        A1C_RESULT_MAP.put("Tidak Ada", "None");

        READMITTED_MAP = new LinkedHashMap<>();
        READMITTED_MAP.put("Tidak", "No");
        READMITTED_MAP.put("<30 hari", "<30 days");
        READMITTED_MAP.put(">30 hari", ">30 days");
    }

    private void calculateAndDisplayRisk() {
        if (!validateInputs()) {
            return;
        }
        String diag1 = extractDiagnosisCode(diag1Input.getText().toString());
        String diag2 = extractDiagnosisCode(diag2Input.getText().toString());
        String diag3 = extractDiagnosisCode(diag3Input.getText().toString());
        if (hasDuplicateDiagnoses(diag1, diag2, diag3)) {
            Toast.makeText(this, "Diagnosis tidak boleh sama", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, String> patientData = new HashMap<>();

        patientData.put("diag_1", diag1);
        patientData.put("diag_2", diag2);
        patientData.put("diag_3", diag3);

        float bmi = calculateBmi();
        patientData.put("bmi", String.valueOf(bmi));
        patientData.put("height", heightInput.getText().toString());
        patientData.put("weight", weightInput.getText().toString());

        patientData.put("A1Cresult", a1cResultDropdown.getText().toString());
        patientData.put("gluresult", maxGluSerumDropdown.getText().toString());
        patientData.put("readmitted", readmittedDropdown.getText().toString());

        patientData.put("insulin", insulinCheckbox.isChecked() ? "Steady" : "No");
        patientData.put("metformin", metforminCheckbox.isChecked() ? "Steady" : "No");
        patientData.put("diabetesMed", diabetesMedCheckbox.isChecked() ? "Yes" : "No");
        patientData.put("change", changeMedCheckbox.isChecked() ? "Ch" : "No");

        patientData.put("age", ageInput.getText().toString());
        patientData.put("num_lab_procedures", numLabProceduresInput.getText().toString());
        patientData.put("num_medications", numMedicationsInput.getText().toString());
        patientData.put("number_outpatient", numberOutpatientInput.getText().toString());
        patientData.put("number_emergency", numberEmergencyInput.getText().toString());
        patientData.put("number_inpatient", numberInpatientInput.getText().toString());
        patientData.put("number_diagnoses", numberDiagnosesInput.getText().toString());

        ArrayList<String> selectedSymptoms = getSelectedSymptoms();
        double dpnRisk = calculateDPNRisk(patientData);
        String riskLevel = interpretDPNRiskScore(dpnRisk);

        try {
            float[] inputData = preprocessInput();
            float[] result = runInference(inputData);
            updateResultWithModelPrediction((double) result[0]);
        } catch (Exception e) {
            Log.e("DpnCalc", "Kesalahan dalam inferensi ONNX: " + e.getMessage(), e);
        }
        Intent intent = new Intent(DpnCalcActivity.this, ResultActivity.class);

        intent.putExtra("dpnRiskScore", dpnRisk);
        intent.putExtra("dpnRiskLevel", riskLevel);
        intent.putExtra("patientData", patientData);
        intent.putStringArrayListExtra("selectedSymptoms", selectedSymptoms);

        startActivity(intent);
    }
    private float calculateBmi() {
        try {
            float heightCm = Float.parseFloat(heightInput.getText().toString());
            float weightKg = Float.parseFloat(weightInput.getText().toString());

            if (heightCm > 0 && weightKg > 0) {
                float heightM = heightCm / 100f;
                return weightKg / (heightM * heightM);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        return 0f;
    }
    private void updateResultWithModelPrediction(double modelScore) {
        String currentText = resultText.getText().toString();
        String modelRiskLevel = interpretDPNRiskScore(modelScore);
        resultText.setText(currentText + "\nModel Prediction: " + modelRiskLevel +
                String.format(" (Score: %.2f)", modelScore));
    }

    private String interpretDPNRiskScore(double score) {
        if (score < 0.3) return "Low Risk";
        else if (score < 0.7) return "Medium Risk";
        else return "High Risk";
    }

    public static double calculateDPNRisk(Map<String, String> patientData) {
        double diagnosisScore = getDiagnosisScore(patientData);
        double riskScore = getRiskFactorScore(patientData);

        double MAX_DIAGNOSIS_SCORE = 6.0;
        double MAX_RISK_SCORE = 8.0;
        double BASE_RISK = 0.1;
        double DIAGNOSIS_WEIGHT = 0.4;
        double RISK_FACTOR_WEIGHT = 0.4;
        double INTERACTION_WEIGHT = 0.3;

        double normDiagnosis = Math.min(diagnosisScore / MAX_DIAGNOSIS_SCORE, 1.0);
        double normRisk = Math.min(riskScore / MAX_RISK_SCORE, 1.0);


        double ageAdjustment = 0.0;
        if (patientData.containsKey("age")) {
            try {
                double age = Double.parseDouble(patientData.get("age"));
                double baseAge = 40.0;

                if (age >= baseAge) {
                    double maxAdjustment = 0.20;
                    double growthRate = 0.6;
                    ageAdjustment = maxAdjustment * (1 - Math.exp(-growthRate * (age - baseAge) / 30));
                    ageAdjustment = Math.min(maxAdjustment, Math.max(0, ageAdjustment));
                }
            } catch (NumberFormatException ignored) {}
        }
        double risk = BASE_RISK + ageAdjustment +
                (normDiagnosis * DIAGNOSIS_WEIGHT) +
                (normRisk * RISK_FACTOR_WEIGHT);

        risk *= (1.0 + (normDiagnosis * normRisk * INTERACTION_WEIGHT));

        double confidence = 1.0;
        if (patientData.containsKey("number_diagnoses")) {
            try {
                int diagnoses = Integer.parseInt(patientData.get("number_diagnoses"));
                confidence = Math.min(1.0, diagnoses / 5.0);
            } catch (NumberFormatException ignored) {}
        }

        double adjustedRisk = (risk * confidence) + ((BASE_RISK + ageAdjustment) * (1 - confidence));

        return Math.max(0.1, Math.min(adjustedRisk, 1.0));
    }

    private static double getDiagnosisScore(Map<String, String> patientData) {
        double score = 0;
        double severityMultiplier = 1.0;

        String[] diagCodes = new String[3];
        for (int i = 1; i <= 3; i++) {
            diagCodes[i - 1] = patientData.get("diag_" + i);
        }

        List<String> conditions = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        boolean hasDiabetes = false;
        boolean hasNeuropathy = false;

        for (String codeStr : diagCodes) {
            if (codeStr == null || codeStr.trim().isEmpty()) {
                continue;
            }

            ICDInfo info = getICDInfo(codeStr);
            if (info != null) {
                score += info.severity;
                conditions.add(info.condition);
                categories.add(info.category);

                if (info.condition.contains("diabetes")) hasDiabetes = true;
                if (info.condition.contains("neuropathy")) hasNeuropathy = true;
            }
        }

        if (conditions.isEmpty()) {
            return 0.1;
        }

        double interactionScore = getConditionInteractionScore(conditions, categories);
        score += interactionScore;

        if (hasDiabetes && hasNeuropathy) {
            severityMultiplier = 1.2;
        }

        return Math.max(0.1, score * severityMultiplier);
    }


    private static double getRiskFactorScore(Map<String, String> patientData) {
        double score = 0.1;

        Map<String, Double> a1cMap = new HashMap<>();
        a1cMap.put(">8", 2.0);
        a1cMap.put(">7", 1.5);
        a1cMap.put("Normal", 0.0);
        a1cMap.put("Norm", 0.0);
        a1cMap.put("None", 0.0);

        Map<String, Double> gluMap = new HashMap<>();
        gluMap.put(">300", 2.0);
        gluMap.put(">200", 1.5);
        gluMap.put("Normal", 0.0);
        gluMap.put("Norm", 0.0);
        gluMap.put("None", 0.0);
        String a1cResult = patientData.getOrDefault("A1Cresult", "");
        String gluResult = patientData.getOrDefault("gluresult", "");
        String insulin = patientData.getOrDefault("insulin", "");
        String metformin = patientData.getOrDefault("metformin", "");
        String maxGluSerum = patientData.getOrDefault("max_glu_serum", "");
        String change = patientData.getOrDefault("change", "");

        // A1C & Glucose score
        if (a1cMap.containsKey(a1cResult)) {
            score += a1cMap.get(a1cResult);
        }
        if (gluMap.containsKey(gluResult)) {
            score += gluMap.get(gluResult);
        }

        // Medication usage
        if ("Steady".equals(insulin) || "Up".equals(insulin)) {
            score += 1.5;
        }
        if ("Steady".equals(metformin) || "Up".equals(metformin)) {
            score += 1.0;
        }
        if ("Ch".equals(change)) {
            score += 0.8;
        }

        int numOutpatient = parseIntOrZero(patientData.get("number_outpatient"));
        int numEmergency = parseIntOrZero(patientData.get("number_emergency"));
        int numInpatient = parseIntOrZero(patientData.get("number_inpatient"));

        if (numEmergency > 0) score += 0.5 * Math.min(numEmergency, 3);
        if (numInpatient > 0) score += 0.5 * Math.min(numInpatient, 3);

        Double bmi = parseDoubleOrNull(patientData.get("bmi"));
        if (bmi != null) {
            if (bmi >= 40) score += 2.0;
            else if (bmi >= 35) score += 1.5;
            else if (bmi >= 30) score += 1.0;
            else if (bmi >= 25) score += 0.5;
        }

        int numLabProcedures = parseIntOrZero(patientData.get("num_lab_procedures"));
        int numProcedures = parseIntOrZero(patientData.get("num_procedures"));
        int numMedications = parseIntOrZero(patientData.get("num_medications"));

        if (numLabProcedures > 0) score += 0.05 * Math.min(numLabProcedures, 100);
        if (numProcedures > 0) score += 0.2 * Math.min(numProcedures, 10);
        if (numMedications > 0) score += 0.1 * Math.min(numMedications, 50);

        double interactionScore = 0.0;
        Set<Object> conditions = new HashSet<>();
        conditions.add(patientData.get("A1Cresult"));
        conditions.add(patientData.get("max_glu_serum"));
        conditions.add(patientData.get("insulin"));
        conditions.add(bmi);

        boolean gluCondition = conditions.contains(">200") || conditions.contains(">300");
        boolean a1cCondition = conditions.contains(">7") || conditions.contains(">8");

        if (gluCondition && a1cCondition) interactionScore += 0.5;

        if (("Steady".equals(insulin) || "Up".equals(insulin)) &&
                (">200".equals(maxGluSerum) || ">300".equals(maxGluSerum)) &&
                numEmergency > 0) {
            interactionScore += 0.6;
        }

        if (bmi != null && bmi >= 30 && (">7".equals(a1cResult) || ">8".equals(a1cResult))) {
            interactionScore += 0.5;
        }

        int riskFactorCount = 0;
        if (">7".equals(a1cResult) || ">8".equals(a1cResult)) riskFactorCount++;
        if (">200".equals(maxGluSerum) || ">300".equals(maxGluSerum)) riskFactorCount++;
        if ("Steady".equals(insulin) || "Up".equals(insulin)) riskFactorCount++;
        if (bmi != null && bmi >= 30) riskFactorCount++;
        if (numEmergency > 0) riskFactorCount++;
        if (numInpatient > 0) riskFactorCount++;

        interactionScore += Math.min(Math.max(riskFactorCount - 2, 0), 4) * 0.2;

        int totalVisits = numOutpatient + numEmergency + numInpatient;
        double hospitalizationRatio = 0.0;
        if (totalVisits > 0) {
            hospitalizationRatio = (double) numInpatient / totalVisits;
            hospitalizationRatio = Math.min(hospitalizationRatio, 1.0);
        }
        score += hospitalizationRatio * 2.0;


        return score + interactionScore;
    }


    private static int parseIntOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    private float[] preprocessInput() {
        float[] input = new float[19];

        try {
            input[0] = convertRaceToFloat(raceDropdown.getText().toString());
            input[1] = convertGenderToFloat(genderDropdown.getText().toString());
            input[2] = standardizeValue(
                    parseFloatSafely(ageInput.getText().toString()),
                    PreprocessingConstants.AGE_MEAN,
                    PreprocessingConstants.AGE_STD
            );

            input[3] = standardizeValue(
                    parseFloatSafely(numLabProceduresInput.getText().toString()),
                    PreprocessingConstants.NUM_LAB_PROCEDURES_MEAN,
                    PreprocessingConstants.NUM_LAB_PROCEDURES_STD
            );
            input[4] = standardizeValue(
                    parseFloatSafely(numProceduresInput.getText().toString()),
                    PreprocessingConstants.NUM_PROCEDURES_MEAN,
                    PreprocessingConstants.NUM_PROCEDURES_STD
            );
            input[5] = standardizeValue(
                    parseFloatSafely(numMedicationsInput.getText().toString()),
                    PreprocessingConstants.NUM_MEDICATIONS_MEAN,
                    PreprocessingConstants.NUM_MEDICATIONS_STD
            );
            input[6] = standardizeValue(
                    parseFloatSafely(numberDiagnosesInput.getText().toString()),
                    PreprocessingConstants.NUM_DIAGNOSES_MEAN,
                    PreprocessingConstants.NUM_DIAGNOSES_STD
            );
            input[7] = standardizeValue(
                    parseFloatSafely(numberOutpatientInput.getText().toString()),
                    PreprocessingConstants.NUM_OUTPATIENT_MEAN,
                    PreprocessingConstants.NUM_OUTPATIENT_STD
            );
            input[8] = standardizeValue(
                    parseFloatSafely(numberEmergencyInput.getText().toString()),
                    PreprocessingConstants.NUM_EMERGENCY_MEAN,
                    PreprocessingConstants.NUM_EMERGENCY_STD
            );
            input[9] = standardizeValue(
                    parseFloatSafely(numberInpatientInput.getText().toString()),
                    PreprocessingConstants.NUM_INPATIENT_MEAN,
                    PreprocessingConstants.NUM_INPATIENT_STD
            );
            input[10] = convertMaxGluSerumToFloat(maxGluSerumDropdown.getText().toString());
            input[11] = convertA1CResultToFloat(a1cResultDropdown.getText().toString());
            input[12] = metforminCheckbox.isChecked() ? 1.0f : 0.0f;
            input[13] = insulinCheckbox.isChecked() ? 1.0f : 0.0f;
            input[14] = diabetesMedCheckbox.isChecked() ? 1.0f : 0.0f;
            input[15] = changeMedCheckbox.isChecked() ? 1.0f : 0.0f;
            input[16] = parseFloatSafely(heightInput.getText().toString()) / 100.0f;
            input[17] = parseFloatSafely(weightInput.getText().toString());
            input[18] = calculateBmi();

        } catch (Exception e) {
            Log.e("PreprocessInput", "Error processing inputs: " + e.getMessage());
            Toast.makeText(this, "Error processing inputs: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        return input;
    }
    private void setupResetFunctionality() {
        Button resetButton = findViewById(R.id.resetButton);
        final ScrollView scrollView = findViewById(R.id.scrollView);
        resetButton.setOnClickListener(v -> {
            ((AutoCompleteTextView) findViewById(R.id.raceSpinner)).setText("", false);
            ((AutoCompleteTextView) findViewById(R.id.genderSpinner)).setText("", false);
            ((TextInputEditText) findViewById(R.id.ageInput)).setText("");

            ((TextInputEditText) findViewById(R.id.heightInput)).setText("");
            ((TextInputEditText) findViewById(R.id.weightInput)).setText("");
            ((TextInputEditText) findViewById(R.id.bmiInput)).setText("");

            ((TextInputEditText) findViewById(R.id.numLabProceduresInput)).setText("");
            ((TextInputEditText) findViewById(R.id.numProceduresInput)).setText("");
            ((TextInputEditText) findViewById(R.id.numMedicationsInput)).setText("");

            ((TextInputEditText) findViewById(R.id.numberOutpatientInput)).setText("");
            ((TextInputEditText) findViewById(R.id.numberEmergencyInput)).setText("");
            ((TextInputEditText) findViewById(R.id.numberInpatientInput)).setText("");

            ((AutoCompleteTextView) findViewById(R.id.diag1Input)).setText("");
            ((AutoCompleteTextView) findViewById(R.id.diag2Input)).setText("");
            ((AutoCompleteTextView) findViewById(R.id.diag3Input)).setText("");
            ((TextInputEditText) findViewById(R.id.numberDiagnosesInput)).setText("");

            ((AutoCompleteTextView) findViewById(R.id.maxGluSerumSpinner)).setText("", false);
            ((AutoCompleteTextView) findViewById(R.id.a1cResultSpinner)).setText("", false);

            ((CheckBox) findViewById(R.id.metforminCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.insulinCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.diabetesMedCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.changeMedCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.kesemutanCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.matirasaCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.senspanasCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.nyeriCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.lemahototCheckbox)).setChecked(false);
            ((CheckBox) findViewById(R.id.jarumCheckbox)).setChecked(false);

            ((AutoCompleteTextView) findViewById(R.id.readmittedSpinner)).setText("", false);

            findViewById(R.id.resultText).setVisibility(View.GONE);
            findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
            findViewById(R.id.loadingText).setVisibility(View.GONE);

            Toast.makeText(getApplicationContext(), "Form telah direset", Toast.LENGTH_SHORT).show();
            scrollView.smoothScrollTo(0, 0);
        });
    }

    private static final Map<String, ICDInfo> icdMap = new HashMap<>();
    static {
        icdMap.put("250", new ICDInfo("diabetes", 1.0f, "endocrine"));
        icdMap.put("250.01", new ICDInfo("type_1_diabetes", 1.2f, "endocrine"));
        icdMap.put("250.02", new ICDInfo("type_2_diabetes", 1.1f, "endocrine"));
        icdMap.put("250.6", new ICDInfo("diabetes_with_neuropathy", 3.0f, "endocrine"));
        icdMap.put("249", new ICDInfo("secondary_diabetes", 0.8f, "endocrine"));
        icdMap.put("249.6", new ICDInfo("secondary_diabetes_with_neuropathy", 2.8f, "endocrine"));
        icdMap.put("356", new ICDInfo("hereditary_idiopathic_neuropathy", 2.5f, "nervous"));
        icdMap.put("357.0", new ICDInfo("inflammatory_neuropathy", 2.0f, "nervous"));
        icdMap.put("357.2", new ICDInfo("diabetic_polyneuropathy", 3.5f, "nervous"));
        icdMap.put("278", new ICDInfo("obesity", 0.6f, "endocrine"));
        icdMap.put("278.01", new ICDInfo("morbid_obesity", 1.2f, "endocrine"));
        icdMap.put("707", new ICDInfo("chronic_ulcer", 2.0f, "skin"));
        icdMap.put("707.1", new ICDInfo("ulcer_lower_limbs", 2.5f, "skin"));
        icdMap.put("443.0", new ICDInfo("peripheral_vascular_disease", 1.5f, "circulatory"));
        icdMap.put("443.81", new ICDInfo("peripheral_angiopathy", 1.8f, "circulatory"));
        icdMap.put("440", new ICDInfo("atherosclerosis", 1.2f, "circulatory"));
        icdMap.put("401", new ICDInfo("hypertension", 1.0f, "circulatory"));
        icdMap.put("428", new ICDInfo("heart_failure", 2.5f, "circulatory"));
        icdMap.put("414", new ICDInfo("ischemic_heart_disease", 2.0f, "circulatory"));
        icdMap.put("785.4", new ICDInfo("gangrene", 2.5f, "symptoms"));
        icdMap.put("V49.75", new ICDInfo("lower_limb_amputation_status", 2.0f, "factors"));
    }

    private static ICDInfo getICDInfo(String code) {
        if (icdMap.containsKey(code)) {
            return icdMap.get(code);
        }
        if (code.matches("^\\d+$")) {
            String withDecimal = code + ".0";
            return icdMap.getOrDefault(withDecimal, null);
        }
        return null;
    }
    private static float getConditionInteractionScore(List<String> conditions, List<String> categories) {
        float score = 0.0f;
        Set<String> conditionSet = new HashSet<>(conditions);

        String[][][] interactions = {
                {
                        {"diabetes", "type_1_diabetes", "type_2_diabetes", "secondary_diabetes", "diabetes_with_neuropathy"},
                        {"idiopathic_neuropathy", "inflammatory_neuropathy", "diabetic_polyneuropathy", "hereditary_idiopathic_neuropathy"},
                        {"0.8"}
                },
                {
                        {"diabetes", "type_1_diabetes", "type_2_diabetes", "secondary_diabetes", "diabetes_with_neuropathy"},
                        {"atherosclerosis", "peripheral_vascular_disease", "peripheral_angiopathy"},
                        {"0.6"}
                },
                {
                        {"diabetes", "type_1_diabetes", "type_2_diabetes", "secondary_diabetes", "diabetes_with_neuropathy"},
                        {"chronic_ulcer", "ulcer_lower_limbs"},
                        {"0.7"}
                },
                {
                        {"atherosclerosis", "peripheral_vascular_disease", "peripheral_angiopathy"},
                        {"chronic_ulcer", "ulcer_lower_limbs"},
                        {"0.5"}
                },
                {
                        {"diabetes", "type_1_diabetes", "type_2_diabetes", "secondary_diabetes", "diabetes_with_neuropathy"},
                        {"obesity", "morbid_obesity"},
                        {"0.4"}
                }
        };

        for (String[][] rule : interactions) {
            Set<String> set1 = new HashSet<>(Arrays.asList(rule[0]));
            Set<String> set2 = new HashSet<>(Arrays.asList(rule[1]));
            float value = Float.parseFloat(rule[2][0]);

            if (!Collections.disjoint(conditionSet, set1) && !Collections.disjoint(conditionSet, set2)) {
                score += value;
            }
        }

        if (conditionSet.contains("gangrene")) {
            score += 1.0f;
        }

        if (conditionSet.contains("lower_limb_amputation_status") &&
                conditions.stream().anyMatch(c -> c.startsWith("diabetes"))) {
            score += 0.8f;
        }

        int categoryCount = new HashSet<>(categories).size();
        if (categoryCount >= 3) {
            score += 0.4f;
        } else if (categoryCount == 2) {
            score += 0.2f;
        }

        Set<String> diabetesSet = new HashSet<>(Arrays.asList(
                "diabetes", "type_1_diabetes", "type_2_diabetes", "secondary_diabetes", "diabetes_with_neuropathy"
        ));
        if (conditionSet.containsAll(diabetesSet) &&
                conditions.stream().anyMatch(c -> c.equals("atherosclerosis") || c.equals("peripheral_vascular_disease") || c.equals("peripheral_angiopathy")) &&
                conditions.stream().anyMatch(c -> c.equals("idiopathic_neuropathy") || c.equals("inflammatory_neuropathy") || c.equals("diabetic_polyneuropathy"))) {
            score += 1.0f;
        }

        return score;
    }

    private String extractDiagnosisCode(String diagDropdownValue) {
        if (diagDropdownValue == null || diagDropdownValue.isEmpty()) {
            return "";
        }
        String[] parts = diagDropdownValue.split(" - ", 2);
        if (parts.length > 0) {
            return parts[0].trim();
        }

        return diagDropdownValue;
    }
    private boolean hasDuplicateDiagnoses(String d1, String d2, String d3) {
        Set<String> set = new HashSet<>();
        if (!d1.isEmpty()) set.add(d1);
        if (!d2.isEmpty()) set.add(d2);
        if (!d3.isEmpty()) set.add(d3);
        int nonEmptyCount = 0;
        if (!d1.isEmpty()) nonEmptyCount++;
        if (!d2.isEmpty()) nonEmptyCount++;
        if (!d3.isEmpty()) nonEmptyCount++;
        return set.size() < nonEmptyCount;
    }
    private ArrayList<String> getSelectedSymptoms() {
        ArrayList<String> selectedSymptoms = new ArrayList<>();

        if (kesemutanCheckbox.isChecked()) selectedSymptoms.add("Kesemutan");
        if (matirasaCheckbox.isChecked()) selectedSymptoms.add("Mati rasa");
        if (senspanasCheckbox.isChecked()) selectedSymptoms.add("Sensasi terbakar");
        if (nyeriCheckbox.isChecked()) selectedSymptoms.add("Nyeri di kaki/tangan");
        if (lemahototCheckbox.isChecked()) selectedSymptoms.add("Lemah otot");
        if (jarumCheckbox.isChecked()) selectedSymptoms.add("Sensasi ditusuk jarum");

        return selectedSymptoms;
    }


    private float standardizeValue(float value, float mean, float std) {
        return (value - mean) / std;
    }

    private float convertA1CResultToFloat(String a1cResult) {
        return A1C_RESULT.getOrDefault(a1cResult, 0.0f);
    }

    private float convertMaxGluSerumToFloat(String gluResult) {
        return GLU_RESULT.getOrDefault(gluResult, 0.0f);
    }
    private boolean validateInputs() {
        if (ageInput.getText().toString().isEmpty() ||
                raceDropdown.getText().toString().isEmpty() ||
                genderDropdown.getText().toString().isEmpty() ||
                heightInput.getText().toString().isEmpty() ||
                weightInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom yang diperlukan",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            float age = parseFloatSafely(ageInput.getText().toString());
            if (age < 0 || age > 120) {
                Toast.makeText(this, "Harap masukkan usia yang valid",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            float height = parseFloatSafely(heightInput.getText().toString());
            if (height < 30 || height > 300) { // in cm, change if unit is different
                Toast.makeText(this, "Harap masukkan tinggi badan yang valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float weight = parseFloatSafely(weightInput.getText().toString());
            if (weight < 2 || weight > 500) {
                Toast.makeText(this, "Harap masukkan berat badan yang valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float labProcedures = parseFloatSafely(numLabProceduresInput.getText().toString());
            if (labProcedures < 0 || labProcedures > 200) {
                Toast.makeText(this, "Jumlah prosedur lab tidak valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float procedures = parseFloatSafely(numProceduresInput.getText().toString());
            if (procedures < 0 || procedures > 50) {
                Toast.makeText(this, "Jumlah prosedur tidak valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float medications = parseFloatSafely(numMedicationsInput.getText().toString());
            if (medications < 0 || medications > 100) {
                Toast.makeText(this, "Jumlah pengobatan tidak valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float diagnoses = parseFloatSafely(numberDiagnosesInput.getText().toString());
            if (diagnoses < 0 || diagnoses > 30) {
                Toast.makeText(this, "Jumlah diagnosis tidak valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float emergency = parseFloatSafely(numberEmergencyInput.getText().toString());
            if (emergency < 0 || emergency > 50) {
                Toast.makeText(this, "Jumlah kunjungan darurat tidak valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float inpatient = parseFloatSafely(numberInpatientInput.getText().toString());
            if (inpatient < 0 || inpatient > 50) {
                Toast.makeText(this, "Jumlah rawat inap tidak valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float outpatient = parseFloatSafely(numberOutpatientInput.getText().toString());
            if (outpatient < 0 || outpatient > 100) {
                Toast.makeText(this, "Jumlah rawat jalan tidak valid", Toast.LENGTH_SHORT).show();
                return false;
            }
            float numDiagnoses = parseFloatSafely(numberDiagnosesInput.getText().toString());
            if (numDiagnoses < 0) {
                Toast.makeText(this, "Jumlah diagnosis tidak boleh negatif",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Harap masukkan angka yang valid",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private float[] runInference(float[] inputData) throws OrtException {
        OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment,
                new float[][]{inputData});
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("input", inputTensor);

        try (OrtSession.Result results = ortSession.run(inputs)) {
            float[][] outputData = (float[][]) results.get(0).getValue();
            return outputData[0];
        }
    }

    private float parseFloatSafely(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }
    private static Double parseDoubleOrNull(Object val) {
        try {
            return val != null ? Double.parseDouble(val.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private float convertRaceToFloat(String race) {
        switch (race) {
            case "Caucasian": return 0f;
            case "African American": return 1f;
            case "Asian": return 2f;
            case "Hispanic": return 3f;
            default: return 4f;
        }
    }

    private float convertGenderToFloat(String gender) {
        switch (gender) {
            case "Male": return 0f;
            case "Female": return 1f;
            default: return 2f;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ortSession.close();
            ortEnvironment.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
