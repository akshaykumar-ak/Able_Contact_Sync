package com.example.ablecontactsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Button sync_btn,del_btn;
    String Fname;
    String Lname;
    String number;
    TextView progress;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public String getProgress(int n , int all){
        return Double.toString(((1.0*n)/(1.0*all))*100).substring(0,5);
    }
    public boolean isTodaysData(String dt){
        String ml = dt.substring(18,28);
        long mls = Long.parseLong(ml)*1000;
        String d = new Date(mls).toString();
        String Month = d.substring(4,7);
        String Date = d.substring(8,10);
        String Year = d.substring(29,34);
        String curD = new Date().toString();
        String curMonth = curD.substring(4,7);
        String curDate = curD.substring(8,10);
        String curYear = curD.substring(29,34);
        if(curDate.equals(Date)&&curMonth.equals(Month)&&curYear.equals(Year)){
            return  true;
        }
        return false;
    }

    public void getData(){
        db.collection("Contacts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int c = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int count = task.getResult().size();
                                String ms = Integer.toString(count);
                                Fname = document.get("FirstName").toString();
                                Lname = document.get("LastName").toString();
                                number = Objects.requireNonNull(document.get("Number")).toString();
                                String tm = document.get("addedOn").toString();

                                if(isTodaysData(tm)){
                                    Toast.makeText(MainActivity.this, Fname+"  "+Lname+" Added to Contacts", Toast.LENGTH_SHORT).show();
//                                    try {
//                                        testBatchInsertion();
//                                    } catch (RemoteException | OperationApplicationException e) {
//                                        e.printStackTrace();
//                                    }
                                }
                                c++;
                                progress.setText(getProgress(c,count) + "%");
                                Toast.makeText(MainActivity.this, getProgress(c,count) + "%", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Error Getting Data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void testBatchInsertion() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            generateSampleProviderOperation(ops,Fname,Lname,number);
                this.getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops);
//                ops.clear();
    }
    private void generateSampleProviderOperation(ArrayList<ContentProviderOperation> ops,String Fname,String Lname,String number){
        int backReference = ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                .build()
        );
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReference)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, Fname)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, Lname)
                .build()
        );
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReference)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .build()
            );
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sync_btn = findViewById(R.id.sync_btn);
        del_btn = findViewById(R.id.del_btn);
        progress = findViewById(R.id.progress);

        sync_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
        
        
        del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}