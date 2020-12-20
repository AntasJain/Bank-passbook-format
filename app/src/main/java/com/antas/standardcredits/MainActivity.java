package com.antas.standardcredits;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] banks ={"Select File","HDFC","IDFC","ICICI","AXIS"};
//    List<String> Date=new ArrayList<String>();
//    List<String> Description=new ArrayList<String>();
//    List<String> Debit=new ArrayList<String>();
//    List<String> Credit=new ArrayList<String>();
//    List<String> Currency=new ArrayList<String>();
//  List<String> location=new ArrayList<String>();
    List <String[]> Data=new ArrayList<String[]>();
    Button btn;
    String op=new String();
    InputStream inputStream;
    String item=new String();
    String filler_data[]=new String[8];
    AlertDialog.Builder builder;
    EditText console;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner=findViewById(R.id.spinner);
        btn=findViewById(R.id.convert_btn);
        console=findViewById(R.id.console);
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck==1)
            console.setText("Please Provide Permission to write Data");
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter ad=new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,banks);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);
        filler_data[0]="Date";
        filler_data[1]="Transaction Description";
        filler_data[2]="Debit";
        filler_data[3]="Credit";
        filler_data[4]="Currency";
        filler_data[5]="CardName";
        filler_data[6]="Transaction";
        filler_data[7]="Location";
        Data.add(new String[]{filler_data[0],filler_data[1],filler_data[2],filler_data[3],filler_data[4],filler_data[5],filler_data[6],filler_data[7]});
        Log.e("Data:", Arrays.toString(Data.get(Data.size()-1)));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String outputFIle = "";
                if (item.equals("HDFC")) {
                    outputFIle = "HDFC-Output-Case1.csv";
                    inputStream = getResources().openRawResource(R.raw.hdfc_input_case1);
                }
                else if (item.equals("ICICI")) {
                    outputFIle = "ICICI-Output-Case2.csv";
                    inputStream = getResources().openRawResource(R.raw.icici_input_case2);
                }
                else if (item.equals("IDFC")) {
                    outputFIle = "IDFC-Output-Case4.csv";
                    inputStream=getResources().openRawResource(R.raw.idfc_input_case4);
                }
                else if (item.equals("AXIS")) {
                    outputFIle = "Axis-Output-Case3.csv";
                    inputStream = getResources().openRawResource(R.raw.axis_input_case3);
                }
                //   Toast.makeText(getApplicationContext(),"File Generated: "+outputFIle,Toast.LENGTH_LONG).show();
                StandardizeStatement(inputStream,outputFIle);
            }
        });

    }

    private void StandardizeStatement(InputStream inputFile, String outputFIle) {
        op=console.getText().toString()+"\nStandardization Started...";
        Log.e("OP",op);
        console.setText(op);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputFile));
        try {
            String csvline;

            String transaction=new String();
            String name=new String();
            while ((csvline = br.readLine()) != null) {

                //data=csvline.split(",");
                if(csvline.contains("Domestic"))
                {
                    transaction="Domestic";
                }
                else if(csvline.contains("International"))
                {
                    transaction="International";
                }
                else if(csvline.contains("Rahul")|| csvline.contains("Ritu")||csvline.contains("Raj")||csvline.contains("Rajat"))
                    name=csvline;

                else {
                    String data[] = csvline.split(",");
                    // String regex = "[0-9]+";
                    String regex = "[+-]?[0-9]+(\\.[0-9]+)?([Ee][+-]?[0-9]+)?";
                    Pattern p = Pattern.compile(regex);

                    //String line= data.toString();
                    //Log.e("Error",line);
                    //String innerData[]=line.split(",");
                    String date="0",debit="0",credit="0",desc="0";
                    for (int i = 0; i < data.length; i++) {

                        try {
                            data[i]=data[i].trim();
                            Matcher m = p.matcher(data[i]);
                            if(data[i].equalsIgnoreCase("Date"))
                                continue;
                            else if(data[i].equalsIgnoreCase("Transaction Description"))
                                continue;
                            else if(data[i].equalsIgnoreCase("Amount"))
                                continue;
                            else if(data[i].equalsIgnoreCase("Transaction Details"))
                                continue;
                            else if (m.matches()) {
                                if(outputFIle.contains("ICICI")&& i==3)
                                    credit=data[i];
                                else if(outputFIle.contains("Axis")) {
                                 if (data[i - 1].equalsIgnoreCase(date))
                                        debit = data[i];
                                    else if (data[ i-2].equalsIgnoreCase(date))
                                        credit = data[i];
                                }
                                else
                                debit=data[i];
                            }
                            else if (data[i].contains("-"))
                                // Log.d("Date: ", data[i]);
                                date=data[i];
                            else if (data[i].length() > 10)
                                //Log.d("Transaction Description", data[i]);
                                desc=data[i];
                            else {
                                if (data[i].endsWith("cr") || data[i].endsWith(("Cr")) || data[i].endsWith("CR"))
                                    //Log.d("Credit", data[i]);
                                    credit = data[i];
                            }



                            // Log.d("Transaction ", transaction);
                        } catch (Exception e) {
                            Log.e("Error", e.toString());
                        }
                    }
                    name = name. replaceAll(",", "");
                    if(!date.equals("0"))
                    {   String currency=new String();
                        String cur[]=desc.split("\\s+");
                        String Location=new String();
                        if(transaction.equalsIgnoreCase("Domestic")) {
                            currency = "INR";
                            Location=cur[cur.length-1];

                        }else
                        {
                          currency=cur[cur.length-1];
                         // desc=desc.replace(currency,"");
                            int x=desc.lastIndexOf(currency);
                            String sub=desc.substring(0,x);
                            desc=sub;
                          Location=cur[cur.length-2];
                        }



                        credit=credit.replace("cr","");
                        credit=credit.replace("Cr","");
                        Extract(date,desc,debit,credit,currency,name,transaction,Location.toLowerCase());

                        Log.d("Transaction: ",transaction);

                }}

            }
            WriteCSV(outputFIle);



        } catch (Exception e)

        {
            Log.e("Error",e.toString());
        }
    }

    private void WriteCSV(String outputFIle)throws IOException {
        for(int i=0;i<Data.size();i++)
            System.out.println(Arrays.toString(Data.get(i)));
        String csv=(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+outputFIle);
        CSVWriter writer=null;
        try{
            writer=new CSVWriter(new FileWriter(csv));
            writer.writeAll(Data);
            writer.close();
            builder=new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            //Toast.makeText(getApplicationContext(),"File Saved :"+Environment.getExternalStorageDirectory().getAbsolutePath(),Toast.LENGTH_LONG).show();
            builder.setMessage("File Saved at Location: "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+outputFIle);
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
            op=console.getText().toString()+"\nStandardization Success\nFILE LOCATION: "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+outputFIle;
            console.setText(op);

            //
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
            Log.e("Error:", e.toString());
        }



    }

    private void Extract(String date, String desc, String debit, String credit,String currency, String name, String transaction,String Location) {

//        try {
//            Log.d("Data: ", date + " \n" + desc + " \n" + debit + " \n" + credit + " \n" +currency+" \n"+ name + " \n" + transaction+ " \n" + Location);
//        }
//        catch (Exception e) {
//            Log.e("Error", e.toString());
//        }
        filler_data[0]=date;
        filler_data[1]=desc;
        filler_data[2]=debit;
        filler_data[3]=credit;
        filler_data[4]=currency;
        filler_data[5]=name;
        filler_data[6]=transaction;
        filler_data[7]=Location;
        Data.add(new String[] {filler_data[0],filler_data[1],filler_data[2],filler_data[3],filler_data[4],filler_data[5],filler_data[6],filler_data[7]});

        //Log.e("Data:", Arrays.toString(Data.get(Data.size()-1)));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        item= adapterView.getItemAtPosition(i).toString();
        op="SELECTED: "+item;
       console.setText(op);
        //Toast.makeText(adapterView.getContext(),"Selected: "+item, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}