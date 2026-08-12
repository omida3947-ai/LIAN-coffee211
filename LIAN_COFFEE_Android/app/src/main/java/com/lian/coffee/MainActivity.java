package com.lian.coffee;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    DB db;
    TextView income, expense, profit;

    String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.BLACK);
        db = new DB(this);
        setContentView(R.layout.activity_main);

        income = findViewById(R.id.txtIncome);
        expense = findViewById(R.id.txtExpense);
        profit = findViewById(R.id.txtProfit);

        findViewById(R.id.btnSale).setOnClickListener(v -> addTransaction("income"));
        findViewById(R.id.btnExpense).setOnClickListener(v -> addTransaction("expense"));
        findViewById(R.id.btnInventory).setOnClickListener(v -> inventoryDialog());
        findViewById(R.id.btnReport).setOnClickListener(v -> reportDialog());
        findViewById(R.id.btnBackup).setOnClickListener(v -> backupDialog());

        refresh();
    }

    void refresh() {
        double i = db.total("income", today());
        double e = db.total("expense", today());
        income.setText("درآمد امروز\n" + money(i));
        expense.setText("هزینه امروز\n" + money(e));
        profit.setText("سود / ضرر امروز: " + money(i-e));
    }

    String money(double x) {
        return String.format(Locale.US, "%,.0f تومان", x);
    }

    EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(2);
        return e;
    }

    void addTransaction(String type) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        EditText title = new EditText(this); title.setHint("عنوان");
        EditText amount = input("مبلغ به تومان");
        box.addView(title); box.addView(amount);

        new AlertDialog.Builder(this)
            .setTitle(type.equals("income") ? "ثبت فروش / درآمد" : "ثبت هزینه / خروجی")
            .setView(box)
            .setPositiveButton("ثبت", (d,w) -> {
                try {
                    db.addTransaction(type, title.getText().toString(),
                        Double.parseDouble(amount.getText().toString()), today());
                    refresh();
                } catch(Exception ex) {
                    Toast.makeText(this, "مبلغ را صحیح وارد کنید", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("لغو", null).show();
    }

    void inventoryDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        EditText name = new EditText(this); name.setHint("نام کالا");
        EditText qty = input("موجودی");
        EditText min = input("حداقل موجودی");
        EditText buy = input("قیمت خرید");
        EditText sell = input("قیمت فروش");
        box.addView(name); box.addView(qty); box.addView(min); box.addView(buy); box.addView(sell);

        new AlertDialog.Builder(this).setTitle("افزودن کالا به انبار")
            .setView(box)
            .setPositiveButton("ثبت کالا", (d,w) -> {
                try {
                    db.addProduct(name.getText().toString(),
                        Double.parseDouble(qty.getText().toString()),
                        Double.parseDouble(min.getText().toString()),
                        Double.parseDouble(buy.getText().toString()),
                        Double.parseDouble(sell.getText().toString()));
                    showInventory();
                } catch(Exception ex) {
                    Toast.makeText(this, "اطلاعات کالا را صحیح وارد کنید", Toast.LENGTH_SHORT).show();
                }
            })
            .setNeutralButton("مشاهده انبار", (d,w) -> showInventory())
            .setNegativeButton("لغو", null).show();
    }

    void showInventory() {
        android.database.Cursor c = db.inventory();
        StringBuilder s = new StringBuilder();
        while(c.moveToNext()) {
            String n=c.getString(1); double q=c.getDouble(2), m=c.getDouble(3);
            double bp=c.getDouble(4), sp=c.getDouble(5);
            s.append(n).append("\n")
             .append("موجودی: ").append(q)
             .append(" | حداقل: ").append(m)
             .append("\nخرید: ").append(money(bp))
             .append(" | فروش: ").append(money(sp))
             .append("\nسود هر واحد: ").append(money(sp-bp))
             .append(q <= m ? "\n⚠️ موجودی کم\n" : "\n")
             .append("--------------------\n");
        }
        c.close();
        if(s.length()==0) s.append("هنوز کالایی ثبت نشده است.");
        new AlertDialog.Builder(this).setTitle("انبار LIAN").setMessage(s.toString())
            .setPositiveButton("باشه", null).show();
    }

    void reportDialog() {
        android.database.Cursor c = db.transactions();
        StringBuilder s = new StringBuilder("گزارش تراکنش‌ها\n\n");
        while(c.moveToNext()) {
            String type=c.getString(0), title=c.getString(1), date=c.getString(3);
            double amount=c.getDouble(2);
            s.append(type.equals("income") ? "درآمد" : "هزینه")
             .append(" | ").append(title).append(" | ")
             .append(money(amount)).append(" | ").append(date).append("\n");
        }
        c.close();
        s.append("\n\nامروز:\nدرآمد: ").append(money(db.total("income", today())))
         .append("\nهزینه: ").append(money(db.total("expense", today())))
         .append("\nسود/ضرر: ").append(money(db.total("income", today())-db.total("expense", today())));
        new AlertDialog.Builder(this).setTitle("گزارش مالی").setMessage(s.toString())
            .setPositiveButton("باشه", null).show();
    }

    void backupDialog() {
        Toast.makeText(this, "پشتیبان‌گیری در نسخه بعدی به فایل Excel/CSV اضافه می‌شود.", Toast.LENGTH_LONG).show();
    }
}
