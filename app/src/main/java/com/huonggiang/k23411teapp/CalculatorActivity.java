package com.huonggiang.k23411teapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import java.text.DecimalFormat;

public class CalculatorActivity extends AppCompatActivity {

    private EditText edtFormular;
    private Button btnDel, btnC, btnCE, btnEqual;

    private static final String PREFS_NAME = "CalculatorPrefs";
    private static final String KEY_EXPRESSION = "current_expression";
    private static final String KEY_IS_NEW_OP = "is_new_op";
    
    private double memoryValue = 0;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##########");
    private boolean isNewOp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calculator);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        addEvents();
        loadSavedExpression();
    }

    private void saveCurrentExpression() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_EXPRESSION, edtFormular.getText().toString());
        editor.putBoolean(KEY_IS_NEW_OP, isNewOp);
        editor.apply();
    }

    private void loadSavedExpression() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedExpression = prefs.getString(KEY_EXPRESSION, "0");
        isNewOp = prefs.getBoolean(KEY_IS_NEW_OP, true);
        edtFormular.setText(savedExpression);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void addViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtFormular = findViewById(R.id.edtFormular);
        btnDel = findViewById(R.id.btnDel);
        btnC = findViewById(R.id.btnC);
        btnCE = findViewById(R.id.btnCE);
        btnEqual = findViewById(R.id.btnEqual);
    }

    private void addEvents() {
        edtFormular.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                saveCurrentExpression();
            }
        });

        btnDel.setOnClickListener(view -> {
            String currentText = edtFormular.getText().toString();
            if (currentText.equals("Error")) {
                edtFormular.setText("0");
                isNewOp = true;
                return;
            }
            if (currentText.length() > 0 && !currentText.equals("0")) {
                String newText = currentText.substring(0, currentText.length() - 1);
                if (newText.isEmpty() || newText.equals("−")) {
                    newText = "0";
                    isNewOp = true;
                }
                edtFormular.setText(newText);
            }
        });

        btnC.setOnClickListener(view -> {
            edtFormular.setText("0");
            isNewOp = true;
        });

        btnCE.setOnClickListener(view -> {
            edtFormular.setText("0");
            isNewOp = true;
        });

        btnEqual.setOnClickListener(view -> calculate());
    }

    public void processInputData(View view) {
        Button btn = (Button) view;
        String btnText = btn.getText().toString();

        if (isDigit(btnText)) {
            handleDigit(btnText);
        } else if (btnText.equals(".")) {
            handleDecimal();
        } else if (isOperator(btnText)) {
            handleOperator(btnText);
        } else if (isMemoryFunction(btnText)) {
            handleMemoryFunction(btnText);
        } else {
            handleSpecialFunction(btnText);
        }
    }

    private boolean isMemoryFunction(String text) {
        return text.equals("MC") || text.equals("MR") || text.equals("M+") || 
               text.equals("M-") || text.equals("MS") || text.equals("M");
    }

    private void handleMemoryFunction(String func) {
        double currentValue;
        try {
            currentValue = evaluate(edtFormular.getText().toString());
            if (Double.isNaN(currentValue)) return;
        } catch (Exception e) {
            return;
        }
        
        switch (func) {
            case "MC": memoryValue = 0; break;
            case "MR": 
                edtFormular.setText(decimalFormat.format(memoryValue));
                isNewOp = true;
                break;
            case "M+": memoryValue += currentValue; isNewOp = true; break;
            case "M-": memoryValue -= currentValue; isNewOp = true; break;
            case "MS": memoryValue = currentValue; isNewOp = true; break;
            case "M":
                edtFormular.setText(decimalFormat.format(memoryValue));
                isNewOp = true;
                break;
        }
    }

    private boolean isDigit(String text) {
        return text.matches("[0-9]");
    }

    private void handleDigit(String digit) {
        if (isNewOp) {
            edtFormular.setText(digit);
            isNewOp = false;
        } else {
            String current = edtFormular.getText().toString();
            if (current.equals("0")) {
                edtFormular.setText(digit);
            } else {
                edtFormular.setText(current + digit);
            }
        }
    }

    private void handleDecimal() {
        if (isNewOp) {
            edtFormular.setText("0.");
            isNewOp = false;
        } else {
            String current = edtFormular.getText().toString();
            int lastOpIndex = -1;
            for (int i = current.length() - 1; i >= 0; i--) {
                if (isOperator(String.valueOf(current.charAt(i)))) {
                    lastOpIndex = i;
                    break;
                }
            }
            String lastNumber = current.substring(lastOpIndex + 1);
            if (!lastNumber.contains(".")) {
                if (lastNumber.isEmpty()) {
                    edtFormular.setText(current + "0.");
                } else {
                    edtFormular.setText(current + ".");
                }
            }
        }
    }

    private boolean isOperator(String text) {
        return text.equals("+") || text.equals("−") || text.equals("×") || text.equals("÷");
    }

    private void handleOperator(String operator) {
        if (isNewOp) {
            String current = edtFormular.getText().toString();
            if (current.equals("Error")) {
                edtFormular.setText("0");
            }
            isNewOp = false;
        }

        String current = edtFormular.getText().toString();
        if (current.length() > 0) {
            String lastChar = current.substring(current.length() - 1);
            if (isOperator(lastChar)) {
                edtFormular.setText(current.substring(0, current.length() - 1) + operator);
            } else if (!lastChar.equals(".")) {
                edtFormular.setText(current + operator);
            }
        } else {
            edtFormular.setText("0" + operator);
        }
    }

    private void calculate() {
        String expr = edtFormular.getText().toString();
        if (expr.isEmpty() || expr.equals("Error") || expr.equals("0")) return;

        if (expr.length() > 0 && isOperator(expr.substring(expr.length() - 1))) {
            expr = expr.substring(0, expr.length() - 1);
        }

        double result = evaluate(expr);
        if (!Double.isFinite(result)) {
            edtFormular.setText("Error");
        } else {
            edtFormular.setText(decimalFormat.format(result));
        }
        isNewOp = true;
    }

    private double evaluate(String expression) {
        try {
            final String finalExpr = expression.replace("−", "-").replace("×", "*").replace("÷", "/");
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < finalExpr.length()) ? finalExpr.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < finalExpr.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(finalExpr.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private void handleSpecialFunction(String func) {
        double val;
        try {
            val = evaluate(edtFormular.getText().toString());
            if (Double.isNaN(val)) {
                edtFormular.setText("Error");
                isNewOp = true;
                return;
            }
        } catch (Exception e) {
            edtFormular.setText("Error");
            isNewOp = true;
            return;
        }

        double result = 0;
        boolean handled = true;

        switch (func) {
            case "√x":
                if (val >= 0) result = Math.sqrt(val);
                else handled = false;
                break;
            case "x²":
                result = Math.pow(val, 2);
                break;
            case "1/x":
                if (val != 0) result = 1 / val;
                else handled = false;
                break;
            case "%":
                result = val / 100;
                break;
            case "+/-":
                result = val * -1;
                break;
            default:
                handled = false;
                break;
        }

        if (handled) {
            edtFormular.setText(decimalFormat.format(result));
            isNewOp = true;
        } else {
            edtFormular.setText("Error");
            isNewOp = true;
        }
    }
}